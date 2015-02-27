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
package GUI;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import Tester.Tester;
import Tester.tests.*;
import Tester.utils.*;
import com.google.gson.Gson;
import Database.Database;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import Viewer.ArcGis;

public class MainController extends StackPane {

    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();
    JSObject jsTester; //Tester.js
    JSObject jsTemplater; //Templater.js
    Result testResult;
    private boolean testRunning = false;
    private Thread testThread;
    private int tests_complete;
    private ArcGis advertisedGis;
    private ArcGis predictedGis;

    public MainController() {
        webView.setContextMenuEnabled(false);
        final Database database = new Database(Globals.DB_NAME, Globals.TABLE_NAME);
//      database.dropTable();
        database.createInit();
        database.updateTable();
        final String data = database.selectData();

        final URL url = getClass().getResource("index.html");
        webEngine.load(url.toExternalForm());

        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>(){
                    @Override
                    public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                        if(newState == Worker.State.SUCCEEDED){
                            jsTester = (JSObject) webEngine.executeScript("Tester");
                            jsTester.setMember("app", this);
                            
                            // Set up the viewer message to display or not
                            ((JSObject) webEngine.executeScript("ViewerMsg")).call("setMessageDisplay", true);

                            jsTemplater = (JSObject) webEngine.executeScript("Templater");
                            String []args = { "#history-results", "result", data };
                            jsTemplater.call("loadTemplate", args);
                        }
                    }
                });

        jsTester = jsTemplater = (JSObject) webEngine.executeScript("window");
        jsTester.setMember("app", this);

        getChildren().add(webView);
    }

    /**
     * This will initiate the test and call the necessary tasks that will sequentially then be monitored for setting values on the gui.
     */
    public void startTest() {
        //simple way to prevent a test to be run more than once at a time
        if(testRunning) {
            return;
        }
        MOSCalculation.clearData();
        
        Calendar cal = Calendar.getInstance();
        testResult = new Result();

        testRunning = true;

        Integer[] arg = { 1 }; //once you click it it should go on
        jsTester.call("setPhase", arg);

        testResult.time = new SimpleDateFormat("HH:mm").format(cal.getTime());
        testResult.date = new SimpleDateFormat("dd MMM, yyyy").format(cal.getTime());
        testResult.timestamp = String.valueOf(cal.getTimeInMillis());

        LocationService.setLocation();
        testResult.location = LocationService.getLocation(); //need to write a method that will actually do some reverse geocoding mumbo jumbo
        testResult.lat = LocationService.getLat();
        testResult.lng = LocationService.getLng();

        Tester[] tests = { new TCPTester(Globals.WEST_SERVER),
                           new PingTester(Globals.WEST_SERVER),
                           new UDPTester(Globals.WEST_SERVER),
                           new TCPTester(Globals.EAST_SERVER),
                           new PingTester(Globals.EAST_SERVER),
                           new UDPTester(Globals.EAST_SERVER),};

        //Register generic listeners
        TCPTester.uploadProperty().addListener((o, oldVal, newVal) -> {
            if((Double)newVal > 0.0) {
                testResult.upload = Double.valueOf(String.format("%1$,.2f", ((Double) newVal / 1024)));
            }
            tests_complete = 0;
            final Integer[] arg1 = { tests_complete };
            final Double[] result = { testResult.upload };

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1); //make sure it's the correct test
                    jsTester.call("setTestResult", result);
                }
            });
        });

        TCPTester.downloadProperty().addListener((o, oldVal, newVal) -> {
            if((Double)newVal > 0.0) {
                testResult.download = Double.valueOf(String.format("%1$,.2f", ((Double) newVal / 1024)));
            }

            tests_complete = 1;
            final Integer[] arg1 = { tests_complete };
            final Double[] result = { testResult.download };

            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1);
                    //System.out.println("setting download value");
                    jsTester.call("setTestResult", result);
                    //System.out.println("download value set");
                }
            });
        });

        PingTester.pingProperty().addListener(new ChangeListener(){
            @Override public void changed(ObservableValue o,Object oldVal, Object newVal){
                if((Double) newVal > 0.0) {
                    testResult.delay = Double.valueOf(String.format("%1$,.2f", ((Double) newVal)));
                }
                tests_complete = 2;
                final Integer[] arg1 = { tests_complete };
                final Double[] result = { testResult.delay };

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jsTester.call("setTestsComplete", arg1);
                        jsTester.call("setTestResult", result);
                    }
                });
            }
        });

        UDPTester.udpProperty().addListener(new ChangeListener(){
            @Override public void changed(ObservableValue o,Object oldVal, Object newVal){
                if((Double) newVal > 0.0) {
                    testResult.jitter = Double.valueOf(String.format("%1$,.2f", ((Double) newVal)));
                }
                tests_complete = 3;
                final Integer[] arg1 = { tests_complete };
                final Double[] result = { testResult.jitter };

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jsTester.call("setTestsComplete", arg1);
                        jsTester.call("setTestResult", result);
                    }
                });
            }
        });
//Keep here to maybe one day enable error messages for the user...
//        Tester.alertMessageProperty().addListener(new ChangeListener(){
//            @Override public void changed(ObservableValue o,Object oldVal, Object newVal){
//                Platform.runLater(new Runnable() {
//                    @Override
//                    public void run() {
//                        testRunning = false;
//                        Integer[] arg = { 0 };
//                        jsTester.call("setPhase", arg);
//                        //second argument is the html class name for the font-awesome (fa) icon
//                        String[] args = { "<h1 class='text-center'>" + newVal + "</h1>", "fa-exclamation-triangle", "small" };
//                        jsTester.call("showMessage", args);
//                    }
//                });
//            }
//        });

        FilePrep.setTimestamp(testResult.timestamp);
        
        runAllTests(tests, 0);
    }

    /**
     *
     * @param tests The array of tests that will be run by this method
     * @param current_test The current index for the tests array
     */
    private void runAllTests(final Tester[] tests, final int current_test) {
        //base case (when the final test has been reached)
        if(tests.length == (current_test + 1)) {
            final Gson jsonParser = new Gson();
            tests[current_test].isDoneProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                        testResult.mos = MOSCalculation.getMOS();
                        final String result = jsonParser.toJson(testResult);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Database database = new Database(Globals.DB_NAME, Globals.TABLE_NAME);
                                Integer[] arg = { 4 };
                                jsTester.call("setTestsComplete", arg);
                                String[] args = {"#final-results", "finish", result};
                                jsTemplater.call("loadTemplate", args);
                                FilePrep.saveToFile();
                                database.insertData(testResult);
                                args[0] = "#history-results";
                                args[1] = "result";
                                args[2] = database.selectData();
                                jsTemplater.call("loadTemplate", args);
                                System.out.println("MOS: " + MOSCalculation.getMOS());
                                testRunning = false;
                            }
                        });
                }
            });
        } else {
            tests[current_test].isDoneProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                    //keep to possibly enable ending all the tests when the first fails
                    //if((Integer)newVal != 3 || current_test != 0) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                setTestsCompleteFromIndex(current_test);
                                runAllTests(tests, (current_test + 1));
                            }
                        });
                    //}
                }
            });
        }

        FilePrep.addDetail(tests[current_test].whichTest());
        //System.out.println("started another thread: " + current_test);
        testThread = new Thread(tests[current_test]);
        testThread.setDaemon(true);
        testThread.start();
    }

    /**
     * allows the main Main to kill the test thread when the app is closed.
     */
    public void stopTest() {
        if(testThread != null && testThread.isAlive()) {
            //need a way to kill the thread.
            testThread.interrupt();
        }
    }

    /**
     * helper function to get the correct tests complete number (hardcoded for now. maybe one day we'll get fancy)
     * @param index the current index of the tests handled from the runAllTests method.
     */
    public void setTestsCompleteFromIndex(int index) {
        int testsComplete;

        if(index == 0 || index == 3) {
            //System.out.println("END TCP TEST");
            testsComplete = 2;
        } else if(index == 1 || index == 4) {
            //System.out.println("END PING TEST");
            testsComplete = 3;
        } else {
            //System.out.println("END UDP TEST");
            testsComplete = 4;
        }
        Integer[] arg = { testsComplete };
        jsTester.call("setTestsComplete", arg);
    }

//keep here to open the about page in the browser
//    public void openAbout() {
////        String[] args = { "Failed to open browser." };
////        jsTester.call("showMessage", args);
//        try {
//            //Set your page url in this string. For eg, I m using URL for Google Search engine
//            String url = "http://cpuc.ca.gov/PUC/Telco/bb_drivetest.htm";
//            Desktop.getDesktop().browse(URI.create(url));
//        }
//        catch (java.io.IOException e) {
//            String[] args = { "Failed to open browser." };
//            jsTester.call("showMessage", args);
//        }
//    }

    /**
     * Helper function for debugging javascript
     * @param result the result that comes from javascript that will be displayed in the console.
     */
    public void printResult(String result) {
        System.out.println(result);
    }

    /**
     * @param lat
     * @param lng
     * called from the GUI when a marker is clicked
     */
    public void getViewerResults(String lat, String lng) {

        if (this.predictedGis != null && this.advertisedGis != null) {
            String currentLat = predictedGis.getLat();
            String currentLng = predictedGis.getLng();

            predictedGis.cancel();
            advertisedGis.cancel();

            if (currentLat.equals(lat) && currentLng.equals(lng)) {
                return;
            }
        }
        //this way we run the predicted and advertised data requests at the "same" time
        predictedGis = new ArcGis(lng, lat, "predicted");
        advertisedGis = new ArcGis(lng, lat, "advertised");

        //kick off the threads
        Thread predictedThread = new Thread(predictedGis);
        predictedThread.setDaemon(true);
        predictedThread.start();
        Thread advertisedThread = new Thread(advertisedGis);
        advertisedThread.setDaemon(true);
        advertisedThread.start();

        predictedGis.finalValueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        String value = (String) newVal;
                        if( value.equals("error") ) {
                            value = ArcGis.getJSONError();
                        }
                        String[] args = {"#estimated-mobile", "viewer-results", value};
                        jsTemplater.call("loadTemplate", args);
                    }
                });
            }
        });

        advertisedGis.finalValueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //sort by the different tabs we have for advertised data
                        String value = (String) newVal;
                        if( !value.equals("error") ) {
                            String advertisedFixed = ArcGis.getByType((String) newVal, "Fixed");
                            if(advertisedFixed.equals("error")) {
                                advertisedFixed = ArcGis.getJSONError();
                            }
                            String[] fixedArgs = {"#advertised-fixed", "viewer-results", advertisedFixed};
                            jsTemplater.call("loadTemplate", fixedArgs);
                            String advertisedMobile = ArcGis.getByType((String) newVal, "Mobile");
                            if(advertisedMobile.equals("error")) {
                                advertisedMobile = ArcGis.getJSONError();
                            }
                            String[] mobileArgs = {"#advertised-mobile", "viewer-results", advertisedMobile};
                            jsTemplater.call("loadTemplate", mobileArgs);
                            String advertisedSatellite = ArcGis.getByType((String) newVal, "Satellite");
                            if(advertisedSatellite.equals("error")) {
                                advertisedSatellite = ArcGis.getJSONError();
                            }
                            String[] satelliteArgs = {"#advertised-satellite", "viewer-results", advertisedSatellite};
                            jsTemplater.call("loadTemplate", satelliteArgs);
                        } else {

                        }
                    }
                });
            }
        });
    }
}