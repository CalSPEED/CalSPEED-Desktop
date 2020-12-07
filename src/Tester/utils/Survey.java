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

import Database.Database;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import logging.Timber;

/**
 *
 * @author joshuaahn
 */
public class Survey {

    private final static String CLASSNAME = Survey.class.getName();
    boolean isSatisfied;
    String uploadSpeed;
    String downloadSpeed;
    String additionalComments;
    long date;
    String filename;

    public Survey(boolean isSatisfied, Double uploadSpeed,
            Double downloadSpeed, String additionalComments, long date) {
        this.isSatisfied = isSatisfied;
        if (uploadSpeed == 0.0 && downloadSpeed == 0.0) {
            this.uploadSpeed = "N/A";
            this.downloadSpeed = "N/A";
        } else {
            this.uploadSpeed = Double.toString(uploadSpeed);
            this.downloadSpeed = Double.toString(downloadSpeed);
        }
        this.additionalComments = additionalComments;
        this.date = date;
    }

    public final void saveToFile() throws FileNotFoundException {
        PrintWriter file = null;
        File uploadDir;
        SimpleDateFormat fileDateFormat = new SimpleDateFormat("MMddyyyykkmmssS");
        String satisfaction;
        if (this.isSatisfied) {
            satisfaction = "Yes";
        } else {
            satisfaction = "No";
        }
        try {
            uploadDir = new File(Globals.USER_DIR + File.separator + "uploads");
            if (!uploadDir.exists()) {
                try {
                    uploadDir.mkdir();
                } catch (SecurityException se) {
                    Timber.error(CLASSNAME, se.getMessage());
                    Timber.error(CLASSNAME, "Debug: Problem with security.");
                }
            }
            filename = String.format("%s%s", fileDateFormat.format(this.date), ".txt");
            String filenameAndPath = String.format("%s%suploads%s%s", Globals.USER_DIR, File.separator,
                    File.separator, filename);
            file = new PrintWriter(filenameAndPath);

            file.println("LastResultUploadSpeed: " + Database.getLastUploadSpeed());
            file.println("LastResultDownloadSpeed: " + Database.getLastDownloadSpeed());
            file.println();
            file.println("Satisfied?: " + satisfaction);
            file.println("UserDefinedUploadSpeed: " + this.uploadSpeed);
            file.println("UserDefinedDownloadSpeed: " + this.downloadSpeed);
            file.println();
            file.println("Additional Comments:" + System.lineSeparator() + this.additionalComments);
        } catch (FileNotFoundException ex) {
            Timber.error(CLASSNAME, ex.getMessage());
            Timber.error(CLASSNAME, "Debug: File not found." + ex.getMessage());
        } finally {
            if (file != null) {
                file.close();
            }
        }
    }

    /**
     * Upload all the files onto the server. It will also upload files that were
     * left behind by previous tests if they exist.
     */
    public void send() {
        String uploadPath = "uploads" + File.separator;
        if (!filename.isEmpty()) {
            SecureFileTransfer sft = new SecureFileTransfer(
                    Globals.FTP_USERNAME,
                    "",
                    "",
                    filename,
                    uploadPath,
                    "./Surveys/"
            );
            String status = sft.send();
            if (status.contains("successfully")) {
                Timber.debug(CLASSNAME, "Upload successful");
                Timber.debug(CLASSNAME, status);
                new File(Globals.USER_DIR + File.separator + "uploads" + File.separator + filename + ".txt").delete();
            } else {
                Timber.info(CLASSNAME, "Upload not successful");
                Timber.info(CLASSNAME, status);
            }
        }
    }

    public void write() {
        String satisfaction;
        if (this.isSatisfied) {
            satisfaction = "Yes";
        } else {
            satisfaction = "No";
        }
        StringBuilder message = new StringBuilder();
        message.append(System.getProperty("line.separator"));
        message.append(System.getProperty("line.separator"));
        message.append("Customer Survey");
        message.append(System.getProperty("line.separator"));
        message.append(String.format("LastResultUploadSpeed: %s",
                Database.getLastUploadSpeed()));
        message.append(System.getProperty("line.separator"));
        message.append(String.format("LastResultDownloadSpeed: %s",
                Database.getLastDownloadSpeed()));
        message.append(System.getProperty("line.separator"));
        message.append(System.getProperty("line.separator"));
        message.append(String.format("Satisfied?: %s", satisfaction));
        message.append(System.getProperty("line.separator"));
        message.append(String.format("UserDefinedUploadSpeed: %s", this.uploadSpeed));
        message.append(System.getProperty("line.separator"));
        message.append("UserDefinedDownloadSpeed: ").append(this.downloadSpeed);
        message.append(System.getProperty("line.separator"));
        message.append(System.getProperty("line.separator"));
        message.append(String.format("Additional Comments:%s%s",
                System.lineSeparator(), this.additionalComments));
        Timber.debug(CLASSNAME, message.toString());
        FilePrep.addDetail(message.toString());
    }
}
