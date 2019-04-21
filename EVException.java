/**
 * @author <your-name-here>
 * Matrikelnummer:
 */

public class EVException extends Exception {
    private static final long serialVersionUID = 1L;

    public EVException(String message)
    {
        super("<EVException " + message + ">");
    }
}
