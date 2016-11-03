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
package Tester.utils;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MOSCalculation {

    private static ArrayList <Double> pingAverages;
    private static ArrayList <Double> udpJitters;
    private static ArrayList <Double> udpLosses;

    /**
     * Initialize all the variables
     * pingAverages - Pre-calculated ping average of each server test.
     * udpJitters - UDP Jitter from each server.
     * udpLosses - UDP Loss from each server. 
     */
    static {
        pingAverages = new ArrayList();
        udpJitters = new ArrayList();
        udpLosses = new ArrayList();
    }
    
    /**
     * reset the mos calculation data
     */
    public static void clearData() {
        pingAverages.clear();
        udpJitters.clear();
        udpLosses.clear();
    }
    
    public static ArrayList getPingAverages() {
        return pingAverages;
    }
    
    public static ArrayList getUDPJitters() {
        return udpJitters;
    }
    
    public static ArrayList getUDPLosses() {
        return udpLosses;
    }
    
    /**
     * Add value to ping list
     * @param ping ping (ms)
     */
    public static void addPing(double ping) {
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Add Ping: " + ping);
        }
        pingAverages.add(ping);
    }
    
    /**
     * Add value to jitter list
     * @param jitter jitter (ms)
     */
    public static void addJitter(double jitter) {
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Add Jitter: " + jitter);
        }
        udpJitters.add(jitter);
    }
    
    /**
     * Add value to udp loss list
     * @param loss udp packet loss (%)
     */
    public static void addUDPLoss(double loss) {
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Add Loss: " + loss);
        }
        udpLosses.add(loss);
    }

    /**
     * Returns the average of a list
     * @param numbers list of numbers
     * @return average of the list
     */
    private static double average(ArrayList <Double> numbers) {
        double sum = 0;
        for (Double number : numbers) {
            sum += number;
        }
        return sum / numbers.size();
    }

    /**
     * Mean latency of ping packets
     * @return average of latency
     */
    public static double getMeanLatency() {
    // EXCEL Function
        //=AVERAGE(V2,Z2)

    // Translated
        // return (V2 + Z2) / 2;
        
    // Further translate
        // return (wPktAvg + ePktAvg) / 2;
        double result = average(pingAverages);
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Mean Latency: " + result);
        }
        return result;
    }

    /**
     * Mean jitter (ms) of udp packets
     * @return average of jitter packets
     */
    public static double getMeanJitter() {
    // EXCEL Function
        //=AVERAGE(AB2,AE2)

    // Translated
        // return (AB2 + AE2) / 2;
        
    // Further translate
        // return (wUDP_Jitter + eUDP_Jitter) / 2;
        double result = average(udpJitters);
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Mean Jitter: " + result);
        }
        return result;
    }

    /**
     * Mean udp packet loss (%)
     * @return average of udp packet loss
     */
    public static double getMeanUDPPacketLoss() {
    // EXCEL Function
        //=AVERAGE(AC2,AF2)

    // Translated
        // return (AC2 + AF2) / 2;
        
    // Further translate
        // return (wUDP_Loss + eUDP_Loss) / 2;
        double result = average(udpLosses);
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Mean Packet Loss: " + result);
        }
        return result;
    }

    /**
     * Effective latency
     * @return effective latency
     */
    public static double getEffectiveLatency() {
    // EXCEL Function
        //=AL2+AM2*2+10
        
    // Translated
        // return Mean_Latency + Mean_Jitter * 2 + 10;
        double meanLatency = getMeanLatency();
        double meanJitter = getMeanJitter();
        double result = meanLatency + meanJitter * 2 + 10;
        if (Globals.DEBUG_MOS) {
            System.out.println("MOS Effective Latency: " + result);
        }
        return result;
    }

    /**
     * Calculate R-Value from effective latency and mean udp packet loss
     * @return R-Value
     */
    public static double getRValue() {
    // EXCEL Function
        //=IF(AO2<160,(93.2-(AO2/40)-2.5*AN2),(93.2-(AO2-120)/10-2.5*AN2))

    // Translated
        // if (AO2 < 160) {
        //   return 93.2 - (AO2/40) - 2.5 * AN2;
        // } else {
        //   return 93.2 - (AO2 - 120) / 10 - 2.5 * AN2;
        // }
        
    // Further translate
        // if (Effective_Latency < 160) {
        //   return 93.2 - (Effective_Latency/40) - 2.5 * Mean_Packet_Loss;
        // } else {
        //   return 93.2 - (Effective_Latency - 120) / 10 - 2.5 * Mean_Packet_Loss;
        // }
        double effectiveLatency = getEffectiveLatency();
        double meanPacketLoss = getMeanUDPPacketLoss();
        double result;

        if (effectiveLatency < 160) {
            result = 93.2 - (effectiveLatency / 40) - 2.5 * meanPacketLoss;
            if (Globals.DEBUG_MOS) {
                System.out.println("MOS R-Value: " + result);
            }
        } else {
            result = 93.2 - (effectiveLatency - 120) / 10 - 2.5 * meanPacketLoss;
            if (Globals.DEBUG_MOS) {
                System.out.println("MOS R-Value: " + result);
            }
        }
        return result;
    }

    /**
     * Calculate the MOS value
     * @return calculated MOS value
     */
    public static double getMOS() {
    // EXCEL Function
        //=IF(AND(ISNUMBER(AP2)),IF(AND(AP2>0,AP2<101),(1+0.035*AP2+0.000007*AP2*(AP2-60)*(100-AP2)),0),0)

    // Translated
        // if (ISNUMBER(AP2)) {
        //   if (AP2 > 0 && AP2 < 101) {
        //     1 + 0.035 * AP2 + 0.000007 * AP2 * (AP2 - 60) * (100 - AP2)
        //   } else {
        //     return 0;
        //   }
        // } else {
        //   return 0;
        // }
        
    // Further translate
        // if (R-Value > 0 && R-Value < 101) {
        //   return 1 + 0.035 * R-Value + 0.000007 * R-Value * (R-Value - 60) * (100 - R-Value)
        // } else {
        //   return 0;
        // }
        DecimalFormat df = new DecimalFormat("#0.00");
        double rValue = getRValue();
        double result;

        if (rValue > 0 && rValue < 101) {
            result = 1 + 0.035 * rValue + 0.000007 * rValue * (rValue - 60) * (100 - rValue);
            if (Globals.DEBUG_MOS) {
                System.out.println("MOS Value: " + result);
            }
        } else {
            result = 0;
            if (Globals.DEBUG_MOS) {
                System.out.println("MOS Value: " + 0);
            }
        }
        return Double.parseDouble(df.format(result));
    }
}
