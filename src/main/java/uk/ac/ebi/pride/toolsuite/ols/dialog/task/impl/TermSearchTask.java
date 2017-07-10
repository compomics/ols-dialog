package uk.ac.ebi.pride.toolsuite.ols.dialog.task.impl;

import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import javax.swing.table.DefaultTableModel;
import java.util.List;
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
 * Created by ypriverol (ypriverol@gmail.com) on 09/07/2017.
 */
public class TermSearchTask extends AbstractTask{


    private String ontologyName;

    private Map<String, List<Identifier>> preselectedOntologies;

    private String term;

    private  boolean keyReverse;

    private Integer numTerms = 0;

    private static String TASK_NAME = "Search Terms by Name";

    public TermSearchTask(OLSDialog olsDialog, OLSClient olsClient) {
        super(TASK_NAME, olsDialog, olsClient);
    }

    /**
     * Search in a subset of ontologies.
     * @param olsDialog
     * @param olsClient
     * @param term
     * @param keyReverse
     * @param preselectedOntologies
     */
    public TermSearchTask(OLSDialog olsDialog, OLSClient olsClient, String term, boolean keyReverse, Map<String, List<Identifier>> preselectedOntologies) {
        super(TASK_NAME, olsDialog, olsClient);
        this.preselectedOntologies = preselectedOntologies;
        this.term = term;
        this.keyReverse = keyReverse;
    }

    /**
     * Search in all ontologies
     * @param olsDialog
     * @param olsClient
     * @param term
     * @param keyReverse
     */
    public TermSearchTask(OLSDialog olsDialog, OLSClient olsClient, String term, boolean keyReverse) {
        super(TASK_NAME, olsDialog, olsClient);
        this.term = term;
        this.keyReverse = keyReverse;
    }

    /**
     * Search in only one ontology
     * @param olsDialog
     * @param olsClient
     * @param term
     * @param keyReverse
     * @param ontology
     */
    public TermSearchTask(OLSDialog olsDialog, OLSClient olsClient, String term, boolean keyReverse, String ontology) {
        super(TASK_NAME, olsDialog, olsClient);
        this.term = term;
        this.keyReverse = keyReverse;
        this.ontologyName = ontology;
    }

    @Override
    protected void done() {
        Integer width = olsDialog.getPreferredColumnWidth(olsDialog.olsResultsTermNameSearchJTable, olsDialog.olsResultsTermNameSearchJTable.getColumn("Accession").getModelIndex(), 6);
        if (width != null) {
            olsDialog.olsResultsTermNameSearchJTable.getColumn("Accession").setMinWidth(width);
            olsDialog.olsResultsTermNameSearchJTable.getColumn("Accession").setMaxWidth(width);
        } else {
            olsDialog.olsResultsTermNameSearchJTable.getColumn("Accession").setMinWidth(15);
            olsDialog.olsResultsTermNameSearchJTable.getColumn("Accession").setMaxWidth(Integer.MAX_VALUE);
        }
        olsDialog.termNameSearchJTextField.requestFocus();
        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        olsDialog.termNameSearchJTextField.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        olsDialog.numberOfTermsTermNameSearchJTextField.setText("" + numTerms);
        if (olsDialog.olsResultsTermNameSearchJTable.getRowCount() > 0) {
            olsDialog.olsResultsTermNameSearchJTable.scrollRectToVisible(olsDialog.olsResultsTermNameSearchJTable.getCellRect(0, 0, false));
        }

    }

    @Override
    protected Object doInBackground() throws Exception {
        if(preselectedOntologies != null && !preselectedOntologies.isEmpty()){
            for (String preselectedOntology : preselectedOntologies.keySet()) {
                List<Term> results = olsClient.getTermsByName(term, preselectedOntology.toLowerCase(), keyReverse);
                if(results != null && !results.isEmpty()){
                    for(Term key: results)
                        ((DefaultTableModel) olsDialog.olsResultsTermNameSearchJTable.getModel()).addRow(new Object[]{key, key});
                }
                numTerms = results.size();
            }
        }else if(ontologyName != null){
            List<Term> results = olsClient.getTermsByName(term, ontologyName.toLowerCase(), keyReverse);
            if(results != null && !results.isEmpty()){
                for(Term key: results)
                    ((DefaultTableModel) olsDialog.olsResultsTermNameSearchJTable.getModel()).addRow(new Object[]{key, key});
            }
            numTerms = results.size();
        }else{
            List<Ontology> ontologies = olsClient.getOntologies();
            numTerms = 0;
            for (Ontology preselectedOntology : ontologies) {
                List<Term> results = olsClient.getTermsByName(term, preselectedOntology.getConfig().getPreferredPrefix().toLowerCase(), keyReverse);
                if(results != null && !results.isEmpty()){
                    for(Term key: results)
                        ((DefaultTableModel) olsDialog.olsResultsTermNameSearchJTable.getModel()).addRow(new Object[]{key, key});
                }
                numTerms += results.size();
            }

        }
        return null;
    }
}
