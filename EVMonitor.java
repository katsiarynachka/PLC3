/**
 * @author Katsiryna Zaitsava
 * Matrikelnummer: 11714999
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//----------------------------------------------------------------------
public class EVMonitor
//----------------------------------------------------------------------
        implements EVInterface {


    private Clock clock;
    public long frist;  // Entlehnfrist in msec
    private ArrayList<Buch> alleBuecher;
    private HashMap<Buch, ArrayList<Person>> entlehnerliste = new HashMap<>(); /* Book : List of persons having the book at current time */
    private HashMap<Buch, ArrayList<Long>> entlehnerzeit = new HashMap<>(); /*Book: List of times when person with the ame index took the book */
    private HashMap<Buch, ArrayList<Boolean>> wurdebenachrichtigt = new HashMap<>();/* Book: List of Booleans that show whether a person with the same index was already notified */
    private HashMap<Buch, ArrayList<Person>> warteliste = new HashMap<>();/* Book: List of persons waiting for a book */
    private HashMap<Person, HashMap<Buch, Long>> nachrichten = new HashMap<>(); /* Person: messages for a person in the form <Book: Time when the person needs to bring the book back */


    //------------------------------------------------------------------
    public Clock getClock() {
        return clock;
    }
    //------------------------------------------------------------------

    //------------------------------------------------------------------
    public EVMonitor(ArrayList<Buch> alleBuecher, int frist, Clock clock)
    //------------------------------------------------------------------
            throws EVException {

        this.clock = clock;
        this.frist = (long)(frist * clock.getLength());

        this.alleBuecher = alleBuecher;
        for (Buch b: this.alleBuecher){
            entlehnerliste.put(b, new ArrayList<>());
            entlehnerzeit.put(b, new ArrayList<>());
            warteliste.put(b, new ArrayList<>());
            wurdebenachrichtigt.put(b, new ArrayList<>());
        }

    }

    //-------------------------------------------------------------------
    public synchronized boolean entlehnen(Buch buch, Person person)
    //-------------------------------------------------------------------
            throws EVException, InterruptedException {

        /* person already has a book -> return false */
        if (entlehnerliste.get(buch).contains(person))
            return false;

        /* there is a free book -> take it! */
        if (buch.getAnzahl() > entlehnerliste.get(buch).size()){
            entlehnerliste.get(buch).add(person);
            entlehnerzeit.get(buch).add(clock.getTime());
            wurdebenachrichtigt.get(buch).add(false);
            return true;
        }

        /* no book is available */
        /* person need to wait for a free one */

        /* check whether someone wasn't notified yet */
        int idx = wurdebenachrichtigt.get(buch).indexOf(false);
        /* found a person that needs to be notified */
        if (idx != -1){
            /* count time */
            long jetzt = clock.getTime();
            long endzeit = entlehnerzeit.get(buch).get(idx) + frist;
            long r = endzeit + clock.getLength();
            long rueckgabezeit = r % clock.getLength() < Test.Delta ? (r / clock.getLength()) * clock.getLength() + Test.Delta : r;

            if (jetzt > endzeit + Test.Delta) {
                rueckgabezeit = 0;
            }

            Person p = entlehnerliste.get(buch).get(idx);

            if (!nachrichten.containsKey(p)){
                nachrichten.put(p, new HashMap<>());
            }
            /* put new message for a person */
            nachrichten.get(p).put(buch, rueckgabezeit);
            wurdebenachrichtigt.get(buch).set(idx, true);

            /*someone can process the list of messages */
            notifyAll();
        }

        /* put person in a waiting list */
        warteliste.get(buch).add(person);

        /* wait until there is a free book and the person is first in the waiting list*/
        while(buch.getAnzahl() == entlehnerliste.get(buch).size() || warteliste.get(buch).indexOf(person) != 0){
            wait();
        }
        /* remove person from waiting list */
        warteliste.get(buch).remove(person);
        /* person takes a book */
        entlehnerliste.get(buch).add(person);
        entlehnerzeit.get(buch).add(clock.getTime());
        wurdebenachrichtigt.get(buch).add(false);

        return true;

    }

    //-------------------------------------------------------------------
    public synchronized boolean rueckgabe(Buch buch, Person person)
    //-------------------------------------------------------------------
            throws EVException {

        /* check whether the person has the book */
        int idx = entlehnerliste.get(buch).indexOf(person);
        /* if not -> return false */
        if (idx == -1)
            return false;
        /* else -> return the book */
        entlehnerliste.get(buch).remove(idx);
        wurdebenachrichtigt.get(buch).remove(idx);
        entlehnerzeit.get(buch).remove(idx);
        notifyAll();

        return true;

    }

    //-------------------------------------------------------------------
    public synchronized HashMap<Buch,Long> empfangen(Person person)
    //-------------------------------------------------------------------
            throws EVException, InterruptedException {

        /* wait until there is no new messages for a person */
        while (nachrichten.get(person) == null ||  nachrichten.get(person).size() == 0){
            wait();
        }

        /* got some news */
        HashMap<Buch, Long> neue_nachrichten = new HashMap<>();
        for(Map.Entry<Buch, Long> element: nachrichten.get(person).entrySet()){
            neue_nachrichten.put(element.getKey(), element.getValue());
        }
        nachrichten.get(person).clear();
        /* return them */
        return neue_nachrichten;

    }

}

