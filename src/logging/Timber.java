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
package logging;

import Tester.utils.Globals;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshuaahn
 */
public class Timber {
    static Logger logger;
    
    private Timber(String className) {
        try{
            Handler handler = new FileHandler(Globals.LOG_NAME, Globals.LOG_SIZE, 
                    Globals.LOG_ROTATION_COUNT);
            logger = Logger.getLogger(className);
            logger.addHandler(handler);
            logger.setLevel(Level.FINE);
            LoggerFormatter formatter = new LoggerFormatter();
            handler.setFormatter(formatter);
            logger.info("Start Logger");
            System.out.println("Start Logger");
        } catch (IOException | SecurityException ex) {
            System.out.println("ERROR: " + ex.getMessage());
        }
    }

    private static Logger getLogger(String className) {
        if(logger == null){
            new Timber(className);
        }
        return logger;
    }
    
    public static void log(Level level, String className, String msg) {
        if ((level == Level.INFO) && (level == Level.WARNING) 
                && (level == Level.SEVERE)) {
            System.out.println(className + " | " + level.toString() + " | " + msg);
        }
        getLogger(className).log(level, "{0} | {1}", new Object[]{className, msg});
    }
    
    public static void verbose(String className, String msg) {
        log(Level.FINER, className, msg);
    }
    
    public static void debug(String className, String msg) {
        log(Level.FINE, className, msg);
    }
    
    public static void info(String className, String msg) {
        log(Level.INFO, className, msg);
    }
    
    public static void warn(String className, String msg) {
        log(Level.WARNING, className, msg);
    }
    
    public static void error(String className, String msg) {
        log(Level.SEVERE, className, msg);
    }
}
