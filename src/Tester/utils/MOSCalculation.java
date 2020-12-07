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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import logging.Timber;

public class MOSCalculation {

    private final static String WEST = "West";
    private final static String EAST = "East";
    private final static String PING = "PING";
    private final static String JITTER = "JITTER";
    private final static String LOSSES = "LOSSES";

    private static ArrayList<Double> pingAveragesWest = new ArrayList<>();
    private static ArrayList<Double> pingAveragesEast = new ArrayList<>();
    private static ArrayList<Double> udpJittersWest = new ArrayList<>();
    private static ArrayList<Double> udpJittersEast = new ArrayList<>();
    private static ArrayList<Double> udpLossesWest = new ArrayList<>();
    private static ArrayList<Double> udpLossesEast = new ArrayList<>();
    private static ArrayList<Double> pingAvgFinals = new ArrayList<>();

    private static final String CLASSNAME = MOSCalculation.class.getName();

    /**
     * Initialize all the variables pingAverages - Pre-calculated ping average
     * of each server test. udpJitters - UDP Jitter from each server. udpLosses
     * - UDP Loss from each server.
     */
    /**
     * reset the mos calculation data
     */
    public static void clearData() {
        pingAveragesWest.clear();
        pingAveragesEast.clear();
        udpJittersWest.clear();
        udpJittersEast.clear();
        udpLossesWest.clear();
        udpLossesEast.clear();
        pingAvgFinals.clear();
    }

    public ArrayList getPingAverages() {
        ArrayList<Double> allPings = new ArrayList<>();
        allPings.addAll(pingAveragesEast);
        allPings.addAll(pingAveragesWest);
        return allPings;
    }

    public ArrayList getJitterAverages() {
        ArrayList<Double> allJitters = new ArrayList<>();
        allJitters.addAll(udpJittersWest);
        allJitters.addAll(udpJittersEast);
        return allJitters;
    }

    public ArrayList getUdpLosses() {
        ArrayList<Double> allLosses = new ArrayList<>();
        allLosses.addAll(udpLossesWest);
        allLosses.addAll(udpLossesEast);
        return allLosses;
    }

    /**
     * Add value to ping list
     *
     * @param ping ping (ms)
     * @param server
     */
    public static void addPing(double ping, String server) {
        Timber.verbose(CLASSNAME, "MOS Add Ping: " + ping);
        if (server.equals(WEST)) {
            pingAveragesWest.add(ping);
        } else {
            pingAveragesEast.add(ping);
        }
    }

    public static void addFinalServerPing(double ping) {
        pingAvgFinals.add(ping);
    }

    /**
     * Add value to jitter list
     *
     * @param jitter jitter (ms)
     * @param server
     */
    public static void addJitter(double jitter, String server) {
        Timber.verbose(CLASSNAME, "MOS Add Jitter: " + jitter);
        if (server.equals(WEST)) {
            udpJittersWest.add(jitter);
        } else {
            udpJittersEast.add(jitter);
        }
    }

    /**
     * Add value to udp loss list
     *
     * @param loss udp packet loss (%)
     * @param server
     */
    public static void addUDPLoss(double loss, String server) {
        Timber.verbose(CLASSNAME, "MOS Add Loss: " + loss);
        if (server.equals(WEST)) {
            udpLossesWest.add(loss);
        } else {
            udpLossesEast.add(loss);
        }
    }

    /**
     * Returns the average of a list
     *
     * @param numbers list of numbers
     * @return average of the list
     */
    private static double average(ArrayList<Double> numbers) {
        double sum = 0;
        for (Double number : numbers) {
            sum += number;
        }
        return sum / numbers.size();
    }

    /**
     * Mean latency of ping packets
     *
     * @param server
     * @return average of latency
     */
    public static double getMeanLatency(String server) {
        ArrayList pingAverages = getValuesForCalculation(server, PING);
        double result = average(pingAverages);
        Timber.verbose(CLASSNAME, "MOS Mean Latency: " + result);
        return result;
    }

    /**
     * Mean jitter (ms) of udp packets
     *
     * @param server
     * @return average of jitter packets
     */
    public static double getMeanJitter(String server) {
        double result = average(getValuesForCalculation(server, JITTER));
        Timber.verbose(CLASSNAME, "MOS Mean Jitter: " + result);
        return result;
    }

    /**
     * Mean udp packet loss (%)
     *
     * @param server
     * @return average of udp packet loss
     */
    public static double getMeanUDPPacketLoss(String server) {
        double result = average(getValuesForCalculation(server, LOSSES));
        Timber.verbose(CLASSNAME, "MOS Mean Packet Loss: " + result);
        return result;
    }

    /**
     * Effective latency
     *
     * @param server
     * @return effective latency
     */
    public static double getEffectiveLatency(String server) {
        double meanLatency = getMeanLatency(server);
        double meanJitter = getMeanJitter(server);
        double result = meanLatency + meanJitter * 2 + 10;
        Timber.verbose(CLASSNAME, "MOS Effective Latency: " + result);
        return result;
    }

    /**
     * Calculate R-Value from effective latency and mean udp packet loss
     *
     * @param server
     * @return R-Value
     */
    public static double getRValue(String server) {
        double effectiveLatency = getEffectiveLatency(server);
        double meanPacketLoss = getMeanUDPPacketLoss(server);
        double result;

        if (effectiveLatency < 160) {
            result = 93.2 - (effectiveLatency / 40) - 2.5 * meanPacketLoss;
        } else {
            result = 93.2 - (effectiveLatency - 120) / 10 - 2.5 * meanPacketLoss;
        }
        Timber.verbose(CLASSNAME, "MOS R-Value: " + result);
        return result;
    }

    /**
     * Calculate the MOS value
     *
     * @param server
     * @return calculated MOS value
     */
    public static double getMOS(String server) {
        ArrayList<Double> pingAverages = getValuesForCalculation(server, PING);
        ArrayList<Double> udpJitters = getValuesForCalculation(server, JITTER);
        ArrayList<Double> udpLosses = getValuesForCalculation(server, LOSSES);
        DecimalFormat df = new DecimalFormat("#0.00");
        double rValue = getRValue(server);
        double result;
        Timber.verbose(CLASSNAME, "Length of data: " + pingAverages.size() + ", "
                + udpJitters.size() + ", " + udpLosses.size());
        if (rValue > 0 && rValue < 101) {
            result = 1 + 0.035 * rValue + 0.000007 * rValue * (rValue - 60) * (100 - rValue);
        } else {
            result = 0;
        }
        Timber.debug(CLASSNAME, "MOS Value: " + result);
        return Double.parseDouble(df.format(result));
    }

    public static double getMOS() {
        return getMOS("BOTH");
    }

    private static ArrayList getValuesForCalculation(String server, String valueType) {
        switch (valueType) {
            case PING:
                switch (server) {
                    case WEST:
                        if (pingAvgFinals.size() > 0) {
                            return new ArrayList<Double>(pingAvgFinals.subList(0, 1));
                        }
                        return pingAveragesWest;
                    case EAST:
                        if (pingAvgFinals.size() > 1) {
                            return new ArrayList<Double>(pingAvgFinals.subList(1, 2));
                        }
                        return pingAveragesEast;
                    default:
                        if (pingAvgFinals.size() == 2) {
                            return pingAvgFinals;
                        }
                        ArrayList pingAverages = new ArrayList<>();
                        pingAverages.addAll(pingAveragesWest);
                        pingAverages.addAll(pingAveragesEast);
                        return pingAverages;
                }
            case JITTER:
                switch (server) {
                    case WEST:
                        return udpJittersWest;
                    case EAST:
                        return udpJittersEast;
                    default:
                        ArrayList udpJitters = new ArrayList<>();
                        udpJitters.addAll(udpJittersWest);
                        udpJitters.addAll(udpJittersEast);
                        return udpJitters;
                }
            case LOSSES:
                switch (server) {
                    case WEST:
                        return udpLossesWest;
                    case EAST:
                        return udpLossesEast;
                    default:
                        ArrayList udpLosses = new ArrayList<>();
                        udpLosses.addAll(udpLossesWest);
                        udpLosses.addAll(udpLossesEast);
                        return udpLosses;
                }
            default:
                return null;
        }
    }
}
