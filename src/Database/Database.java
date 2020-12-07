/*
Copyright (c) 2020, California State University Monterey Bay (CSUMB).
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

import Tester.utils.LocationService;
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
import logging.Timber;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.time.Instant;


public class Database {

    private final static String CLASS_NAME = Database.class.getName();
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
            Timber.debug(CLASS_NAME, "DB connection successful");
        } catch (ClassNotFoundException | SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
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
                    + "address VARCHAR(250),"
                    + "video VARCHAR(250),"
                    + "videoConference VARCHAR(250),"
                    + "voip VARCHAR(250),"
                    + ");"
            );

        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    //methods for Terms of Service Popup and data validation.
    public void createValidationTable() {
        try {
            stat.execute("CREATE TABLE IF NOT EXISTS Validation"
                    + "(TermsAgreement VARCHAR(1) NOT NULL DEFAULT '0');");//0 for false, 1 for true they agreed
            if (validationTableEmpty()) {
                stat.execute("INSERT INTO Validation (TermsAgreement) VALUES('0')");
            }
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }

    }

    public boolean validationTableEmpty() {
        try {
            ResultSet rs = stat.executeQuery("SELECT * from Validation");
            return !rs.next(); //if there's a row, it's not empty
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
        return true;
    }

    public void insertTermsValue() {
        try {
            stat.execute("DELETE FROM Validation");
            stat.execute("INSERT INTO Validation (TermsAgreement) "
                    + "VALUES('1'"
                    + ")" //TermsAgreement: 0 have not agreed yet, 1 for agreed
            );
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public String getTermsValue() {
        String termsValue = "";
        try {
            ResultSet rs = stat.executeQuery("SELECT TermsAgreement FROM Validation");
            while (rs.next()) {
                termsValue = rs.getString("TermsAgreement");
            }
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
        return termsValue;
    }

    public void createLocationTable() {
        try {
            stat.execute("CREATE TABLE IF NOT EXISTS LocationData"
                    + " (id INT PRIMARY KEY AUTO_INCREMENT,"
                    + "address VARCHAR(250),"
                    + "lat VARCHAR(250),"
                    + "lng VARCHAR(250),"
                    + "city VARCHAR(250),"
                    + "zip VARCHAR(250)"
                    + ")"
            );
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public boolean locationTableEmpty() {
        try {
            ResultSet rs = stat.executeQuery("SELECT * from LocationData");
            return !rs.next(); //if there's a row, it's not empty
            //table is not empty

        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
        return true;
    }

    public void createIgnoreTable() {
        long now = Instant.now().toEpochMilli();
        try {
            stat.execute("CREATE TABLE IF NOT EXISTS Expiration"
                    + "(expires long NOT NULL DEFAULT " + now + ","
                    + ")"
            );
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public static long getExpiredTime() {
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM Expiration");
            if (!rs.next()) {
                statement.execute("INSERT INTO Expiration (expires) VALUES (0)");
                Timber.debug(CLASS_NAME, "Empty value, setting to 0");
            } else if (rs.isFirst() && rs.isLast()) {
                return rs.getLong("expires");
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return 0;
    }

    public static void setExpiredTime(Date newExpiration) {
        long newTime = newExpiration.getTime();
        Timber.debug(CLASS_NAME, "Setting new time: "
                + Long.toString(newExpiration.getTime()));
        try {
            statement.execute("UPDATE Expiration SET expires = "
                    + newTime
            );
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM Expiration");
            while (rs.next()) {
                Timber.verbose(CLASS_NAME, "Updated expiration time: " + rs.getLong("expires"));
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public void dropIgnoreTable() {
        try {
            stat.execute("DROP TABLE Expiration IF EXISTS ");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public void updateTable() {
        try {
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD address VARCHAR(250) NOT NULL");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, "Error altering adress table: " + e.getMessage());
        }
        try {
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD mos DOUBLE");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, "Error altering MOS table: " + e.getMessage());
        }
    }

    public void updateTableVideo() {
        try {
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD video VARCHAR(250)");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, "Error altering video tables: " + e.getMessage());
        }
        try {
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD videoConference VARCHAR(250)");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, "Error altering video tables: " + e.getMessage());
        }
        try {
            stat.execute("ALTER TABLE " + tblName + " "
                    + "ADD voip VARCHAR(250)");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, "Error altering video tables: " + e.getMessage());
        }
    }

    public void updateLocationTable() {
        try {
            stat.execute("ALTER TABLE LocationData "
                    + "ADD typeID VARCHAR(1)");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("Duplicate column name")) {
                Timber.debug(CLASS_NAME, "typeID already exists");
            } else {
                Timber.error(CLASS_NAME, e.getMessage());
            }
        }
        try {
            stat.execute("ALTER TABLE LocationData "
                    + "ADD carrier VARCHAR(255)");
        } catch (SQLException e) {
            String msg = e.getMessage();
            if (msg.contains("Duplicate column name")) {
                Timber.debug(CLASS_NAME, "Carrier already exists");
            } else {
                Timber.error(CLASS_NAME, e.getMessage());
            }
        }
    }

    public void dropDatabase() {
        try {
            stat.execute("DROP SCHEMA IF EXISTS " + dbName);
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public void dropValidationTable() {
        try {
            stat.execute("DROP TABLE Validation IF EXISTS ");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public void dropTable() {
        try {
            stat.execute("DROP TABLE " + tblName + " IF EXISTS ");
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public void clearLocationData() {
        if (!Database.LocationTableEmpty()) {
            try {
                stat.execute("DELETE FROM LocationData");
            } catch (SQLException e) {
                Timber.error(CLASS_NAME, e.getMessage());
            }
        }
    }

    public void insertLocationData(String address, String lat, String lng,
            String city, String zip, String type, String carrier) {
        if (address.contains("'")) {
            address = address.replace("'", "''"); //replace ' with '' because apostrophes disrupt queries
        }
        try {
            stat.execute("INSERT INTO LocationData (address, lat, lng, city, zip, typeID, carrier) "
                    + "VALUES('"
                    + address + "', '"
                    + lat + "', '"
                    + lng + "', '"
                    + city + "', '"
                    + zip + "', '"
                    + type + "', '"
                    + carrier + "')" //typeID: 0 for address, 1 for latlong
            ); //if the query isn't valid, this will be caught, and clearLocationData() won't execute

            clearLocationData(); //
            stat.execute("INSERT INTO LocationData (address, lat, lng, city, zip, typeID, carrier) "
                    + "VALUES('"
                    + address + "', '"
                    + lat + "', '"
                    + lng + "', '"
                    + city + "', '"
                    + zip + "', '"
                    + type + "', '"
                    + carrier + "')" //typeID: 0 for address, 1 for latlong
            );

        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }

    }

    public static void updateCarrier(String carrier) {
        try {
            if (!Database.LocationTableEmpty()) {
                statement.execute("UPDATE LocationData SET carrier = '" + carrier + "';");
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public void insertData(String type, String location, String lat,
            String lng, Double upload, Double download, Double jitter,
            Double delay, Double mos, String address, String video,
            String videoConference, String voip) {
        if (address.contains("'")) {
            address = address.replace("'", "''"); //replace ' with '' because apostrophes disrupt queries
        }
        try {
            stat.execute("INSERT INTO "
                    + tblName + " (timestamp, type, location, lat, lng, "
                    + "upload, download, jitter, delay, mos, address, "
                    + "video, videoConference, voip) "
                    + "VALUES("
                    + "CURRENT_TIMESTAMP, '"
                    + type + "', '"
                    + location + "', '"
                    + lat + "', '"
                    + lng + "', "
                    + upload + ", "
                    + download + ", "
                    + jitter + ", "
                    + delay + ", "
                    + mos + ", '"
                    + address + "', '"
                    + video + "', '"
                    + videoConference + "', '"
                    + voip + "'"
                    + ");"
            );
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public void insertData(Result result) {
        insertData(result.type, result.location, result.lat, result.lng,
                result.upload, result.download, result.jitter, result.delay,
                result.mos, result.address, result.video, result.conference,
                result.voip);
    }

    public void insertSurvey(int testId, String survey) {
        try {
            stat.execute("INSERT INTO "
                    + tblName + " (testId, survey) "
                    + "VALUES("
                    + testId + "', '"
                    + survey + "'"
                    + ");"
            );
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public String getLocation() {
        String location = "";
        try {
            ResultSet rs
                    = stat.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                String city = rs.getString("city");
                String zipCode = rs.getString("zip");
                if (city.equals("N/A")) {
                    return zipCode;
                }
                if (zipCode.equals("N/A")) {
                    return city;
                }
                return city + ", " + zipCode;
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return location;
    }

    public static String getAddress() {
        String address = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");

            while (rs.next()) {
                address = rs.getString("address");
            }

        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return address;
    }

    public static String getCarrier() {
        String carrier = "";
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                carrier = rs.getString("carrier");
            }

        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return carrier;
    }

    public static String getTypeID() {
        String type = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");

            while (rs.next()) {
                type = rs.getString("typeID");
            }

        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return type;
    }

    public static String getLatitude() {
        String latitude = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");

            while (rs.next()) {
                latitude = rs.getString("lat");
            }

        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return latitude;
    }

    public static String getLongitude() {
        String longitude = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");

            while (rs.next()) {
                longitude = rs.getString("lng");
            }

        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return longitude;
    }

    public static String getCity() {
        String city = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                if (rs.getString("city") != null) {
                    city = rs.getString("city");
                }
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return city;
    }

    public static String getZip() {
        String zip = "";
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                if (rs.getString("zip") != null) {
                    zip = rs.getString("zip");
                }
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return zip;
    }

    public static String getExistingIsp() {
        String carrier = LocationService.getCarrier();
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                if (rs.getString("carrier") != null) {
                    carrier = rs.getString("carrier");
                }
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return carrier;
    }

    public static String getLastDownloadSpeed() {
        return getLastValue("download");
    }

    public static String getLastUploadSpeed() {
        return getLastValue("upload");
    }

    public static String getLastId() {
        return getLastValue("id");
    }

    private static String getLastValue(String valueType) {
        int lastIndex = getLastResultIndex();
        try {
            ResultSet rs = statement.executeQuery("SELECT * FROM results WHERE id = " + lastIndex);
            while (rs.next()) {
                if (rs.getString("id") != null && rs.isFirst()) {
                    return rs.getString(valueType);
                }
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
        return "-1.0";
    }

    private static int getLastResultIndex() {
        try {
            ResultSet rs
                    = statement.executeQuery("SELECT id FROM results ORDER BY id DESC");
            while (rs.next()) {
                if (rs.getString("id") != null && rs.isFirst()) {
                    return Integer.valueOf(rs.getString("id"));
                }
            }
            return -1;
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
            return -1;
        }
    }

    public static boolean LocationTableEmpty() {
        try {
            ResultSet rs = statement.executeQuery("SELECT * from LocationData");
            return !rs.next(); //if there's a row, it's not empty
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
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
                arrayResult.put("id", rs.getString("id"));
                arrayResult.put("timestamp", rs.getString("timestamp"));

                //take the timestamp and convert it to times and dates used by the UI
                try {
                    String ds = rs.getString("timestamp");
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date d = f.parse(ds);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    arrayResult.put("time", new SimpleDateFormat("hh:mm aa").format(cal.getTime()));
                    arrayResult.put("date", new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime()));

                } catch (ParseException e) {
                    Timber.error(CLASS_NAME, e.getMessage());
                }

                arrayResult.put("type", rs.getString("type"));
                arrayResult.put("location", rs.getString("location"));
                arrayResult.put("upload", rs.getString("upload"));
                arrayResult.put("download", rs.getString("download"));
                arrayResult.put("jitter", rs.getString("jitter"));
                arrayResult.put("delay", rs.getString("delay"));
                arrayResult.put("mos", rs.getString("mos"));
                if (rs.getString("address").length() > 0) {
                    arrayResult.put("address", rs.getString("address"));
                } else {
                    arrayResult.put("address", rs.getString("location"));
                }
                try {
                    String videoStream = rs.getString("video");
                    if (videoStream.length() > 0
                            && !videoStream.equals("null")) {
                        if (videoStream.equals("High Definition")) {
                            arrayResult.put("video", "HD");
                        } else if (videoStream.equals("Standard Definition")) {
                            arrayResult.put("video", "SD");
                        } else if (videoStream.equals("Low Definition")) {
                            arrayResult.put("video", "LD");
                        } else {
                            arrayResult.put("video", videoStream);
                        }
                    } else {
                        arrayResult.put("video", "N/A");
                    }
                } catch (Exception e) {
                    arrayResult.put("video", "N/A");
                }
                try {
                    String videoConference = rs.getString("videoConference");
                    if (videoConference == null) {
                        arrayResult.put("videoConference", "N/A");
                    } else if (videoConference.length() > 0
                            && !videoConference.equals("null")) {
                        if (videoConference.equals("High Definition")) {
                            arrayResult.put("videoConference", "HD");
                        } else if (videoConference.equals("Standard Definition")) {
                            arrayResult.put("videoConference", "SD");
                        } else if (videoConference.equals("Low Definition")) {
                            arrayResult.put("videoConference", "LD");
                        } else {
                            arrayResult.put("videoConference", videoConference);
                        }
                    } else {
                        arrayResult.put("videoConference", "N/A");
                    }
                } catch (Exception e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error retriving Video Conference: {0}",
                                    e.getMessage()));
                    arrayResult.put("videoConference", "N/A");
                }
                try {
                    String voip = rs.getString("voip");
                    if (voip == null) {
                        arrayResult.put("voip", "N/A");
                    } else if (voip.length() > 0
                            && !voip.equals("null")) {
                        arrayResult.put("voip", voip);
                    } else {
                        arrayResult.put("voip", "N/A");
                    }
                } catch (Exception e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error retriving VoIP: {0}",
                                    e.getMessage()));
                    arrayResult.put("voip", "N/A");
                }
                arrayResults.add(0, arrayResult);
            }

            Type listOfTestObject = new TypeToken<ArrayList<Map>>() {
            }.getType();
            finalResults = results.toJson(arrayResults, listOfTestObject);

        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }

        return finalResults;
    }

    public void debug() {
        try {
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName);
            while (rs.next()) {
                Timber.debug(CLASS_NAME,
                        "id:" + rs.getString("id") + " "
                        + "timestamp:" + rs.getString("timestamp") + " "
                        + "type:" + rs.getString("type") + " "
                        + "location:" + rs.getString("location") + " "
                        + "upload:" + rs.getString("upload") + " "
                        + "download:" + rs.getString("download") + " "
                        + "jitter:" + rs.getString("jitter") + " "
                        + "delay:" + rs.getString("delay") + " "
                        + "mos:" + rs.getString("mos") + " "
                        + "address: " + rs.getString("address") + " "
                        + "video: " + rs.getString("video")
                );
            }
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public void debugLocation() {
        try {
            ResultSet rs = stat.executeQuery("SELECT * FROM LocationData");
            while (rs.next()) {
                Timber.debug(CLASS_NAME,
                        "id: " + rs.getString("id") + " "
                        + "address: " + rs.getString("address") + " "
                        + "lat: " + rs.getString("lat") + " "
                        + "lng: " + rs.getString("lng") + " "
                        + "zip: " + rs.getString("zip") + " "
                        + "city: " + rs.getString("city") + " "
                        + "type: " + rs.getString("typeID") + " "
                        + "carrier: " + rs.getString("carrier")
                );
            }
        } catch (SQLException ex) {
            Timber.error(CLASS_NAME, ex.getMessage());
        }
    }

    public void closeConnection() {
        try {
            stat.close();
            conn.close();
        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    public String getDataById(int id) {
        ArrayList<Map> arrayResults = new ArrayList<>();
        String finalResults = "";

        try {
            Map<String, String> arrayResult;
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName + " WHERE id = " + id);
            while (rs.next()) {
                arrayResult = new HashMap<>();
                arrayResult.put("id", rs.getString("id"));
                arrayResult.put("timestamp", rs.getString("timestamp"));

                //take the timestamp and convert it to times and dates used by the UI
                try {
                    String ds = rs.getString("timestamp");
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date d = f.parse(ds);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    arrayResult.put("time", new SimpleDateFormat("hh:mm aa").format(cal.getTime()));
                    arrayResult.put("date", new SimpleDateFormat("MMM dd, yyyy").format(cal.getTime()));
                } catch (ParseException e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error parsing time and date: {0}", e.getMessage()));
                }

                arrayResult.put("type", rs.getString("type"));
                arrayResult.put("location", rs.getString("location"));
                arrayResult.put("upload", rs.getString("upload"));
                arrayResult.put("download", rs.getString("download"));
                arrayResult.put("jitter", rs.getString("jitter"));
                arrayResult.put("delay", rs.getString("delay"));
                arrayResult.put("mos", rs.getString("mos"));
                if (rs.getString("address").length() > 0) {
                    arrayResult.put("address", rs.getString("address"));
                } else {
                    arrayResult.put("address", rs.getString("location"));
                }

                try {
                    if (rs.getString("video") == null) {
                        arrayResult.put("video", "N/A");
                    } else if (rs.getString("video").length() > 0
                            && !rs.getString("video").equals("null")) {
                        arrayResult.put("video", rs.getString("video"));
                    } else {
                        arrayResult.put("video", "N/A");
                    }
                } catch (Exception e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error retriving Video Stream: {0}", e.getMessage()));
                    arrayResult.put("video", "N/A");
                }
                try {
                    if (rs.getString("videoConference") == null) {
                        arrayResult.put("videoConference", "N/A");
                    } else if (rs.getString("videoConference").length() > 0
                            && !rs.getString("videoConference").equals("null")) {
                        arrayResult.put("videoConference",
                                rs.getString("videoConference"));
                    } else {
                        arrayResult.put("videoConference", "N/A");
                    }
                } catch (Exception e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error retriving Video Conference: {0}",
                                    e.getMessage()));
                    arrayResult.put("videoConference", "N/A");
                }
                try {
                    if (rs.getString("voip") == null) {
                        arrayResult.put("voip", "N/A");
                    } else if (rs.getString("voip").length() > 0
                            && !rs.getString("voip").equals("null")) {
                        arrayResult.put("voip", rs.getString("voip"));
                    } else {
                        arrayResult.put("voip", "N/A");
                    }
                } catch (Exception e) {
                    Timber.error(CLASS_NAME,
                            String.format("Error retriving VoIP: {0}", e.getMessage()));
                    arrayResult.put("voip", "N/A");
                }
                arrayResults.add(0, arrayResult);
            }

            Type listOfTestObject = new TypeToken<ArrayList<Map>>() {
            }.getType();
            finalResults = results.toJson(arrayResults, listOfTestObject);
            Timber.debug(CLASS_NAME, finalResults);

        } catch (SQLException e) {
            Timber.error(CLASS_NAME, e.getMessage());
        }
        return finalResults;
    }
}
