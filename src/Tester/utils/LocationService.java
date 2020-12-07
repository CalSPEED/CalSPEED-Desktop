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

package Tester.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import logging.Timber;

public class LocationService {

    private final static String CLASSNAME = LocationService.class.getName();
    private final static String ERROR = "ERROR";
    private static String location = "Not found";
    private static String lat = "N/A";
    private static String lng = "N/A";
    private static String city = "N/A";
    private static String zip = "N/A";
    private static String carrier = "N/A";

    public static void setLocation() {
        String ip = getIp();
        if (ip.equals(ERROR)) {
            location = lat = lng = city = zip = carrier = "N/A";
        } else {
            try {
                ArrayList<String> locationInfo = (ArrayList<String>) maxMindInfo(ip);
                if (locationInfo.size() > 0) {
                    //assumes that the array indexes will remain constant...
                    location = locationInfo.get(2);
                    if (locationInfo.get(3) != null) {
                        location += ", " + locationInfo.get(3);
                    }
                    city = locationInfo.get(2);
                    zip = locationInfo.get(3);
                    lat = locationInfo.get(4);
                    lng = locationInfo.get(5);
                    carrier = locationInfo.get(8);
                    if (carrier == null) {
                        Timber.info(CLASSNAME, "Carrier info is null");
                        carrier = "N/A";
                    }
                } else {
                    Timber.debug(CLASSNAME, "MaxMind info is empty");
                    location = lat = lng = city = zip = carrier = "N/A";
                }
            } catch (IOException e) {
                Timber.error(CLASSNAME, e.getMessage());
                location = lat = lng = city = zip = carrier = "N/A";
            }
        }
    }

    /**
     *
     * @return
     */
    public static String getIp() {
        //  Public IP Address of the Host.
        try {
            URL url = new URL("http://checkip.amazonaws.com/");
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String ipAddress = (in.readLine()).trim();

            if (!(ipAddress.length() > 0)) {
                Timber.error(CLASSNAME, "Ip address length is 0");
                return ERROR;
            }

            return (ipAddress);
        } catch (Exception e) {
            Timber.error(CLASSNAME, e.getMessage());
            return ERROR;
        }
    }

    /**
     *
     * @param ip
     * @return
     * @throws IOException
     */
    public static ArrayList maxMindInfo(String ip) throws IOException {
        String ip_address = ip;
        ArrayList fields = new ArrayList();
        String url_str = "http://geoip.maxmind.com/f?l=" + Globals.MAXMIND_LICENSE + "&i=" + ip_address;
        URL url = new URL(url_str);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()))) {
            String inLine;
            while ((inLine = in.readLine()) != null) {
                Pattern p = Pattern.compile("\"([^\"]*)\"|(?<=,|^)([^,]*)(?:,|$)");
                Matcher m = p.matcher(inLine);
                String f;
                while (m.find()) {
                    f = m.group(1);
                    if (f != null) {
                        fields.add(f);
                    } else {
                        fields.add(m.group(2));
                    }
                }
            }
        }
        return fields;
    }

    /**
     * get the location.
     *
     * @return
     */
    public static String getLocation() {
        return location;
    }

    /**
     * get the latitude.
     *
     * @return the Latitude of location.
     */
    public static String getLat() {
        return lat;
    }

    /**
     * get the Longitude.
     *
     * @return the Longitude of location.
     */
    public static String getLng() {
        return lng;
    }

    public static String getCity() {
        return city;
    }

    public static String getZip() {
        return zip;
    }

    /**
     * get the ISP.
     *
     * @return the ISP or Carrier base on the ip whois service check.
     */
    public static String getCarrier() {
        return carrier;
    }

    /**
     * Set the ISP. This case is for user input on the Location Settings page.
     *
     * @param newCarrier the new carrier to update the current one
     */
    public static void setCarrier(String newCarrier) {
        carrier = newCarrier;
    }

    public static void updateCarrier() {
        String ip = getIp();
        if (ip.equals(ERROR)) {
            Timber.info(CLASSNAME, "No IP address");
            carrier = "N/A";
        } else {
            try {
                ArrayList<String> locationInfo = (ArrayList<String>) maxMindInfo(ip);
                if (locationInfo.size() > 0) {
                    carrier = locationInfo.get(8);
                } else {
                    Timber.info(CLASSNAME, "Carrier not founs");
                    carrier = "N/A";
                }
            } catch (IOException e) {
                Timber.error(CLASSNAME, e.getMessage());
                carrier = "N/A";
            }
        }
    }
}
