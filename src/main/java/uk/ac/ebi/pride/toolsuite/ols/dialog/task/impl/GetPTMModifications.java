package uk.ac.ebi.pride.toolsuite.ols.dialog.task.impl;

import uk.ac.ebi.pride.toolsuite.ols.dialog.model.MassSearchModel;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class search in the background for PTMs with specific parameters.
 *
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 09/07/2017.
 */
public class GetPTMModifications extends AbstractTask {

    private MassSearchModel model;

    private String massDeltaType;

    private double fromMass;

    private double toMass;

    /**
     * Default Constructor
     * @param progressBar progress bar
     * @param olsClient OLS client
     */
    private GetPTMModifications(JProgressBar progressBar, OLSClient olsClient) {
        super(progressBar, olsClient);
    }

    public GetPTMModifications(JProgressBar progressBar, OLSClient olsclient,
                               MassSearchModel model, String massType, double fromMass, double toMass){
        this(progressBar, olsclient);
        this.model = model;
        this.massDeltaType = massType;
        this.fromMass = fromMass;
        this.toMass = toMass;
    }

    @Override
    protected Object doInBackground() throws Exception {
        List<Term> result;
        result = olsClient.getTermsByAnnotationData("mod", massDeltaType, fromMass, toMass);
        for(Term term: result){
            model.addRow(term, massDeltaType);
        }
        return result;
    }
}
