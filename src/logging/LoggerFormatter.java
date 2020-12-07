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

package logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author joshuaahn
 */
public class LoggerFormatter extends Formatter {
    public LoggerFormatter() {
        super();
    }

    @Override
    public String format(LogRecord record) {
        StringBuilder builder = new StringBuilder(1000);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-dd-MM hh:mm:ss.SSS");
        builder.append(df.format(new Date(record.getMillis())));
        builder.append(" | ");
        builder.append(convertToStandardLevel(record.getLevel()));
        builder.append(" | ");
        builder.append(formatMessage(record));
        builder.append("\n");
        return builder.toString();
    }
    
    public String convertToStandardLevel(Level logLevel) {
        if (logLevel.equals(Level.SEVERE)) {
            return "ERROR";
        } else if (logLevel.equals(Level.WARNING)) {
            return "WARNING";    
        } else if (logLevel.equals(Level.INFO)) {
            return "INFO"; 
        } else if (logLevel.equals(Level.FINE)) {
            return "DEBUG";
        } else if (logLevel.equals(Level.FINER)) {
            return "VERBOSE";
        } else {
            return "DEBUG";
        }
    }
}
