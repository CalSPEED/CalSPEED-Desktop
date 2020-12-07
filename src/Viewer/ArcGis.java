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
import java.util.logging.Level;
import logging.Timber;

/**
 * Simply meant to grab results from ARCGis. Format them and send them back (in
 * the form of json)
 */
public class ArcGis extends Task {

    private String lat;
    private String lng;
    private String type;
    private StringProperty finalValue = new SimpleStringProperty();
    public final String UPLOAD_KEY = "UploadKey";
    public final String DOWNLOAD_KEY = "DownloadKey";
    public final String TECH_CODE = "TechCode";
    public final String FIXED = Globals.FIXED;
    public final String FIXED_WIRELESS = "Fixed Wireless";
    public final String WIRELINE = "Wireline";
    public final String MOBILE = Globals.MOBILE;
    public final String ATTRIBUTES = "attributes";
    public final String ERROR = "error";
    private final static String CLASSNAME = ArcGis.class.getName();

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
        if (type.equals(Globals.ADVERTISED)) {
            getAdvertisedData();
        }
        return null;
    }

    protected void getAdvertisedData() {
        try {
            String[] coordinates = latLngToXY(this.lat, this.lng);
            //only should get the location when coordinates are actually returned...
            if (coordinates != null) {
                Timber.debug(CLASSNAME, "URL: " + String.format(Globals.ARCGIS_MAPSERVER_URL, coordinates[0], coordinates[1],
                        Globals.OUT_SECRET, Globals.OUT_SECRET));
                String allLines = urlRequestHelper(String.format(Globals.ARCGIS_MAPSERVER_URL, coordinates[0], coordinates[1],
                        Globals.OUT_SECRET, Globals.OUT_SECRET));
                JsonArray jarray = getFeaturesAsJsonArray(allLines);
                String reducedJsonObjects = reduceData(jarray);
                setFinalValueProperty(reducedJsonObjects);
            }
        } catch (Exception e) {
            Timber.error(CLASSNAME, "JSON ERROR: " + e.getMessage());
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
        String allLines = urlRequestHelper(String.format(Globals.LATLONG_XY_URL, 
                Globals.IN_SECRET, Globals.OUT_SECRET, lat, lng));
        if (!allLines.equals("")) {
            JsonElement jelement = new JsonParser().parse(allLines);
            JsonObject jobject = jelement.getAsJsonObject()
                    .getAsJsonArray("geometries").get(0).getAsJsonObject();
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
        while ((line = rd.readLine()) != null) {
            if (isCancelled()) {
                return "";
            }
            allLines += line;
        }
        return allLines;
    }

    /**
     * @param json
     * @return JsonArray that converts the String response from the URL to a
     * JsonArray for easier data manipulation
     *
     */
    protected JsonArray getFeaturesAsJsonArray(String json) {
        try {
            JsonElement jelement = new JsonParser().parse(json);
            return jelement.getAsJsonObject().get("features").getAsJsonArray();
        } catch (Exception e) {
            Timber.error(CLASSNAME, "ERROR: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reduce the data to just the name, upload value, download value, service
     * type,and technology code
     *
     * @param jsonArray
     * @return
     */
    protected String reduceData(JsonArray jsonArray) {
        TreeSet<JsonObject> reducedJsonData = new TreeSet<>(
                new GisDownloadComparator(Globals.DBA_NAME, UPLOAD_KEY, DOWNLOAD_KEY, TECH_CODE)
        );
        Timber.verbose(CLASSNAME, "reduceData json array size:" + String.valueOf(jsonArray.size()));
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject attribute = jsonArray.get(i).getAsJsonObject().get(ATTRIBUTES)
                    .getAsJsonObject();
            if ((attribute.get(Globals.CONS_BUS).getAsString()).equalsIgnoreCase("consumer")) {
                String serviceTyp = attribute.get(Globals.SERVICETYPE).getAsString().trim();
                String uploadKey;
                String downloadKey;
                Timber.verbose(CLASSNAME, "Service Type: " + serviceTyp);
                if (serviceTyp.equalsIgnoreCase(WIRELINE)) {
                    uploadKey = Globals.MAX_AD_UP;
                    downloadKey = Globals.MAX_AD_DN;
                } else if (serviceTyp.equalsIgnoreCase(FIXED_WIRELESS)) {
                    uploadKey = Globals.MAX_AD_UP;
                    downloadKey = Globals.MAX_AD_DN;
                } else if (serviceTyp.equalsIgnoreCase(MOBILE)) {
                    uploadKey = Globals.MIN_AD_UP;
                    downloadKey = Globals.MIN_AD_DOWN;
                } else {
                    uploadKey = " ";
                    downloadKey = " ";
                }
                Timber.verbose(CLASSNAME, "upload key: " + uploadKey + ", download key: " + downloadKey);
                String uploadValue
                        = attribute.get(uploadKey).getAsString().equals(" ") ? "0"
                        : attribute.get(uploadKey).getAsString();
                String downloadValue
                        = attribute.get(downloadKey).getAsString().equals(" ")
                        ? "0" : attribute.get(downloadKey).getAsString();
                Timber.verbose(CLASSNAME, "upload value: " + uploadValue + ", download value: " + downloadValue);
                // Get values to write into the TreeSet of JsonObjects
                int techCode = attribute.get(TECH_CODE).getAsInt();
                Integer uploadBucketValue = convertBandwidthToBucket(uploadValue);
                Integer downloadBucketValue = convertBandwidthToBucket(downloadValue);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty(Globals.DBA_NAME,
                        attribute.get(Globals.DBA_NAME).getAsString());
                jsonObject.addProperty(UPLOAD_KEY, uploadBucketValue);
                jsonObject.addProperty(DOWNLOAD_KEY, downloadBucketValue);
                jsonObject.addProperty(Globals.SERVICETYPE, serviceTyp);
                jsonObject.addProperty(TECH_CODE, TechnologyCode.get(techCode));
                Timber.verbose(CLASSNAME, "Provider Name: " + attribute.get(Globals.DBA_NAME).getAsString());
                Timber.verbose(CLASSNAME, "Tech Code: " + TechnologyCode.get(techCode));
                reducedJsonData.add(jsonObject);
            }
        }
        Timber.verbose(CLASSNAME, new Gson().toJson(reducedJsonData));
        return new Gson().toJson(reducedJsonData);
    }

    /**
     * @param bandwidth_as_string
     * @return The bucket size in string Convert the actual bandwidth values to
     * a bucket size (between 1 and 11)
     */
    private Integer convertBandwidthToBucket(String bandwidth_as_string) {
        Double bandwidth = Double.parseDouble(bandwidth_as_string);
        if (bandwidth < 0.2) {
            return 1;
        } else if (bandwidth >= 0.2 && bandwidth < 0.75) {
            return 2;
        } else if (bandwidth >= 0.75 && bandwidth < 1.5) {
            return 3;
        } else if (bandwidth >= 1.5 && bandwidth < 3) {
            return 4;
        } else if (bandwidth >= 3 && bandwidth < 6) {
            return 5;
        } else if (bandwidth >= 6 && bandwidth < 10) {
            return 6;
        } else if (bandwidth >= 10 && bandwidth < 25) {
            return 7;
        } else if (bandwidth >= 25 && bandwidth < 50) {
            return 8;
        } else if (bandwidth >= 50 && bandwidth < 100) {
            return 9;
        } else if (bandwidth >= 100 && bandwidth < 1000) {
            return 10;
        } else if (bandwidth >= 1000) {
            return 11;
        } else {
            return null;
        }
    }

    public String getLat() {
        return this.lat;
    }

    public String getLng() {
        return this.lng;
    }

    protected final void setFinalValueProperty(String value) {
        this.finalValue.set(value);
    }

    public StringProperty finalValueProperty() {
        return this.finalValue;
    }

    @Override
    public void succeeded() {
        super.succeeded();
        //if the final value hasn't changed then we know we have 0 results
        if (this.finalValue.get().equals("")) {
            this.finalValue.set(ERROR);
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
        return Globals.JSON_ERROR;
    }

    public String getByType(String json, String type) {
        try {
            JsonElement jelement = new JsonParser().parse(json);
            JsonArray jarray = jelement.getAsJsonArray();
            List<JsonObject> jsonObjects = new ArrayList<>();
            for (int i = 0; i < jarray.size(); i++) {
                JsonObject jobject = jarray.get(i).getAsJsonObject();
                jobject = jobject.get("members").getAsJsonObject();
                if (type.equalsIgnoreCase(FIXED)) {
                    if ((jobject.get(Globals.SERVICETYPE).getAsString()).equalsIgnoreCase(FIXED_WIRELESS)
                            || (jobject.get(Globals.SERVICETYPE).getAsString()).equalsIgnoreCase(WIRELINE)) {
                        jsonObjects.add(jobject);
                    }
                } else if ((jobject.get(Globals.SERVICETYPE).getAsString()).equalsIgnoreCase(type)) {
                    jsonObjects.add(jobject);
                }
            }
            if (jsonObjects.size() > 0) {
                return new Gson().toJson(jsonObjects);
            }
        } catch (Exception e) {
            Timber.error(CLASSNAME, json);
            Timber.error(CLASSNAME, e.toString());
        }
        return ERROR;
    }
}
