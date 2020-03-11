
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jkmar
 */
public class RoomQueries {
    private static Connection connection;
    private static ArrayList<RoomEntry> rooms;
    private static PreparedStatement addRoom;
    private static PreparedStatement dropRoom;
    private static PreparedStatement droppedReservations;
    private static PreparedStatement getRoomList;
    private static ResultSet resultSet;
    
    public static String addRoom(RoomEntry room) throws SQLIntegrityConstraintViolationException
    {
        connection = DBConnection.getConnection();
        String status = "";
        try
        {
            addRoom = connection.prepareStatement("insert into rooms (name, seats) values (?, ?)");
            addRoom.setString(1, room.getName());
            addRoom.setInt(2, room.getSeats());
            addRoom.executeUpdate();
            
            ArrayList<WaitlistEntry> waitlist = WaitlistQueries.getWaitlistByTimestamp();
            for(WaitlistEntry entry: waitlist) {
                if(reserveRoom(entry.getSeats(), entry.getDate()) != null) {
                    ReservationEntry newEntry = new ReservationEntry(entry.getName(), room.getName(), 
                            entry.getDate(), entry.getSeats(), entry.getTimestamp());
                    ReservationQueries.addReservationEntry(newEntry);
                    
                    WaitlistQueries.cancelWaitlistEntry(entry.getName(), entry.getDate());
                    
                    status += newEntry.toString() + "<br>";
                }
            }
        }
        catch(SQLException sqlException)
        {
            throw new SQLIntegrityConstraintViolationException("Duplicate key entered.");
        }
        
        return status;
    }
    
    public static String dropRoom(String name){
        connection = DBConnection.getConnection();
        String status = "";
        try {
            dropRoom = connection.prepareStatement("delete from rooms where name=(?)");
            dropRoom.setString(1, name);
            dropRoom.executeUpdate();
                        
            status += ReservationQueries.cancelReservationEntry(name);        
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }  
        return status;
    }
    
    public static ArrayList<RoomEntry> getRoomList(String order) {
        connection = DBConnection.getConnection();
        rooms = new ArrayList<RoomEntry>();
        try {
            getRoomList = connection.prepareStatement("select * from rooms order by " + order + " desc");
            resultSet = getRoomList.executeQuery();

            while(resultSet.next()) {
                String name = resultSet.getString(1);
                int seats = resultSet.getInt(2);
                rooms.add(new RoomEntry(name, seats));
            }
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return rooms; 
    }
    
    public static RoomEntry reserveRoom(int seatsNeeded, Date date) {
        getRoomList("seats");
        RoomEntry currentRoom = null;
        for(RoomEntry room: rooms) {
            if(room.getSeats() >= seatsNeeded) {
                if(!ReservationQueries.reservationFound(room.getName(), date)){
                    currentRoom = room;
                }
            }
        }
        return currentRoom;
    }
}
