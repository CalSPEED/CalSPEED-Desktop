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
package Tester.tests;

import Tester.Tester;
import Tester.utils.Globals;
import Tester.utils.ResultProcessor;
import javafx.beans.property.*;

import java.util.ArrayList;

public class TCPTester extends Tester {

    protected static ArrayList<Double> uploadValues;
    protected static ArrayList<Double> downloadValues;
    protected static DoubleProperty uploadValue;
    protected static DoubleProperty downloadValue;

    private static int TCP_COUNT = 0;
    private static int TCP_COMPLETE = 0;

    //So we can calculate the correct values on the final result
    private int thread_count;
    private double thread_sum;

    public TCPTester(String ip) {
        super("tcp", ip, Globals.TCP_PORT);
        uploadValues = new ArrayList();
        downloadValues = new ArrayList();
        uploadValue = new SimpleDoubleProperty();
        downloadValue = new SimpleDoubleProperty();
        this.test_timeout = 60000; //1 minute
        thread_count = 0;
        thread_sum = 0.0;
        ++TCP_COUNT;
    }

    @Override
    protected final void setFinalValue(double result) {

        thread_sum += result;

        if( ++thread_count == Globals.NUM_TCP_TESTS_PER_SERVER ) {
            if( currentTest == ResultProcessor.UPLOAD_TEST ) {
                uploadValues.add(thread_sum);
                uploadValue.set(getAverageResult(uploadValues));
            } else if( currentTest == ResultProcessor.DOWNLOAD_TEST ) {
                downloadValues.add(thread_sum);
                downloadValue.set(getAverageResult(downloadValues));
            }
            thread_count = 0;
            thread_sum = 0.0;
        }
    }
    public static DoubleProperty uploadProperty() { return uploadValue; }
    public static DoubleProperty downloadProperty() { return downloadValue; }

    @Override
    protected void succeeded() {
        ++TCP_COMPLETE;
        /*
        * NOTE: the code looks weird because we are dealing with change listeners.
        * These will only be triggered when the value being set to listen for is actually changed.
        * we are forcing uploadValue & downloadValue to change by using the static TCP_COMPLETE variable
        * as well as adding 1 and immediately subtracting 1, thereby forcing the value to change so the
        * change listener can recognize it. Super hacky and dumb. Don't try this at home.
        */

        if(uploadValue.get() <= 0.0) {
            //when no value has been set we can force the values on the GUI to be N/A
            uploadValue.set(-1.0 * TCP_COMPLETE);
        } else if(uploadValue.get() != getRealAverageResult(uploadValues)) {
            Double realUploadValue = getRealAverageResult(uploadValues) + 1.0;
            uploadValue.set(realUploadValue);
            uploadValue.set(realUploadValue - 1.0);
        }

        if(downloadValue.get() <= 0.0) {
            downloadValue.set(-1.0 * TCP_COMPLETE);
        } else if(downloadValue.get() != getRealAverageResult(downloadValues)) {
            Double realDownloadValue = getRealAverageResult(downloadValues) + 1.0;
            downloadValue.set(realDownloadValue);
            downloadValue.set(realDownloadValue - 1.0);
        }

        if(TCP_COMPLETE == TCP_COUNT) { resetCounts(); }
        //uploadValue to database here. To get the value use uploadValue.get()
        //downloadValue to database here. To get the value use downloadValue.get()
        super.succeeded();
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

    private void resetCounts() { TCP_COMPLETE = TCP_COUNT = 0; }
}
