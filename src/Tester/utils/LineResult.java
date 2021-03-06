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

/**
 *
 * @author Kyle
 */
public class LineResult {
    Double result;
    Integer threadNum;
    Integer startInterval;
    Integer endInterval;
    
    public LineResult() {
        this.result = null;
        this.threadNum = null;
        this.startInterval = null;
        this.endInterval = null;
    }
    
    public LineResult(Double result, Integer threadNum, Integer startInterval, 
            Integer endInterval) {
        this.result = result;
        this.threadNum = threadNum;
        this.startInterval = startInterval;
        this.endInterval = endInterval;
    }
    
    public LineResult(Double result) {
        this.result = result;
        this.threadNum = null;
        this.startInterval = null;
        this.endInterval = null;
    }
    
    public Double getResult() { return result; }
    
    public Integer getThreadNum() { return threadNum; }
    
    public Integer getStartInterval() { return startInterval; }
    
    public Integer getEndInterval() { return endInterval; }
    
    @Override
    public String toString() {
        return String.format("[%d] %d-%d : %f", this.threadNum, 
                this.startInterval, this.endInterval, this.result);
    }
}
