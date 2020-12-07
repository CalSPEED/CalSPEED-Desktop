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

package Tester;

import Tester.utils.*;
import Tester.utils.configs.TestConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import logging.Timber;

public class Tester extends Task {

    private final String className;
    private final static String CLASSNAME = Tester.class.getName();
    protected int test_timeout;
    protected TestConfig configs;
    protected String test;
    protected String ip;
    protected String port;
    protected String server;
    public ResultProcessor processor;
    protected static int TEST_COUNT = 0;
    protected static int TESTS_COMPLETE = 0;
    protected final static int TEST_SUCCEEDED = 1;
    protected final static int TEST_CANCELLED = 2;
    protected final static int TEST_FAILED = 3;
    protected Integer currentTest = 0;
    private List<String> elements;
    private Map<String, String> threadDirections;
    private final static String UP = "Up";
    private final static String DOWN = "Down";
    private final static String WEST = "West";
    private final static String EAST = "East";
    private static Map<String, Double> WestUp = new HashMap<>();
    private static Map<String, Double> WestDown = new HashMap<>();
    private static Map<String, Double> EastUp = new HashMap<>();
    private static Map<String, Double> EastDown = new HashMap<>();
    private static String westDownRaw;
    private static String westUpRaw;
    private static String eastDownRaw;
    private static String eastUpRaw;
    public static String conferenceDetails;
    public static String videoDetails;
    private static ArrayList<Metric> allMetrics = new ArrayList<>();
    private static HashSet<Integer> threadSet = new HashSet<Integer>();
    public static Map<Integer, String> threadDirection = new HashMap<Integer, String>();
    private static HashMap<Integer, ArrayList<Metric>> metricsByThread
            = new HashMap<Integer, ArrayList<Metric>>();

    //allow messages to be passed to the GUI from the tests (in case a fatal error occurs)
    protected static StringProperty alertMessage = new SimpleStringProperty();

    protected IntegerProperty isDone = new SimpleIntegerProperty();
    /*
    * If an error found during call(), whenDone will be set so that when succeeded runs, 
    * isDone (above) can be set to whenDone
     */
    protected int whenDone;

    public Tester(TestConfig configs) {
        className = this.getClass().getName();
        this.processor = new ResultProcessor(configs.getThreadNumber());
        this.configs = configs;
        this.test_timeout = Globals.DEFAULT_TCP_TIMEOUT;
        this.test = configs.whichTestType();
        this.ip = configs.getIp();
        this.port = configs.getPort();
        this.whenDone = TEST_SUCCEEDED; //if no errors are thrown, then value will remain 1.
        ++TEST_COUNT;
        /* Allows us to also listen for the processor to change tests 
         * and find out which test is being run (especially helpful for upload & download)
        */
        processor.currentTestProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                currentTest = (Integer) newVal;
            }
        });
    }

    //Only because it throws an error if this is removed
    public Tester() {
        this.threadDirections = new HashMap<>();
        className = this.getClass().getName();
        this.test = "all";
    }

    public String whichTest() {
        String testType = "";
        switch (test) {
            case "tcp":
                testType = "TCP";
                break;
            case "udp":
                testType = "UDP";
                break;
            case "ping":
                testType = "PING";
                break;
            default:
                break;
        }
        if (ip.equals(Globals.WEST_SERVER)) {
            testType += " West Server";
            server = WEST;
        } else if (ip.equals(Globals.EAST_SERVER)) {
            testType += " East Server";
            server = EAST;
        }
        return testType;
    }

    /**
     * @return
     */
    @Override
    public Void call() {
        CommandLine command = new CommandLine() {
            @Override
            public void handleReader(int output_type, BufferedReader reader, boolean showMessages) {
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        if (isCancelled() || Thread.currentThread().isInterrupted()) {
                            whenDone = TEST_CANCELLED;
                            return;
                        }
                        if (showMessages) {
                            if (!processLine(line)) {
                                return;
                            }
                        }
                    }
                    Collections.sort(allMetrics, new MetricComparator());
                    for (Integer thread : threadSet) {
                        metricsByThread.put(thread, new ArrayList<Metric>());
                    }
                } catch (IOException e) {
                    Timber.error(className, e.getMessage());
                }
            }
            @Override
            public boolean processLine(String line) {
                if (isCancelled() || Thread.currentThread().isInterrupted()) {
                    whenDone = TEST_CANCELLED;
                    return false;
                }
                FilePrep.addDetail(line);
                if (whichTest().contains("TCP")) {
                    addLineToMetric(line, server);
                }
                LineResult lineResult = processor.getResult(line, server);
                if (lineResult.getResult() != null) {
                    setFinalValue(lineResult);
                }
                return true;
            }
            @Override
            public void handleError(String message) {
                whenDone = TEST_FAILED;
                Timber.error(className, message);
                setAlertMessage(message);
            }
        };
        command.runTest(configs.createIperfCommandLine(), this.test_timeout);
        return null;
    }

    /**
     * @param lineResult
     */
    protected void setFinalValue(LineResult lineResult) {
        Timber.debug(className, String.valueOf(lineResult.getResult()));
    }
    
    public IntegerProperty isDoneProperty() {
        return isDone;
    }

    /**
     * @param isDone
     */
    protected void setIsDoneProperty(int isDone) {
        this.isDone.set(isDone);
    }

    /**
     * @param message
     */
    protected static final void setAlertMessage(String message) {
        alertMessage.set(message);
    }

    /**
     * @return
     */
    public static StringProperty alertMessageProperty() {
        return alertMessage;
    }

    @Override
    protected void succeeded() {
        super.succeeded();
        setIsDoneProperty(whenDone);
        
        if (++TESTS_COMPLETE == TEST_COUNT) {
            resetCounts();
        }
    }

    @Override
    protected void cancelled() {
        succeeded();
    }

    @Override
    protected void failed() {
        succeeded();
    }

    /**
     * @param list: list of all values from Iperf output for each of the four
     * tests i.e. [ 9] 0.0- 1.0 sec 1136 KBytes 9306 Kbits/sec
     * @return integer of the average value to display
     *
     * We want TCP results to look like they are climbing at the start
     */
    protected Double getAverageResult(ArrayList<Double> list) {
        double sum = 0.0;

        if (!(list.size() > 0)) {
            return sum;
        }

        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        int baseNumber = configs.getThreadNumber();
        int testLength = configs.getTestTime();
        int sixthInterval = 6 * (testLength / Globals.DEFAULT_TCP_TEST_TIME);
        if (this.test.equals(Globals.TCP)) {
            int divideSize = list.size() / baseNumber;
            if (divideSize == 0) {
                divideSize++;
            }
            if (this.ip.equals(Globals.WEST_SERVER)) {
                Timber.debug(className, "west sum: " + String.valueOf(sum) + " size: " + String.valueOf(list.size()));
                if (list.size() < (baseNumber * sixthInterval)) {
                    return sum / sixthInterval;
                } else {
                    return sum / divideSize;
                }
            } else {
                if (divideSize < testLength) {
                    Timber.debug(className, "Smoothing calculation by 16: " + sum / (testLength + sixthInterval));
                    return sum / (testLength + sixthInterval);
                } else {
                    Timber.debug(className, "Smoothing calculation by divideSize " + divideSize + ": " + sum / divideSize);
                    return sum / divideSize;
                }
            }
        }
        return sum / list.size();
    }

    private void resetCounts() {
        TESTS_COMPLETE = TEST_COUNT = 0;
    }

    public static void clearMetrics() {
        allMetrics.clear();
    }

    public void addLineToMetric(String line, String server) {
        elements = Arrays.asList(line.split("\\s+"));
        int speed, time, threadID;
        if ((elements.size() >= 8) && elements.contains("[")) {
            threadID = elements.indexOf("[") + 1;
            Integer validThreadID = Integer.valueOf(elements.get(threadID).replace("]", ""));
            if (elements.contains("local") && elements.contains("port")) {
                changeDirection(validThreadID);
            }
            if (elements.contains("Kbits/sec")) {
                speed = elements.indexOf("Kbits/sec") - 1; //find index before speed measurement
                time = elements.indexOf("sec") - 1; //find index before "sec"
                String timeValue = elements.get(time);
                String timeRangeFirst, timeRangeSecond;
                if (timeValue.contains("-")) {
                    timeRangeFirst = timeValue.split("-")[0];
                    timeRangeSecond = timeValue.split("-")[1];
                } else {
                    timeRangeFirst = elements.get(elements.indexOf("sec") - 2).replace("-", "");
                    timeRangeSecond = timeValue;
                }
                if (speed >= 0 && time >= 0
                        && elements.indexOf("[SUM]") == -1
                        && isValidTime(timeRangeFirst, timeRangeSecond)) {
                    String validSpeed = elements.get(speed);
                    String validTime = formatTimeForMetric(elements.get(time));
                    Metric validMetric = new Metric(validThreadID,
                            Double.valueOf(validSpeed), validTime,
                            threadDirection.get(validThreadID), server);
                    threadSet.add(validThreadID);
                    allMetrics.add(validMetric);
                }
            }
        }
    }

    private void changeDirection(Integer threadNumber) {
        if (null == threadDirection.get(threadNumber)) {
            threadDirection.put(threadNumber, UP);
        } else switch (threadDirection.get(threadNumber)) {
            case UP:
                threadDirection.put(threadNumber, DOWN);
                break;
            case DOWN:
                threadDirection.put(threadNumber, UP);
                break;
            default:
                break;
        }
    }

    public static void calculateSpeed() {
        for (Metric metric : allMetrics) {
            Timber.verbose(Tester.class.getName(), metric.toString());
            Map<String, Double> mapToUpdate = null;
            if (metric.getServer().equals(WEST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = WestUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = WestDown;
                }
            } else if (metric.getServer().equals(EAST)) {
                if (metric.getDirection().equals(UP)) {
                    mapToUpdate = EastUp;
                } else if (metric.getDirection().equals(DOWN)) {
                    mapToUpdate = EastDown;
                }
            }
            if (mapToUpdate == null) {
                Timber.error(Tester.class.getName(),
                        String.format("Didn''t find a valid metric: {0}", metric.toString()));
                throw new NullPointerException();
            }
            String time = metric.getTimeRange();
            if (Double.parseDouble(time) < 11.0) {
                Double speed = metric.getSpeed();
                Double oldSpeed = mapToUpdate.get(time);
                if (oldSpeed == null) {
                    oldSpeed = 0.0;
                }
                mapToUpdate.put(time, oldSpeed + speed);

                ArrayList<Metric> valuesForThread = metricsByThread.get(metric.getThreadID());
                valuesForThread.add(metric);
            }
        }
    }

    private boolean isValidTime(String timeOne, String timeTwo) {
        Timber.verbose(className, "Comparing times: " + timeOne + " and " + timeTwo);
        if (timeOne == null && timeTwo == null) {
            Timber.error(className, "Got invalid times: " + timeOne + ", " + timeTwo);
            return false;
        } else {
            if ((timeTwo.length() == 3) && timeTwo.contains("0") && timeOne.contains("0")) {
                return true;
            } else {
                Double firstTime = Double.valueOf(timeOne);
                Double secondTime = Double.valueOf(timeTwo);
                if (secondTime - firstTime == 1.0) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private String formatTimeForMetric(String rawTime) {
        if (rawTime.contains("-")) {
            return rawTime.split("-")[1];
        } else {
            return rawTime;
        }
    }

    public static String calcVideo() {
        int downHD, downSD, downLS, upHD, upSD, upLS;
        downHD = downSD = downLS = upHD = upSD = upLS = 0;

        Map mp = WestDown;
        Map<String, Double> westDownCopy = new HashMap<String, Double>();
        westDownCopy.putAll(WestDown);
        westDownRaw = mapToString(westDownCopy);
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((double) pair.getValue() > 2500) {
                downHD++;
            } else if ((double) pair.getValue() > 700) {
                downSD++;
            } else {
                downLS++;
            }
            it.remove();
        }

        mp = WestUp;
        Map<String, Double> westUpCopy = new HashMap<String, Double>();
        westUpCopy.putAll(WestUp);
        westUpRaw = mapToString(westUpCopy);
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((double) pair.getValue() > 2500) {
                upHD++;
            } else if ((double) pair.getValue() > 700) {
                upSD++;
            } else {
                upLS++;
            }
            it.remove();
        }

        videoDetails = "West [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS
                + "\nWest [Up] HD: " + upHD
                + " SD: " + upSD
                + " LS : " + upLS;
        Timber.debug(Tester.class.getName(), videoDetails);
        if (downHD >= 9) {
            return "High Definition";
        } else if (!hasTenValues(EAST, DOWN) && !hasTenValues(EAST, UP)) {
            return "N/A";
        } else if (downHD + downSD >= 9) {
            return "Standard Definition";
        } else if (upHD + upSD + upLS >= 10 && downHD + downSD + downLS >= 10) {
            return "Low Definition";
        } else {
            return "N/A";
        }
    }

    public static String calcConference() {
        int upHD, upSD, upLS, downHD, downSD, downLS;
        upHD = upSD = upLS = downHD = downSD = downLS = 0;
        Map mp = EastUp;
        Map<String, Double> eastUpCopy = new HashMap<String, Double>();
        eastUpCopy.putAll(EastUp);
        eastUpRaw = mapToString(eastUpCopy);
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((double) pair.getValue() > 2500) {
                upHD++;
            } else if ((double) pair.getValue() > 700) {
                upSD++;
            } else {
                upLS++;
            }
            it.remove();
        }

        mp = EastDown;
        Map<String, Double> eastDownCopy = new HashMap<String, Double>();
        eastDownCopy.putAll(EastDown);
        eastDownRaw = mapToString(eastDownCopy);
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            if ((double) pair.getValue() > 2500) {
                downHD++;
            } else if ((double) pair.getValue() > 700) {
                downSD++;
            } else {
                downLS++;
            }
            it.remove();
        }

        conferenceDetails = "East [Up] HD: " + upHD
                + ", SD: " + upSD
                + ", LS: " + upLS + "\n";

        conferenceDetails += "East [Down] HD: " + downHD
                + ", SD: " + downSD
                + ", LS: " + downLS;
        Timber.debug(CLASSNAME, conferenceDetails);
        if (!hasTenValues(EAST, DOWN) && !hasTenValues(EAST, UP)
                && (MOSCalculation.getMOS(EAST) <= 0.0)) {
            return "N/A";
        } else if (MOSCalculation.getMOS(EAST) < 4.0) {
            return "LD";
        } else if (downHD >= 9 && upHD >= 9) {
            return "HD";
        } else if (upHD + upSD >= 9 && downHD + downSD >= 9) {
            return "SD";
        } else if (upHD + upSD + upLS >= 10 && downHD + downSD + downLS >= 10) {
            return "LD";
        } else {
            return "N/A";
        }
    }

    public static String calcVoip() {
        Timber.debug(CLASSNAME, "East MOS: " + MOSCalculation.getMOS(EAST));
        Double mosValueEast = MOSCalculation.getMOS(EAST);
        if (mosValueEast <= 0.0) {
            return "N/A";
        } else if (mosValueEast < 3.0) {
            return "Poor";
        } else if (mosValueEast < 4.0) {
            return "Fair";
        } else {
            return "Good";
        }
    }

    private static boolean hasTenValues(String server, String direction) {
        for (Map.Entry<Integer, ArrayList<Metric>> entry : metricsByThread.entrySet()) {
            ArrayList<Metric> matchingMetrics = new ArrayList<Metric>();
            for (Metric metric : entry.getValue()) {
                if ((metric.getServer()).equals(server)
                        && metric.getDirection().equals(direction)) {
                    matchingMetrics.add(metric);
                }
            }
            if (matchingMetrics.size() < 10) {
                return false;
            }
        }
        return true;
    }

    public static void resetScores() {
        Map mp = WestUp;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put(pair.getKey(), 0);
            it.remove();
        }
        mp = WestDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put(pair.getKey(), 0);
            it.remove();
        }
        mp = EastUp;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put(pair.getKey(), 0);
            it.remove();
        }
        mp = EastDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            mp.put(pair.getKey(), 0);
            it.remove();
        }

    }

    boolean isValidTime(String time) {
        if (time.length() == 3) {
            return true;
        } else {
            if (Double.valueOf(time.substring(time.length() - 4)) <= 10.0) {
                return Double.valueOf(time.substring(time.length() - 4))
                        - Double.valueOf(time.substring(0, 3)) == 1;
            } else {
                return false;
            }
        }
    }

    private static String mapToString(Map mp) {
        Iterator it = mp.entrySet().iterator();
        String stringMap = "[ ";
        while (it.hasNext()) {
            Map.Entry keyValue = (Map.Entry) it.next();
            stringMap += keyValue.getKey() + " : ";
            if ((Double) keyValue.getValue() > 2500) {
                stringMap += keyValue.getValue() + " : HD; ";
            } else if ((Double) keyValue.getValue() > 700) {
                stringMap += keyValue.getValue() + " : SD; ";
            } else {
                stringMap += keyValue.getValue() + " : LD; ";
            }
            it.remove();
        }
        stringMap += "]";
        return stringMap;
    }

    public static void printMap(Map<String, Double> mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Timber.verbose(CLASSNAME, pair.getKey() + " = " + pair.getValue());
            it.remove();
        }
    }
}
