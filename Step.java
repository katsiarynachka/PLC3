/**
 * @author <your-name-here>
 * Matrikelnummer:
 */

public class Step {

    private double time;
    private int personNr;
    private int buchNr; 
    private boolean entlehnen;

    public Step(double time, int personNr, boolean entlehnen, int buchNr) {
        this.time = time;
        this.personNr = personNr;
        this.buchNr = buchNr;
        this.entlehnen = entlehnen;
    }

    public double getTime() {
        return time;
    }

    public int getPersonNr() {
        return personNr;
    }

    public int getBuchNr() {
        return buchNr;
    }

    public boolean isEntlehnen() {
        return entlehnen;
    }

}
