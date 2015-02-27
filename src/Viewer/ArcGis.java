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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import Viewer.utils.GisDownloadComparator;
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
        } else {
            getPredictedData();
        }

        return null;
    }

    protected void getAdvertisedData() {
        try {
            String[] coordinates = latLngToXY(this.lat, this.lng);
            //only should get the location when coordinates are actually returned...
            if(coordinates != null) {
                String allLines = urlRequestHelper("http://IP_ADDRESS/ArcGIS/rest/services/MOBILE_VIEWER_APP_mod/MapServer/0/query?text=&geometry=%7B%22x%22%3A\"" + coordinates[0] + "\"%2C%22y%22%3A\"" + coordinates[1] + "\"%2C%22spatialReference%22%3A%7B%22wkid%22%3A102113%7D%7D&geometryType=esriGeometryPoint&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&objectIds=&where=&time=&returnCountOnly=false&returnIdsOnly=false&returnGeometry=false&maxAllowableOffset=&outSR=&outFields=*&f=pjson");
                setFinalValueProperty(sortJsonResults(allLines, "DBANAME", "MAXADUP", "MAXADDOWN"));
                System.out.println(sortJsonResults(allLines, "DBANAME", "MAXADUP", "MAXADDOWN"));
            }
        } catch(Exception e) {
            setFinalValueProperty(getJSONError());
        }
    }

    protected void getPredictedData() {
        try {
            String[] coordinates = latLngToXY(this.lat, this.lng);
            //only should get the location when coordinates are actually returned...
            if(coordinates != null) {
                String allLines = urlRequestHelper("http://IP_ADDRESS/ArcGIS/rest/services/MOBILE_VIEWER_APP_mod/MapServer/1/query?text=&geometry={%22x%22%3A" + coordinates[0] + "%2C%22y%22%3A" + coordinates[1] + "%2C%22spatialReference%22%3A{%22wkid%22%3A102113}}&geometryType=esriGeometryPoint&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&objectIds=&where=&time=&returnCountOnly=false&returnIdsOnly=false&returnGeometry=false&maxAllowableOffset=&outSR=&outFields=*&f=json");
                setFinalValueProperty( sortJsonResults(allLines, "DBANAME", "MUP", "MDOWN") );
                System.out.println( sortJsonResults(allLines, "DBANAME", "MUP", "MDOWN") );
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
        String allLines = urlRequestHelper("http://tasks.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer/project?inSR=4326&outSR=102113&geometries=" + lat + "%2C+" + lng + "&f=pjson");

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
        System.out.println(urlString);
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
     *
     * @param json
     * @param carrierKey
     * @param uploadKey
     * @param downloadKey
     */
    protected String sortJsonResults(String json, String carrierKey, String uploadKey, String downloadKey) {
        //duplicates need to be trimmed out
        json = trimJsonResults(json, carrierKey, uploadKey, downloadKey);
        JsonElement jelement = new JsonParser().parse(json);
        JsonArray jarray = jelement.getAsJsonArray();
        List<JsonObject> jsonObjects = new ArrayList<JsonObject>();

        for(int i = 0; i < jarray.size(); i++) {
            jsonObjects.add(jarray.get(i).getAsJsonObject());
        }

        //now we can sort because it's in a collection
        Collections.sort( jsonObjects, new GisDownloadComparator(carrierKey, uploadKey, downloadKey) );

        return new Gson().toJson(jsonObjects);
    }

    /**
     *
     * @param json
     * @param carrierKey
     * @param uploadKey
     * @param downloadKey
     * @return
     * This method will trim the json results from arcgis so that the results that have a duplicate DBANAME will
     * use the highest download and upload values. After that we can sort by the highest download and then upload
     * respectively. NOTE: not heavily tested
     */
    protected String trimJsonResults(String json, String carrierKey,String uploadKey, String downloadKey) {
        JsonElement jelement = new JsonParser().parse(json);
        JsonArray jarray = jelement.getAsJsonObject()
                                   .get("features").getAsJsonArray();

        JsonArray trimmed = new JsonArray();
        //this assumes that the features have already been sorted properly
        for(int i = 0; i < jarray.size(); i++) {
            JsonObject a = jarray.get(i).getAsJsonObject().get("attributes").getAsJsonObject();
            int aupload = a.get(uploadKey).getAsString().equals(" ") ? 0 : Integer.valueOf(a.get(uploadKey).getAsString());
            int adownload = a.get(downloadKey).getAsString().equals(" ") ? 0 : Integer.valueOf(a.get(downloadKey).getAsString());
            for(int j = i; j < jarray.size(); j++) {
                i = j; //also allows us to skip over the duplicates...
                JsonObject b = jarray.get(j).getAsJsonObject().get("attributes").getAsJsonObject();
                int bupload = b.get(uploadKey).getAsString().equals(" ") ? 0 : Integer.valueOf( b.get(uploadKey).getAsString() );
                int bdownload = b.get(downloadKey).getAsString().equals(" ") ? 0 : Integer.valueOf( b.get(downloadKey).getAsString() );
                if( !a.get(carrierKey).getAsString().equals( b.get(carrierKey).getAsString() ) ) {
                    i = j - 1; //allows us to skip over the duplicates...
                    break;
                }
                if( aupload < bupload ) {
                    aupload = bupload;
                }

                if( adownload < bdownload ) {
                    adownload = bdownload;
                }
            }
            //addProperty also overrides the one that existed before...
            a.addProperty(uploadKey, aupload);
            a.addProperty(downloadKey, adownload);
            trimmed.add(a);
        }

//for debugging
//        for(int i = 0; i < trimmed.size(); i++) {
//            JsonObject jobject = trimmed.get(i).getAsJsonObject();
//            System.out.println( "CARRIER: " + jobject.get(carrierKey).getAsString() );
//            System.out.println( "   UPLOAD:" + jobject.get(uploadKey).getAsString() );
//            System.out.println( "   DOWNLOAD:" + jobject.get(downloadKey).getAsString() );
//            System.out.println( "" );
//        }

        if(trimmed.size() > 0) {
            return new Gson().toJson(trimmed);
        } else {
            return "";
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

            List<JsonObject> jsonObjects = new ArrayList<JsonObject>();
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
