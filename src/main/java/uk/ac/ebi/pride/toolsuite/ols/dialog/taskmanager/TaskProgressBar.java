package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.TaskEvent;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.TaskListener;

import javax.swing.*;
import java.util.List;

public class TaskProgressBar extends JProgressBar implements TaskListener<Object, Object> {

    public TaskProgressBar(AbstractTask task) {
        super();
        task.addTaskListener(this);

        this.setString(task.getNameTask());
        this.setIndeterminate(true);
        this.setStringPainted(true);
    }

    @Override
    public void started(TaskEvent<Void> event) {
    }

    @Override
    public void process(TaskEvent<List<Object>> taskEvent) {
        List<Object> values = taskEvent.getValue();
        for (Object value : values) {
            if (value instanceof String)
                updateMessage((String) value);
        }
    }

    @Override
    public void finished(TaskEvent<Void> event) {
        this.setIndeterminate(false);
    }

    @Override
    public void failed(TaskEvent<Throwable> event) {
        updateMessage("Failed!");
    }

    @Override
    public void succeed(TaskEvent<Object> taskEvent) {
        updateMessage("Succeed!");
    }

    @Override
    public void cancelled(TaskEvent<Void> event) {
        updateMessage("Cancelled!");
    }

    @Override
    public void interrupted(TaskEvent<InterruptedException> iex) {
        updateMessage("Interrupted!");
    }

    @Override
    public void progress(TaskEvent<Integer> progress) {
    }

    /**
     * Thread safe message update.
     *
     * @param msg message.
     */
    private void updateMessage(final String msg) {
        Runnable eventDispatcher = new Runnable() {
            public void run() {
                TaskProgressBar.this.setString(msg);
            }
        };
    }

}
