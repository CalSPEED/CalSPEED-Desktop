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

import java.util.ArrayList;

public class Globals {    
    public static Boolean DEBUG_MOS = true;
    
    public static final boolean IS_OSX = System.getProperty("os.name").equals("Mac OS X");
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String PROJECT_PROPERTIES = String.format("%s/nbproject/project.properties", USER_DIR);
    
    public static final String IPERF_MAC = "iperf_mac_x64";
    public static final String IPERF_WIN = "iperf";
    
    public static final String TCP_PORT = "";
    public static final String UDP_PORT = "";
    public static String WEST_SERVER = "";
    public static String EAST_SERVER = "";
    
    public static String VERSION_NUMBER = "1.3.2";
    public static String FTP_USERNAME = "";
    public static String FTP_PASSWORD = "";
    public static String FTP_SERVER = "";
    public static String MAXMIND_LICENSE = "";
    
    /** DATABASE **/
    public static final String DB_NAME = "calspeed";
    public static final String TABLE_NAME = "results";
    
    /** LOGGING **/
    public static final String LOG_NAME = "logs/CalSPEED.log";
    public static final int LOG_SIZE = 1024000;
    public static final int LOG_ROTATION_COUNT = 3;
    public static final String LOGGING_CONFIG = "logging.properties";
    
    public static final Integer NUM_TCP_TESTS_PER_SERVER = 4;
    public static final Integer NUM_UDP_TESTS_PER_SERVER = 4;
    public static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data
    
    /** DEFAULTS **/
    public static final Integer DEFAULT_TCP_THREAD_NUMBER = 4;
    public static final String DEFAULT_TCP_WINDOW_SIZE = "64k";
    public static final String DEFAULT_TCP_FORMAT = "k";
    public static Integer DEFAULT_TCP_TEST_INTERVAL = 1;
    public static Integer DEFAULT_TCP_TEST_TIME = 10;
    public static Integer DEFAULT_TCP_TIMEOUT = 60000;
    
    public static Integer DEFAULT_UDP_THREAD_NUMBER = 1;
    public static Integer DEFAULT_UDP_TEST_INTERVAL = 1;
    public static Integer DEFAULT_UDP_TEST_TIME = 1;
    public static Integer DEFAULT_UDP_BUFFER_LENGTH = 220;
    public static String DEFAULT_UDP_BLOCK_SIZE = "88k";
    public static String DEFAULT_UDP_FORMAT = "k";
    public static Integer DEFAULT_UDP_TIMEOUT = 30000;
    
    public static Integer DEFAULT_PING_COUNT = 10;
    public static Integer DEFAULT_PING_CONNECT_COUNT = 4;
    public static Integer DEFAULT_PING_THREAD_NUMBER = 1;
    
    /** TEST TYPES **/
    public static final String TCP = "tcp";
    public static final String UDP = "udp";
    public static final String PING = "ping";
    
    public static final int IN_SECRET = -1;
    public static final int OUT_SECRET = -1;
    public static final String ARCGIS_MAPSERVER_URL = "localhost";
    public static final String LATLONG_XY_URL = "localhost";
    
    public static final String ADVERTISED = "advertised";
    public static final String FIXED = "Fixed";
    public static final String MOBILE = "Mobile";
    public static final String CONS_BUS = "CONS_BUS";
    public static final String DBA_NAME = "DBA";
    public static final String MAX_AD_DN = "MaxAdDn";
    public static final String MAX_AD_UP = "MaxAdUp";
    public static final String SERVICETYPE = "ServiceTyp";
    public static final String MIN_AD_DOWN = "MinAdDn";
    public static final String MIN_AD_UP = "MinAdUp";
    public static final String JSON_ERROR = "[{\"error\":true}]";
    
    public static int WEST_UPLOAD = 1;
    public static int WEST_DOWNLOAD = 2;
    public static int EAST_UPLOAD = 3;
    public static int EAST_DOWNLOAD = 4;
    public static int whichTest = WEST_UPLOAD;
    
    public static ArrayList<ThreadData> probeThreadData = new ArrayList<>();
    public static ArrayList<ThreadData> threadData = new ArrayList<>();
    
    public static int ERROR = 0;
    public static int WARNING = 1;
    public static int INFO = 2;
    public static int DEBUG = 3;
    public static int VERBOSE = 4;
    
    public static final int PROBE_TEST = 0;
    public static int TIER_ZERO = 0;
    public static int TIER_ONE = 1;
    public static int TIER_TWO = 2;
    public static int TIER_THREE = 3;
    public static int TIER_FOUR = 4;
    public static int TIER_FIVE = 5;
}
