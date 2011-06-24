package no.uib.olsdialog.util;

/**
 * Includes help methods that are used by the other classes.
 *
 * @author Harald Barsnes
 *         <p/>
 *         Created April 2005
 */
public final class Util {

    /**
     * Makes sure that all writing to the ErrorLog has a uniform appearence.
     * <p/>
     * Writes the given String to the errorLog.
     * Adds date and time of entry.
     *
     * @param logEntry
     */
    public static void writeToErrorLog(String logEntry) {
        System.out.println(new java.util.Date(System.currentTimeMillis()).toString() + ": " + logEntry);
    }
}
