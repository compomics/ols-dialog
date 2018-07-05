package uk.ac.ebi.pride.toolsuite.ols.dialog.task;

import net.jcip.annotations.GuardedBy;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 09/07/2017.
 */
public abstract class AbstractTask<T, R>  extends SwingWorker<T, R> {

    protected OLSDialog olsDialog;

    protected OLSClient olsClient;

    protected String nameTask;

    public static final String COMPLETED_PROP = "completed";

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractTask.class);


    @GuardedBy("ownersLock")
    private final Collection<Object> owners;

    // lock for task listeners collection
    private final Object taskListenersLock = new Object();
    @GuardedBy("taskListenersLock")
    private final Collection<TaskListener<T, R>> taskListeners;



    public AbstractTask(String nameTask, OLSDialog olsDialog, OLSClient olsClient) {
        this.olsDialog = olsDialog;
        this.olsClient = olsClient;
        this.nameTask  = nameTask;
        taskListeners = Collections.synchronizedList(new ArrayList<TaskListener<T, R>>());
        addPropertyChangeListener(new TaskStateMonitor());
        owners = Collections.synchronizedList(new ArrayList<>());
    }

    public String getNameTask() {
        return nameTask;
    }

    public void setNameTask(String nameTask) {
        this.nameTask = nameTask;
    }

    public void addTaskListener(TaskListener<T, R> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null Task Listener");
        }

        synchronized (taskListenersLock) {
            taskListeners.add(listener);
        }
    }

    public void removeTaskListener(TaskListener<T, R> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Null Task Listener");
        }

        synchronized (taskListenersLock) {
            taskListeners.remove(listener);
        }
    }

    public boolean hasTaskListener(TaskListener<T, R> listener) {
        synchronized (taskListenersLock) {
            return taskListeners.contains(listener);
        }
    }

    public Collection<TaskListener<T, R>> getTaskListeners() {
        synchronized (taskListenersLock) {
            return new ArrayList<>(taskListeners);
        }
    }

    /**
     * Check whether task has a property change listener
     *
     * @param listener property change listener
     * @return boolean true mean exist
     */
    public synchronized boolean hasPropertyChangeListener(PropertyChangeListener listener) {
        for (PropertyChangeListener propertyChangeListener : this.getPropertyChangeSupport().getPropertyChangeListeners()) {
            if (propertyChangeListener.equals(listener)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if the state of the task is pending.
     *
     * @return boolean  true means pending
     */
    public final boolean isPending() {
        return getState() == StateValue.PENDING;
    }

    /**
     * Return true if the state of the task is started
     *
     * @return boolean  true means pending
     */
    public final boolean isStarted() {
        return getState() == StateValue.STARTED;
    }

    private class TaskStateMonitor implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();

            if ("state".equals(propName)) {
                StateValue state = (StateValue) (evt.getNewValue());
                switch (state) {
                    case STARTED:
                        taskStarted();
                        break;
                    case DONE:
                        taskDone();
                        break;
                }
            } else if ("progress".equals(propName)) {
                fireProgressListeners(getProgress());
            }
        }

        /**
         * Called when task started
         */
        private void taskStarted() {
            fireStartedListeners();
        }

        /**
         * Called when task is done
         */
        private void taskDone() {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (isCancelled())
                            cancelled();
                        else
                            succeed(get());
                    } catch (InterruptedException iex) {
                        interrupted(iex);
                    } catch (ExecutionException eex) {
                        failed(eex.getCause());
                    } finally {
                        finished();
                        try {
                            fireCompletionListeners();
                        } finally {
                            firePropertyChange(COMPLETED_PROP, false, true);
                        }
                    }
                }
            });
        }
    }

    private void fireStartedListeners() {
        TaskEvent<Void> event = new TaskEvent<>(this, null);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.started(event);
            }
        }
    }

    private void fireProcessListeners(java.util.List<R> values) {
        TaskEvent<java.util.List<R>> event = new TaskEvent<>(this, values);
        synchronized (taskListenersLock) {
            for (TaskListener<T, R> listener : taskListeners) {
                listener.process(event);
            }
        }
    }
    protected abstract void cancelled();
    protected abstract void succeed(T results);
    /**
     * finished method is called by SwingWorker's done method
     */
    protected abstract void finished();

    protected abstract void interrupted(InterruptedException iex);

    private void fireCompletionListeners() {
        try {
            if (isCancelled())
                fireCancelledListeners();
            else
                fireSucceedListeners(get());
        } catch (InterruptedException iex) {
            fireInterruptedListeners(iex);
        } catch (ExecutionException eex) {
            fireFailedListeners(eex.getCause());
        } finally {
            fireFinishedListeners();
        }
    }

    private void fireCancelledListeners() {
        TaskEvent<Void> event = new TaskEvent<>(this, null);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.cancelled(event);
            }
        }
    }

    private void fireInterruptedListeners(InterruptedException iex) {
        TaskEvent<InterruptedException> event = new TaskEvent<>(this, iex);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.interrupted(event);
            }
        }
    }

    private void fireSucceedListeners(T result) {
        TaskEvent<T> event = new TaskEvent<>(this, result);
        synchronized (taskListenersLock) {
            for (TaskListener<T, R> listener : taskListeners) {
                listener.succeed(event);
            }
        }
    }

    private void fireFailedListeners(Throwable error) {
        TaskEvent<Throwable> event = new TaskEvent<>(this, error);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.failed(event);
            }
        }
    }

    private void fireFinishedListeners() {
        TaskEvent<Void> event = new TaskEvent<>(this, null);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.finished(event);
            }
        }
    }

    private void fireProgressListeners(int progress) {
        TaskEvent<Integer> event = new TaskEvent<>(this, progress);
        synchronized (taskListenersLock) {
            for (TaskListener listener : taskListeners) {
                listener.progress(event);
            }
        }
    }



    /**
     * failed method is called by done method from SwingWorker when the task has failed.
     *
     * @param error Throwable generated by failed task
     */
    protected void failed(Throwable error) {
        String msg = String.format("%s failed on : %s", this, error);
        logger.error(msg, error);
    }
}
