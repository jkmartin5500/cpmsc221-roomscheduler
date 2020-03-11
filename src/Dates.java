
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
public class Dates {
    private static Connection connection;
    private static ArrayList<String> dates;
    private static PreparedStatement addDate;
    private static PreparedStatement dropDate;
    private static PreparedStatement droppedReservations;    
    private static PreparedStatement getDateList;
    private static ResultSet resultSet;
    
    public static void addDate(Date date) throws SQLIntegrityConstraintViolationException
    {
        connection = DBConnection.getConnection();
        try {
            addDate = connection.prepareStatement("insert into dates (date) values (?)");
            addDate.setDate(1, date);
            addDate.executeUpdate();
        } catch(SQLException sqlException) {
            throw new SQLIntegrityConstraintViolationException("Duplicate key entered.");
        }  
    }
    
    public static String dropDate(Date date){
        connection = DBConnection.getConnection();
        String status = "";
        try {
            dropDate = connection.prepareStatement("delete from dates where date=(?)");
            dropDate.setDate(1, date);
            dropDate.executeUpdate();
            
            status = setDroppedDateText(date);
            
            ReservationQueries.cancelReservationEntry(date);
            WaitlistQueries.cancelWaitlistEntry(date);
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        } 
        return status;
    }
    
    public static ArrayList<String> getDateList() {
        connection = DBConnection.getConnection();
        dates = new ArrayList<>();
        try {
            getDateList = connection.prepareStatement("select date from dates order by date");
            resultSet = getDateList.executeQuery();
            
            while(resultSet.next()) {
                dates.add(resultSet.getString(1));
            }
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return dates;
    }
    
    public static String setDroppedDateText(Date date) {
        connection = DBConnection.getConnection();
        String status = "<html>Canceled Reservations:<br>";
        try {
            droppedReservations = connection.prepareStatement("select * from reservations where date=(?) order by date");
            
            droppedReservations.setDate(1, date);
            
            resultSet = droppedReservations.executeQuery();
            
            while(resultSet.next()) {
                String name = resultSet.getString(1);
                String room = resultSet.getString(2);
                
                status += "Reservation for " + name + " for room " + room + "<br>";
            }
            
            status += "<br>Canceled Waitlists:<br>";
            
            droppedReservations = connection.prepareStatement("select * from waitlist where date=(?) order by date");
            
            droppedReservations.setDate(1, date);
            
            resultSet = droppedReservations.executeQuery();
            
            while(resultSet.next()) {
                String name = resultSet.getString(1);
                String room = resultSet.getString(2);
                
                status += "Reservation for " + name + " for room " + room + "<br>";
            }
            
        } catch(SQLException sqlException) {
            sqlException.printStackTrace();
        }
        
        return status;
    }
}
