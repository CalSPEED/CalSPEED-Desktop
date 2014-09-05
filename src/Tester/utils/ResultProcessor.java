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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ResultProcessor {

    public static final int UPLOAD_TEST = 1;
    public static final int DOWNLOAD_TEST = 2;
    public static final int UDP_TEST = 3;
    public static final int PING_TEST = 4;
    private IntegerProperty current_test = new SimpleIntegerProperty();
    private int occurrences = 0; // used to track how many times a string has occurred (determines when upload and when download)
    private boolean tcp_triggered = false;

    /**
     * 
     * @param line
     * @return 
     */
    public Double getResult(String line) {

        Double result = null;
        setTest(line);

        switch (current_test.get()) {
            case UPLOAD_TEST:
            case DOWNLOAD_TEST:
                result = processTCP(line);
            break;
            case UDP_TEST:
                result = processUDP(line);
            break;
            case PING_TEST:
                result = processPing(line);
            break;
        }

        return result;
    }

    /**
     * searches by certain regex criteria to determine what test we are on.
     * 
     * @param line 
     */
    private void setTest(String line) {
        if(line.matches("^(PING|ping|Ping)(.*)")) {
            tcp_triggered = false;
            current_test.set(PING_TEST);
        } else if(line.matches("^(TCP|tcp)(.*)")) {
            tcp_triggered = true;
        } else if(line.matches("(.*)\\d+\\sconnected\\swith(.*)") && tcp_triggered) {
            if(++occurrences % (Globals.NUM_TCP_TESTS_PER_SERVER * 2) == 0 ) {
                current_test.set(DOWNLOAD_TEST);
            } else {
                current_test.set(UPLOAD_TEST);
            }
        } else if(line.matches("^(UDP|udp)(.*)")) {
            tcp_triggered = false;
            current_test.set(UDP_TEST);
        }
    }

    /**
     * 
     * @param line
     * @return 
     */
    private Double processPing(String line) {
        Double result = null;
        Pattern regex;
        Matcher match;

        // Regex: /time=\(\d*\.?\d\)\s?ms/\1/
        // Looks for time= digits maybe a . digits maybe a space ms.
        regex = Pattern.compile("time=(\\d*\\.?\\d*)\\s?ms");
        match = regex.matcher(line);
        if(match.find()) {
            result = Double.valueOf( match.group(1) );
        }

        
//        if(line.matches("^(\\d\\d?\\sbytes)(.*)")) {
//            regex = Pattern.compile("(\\d+\\.+\\d*)(\\sms)$"); //looks at the end of the line for anything with [digit].[digit] ms
//            match = regex.matcher(line);
//            if(match.find()) {
//                result = Double.valueOf( match.group(1) );
//            }
//        }
//        System.out.println("Debug: processPing " + result);
        return result;
    }

    /**
     * 
     * @param line
     * @return 
     */
    private Double processTCP(String line) {
        Double result = null;
        Pattern regex;
        Matcher match;

        //looks for a pattern like this "[ DIGIT]" and ignores any lines that have 0.0-NUM_SECS_OF_TEST sec
        if(line.matches("^(\\[\\s+\\d+\\]\\s+\\d)(.*)") && !line.matches("(.*)(0\\.0-" + Globals.NUM_SECS_OF_TEST + "\\.\\d\\ssec)(.*)")) { // checks if a line is test or not

              regex = Pattern.compile("(\\d+)(\\sKbits/sec)$");
              match = regex.matcher(line);
              if(match.find()) {
                  Double value = Double.valueOf( match.group(1) );
                  if(value <= Globals.IPERF_BIG_NUMBER_ERROR && value != 0.0) {
                        result = Double.valueOf( match.group(1) );
                  }
              }
        }

        return result;
    }

    /**
     * 
     * @param line
     * @return 
     */
    private Double processUDP(String line) {
        Double result = null;
        Pattern regex;
        Matcher match;

        if(line.matches("(.*)(\\d+\\s\\(\\d+%\\)$)")) {
            regex = Pattern.compile("(\\d+\\.\\d+)(\\sms)");
            match = regex.matcher(line);
            if(match.find()) {
                result = Double.valueOf( match.group(1) );
            }
        }

        return result;
    }

    /**
     * 
     * @return 
     */
    public IntegerProperty currentTestProperty() {
        return current_test;
    }
}
