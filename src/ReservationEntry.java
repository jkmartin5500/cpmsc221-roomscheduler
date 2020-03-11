
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
public class ReservationEntry {
    private String faculty;
    private String room;
    private Date date;
    private int seats;
    private Timestamp timestamp;
    
    public ReservationEntry(String faculty, String room, Date date, int seats, Timestamp timestamp) {
        this.faculty = faculty;
        this.room = room;
        this.date = date;
        this.seats = seats;
        this.timestamp = timestamp;
    }
    
    public String getName(){
        return this.faculty;
    }
    
    public String getRoom(){
        return this.room;
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
        return "Room " + this.getRoom() + " reserved by " + this.getName() + " for " + this.getDate().toString();
    }
}
