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

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import logging.Timber;
import java.net.URLDecoder;

public class CommandLine {

    private Timer timeoutTimer;
    private Process currentProcess;
    private ActionListener processTimeout;
    public static final int STANDARD_BUFFER = 0;
    public static final int ERROR_BUFFER = 1;
    public final String className = CommandLine.class.getName();

    
    public void runTest(String test, String ip, String port, int timeout) {
        try {
            String command = getCommand(test, ip, port);
            FilePrep.addDetail(command);
            runCommand(command, timeout);
        } catch (Exception e) {
            handleError("looks like there was a problem.");
            handleError(e.getMessage());
        }
    }

    public void runTest(String command, int timeout) {
        try {
            FilePrep.addDetail(command);
            runCommand(command, timeout);
        } catch (Exception e) {
            handleError("Command string raised an exception.\n");
            handleError(e.getMessage());
        }
    }

    /**
     *
     * @param test
     * @param ip
     * @param port
     * @return
     */
    public String getCommand(String test, String ip, String port) {
        test = test.toLowerCase();
        String command;

        if (test.equals(Globals.TCP)) {
            command = Globals.USER_DIR + File.separator
                    + (Globals.IS_OSX ? Globals.IPERF_MAC : Globals.IPERF_WIN)
                    + " -c " + ip + " -p " + port + " -e -w 64k -P " + Globals.NUM_TCP_TESTS_PER_SERVER + " -i 1 -t " + Globals.DEFAULT_TCP_TEST_TIME + " -f k";
        } else if (test.equals("udp")) {
            command = Globals.USER_DIR + File.separator
                    + (Globals.IS_OSX ? Globals.IPERF_MAC : Globals.IPERF_WIN)
                    + " -c " + ip + " -u -p " + port + " -i 1 -t 1 -l 220 -b 88k -f k";
        } else {
            command = "ping " + (Globals.IS_OSX ? "-c" : "-n") + " 10 " + ip;
        }
        Timber.debug(className, command);
        return command;
    }

    /**
     *
     * @param command
     * @param timeout
     * @param showMessages
     * @return boolean
     * @throws Exception
     */
    public boolean runCommand(String command, int timeout, boolean showMessages) {
        boolean commandSuccessful = false;
        try {
            // executes the command
            currentProcess = Runtime.getRuntime().exec(command);
        } catch (Exception e) {
            handleError("Runtime command Exception");
            handleError(e.getMessage());
        }
        try {
            // make sure it doesn't take too long
            processTimeout = new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    // UDP is a best effort protocol it will not always return.
                    if (!command.contains("-u")) {
                        timeoutTimer.stop();
                        currentProcess.destroyForcibly();
                        timeoutTimer.removeActionListener(processTimeout);
                    }
                }
            };
            timeoutTimer = new Timer(timeout, processTimeout);
            timeoutTimer.setRepeats(false);
            timeoutTimer.start();
        } catch (Exception e) {
            handleError(e.getLocalizedMessage());
        }
        try {
            BufferedReader reader;
            BufferedReader readerError;
            reader = new BufferedReader(new InputStreamReader(
                    currentProcess.getInputStream()));
            readerError = new BufferedReader(new InputStreamReader(
                    currentProcess.getErrorStream()));
            // Check for errors first
            handleReader(STANDARD_BUFFER, reader, showMessages);
            handleReader(ERROR_BUFFER, readerError, showMessages);
            readerError.close();

            // Waits for the command to finish.
            currentProcess.waitFor();
            timeoutTimer.stop();
            timeoutTimer.removeActionListener(processTimeout);
            if (currentProcess.exitValue() == 0) {
                commandSuccessful = true;
            }
            currentProcess.destroy();
        } catch (IOException | InterruptedException e) {
            handleError("IOException, InterruptedException");
            handleError(e.getMessage());
        } catch (Exception e) {
            handleError("General Exception");
            handleError(e.getLocalizedMessage());
        }
        return commandSuccessful;
    }

    /**
     * calls the other function with the same name allowing for an "optional"
     * parameter to not require passing the showMessages boolean.
     *
     * @param command
     * @param timeout
     * @return boolean
     */
    public boolean runCommand(String command, int timeout) throws Exception {
        return runCommand(command, timeout, true);
    }

    /**
     * called from within runCommand. By altering this class you are able to
     * handle how the program deals with the results from the command.
     *
     * @param output_type
     * @param reader
     * @param showMessages
     */
    public void handleReader(int output_type, BufferedReader reader, boolean showMessages) {
        try {
            if (output_type == STANDARD_BUFFER) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            } else {
                String line;
                while ((line = reader.readLine()) != null) {
                    Timber.error(className, line);
                }
            }
        } catch (IOException e) {
            Timber.error(className, "IOException: " + e.getMessage());
        } catch (Exception e) {
            Timber.error(className, "handleReader: General Exception: " +  e.getMessage());
        }
    }

    /**
     * used to handle parsing and formatting the line to be handled by the
     * application.
     *
     * @param line
     * @return boolean
     */
    public boolean processLine(String line) {
        Timber.verbose(className, line);
        return true;
    }

    /**
     * whenever runCommand throws an error it is handled here. The limiting
     * factor is that you can only modify it to handle the message spit out by
     * the error.
     *
     * @param message
     */
    public void handleError(String message) {
        Timber.error(className, message);
    }


    /**
     * used to make an initial test of an ip address to make sure we can connect
     * to it in the first place. You can pass it as many tests as you want.
     *
     * @param ip
     * @param testCount
     * @return
     * @throws Exception
     */
    public boolean testIP(String ip, int testCount) {
        try {
            return runCommand("ping " + (Globals.IS_OSX ? "-c " : "-n ") + 
                testCount + " " + ip, 5000, false);
        } catch (Exception e) {
            Timber.error(className, e.getLocalizedMessage());
            return false;
        }
    }

    /**
     * makes use of the copyFromJar helper function and sets up the testing
     * files depending on the operating system
     */
    public static void prepareFiles() {
        try {
            if (Globals.IS_OSX) {
                copyFromJar("/assets/", Globals.IPERF_MAC);
                (Runtime.getRuntime()).exec("chmod u+x " + Globals.USER_DIR
                        + File.separator + Globals.IPERF_MAC);
            } else {
                copyFromJar("/assets/", "cygwin1.dll");
                copyFromJar("/assets/", Globals.IPERF_WIN + ".exe");
            }
        } catch (IOException e) {
        }
    }

    /**
     * removes the files that were copied
     */
    public static void cleanUpFiles() {
        if (Globals.IS_OSX) {
            (new File(Globals.USER_DIR + File.separator + Globals.IPERF_MAC)).delete();
        } else {
            (new File(Globals.USER_DIR + File.separator + Globals.IPERF_WIN + ".exe")).delete();
            (new File(Globals.USER_DIR + File.separator + "cygwin1.dll")).delete();
        }
    }

    /**
     * used for running the test on the desktop. Depending on what OS you're
     * running it will copy the correct executable to the working directory to
     * make it possible to run.
     *
     * @param path
     * @param name
     * @throws IOException
     */
    public static void copyFromJar(String path, String name) throws IOException {
        InputStream in;
        byte[] buffer = new byte[1024];
        int read;
        String decodedPath;
        FileOutputStream fos;
        in = CommandLine.class.getResourceAsStream(path + name);
        decodedPath = URLDecoder.decode(Globals.USER_DIR, "UTF-8");
        fos = new FileOutputStream(new File(decodedPath + "/" + name));
        while ((read = in.read(buffer)) != -1) {
            fos.write(buffer, 0, read);
        }
        fos.close();
        in.close();
    }
}
