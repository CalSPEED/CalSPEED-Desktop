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

public class Database {
    private String dbName;
    private Connection conn;
    private Statement stat;
    private String tblName;
    private Gson results;

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
        } catch (ClassNotFoundException classE ) {
            classE.printStackTrace();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void createInit() {
        try {
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
                            + "delay DOUBLE)"
            );
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void dropDatabase() {
        try {
            stat.execute("DROP SCHEMA IF EXISTS " + dbName);
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void dropTable() {
        try {
            stat.execute("DROP TABLE " + tblName + " IF EXISTS ");
        } catch(SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void createFromFile(String filename) {
        try {
            stat.execute("runscript from '" + filename + "'");
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void insertData(String type, String location, String lat, String lng, Double upload, Double download, Double jitter, Double delay) {
        try {
            stat.execute("INSERT INTO " +
                            tblName + "(timestamp, type, location, lat, lng, upload, download, jitter, delay) " +
                            "VALUES(" +
                            "CURRENT_TIMESTAMP, '" +
                            type + "', '" +
                            location + "', '" +
                            lat + "', '" +
                            lng + "', " +
                            upload + ", " +
                            download + ", " +
                            jitter + ", " +
                            delay + ")"
            );
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void insertData(Result result) {
        insertData(result.type, result.location, result.lat, result.lng, result.upload, result.download, result.jitter, result.delay);
    }

    public String selectData() {
        ArrayList<Map> arrayResults = new ArrayList<Map>();
        String finalResults = "";

        try {
            Map<String, String> arrayResult;
            ResultSet rs = stat.executeQuery("SELECT * FROM " + tblName);
            while (rs.next()) {
                arrayResult = new HashMap<String, String>();
                arrayResult.put("timestamp", rs.getString("timestamp"));

                //take the timestamp and convert it to times and dates used by the UI
                try {
                    String ds = rs.getString("timestamp");
                    SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    Date d = f.parse(ds);

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);

                    arrayResult.put("time", new SimpleDateFormat("HH:mm").format(cal.getTime()));
                    arrayResult.put("date", new SimpleDateFormat("dd MMM, yyyy").format(cal.getTime()));

                } catch(ParseException e) {
                    e.printStackTrace();
                }
                arrayResult.put("type", rs.getString("type"));
                arrayResult.put("location", rs.getString("location"));
                arrayResult.put("upload", rs.getString("upload"));
                arrayResult.put("download", rs.getString("download"));
                arrayResult.put("jitter", rs.getString("jitter"));
                arrayResult.put("delay", rs.getString("delay"));

                arrayResults.add(0, arrayResult);
            }

            Type listOfTestObject = new TypeToken<ArrayList<Map>>(){}.getType();
            finalResults = results.toJson(arrayResults, listOfTestObject);

        } catch(SQLException sqlE) {
            sqlE.printStackTrace();
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
                                "delay:" + rs.getString("delay")
                );
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            stat.close();
            conn.close();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }
}