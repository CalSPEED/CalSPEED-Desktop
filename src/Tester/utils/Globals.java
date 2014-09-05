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

/**
 *
 * @author CPUC
 */
public class Globals {
    public static Boolean DEBUG = false;

    public static final String testerVersion = "CPUC Tester Beta v2.0";

    public static final Integer NUM_SERVERS=2;
    public static final Integer DEFAULT_SERVER=0;
    public static final String SERVER_FILE = "/assets/servers.txt";
    public static final String serverlist[][]={
            {"N. California Server","Server IP","Server Port"},
            {"N. Virginia Server","Server IP","Server Port"}
    };

    public static final String DB_NAME = "calspeed";
    public static final String TABLE_NAME = "results";

    public static final String WEST_SERVER = serverlist[0][1];
    public static final String EAST_SERVER = serverlist[1][1];
    public static final String TCP_PORT = "TCP Port";
    public static final String UDP_PORT = "UDP Port";

    public static final String provider[]={
            "Verizon",
            "T-Mobile",
            "Sprint",
            "AT&T",
            "Ethernet",
            "Wi-Fi",
            "Other"
    };
    public static final Integer NUM_PROVIDERS = 5;
    public static final Integer NUM_TESTS = 15;  // including ping connectivity test

    // location ID validity check
    public static final Integer MIN_LOCATION = 1000;
    public static final Integer MAX_LOCATION = 3000;



    public static final String driverID[][]={
            {"Verizon","\"@USB\\VID_106C&PID_3718\\*\""},
            {"AT&T","\"@USB\\VID_0F3D&PID_68a3\\3*\""},
            {"Sprint","\"@USB\\VID_1199&PID_0301\\*\""},
            {"Sprint","\"@USB\\VID_198F&PID_0220\\*\""},
            {"T-Mobile","\"@USB\\VID_12D1&SUBCLASS_*\""}
    };

    public static final Integer IperfUDPTimeout = 60000;    // milliseconds (1 min)
    public static final Integer IperfTCPTimeout = 120000;  // milliseconds (2 min)
    public static final Integer UDP_REPORT_WAIT_SECS = 5;  //UDP last test wait time
    public static final Integer BAD_CONNECTIVITY_WAIT_SECS = 5; // wait for last ping test

    public static final int NUM_PROGRESS_STEPS = 67;

    // used in StandardTest to write to JTextAreas
    public static final Integer POST_TO_BOTH = 0;
    public static final Integer POST_TO_DETAILED = 1;
    public static final Integer POST_TO_SUMMARY = 2;
    public static final String postToDetailedResults = "1";
    public static final String postToTestResults = "0";
    public static final String postToBoth = "2";

    public static final String addToResultsList = "3"; // add to jlist of on the fly results
    public static final String updateResultsList = "4"; // update list element of results


    public static final String updateTopLatLong = "5"; //updates Lat/Long on front tab screen

    // special top text codes
    public static final String setTopText1 = "6";  // sets top of front tab text area
    public static final String setTopText2 = "9";  // sets top of front tab text area
    public static final String setcheckBlank = "0";
    public static final String setcheckGood = "1";
    public static final String setcheckBad = "2";
    public static final String setcheckDisabled = "3";
    public static final String setcheckNothing = "4";  //leave checkbox alone, as it is

    public static final String setTimeOutStatus = "7";  // for setting current test timeout status



    // number of 1 second waits to do before exiting when checking for GPS signal
    public static final Integer GPS_WAIT_COUNT = 10;
    public static final Boolean GPS_BAD_SIGNAL_DEBUG = false;

    public static final boolean androidPhone = false;

    public static final Integer NUM_SECS_OF_TEST = 10;
    public static final Integer IPERF_TCP_THREADS = 8;    //number of -P option for TCP test
    public static final Integer NUM_UDP_TESTS_PER_SERVER = 4;       // number of total 1 sec and 5 sec tests
    public static final Integer NUM_TCP_TESTS_PER_SERVER = 4;       // number of TCP tests per server
    public static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data

    // Iperf test errorState values
    public static final Integer IPERF_TEST_NO_ERRORS = 0;  // no errors
    public static final Integer IPERF_TEST_TIMEOUT = 1;  // timeout errorState
    public static final Integer IPERF_TEST_WRITE1_FAILED = 2; // write1 failed errorState
    public static final Integer IPERF_TEST_WRITE2_FAILED = 3; // write2 failed errorState
    public static final Integer IPERF_TEST_CONNECT_FAILED = 4; // connect failed errorState

    final static ImageIcon BadCheckbox = new ImageIcon("./assets/BCheck.jpg");
    final static ImageIcon GoodCheckbox = new ImageIcon("./assets/GCheck.jpg");
    final static ImageIcon BlankCheckbox = new ImageIcon("./assets/BlankCheck.jpg");

    public static final Integer BLANK_CHECKBOX = 0;
    public static final Integer GOOD_CHECKBOX = 1;
    public static final Integer BAD_CHECKBOX = 2;

    // messages for top of front tab
    public static final String MESSAGE_MAIN_APPLICATION = "         CPUC Tester Application";
    public static final String MESSAGE_ACQUIRING_GPS = "Acquiring GPS";
    public static final String MESSAGE_GPS_ACQUIRED = "GPS Acquired";
    public static final String MESSAGE_GPS_OVERIDE = "GPS Override Enabled";
    public static final String MESSAGE_GPS_NO_SIGNAL = "Error-- No GPS Signal Detected";
    public static final String MESSAGE_FILE_SAVED_LOCALLY = "File Saved Locally";
    public static final String MESSAGE_FILE_SAVING_LOCALLY = "Saving File Locally...";
    public static final String MESSAGE_FILE_NOT_SAVED = "Error-- File Not saved";
    public static final String MESSAGE_UPLOADING_FILE = "Uploading File...";
    public static final String MESSAGE_FILE_UPLOADED = "All Files Uploaded Successfully";
    public static final String MESSAGE_FILE_NOT_UPLOADED = "Error-- File Not Uploaded";
    public static final String MESSAGE_NO_FILES = "No Files Found";
    public static final String MESSAGE_STOP = "CPUC Tester Stopped";
    
    public static final boolean IS_OSX = System.getProperty("os.name").equals("Mac OS X");
    public static final String USER_DIR = System.getProperty("user.dir");
    public static final String JAR_FILE = new java.io.File(GUI.Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();

}