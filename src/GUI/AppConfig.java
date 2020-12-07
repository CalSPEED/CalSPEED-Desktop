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
package GUI;

import java.util.HashMap;

/**
 *
 * @author joshuaahn
 */
class AppConfig {
    HashMap appConfig;
    
    AppConfig() {
        this.appConfig = new HashMap();
        this.appConfig.put("app.version", "1.3.2");
        this.appConfig.put("ftp.user.release", "");
        this.appConfig.put("ftp.user.dev", "");
        this.appConfig.put("ftp.password", "");
        this.appConfig.put("ftp.server", "");
        this.appConfig.put("iperf.server.west", "");
        this.appConfig.put("iperf.server.east", "");
        this.appConfig.put("test.tcp.threads", "4");
        this.appConfig.put("test.tcp.window_size", "64k");
        this.appConfig.put("test.tcp.test_time", "20");
        
        /* TIER ZERO - Probe Test */
        this.appConfig.put("test.tcp.zero.threads", "1");
        this.appConfig.put("test.tcp.zero.window_size", "512k");
        this.appConfig.put("test.tcp.zero.test_time", "10");
        
        /* TIER ONE - Under 10M */
        this.appConfig.put("test.tcp.one.threads", "1");
        this.appConfig.put("test.tcp.one.window_size", "512k");
        this.appConfig.put("test.tcp.one.test_time", "20");
        
        /* TIER TWO - 10M ~ 100M */
        this.appConfig.put("test.tcp.two.threads", "4");
        this.appConfig.put("test.tcp.two.window_size", "512k");
        this.appConfig.put("test.tcp.two.test_time", "20");
        
        /* TIER THREE - 100M ~ 250M */
        this.appConfig.put("test.tcp.three.threads", "8");
        this.appConfig.put("test.tcp.three.window_size", "512k");
        this.appConfig.put("test.tcp.three.test_time", "20");
        
        /* TIER FOUR - OVER 250M */
        this.appConfig.put("test.tcp.four.threads", "8");
        this.appConfig.put("test.tcp.four.window_size", "1M");
        this.appConfig.put("test.tcp.four.test_time", "20");
    }
    
    public HashMap getConfig() {
        return this.appConfig;
    }
}
