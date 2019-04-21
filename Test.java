/**
 * @author  H.Moritsch
 * @version 2019-01-02
 */

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Test {

    public static long Delta;   // Anpassung (in msec)

    public static ArrayList<Step> schedule = new ArrayList<>();

    public static void main(String[] args) {
        test(args);
    }

    public static void test(String[] args) {

        int maxLines = 999; // Obergrenze für Anzahl Zeilen, die von Datei schedule gelesen werden

        int unitlength;     // Länge einer Zeiteinheit in msec (1 Zeiteinheit = 1 Tag)
        int digits;         // Genauigkeit der Zeitprotokollierung: Anzahl der Nachkommastellen (0-3)

        int frist;          // Entlehnfrist in Tagen, > 0
        int npersonen;      // Anzahl Personen, 1-10
        int nbuecher;       // Anzahl Bücher

        // Aufruf: java Test <unitlength> <digits> <frist> <npersonen> <nex_1> <nex_2> ...
        // Anzahl Bücher = args.length - 4
        // nex_i ... Anzahl Exemplare von Buch i, nex_i > 0

        Clock clock;
        EVMonitor evm;

        ArrayList<Buch> buecher = new ArrayList<>();
        ArrayList<Person> personen = new ArrayList<>();

        if (args.length < 1)
            System.out.println(
                    "usage: java Test <unitlength> <digits> <frist> <npersonen> <nex_1> <nex_2> ...");
        else
            try {
                if (args.length < 5 || args.length > 9)
                    throw new IllegalArgumentException("args.length");

                unitlength = Integer.parseInt(args[0]);
                if (unitlength < 10)
                    throw new IllegalArgumentException("unitlength");

                Delta = (long)(30*unitlength/100); // fix auf 30% eingestellt

                digits = Integer.parseInt(args[1]);
                if (digits < 0 || digits > 3)
                    throw new IllegalArgumentException("digits");

                frist = Integer.parseInt(args[2]);
                if (frist < 1)
                    throw new IllegalArgumentException("frist");

                npersonen = Integer.parseInt(args[3]);
                if (npersonen < 1 || npersonen > 10)
                    throw new IllegalArgumentException("npersonen");

                System.out.println("# unitlength " + unitlength + " digits " + digits);

                nbuecher = args.length - 4;
                System.out.println("# frist " + frist + " npersonen " + npersonen + " nbuecher " + nbuecher);

                int nex[] = new int[nbuecher];
                for (int i = 0; i < nbuecher; i++) {
                    nex[i] = Integer.parseInt(args[4 + i]);
                    if (nex[i] < 1)
                        throw new IllegalArgumentException("nex_" + (i + 1));
                }

                buecher.add(new Buch(1, "Der kleine Prinz", nex[0]));
                if (nbuecher > 1) buecher.add(new Buch(2, "Der Name der Rose", nex[1]));
                if (nbuecher > 2) buecher.add(new Buch(3, "Der Zauberberg", nex[2]));
                if (nbuecher > 3) buecher.add(new Buch(4, "Die Blechtrommel", nex[3]));
                if (nbuecher > 4) buecher.add(new Buch(5, "Der Herr der Ringe", nex[4]));

                for (int i = 0; i < buecher.size(); i++)
                    System.out.println("# " + (i+1) + " " + buecher.get(i) + " "
                            + buecher.get(i).getAnzahl() + " Ex.");

                clock = new Clock(unitlength);
                clock.setDigits(digits);

                evm  = new EVMonitor(buecher, frist, clock);

                personen.add(new Person(1, "Anna", buecher, evm));
                if (npersonen > 1) personen.add(new Person(2,  "Birgit", buecher, evm));
                if (npersonen > 2) personen.add(new Person(3,  "Emil",   buecher, evm));
                if (npersonen > 3) personen.add(new Person(4,  "Guido",  buecher, evm));
                if (npersonen > 4) personen.add(new Person(5,  "Helga",  buecher, evm));
                if (npersonen > 5) personen.add(new Person(6,  "Karin",  buecher, evm));
                if (npersonen > 6) personen.add(new Person(7,  "Lukas",  buecher, evm));
                if (npersonen > 7) personen.add(new Person(8,  "Nina",   buecher, evm));
                if (npersonen > 8) personen.add(new Person(9,  "Ulrich", buecher, evm));
                if (npersonen > 9) personen.add(new Person(10, "Werner", buecher, evm));

                System.out.print("#");
                for (int i = 0; i < personen.size(); i++)
                    System.out.print(" " + (i+1) + " " + personen.get(i));
                System.out.println();

                System.out.println("# 1 Tag <-> " + unitlength + " msec" + ", Frist <-> "
                        + String.format("%.3f", frist*unitlength/1000.0) + " sec");

                double end = 0.0;
                {
                    BufferedReader in = new BufferedReader(new FileReader("schedule"));
                    String[] field;
                    String line = in.readLine();
                    int count = 0;
                    while (line != null && count < maxLines) {

                        count++;
                        field = line.split(" +");
                        double time = Double.parseDouble(field[0]);

                        if (field.length > 1) {
                            int personNr = Integer.parseInt(field[1]);
                            boolean entlehnen = field[2].charAt(0) == 'E';
                            int buchNr = Integer.parseInt(field[3]);
                            if (personNr <= personen.size() && buchNr <= buecher.size()) {
                                // System.out.println(String.format("%.2f", time) + " " + personNr + " "
                                // +  entlehnen + " " + buchNr);
                                schedule.add(new Step(time, personNr, entlehnen, buchNr));
                                end = time;
                            }
                        }
                        else
                            end = time;

                        line = in.readLine();
                    }
                    in.close();
                }
                System.out.println("# schedule " + (schedule.size()) + " lines ending "
                        + String.format("%.2f", end) + " <-> " + String.format("%.2f", end*unitlength/1000.0) + " sec");

                clock.setStartTime();

                for (Person person: personen)
                    person.start();
                clock.show("Personen gestartet");

                clock.continueAt(end + 500.0/unitlength); // + 500 msec
                clock.show("end of script");

                for (Person person: personen)
                    person.stop();

            }
            catch (Exception ex) {
                System.out.println("Exception in Test.test(): " + ex.getMessage());
            }
    }
}

