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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

public class IPerf extends CommandLine {
    private static final String iperf_mac = "iperf_mac_10_6";
    private static final String iperf_win = "iperf";

    public void runTest(String test, String ip, String port, int timeout) {
        try {
            String command = getCommand(test, ip, port);
            FilePrep.addDetail(command);
            runCommand(command, timeout);
        } catch(Exception e) {
            handleError("looks like there was a problem.");
        }
    }

    /************************
    *                       *
    *   HELPER FUNCTIONS    *
    *                       *
    *************************/

    /**
     * 
     * @param test
     * @param ip
     * @param port
     * @return 
     */
    public String getCommand(String test, String ip, String port) {
        test = test.toLowerCase();
        String command = "";

        if(test.equals("tcp")) {
            command = Globals.USER_DIR + File.separator + (Globals.IS_OSX ? iperf_mac : iperf_win)
                    + " -c " + ip + " -p " + port + " -e -w 64k -P " + Globals.NUM_TCP_TESTS_PER_SERVER + " -i 1 -t " + Globals.NUM_SECS_OF_TEST + " -f k";
        } else if(test.equals("udp")) {
            command = Globals.USER_DIR + File.separator + (Globals.IS_OSX ? iperf_mac : iperf_win)
                    + " -c " + ip + " -u -p " + port + " -i 1 -t 1 -l 220 -b 88k -f k";
        } else {
            command = "ping " + (Globals.IS_OSX ? "-c" : "-n") + " 10 " + ip;
        }

        return command;
    }

    /*
    * name: testIP
    * purpose: 
    */
    /**
     * used to make an initial test of an ip address to make sure we can 
     * connect to it in the first place. You can pass it as many tests as you 
     * want.
     * 
     * @param ip
     * @param testCount
     * @return
     * @throws Exception 
     */
    public boolean testIP(String ip, int testCount) {
        if(Globals.IS_OSX) {
            return runCommand("ping -c " + testCount + " " + ip, 5000, false);
        } else {
            return runCommand("ping -n " + testCount + " " + ip, 5000, false);
        }
    }

    /**
     * makes use of the copyFromJar helper function and sets up the testing 
     * files depending on the operating system
     */
    public static void prepareFiles() {
        try {
            if(Globals.IS_OSX) {
                copyFromJar("/assets/", iperf_mac);
                (Runtime.getRuntime()).exec("chmod u+x " + Globals.USER_DIR + File.separator + iperf_mac);
            } else {
                copyFromJar("/assets/","cygwin1.dll");
                copyFromJar("/assets/", iperf_win + ".exe");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * removes the files that were copied
     */
    public static void cleanUpFiles()
    {
        if(Globals.IS_OSX) {
            (new File(Globals.USER_DIR + File.separator + iperf_mac)).delete();
        } else {
            (new File(Globals.USER_DIR + File.separator + iperf_win + ".exe")).delete();
            (new File(Globals.USER_DIR + File.separator + "cygwin1.dll")).delete();
        }
    }

    /**
     * used for running the test on the desktop. Depending on what OS you're 
     * running it will copy the correct executable to the working directory to 
     * make it possible to run.
     * 
     * @param path
     * @param name
     * @throws IOException 
     */
    public static void copyFromJar(String path, String name) throws IOException {
        InputStream in;
        byte[] buffer = new byte[1024];
        int read = -1;
        String decodedPath;
        FileOutputStream fos = null;
        
        in = IPerf.class.getResourceAsStream(path + name);
//        System.out.println("Debug: " + in.toString());
        decodedPath = URLDecoder.decode(Globals.USER_DIR, "UTF-8");       
//        System.out.println("Debug: decodedPath -> " + decodedPath);
        // For some reason this one need to use / instead of File.separator
        fos = new FileOutputStream(new File(decodedPath + "/" + name));

        while((read = in.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }
        fos.close();
        in.close();
    }
}