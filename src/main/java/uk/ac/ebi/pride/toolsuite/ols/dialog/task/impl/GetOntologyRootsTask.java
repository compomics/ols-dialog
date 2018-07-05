package uk.ac.ebi.pride.toolsuite.ols.dialog.task.impl;

import org.springframework.web.client.RestClientException;
import uk.ac.ebi.pride.toolsuite.ols.dialog.OLSDialog;
import uk.ac.ebi.pride.toolsuite.ols.dialog.task.AbstractTask;
import uk.ac.ebi.pride.toolsuite.ols.dialog.util.Util;
import uk.ac.ebi.pride.utilities.ols.web.service.client.OLSClient;
import uk.ac.ebi.pride.utilities.ols.web.service.model.ITerm;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Identifier;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

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
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 11/07/2017.
 */
public class GetOntologyRootsTask extends AbstractTask{

    String ontologyName;

    Identifier parentTermId;

    private static String TASK_NAME = "Get Ontology Roots Task";

    private static Term notDefinedNode = new Term(null, "No Root Terms Defined!", null, null, null, null, null, null, true, null);


    public GetOntologyRootsTask(OLSDialog olsDialog, OLSClient olsClient,
                                String ontologyName, Identifier parentTermId) {
        super(TASK_NAME, olsDialog, olsClient);
        this.ontologyName = ontologyName;
        this.parentTermId = parentTermId;
    }

    @Override
    protected void cancelled() {

    }

    @Override
    protected void succeed(Object results) {

    }

    @Override
    protected void finished() {

    }

    @Override
    protected void interrupted(InterruptedException iex) {

    }

    @Override
    protected Object doInBackground() throws Exception {

        List<ITerm> rootTerms = null;
        if (!ontologyName.equalsIgnoreCase(olsDialog.SEARCH_IN_ALL_ONTOLOGIES_AVAILABLE_IN_THE_OLS_REGISTRY) && !ontologyName.equalsIgnoreCase(olsDialog.SEARCH_IN_THESE_PRESELECTED_ONTOLOGIES)) {
            rootTerms = getOntologyRoots(ontologyName, parentTermId);
        }
        if (rootTerms!=null) {
            if (rootTerms.isEmpty()) {
                olsDialog.treeBrowser.addNode(notDefinedNode);
            } else {
                for (ITerm term : rootTerms) {
                    olsDialog.treeBrowser.addNode(term);
                }
            }
        }
        return null;
    }

    public List<ITerm> getOntologyRoots(String ontologyName, Identifier parentTermId){
        List<ITerm> retrievedValues = new ArrayList<>();

        try {
            List<Term> roots;
            if (parentTermId == null) {
                roots = olsClient.getRootTerms(ontologyName);
            } else {
                roots = olsClient.getTermChildren(parentTermId, ontologyName, 1);
            }

            if (roots != null) {
                roots = Util.refineOntologyNullIds(roots);
                retrievedValues.addAll(roots);
            }

        } catch (RestClientException e) {
            logger.error("Error when trying to access OLS: ");
            e.printStackTrace();
        }
        return retrievedValues;
    }

    @Override
    protected void done() {
        olsDialog.treeBrowser.updateTree();
        olsDialog.treeBrowser.scrollToTop();
        olsDialog.currentlySelectedBrowseOntologyAccessionNumber = null;
        olsDialog.clearData(olsDialog.OLS_DIALOG_BROWSE_ONTOLOGY, true, true);
        olsDialog.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }
}
