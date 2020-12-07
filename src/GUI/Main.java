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

package GUI;

import Tester.utils.FilePrep;
import Tester.utils.Globals;
import Tester.utils.CommandLine;
import java.io.File;
import java.util.Properties;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import logging.Timber;


public class Main extends Application {

    private Scene scene;
    MainController webView;
    private final String CLASS_NAME = Main.class.getName();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        makeLogDir();
        Timber.info(CLASS_NAME, "Starting Main class");
        Package pack = this.getClass().getPackage();
        Globals.VERSION_NUMBER = pack.getImplementationVersion();
        System.out.println(Globals.VERSION_NUMBER);
        ConfigManager configManager = new ConfigManager();
        readConfiguration(configManager.getAppConfig());
        updateVersion(configManager);
        CommandLine.prepareFiles();
        CommandLine.copyFromJar("/GUI/images/", "logo.png");

        primaryStage.setTitle("CalSPEED");
        primaryStage.getIcons().add(new Image("file:logo.png"));

        webView = new MainController();
        scene = new Scene(webView, 1180, 600);

        primaryStage.setScene(scene);
        primaryStage.setMinHeight(600);
        primaryStage.setMinWidth(1000);
        primaryStage.show();
    }

    @Override
    public void stop() {
        Timber.info(CLASS_NAME, "Stopping Main class");
        webView.stopTest();     //hopefully it will interrupt the sucker
        CommandLine.cleanUpFiles();
        try {
            FilePrep.cleanUpDir();
        } catch (Exception e) {
            Timber.error(CLASS_NAME, "InvocationTargetException caught");
            Timber.error(CLASS_NAME, e.getMessage());
        }
    }

    private void readConfiguration(Properties appProperties) {
        String versionNumber = appProperties.getProperty("app.version");
        if (versionNumber.contains("dev")) {
            Globals.FTP_USERNAME = appProperties.getProperty("ftp.user.dev");
        } else {
            Globals.FTP_USERNAME = appProperties.getProperty("ftp.user.release");
        }
        Globals.VERSION_NUMBER = versionNumber;
        Globals.FTP_PASSWORD = appProperties.getProperty("ftp.password");
        Globals.FTP_SERVER = appProperties.getProperty("ftp.server");
        Globals.WEST_SERVER = appProperties.getProperty("iperf.server.west");
        Globals.EAST_SERVER = appProperties.getProperty("iperf.server.east");
    }

    private void updateVersion(ConfigManager configManager) {
        if (configManager.hasProjectProperty()) {
            String appVersion = configManager.getAppProperty("app.version");
            configManager.setProjProperty("javafx.application.implementation.version", appVersion);
            configManager.writeProjectConfigToFile();
        }
    }
    
    private void makeLogDir() {
        File logsDir = new File("logs");
        if (!logsDir.exists()) {
            try {
                logsDir.mkdir();
                System.out.println("SUCCESS: logs Directory was created successfully");
            } catch (Exception e) {
                System.out.println("FAIL: Failed trying to create the logs directory");
            }
        } else { 
            System.out.println("logs directory already exists");
        }
    }
}
