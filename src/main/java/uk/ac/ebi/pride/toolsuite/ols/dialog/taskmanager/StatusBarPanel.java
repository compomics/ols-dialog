package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
public abstract class StatusBarPanel extends JPanel implements PropertyChangeListener {

    private int panelWidth;
    private boolean isFixedWidth;

    public StatusBarPanel() {
        this(-1, false);
    }

    public StatusBarPanel(int width, boolean fixed) {
        super();
        this.panelWidth = width < -1 ? -1 : width;
        this.isFixedWidth = fixed;
    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public boolean isFixedWidth() {
        return isFixedWidth;
    }

    public void setFixedWidth(boolean fixedWidth) {
        isFixedWidth = fixedWidth;
    }

    @Override
    public abstract void propertyChange(PropertyChangeEvent evt);
}
