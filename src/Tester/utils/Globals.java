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
package Tester.utils;

import javax.swing.ImageIcon;

public class Globals {
    public static Boolean DEBUG_MOS = false;
    
    public static final String serverlist[][]={
      {"N. California Server","Server IP","Server Port"},
      {"N. Virginia Server","Server IP","Server Port"}
    };
    
    public static final String DB_NAME = "calspeed";
    public static final String TABLE_NAME = "results";
    
    public static final String WEST_SERVER = serverlist[0][1];
    public static final String EAST_SERVER = serverlist[1][1];
    public static final String TCP_PORT = "TCP PORT";
    public static final String UDP_PORT = "UDP PORT";

    public static final Integer NUM_SECS_OF_TEST = 10;
    public static final Integer NUM_TCP_TESTS_PER_SERVER = 4;       // number of TCP tests per server
    public static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data

    public static final boolean IS_OSX = System.getProperty("os.name").equals("Mac OS X");
    public static final String USER_DIR = System.getProperty("user.dir");

    public static final boolean doneProg = false;
    
    public static final String ARC_GIS_SERVER_URL = "http://IP_ADDRESS/ArcGIS/rest/services/"
        + "CalSPEED_Mobile_App/MapServer/0/query?text=&geometry=%s,%s&"
        + "geometryType=esriGeometryPoint&inSR=IN_SECRET&spatialRel=esriSpatialRelIntersects&relationParam"
        + "=&objectIds=&where=&time=&returnCountOnly=false&returnIdsOnly=false&returnGeometry=false"
        + "&maxAllowableOffset=&outSR=OUT_SECRET&outFields=*&f=pjson";
    public static final String LATLONG_XY_URL = "http://IP_ADDRESS/ArcGIS/rest/"
        + "services/Geometry/GeometryServer/project?inSR=IN_SECRET&outSR=OUT_SECRET&geometries=%s,%s&f=json";
    
    public static final String dbANAME = "DBA";
    public static final String maxADDOWN = "MaxAdDn";
    public static final String maxADUP = "MaxAdUp";
    public static final String ServiceTyp = "ServiceTyp";
    public static final String interpMBDN = "InterpMBDn";
    public static final String interpMBUp = "InterpMBUp";
}
