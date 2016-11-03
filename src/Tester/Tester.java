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
package Tester;

import Tester.utils.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;

public class Tester extends Task {

    protected int test_timeout;
    protected String test;
    protected String ip;
    protected String port;
    public ResultProcessor processor;
    protected static int TEST_COUNT = 0;
    protected static int TESTS_COMPLETE = 0;
    protected final static int TEST_SUCCEEDED = 1;
    protected final static int TEST_CANCELLED = 2;
    protected final static int TEST_FAILED = 3;
    protected Integer currentTest = -1;
    private List<String> elements;
    private Map<String, String> threadDirections = new HashMap<>();
    private String UP = "Up";
    private String DOWN = "Down";
    private static Map<String, Double> WestUp = new HashMap<>();
    private static Map<String, Double> WestDown = new HashMap<>();
    private static Map<String, Double> EastUp = new HashMap<>();
    private static Map<String, Double> EastDown = new HashMap<>();
    public static String conferenceDetails;
    public static String videoDetails;
    
    //allow messages to be passed to the GUI from the tests (in case a fatal error occurs)
    protected static StringProperty alertMessage = new SimpleStringProperty();

    protected IntegerProperty isDone = new SimpleIntegerProperty();
    protected int whenDone; //if an error found during call(), whenDone will be set so that when succeeded runs, isDone (above) can be set to whenDone

    public Tester(String test, String ip, String port) {
        this.processor = new ResultProcessor();
        this.test_timeout = 30000; //30 seconds (can be overwritten from the classes that extend it)
        this.test = test;
        this.ip = ip;
        this.port = port;
        this.whenDone = TEST_SUCCEEDED; //if no errors are thrown, then value will remain 1.
        ++TEST_COUNT;
        //Allows us to also listen for the processor to change tests and find out which test is being run (especially helpful for upload & download)
        processor.currentTestProperty().addListener(new ChangeListener(){
            @Override public void changed(ObservableValue o,Object oldVal, Object newVal) {
                currentTest = (Integer) newVal;
            }
        });
        
        
    }

    //Only because it throws an error if this is removed
    public Tester() {
        this.test = "all";
    }
    
    public String whichTest() {
        String testType = "";

        if (test.equals("tcp")) {
            testType = "TCP";
        } else if (test.equals("udp")) {
            testType = "UDP";
        } else if (test.equals("ping")) {
            testType = "PING";
        }
        
        if (ip.equals(Globals.WEST_SERVER)) {
            testType += " West Server";
        } else if (ip.equals(Globals.EAST_SERVER)) {
            testType += " East Server";
        }
        
        return testType;
    }

    /**
     * @return
     */
    @Override
    public Void call() {
        IPerf iPerf = new IPerf() {
            @Override
            public void handleReader(int output_type, BufferedReader reader, boolean showMessages) {
                   String line;
                   try {
                        while ((line = reader.readLine()) != null) {
                            if(isCancelled() || Thread.currentThread().isInterrupted()) {
                                whenDone = TEST_CANCELLED;
                                return;
                            }

                            if (showMessages) {
                                if (!processLine(line)) {
                                    return;
                                }
                            }
                        }
                    } catch(IOException e) {
                       //handleError(e.getMessage());
                       return;
                    }
            }

            // overwritten from IPerf so we can make use of the labels we have in the GUI
            @Override
            public boolean processLine(String line) {
                if(isCancelled() || Thread.currentThread().isInterrupted()) {
                    whenDone = TEST_CANCELLED;
                    return false;
                }

                // This is where all the lines show up on the screen
                FilePrep.addDetail(line);
                System.out.println(line);
            
                if(whichTest().contains("TCP"))
                    addLineToMetric(line);
                
                Double result = processor.getResult(line);
                if (result != null) {
                   setFinalValue(result);
                }

                return true;
            }

            @Override
            public void handleError(String message) {
                whenDone = TEST_FAILED;
                System.out.println(message);
                setAlertMessage(message);
            }
        };

        //make sure we're in the first test
//        if(this.test.equals("tcp") && this.ip.equals(Globals.WEST_SERVER)) {
//            if(iPerf.testIP(this.ip, 4)) {
//                iPerf.runTest(this.test, this.ip, this.port, this.test_timeout);
//            } else {
//                iPerf.handleError("connectivity issues");
//            }
//        } else {
            //otherwise run it regardless of the ping test
            iPerf.runTest(this.test, this.ip, this.port, this.test_timeout);
//        }

        return null;
    }

    /**
     * @param result
     */
    protected void setFinalValue(double result) { System.out.println(String.valueOf(result)); }
    public IntegerProperty isDoneProperty() { return isDone; }

    /**
     * @param isDone
     */
    protected void setIsDoneProperty(int isDone) { this.isDone.set(isDone); }

    /**
     * @param message
     */
    protected static final void setAlertMessage(String message) { alertMessage.set(message); }

    /**
     * @return
     */
    public static StringProperty alertMessageProperty() { return alertMessage; }

    @Override
    protected void succeeded() {
       super.succeeded();
       setIsDoneProperty(whenDone);

       if(++TESTS_COMPLETE == TEST_COUNT) { resetCounts(); }
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
     * @param list
     * @return
     */
    protected Double getAverageResult(ArrayList<Double> list) {
        double sum = 0.0;

        if(!(list.size() > 0)) {
            return sum;
        }

        for(int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }

        //we want TCP results to look like they are climbing at the start
        if(this.test.equals("tcp")) {
            if(this.ip.equals(Globals.WEST_SERVER)) {
                if (list.size() < 6) {
                    return sum / 6;
                }
            } else {
                if (list.size() < 16) {
                    return sum / 16;
                }
            }
        }

        return sum / list.size();
    }

    private void resetCounts() { TESTS_COMPLETE = TEST_COUNT = 0; }
    
    private void addLineToMetric(String line){
        
        elements = Arrays.asList(line.split("\\s+"));
        int speed, time, threadID;
       
        if (elements.contains("local") && elements.contains("port")) //set direction (up/down)
            setDirection(elements.get(1));
        
        if (elements.size() >= 8) //the lines we care about are greater than 8 fields
        {
            speed = elements.indexOf("Kbits/sec") - 1; //find index before speed measurement
            time = elements.indexOf("sec") - 1; //find index before "sec"
            threadID = elements.indexOf("[") + 1; //find threadID e.g. 3, 4, 5, 6
            if (speed >= 0 && time >= 0 && elements.indexOf("[SUM]") == -1
                    && isValidTime(elements.get(time))) //makes sure that the index is found
            {
                addSpeed(elements.get(time), elements.get(speed), elements.get(threadID));
            }
            
        }
        
    }
    
    public void addSpeed(String time, String speed, String threadID){
        if (threadDirections.get(threadID).equals(UP))
        {
            if (whichTest().contains("West"))
                addSpeedToStream(WestUp, time, Double.parseDouble(speed));
            else 
                addSpeedToStream(EastUp, time, Double.parseDouble(speed));
        }
        
        if (threadDirections.get(threadID).equals(DOWN))
        {
            if (whichTest().contains("West"))
                addSpeedToStream(WestDown, time, Double.parseDouble(speed));
            else 
                addSpeedToStream(EastDown, time, Double.parseDouble(speed));
        }
        
    }
    
    public void addSpeedToStream(Map <String, Double> map, String time, Double speed){
        Double tempSpeed = map.get(time);
        
        if (tempSpeed == null)
            map.put(time, speed);
        else
            map.put(time, speed + tempSpeed);
    }
    
    public void setDirection(String thread){
        if (threadDirections.get(thread) == null){
            threadDirections.put(thread, UP);
        }
        else if (threadDirections.get(thread).equals(UP)){
            threadDirections.put(thread, DOWN);
        }
        
        else if (threadDirections.get(thread).equals(DOWN))
            threadDirections.put(thread, UP);
    }
    
    public static String calcVideo(){
        int downHD, downSD, downLS, upHD, upSD, upLS;
        downHD = downSD = downLS = upHD = upSD = upLS = 0;
        
        Map mp = WestDown;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((double)pair.getValue() > 2500)
                downHD++;
            else if ((double)pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        mp = WestUp;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((double)pair.getValue() > 2500)
                upHD++;
            else if ((double)pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        videoDetails = "West [Down] HD: " + downHD
                       + ", SD: " + downSD
                       + ", LS: " + downLS
                       + " -- West [Up] HD: " + upHD
                       + " SD: " + upSD
                       + " LS : " + upLS;
        
        if (downHD >= 9)
            return "High Definition";
        
        else if (downHD + downSD >= 9)
            return "Standard Definition";
        
        return "Low Service";
    }
    
    public static String calcConference(){
        
        int upHD, upSD, upLS, downHD, downSD, downLS;
        upHD = upSD = upLS = downHD = downSD = downLS = 0;
        
        Map mp = EastUp;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((double)pair.getValue() > 2500)
                upHD++;
            else if ((double)pair.getValue() > 700)
                upSD++;
            else
                upLS++;
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        mp = EastDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            if ((double)pair.getValue() > 2500)
                downHD++;
            else if ((double)pair.getValue() > 700)
                downSD++;
            else
                downLS++;
            it.remove(); // avoids a ConcurrentModificationException
        }
        
        conferenceDetails = "East [Up] HD: " + upHD
                       + ", SD: " + upSD
                       + ", LS: " + upLS;
        
        conferenceDetails += " -- East [Down] HD: " + downHD
                       + ", SD: " + downSD
                       + ", LS: " + downLS;
        
        if (MOSCalculation.getMOS() < 4.0)
            return "Low Service";
        
        else if (downHD >= 9 && upHD >= 9)
            return "High Definition";
        
        else if (upHD + upSD >= 9 && downHD + downSD >= 9)
            return "Standard Definition";
        
        return "Low Service";
    }
    
    public static void resetScores(){
        Map mp = WestUp;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mp.put(pair.getKey(), 0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = WestDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mp.put(pair.getKey(), 0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = EastUp;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mp.put(pair.getKey(), 0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = EastDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            mp.put(pair.getKey(), 0);
            it.remove(); // avoids a ConcurrentModificationException
        }
        
    }
    
    boolean isValidTime(String time){
        if (time.length() == 3)
            return true;
        
        else {
            if (Double.valueOf(time.substring(time.length() - 4)) <= 10.0)
            {
                if (Double.valueOf(time.substring(time.length() - 4))  - 
                    Double.valueOf(time.substring(0, 3)) != 1)
                    return false;
                else
                    return true;
            }
            
            else
                return false;
           
        }
    }
    
    public static void printMap(Map <String, Double> mp) {
       
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }
        
    public static void printMaps(){
        Map mp = WestUp;
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("west up " + pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("west down now");
        mp = WestDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("west down " + pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        System.out.println("east up now");
        mp = EastUp;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("east up " + pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
        mp = EastDown;
        it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            System.out.println("east down " + pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    
    }
    
}