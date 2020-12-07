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

 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Tester.utils.configs;

import Tester.utils.Globals;
import java.io.File;

/**
 *
 * @author joshuaahn
 */
public class TCPTestConfig extends TestConfig {
    String windowSize;
    String windowSizeFormat;
    Integer threadNumber;
    Integer testInterval;
    Integer timeout;
    
    public TCPTestConfig(String serverIp, String port, String windowSizeFormat, 
            String windowSize, Integer threadNumber, Integer testTime,
            Integer testInterval, Integer timeout) {
        this(serverIp, port, windowSizeFormat, windowSize, threadNumber, testTime, 
                testInterval);
        this.timeout = timeout;
    }
    
    public TCPTestConfig(String serverIp, String port, String windowSizeFormat, 
            String windowSize, Integer threadNumber, Integer testTime,
            Integer testInterval) {
        this(serverIp, port, windowSize, threadNumber, testTime);
        this.testInterval = testInterval;
        this.timeout = Globals.DEFAULT_TCP_TIMEOUT;
    }
    
    public TCPTestConfig(String serverIp, String port, String windowSize, 
            Integer threadNumber, Integer testTime) {
        this(serverIp, port, windowSize, threadNumber);
        this.testTime = testTime;
        this.windowSizeFormat = Globals.DEFAULT_TCP_FORMAT;
        this.testInterval = Globals.DEFAULT_TCP_TEST_INTERVAL;
    }
    
    public TCPTestConfig(String serverIp, String port, String windowSize, 
            Integer threadNumber) {
        super(Globals.TCP, serverIp, port, threadNumber, Globals.DEFAULT_TCP_TEST_TIME);
        this.serverIp = serverIp;
        this.port = port;
        this.windowSize = windowSize;
        this.threadNumber = threadNumber;
        this.windowSizeFormat = Globals.DEFAULT_TCP_FORMAT;
        this.testInterval = Globals.DEFAULT_TCP_TEST_INTERVAL;
        this.timeout = Globals.DEFAULT_TCP_TIMEOUT;
    }
    
    public TCPTestConfig(String ip, String port, String test) {
        this(ip, port, Globals.DEFAULT_TCP_WINDOW_SIZE, Globals.DEFAULT_TCP_THREAD_NUMBER);
    }
    
    @Override
    public String createIperfCommandLine() {
        String iperfFilePath = Globals.USER_DIR + File.separator + 
                (Globals.IS_OSX ? Globals.IPERF_MAC : Globals.IPERF_WIN);
        return String.format("%1$s -c %2$s -p %3$s -e -w %4$s -P %5$d -i %6$d "
                + "-t %7$d -f %8$s", iperfFilePath, this.serverIp, this.port, 
                this.windowSize, this.threadNumber, this.testInterval, 
                this.testTime, this.windowSizeFormat);
    }
    
    public String whichServer() {
        if (this.serverIp.equals(Globals.WEST_SERVER)) {
            return "WEST";
        } else if (this.serverIp.equals(Globals.EAST_SERVER)) {
            return "EAST";
        } else {
            return null;
        }
    }
    
    public String getWindowSize() {
        return this.windowSize;
    }
    
    public int getTimeout() {
        return this.timeout;
    }
    
    void setIp(String ip) {
        this.serverIp = ip;
    }
    
    void setPort(String port) {
        this.port = port;
    }
    
    void setWindowSize(String windowSize) {
        this.windowSize = windowSize;
    }
    
    void setWindowSize(Integer windowSize) {
        this.windowSize = String.format("$1%dk", windowSize);
    }
    
    void setThreadNumber(Integer threadNum) {
        this.threadNumber = threadNum;
    }
    
    void setTestTime(Integer testTime) {
        this.testTime = testTime;
    }
    
    void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(this.serverIp);
        sb.append("\nPort: ").append(this.port);
        sb.append("\nWindow size: ").append(this.windowSize);
        sb.append("\nNumber of threads: ").append(this.threadNumber);
        String fullFormatName;
        switch (this.windowSizeFormat) {
            case "k":
                fullFormatName = "Kilobits";
                break;
            case "K":
                fullFormatName = "Kilobytes";
                break;
            case "m":
                fullFormatName = "Megabits";
                break;
            case "M":
                fullFormatName = "Megabytes";
                break;
            default:
                fullFormatName = "Kilobits";
                break;
        }
        sb.append("\nWindow size format: ").append(fullFormatName);
        sb.append("\nTest time length: ").append(this.testTime);
        sb.append("\nTest time interveral: ").append(this.testInterval);
        sb.append("\nTimeout: ").append(this.timeout);
        return sb.toString();
    }

    public String toDict() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\n    Number of threads: ").append(this.threadNumber);
        sb.append("\n    Window size: ").append(this.windowSize);
        sb.append("\n    Test time: ").append(this.testTime);
        sb.append("\n}");
        return sb.toString();
    }
}
