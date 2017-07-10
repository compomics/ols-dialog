package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import uk.ac.ebi.pride.toolsuite.ols.dialog.prop.PropertyChangeHelper;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.TaskListener;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * TaskManager acts as a thread pool, it does the followings:
 * <p/>
 * 1. maintain a list of Tasks
 * <p/>
 * 2. manage a queue of Tasks
 * <p/>
 */
@ThreadSafe
public class TaskManager extends PropertyChangeHelper {
    /**
     * property change event name, this is fired when a new task is added
     */
    public final static String ADD_TASK_PROP = "add_new_task";

    /**
     * property change event name, this is fired when a task is removed
     */
    public final static String REMOVE_TASK_PROP = "remove_new_task";

    /**
     * Threshold pool executor, it is responsible to running all the tasks
     */
    private final ExecutorService executor;

    /**
     * task list lock
     */
    private final Object tasksLock = new Object();

    /**
     * A list of current ongoing tasks
     */
    @GuardedBy("tasksLock")
    private final List<AbstractTask> tasks;

    /**
     * property change listener
     */
    private final PropertyChangeListener taskPropListener;

    private final int numberOfThreads = 20;


    /**
     * Constructor
     */
    public TaskManager() {

        // thread pool
        this.executor = new ThreadPoolExecutor(5, numberOfThreads,
                                               0L, TimeUnit.MILLISECONDS,
                                               new LinkedBlockingQueue<Runnable>());

        // a list of tasks
        this.tasks = new CopyOnWriteArrayList<>();

        // internal property change listener
        this.taskPropListener = new TaskPropertyListener();
    }

    /**
     * Add a new task to the task manager, hence the thread pool.
     * <p/>
     * Notify any listeners listen to the task manager
     *
     * @param task new task
     */
    public void addTask(AbstractTask task) {
        addTask(task, true);
    }

    /**
     * Add a new task to the task manager, you can choose whether to notify
     * the task manager listeners, for example, if false, the status bar will
     * not change.
     *
     * @param task   new task
     * @param notify choose whether to notify
     */
    public void addTask(AbstractTask task, boolean notify) {
        // add task the task list
        List<AbstractTask> oldTasks, newTasks;
        synchronized (tasksLock) {
            oldTasks = new ArrayList<>(tasks);
            tasks.add(task);
            newTasks = new ArrayList<>(tasks);
            task.addPropertyChangeListener(taskPropListener);
        }

        // notify the status bar
        if (notify) {
            firePropertyChange(ADD_TASK_PROP, oldTasks, newTasks);
        }
        executor.execute(task);
    }

    /**
     * Return a list of Tasks which has the specified TaskListener.
     *
     * @param listener Task listener.
     * @return List<Task>   a list of tasks.
     */
    @SuppressWarnings("unchecked")
    public List<AbstractTask> getTasks(TaskListener listener) {
        List<AbstractTask> ts = new ArrayList<>();

        synchronized (tasksLock) {
            for (AbstractTask task : tasks) {
                if (task.hasTaskListener(listener)) {
                    ts.add(task);
                }
            }
        }

        return ts;
    }

    /**
     * Return a list of tasks which has the specified property change listener
     *
     * @param listener property change listener
     * @return List<Task>   a list of tasks
     */
    public List<AbstractTask> getTasks(PropertyChangeListener listener) {
        List<AbstractTask> ts = new ArrayList<>();

        synchronized (tasksLock) {
            for (AbstractTask task : tasks) {
                if (task.hasPropertyChangeListener(listener)) {
                    ts.add(task);
                }
            }
        }

        return ts;
    }

    /**
     * Return as list of tasks which is the specified task class type.
     *
     * @param taskClass task class type
     * @return List<Task>   a list of matching tasks
     */
    public List<AbstractTask> getTasks(Class<? extends AbstractTask> taskClass) {
        List<AbstractTask> ts = new ArrayList<>();

        synchronized (tasksLock) {
            for (AbstractTask task : tasks) {
                if (task.getClass().equals(taskClass)) {
                    ts.add(task);
                }
            }
        }

        return ts;
    }

    /**
     * Check whether the task is already registered with task manager
     *
     * @param task task
     * @return boolean  true if the task is in task manager
     */
    public boolean hasTask(AbstractTask task) {
        synchronized (tasksLock) {
            return tasks.contains(task);
        }
    }

    /**
     * Remove a task listener from all the ongoing tasks.
     *
     * @param listener task listener
     */
    @SuppressWarnings("unchecked")
    public void removeTaskListener(TaskListener listener) {
        synchronized (tasksLock) {
            for (AbstractTask task : tasks) {
                task.removeTaskListener(listener);
            }
        }
    }

    /**
     * Stop task. If it is in task manager then it will be removed from the TaskManager,
     * all the TaskListeners assigned to this Task will also be deleted.
     * all the property change listeners assigned to thia task will be deleted
     *
     * @param task      task
     * @param interrupt whether to notify
     * @return boolean  true is the cancel has been finished.
     */
    @SuppressWarnings("unchecked")
    public boolean cancelTask(AbstractTask task, boolean interrupt) {
        boolean canceled = false;

        // remove task from task manager
        synchronized (tasksLock) {
            boolean hasTask = hasTask(task);
            if (hasTask) {
                // cancel all the children tasks first
                //cancelTasksByOwner(task);

                List<AbstractTask> oldTasks, newTasks;
                oldTasks = new ArrayList<>(tasks);
                tasks.remove(task);
                canceled = task.cancel(interrupt);
                newTasks = new ArrayList<>(tasks);
                task.removePropertyChangeListener(taskPropListener);

                firePropertyChange(REMOVE_TASK_PROP, oldTasks, newTasks);
            }
        }

        return canceled;
    }

    /**
     * orderly shutdown, all existing tasks are allowed to finish
     * no task is submitted.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * attempt to stop all running tasks at once
     */
    public void shutdownNow() {
        executor.shutdownNow();
    }

    /**
     * Internal task listener, listens to any completion of the task,
     * if yes, then remove the task from task list and fire a remove task property change event.
     */
    private class TaskPropertyListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (AbstractTask.COMPLETED_PROP.equals(propName)) {
                AbstractTask task = (AbstractTask) evt.getSource();
                List<AbstractTask> oldTasks, newTasks;

                synchronized (tasksLock) {
                    oldTasks = new ArrayList<>(tasks);
                    tasks.remove(task);
                    // remove all the children tasks too
                   // TaskManager.this.cancelTasksByOwner(task);
                    newTasks = new ArrayList<>(tasks);
                    task.removePropertyChangeListener(taskPropListener);
                }

                firePropertyChange(REMOVE_TASK_PROP, oldTasks, newTasks);


            }
        }
    }
}
