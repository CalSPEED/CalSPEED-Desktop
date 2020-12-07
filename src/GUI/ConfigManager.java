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

import Tester.utils.Globals;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import logging.Timber;

/**
 *
 * @author joshuaahn
 */
public class ConfigManager {
    
    Properties appProperties;
    Properties projectProperties;
    boolean hasProjectProp;
    private static String CLASSNAME;
    
    public ConfigManager() {
        loadAppConfig();
        File projectPropertiesPath = new File(Globals.PROJECT_PROPERTIES);
        if (projectPropertiesPath.exists()) {
            loadProjectConfig();
            hasProjectProp = true;
        } else {
            hasProjectProp = false;
        } 
        CLASSNAME = this.getClass().getName();
    }
    
    private void loadAppConfig() {
        this.appProperties = new Properties();
        try {
            AppConfig appConfig = new AppConfig();
            Set<Map.Entry<String, String>> set = appConfig.getConfig().entrySet();
            for (Map.Entry<String, String> entry : set) {
                this.appProperties.put(entry.getKey(), entry.getValue());
            }
        } catch (Exception eta) {
            Timber.error(CLASSNAME, eta.getMessage());
        }
    }
    
    private void loadProjectConfig() {
        this.projectProperties = new Properties();
        try (InputStream input = new FileInputStream(Globals.PROJECT_PROPERTIES)) {
            this.projectProperties.load(input);
        } catch (Exception eta) {
            Timber.error(CLASSNAME, eta.getMessage());
        }
    }
    
    public boolean hasProjectProperty() {
        return this.hasProjectProp;
    }
    
    public Properties getAppConfig() {
        return this.appProperties;
    }
    
    public Properties getProjectConfig() {
        return this.projectProperties;
    }

    public String getAppProperty(String key) {
        String value = this.appProperties.getProperty(key);
        return value;
    }
    
    public String getProjectProperty(String key) {
        String value = this.projectProperties.getProperty(key);
        return value;
    }

    public void setProjProperty(String key, String value) {
        Timber.info(CLASSNAME, "Setting Project Property " + key + ": " + value);
        this.projectProperties.setProperty(key, value);
    }
    
    void writeProjectConfigToFile() {
        if (this.projectProperties == null) {
            return;
        }
        if (this.projectProperties.isEmpty()) {
            return;
        }
        try (OutputStream output = new FileOutputStream(Globals.PROJECT_PROPERTIES)) {
            this.projectProperties.store(output, null);
        } catch (IOException io) {
            System.out.println(this.projectProperties);
            Timber.error(CLASSNAME, io.getMessage());
        }
    }
}
