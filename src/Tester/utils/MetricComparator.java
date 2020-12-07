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
package Tester.utils;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author joshuaahn
 */
public class MetricComparator implements Comparator {
    @Override
    public int compare(Object objectA, Object objectB) {
        Metric a = (Metric) objectA;
        Metric b = (Metric) objectB;
        if (a.getServer().equals("WEST") && !(b.getServer().equals("WEST"))) {
            return -1;
        } else if (a.getServer().equals("EAST") && !(b.getServer().equals("EAST"))) {
            return 1;
        } else {
            if (a.getDirection().equals("UP") && !(b.getDirection().equals("UP"))) {
                return -1;
            } else if (a.getDirection().equals("DOWN") && !(b.getDirection().equals("DOWN"))) {
                return 1;
            } else {
                Integer aInt = (int) Double.parseDouble(a.getTimeRange());
                Integer bInt = (int) Double.parseDouble(b.getTimeRange());
                if (Objects.equals(aInt, bInt)) {
                    return a.getThreadID() - b.getThreadID();
                } else {
                    return aInt - bInt;
                }
            }
        }
    }
}