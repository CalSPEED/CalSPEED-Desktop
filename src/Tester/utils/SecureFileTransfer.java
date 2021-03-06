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

import com.jcraft.jsch.*;
import logging.Timber;

public class SecureFileTransfer {

    private final String username;
    private final String host;
    private final String password;
    private final String filename;
    private final String sourcepath;
    private final String destpath;
    private final String CLASSNAME = SecureFileTransfer.class.getName();

    /**
     *
     * @param username
     * @param password
     * @param host
     * @param filename
     * @param sourcepath
     * @param destpath
     */
    public SecureFileTransfer(String username, String password, String host,
            String filename, String sourcepath, String destpath) {
        this.username = username;
        this.host = host;
        this.password = password;
        this.filename = filename;
        this.sourcepath = sourcepath;
        this.destpath = destpath;
    }

    /**
     *
     * @return
     */
    public String send() {
        JSch jsch;
        Session session;
        Channel channel;
        ChannelSftp c;
        try {
            Timber.debug(CLASSNAME, "Starting JSch session");
            jsch = new JSch();
            session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = session.openChannel("sftp");
            channel.connect();
            c = (ChannelSftp) channel;
        } catch (JSchException e) {
            Timber.error(CLASSNAME, e.getMessage());
            return ("Upload File error--Unable to connect to server for upload.\n");
        }
        try {
            Timber.debug(CLASSNAME, "Starting File Upload:");
            String fsrc = sourcepath + filename;
            String fdest = destpath + filename;
            c.put(fsrc, fdest);
        } catch (SftpException e) {
            Timber.error(CLASSNAME, e.getMessage());
            return ("Upload File error--unable to transfer data.\n");
        }
        c.disconnect();
        session.disconnect();
        Timber.debug(CLASSNAME, "Ending session");
        return ("File " + sourcepath + filename + " successfully uploaded.\n");
    }
}
