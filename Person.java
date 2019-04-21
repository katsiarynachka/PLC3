/**
 * @author Katsiryna Zaitsava
 * Matrikelnummer: 11714999
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//----------------------------------------------------------------------
public class Person {
    //------------------------------------------------------------------
    Clock clock;

    private int nr;
    private String name;


    private ArrayList<Buch> buecher;   // alle Buecher

    private EVMonitor evm;

    Thread aktiv;
    Thread passiv;

    // Buecher, die ich entlehnt habe
    private ArrayList<Buch> meineBuecher = new ArrayList<>();

    //------------------------------------------------------------------
    public String toString() {
        return name;

    }
    //-----------------------------------------------------------------

    //------------------------------------------------------------------
    public Person(int nr, String name, ArrayList<Buch> buecher,
                  EVMonitor evm) {
        //------------------------------------------------------------------
        this.nr = nr;
        this.name = name;
        this.buecher = buecher;
        this.evm = evm;
    }

    //------------------------------------------------------------------
    public void stop() {
        //------------------------------------------------------------------
        aktiv.interrupt();
        passiv.interrupt();
    }

    //------------------------------------------------------------------
    public void start() {
        //------------------------------------------------------------------

        clock = evm.getClock();

        //------------------------------------------------------------------
        aktiv = new Thread( () ->  {
            //------------------------------------------------------------------

            Buch buch;
            double time;
            Step step;

            try {
                clock.show(this + "/aktiv gestartet ");

                // Ausfuehren des Schedule-Skripts

                for (int i = 0; i < Test.schedule.size(); i++) {

                    step = Test.schedule.get(i);
                    if (step.getPersonNr() == nr) {
                        time = step.getTime();
                        buch = buecher.get(step.getBuchNr() - 1);

                        if (time > clock.getTimeUnits() + 30.0/clock.getLength()) { // + 50 msec

                            clock.continueAt(time);

                            //------------------------------------------------------------------
                            if (step.isEntlehnen()) { // entlehnen-Schritt
                                //------------------------------------------------------------------
                                boolean ok = evm.entlehnen(buch, this);
                                // diese Ausgabe wird verlangt:
                                clock.show(this + ": Entlehnung " + buch + " " + (ok?"ok":"nicht erfolgt"));
                                if (ok)
                                    meineBuecher.add(buch);
                            }
                            //------------------------------------------------------------------
                            else { // zurueckgeben-Schritt: Rueckgabe Fall A
                                //------------------------------------------------------------------
                                boolean ok = evm.rueckgabe(buch, this);
                                // diese Ausgabe wird verlangt:
                                clock.show(this + ": Rueckgabe/A " + buch  + " " + (ok?"ok":"nicht erfolgt"));
                                if (ok)
                                    meineBuecher.remove(buch);
                            }
                        }
                    }
                }
            }
            catch(InterruptedException ex) {
                clock.show(this + "/aktiv interrupt");
            }
            catch (EVException ex) {
                clock.show(this + " " +ex.getMessage());
            }

            clock.show(this + "/aktiv beendet");

        } );

        //------------------------------------------------------------------
        passiv = new Thread( () -> {
            //------------------------------------------------------------------

            // eventuell Instanzvariabe DEKLARIEREN

            try {

                clock.show(this + "/passiv gestartet ");
                HashMap<Buch, Long> nachrichten;

                while (!Thread.currentThread().isInterrupted()) {

                    /* get some news */
                    nachrichten = evm.empfangen(this);
                    /* look throught the array of news */
                    for (Map.Entry<Buch, Long> nachricht: nachrichten.entrySet()){
                        /* return book immediately */
                        if (nachricht.getValue() == 0){
                            clock.show(this + ": " + nachricht.getKey()  + " sofort zurueckgeben");
                            boolean ok = evm.rueckgabe(nachricht.getKey(), this);
                            clock.show(this + ": Rueckgabe/B " + nachricht.getKey()  + " " + (ok?"ok":"nicht erfolgt"));
                            if (ok)
                                meineBuecher.remove(nachricht.getKey());

                        }
                        /*return book later*/
                        else {
                            clock.show(this + ": " + nachricht.getKey()  + " um " +  clock.format(nachricht.getValue()) + " zurueckgeben");
                            new RueckgabeSpaeter(nachricht.getKey(), this, nachricht.getValue(), meineBuecher, evm, clock).start();
                        }
                    }
                }
            }
            catch(InterruptedException ex) {
                clock.show(this + "/passiv interrupt");
            }
            catch(EVException ex) {
                clock.show(this + " " + ex.getMessage());
            }

            clock.show(this + "/passiv beendet");
        } );

        // jetzt geht's los ...
        aktiv.start();
        passiv.start();
    }

    //------------------------------------------------------------------
    public static class RueckgabeSpaeter extends Thread {
        //------------------------------------------------------------------

        Buch buch;
        Person person;
        long rueckgabezeit;
        ArrayList<Buch> meineBuecher;
        EVMonitor evm;
        Clock clock;

        public RueckgabeSpaeter(Buch buch, Person person, long rueckgabezeit, ArrayList<Buch> meineBuecher, EVMonitor evm, Clock clock){
            this.buch = buch;
            this.person = person;
            this.rueckgabezeit = rueckgabezeit;
            this.meineBuecher = meineBuecher;
            this.evm = evm;
            this.clock = clock;
        }

        public void run(){
            try{
                clock.show(person + ": Rueckgabe/C " + buch  + " um " +  clock.format(rueckgabezeit) + " geplant");
                clock.continueAt(rueckgabezeit);
                if (meineBuecher.contains(buch)){
                    boolean ok = evm.rueckgabe(buch, person);
                    clock.show( person + ": Rueckgabe/C " + buch  + " " + (ok?"ok":"nicht erfolgt"));
                    if (ok)
                        meineBuecher.remove(buch);
                }

            } catch(EVException ex){
                clock.show("Rueckgabespaeter: " + this + " " + ex.getMessage());
            }

        }
    }
}

