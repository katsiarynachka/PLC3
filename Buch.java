/**
 * @author <your-name-here>
 * Matrikelnummer:
 */

public class Buch
{
    private int nr;    
    private String titel;
    private int anzahl;

    public Buch(int nr, String titel, int anzahl) {
        this.nr = nr;
        this.titel = titel;
        this.anzahl = anzahl;
    }

    public int getAnzahl()
    {
        return anzahl;
    }

    public String toString() {
        return "\"" + titel + "\"";
    }
}
