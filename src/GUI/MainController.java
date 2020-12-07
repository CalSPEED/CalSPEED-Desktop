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
package GUI;

import Tester.utils.configs.TCPTestConfig;
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
import Tester.utils.configs.PingTestConfig;
import Tester.utils.configs.UDPTestConfig;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import Viewer.ArcGis;
import java.io.IOException;
import java.net.URL;
import java.awt.Desktop;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import logging.Timber;

public class MainController extends StackPane {

    private final String version = this.getClass().getPackage().getImplementationVersion();
    private final String className;
    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();
    JSObject jsTester; //Tester.js
    JSObject jsTemplater; //Templater.js
    JSObject jsLocation; //Location.js
    JSObject jsNavigator; //Navigator.js
    JSObject jsViewerMsg;
    JSObject jsSurvey;
    Result testResult;
    private boolean testRunning = false;
    private Thread testThread;
    private int tests_complete;
    private ArcGis arcGisData;
    private ArcGis predictedGis;
    Database database;
    CommandLine command = new CommandLine();
    long timestamp;
    private boolean showSurvey;
    ConfigManager config;
    Instant start;
    int configTier;
    Double probeValue;
    String probeResults;
    private boolean probeTestFinished;

    public MainController() {
        webView.setContextMenuEnabled(false);
        className = this.getClass().getName();
        config = new ConfigManager();
        Timber.debug(className, "Starting setting up database");
        database = new Database(Globals.DB_NAME, Globals.TABLE_NAME);
        database.createInit();
        database.createValidationTable();
        database.updateTable();
        database.updateTableVideo();
        database.createLocationTable();
        database.updateLocationTable();
        database.createIgnoreTable();
        Timber.debug(className, "Finish setting up database");

        final String data = database.selectData();
        final URL url = getClass().getResource("index.html");
        this.probeValue = 0.0;
        this.probeTestFinished = false;
        this.probeResults = "";
        configTier = 0;
        webEngine.load(url.toExternalForm());
        webEngine.getLoadWorker().stateProperty().addListener(
                new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> ov, Worker.State oldState, Worker.State newState) {
                if (newState == Worker.State.SUCCEEDED) {
                    jsTester = (JSObject) webEngine.executeScript("Tester");
                    jsTester.setMember("app", this);

                    jsLocation = (JSObject) webEngine.executeScript("Location");
                    jsLocation.setMember("app", this);

                    jsNavigator = (JSObject) webEngine.executeScript("Navigator");
                    jsNavigator.setMember("app", this);

                    // Set up the viewer message to display or not
                    // Causes history to break when wifi is off
                    jsViewerMsg = (JSObject) webEngine.executeScript("ViewerMsg");

                    jsSurvey = (JSObject) webEngine.executeScript("Feedback");
                    jsSurvey.setMember("app", this);
                    updateSetSurvey();
                    jsTemplater = (JSObject) webEngine.executeScript("Templater");
                    String[] args = {"#history-results", "result", data};
                    jsTemplater.call("loadTemplate", args);
                    database.debugLocation();
                    log("Updating settings and last test result");
                    LocationService.updateCarrier();
                    updateSettings();
                    updateLastTestResult();
                    if (!Database.LocationTableEmpty()) {
                        log("Inputing address");
                        switch (Database.getTypeID()) {
                            case "0":
                                jsLocation.call("loadLocationInput", Database.getAddress());
                                break;
                            case "1":
                                jsLocation.call("loadLocationInput", Database.getLatitude() + ", "
                                        + Database.getLongitude() + " (" + database.getLocation() + ")");
                                break;
                            default:
                                jsLocation.call("loadLocationInput", Database.getAddress());
                                break;
                        }
                    } else {
                        LocationService.setLocation();
                        insertLocationData(LocationService.getLocation(),
                                LocationService.getLat(), LocationService.getLng(),
                                LocationService.getCity(), LocationService.getZip(),
                                "0", LocationService.getCarrier());
                        log("Inputing previous location: " + LocationService.getLocation());
                        jsLocation.call("loadLocationInput", LocationService.getLocation());
                    }
                }
            }
        });
        jsTester = jsTemplater = (JSObject) webEngine.executeScript("window");
        jsTester.setMember("app", this);
        getChildren().add(webView);
    }

    public void openInBrowser() {
        if (Desktop.isDesktopSupported()) {
            try {
                try {
                    Desktop.getDesktop().browse(new URI("https://www.google.com/maps"));
                } catch (URISyntaxException ex) {
                    Timber.error(className, ex.getMessage());
                }
            } catch (IOException ex) {
                Timber.error(className, ex.getMessage());
            }
        }
    }

    /**
     * This will initiate the test and call the necessary tasks that will
     * sequentially then be monitored for setting values on the gui.
     */
    public void startTest() {
        log("Starting test");
        start = Instant.now();
        Timber.info(className, "Start stopwatch: " + start.toString());
        //simple way to prevent a test to be run more than once at a time
        if (testRunning) {
            jsTester.call("hideConnection"); //fixes bug when start button is clicked multiple times
        } else {
            testRunning = true;
            MOSCalculation.clearData();
            Calendar cal = Calendar.getInstance();
            testResult = new Result();
            testResult.time = new SimpleDateFormat("HH:mm").format(cal.getTime());
            testResult.date = new SimpleDateFormat("dd MMM, yyyy").format(cal.getTime());
            this.timestamp = cal.getTimeInMillis();
            testResult.timestamp = String.valueOf(timestamp);

            LocationService.setLocation();
            if (Database.LocationTableEmpty()) { 
                testResult.location = LocationService.getLocation();
                testResult.address = LocationService.getLocation();
            } else {
                testResult.location = database.getLocation();
                testResult.address = Database.getAddress() + " (" + Database.getLatitude() + ", "
                        + Database.getLongitude() + ")";
            }
            testResult.lat = LocationService.getLat();
            testResult.lng = LocationService.getLng();
            testResult.carrier = LocationService.getCarrier();
            testResult.videoDetails = Tester.videoDetails;

            if (!command.testIP("", 4)) {
                jsTester.call("toggleTestingInProgress", "false");  //show connection message
                FilePrep.setTimestamp(testResult.timestamp);
                FilePrep.addDetail("fail");
                testResult.type = "N/A";
                testResult.upload = 0.0;
                testResult.download = 0.0;
                testResult.jitter = 0.0;
                testResult.delay = 0.0;
                testResult.mos = 0.0;
                testResult.videoDetails = "N/A";

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        Database database = new Database(Globals.DB_NAME, Globals.TABLE_NAME);
                        Integer[] arg = {4};
                        Timber.debug(className, "This is the Runnable attempt to save to File from failed state");
                        FilePrep.saveToFile();
                        //insert data into
                        database.insertData(testResult);
                        String[] args = {"#history-results", "result", database.selectData()};
                        jsTemplater.call("loadTemplate", args);
                    }
                });
                jsTester.call("resetClick");
                testRunning = false;
                return;
            } else {
                jsTester.call("hideConnection");
            }
            
            testRunning = true;
            Integer[] arg = {1}; //once you click it it should go on
            jsTester.call("setPhase", arg);
            FilePrep.setTimestamp(testResult.timestamp);
            Timber.debug(className, "call probe test");
            runProbeTest();
        }
    }
    
    private Tester[] createTests() {
        TCPTestConfig tcpTestWest = fromConfig(Globals.WEST_SERVER, configTier);
        writeProbeResult("Test parameters: ");
        writeProbeResult(tcpTestWest.toDict());
        FilePrep.addDetail(this.probeResults);
        FilePrep.addDivider();
        FilePrep.addDetail("\n");
        this.probeResults = "";
        TCPTestConfig tcpTestEast = fromConfig(Globals.EAST_SERVER, configTier);
        UDPTestConfig udpTestWest = new UDPTestConfig(Globals.WEST_SERVER, Globals.UDP_PORT);
        UDPTestConfig udpTestEast = new UDPTestConfig(Globals.EAST_SERVER, Globals.UDP_PORT);
        PingTestConfig pingTestWest = new PingTestConfig(Globals.WEST_SERVER);
        PingTestConfig pingTestEast = new PingTestConfig(Globals.EAST_SERVER);
        Tester[] tests = {
            new TCPTester(tcpTestWest),
            new PingTester(pingTestWest),
            new UDPTester(udpTestWest),
            new TCPTester(tcpTestEast),
            new PingTester(pingTestEast),
            new UDPTester(udpTestEast)};
        return tests;
    }
    
    private void createListeners() {
        //Register generic listeners
        ChangeListener listener = (ChangeListener) (ObservableValue observable, Object oldValue, Object newValue) -> {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        };
        
        TCPTester.uploadProperty().addListener((o, oldVal, newVal) -> {
            if ((Double) newVal > 0.0) {
                testResult.upload = Double.valueOf(String.format("%1$.2f", ((Double) newVal / 1000)));
            }
            tests_complete = 0;
            final Integer[] arg1 = {tests_complete};
            final Double[] result = {testResult.upload};
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1);
                    jsTester.call("setTestResult", result);
                }
            });
        });

        TCPTester.downloadProperty().addListener((o, oldVal, newVal) -> {
            if ((Double) newVal > 0.0) {
                testResult.download = Double.valueOf(String.format("%1$.2f", ((Double) newVal / 1000)));
            }
            tests_complete = 1;
            final Integer[] arg1 = {tests_complete};
            final Double[] result = {testResult.download};
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1);
                    jsTester.call("setTestResult", result);
                }
            });
        });

        PingTester.pingProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if ((Double) newVal > 0.0) {
                    testResult.delay = Double.valueOf(String.format("%1$.2f", ((Double) newVal)));
                }
                tests_complete = 2;
                final Integer[] arg1 = {tests_complete};
                final Double[] result = {testResult.delay};

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jsTester.call("setTestsComplete", arg1);
                        jsTester.call("setTestResult", result);
                    }
                });
            }
        });

        UDPTester.udpProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                if ((Double) newVal > 0.0) {
                    testResult.jitter = Double.valueOf(String.format("%1$.2f", ((Double) newVal)));
                }
                tests_complete = 3;
                final Integer[] arg1 = {tests_complete};
                final Double[] result = {testResult.jitter};
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        jsTester.call("setTestsComplete", arg1);
                        jsTester.call("setTestResult", result);
                    }
                });
            }
        });
    }
    
    private void createPrelimListeners() {
        TCPTester.uploadProperty().addListener((o, oldVal, newVal) -> {
            if ((Double) newVal > 0.0) {
                testResult.upload = Double.valueOf(String.format("%1$.2f", ((Double) newVal / 1000)));
            }
            tests_complete = 0;
            final Integer[] arg1 = {tests_complete};
            final Double[] result = {testResult.upload};
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1);
                    jsTester.call("setTestResult", result);
                }
            });
        });

        TCPTester.downloadProperty().addListener((o, oldVal, newVal) -> {
            if ((Double) newVal > 0.0) {
                testResult.download = Double.valueOf(String.format("%1$.2f", ((Double) newVal / 1000)));
            }
            tests_complete = 1;
            final Integer[] arg1 = {tests_complete};
            final Double[] result = {testResult.download};
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    jsTester.call("setTestsComplete", arg1);
                    jsTester.call("setTestResult", result);
                }
            });
        });
    }
    
    
    private TCPTestConfig fromConfig(String server, int tier) {
        Integer threadNumber;
        Integer testTime;
        String tierName = getTierName(tier);
        String windowSizeProperty = config.getAppProperty(String.format("test.tcp.%s.window_size", tierName));
        String threadsProperty = config.getAppProperty(String.format("test.tcp.%s.threads", tierName));
        String testTimeProperty = config.getAppProperty(String.format("test.tcp.%s.test_time", tierName));
        Timber.debug(className, String.format("tier: %d, thread num: %s, window size: %s, test time: %s",
                tier, threadsProperty, windowSizeProperty, testTimeProperty));
        try {
            threadNumber = Integer.valueOf(threadsProperty);
        } catch (NumberFormatException e) {
            threadNumber = Globals.DEFAULT_TCP_THREAD_NUMBER;
        }
        try {
            testTime = Integer.valueOf(testTimeProperty);
        } catch (NumberFormatException e) {
            testTime = Globals.DEFAULT_TCP_TEST_TIME;
        }
        if (tier == 0) {
            return new TCPTestConfig(server, Globals.TCP_PORT, windowSizeProperty,
                    threadNumber, testTime);
        } else {
            return new TCPTestConfig(server, Globals.TCP_PORT, windowSizeProperty,
                    threadNumber, testTime);
        }
    }
    
    private String getTierName(int tier) {
        switch (tier) {
            case 0:
                return "zero";
            case 1:
                return "one";
            case 2:
                return "two";
            case 3:
                return "three";
            case 4:
                return "four";
            default:
                return "one";
        }
    }
    /**
     * 7/13/2020 - Adding preliminary tests
     * Create a probe test TCPTester object. Then set up a isDoneProperty()
     * listener to get the final download value. Set up the configuration
     * for the actual TCP test.
     * Start the the TCPTester object in a separate thread. Create a listener
     * to capture the output. Then start the thread. As the thread runs, 
     * the output will write to the files with the "PRELIM TEST NOTES"
     */
    private void runProbeTest() {
        TCPTestConfig tcpTestWest = fromConfig(Globals.WEST_SERVER, Globals.TIER_ZERO);
        TCPTester probeTester = new TCPTester(tcpTestWest);
        createPrelimListeners();
        probeTester.isDoneProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                double downloadValue = probeTester.getFinalDownloadValue().getValue();
                Timber.debug(className, "Download speed is: " + String.valueOf(downloadValue));
                writeProbeResult("\nDownload speed result is: " + String.valueOf(downloadValue));
                setTier(downloadValue);
                Globals.threadData.clear();
                Tester tests[] = createTests();
                probeTestFinished = true;
                System.out.println("probe test: change to phase 2");
                jsTester.call("setPhase", 2);
                createListeners();
                runAllTests(tests, 0);
            }
        });
        FilePrep.addDivider();
        writeProbeResult("PRELIM TEST NOTES");
        writeProbeResult("\nPrelim test configuration: ");
        writeProbeResult(tcpTestWest.toDict());
        FilePrep.addDetail(this.probeResults);
        this.probeResults = "";
        testThread = new Thread(probeTester);
        testThread.setDaemon(true);
        Timber.debug(className, "start probe test");
        testThread.start();
        this.probeTestFinished = false;
        Timber.debug(className, "calling jsTester probeTest");
        jsTester.call("probeTest");
    }
    
    private void writeProbeResult(String msg) {
        this.probeResults += msg;
        this.probeResults += System.lineSeparator();
    }
    
    private void setTier(Double value) {
        if (value < 10000) {
            configTier = Globals.TIER_ONE;
        } else if ((value >= 10000) && (value < 100000)) {
            configTier = Globals.TIER_TWO;
        } else if ((value >= 100000) && (value < 250000)) {
            configTier = Globals.TIER_THREE;
        } else if (value >= 250000) {
            configTier = Globals.TIER_FOUR;
        } else {
            configTier = Globals.TIER_ONE;
        }
        Timber.debug(className, "CONFIG TIER: " + String.valueOf(configTier));
    }

    public void updateSetSurvey() {
        Calendar cal = Calendar.getInstance();
        showSurvey = cal.getTimeInMillis() > Database.getExpiredTime();
        jsSurvey.call("setShowSurvey", showSurvey);
    }

    public void updateSettings() {
        jsLocation.call("loadSettingsInput", Database.getExistingIsp(), LocationService.getCarrier());
    }

    /**
     *
     * @param tests The array of tests that will be run by this method
     * @param current_test The current index for the tests array
     */
    private void runAllTests(final Tester[] tests, final int current_test) {
        Timber.debug(className, "RUNNING TEST " + String.valueOf(current_test));
        //base case (when the final test has been reached)
        if (tests.length == (current_test + 1)) {
            final Gson jsonParser = new Gson();
            tests[current_test].isDoneProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue o, Object oldVal, Object newVal) {
                    Tester.calculateSpeed();
                    testResult.mos = MOSCalculation.getMOS();
                    testResult.video = Tester.calcVideo();
                    testResult.conference = Tester.calcConference();
                    testResult.voip = Tester.calcVoip();
                    testResult.videoDetails = Tester.videoDetails;
                    testResult.conferenceDetails = Tester.conferenceDetails;
                    //Tester.printMaps();
                    Tester.resetScores();
                    final String result = jsonParser.toJson(testResult);

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            jsTester.call("resetClick");
                            testRunning = false;
                            jsNavigator.call("setTestLock", false);
                            Database database = new Database(Globals.DB_NAME, Globals.TABLE_NAME);
                            Integer[] arg = {4};
                            jsTester.call("setTestsComplete", arg);
                            String[] args = {"#final-results", "finish", result};
                            try {
                                jsTemplater.call("loadTemplate", args);
                            } catch (Exception e) {
                                Timber.error(className, e.getMessage());
                            }
                            Timber.debug(className, "Do no ask again set. Going to send file");
                            FilePrep.saveToFile();
                            //insert data into
                            database.insertData(testResult);
                            args[0] = "#history-results";
                            args[1] = "result";
                            args[2] = database.selectData();
                            jsTemplater.call("loadTemplate", args);
                            Timber.debug(className, "MOS: " + MOSCalculation.getMOS());
                            Tester.clearMetrics();
                            updateLastTestResult();
                            Instant end = Instant.now();
                            Duration timeElapsed = Duration.between(start, end); 
                            Timber.info(className, "Test time: " + timeElapsed.toString());
                            System.out.println("Test time: " + timeElapsed.toString());
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
        testThread = new Thread(tests[current_test]);
        testThread.setDaemon(true);
        testThread.start();
    }

    /**
     * allows the main Main to kill the test thread when the app is closed.
     */
    public void stopTest() {
        if (testThread != null && testThread.isAlive()) {
            //need a way to kill the thread.
            testThread.interrupt();
        }
    }

    /**
     * helper function to get the correct tests complete number (hardcoded for
     * now. maybe one day we'll get fancy)
     *
     * @param index the current index of the tests handled from the runAllTests
     * method.
     */
    public void setTestsCompleteFromIndex(int index) {
        Timber.debug(className, "test index: " + String.valueOf(index));
        int testsComplete;
        switch (index) {
            case 0:
            case 3:
                testsComplete = 2;
                break;
            case 1:
            case 4:
                testsComplete = 3;
                break;
            default:
                testsComplete = 4;
                break;
        }
        Integer[] arg = {testsComplete};
        jsTester.call("setTestsComplete", arg);
    }

    /**
     * Helper function for debugging javascript
     *
     * @param result the result that comes from javascript that will be
     * displayed in the console.
     */
    public void printResult(String result) {
        System.out.println(result);
    }

    public void log(String msg) {
        Timber.debug(className, msg);
    }

    public String getVersion() {
        return this.version;
    }

    /**
     * @param lat
     * @param lng called from the GUI when a marker is clicked
     */
    public void getViewerResults(String lat, String lng) {

        if (this.predictedGis != null && this.arcGisData != null) {
            String currentLat = predictedGis.getLat();
            String currentLng = predictedGis.getLng();

            predictedGis.cancel();
            arcGisData.cancel();

            if (currentLat.equals(lat) && currentLng.equals(lng)) {
                return;
            }
        }
        
        arcGisData = new ArcGis(lng, lat, Globals.ADVERTISED);
        Thread advertisedThread = new Thread(arcGisData);
        advertisedThread.setDaemon(true);
        advertisedThread.start();
        arcGisData.finalValueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue o, Object oldVal, Object newVal) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        //sort by the different tabs we have for advertised data
                        String value = (String) newVal;
                        if (!value.equals("error")) {
                            String advertisedFixed = arcGisData.getByType((String) newVal, Globals.FIXED);
                            Timber.verbose(className, "Advertised Fixed: " + advertisedFixed);
                            if (advertisedFixed.equals("error")) {
                                advertisedFixed = ArcGis.getJSONError();
                            }
                            String[] fixedArgs = {"#advertised-fixed", "viewer-results", advertisedFixed};
                            jsTemplater.call("loadTemplate", fixedArgs);
                            String advertisedMobile = arcGisData.getByType((String) newVal, Globals.MOBILE);
                            Timber.verbose(className, "Advertised Min Mobile: " + advertisedMobile);
                            if (advertisedMobile.equals("error")) {
                                advertisedMobile = ArcGis.getJSONError();
                            }
                            String[] mobileArgs = {"#advertised-mobile", "viewer-results", advertisedMobile};
                            jsTemplater.call("loadTemplate", mobileArgs);
                        }
                    }
                });
            }
        });
    }

    public void processLocation() {
        jsLocation = (JSObject) webEngine.executeScript("Location");
        jsTester = (JSObject) webEngine.executeScript("Tester");//will have to switch to one in location
        //jsTester.setMember("app", this);
        jsLocation.setMember("app", this);
        //Stringtemp = (String) jsTester.call("doThis");

        String address = (String) jsLocation.call("getAddress");
        String lat = (String) jsLocation.call("getLat");
        String lng = (String) jsLocation.call("getLong");

        if (!(address.equals("") && (lat.equals("") || lng.equals("")))) //if either address or latlong is valid
        {
            String latlng = lat + "," + lng;
            //locationProcess(address, latlng);
        }

    }

    /* 
    Terms methods to be called in Tester.js
    */
    public String getTermsValue() {
        return database.getTermsValue();
    }

    /* 
    Insert agree value to validation table 
    */
    public void insertTermsValue() {
        database.insertTermsValue();
    }

    public void exitOnTerms() {
        System.exit(1);
    }

    public void insertLocationData(String address, String lat, String lng,
            String city, String zip, String type, String carrier) {
        if ((!address.equals("N/A")) && (city.equals("N/A")) && (zip.equals("N/A"))) {
            city = Database.getCity();
            zip = Database.getZip();
        }
        database.insertLocationData(address, lat, lng, city, zip, type, carrier);
    }

    public Database getDatabase() {
        return database;
    }

    public void printLocationData(String address, String lat, String lng,
            String city, String zip, String carrier) {
        Timber.debug(className, "address: " + address);
        Timber.debug(className, "lat: " + lat);
        Timber.debug(className, "long: " + lng);
        Timber.debug(className, "city: " + city);
        Timber.debug(className, "zip: " + zip);
        Timber.debug(className, "carrier: " + carrier);
    }

    public void disableSurvey() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 7);
        Database.setExpiredTime(calendar.getTime());
    }

    public void saveSurveyForm(String isSatisfied, String uploadSpeed,
            String downloadSpeed, String additionalComments) {
        boolean satisfied = isSatisfied.equals("true");
        Double upload = Double.parseDouble(uploadSpeed);
        Double download = Double.parseDouble(downloadSpeed);
        if (this.timestamp == 0L) {
            this.timestamp = Calendar.getInstance().getTimeInMillis();
        }
        FilePrep.saveToFile();
    }

    public void skippedSurvey() {
        FilePrep.saveToFile();
    }

    private void updateLastTestResult() {
        String lastUploadValue = Database.getLastUploadSpeed();
        String lastDownloadValue = Database.getLastDownloadSpeed();
        Double uploadValue = Double.parseDouble(lastUploadValue);
        Double downloadValue = Double.parseDouble(lastDownloadValue);
        if (uploadValue < 0.0) {
            lastUploadValue = "N/A";
        }
        if (downloadValue < 0.0) {
            lastDownloadValue = "N/A";
        }
        log("Last known values: UP: " + lastUploadValue + " DOWN: " + lastDownloadValue);
        jsSurvey.call("loadLastResult", lastUploadValue, lastDownloadValue);
    }

}
