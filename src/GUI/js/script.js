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
        url: 'http://URL/FILE',
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

var infoMessage = "<p>CalSPEED, released by the California Public Utilities Commission (CPUC), empowers end-users with a professional-level, industry-standard testing tool to measure the quality and speed of their mobile data connection.</p>";
infoMessage += "<p>CalSPEED conducts a two-phase test of an initial test and a second validation test in order to ensure statistically significant measurements. The test captures upload speed, download speed, message delay (latency), and message delay variation (jitter). The first two metrics measure broadband throughput, while the second two measure the streaming quality of your mobile broadband connection. A brief results history of each test is stored locally, displaying a detailed description when a test is selected. The results are uploaded to a public repository at the CPUC to provide you with the ability to compare broadband coverage and performance at your location with that in other areas of California.</p>";
infoMessage += "<p>The test results may vary based on factors such as location, end-user hardware, network congestion, and time of day. If you receive a results \"Incomplete\" message, it is because one of these factors has hindered the collection of valid results. Please try running the test again.</p>";
infoMessage += "<p>The California Public Utilities Commission (CPUC) is the California recipient of an $8,000,000 State Broadband Data and Development Grant, awarded by the National Telecommunications and Information Administration (NTIA) under the American Recovery and Reinvestment Act (ARRA). A portion of this Grant funds the development, maintenance, and operation of CalSPEED.</p>";
infoMessage += "<p>CalSPEED is developed by California State University, Monterey Bay\'s Computer Science and Information Technology Program.</p>";
infoMessage += "<p>*<small><em>Please note that, depending on the network connection, tests in CalSPEED may use a lot of data capacity. As with any mobile application, monitor your usage relative to your particular data plan. The CPUC does not assume any responsibility for charges incurred while running CalSPEED.</em></small></p>";

var viewerMessage = "<h3>Welcome to Viewer</h3>";
viewerMessage += "<ol><li> Click a location on the map or type an address at the search bar.</li>";
viewerMessage += "<li> Tap the address of selected location.</li>";
viewerMessage += "<li> View upstream and downstream speeds of carriers.</li></ol>";
