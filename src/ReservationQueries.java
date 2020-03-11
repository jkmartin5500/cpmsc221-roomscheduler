
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
public class ReservationQueries {
    private static Connection connection;
    private static ArrayList<ReservationEntry> reservations = new ArrayList<ReservationEntry>();
    private static PreparedStatement getReservationList;
    private static PreparedStatement addReservationEntry;
    private static PreparedStatement cancelReservationEntry;
    private static PreparedStatement findReservation;
    private static ResultSet resultSet;
    
    public static void addReservationEntry(ReservationEntry entry) throws SQLIntegrityConstraintViolationException{
        connection = DBConnection.getConnection();
        try {
            addReservationEntry = connection.prepareStatement("insert into reservations (faculty, room, date, seats, timestamp) values (?,?,?,?,?)");
            
            addReservationEntry.setString(1, entry.getName());
            addReservationEntry.setString(2, entry.getRoom());
            addReservationEntry.setDate(3, entry.getDate());
            addReservationEntry.setInt(4, entry.getSeats());
            addReservationEntry.setTimestamp(5, entry.getTimestamp());
            
            addReservationEntry.executeUpdate();
        } catch(SQLException sqlException) {
            throw new SQLIntegrityConstraintViolationException("Duplicate key entered.");
        }
    }
    
    public static ReservationEntry cancelReservationEntry(String name, Date date) {
        connection = DBConnection.getConnection();
        try {
            WaitlistQueries.cancelWaitlistEntry(name, date);
            
            cancelReservationEntry = connection.prepareStatement("delete from reservations where (faculty=(?) and date=(?))");
            
            cancelReservationEntry.setString(1, name);
            cancelReservationEntry.setDate(2, date);
            
            cancelReservationEntry.executeUpdate();
            
            ArrayList<WaitlistEntry> waitlist = WaitlistQueries.getWaitlistByTimestamp();
            for(WaitlistEntry entry: waitlist) {
                RoomEntry room = RoomQueries.reserveRoom(entry.getSeats(), entry.getDate());
                if(room != null) {
                    ReservationEntry newEntry = new ReservationEntry(entry.getName(), room.getName(), 
                            entry.getDate(), entry.getSeats(), entry.getTimestamp());
                    addReservationEntry(newEntry); 
                    WaitlistQueries.cancelWaitlistEntry(entry.getName(), entry.getDate());
                    return newEntry;
                }
            }
            
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
        return null;
    }
    
    public static void cancelReservationEntry(Date date) {
        connection = DBConnection.getConnection();
        try {
            cancelReservationEntry = connection.prepareStatement("delete from reservations where date=(?)");
            
            cancelReservationEntry.setDate(1, date);
            
            cancelReservationEntry.executeUpdate();
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
    }
    
    public static String cancelReservationEntry(String room) {
        connection = DBConnection.getConnection();
        String status = "";
        try {
            
            cancelReservationEntry = connection.prepareStatement("select * from reservations where room=(?)");
            cancelReservationEntry.setString(1, room);
            
            resultSet = cancelReservationEntry.executeQuery();
            
            while(resultSet.next())
            {
                String name = resultSet.getString(1);
                Date date = resultSet.getDate(3);
                int seats = resultSet.getInt(4);
                Timestamp timestamp = resultSet.getTimestamp(5);
                
                WaitlistEntry entry = new WaitlistEntry(name, date, seats, timestamp);
                WaitlistQueries.addWaitlistEntry(entry);
                status += entry.toString() + "<br>";
            }
            
            status += WaitlistQueries.reserveRooms();
            
            cancelReservationEntry = connection.prepareStatement("delete from reservations where room=(?)");
            
            cancelReservationEntry.setString(1, room);
            
            cancelReservationEntry.executeUpdate();
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
        return status;
    }
    
    public static ArrayList<ReservationEntry> getReservationsByDate() {
        connection = DBConnection.getConnection();
        reservations = new ArrayList<>();
        try
        {
            getReservationList = connection.prepareStatement("select * from reservations order by date, faculty");
            resultSet = getReservationList.executeQuery();
            
            while(resultSet.next())
            {
                String name = resultSet.getString(1);
                String room = resultSet.getString(2);
                Date date = resultSet.getDate(3);
                int seats = resultSet.getInt(4);
                Timestamp timestamp = resultSet.getTimestamp(5);
                reservations.add(new ReservationEntry(name, room, date, seats, timestamp));
            }
        }
        catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
        return reservations; 
    }
    
    public static boolean reservationFound(String room, Date date) {
        connection = DBConnection.getConnection();
        try {
            findReservation = connection.prepareStatement("select * from reservations where (room= (?) and date=(?))");
            findReservation.setString(1, room);
            findReservation.setDate(2, date);
            
            resultSet = findReservation.executeQuery();
            return resultSet.next();
        } catch (SQLException ex) {
            Logger.getLogger(ReservationQueries.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }
    
    public static String setStatusText() {
        getReservationsByDate();
        String status = "<html>Reservation Status:<br>";
        for(ReservationEntry entry: reservations) {
            status += entry.toString() + "<br>";
        }
        status += "</html>";
        return status;
    }
}
