package uk.ac.ebi.pride.toolsuite.ols.dialog.util;

import uk.ac.ebi.pride.utilities.ols.web.service.model.ITerm;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Ontology;
import uk.ac.ebi.pride.utilities.ols.web.service.model.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Includes help methods that are used by the other classes.
 *
 * @author Harald Barsnes
 * <p>
 * Created April 2005
 */
public final class Util {

    /**
     * Makes sure that all writing to the ErrorLog has a uniform appearance.
     * <p>
     * Writes the given String to the errorLog. Adds date and time of entry.
     *
     * @param logEntry
     */
    public static void writeToErrorLog(String logEntry) {
        System.out.println(new java.util.Date(System.currentTimeMillis()).toString() + ": " + logEntry);
    }

    /**
     * This function refine the ontolgoy name. If they are larget than a certain number of character it reduce they chunck them
     * @param ontologies
     */
    public static List<Ontology> refineOntologyNames(List<Ontology> ontologies){
        List<Ontology> resultOntologies = new ArrayList<Ontology>();
        if(ontologies != null && ontologies.size() > 0){
            for(Ontology key: ontologies)
                if(key.getName().length() > 80){
                    key.setName(key.getName().substring(0, 50) + "..");
                    resultOntologies.add(key);
                }else
                    resultOntologies.add(key);
        }
        return resultOntologies;
    }

    public static List<Term> refineOntologyNullIds(List<Term> roots) {
        List<Term> result = new ArrayList<Term>();
        if(roots != null && roots.size() > 0){
            for(Term key: roots)
                if(key.getGlobalId() != null && key.getGlobalId().getIdentifier() != null)
                    result.add(key);
        }
        return result;

    }

    public static Ontology findOntology(List<Ontology> ontologies, String shortName){
        if(ontologies != null && shortName != null){
            for(Ontology ontology: ontologies)
                if(ontology.getId().equalsIgnoreCase(shortName))
                    return ontology;
        }
        return null;
    }

    /**
     * Returns the protein accession number as a web link to the given PSI-MOD
     * at http://www.ebi.ac.uk/ontology-lookup.
     *
     * @return the OLS web link
     */
    public static String getOlsAccessionLink(ITerm term) {
        String notSelectedRowHtmlTagFontColor = "#0101DF";
        return "<html><a href=\"http://www.ebi.ac.uk/ols/beta/ontologies/" + term.getOntologyName()+"/terms?iri=" + term.getIri() + "\""
                + "\"><font color=\"" + notSelectedRowHtmlTagFontColor + "\">"
                + term.getGlobalId().getIdentifier() + "</font></a></html>";
    }

    public static String getOlsTermLink(ITerm term){
        return "http://www.ebi.ac.uk/ols/beta/ontologies/" + term.getOntologyName()+"/terms?iri=" + term.getIri().getIdentifier();

    }
}
