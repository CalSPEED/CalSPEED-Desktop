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

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Kyle
 */
public class ThreadData {

    private static final HashMap<Integer, ArrayList<Double>> totalUploadVals = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Double>> totalDownloadVals = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Double>> westUp = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Double>> westDown = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Double>> eastUp = new HashMap<>();
    private static final HashMap<Integer, ArrayList<Double>> eastDown = new HashMap<>();
    private final HashMap<Integer, Double> uploadVals;
    private final HashMap<Integer, Double> downloadVals;
    private boolean directionUp;
    private Integer threadNum;

    public ThreadData() {
        uploadVals = new HashMap<>();
        downloadVals = new HashMap<>();
        directionUp = true;
        threadNum = null;
    }

    public void setThreadNum(Integer threadNum) {
        this.threadNum = threadNum;
    }

    public void setDirectionUp(boolean directionUp) {
        this.directionUp = directionUp;
    }

    public boolean addValue(Integer interval, Double val) {
        if (directionUp) {
            this.addUploadVal(interval, val);
        } else {
            this.addDownloadVal(interval, val);
        }
        return directionUp;
    }

    private void addUploadVal(Integer interval, Double upVal) {
        uploadVals.put(interval, upVal);
        if (Globals.whichTest == Globals.WEST_UPLOAD) {
            if (westUp.containsKey(interval)) {
                westUp.get(interval).add(upVal);
            } else {
                ArrayList<Double> tempVal = new ArrayList<>();
                tempVal.add(upVal);
                westUp.put(interval, tempVal);
            }
        } else {
            if (eastUp.containsKey(interval)) {
                eastUp.get(interval).add(upVal);
            } else {
                ArrayList<Double> tempVal = new ArrayList<>();
                tempVal.add(upVal);
                eastUp.put(interval, tempVal);
            }
        }
    }

    private void addDownloadVal(Integer interval, Double downVal) {
        downloadVals.put(interval, downVal);
        if (Globals.whichTest == Globals.WEST_DOWNLOAD) {
            if (westDown.containsKey(interval)) {
                westDown.get(interval).add(downVal);
            } else {
                ArrayList<Double> tempVal = new ArrayList<>();
                tempVal.add(downVal);
                westDown.put(interval, tempVal);
            }
        } else {
            if (eastDown.containsKey(interval)) {
                eastDown.get(interval).add(downVal);
            } else {
                ArrayList<Double> tempVal = new ArrayList<>();
                tempVal.add(downVal);
                eastDown.put(interval, tempVal);
            }
        }
    }

    public void resetThread() {
        uploadVals.clear();
        downloadVals.clear();
        directionUp = true;
        threadNum = null;
    }

    public static void resetAllThreads() {
        totalUploadVals.clear();
        totalDownloadVals.clear();
        westUp.clear();
        westDown.clear();
        eastUp.clear();
        eastDown.clear();
    }

    public void toggleDirectionUp() {
        directionUp = !directionUp;
    }

    public Integer getThreadNum() {
        return threadNum;
    }

    public HashMap<Integer, Double> getUploadVals() {
        return new HashMap<>(uploadVals);
    }

    public HashMap<Integer, Double> getDownloadVals() {
        return new HashMap<>(downloadVals);
    }

    public void addTotalVal(Integer interval, Double val) {
        if (directionUp) {
            addTotalUpVal(interval, val);
        } else {
            addTotalDownVal(interval, val);
        }
    }

    private void addTotalUpVal(Integer interval, Double val) {
        if (totalUploadVals.containsKey(interval)) {
            totalUploadVals.get(interval).add(val);
        } else {
            ArrayList<Double> tempVal = new ArrayList<>();
            tempVal.add(val);
            totalUploadVals.put(interval, tempVal);
        }
    }

    private void addTotalDownVal(Integer interval, Double val) {
        if (totalDownloadVals.containsKey(interval)) {
            totalDownloadVals.get(interval).add(val);
        } else {
            ArrayList<Double> tempVal = new ArrayList<>();
            tempVal.add(val);
            totalDownloadVals.put(interval, tempVal);
        }
    }

    public static double getTotalWestUP() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : westUp.keySet()) {
            for (int i = 0; i < westUp.get(key).size(); i++) {
                sum += westUp.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

    public static double getTotalWestDown() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : westDown.keySet()) {
            for (int i = 0; i < westDown.get(key).size(); i++) {
                sum += westDown.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

    public static double getTotalEastUp() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : eastUp.keySet()) {
            for (int i = 0; i < eastUp.get(key).size(); i++) {
                sum += eastUp.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

    public static double getTotalEastDown() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : eastDown.keySet()) {
            for (int i = 0; i < eastDown.get(key).size(); i++) {
                sum += eastDown.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

    public static double getTotalUpAvg() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : totalUploadVals.keySet()) {
            for (int i = 0; i < totalUploadVals.get(key).size(); i++) {
                sum += totalUploadVals.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }

    public static double getTotalDownAvg() {
        double sum = 0.0;
        ArrayList<Double> avgs = new ArrayList<>();
        for (Integer key : totalDownloadVals.keySet()) {
            for (int i = 0; i < totalDownloadVals.get(key).size(); i++) {
                sum += totalDownloadVals.get(key).get(i);
            }
            avgs.add(sum);
            sum = 0;
        }
        for (int i = 0; i < avgs.size(); i++) {
            sum += avgs.get(i);
        }
        return sum / avgs.size();
    }
}
