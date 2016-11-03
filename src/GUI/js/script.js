/*!
* Copyright (c) 2014, California State University Monterey Bay (CSUMB).
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* 
*     1. Redistributions of source code must retain the above copyright notice,
*        this list of conditions and the following disclaimer.
* 
*     2. Redistributions in binary form must reproduce the above
*            copyright notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
* 
*     3. Neither the name of the CPUC, CSU Monterey Bay, nor the names of
*        its contributors may be used to endorse or promote products derived from
*        this software without specific prior written permission.
* 
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
* BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
* DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

var DEBUG = false;
var DEBUG_SPEED = 500;
var VERSION = "0.0.0";

// initialize everything
$(function () {
    // setupTemplates();
    $.each($("[data-template]"), function () {
        Templater.loadTemplate(this, "test");
    });

    Tester.init();
    Navigator.init();

    $('#final-results').bind('DOMNodeInserted', function (event) {
        $('#resultModal').modal('show');
    });
    
    var upgrade = $('#upgrade'),
        update = false;
    upgrade.hide();
    $.ajax({
        dataType: 'JSONP',
        url: 'http://calspeed.org/desktop_version',
        success: function(d) {
            var v = d.version.split(/\./),
                o = VERSION.split(/\./);
            for(var i = 0; i < 2; i++) {
                if(v[i] > o[i]) {
                    update = true;
                }
            } 
            if(update) {
               upgrade.show();
            }            
        }
    });
});

if (DEBUG) {
    var app = {
        startTest: function () {
            var totalTimeout = 0;
            var nextPhase = function (phase) {
                setTimeout(function () {
                    Tester.setPhase(phase);
                    if (phase === 3) {
                        Templater.loadTemplate("#final-results", "finish", [{date: 'Jan 20, 2015', time: '19:30', location: 'Marina, 93933', upload: '100.123', download: '100.123', jitter: '1.123', delay: '10.123', mos: '4.5'}]);
                        Templater.loadTemplate("#history-results", "result", [{date: 'Jan 20, 2015', time: '19:30', location: 'Marina, 93933', upload: '100.123', download: '100.123', jitter: '1.123', delay: '10.123', mos: '4.5'}, {}]);
                    }
                }, totalTimeout += DEBUG_SPEED);
            };
            nextPhase(1);
            nextPhase(2);
            nextPhase(3);
        },
        printResult: function (toConsole) {
            console.log(toConsole);
        }
    };
}

$(function () {
    $('#final-results').bind("contentChange", function () {
        $('.mos a').tooltip({
          'placement': 'right'
        }).tooltip('show');
    });
    $('#history-results').bind("contentChange", function () {
        $('td a').tooltip({
          'placement': 'bottom'
        });
    });   
});

var infoMessage = "<p>CalSPEED (Version " + VERSION + "), released by the California Public Utilities Commission (CPUC), empowers end-users with a professional-level, industry-standard testing tool to measure the quality and speed of their mobile data connection.</p>";
infoMessage += "<p>CalSPEED conducts a two-phase test of an initial test and a second validation test in order to ensure statistically significant measurements. The test captures upload speed, download speed, message delay (latency), and message delay variation (jitter). The first two metrics measure broadband throughput, while the second two measure the streaming quality of your mobile broadband connection. A brief results history of each test is stored locally, displaying a detailed description when a test is selected. The results are uploaded to a public repository at the CPUC to provide you with the ability to compare broadband coverage and performance at your location with that in other areas of California.</p>";
infoMessage += "<p>The test results may vary based on factors such as location, end-user hardware, network congestion, and time of day. If you receive a results \"Incomplete\" message, it is because one of these factors has hindered the collection of valid results. Please try running the test again.</p>";

infoMessage += "<p style='display:inline;'>The MOS (Mean Opinion Score) classification for a test result is as follows:</p>";
infoMessage += "<ul style='margin:0px;'><li>Satisfactory: <span style='position:absolute;left:160px;'>Higher than or equal to 4.0</span></li>";
infoMessage += "<li>Unsatisfactory: <span style='position:absolute;left:160px;'>Lower than 4.0</span></li>";
infoMessage += "<li>N/A: <span style='position:absolute;left:160px;'>We can't determine the MOS value.</span></li></ul><br/>";
infoMessage += "<p style='display:inline;'>Video streaming quality for a test result is as follows:</p>";
infoMessage += "<ul style='margin:0px;'><li> HD (High Definition): <span style='position:absolute;left:160px;'>Smooth streaming of 720p or above<span></li>";
infoMessage += "<li> SD (Standard Definition): <span style='position:absolute;left:160px;'>Smooth streaming between 380p and 720p</li>";
infoMessage += "<li> LD (Lower Definition): <span style='position:absolute;left:160px;'>Streaming less than 380p</li>";
infoMessage += "<li> N/A: <span style='position:absolute;left:160px;'>We can't determine the quality.</li></ul><br/>";
infoMessage += "<p>The California Public Utilities Commission (CPUC) is the California recipient of an $8,000,000 State Broadband Data and Development Grant, awarded by the National Telecommunications and Information Administration (NTIA) under the American Recovery and Reinvestment Act (ARRA). A portion of this Grant funds the development, maintenance, and operation of CalSPEED.</p>";
infoMessage += "<p>CalSPEED is developed by California State University, Monterey Bay\'s Computer Science and Information Technology Program.</p>";
infoMessage += "<p>*<small><em>Please note that, depending on the network connection, tests in CalSPEED may use a lot of data capacity. As with any mobile application, monitor your usage relative to your particular data plan. The CPUC does not assume any responsibility for charges incurred while running CalSPEED.</em></small></p>";


var viewerMessage = "<h3>Welcome to Viewer</h3>";
viewerMessage += "<ol><li>Click a location on the map or type an address at the search bar.</li>";
viewerMessage += "<li>Tap the address of selected location.</li>";
viewerMessage += "<li>View upstream and downstream speeds of carriers.</li></ol>";

var termsMessage = "<h3>By selecting the Agree button, you allow the California Public Utilities Commission to collect and display your location data.</h3>";

var locationMessage = "<p>Welcome to Location Settings</p>";
locationMessage += "<ol><li> Enter your address.</li>";
locationMessage += "<li>Click update to save it.</li>";

var feedbackMessage = "";

var errorMessage = "<h3>Invalid location information</h3>";
var successMessage = "<h3>Location information has been successfully updated to: </h3>";
var blankMessage = "<h3>Invalid location information</h3>You must completely enter either an address or a latitude and longitude.";
var connMessage = "<h3>Conection error</h3>You are not connected to the internet.";

var historyMessage = "<h3>Detailed Information</h3>";