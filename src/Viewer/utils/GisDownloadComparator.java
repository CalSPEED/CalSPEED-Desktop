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
package Viewer.utils;

import com.google.gson.JsonObject;
import java.util.Comparator;
/**
 * Created by micahiriye on 8/13/14.
 */
public class GisDownloadComparator implements Comparator<JsonObject> {
    private final String carrierKey;
    private final String downloadKey;
    private final String uploadKey;
    private final String techCode;

    public GisDownloadComparator(String carrierKey, String uploadKey, String downloadKey, String techCode) {
        super();
        this.carrierKey = carrierKey;
        this.uploadKey = uploadKey;
        this.downloadKey = downloadKey;
        this.techCode = techCode;
    }

    public GisDownloadComparator() {
        super();
        this.carrierKey = "DBA";
        this.uploadKey = "UploadKey";
        this.downloadKey = "DownloadKey";
        this.techCode = "TechCode";
    }

    /**
     *
     * @param a first JsonObject to compare
     * @param b second JsonObject to compare
     * @return comparator value of -1, 0, or 1
     * This method might seem complicated but really all it's doing is comparing
     * download speeds, upload speeds, and carrier speeds between two 
     * JsonObjects and sorting them based on that priority. The priority is by
     * name, technology description, download speed, upload speed.
     */
    @Override
    public int compare(JsonObject a, JsonObject b) {
        String aName = a.get(carrierKey).getAsString();
        String bName = b.get(carrierKey).getAsString();

        if (aName.compareTo(bName) == 0) {
            String aTechCode = a.get(techCode).getAsString();
            String bTechCode = b.get(techCode).getAsString();
            if (aTechCode.compareTo(bTechCode) == 0) {
                int aDownload = a.get(downloadKey).getAsString().equals(" ") 
                        ? 0 
                        : Integer.valueOf( a.get(downloadKey).getAsString() );
                int bDownload = b.get(downloadKey).getAsString().equals(" ") 
                        ? 0 
                        : Integer.valueOf( b.get(downloadKey).getAsString() );

                if (aDownload < bDownload) {
                    return 1;
                } else if(aDownload > bDownload) {
                    return -1;
                } else {
                    int aUpload = a.get(uploadKey).getAsString().equals(" ") 
                            ? 0 
                            : Integer.valueOf( a.get(uploadKey).getAsString() );
                    int bUpload = b.get(uploadKey).getAsString().equals(" ") 
                            ? 0 
                            : Integer.valueOf( b.get(uploadKey).getAsString() );
                    return aUpload > bUpload ? 1 : aUpload < bUpload ? -1 : 0;
                }
            } else {
                return aTechCode.compareTo(bTechCode); 
            }
        } else {
            return aName.compareTo(bName);
        }
    }
}
