
import java.sql.Date;
import java.sql.Timestamp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jkmar
 */
public class WaitlistEntry {
    private String faculty;
    private Date date;
    private int seats;
    private Timestamp timestamp;
    
    public WaitlistEntry(String faculty, Date date, int seats, Timestamp timestamp) {
        this.faculty = faculty;
        this.date = date;
        this.seats = seats;
        this.timestamp = timestamp;
    }
    
    public String getName(){
        return this.faculty;
    }

    public Date getDate(){
        return this.date;
    }
    
    public int getSeats(){
        return this.seats;
    }
    
    public Timestamp getTimestamp(){
        return this.timestamp;
    }
    
    @Override
    public String toString() {
        return this.getName() + " is waiting for a room with " + this.getSeats() + " seats for " + this.getDate().toString();
    }
}
