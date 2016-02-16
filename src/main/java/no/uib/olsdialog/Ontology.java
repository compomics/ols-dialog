/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package no.uib.olsdialog;

/**
 *
 * @author Niels Hulstaert
 */
public class Ontology {

    private String shortName;
    private String name;
    private String baseUri;

    public Ontology(String shortName, String name, String baseUri) {
        this.shortName = shortName;
        this.name = name;
        this.baseUri = baseUri;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUri() {
        return baseUri;
    }

    public void setBaseUri(String baseUri) {
        this.baseUri = baseUri;
    }

}
