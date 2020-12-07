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
package Viewer.utils;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joshuaahn
 */
public class TechnologyCode {
    private static Logger logger = Logger.getLogger(TechnologyCode.class.getName());
    private static final HashMap<Integer, String> TECH_CODE_DICTIONARY = populateDictionary();
    
    public static String get(Integer key) {
        if (TECH_CODE_DICTIONARY.containsKey(key)) {
            return TECH_CODE_DICTIONARY.get(key);
        } else {
            logger.log(Level.WARNING, "Key not found %d, returning N/A", key);
            return "N/A";
        }
        
    }
    
    public HashMap getTechCode() {
        return TECH_CODE_DICTIONARY;
    }

    private static HashMap<Integer, String> populateDictionary() {
        HashMap<Integer, String> initializeDictionary = new HashMap<>();
        initializeDictionary.put(0, "All Other");
        initializeDictionary.put(10, "Asymmetric xDSL");
        initializeDictionary.put(11, "ADSL2, ADSL2+");
        initializeDictionary.put(12, "VDSL");
        initializeDictionary.put(20, "Symmetric xDSL");
        initializeDictionary.put(30, "Other Copper Wireline");
        initializeDictionary.put(40, "Cable Modem other");
        initializeDictionary.put(41, "Cable Modem DOCSIS 1, 1.1, 2.0");
        initializeDictionary.put(42, "Cable Modem DOCSIS 3.0");
        initializeDictionary.put(43, "Cable Modem DOCSIS 3.1");
        initializeDictionary.put(50, "Optical Carrier / Fiber to the end user");
        initializeDictionary.put(60, "Satellite");
        initializeDictionary.put(70, "Terrestrial Fixed Wireless");
        initializeDictionary.put(80, "WCDMA/UTMS/HSPA");
        initializeDictionary.put(81, "HSPA+");
        initializeDictionary.put(82, "EVDO/EVDO Rev A");
        initializeDictionary.put(83, "LTE");
        initializeDictionary.put(84, "WiMAX");
        initializeDictionary.put(85, "CDMA");
        initializeDictionary.put(86, "GSM");
        initializeDictionary.put(87, "Analog");
        initializeDictionary.put(88, "Other");
        initializeDictionary.put(89, "Mobile");
        initializeDictionary.put(90, "Electric Power Line");
        initializeDictionary.put(-999, "N/A");
        return initializeDictionary;
    }
}
