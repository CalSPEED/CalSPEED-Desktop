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
public class UDPTestConfig extends TestConfig {

    Integer testInterval;
    Integer bufferLength;
    String blockSize;
    String windowSizeFormat;
    Integer timeout;

    public UDPTestConfig(String ip, String port) {
        super(Globals.UDP, ip, port, Globals.DEFAULT_UDP_THREAD_NUMBER, 
                Globals.DEFAULT_UDP_TEST_TIME);
        this.serverIp = ip;
        this.port = port;
        this.testType = Globals.UDP;
        this.testInterval = Globals.DEFAULT_UDP_TEST_INTERVAL;
        this.bufferLength = Globals.DEFAULT_UDP_BUFFER_LENGTH;
        this.blockSize = Globals.DEFAULT_UDP_BLOCK_SIZE;
        this.windowSizeFormat = Globals.DEFAULT_UDP_FORMAT;
        this.timeout = Globals.DEFAULT_UDP_TIMEOUT;
    }

    public UDPTestConfig(String ip, String port, Integer testInterval,
            Integer testTime, Integer bufferLength, String blockSize,
            String windowSizeFormat) {
        this(ip, port);
        this.testType = Globals.UDP;
        this.testInterval = testInterval;
        this.testTime = testTime;
        this.bufferLength = bufferLength;
        this.blockSize = blockSize;
        this.windowSizeFormat = windowSizeFormat;
    }

    @Override
    public String createIperfCommandLine() {
        String iperfFilePath = Globals.USER_DIR + File.separator
                + (Globals.IS_OSX ? Globals.IPERF_MAC : Globals.IPERF_WIN);
        return String.format("%1$s -c %2$s -u -p %3$s -i %4$d -t %5$d -l %6$d "
                + "-b %7$s -f %8$s", iperfFilePath, this.serverIp, this.port,
                this.testInterval, this.testTime, this.bufferLength,
                this.blockSize, this.windowSizeFormat);
    }

    public Integer getTestInterval() {
        return this.testInterval;
    }

    public int getBufferLength() {
        return this.bufferLength;
    }

    public String getBlockSize() {
        return this.blockSize;
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

    void setTestInterval(Integer testInterval) {
        this.testInterval = testInterval;
    }

    void setTestTime(Integer testTime) {
        this.testTime = testTime;
    }

    void setBufferLength(Integer bufferLength) {
        this.bufferLength = bufferLength;
    }

    void setBlockSize(String blockSize) {
        this.blockSize = blockSize;
    }

    void setBlockSize(Integer blockSize) {
        this.blockSize = String.format("$1%dk", blockSize);
    }

    void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("IP: ").append(this.serverIp);
        sb.append("\nPort: ").append(this.port);
        sb.append("\nBlock size: ").append(this.blockSize);
        sb.append("\nBuffer length: ").append(this.bufferLength);
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
}
