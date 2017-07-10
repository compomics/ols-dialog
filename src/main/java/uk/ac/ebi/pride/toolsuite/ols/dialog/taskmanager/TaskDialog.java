package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.toolsuite.ols.dialog.util.GUIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

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
 * Created by ypriverol (ypriverol@gmail.com) on 10/07/2017.
 */

public class TaskDialog extends JDialog implements PropertyChangeListener {
    /**
     * the title of the dialog
     */
    private static final String TASK_DIALOG_TITLE = "Background Tasks";

    /**
     * main pane contains all the ongoing task panels
     */
    private JPanel mainPane;

    /**
     * a map of task panels, each task panel is created using createTaskPanel() method
     */
    private Map<AbstractTask, JPanel> taskPanelMap;

    /**
     * PRIDE Inspector context
     */
    private OLSDialog olsDialog;


    public TaskDialog(OLSDialog olsDialog) {
        super(olsDialog, TASK_DIALOG_TITLE);

        this.olsDialog = olsDialog;

        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(570, 400));

        // main panel
        mainPane = new JPanel();
        mainPane.setLayout(new BoxLayout(mainPane, BoxLayout.Y_AXIS));
        mainPane.setBackground(Color.white);
        mainPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // scroll pane
        JScrollPane scrollPane = new JScrollPane(mainPane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, BorderLayout.CENTER);

        // todo: add icon
        this.taskPanelMap = new HashMap<>();

        // set display location
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((d.width - getWidth())/2, (d.height - getHeight())/2);

        // add itself as a listener to task manager
        TaskManager taskMgr = olsDialog.getTaskManager();
        taskMgr.addPropertyChangeListener(this);
    }

    /**
     * Create a JPanel which shows the name of the task
     * and progress.
     *
     * @param task a background running task
     * @return JPanel a panel which cotains a JLabel and a progress bar
     */
    private JPanel createTaskPanel(AbstractTask task) {
        JPanel displayPane = new JPanel();
        displayPane.setOpaque(false);
        displayPane.setLayout(new BorderLayout());
        displayPane.setMaximumSize(new Dimension(520, 80));

        // get title
        String title = getTaskPanelTitle(task);
        CollapsiblePane panel = new CollapsiblePane(title);

        // progress bar panel
        JPanel progBarPanel = new JPanel();
        progBarPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        progBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // add progress bar
        TaskProgressBar progBar = new TaskProgressBar(task);
        progBar.setPreferredSize(new Dimension(440, 20));
        progBarPanel.add(progBar);

        // add close button
        Icon icon = GUIUtils.loadIcon("icons/cancel.png");
        JButton closeButton = new JButton(icon);
        closeButton.addActionListener(new CloseTaskListener(task, olsDialog));
        progBarPanel.add(closeButton);

        panel.setContentComponent(progBarPanel);

        // add collapsible pane
        displayPane.add(panel, BorderLayout.CENTER);

        return displayPane;
    }

    /**
     * Get the title for the task panel
     * @param task  task
     * @return  String task panel title
     */
    private String getTaskPanelTitle(AbstractTask task) {
        String title = task.getNameTask();
        return title;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void propertyChange(PropertyChangeEvent evt) {
        String eventName = evt.getPropertyName();
        if (TaskManager.ADD_TASK_PROP.equals(eventName)) {
            java.util.List<AbstractTask> newTasks = (java.util.List<AbstractTask>) evt.getNewValue();
            // get the newest task
            AbstractTask newTask = newTasks.get(newTasks.size() - 1);
            // 1. create a new progress bar
            JPanel taskPanel = createTaskPanel(newTask);
            // display the newest task
            mainPane.add(taskPanel);
            mainPane.revalidate();
            mainPane.repaint();
            this.repaint();
            // register the mapping
            taskPanelMap.put(newTask, taskPanel);
        } else if (TaskManager.REMOVE_TASK_PROP.equals(eventName)) {
            java.util.List<AbstractTask> oldTasks = (java.util.List<AbstractTask>) evt.getOldValue();
            java.util.List<AbstractTask> newTasks = (java.util.List<AbstractTask>) evt.getNewValue();
            oldTasks.removeAll(newTasks);
            for (AbstractTask task : oldTasks) {
                JPanel taskPanel = taskPanelMap.get(task);
                if (taskPanel != null) {
                    mainPane.remove(taskPanel);
                    mainPane.revalidate();
                    mainPane.repaint();

                    // remove from memory
                    taskPanelMap.remove(task);
                }
            }
            this.repaint();
        }
    }

    private static class CloseTaskListener implements ActionListener {
        private AbstractTask task;
        private OLSDialog olsDialog;

        private CloseTaskListener(AbstractTask task, OLSDialog olsDialog) {
            this.task = task;
            this.olsDialog = olsDialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            olsDialog.cancelTask(task, true);
        }
    }
}