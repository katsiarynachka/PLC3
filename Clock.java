/**
 * @author <your-name-here>
 * Matrikelnummer:
 */

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

//----------------------------------------------------------------------
public class Clock {
    //------------------------------------------------------------------

    private DecimalFormat df  = (DecimalFormat)NumberFormat.getInstance(Locale.US);

    { 
        df.applyPattern("000,000"); 
    }

    private long    startTime;
    private int     length = 1000;
    private int     digits = 3;

    public Clock() {
        super();
    }

    public Clock(boolean start) {
        if (start) 
            setStartTime();
    }

    public Clock(int length) {
        setLength(length);
    }

    public void setStartTime() {
        startTime = System.currentTimeMillis();
        System.out.println("[seconds]");
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    } 

    public void setDigits(int digits) {
        this.digits = digits;
    }//----------------------------------------------------------------------

    public long getTime() {
        return System.currentTimeMillis() - startTime;
    }

    public double getTimeUnits() {
        return (System.currentTimeMillis() - startTime)/(double)length;
    }

    public void continueAfter(long ms) {
        if (ms > 0)
            try { Thread.sleep(ms); } 
            catch (InterruptedException e) { 
                System.out.println(this + "continueAfter: das sollte nicht auftreten (\"" + e.getMessage() + "\")");
            }
    }//----------------------------------------------------------------------

    public void continueAfter(double units) {
        if (units > 0.0)
            try { Thread.sleep((long)(units*length)); } 
            catch (InterruptedException e) { 
                System.out.println(this + "continueAfter: das sollte nicht auftreten (\"" + e.getMessage() + "\")");
            }
    }

    public  void continueAt(long ms) {
        try { Thread.sleep(ms - System.currentTimeMillis() + startTime); } 
        catch (IllegalArgumentException e) {
            System.out.println(this + "continueAt: " + ms + " <= aktuelle Zeit (\"" + e.getMessage() + "\")");
        }
        catch (InterruptedException e) {
            System.out.println(this + "continueAt: das sollte nicht auftreten (\"" + e.getMessage() + "\")");
        }
    }

    public  void continueAt(double units) {
        long ms = (long)(units*length);
        try { Thread.sleep(ms - System.currentTimeMillis() + startTime); }
        catch (IllegalArgumentException e) {
            System.out.println(this + "continueAt: " + ms + " <= aktuelle Zeit (\"" + e.getMessage() + "\")");
        }
        catch (InterruptedException e) { 
            System.out.println(this + "continueAt: das sollte nicht auftreten (\"" + e.getMessage() + "\")");
        }
    }

    public String toString() {
        long current = System.currentTimeMillis() - startTime;
        return "[" + df.format(current).replace(',', '.').substring(0, 4 + digits) + "] " + 
        "(" + Thread.currentThread().getName() + ") ";
    }

    public String format(long time) {
        return "[" + df.format(time).replace(',', '.').substring(0, 4 + digits) + "]";
    }

    public String thread() {
        return "(" + Thread.currentThread().getName() + ") ";
    }

    public void show(String s) {
        System.out.println(this + s);
    }

    public void show(Thread t) {
        System.out.println(this + t.getName() + " is " + t.getState() + 
            ( t.isInterrupted() ? "-IR" : "" ) );
    }

    public void show(Thread t, String s) {
        System.out.println(this + t.getName() + " is " + t.getState() + 
            ( t.isInterrupted() ? "-IR" : "" ) + " // " + s);
    }

}
