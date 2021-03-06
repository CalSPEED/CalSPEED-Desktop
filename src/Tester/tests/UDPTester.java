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

package Tester.tests;

import Tester.Tester;
import Tester.utils.LineResult;
import Tester.utils.configs.TestConfig;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;

public class UDPTester extends Tester {

    protected static ArrayList<Double> values;
    protected static DoubleProperty finalValue;

    private static int UDP_COUNT = 0;
    private static int UDP_COMPLETE = 0;

    public UDPTester(TestConfig config) {
        super(config);
        values = new ArrayList<Double>();
        finalValue = new SimpleDoubleProperty();
        ++UDP_COUNT;
    }

    @Override
    protected final void setFinalValue(LineResult lineResult) {
        values.add(lineResult.getResult());
        finalValue.set(getAverageResult(values));
    }

    public static DoubleProperty udpProperty() {
        return finalValue;
    }

    @Override
    protected void succeeded() {
        ++UDP_COMPLETE;
        /*
        * NOTE: the code looks weird because we are dealing with change listeners.
        * These will only be triggered when the value being set to listen for is actually changed.
        * we are forcing finalValue to change by using the static UDP_COMPLETE variable
        * as well as adding 1 and immediately subtracting 1, thereby forcing the value to change so the
        * change listener can recognize it. Super hacky and dumb. Don't try this at home.
         */

        if (finalValue.get() <= 0.0) {
            //when no value has been set we can force the values on the GUI to be N/A
            finalValue.set(-1.0 * UDP_COMPLETE);
        } else if (finalValue.get() != getAverageResult(values)) {
            Double realFinalValue = getAverageResult(values) + 1.0;
            finalValue.set(realFinalValue);
            finalValue.set(realFinalValue - 1.0);
        }
        if (UDP_COMPLETE == UDP_COUNT) {
            resetCounts();
        }
        super.succeeded();
    }

    @Override
    protected void cancelled() {
        succeeded();
    }

    @Override
    protected void failed() {
        succeeded();
    }

    private void resetCounts() {
        UDP_COMPLETE = UDP_COUNT = 0;
    }
}
