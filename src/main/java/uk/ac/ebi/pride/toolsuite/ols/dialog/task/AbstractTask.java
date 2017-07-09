package uk.ac.ebi.pride.toolsuite.ols.dialog.task;

import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;

import javax.swing.*;

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

    JProgressBar progressBar;

    protected OLSClient olsClient;

    public AbstractTask(JProgressBar progressBar, OLSClient olsClient) {
        this.progressBar = progressBar;
        this.olsClient = olsClient;
    }
}
