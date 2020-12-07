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

import static Tester.utils.ResultProcessor.DOWNLOAD_TEST;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import logging.Timber;

public class ResultProcessor {
    public static final int PROBE_TEST = 0;
    public static final int UPLOAD_TEST = 1;
    public static final int DOWNLOAD_TEST = 2;
    public static final int UDP_TEST = 3;
    public static final int PING_TEST = 4;
    private final IntegerProperty current_test = new SimpleIntegerProperty();
    private int occurrences = 0; // used to track how many times a string has occurred (determines when upload and when download)
    private boolean tcp_triggered = false;
    private static int connectingCounter = 0;
    private static int testNum = 0;
    private static int counter = 0;
    private static double tempResult = 0.0;
    private static final String CLASSNAME = ResultProcessor.class.getName();
    private Integer numberOfTestThreads;

    /**
     * Default constructor
     */
    public ResultProcessor() {
        this.numberOfTestThreads = 4;
    }

    /*
    * Constructor with TestConfig parameter
     */
    public ResultProcessor(Integer numberOfTestThreads) {
        this.numberOfTestThreads = numberOfTestThreads;
    }

    /*
     * @param line
     * @param server
     * @return
     */
    public LineResult getResult(String line, String server) {
        LineResult result;
        setTest(line);
        switch (current_test.get()) {
            case PROBE_TEST:
            case UPLOAD_TEST:
            case DOWNLOAD_TEST:
                result = processTCP(line);
                break;
            case UDP_TEST:
                result = processUDP(line, server);
                break;
            case PING_TEST:
                result = processPing(line, server);
                break;
            default:
                result = new LineResult();
        }
        return result;
    }

    /**
     * searches by certain regex criteria to determine what test we are on.
     *
     * @param line
     */
    private void setTest(String line) {
        if (line.matches("^(PING|ping|Ping)(.*)")) {
            tcp_triggered = false;
            current_test.set(PING_TEST);
        } else if (line.matches("^(TCP|tcp)(.*)")) {
            tcp_triggered = true;
        } else if (line.matches("(.*)\\d+\\sconnected\\swith(.*)") && tcp_triggered) {
            if (++occurrences % (this.numberOfTestThreads * 2) == 0) {
                current_test.set(DOWNLOAD_TEST);
            } else {
                current_test.set(UPLOAD_TEST);
            }
        } else if (line.matches("^(UDP|udp)(.*)")) {
            tcp_triggered = false;
            current_test.set(UDP_TEST);
        }
    }

    /**
     *
     * @param line
     * @param server
     * @return
     */
    private LineResult processPing(String line, String server) {
        Double result = null;
        Pattern regex;
        Matcher match;
        regex = Pattern.compile("time=(\\d*\\.?\\d*)\\s?ms");
        match = regex.matcher(line);
        if (match.find()) {
            result = Double.valueOf(match.group(1));
            MOSCalculation.addPing(result, server);
        }
        if (line.matches("\\s*Minimum\\s=\\s(\\d+)ms,\\sMaximum\\s=\\s(\\d+)ms,\\sAverage\\s=\\s(\\d+)ms")) {
            regex = Pattern.compile("\\s*Minimum\\s=\\s(\\d+)ms,\\sMaximum\\s=\\s(\\d+)ms,\\sAverage\\s=\\s(\\d+)ms");
            match = regex.matcher(line);
            if (match.find()) {
                Integer min = Integer.valueOf(match.group(1));
                Integer max = Integer.valueOf(match.group(2));
                Integer avg = Integer.valueOf(match.group(3));
                Timber.debug(CLASSNAME, "Min " + min + " max " + max + " avg: " + avg);
                // Setting to negative to indicate it is a final average of one server
                result = -avg.doubleValue();
                MOSCalculation.addFinalServerPing(avg);
            }
        }
        LineResult lineResult = new LineResult(result);
        return lineResult;
    }

    /**
     *
     * @param line
     * @return
     */
    private LineResult processTCP(String line) {
        Double result = null;
        Integer threadNum = null;
        Integer startInterval = null;
        Integer endInterval = null;
        Pattern regex;
        Matcher match;

        //looks for a pattern like this "[ DIGIT]" and ignores any lines that have 0.0-NUM_SECS_OF_TEST sec
        if (line.matches("^(\\[\\s+\\d+\\]\\s+\\d)(.*)") && !line.matches("(.*)(0\\.0-" + Globals.DEFAULT_TCP_TEST_TIME + "\\.\\d\\ssec)(.*)")) { // checks if a line is test or not
            regex = Pattern.compile("\\[\\s+(\\d+)\\]\\s+(\\d+)\\.\\d+-\\s*(\\d+).*KBytes\\s+((\\d+)((\\.\\d+)?))(\\sKbits/sec)$");
            match = regex.matcher(line);
            if (match.find()) {
                threadNum = Integer.valueOf(match.group(1));
                startInterval = Integer.valueOf(match.group(2));
                endInterval = Integer.valueOf(match.group(3));
                Double value = Double.valueOf(match.group(4));
                if (value <= Globals.IPERF_BIG_NUMBER_ERROR) {
                    result = Double.valueOf(match.group(4));
                }
                if (Integer.valueOf(match.group(2)).equals(0) && !(Integer.valueOf(match.group(3)).equals(1))) {
                    result = null;
                }
            }
        }

        /* 
        When 4 threads have made a connection, this signifies a test change
        (i.e WEST_UP to WEST_DOWN, WEST_DOWN to EAST_UP, EAST_UP to EAST_DOWN)
         */
        if (line.matches("^(\\[\\s+\\d+\\])(.*)(local)(.*)")) {
            regex = Pattern.compile("\\[\\s+(\\d+)\\](.*)");
            match = regex.matcher(line);
            if (match.find()) {
                for (int i = 0; i < this.numberOfTestThreads; i++) {
                    if (Globals.threadData.get(i).getThreadNum() == null) {
                        Globals.threadData.get(i).setThreadNum(Integer.valueOf(match.group(1)));
                        break;
                    } else if (Globals.threadData.get(i).getThreadNum().equals(Integer.valueOf(match.group(1)))) {
                        Globals.threadData.get(i).toggleDirectionUp();
                    }
                }
            }
            if (++connectingCounter == this.numberOfTestThreads) {
                connectingCounter = 0;
                tempResult = 0.0;
                counter = 0;
                testNum++;
                switch (testNum) {
                    case 1:
                        Globals.whichTest = Globals.WEST_UPLOAD;
                        break;
                    case 2:
                        Globals.whichTest = Globals.WEST_DOWNLOAD;
                        break;
                    case 3:
                        Globals.whichTest = Globals.WEST_UPLOAD;
                        break;
                    case 4:
                        Globals.whichTest = Globals.WEST_DOWNLOAD;
                        break;
                    case 5:
                        Globals.whichTest = Globals.EAST_UPLOAD;
                        break;
                    case 6:
                        Globals.whichTest = Globals.EAST_DOWNLOAD;
                        testNum = 0;
                }                
                Timber.debug(CLASSNAME, "Changing test to: " + String.valueOf(Globals.whichTest));
            }
        }

        // Adds sum of thread final values for 0.0-NUM_SECS_OF_TEST lines
        if (line.matches("(.*)(\\s+0\\.0-\\d+\\.\\d\\ssec)(.*)") && !line.matches("(.*)[SUM](.*)")) {
            counter++;
            threadNum = null;
            startInterval = null;
            endInterval = null;
            regex = Pattern.compile("\\[\\s+(\\d+)\\]\\s+(\\d+)\\.\\d+-\\s*(\\d+).*KBytes\\s+((\\d+)((\\.\\d+)?))(\\sKbits/sec)$");
            match = regex.matcher(line);
            if (match.find()) {
                threadNum = Integer.valueOf(match.group(1));
                startInterval = Integer.valueOf(match.group(2));
                endInterval = Integer.valueOf(match.group(3));
            }
            regex = Pattern.compile("(\\d+)(\\sKbits/sec)$");
            match = regex.matcher(line);
            if (match.find()) {
                Double value = Double.valueOf(match.group(1));
                if (value <= Globals.IPERF_BIG_NUMBER_ERROR && value != 0.0) {
                    tempResult += Double.valueOf(match.group(1));
                }
                if (counter == numberOfTestThreads) {
                    result = -1 * tempResult;
                    counter = 0;
                    tempResult = 0.0;
                }
            }
        }
        LineResult lineResult = new LineResult(result, threadNum, startInterval, endInterval);
        return lineResult;
    }

    public void resetThreads() {
        for (int i = 0; i < this.numberOfTestThreads; i++) {
            Globals.threadData.get(i).resetThread();
        }
    }

    /**
     *
     * @param line
     * @return
     */
    private LineResult processUDP(String line, String server) {
        Double result = null;
        Pattern regex;
        Matcher match;
        if (line.matches("(.*)(\\d+\\s\\(\\d+(\\.\\d)?%\\)$)")) {
            regex = Pattern.compile("(\\d+\\.\\d+)\\sms\\s+(\\d+)/\\s+(\\d+)\\s\\(");
            match = regex.matcher(line);
            if (match.find()) {
                result = Double.valueOf(match.group(1));
                MOSCalculation.addJitter(result, server);
                MOSCalculation.addUDPLoss(Double.valueOf(match.group(2)) / Double.valueOf(match.group(3)), server);
            }
        }
        LineResult lineResult = new LineResult(result);
        return lineResult;
    }

    /**
     *
     * @return
     */
    public IntegerProperty currentTestProperty() {
        return current_test;
    }
}
