package no.uib.olsdialog.util;

import java.util.HashMap;
import java.util.Map;

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
    public static Map<String, String> refineOntologyNames(Map<String, String> ontologies){
        Map<String, String> resultOntologies = new HashMap<String, String>();
        if(ontologies != null && ontologies.size() > 0){
            for(String key: ontologies.keySet())
                if(ontologies.get(key).length() > 80)
                    resultOntologies.put(key, ontologies.get(key).substring(0, 50) + "..");
                else
                    resultOntologies.put(key, ontologies.get(key));
        }
        return resultOntologies;
    }

    public static Map<String, String> refineOntologyNullIds(Map<String, String> roots) {
        Map<String, String> resultOntologies = new HashMap<String, String>();
        if(roots != null && roots.size() > 0){
            for(String key: roots.keySet())
                if(key != null)
                    resultOntologies.put(key, roots.get(key));
        }
        return resultOntologies;

    }
}
