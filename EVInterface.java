/**
 * @author <your-name-here>
 * Matrikelnummer:
 */

import java.util.HashMap;

public interface EVInterface {
    // Person entlehnt Buch - true bei Erfolg
    boolean entlehnen(Buch buch, Person person) throws InterruptedException, EVException;

    // Person gibt Buch zurück - true bei Erfolg
    boolean rueckgabe(Buch buch, Person person) throws EVException;  

    // Person wartet auf Nachrichten (Buch, Zeitpunkt der Rückgabe in msec)
    HashMap<Buch,Long> empfangen(Person person) throws InterruptedException, EVException;
}
