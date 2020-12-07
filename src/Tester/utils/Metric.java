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

/**
 *
 * @author joshuaahn
 */
public class Metric {
    private final int threadID;
    private final Double speed;
    private final String timeRange;
    private final String direction;
    private final String server;

    public Metric(int threadID, Double speed, String timeRange, String direction, String server) {
        this.threadID = threadID;
        this.speed = speed;
        this.timeRange = timeRange;
        this.direction = direction;
        this.server = server;
    }

    public int getThreadID(){
        return threadID;
    }

    public Double getSpeed() {
        return speed;
    }

    public String getTimeRange() {
        return timeRange;
    }

    public String getDirection() {
        return direction;
    }

    public String getServer() {
        return server;
    }

    @Override
    public String toString() {
        String printString = "[ ";
        printString += "Thread ID: ";
        printString += threadID;
        printString += "| Server: ";
        printString += server;
        printString += " | Direction: ";
        printString += direction;
        printString += " | Time Range: ";
        printString += timeRange;
        printString += " | Speed: ";
        printString += speed;
        printString += " ]";
        return printString;
    }
}
