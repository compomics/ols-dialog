package uk.ac.ebi.pride.toolsuite.ols.dialog.task;

import java.util.List;


public interface TaskListener<T, V> {
    /**
     * Called before the Task's <code> doInBackground </code>
     * method is called.
     *
     * @param event a TaskEvent whose source is the Task object.
     */
    void started(TaskEvent<Void> event);
    void process(TaskEvent<List<V>> event);
    void finished(TaskEvent<Void> event);
    void failed(TaskEvent<Throwable> event);
    void succeed(TaskEvent<T> event);
    void cancelled(TaskEvent<Void> event);
    void interrupted(TaskEvent<InterruptedException> iex);
    void progress(TaskEvent<Integer> progress);
}
