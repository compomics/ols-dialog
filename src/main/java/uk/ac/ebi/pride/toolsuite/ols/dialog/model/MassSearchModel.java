package uk.ac.ebi.pride.toolsuite.ols.dialog.model;

import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import javax.swing.table.DefaultTableModel;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This model contains the the properties of the model for the table of modifications.
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 09/07/2017.
 */
public class MassSearchModel extends DefaultTableModel{

    Class[] types = new Class [] {Term.class, Term.class, String.class};
    private static String[] header = new String [] {"Accession", "CV Term", "Mass Type Value"};



    /**
     * MassSearchModel Constructor
     *
     */
    public MassSearchModel() {
        super(new Object [][] {},header);
    }
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }

    public void addRow(Term term, String massType){
        addRow(new Object[]{
                term, term, term.getXRefValue(massType)
        });
    }
}
