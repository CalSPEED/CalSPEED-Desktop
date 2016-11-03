/*
Copyright (c) 2014, California State University Monterey Bay (CSUMB).
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    1. Redistributions of source code must retain the above copyright notice,
       this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above
           copyright notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
       its contributors may be used to endorse or promote products derived from
       this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package Database;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import Tester.utils.Result;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.sql.ResultSetMetaData;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database {
    private String dbName;
    private Connection conn;
    private Statement stat;
    private String tblName;
    private Gson results;
    private static Statement statement;

    public Database(String dbName, String tblName) {
        this.dbName = dbName;
        this.tblName = tblName;
        results = new Gson();
        try {
            Class.forName("org.h2.Driver");
            // AUTO_SERVER=TRUE will allow multiple connections to the database
            // Windows will prompt to allow access through the firewall.
            conn = DriverManager.getConnection("jdbc:h2:./" + dbName + ";AUTO_SERVER=TRUE");
            stat = conn.createStatement();
            statement = conn.createStatement();
        } catch (ClassNotFoundException | SQLException e ) {
            
        }
    }
    

    public void createInit() {
        try {
            //stat.execute("DROP TABLE IF EXISTS " + tblName);
            stat.execute("CREATE TABLE IF NOT EXISTS " + tblName + " ("
                            + "id INT PRIMARY KEY AUTO_INCREMENT,"
                            + "timestamp TIMESTAMP,"
                            + "type VARCHAR(20),"
                            + "location VARCHAR(250),"
                            + "lat VARCHAR(250),"
                            + "lng VARCHAR(250),"
                            + "upload DOUBLE,"
                            + "download DOUBLE,"
                            + "jitter DOUBLE,"
                            + "delay DOUBLE,"
                            + "mos DOUBLE,"
                            + "video VARCHAR(250))"
            );
     
        } catch (SQLException e) {
            
        }
    }
    
    
    //methods for Terms of Service Popup and data validation.
    public void createValidationTable(){
        try{
            stat.execute("CREATE TABLE IF NOT EXISTS Validation"
                     + "(TermsAgreement VARCHAR(1) NOT NULL DEFAULT '0');" );//0 for false, 1 for true they agreed
            if(validationTableEmpty())
                stat.execute("INSERT INTO Validation (TermsAgreement) VALUES('0')");
        }catch(SQLException e){
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }
    
    public boolean validationTableEmpty()
    {
        try
        {
             ResultSet rs = stat.executeQuery("SELECT * from Validation");
             
             if (rs.next()) //if there's a row, it's not empty
                 return false;
             
             else
                 return true; //table is not empty
             
        }catch(SQLException e)
        {
            
        }
       return true;
    }
    
    public void insertTermsValue(){
        try{
            stat.execute("DELETE FROM Validation");
            stat.execute("INSERT INTO Validation (TermsAgreement) " + 
                    "VALUES('1'" + 
                    ")" //TermsAgreement: 0 have not agreed yet, 1 for agreed
            );
        }catch(SQLException e){
            //e.printMessage();
        }
    }
            
    
    public String getTermsValue(){
        String termsValue = "";
        try{
            ResultSet rs = stat.executeQuery("SELECT TermsAgreement FROM Validation");
            while(rs.next())
            {
               termsValue = rs.getString("TermsAgreement");
            }
        }catch(SQLException e){
            //e.printMessage();
        }
        return termsValue;
    }
    public void createLocationTable(){
        try {
            //stat.execute("DROP TABLE IF EXISTS LocationData");
            stat.execute("CREATE TABLE IF NOT EXISTS LocationData"
                    + " (id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "address VARCHAR(250),"
                    + "lat VARCHAR(250),"
                    + "lng VARCHAR(250),"
                    + "city VARCHAR(250),"
                    + "zip VARCHAR(250)"
                    + /*", typeID VARCHAR(1)*/")"   
                    
            );
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public boolean locationTableEmpty()
    {
        try
        {
             ResultSet rs = stat.executeQuery("SELECT * from LocationData");
             
             if (rs.next()) //if there's a row, it's not empty
                 return false;
             
             else
                 return true; //table is not empty
             
        }catch(SQLException e)
        {
            
        }
       return true;
    }
    
    public void updateTable() {
        try {
            
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD address VARCHAR(300) NOT NULL");
            
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD mos DOUBLE");
            
            
               
        } catch (SQLException e) {
          
        }
    }
    
    public void updateTableVideo(){
       try{
           stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD video VARCHAR(300)");
       }
       catch(SQLException e){
       }
    }
    
    public void updateLocationTable(){
        try {
            stat.execute("ALTER TABLE LocationData "
                    + "ADD typeID VARCHAR(1)");
        } catch (SQLException e) {
            
        }
    }

    public void dropDatabase() {
        try {
            stat.execute("DROP SCHEMA IF EXISTS " + dbName);
        } catch (SQLException e) {
            
        }
    }

    public void dropValidationTable(){
        try {
            stat.execute("DROP TABLE Validation IF EXISTS ");
        } catch(SQLException e) {
            
        }
    }
    
    public void dropTable() {
        try {
            stat.execute("DROP TABLE " + tblName + " IF EXISTS ");
        } catch(SQLException e) {
            
        }
    }
    
    public void clearLocationData(){
        if (!this.LocationTableEmpty()){
            try
        {
            stat.execute("DELETE FROM LocationData"
            );
            
        }catch(SQLException e) {
            
        }
        }
    }
    
    public void insertLocationData(String address, String lat, String lng, String city, String zip, String type)
    {
        if (address.contains("'")){ 
            address = address.replace("'", "''"); //replace ' with '' because apostrophes disrupt queries
        }
         
        try
        {
            stat.execute("INSERT INTO LocationData (address, lat, lng, city, zip, typeID) " + 
                    "VALUES('" + 
                    address + "', '" +
                    lat + "', '" +
                    lng + "', '" +
                    city + "', '" +
                    zip + "', '" +
                     type + "')" //typeID: 0 for address, 1 for latlong
            ); //if the query isn't valid, this will be caught, and clearLocationData() won't execute
            
            clearLocationData(); //
            stat.execute("INSERT INTO LocationData (address, lat, lng, city, zip, typeID) " + 
                    "VALUES('" + 
                    address + "', '" +
                    lat + "', '" +
                    lng + "', '" +
                    city + "', '" +
                    zip + "', '" +
                     type + "')" //typeID: 0 for address, 1 for latlong
            );
            
        }catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        
    }

    public void insertData(String type, String location, String lat, String lng, Double upload, Double download, Double jitter, Double delay, Double mos, String address, String video) {
        if (address.contains("'")){ 
            address = address.replace("'", "''"); //replace ' with '' because apostrophes disrupt queries
        }
        
        try {
            stat.execute("INSERT INTO " +
                            tblName + " (timestamp, type, location, lat, lng, upload, download, jitter, delay, mos, address, video) " +
                            "VALUES(" +
                            "CURRENT_TIMESTAMP, '" +
                            type + "', '" +
                            location + "', '" +
                            lat + "', '" +
                            lng + "', " +
                            upload + ", " +
                            download + ", " +
                            jitter + ", " +
                            delay + ", " +
                            mos + ", '" + 
                            address + "', '" +
                            video + "')"
            );
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void insertData(Result result) {
        insertData(result.type, result.location, result.lat, result.lng, result.upload, result.download, result.jitter, result.delay, result.mos, result.address, result.video);
    
    }
    
    public String getLocation(){
        String location = "";
        try {
            ResultSet rs = 
                    stat.executeQuery("SELECT * FROM LocationData");
            
            while (rs.next()){
                location = rs.getString("city") + ", " + rs.getString("zip");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return location;
    }
    
    public static String getAddress(){
        String address = "";
        try {
            ResultSet rs = 
                    statement.executeQuery("SELECT * FROM LocationData");
            
            while (rs.next()){
                address = rs.getString("address");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return address;
    }
    
    public static String getTypeID(){
        String type = "";
        try {
            ResultSet rs = 
                    statement.executeQuery("SELECT * FROM LocationData");
            
            while (rs.next()){
                type = rs.getString("typeID");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return type;
    }
    
    public static String getLatitude(){
        String latitude = "";
        try {
            ResultSet rs = 
                    statement.executeQuery("SELECT * FROM LocationData");
            
            while (rs.next()){
                latitude = rs.getString("lat");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return latitude;
    }
    
    public static String getLongitude(){
        String longitude = "";
        try {
            ResultSet rs = 
                    statement.executeQuery("SELECT * FROM LocationData");
            
            while (rs.next()){
                longitude = rs.getString("lng");
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
        return longitude;
    }
    
    public static boolean LocationTableEmpty(){
        try
        {
             ResultSet rs = statement.executeQuery("SELECT * from LocationData");
             
             if (rs.next()) //if there's a row, it's not empty
                 return false;
             
             else
                 return true; //table is not empty
             
        }catch(SQLException e)
        {
            
        }
       return true;
    }
    
    public String selectData() {
        ArrayList<Map> arrayResults = new ArrayList<>();
        String finalResults = "";

        try {
            Map<String, String> arrayResult;
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName);
            while (rs.next()) {
                arrayResult = new HashMap<>();
                arrayResult.put("id",rs.getString("id"));
                arrayResult.put("timestamp", rs.getString("timestamp"));

                //take the timestamp and convert it to times and dates used by the UI
                try {
                    String ds = rs.getString("timestamp");
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date d = f.parse(ds);
                    
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    
                    arrayResult.put("time", new SimpleDateFormat("HH:mm").format(cal.getTime()));
                    arrayResult.put("date", new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime()));
                    

                } catch(ParseException e) {
                    
                }
                
                arrayResult.put("type", rs.getString("type"));
                arrayResult.put("location", rs.getString("location"));
                arrayResult.put("upload", rs.getString("upload"));
                arrayResult.put("download", rs.getString("download"));
                arrayResult.put("jitter", rs.getString("jitter"));
                arrayResult.put("delay", rs.getString("delay"));
                arrayResult.put("mos", rs.getString("mos"));
                if (rs.getString("address").length() > 0)
                    arrayResult.put("address", rs.getString("address"));
                else
                    arrayResult.put("address", rs.getString("location"));
                try{
                    if(rs.getString("video").length() > 0 && !rs.getString("video").equals("null")){
                        if(rs.getString("video").equals("High Definition"))
                            arrayResult.put("video", "HD");
                        if(rs.getString("video").equals("Standard Definition"))
                            arrayResult.put("video", "SD");
                        if(rs.getString("video").equals("Low Service"))
                            arrayResult.put("video", "LD");
                    }
                    else
                        arrayResult.put("video", "N/A");
                }
                catch(Exception e){
                    arrayResult.put("video", "N/A");
                }
                arrayResults.add(0, arrayResult);
            }

            Type listOfTestObject = new TypeToken<ArrayList<Map>>(){}.getType();
            finalResults = results.toJson(arrayResults, listOfTestObject);

        } catch(SQLException e) {
            
        }

        return finalResults;
    }

    public void debug() {
        
        try {
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName);
            while (rs.next()) {
                System.out.println(
                                "id:" + rs.getString("id") + " " +
                                "timestamp:" + rs.getString("timestamp") + " " +
                                "type:" + rs.getString("type") + " " +
                                "location:" + rs.getString("location") + " " +
                                "upload:" + rs.getString("upload") + " " +
                                "download:" + rs.getString("download") + " " +
                                "jitter:" + rs.getString("jitter") + " " +
                                "delay:" + rs.getString("delay") + " " +
                                "mos:" + rs.getString("mos") + " " +
                                "address: " + rs.getString("address") + " " +
                                "video: " + rs.getString("video")
                );
            }
            
            
        } catch (SQLException e) {
        }
    }
    
    public void debugLocation(){
        try {
            ResultSet rs = stat.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                System.out.println(
                        "id: " + rs.getString("id") + " " +
                                "address: " + rs.getString("address") + " " +
                                "lat: " + rs.getString("lat") + " " +
                                "lng: " + rs.getString("lng") + " " +
                                "zip: " + rs.getString("zip") + " " +
                                "city: " + rs.getString("city") + " " +
                                "type: " + rs.getString("typeID")
                );
            }
        } catch (SQLException ex) {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void closeConnection() {
        try {
            stat.close();
            conn.close();
        } catch (SQLException e) {
            
        }
    }
    
    public String getDataById(int id)
    {
        ArrayList<Map> arrayResults = new ArrayList<>();
        String finalResults = "";

        try {
            Map<String, String> arrayResult;
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName + " WHERE id = " + id);
            while (rs.next()) {
                arrayResult = new HashMap<>();
                arrayResult.put("id",rs.getString("id"));
                arrayResult.put("timestamp", rs.getString("timestamp"));

                //take the timestamp and convert it to times and dates used by the UI
                try {
                    String ds = rs.getString("timestamp");
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date d = f.parse(ds);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    arrayResult.put("time", new SimpleDateFormat("HH:mm").format(cal.getTime()));
                    arrayResult.put("date", new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime()));


                } catch(ParseException e) {

                }

                arrayResult.put("type", rs.getString("type"));
                arrayResult.put("location", rs.getString("location"));
                arrayResult.put("upload", rs.getString("upload"));
                arrayResult.put("download", rs.getString("download"));
                arrayResult.put("jitter", rs.getString("jitter"));
                arrayResult.put("delay", rs.getString("delay"));
                arrayResult.put("mos", rs.getString("mos"));
                if (rs.getString("address").length() > 0)
                    arrayResult.put("address", rs.getString("address"));
                else
                    arrayResult.put("address", rs.getString("location"));
                                try{
                    if(rs.getString("video").length() > 0 && !rs.getString("video").equals("null"))
                        arrayResult.put("video", rs.getString("video"));
                    else
                        arrayResult.put("video", "N/A");
                }
                catch(Exception e){
                    arrayResult.put("video", "N/A");
                }
                arrayResults.add(0, arrayResult);
            }

            Type listOfTestObject = new TypeToken<ArrayList<Map>>(){}.getType();
            finalResults = results.toJson(arrayResults, listOfTestObject);

        } catch(SQLException e) {
            System.out.println(e.getMessage());
        }
        
        

        return finalResults;
    }
    
}