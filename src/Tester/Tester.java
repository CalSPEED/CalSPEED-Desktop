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
}