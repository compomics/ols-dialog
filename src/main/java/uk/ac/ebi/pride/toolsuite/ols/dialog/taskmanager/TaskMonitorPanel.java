package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;


import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;

/**
 */
public class TaskMonitorPanel extends StatusBarPanel {
    /**
     * Task progress bar indicates there is a background running task when appears
     */
    private JProgressBar taskProgressBar;

    /**
     * dialog for displaying all the ongoing tasks
     */
    private JDialog taskProgressDialog;


    public TaskMonitorPanel(OLSDialog olsDialog) {

        super(0, true);

        this.setLayout(new BorderLayout());

        this.setOpaque(false);

        this.setPreferredSize(new Dimension(250, 25));

        // create a dialog to display all ongoing tasks
        taskProgressDialog = new TaskDialog(olsDialog);
        taskProgressDialog.setVisible(false);

        // create the task progress bar
        taskProgressBar = new JProgressBar();
        // set to fixed size
        taskProgressBar.setIndeterminate(true);
        taskProgressBar.setStringPainted(true);
        taskProgressBar.setToolTipText("Click to show/hide background tasks window");
        taskProgressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                taskProgressDialog.setVisible(true);
            }
        });
        // the the progress bar
        taskProgressBar.setVisible(false);
        this.add(taskProgressBar, BorderLayout.CENTER);

        // add itself as a listener to task manager
        TaskManager taskMgr = olsDialog.getTaskManager();
        taskMgr.addPropertyChangeListener(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        String eventName = evt.getPropertyName();
        if (TaskManager.ADD_TASK_PROP.equals(eventName)) {
            List<AbstractTask> newTasks = (List< AbstractTask>) evt.getNewValue();
            if (!newTasks.isEmpty()) {
                // add the new task
                AbstractTask newTask = newTasks.get(newTasks.size() - 1);
                taskProgressBar.setString(newTask.getNameTask());

                // display the newest task
                taskProgressBar.setVisible(true);
            }
        } else if (TaskManager.REMOVE_TASK_PROP.equals(eventName)) {
            List<AbstractTask> newTasks = (List<AbstractTask>) evt.getNewValue();

            // when there is no ongoing tasks
            if (newTasks.isEmpty()) {
                taskProgressBar.setVisible(false);
            } else {
                AbstractTask newTask = newTasks.get(newTasks.size() - 1);
                taskProgressBar.setString(newTask.getNameTask());
            }
        }
    }
}
