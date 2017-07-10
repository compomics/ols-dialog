package uk.ac.ebi.pride.toolsuite.ols.dialog.task.impl;

import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.ITerm;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import javax.swing.table.DefaultTableModel;
import java.util.Iterator;
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
 * Created by ypriverol (ypriverol@gmail.com) on 10/07/2017.
 */
public class GetMetadataTask extends AbstractTask{


    private Integer searchType;
    private String ontologyName;

    private ITerm term;

    private static String TASK_NAME = "Get Metadata for Term";


    public GetMetadataTask(OLSDialog olsDialog, OLSClient olsClient) {
        super(TASK_NAME, olsDialog, olsClient);
    }

    /**
     * Search in a subset of ontologies.
     * @param olsDialog
     * @param olsClient
    **/
    public GetMetadataTask(OLSDialog olsDialog, OLSClient olsClient, ITerm term, String ontologyName, Integer searchType) {
        super(TASK_NAME, olsDialog, olsClient);
        this.ontologyName = ontologyName;
        this.term = term;
        this.searchType = searchType;
    }


    @Override
    protected void done() {

    }

    @Override
    protected Object doInBackground() throws Exception {

        List<String> metadata = olsClient.getTermDescription(term.getGlobalId(), ontologyName);
        String label = term.getName();
        Map<String, String> xRefs = olsClient.getTermXrefs(term.getGlobalId(), ontologyName);
        Map<String, String> oboSynonyms =  olsClient.getOBOSynonyms(term.getGlobalId(), ontologyName);
        Map<String, List<String>> annotations = olsClient.getAnnotations(term.getOboId(), ontologyName);

        String descriptionText = "";
        for (Iterator i = metadata.iterator(); i.hasNext();) {
            descriptionText += i.next() + "\n";
        }
        olsDialog.currentDefinitionsJTextPane.setText("Definition: " + descriptionText);
        olsDialog.currentDefinitionsJTextPane.setCaretPosition(0);

        if (olsDialog.currentDefinitionsJTextPane.getText().equalsIgnoreCase("null")) {
            olsDialog.currentDefinitionsJTextPane.setText("(no definition provided in CV term)");
        }

        // iterate the xrefs and insert them into the table
        if(xRefs != null){
            for (Iterator i = xRefs.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();

                ((DefaultTableModel) olsDialog.currentTermDetailsJTable.getModel()).addRow(
                        new Object[]{key, xRefs.get(key)});
            }
        }
        if(oboSynonyms != null){
            for (Iterator i = oboSynonyms.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();

                ((DefaultTableModel) olsDialog.currentTermDetailsJTable.getModel()).addRow(
                        new Object[]{"synonym:", key});
            }
        }
        if(annotations != null && (searchType == olsDialog.OLS_DIALOG_TERM_NAME_SEARCH || searchType == olsDialog.OLS_DIALOG_TERM_ID_SEARCH)){
            for (Iterator i = annotations.keySet().iterator(); i.hasNext();) {
                String key = (String) i.next();
                for(String value: annotations.get(key))
                    if(value != null && !value.isEmpty())
                        ((DefaultTableModel) olsDialog.currentTermDetailsJTable.getModel()).addRow(
                                new Object[]{key, value});
            }
        }


        return null;

    }

    @Override
    protected void cancelled() {
        operationCancelorInterupted();

    }

    @Override
    protected void succeed(Object results) {

    }

    @Override
    protected void finished() {

    }

    @Override
    protected void interrupted(InterruptedException iex) {
        operationCancelorInterupted();
    }

    private void operationCancelorInterupted(){
        if (searchType == olsDialog.OLS_DIALOG_TERM_NAME_SEARCH) {
            olsDialog.currentlySelectedTermNameSearchAccessionNumber = null;
        } else if (searchType == olsDialog.OLS_DIALOG_PSI_MOD_MASS_SEARCH) {
            olsDialog.currentlySelectedMassSearchAccessionNumber = null;
        } else if (searchType == olsDialog.OLS_DIALOG_BROWSE_ONTOLOGY) {
            olsDialog.currentlySelectedBrowseOntologyAccessionNumber = null;
        } else if (searchType == olsDialog.OLS_DIALOG_TERM_ID_SEARCH) {
            olsDialog.currentlySelectedTermIdSearchAccessionNumber = term;
        }
    }
}