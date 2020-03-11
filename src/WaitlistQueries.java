
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jkmar
 */
public class WaitlistQueries {
    private static Connection connection;
    private static ArrayList<WaitlistEntry> waitlist = new ArrayList<WaitlistEntry>();
    private static PreparedStatement getWaitlistList;
    private static PreparedStatement addWaitlistEntry;
    private static PreparedStatement cancelWaitlistEntry;
    private static PreparedStatement findWaitlist;
    private static ResultSet resultSet;
    
    public static void addWaitlistEntry(WaitlistEntry entry) throws SQLIntegrityConstraintViolationException{
        connection = DBConnection.getConnection();
        try
        {
            addWaitlistEntry = connection.prepareStatement("insert into waitlist (faculty, date, seats, timestamp) values (?,?,?,?)");
            
            addWaitlistEntry.setString(1, entry.getName());
            addWaitlistEntry.setDate(2, entry.getDate());
            addWaitlistEntry.setInt(3, entry.getSeats());
            addWaitlistEntry.setTimestamp(4, entry.getTimestamp());
            
            addWaitlistEntry.executeUpdate();
        }
        catch(SQLException sqlException)
        {
            throw new SQLIntegrityConstraintViolationException("Duplicate key entered.");
        }  
    }
    
    public static void cancelWaitlistEntry(String name, Date date) {
        connection = DBConnection.getConnection();
        try {
            cancelWaitlistEntry = connection.prepareStatement("delete from waitlist where (faculty=(?) and date=(?))");
            
            cancelWaitlistEntry.setString(1, name);
            cancelWaitlistEntry.setDate(2, date);
            
            cancelWaitlistEntry.executeUpdate();
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
    }
    
    public static void cancelWaitlistEntry(Date date) {
        connection = DBConnection.getConnection();
        try {
            cancelWaitlistEntry = connection.prepareStatement("delete from waitlist where date=(?)");
            
            cancelWaitlistEntry.setDate(1, date);
            
            cancelWaitlistEntry.executeUpdate();
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
    }
    
    public static ArrayList<WaitlistEntry> getWaitlistByTimestamp() {
        connection = DBConnection.getConnection();
        waitlist = new ArrayList<>();
        try
        {
            getWaitlistList = connection.prepareStatement("select * from waitlist order by timestamp");
            resultSet = getWaitlistList.executeQuery();
            
            while(resultSet.next())
            {
                String name = resultSet.getString(1);
                Date date = resultSet.getDate(2);
                int seats = resultSet.getInt(3);
                Timestamp timestamp = resultSet.getTimestamp(4);
                waitlist.add(new WaitlistEntry(name, date, seats, timestamp));
            }
        }
        catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
        return waitlist; 
    }
    
    public static String reserveRooms() {
        String status = "";
        waitlist = getWaitlistByTimestamp();
        
        for(WaitlistEntry entry : waitlist) {
            RoomEntry openRoom = RoomQueries.reserveRoom(entry.getSeats(), entry.getDate());
            if(openRoom != null) {
                try {
                    ReservationEntry reservation = new ReservationEntry(entry.getName(), 
                            openRoom.getName(), entry.getDate(), entry.getSeats(), entry.getTimestamp());
                    ReservationQueries.addReservationEntry(reservation);
                    status += reservation.toString() + "<br>";
                    
                    cancelWaitlistEntry(entry.getName(), entry.getDate());
                } catch (Exception e){
                    status += "Could not reserve room for " + entry.getName();
                }
            }
        }        
        return status;
    }
    
    
    public static String setStatusText() {
        getWaitlistByTimestamp();
        String status = "<html>Waitlist Status:<br>";
        for(WaitlistEntry entry: waitlist) {
            status += entry.toString() + "<br>";
        }
        status += "</html>";
        return status;
    }
}
