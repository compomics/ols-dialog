package uk.ac.ebi.pride.toolsuite.ols.dialog.taskmanager;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

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

public class StatusBar extends JToolBar {

    private StatusBarPanel[] panels;

    public StatusBar(StatusBarPanel... panels) {

        this.setFloatable(false);
        this.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 1));
        this.setBorder(BorderFactory.createEtchedBorder());
        this.panels = panels;
        for (StatusBarPanel panel : panels) {
            this.add(new Separator(new Dimension(1, 20)));
            this.add(panel);
        }
    }

    public StatusBarPanel[] getPanels() {
        return panels == null ? null : Arrays.copyOf(panels, panels.length);
    }

    public void setPanels(StatusBarPanel[] panels) {
        this.panels = (panels == null ? null : Arrays.copyOf(panels, panels.length));
    }
}