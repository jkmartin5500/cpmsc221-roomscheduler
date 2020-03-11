import java.sql.Connection;
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

public class Faculty
{
    private static Connection connection;
    private static ArrayList<String> faculty = new ArrayList<String>();
    private static PreparedStatement addFaculty;
    private static PreparedStatement getFacultyList;
    private static ResultSet resultSet;
    
    public static void addFaculty(String name) throws SQLIntegrityConstraintViolationException
    {
        connection = DBConnection.getConnection();
        try
        {
            addFaculty = connection.prepareStatement("insert into faculty (name) values (?)");
            addFaculty.setString(1, name);
            addFaculty.executeUpdate();
        }
        catch(SQLException sqlException)
        {
            throw new SQLIntegrityConstraintViolationException("Duplicate key entered.");
        }  
    }
    
    public static ArrayList<String> getFacultyList() {
        connection = DBConnection.getConnection();
        faculty = new ArrayList<>();
        try
        {
            getFacultyList = connection.prepareStatement("select name from faculty order by name");
            resultSet = getFacultyList.executeQuery();
            
            while(resultSet.next())
            {
                faculty.add(resultSet.getString(1));
            }
        }
        catch(SQLException sqlException)
        {
            sqlException.printStackTrace();
        }
        return faculty; 
    }  
    
    public static String setStatusText(String name) {
        String status = "<html>" + name + "'s Status:<br>";
        
        //Reservation Status
        ArrayList<ReservationEntry> reservations = ReservationQueries.getReservationsByDate();
        status += "<br>Reservation Status:<br>";
        for(ReservationEntry entry: reservations) {
            if(entry.getName().equals(name)) {
                status += entry.toString() + "<br>";
            }
        }
        
        //Waitlist Status
        ArrayList<WaitlistEntry> waitlists = WaitlistQueries.getWaitlistByTimestamp();
        status += "<br>Waitlist Status:<br>";
        for(WaitlistEntry entry: waitlists) {
            if(entry.getName().equals(name)) {
                status += entry.toString() + "<br>";
            }
        }
        
        status += "</html>";
        return status;
    }
}
