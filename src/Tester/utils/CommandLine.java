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

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLine {

    private Timer timeoutTimer;
    private Process currentProcess;
    private ActionListener processTimeout;
    public static final int STANDARD_BUFFER = 0;
    public static final int ERROR_BUFFER = 1;

    /**
     * 
     * @param command
     * @param timeout
     * @param showMessages
     * @return boolean
     * @throws Exception 
     */
    public boolean runCommand(String command, int timeout, boolean showMessages) {
        //will only be set to true when the command successfully completes
        boolean commandSuccessful = false;

        try {
             // executes the command
             currentProcess = Runtime.getRuntime().exec(command);

             // make sure it doesn't take too long
             processTimeout = new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     // UDP is a best effort protocol it will not always return.
                     if (!command.contains("-u")) {
                         timeoutTimer.stop();
                         currentProcess.destroyForcibly();
                         timeoutTimer.removeActionListener(processTimeout);
                         //handleError("The test has timed out. Please try again."); //since we can't throw exceptions from a timer
                     }
                 }
             };

             timeoutTimer = new Timer(timeout, processTimeout);
             timeoutTimer.setRepeats(false);
             timeoutTimer.start();

             // store the responses in the server
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(currentProcess.getInputStream()));
             BufferedReader readerError = new BufferedReader(
                     new InputStreamReader(currentProcess.getErrorStream()));

             // Check for errors first
             handleReader(STANDARD_BUFFER, reader, showMessages);
             handleReader(ERROR_BUFFER, readerError, showMessages);

             reader.close();
             readerError.close();

             // Waits for the command to finish.
             currentProcess.waitFor();
             timeoutTimer.stop();
             timeoutTimer.removeActionListener(processTimeout);

             if (currentProcess.exitValue() == 0) {
                 commandSuccessful = true;
             }
             currentProcess.destroy();
         } catch(Exception e) {
             //do something for the exception
         }
         /*catch (IllegalThreadStateException e) {
            handleError("IllegalThreadStateException", e.getMessage());
        } catch (InterruptedException e) {
            timeoutTimer.stop();
            timeoutTimer.removeActionListener(processTimeout);
            currentProcess.destroy();
            handleError("InterruptedException", e.getMessage());
        } catch (IllegalStateException e) {
            handleError("IllegalStateException", e.getMessage());
        }*/

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

    /************************************************************
    *                                                           *
    *   WRITTEN TO BE OVERWRITTEN BY CLASSES THAT EXTEND IT     *
    *                                                           *
    *************************************************************
    *
    * This approach gives you granular control over what happens within each 
    * command. For example, Tester extends this class and handles processLine 
    * differently.
    *
    * */

    /**
     * called from within runCommand. By altering this class you are able to handle how the program deals
     * with the results from the command.
     * 
     * @param output_type
     * @param reader
     * @param showMessages
     */
    public void handleReader(int output_type, BufferedReader reader, boolean showMessages) {
        try {
            if(output_type == STANDARD_BUFFER) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processLine(line);
                }
            }
        } catch(IOException e) {
            System.out.println(e.getMessage());
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
        System.out.println(line);
        return true;
    }

    /**
     * whenever runCommand throws an error it is handled here. The limiting 
     * factor is that you can only modify it to handle the message
     * spit out by the error.
     * 
     * @param message 
     */
    public void handleError(String message) {
        System.out.println(message);
    }
}