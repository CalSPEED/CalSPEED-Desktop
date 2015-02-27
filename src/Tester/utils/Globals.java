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
    
//    public static final String testerVersion = "CPUC Tester Beta v2.0";

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

//    public static final String driverID[][]={
//            {"Verizon","\"@USB\\VID_106C&PID_3718\\*\""},
//            {"AT&T","\"@USB\\VID_0F3D&PID_68a3\\3*\""},
//            {"Sprint","\"@USB\\VID_1199&PID_0301\\*\""},
//            {"Sprint","\"@USB\\VID_198F&PID_0220\\*\""},
//            {"T-Mobile","\"@USB\\VID_12D1&SUBCLASS_*\""}
//    };

    public static final Integer NUM_SECS_OF_TEST = 10;
    public static final Integer NUM_TCP_TESTS_PER_SERVER = 4;       // number of TCP tests per server
    public static final Double IPERF_BIG_NUMBER_ERROR = 9999999999.99; //iperf error puts big number in kbytes/sec data

    public static final boolean IS_OSX = System.getProperty("os.name").equals("Mac OS X");
    public static final String USER_DIR = System.getProperty("user.dir");
//    public static final String JAR_FILE = new java.io.File(GUI.Main.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();

}