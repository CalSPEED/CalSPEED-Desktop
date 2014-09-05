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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

public class FilePrep {
    private static String details = "";
    private static String timestamp;
    
    /**
     * Add each line of output into the variable details. If output is the same 
     * as equals additional information is stored into the variable details.
     * 
     * @param detail the string to be stored
     */
    public static void addDetail(String detail) {
        if (detail.equals("TCP West Server")) {
            System.out.println("Starting Test 1: Iperf TCP West....");
            details += "Starting Test 1: Iperf TCP West....";
        } else if (detail.equals("TCP East Server")) {
            System.out.println("Starting Test 4: Iperf TCP East....");
            details += System.lineSeparator()
                    + "Starting Test 4: Iperf TCP East....";
        } else if (detail.equals("UDP West Server")) {
            System.out.println("Starting Test 3: Iperf West UDP 1 second test....");
            System.out.println("Starting UDP 1 second Test #1");
            details += System.lineSeparator()
                    + "Starting Test 3: Iperf West UDP 1 second test...."
                    + System.lineSeparator()
                    + "Starting UDP 1 second Test #1";
        } else if (detail.equals("UDP East Server")) {
            System.out.println("Starting Test 6: Iperf East UDP 1 second test....");
            System.out.println("Starting UDP 1 second Test #1");
            details += System.lineSeparator()
                    + "Starting Test 6: Iperf East UDP 1 second test...."
                    + System.lineSeparator()
                    + "Starting UDP 1 second Test #1";
        } else if (detail.equals("PING West Server")) {
            System.out.println("Starting Test 2: Ping West....");
            details += System.lineSeparator()
                    + "Starting Test 2: Ping West....";
        } else if (detail.equals("PING East Server")) {
            System.out.println("Starting Test 5: Ping East....");
            details += System.lineSeparator()
                    + "Starting Test 5: Ping East....";
        } else if (detail.contains("iperf")) {
            System.out.println("Iperf command line:" + detail);
            details += "Iperf command line:" + detail.substring(detail.indexOf("iperf"));
        } else {
            details += detail;
        }
        details += System.lineSeparator();
    }

    /**
     * Store the data from the variable into a file. Store into date generated 
     * filename.
     * <p>
     * Structure (Digits Type)
     * <p>
     * (2 Month)(2 Day)(4 Year)(2 Hour)(2 Minute)(2 Second)(3 Milliseconds).txt
     */
    public static void saveToFile() {
        PrintWriter file = null;
        File uploadDir = null;
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("MMddyyyykkmmssS");
        
        try {
            uploadDir = new File(Globals.USER_DIR + File.separator + "uploads");
            if (!uploadDir.exists()) {
                try {
                    uploadDir.mkdir();
                } catch (SecurityException se) {
                    System.out.println("Debug: Problem with security.");
                }
            }
            
            file = new PrintWriter(Globals.USER_DIR + File.separator + "uploads" + File.separator + fileDateFormat.format(new Date()) + ".txt");
            
            if(Globals.IS_OSX) {
                file.println("Crowd Source OS X Desktop v1.2.1");
            } else {
                file.println("Crowd Source Windows Desktop v1.2.1");
            }

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(timestamp));
            
            file.println("Testing started at " + new SimpleDateFormat("EEE MMM dd kk:mm:ss zzz yyyy").format(cal.getTime()));
            file.println("Network ISP: " + LocationService.getCarrier());
            file.println("OS: Name = " +  System.getProperty("os.name") + ", Architecture = " + System.getProperty("os.arch"));
            file.println("Version: " + System.getProperty("os.version"));
            file.println("IPLastKnownLat: " + LocationService.getLat());
            file.println("IPLastKnownLng: " + LocationService.getLng() + System.lineSeparator());

            Enumeration<NetworkInterface> nets;
            try {
                nets = NetworkInterface.getNetworkInterfaces();
                for(NetworkInterface netint : Collections.list(nets))
                {
                    if(netint.isUp() && !netint.isLoopback() && !netint.getDisplayName().toLowerCase().contains("vmware".toLowerCase()))
                    {
                        file.println("ConnectionName: " + netint.getDisplayName());
                        file.println("ConnectionType: " + netint.getName() + System.lineSeparator());
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }

            file.println(details);
            details = "";
        } catch (FileNotFoundException fnf) {
            System.out.println("Debug: File not found." + fnf);
        } finally {
            if (file != null) {
                file.close();
            }
            saveAllFiles();
        }
    }
    
    /**
     * Set the timestamp.
     * @param timestamp string timestamp to be stored.
     */
    public static void setTimestamp(String timestamp) {
        FilePrep.timestamp = timestamp;
    }
    
    /**
     * Returns an Strings ArrayList with all the files in the upload directory.
     * @return all the files in uploads directory.
     */
    private static ArrayList <String> getAllDetailFiles() {
        ArrayList <String> detailFiles = new ArrayList();
        String uploadPath = Globals.USER_DIR + File.separator + "uploads" + File.separator;
        System.out.println("Debug: uploadPath for getAllDetailFiles " + uploadPath);
        try {
            Files.walk(Paths.get(uploadPath)).forEach(filePath -> {
                String filename = filePath.toString().replace(uploadPath,"");
                System.out.println("Debug: " + filename);
                if (filename.endsWith(".txt")) {
                    detailFiles.add(filename);
                }
            });
            
            return detailFiles;
        } catch (IOException ioe) {
//            System.out.println("Debug: Unable to get results file names.\n");
            return null;
        }
    }
    
    /**
     * Upload all the files onto the server. It will also upload files that were
     *  left behind by previous tests if it exists.
     */
    private static void saveAllFiles() {
        ArrayList <String> detailFiles = getAllDetailFiles();
        String uploadPath = "uploads" + File.separator;
//        System.out.println("Debug: file seperator " + File.separator);
        if (!detailFiles.isEmpty()) {
            for (String detailFile : detailFiles) {
//                System.out.println("Debug: File " + detailFile + " exists.");
                SecureFileTransfer sft = new SecureFileTransfer(
                        "USERNAME",
                        "",
                        "IP ADDRESS",
                        "",
                        "",
                        detailFile,
                        uploadPath,
                        "./UploadData/"
                );
                if ((sft.send()).contains("successfully")) {
//                    System.out.println("Debug: File " + detailFile + " uploaded successfully.");
//                    System.out.println("Debug: File " + Globals.USER_DIR + File.separator + "uploads" + File.separator + detailFile + " deleted.");
                    (new File(Globals.USER_DIR + File.separator + "uploads" + File.separator + detailFile)).delete();
                }
            }
        }
        
    }
    
    /**
     * Finish cleaning up by removing uploads directory.
     */
    public static void cleanUpDir()
    {
        if (getAllDetailFiles().isEmpty()) {
            System.out.println("To be deleted" + new File(Globals.USER_DIR + File.separator + "uploads"));
            (new File(Globals.USER_DIR + File.separator + "uploads")).delete();
            System.out.println("uploads deleted");
        }
    }
}
