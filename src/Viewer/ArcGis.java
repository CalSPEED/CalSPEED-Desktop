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
package Viewer;

import Tester.utils.Globals;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import Viewer.utils.GisDownloadComparator;
import Viewer.utils.TechnologyCode;
import java.util.TreeSet;
/**
 * Simply meant to grab results from ARCGis. Format them and send them back (in the form of json)
 */
public class ArcGis extends Task {

    private String lat;
    private String lng;
    private String type;
    private StringProperty finalValue = new SimpleStringProperty();

    public ArcGis(String lat, String lng, String type) {
        this.lat = lat;
        this.lng = lng;
        this.type = type;
    }

    /**
     * @return
     */
    @Override
    public Void call() {
        if(type.equals("advertised")) {
            getAdvertisedData(); //they should be on their own threads
        }
        return null;
    }

    protected void getAdvertisedData() {
        try {
            String[] coordinates = latLngToXY(this.lat, this.lng);
            //only should get the location when coordinates are actually returned...
            if(coordinates != null) {
                String allLines = urlRequestHelper(String.format(Globals.ARC_GIS_SERVER_URL, coordinates[0], coordinates[1]));
                JsonArray jarray = getFeaturesAsJsonArray(allLines);
                String reducedJsonObjects = reduceData(jarray);
                setFinalValueProperty(reducedJsonObjects);
            }
        } catch(Exception e) {
            setFinalValueProperty(getJSONError());
        }
    }

    /**
     * @param lat
     * @param lng
     * @return
     * @throws IOException
     */
    protected String[] latLngToXY(String lat, String lng) throws IOException {
        String allLines = urlRequestHelper(String.format(Globals.LATLONG_XY_URL, lat, lng));

        //we got results!
        if(!allLines.equals("")) {
           JsonElement jelement = new JsonParser().parse(allLines);
           JsonObject jobject = jelement.getAsJsonObject()
                                        .getAsJsonArray("geometries")
                                        .get(0).getAsJsonObject();
           String x = jobject.get("x").toString();
           String y = jobject.get("y").toString();

           return new String[]{x, y};
        }

        return null;
    }

    protected String urlRequestHelper(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        String allLines = "";
        while((line = rd.readLine()) != null) {
            if(isCancelled()) {
                return "";
            }
            allLines += line;
        }
//for debugging
//        System.out.println(allLines);

        return allLines;
    }
    
    /**
     * @param json
     * @return JsonArray that converts the String response from the URL 
     * to a JsonArray for easier data manipulation
     * 
     */
    
    protected JsonArray getFeaturesAsJsonArray(String json) {
        JsonElement jelement =  new JsonParser().parse(json);
        return jelement.getAsJsonObject().get("features").getAsJsonArray();
    }
    
    /**
     * Reduce the data to just the name, upload value, download value, service 
     * type,and technology code
     * @param jsonArray
     * @return 
     */
    protected String reduceData(JsonArray jsonArray) {
        String uploadKey;
        String downloadKey;
        TreeSet<JsonObject> reducedJsonData = new TreeSet<>(
                new GisDownloadComparator(
                        Globals.dbANAME, "UploadKey", "DownloadKey", "TechCode")
        );
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject attribute = jsonArray.get(i).getAsJsonObject()
                    .get("attributes").getAsJsonObject();
            
            if (attribute.get("ServiceTyp").getAsString().equals("Fixed")) {
                uploadKey = Globals.maxADUP;
                downloadKey = Globals.maxADDOWN;
            } else {
                uploadKey = Globals.interpMBUp;
                downloadKey = Globals.interpMBDN;
            }
            String uploadValue = 
                    attribute.get(uploadKey).getAsString().equals(" ") ? "0" 
                    : attribute.get(uploadKey).getAsString();
            String downloadValue = 
                    attribute.get(downloadKey).getAsString().equals(" ") 
                    ? "0" : attribute.get(downloadKey).getAsString();
            int techCode = attribute.get("TechCode").getAsInt();
            String techCodeType = 
                    TechnologyCode.techCodeDictionary.get(techCode);
            
            if (attribute.get("ServiceTyp").getAsString().equals("Fixed")) {
                uploadValue = convertBandwidthToBucket(uploadValue);
                downloadValue = convertBandwidthToBucket(downloadValue);
            }
            
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(Globals.dbANAME, 
                    attribute.get(Globals.dbANAME).getAsString());
            jsonObject.addProperty("UploadKey", 
                    Integer.parseInt(uploadValue));
            jsonObject.addProperty("DownloadKey", 
                    Integer.parseInt(downloadValue));
            jsonObject.addProperty("ServiceTyp", 
                    attribute.get("ServiceTyp").getAsString());
            jsonObject.addProperty("TechCode", techCodeType);
            reducedJsonData.add(jsonObject);
        }
        return new Gson().toJson(reducedJsonData);
    }

    /**
     * @param bandwidth_as_string
     * @return The bucket size in string
     * Convert the actual bandwidth values to a bucket size (between 1 and 11)
     */
    private String convertBandwidthToBucket(String bandwidth_as_string) {
        Double bandwidth = Double.parseDouble(bandwidth_as_string);
        if (bandwidth < 0.2) {
            return "1";
        } else if (bandwidth >= 0.2 && bandwidth < 0.75) {
            return "2";
        } else if (bandwidth >= 0.75 && bandwidth < 1.5) {
            return "3";
        } else if (bandwidth >= 1.5 && bandwidth < 3) {
            return "4";
        } else if (bandwidth >= 3 && bandwidth < 6) {
            return "5";
        } else if (bandwidth >= 6 && bandwidth < 10) {
            return "6";
        } else if (bandwidth >= 10 && bandwidth < 25) {
            return "7";
        } else if (bandwidth >= 25 && bandwidth < 50) {
            return "8";
        } else if (bandwidth >= 50 && bandwidth < 100) {
            return "9";
        } else if (bandwidth >= 100 && bandwidth < 1000) {
            return "10";
        } else if (bandwidth >= 1000) {
            return "11";
        } else {
            return "null";
        }
    }

    public String getLat() {
        return this.lat;
    }

    public String getLng() {
        return this.lng;
    }

    protected final void setFinalValueProperty(String value) { this.finalValue.set(value); }

    public StringProperty finalValueProperty() { return this.finalValue; }

    @Override
    public void succeeded() {
        super.succeeded();
        //if the final value hasn't changed then we know we have 0 results
        if(this.finalValue.get().equals("")) {
            this.finalValue.set("error");
        }
    }

    @Override
    public void cancelled() {
        this.succeeded();
    }

    @Override
    public void failed() {
        this.succeeded();
    }

    public static String getJSONError() {
        return "[{\"error\":true}]";
    }

    public static String getByType(String json, String type) {
        try {
            JsonElement jelement = new JsonParser().parse(json);
            JsonArray jarray = jelement.getAsJsonArray();

            List<JsonObject> jsonObjects = new ArrayList<>();
            for (int i = 0; i < jarray.size(); i++) {
                JsonObject jobject = jarray.get(i).getAsJsonObject();
                jobject = jobject.get("members").getAsJsonObject();
                if (type.equals(jobject.get("ServiceTyp").getAsString())) {
                    jsonObjects.add(jobject);
                }
            }
            if (jsonObjects.size() > 0) {
                return new Gson().toJson(jsonObjects);
            }
        } catch(Exception e) {}

        return "error";
    }
}
