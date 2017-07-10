package uk.ac.ebi.pride.toolsuite.ols.dialog.task;

import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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

    private final Object taskListenersLock = new Object();

    private final Collection<TaskListener<T, R>> taskListeners;

    public static final String COMPLETED_PROP = "completed";

    public AbstractTask(String nameTask, OLSDialog olsDialog, OLSClient olsClient) {
        this.olsDialog = olsDialog;
        this.olsClient = olsClient;
        this.nameTask  = nameTask;
        taskListeners = Collections.synchronizedList(new ArrayList<TaskListener<T, R>>());
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
}
