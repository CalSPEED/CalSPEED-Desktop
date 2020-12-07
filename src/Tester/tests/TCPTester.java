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

package Tester.tests;

import Tester.Tester;
import Tester.utils.Globals;
import Tester.utils.ThreadData;
import Tester.utils.LineResult;
import Tester.utils.ResultProcessor;
import Tester.utils.configs.TCPTestConfig;
import javafx.beans.property.*;

import java.util.ArrayList;
import logging.Timber;

public class TCPTester extends Tester {
    private final String className;
    protected static ArrayList<Double> uploadValues;
    protected static ArrayList<Double> downloadValues;
    protected static DoubleProperty uploadValue;
    protected static DoubleProperty downloadValue;

    private static int TCP_COUNT = 0;
    private static int TCP_COMPLETE = 0;

    private static Double westUpload = null;
    private static Double westDownload = null;
    private static Double eastUpload = null;
    private static Double eastDownload = null;
    
    private SumValues valueManager;
    private static int westUploadIndex;
    private static int westDownloadIndex;
    protected DoubleProperty trueUploadValue = new SimpleDoubleProperty();
    protected DoubleProperty trueDownloadValue = new SimpleDoubleProperty();

    //So we can calculate the correct values on the final result
    private int thread_count;

    
    public TCPTester(TCPTestConfig configs) {
        super(configs);
        className = this.getClass().getName();
        if (Globals.threadData.isEmpty()) {
            initializeThreads();
        }
        uploadValues = new ArrayList();
        downloadValues = new ArrayList();
        uploadValue = new SimpleDoubleProperty();
        downloadValue = new SimpleDoubleProperty();
        this.test_timeout = configs.getTimeout();   
        valueManager = new SumValues();
        westUploadIndex = 0;
        westDownloadIndex = 0;
        thread_count = 0;
        ++TCP_COUNT;
    }
    
    private void initializeThreads() {
        for (int i = 0; i < this.configs.getThreadNumber(); i++) {
            Globals.threadData.add(new ThreadData());
        }
    }

    @Override
    protected final void setFinalValue(LineResult lineResult) {
        boolean isUpload = true;
        Double result = lineResult.getResult();
        Integer threadNum = lineResult.getThreadNum();
        Integer startInterval = lineResult.getStartInterval();
        addToFinalValue(lineResult);
        if(result >= 0) {
            for(int i = 0; i < configs.getThreadNumber(); i++) {
                if(threadNum.equals(Globals.threadData.get(i).getThreadNum())) {
                    isUpload = Globals.threadData.get(i).addValue(startInterval, result);
                    Integer newKey = startInterval;
                    if (Globals.whichTest == Globals.EAST_UPLOAD || Globals.whichTest == Globals.EAST_DOWNLOAD) {
                        newKey++;
                        newKey *= -1;
                    }
                    Globals.threadData.get(i).addTotalVal(newKey, result);
                }
            }
            if (isUpload) {
                uploadValues.add(result);
            } else {
                downloadValues.add(result);
            }
        }
        if (Globals.whichTest == Globals.EAST_DOWNLOAD) {
            uploadValue.set((westUpload + eastUpload) / 2);
            Timber.verbose(className, String.format("Setting upload value: %s from west: %f and east: %f", 
                    uploadValue.toString(), westUpload, eastUpload));
        }
        if (++thread_count == configs.getThreadNumber() ) {
            if (null != currentTest) switch (currentTest) {
                case ResultProcessor.PROBE_TEST:
                    this.trueDownloadValue.set(valueManager.getDownloadValue());
                    this.trueUploadValue.set(valueManager.getUploadValue());
                    break;
                case ResultProcessor.UPLOAD_TEST:
                    uploadValue.set(getAverageResult(uploadValues));
                    break;
                case ResultProcessor.DOWNLOAD_TEST:
                    downloadValue.set(getAverageResult(downloadValues));
                    break;
                default:
                    break;
            }
            Timber.verbose(className, "Upload rolling value: " + uploadValue.toString());
            Timber.verbose(className, "Download rolling value: " + downloadValue.toString());
            thread_count = 0;
        }

        /** If the result received is negative, that indicates that it is the proper
        * final value and does not need any averaging done to it.
        * must change result to positive and set it properly
        **/
        if (currentTest == ResultProcessor.UPLOAD_TEST && result < 0) {
            double finalResult = result * -1;

            if (Globals.whichTest == Globals.WEST_UPLOAD) {
                westUpload = finalResult;
                Timber.debug(className, "West upload: " + westUpload);
                uploadValue.set(westUpload);
                westUploadIndex = uploadValues.size();
            } else if (Globals.whichTest == Globals.EAST_UPLOAD) {
                eastUpload = finalResult;
                Timber.debug(className, "East upload: " + eastUpload);
                double finalAvgUp = (eastUpload + westUpload) / 2;
                uploadValue.set(finalAvgUp);
            }
        } else if (currentTest == ResultProcessor.DOWNLOAD_TEST && result < 0) {
            double finalResult = result * -1;

            if (Globals.whichTest == Globals.WEST_DOWNLOAD) {
                westDownload = finalResult;
                Timber.debug(className, "West download: " + westDownload);
                downloadValue.set(westDownload);
                westDownloadIndex = downloadValues.size();
            } else if (Globals.whichTest == Globals.EAST_DOWNLOAD) {
                eastDownload = finalResult;
                Timber.debug(className, "East download: " + eastDownload);
                double finalAvgDown = (eastDownload + westDownload) / 2;
                downloadValue.set(finalAvgDown);
            }
        }
    }
    
    /**
     * The algorithm for displaying the rolling results on the GUI.
     * The algorithm for TCP is that we will roll the value up from 0
     * to the actual number Iperf number by the 60th percentile of time. 
     * To simulate that, we use a high divisor, the 60th percentile. 
     * Since the displayed results is an average, we can manipulate the
     * divisor up to a certain point.
     * 
     * e.g. 
     * If we have a 10 second test of all 100 Mbps
     * [  4]  0.0- 1.0 sec  100000 Kbits/sec
     * [  4]  1.0- 2.0 sec  100000 Kbits/sec
     * [  4]  2.0- 3.0 sec  100000 Kbits/sec
     * [  4]  3.0- 4.0 sec  100000 Kbits/sec
     * [  4]  4.0- 5.0 sec  100000 Kbits/sec
     * [  4]  5.0- 6.0 sec  100000 Kbits/sec
     * [  4]  6.0- 7.0 sec  100000 Kbits/sec
     * [  4]  7.0- 8.0 sec  100000 Kbits/sec
     * [  4]  8.0- 9.0 sec  100000 Kbits/sec
     * [  4]  9.0-10.0 sec  100000 Kbits/sec
     * For second #1, the sum is 100,000 and not yet at the 60th percentile
     * (second=6), we display 16,667 (100,000/6) in the GUI.
     * In second #2, the sum is 200,000 and not yet at 6 seconds. 
     * The display is 33,333 (200,000/6). This continues until second = 7
     * At second #7, the sum is 700,000, and now use a divisor of 7
     * since using 6 still would be greater than the 100,000 that Iperf
     * is reporting. So now the GUI display is 100,000 (700,000/7)
     * 
     * Note: The divisor here has an added one. It seems that the rolling
     * number tends to be higher than the actual download value. This is
     * to visual make it look like the final value goes up rather than down.
     * 
     * @param list The total list of all values for uploadValues or downloadValues
     * @return a double value to show for the GUI. These are NOT final values.
     */
    @Override
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
        int divideSize = (int) Math.ceil(list.size() / baseNumber);
        divideSize++;
        if (this.ip.equals(Globals.WEST_SERVER)) {
            if (list.size() < (baseNumber * sixthInterval)) {
                Timber.verbose(className, "using sum / sixthinterval: " + sum + " / " + sixthInterval);
                return sum / sixthInterval;
            } else {
                Timber.verbose(className, "List size: " + list.size());
                Timber.verbose(className, "using sum / dividesize: " + sum + " / " + divideSize);
                return sum / divideSize;
            }
        } else {
            double westValue;
            int offset;
            if (Globals.whichTest == Globals.EAST_UPLOAD) {
                if ((westUpload != null) && (westUpload != 0.0)) {
                    westValue = westUpload;
                    offset = westUploadIndex;
                } else {
                    return eastSmoothResult(sum, divideSize, testLength, sixthInterval);
                }
            } else if (Globals.whichTest == Globals.EAST_DOWNLOAD) {
                if ((westDownload != null) && (westDownload != 0.0)) {
                    westValue = westDownload;
                    offset = westDownloadIndex;
                } else {
                    return eastSmoothResult(sum, divideSize, testLength, sixthInterval);
                }
            } else {
                westValue = 0.0;
                offset = 0;
            }
            if ((westValue == 0.0) || (offset == 0)) {
                return eastSmoothResult(sum, divideSize, testLength, sixthInterval);
            } else {
                divideSize = (list.size() - offset) / baseNumber;
                double eastOnlySum = 0.0;
                for (int k = offset; k < list.size(); k++) {
                    eastOnlySum += list.get(k);
                }
                double eastValue;
                if ((list.size() - offset) < (baseNumber * sixthInterval)) {
                    Timber.verbose(className, "east sum dividing by sixth interval: " + sixthInterval);
                    Timber.verbose(className, "east equivalent sum: " + String.valueOf(eastOnlySum));
                    return eastOnlySum / sixthInterval;
                } else {
                    Timber.verbose(className, "east sum dividing by divide size: " + divideSize);
                    eastValue = eastOnlySum / divideSize;
                    Timber.verbose(className, "east equivalent sum: " + String.valueOf(eastValue));
                    Timber.verbose(className, "taking average of west: " + westValue + " + east: " + eastValue);
                    return (westValue + eastValue ) / 2;
                }                
            }
        }
    }
        
    private Double eastSmoothResult(double sum, int divideSize, int testLength, int sixthInterval) {
        if (divideSize < testLength) {
            Timber.verbose(className, "Smoothing calculation by 12: " + sum / (testLength + sixthInterval));
            return sum / (testLength + sixthInterval);
        } else {
            Timber.verbose(className, "Smoothing calculation by divideSize " + divideSize + ": " + sum / divideSize);
            return sum / divideSize;
        }
    }

    public static DoubleProperty uploadProperty() {
        return uploadValue;
    }

    public static DoubleProperty downloadProperty() {
        return downloadValue;
    }
    
    /**
     * @param lineResult
     */
    protected void addToFinalValue(LineResult lineResult) {
        Timber.verbose(className, String.format("Checking line result %s", lineResult.toString()));
        int startTime = lineResult.getStartInterval();
        int endTime = lineResult.getEndInterval();
        if ((startTime == 0) && (endTime != startTime + 1) && 
                (endTime >= this.configs.getTestTime())) {
            int threadNumber = lineResult.getThreadNum();
            Timber.verbose(className, String.format("Inserting to valueManager: %s=%s", 
                    threadDirection.get(threadNumber), lineResult.getResult()));
            this.valueManager.addValue(threadDirection.get(threadNumber), lineResult.getResult());
        }
    }
    
    void setTrueValue() {
        this.trueUploadValue.set(this.valueManager.getUploadValue());
        this.trueDownloadValue.set(this.valueManager.getDownloadValue());
        Timber.debug(className, String.format("True upload value: %f", 
                    this.trueUploadValue.get()));
        Timber.debug(className, String.format("True download value: %f", 
                    this.trueDownloadValue.get()));
    }
    
    DoubleProperty getFinalUploadValue() {
        return this.trueUploadValue;
    }
    
    public DoubleProperty getFinalDownloadValue() {
        return this.trueDownloadValue;
    }

    private void reset() {
        westUpload = null;
        westDownload = null;
        eastUpload = null;
        eastDownload = null;
    }

    @Override
    protected void succeeded() {
        /*
        * NOTE: the code looks weird because we are dealing with change listeners.
        * These will only be triggered when the value being set to listen for is actually changed.
        * we are forcing uploadValue & downloadValue to change by using the static TCP_COMPLETE variable
        * as well as adding 1 and immediately subtracting 1, thereby forcing the value to change so the
        * change listener can recognize it. Super hacky and dumb. Don't try this at home.
         */
        if (TCP_COMPLETE == 1) {
            if (westUpload == null) {
                westUpload = ThreadData.getTotalWestUP();
            }
            if (westDownload == null) {
                westDownload = ThreadData.getTotalWestDown();   
            }
        }
        if (TCP_COMPLETE == 2) {
            if (eastUpload == null) {
                eastUpload = ThreadData.getTotalEastUp();
            }
            if (eastDownload == null) {
                eastDownload = ThreadData.getTotalEastDown();
            }
            downloadValue.set((westDownload + eastDownload) / 2);
            Timber.verbose(className, String.format("Setting download value: %s from west: %f and east: %f", 
                    downloadValue.toString(), westDownload, eastDownload));

            //resets the values so another test can be run by the user
            reset();
            ThreadData.resetAllThreads();
        }
        setTrueValue();

        if (TCP_COMPLETE == TCP_COUNT) {
            resetCounts();
        }
        //uploadValue to database here. To get the value use uploadValue.get()
        //downloadValue to database here. To get the value use downloadValue.get()
        super.succeeded();
        ++TCP_COMPLETE;
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
    protected Double getRealAverageResult(ArrayList<Double> list) {
        if (!(list.size() > 0)) {
            return -1.0;
        }
        double sum = 0.0;
        for (int i = 0; i < list.size(); i++) {
            sum += list.get(i);
        }
        return sum / list.size();
    }

    private void resetCounts() {
        TCP_COMPLETE = TCP_COUNT = 0;
    }
    
     
    private class SumValues {
        ArrayList<Double> threadUploadValues;
        ArrayList<Double> threadDownloadValues;
        
        SumValues() {
            this.threadUploadValues = new ArrayList<>();
            this.threadDownloadValues = new ArrayList<>();
        }
        
        void addValue(String direction, Double value) {
            if (value < 0) {
                value = -1 * value;
            }
            switch (direction.toLowerCase()) {
                case "up":
                    this.threadUploadValues.add(value);
                    break;
                case "down":
                    this.threadDownloadValues.add(value);
                    break;
                default:
                    Timber.warn(className, "Unknown direction " + direction);
                    break;
            }
        }
        
        Double getUploadValue() {
            Double summ = 0.0;
            for (Double val: this.threadUploadValues) {
                summ += val;
            }
            return summ;
        }
        
        Double getDownloadValue() {
            Double summ = 0.0;
            for (Double val: this.threadDownloadValues) {
                summ += val;
            }
            return summ;
        }
    }
    
}

