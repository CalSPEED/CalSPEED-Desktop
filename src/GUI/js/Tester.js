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

; //Depends on jQuery and Velocityjs and handlebars
var Tester = new function() {

    //variables global to the functions with Tester
    //anything defined with $ at the start is a jQuery object
    this._max_result; //in mbs for the speedometer
    this._$current_phase; //used to monitor the current phase the tester is on
    this._$previous_phase; //used to monitor the phase the tester transitioned from
    this._$current_test; //used to monitor the current test the tester is on
    this._$previous_test; //monitor the previous test the tester was on
    this._$phases; //a wrapper of all the phases
    this._$header;
    this._phase_count; //keeps track of how many phases there are in the tester
    this._test_count; //how many tests are in each phase of tests within the tester
    TesterInstance = this; //allow for jQuery to access it (resolves certain scope issues)

    this.previous_meter;

    this.init = function() {
        _max_result = 100; //in mbs
        _previous_meter = -1;
        _$phases = $(".page-0 .phases");
        _$header = $("#header");
        _$current_phase = _$phases.children(".active");
        _$current_test = _$phases.find(".test").find(".active");
        _test_count = $('.test').first().children("div").length;

        _phase_count = _$phases.children("[class^='phase-']").length;
        _$phases.width(_phase_count * 100 + "%");

        //		$(window).resize(function() {
        //			_$phases.parent().scrollLeft( _$phases.scrollLeft() + _$current_phase.position().left );
        //		});
    }

    this.setPhase = function(phase) {
        //app.printResult("SETPHASE :" + phase);
        if (phase == 1) {
            this.resetResults();
        }

        if (_$phases.find(".phase-" + phase)[0] !== _$current_phase[0]) {
            //reset the tests
            this.setTestsComplete(0);
            _$previous_phase = _$current_phase;
            _$current_phase = _$phases.find(".phase-" + phase);
            _$previous_phase.toggleClass("inactive").toggleClass("active");
            _$current_phase.toggleClass("inactive").toggleClass("active");
        }

        _$phases.parent().velocity("scroll", {
            axis: "x",
            offset: this.calculateScrollLeft(),
            container: _$phases.parent()
        });

        //current test fly in animation
        if (_$current_phase.children(".table").children().hasClass("test")) {
            this.testFlyIn(_$current_phase.find(".test"));
        }

        //previous test fly out animation
        if (_$previous_phase.children(".table").children().hasClass("test")) {
            this.testFlyOut(_$previous_phase.find(".test"));
        }

        //update progress bar
        increment = 100 / (_phase_count - 1);
        $(".progress-bar .progress").velocity({
            width: _$current_phase.index() * increment + "%"
        });
        $(".progress-bar .dot").velocity({
            left: _$current_phase.index() * increment + "%"
        });

        _$header.find(".phase-" + _$current_phase.index()).toggleClass("inactive").toggleClass("active");
        _$header.find(".phase-" + _$previous_phase.index()).toggleClass("inactive").toggleClass("active");
    }

    this.setTestsComplete = function(tests_complete) {
        app.printResult("test complete: " + tests_complete);
        if (_$current_phase.find(".test-" + tests_complete)[0] !== _$current_test[0]) {
            //app.printResult("SETTESTSCOMPLETE: " + tests_complete);
            animation_speed = 500;
            _$previous_test = _$current_test;
            _$current_test = _$current_phase.find(".test-" + tests_complete);

            _$previous_test.toggleClass("inactive").toggleClass("active");
            _$current_test.toggleClass("inactive").toggleClass("active");

            if (tests_complete == 3) {
                animateHeartbeat();
            }

            if (tests_complete == 2) {
                animateDelayLoader();
            }

            percentage_complete = (tests_complete / _test_count * 100) + "%";

            _$current_phase.find(".testprogress .progress").velocity({
                    height: percentage_complete
                }, animation_speed,
                function() {
                    if (tests_complete == _test_count) {
                        //app.printResult("INSIDE CALLBACK:" + _$current_phase.index());
                        TesterInstance.setPhase(_$current_phase.index()); //move on to the next phase when all the tests are complete
                    }
                }
            );

            //move the phase to the next one immediately. but animate it later...
            if (tests_complete == _test_count) {
                //app.printResult("SHOULD SHIFT NOW");
                _$previous_phase = _$current_phase;
                _$current_phase = _$phases.find(".phase-" + (_$current_phase.index() + 1));
            }
        }
    }

    this.setTestResult = function(result) {
        //app.printResult("SETTESTRESULT "  + " current phase: " + _$current_phase.index() + " current test: " + _$current_test.index());
        $current_test = _$phases.find(".phase-" + _$current_phase.index()).find(".test-" + _$current_test.index());
        metric = $current_test.data("metric"); //determines which metric to append

        //determine if we need to set the speedometer or not
        if ($current_test.children().hasClass("speedometer")) {
            this.setSpeedometer(parseFloat(result));
        }

        if (parseFloat(result) <= 0.0) {
            //app.printResult("     should be n/a");
            $current_test.find(".results").html("N/A");
        } else {
            //app.printResult("     should not be n/a");
            $current_test.find(".results").html(parseFloat(result).toFixed(2) + metric);
        }
    }

    this.setSpeedometer = function(result) {
        percentage = result / _max_result * 100;
        if (percentage > 100) {
            percentage = 100;
        }
        $speedometer = _$current_test.find(".speedometer");
        $speedometer.children(".meter").velocity({
            width: percentage + "%"
        }, 100);
    }

    //since velocity's scroll left and jquery's scroll left differ we can't use this calculation in the window resize listener
    this.calculateScrollLeft = function() {
        return _$current_phase.position().left - _$previous_phase.position().left;
    }

    this.testFlyIn = function($selector) {

        $selector.children(".upload").velocity({
            rotateZ: "360",
            left: "0",
            top: "0",
        }, {
            duration: 700,
            easing: "spring"
        });

        $selector.children(".download").velocity({
            rotateZ: "360",
            left: "350",
            top: "200"
        }, {
            duration: 700,
            easing: "spring"
        });

        $selector.children(".jitter").velocity({
            rotateZ: "360",
            left: "0",
            top: "200"
        }, {
            duration: 700,
            easing: "spring"
        });

        $selector.children(".delay").velocity({
            rotateZ: "360",
            left: "350",
            top: "0"
        }, {
            duration: 700,
            easing: "spring"
        });
    }

    this.testFlyOut = function($selector) {
        $selector.children(".upload").velocity("reverse", {
            easing: "easeout"
        });
        $selector.children(".download").velocity("reverse", {
            easing: "easeout"
        });
        $selector.children(".jitter").velocity("reverse", {
            easing: "easeout"
        });
        $selector.children(".delay").velocity("reverse", {
            easing: "easeout"
        });
    }

    this.resetResults = function() {
        //iterate and reset each result
        $(".results").html("");
        $(".meter").css({
            width: "0%"
        });
    }

    this.showMessage = function(message, alert_classname, size) {
        $("#message-backdrop").show();
        //setup the template
        $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
        $("#message i").removeClass().addClass("fa " + alert_classname);
        $("#message .body").html(message);
        $("#message-wrapper").velocity({
            top: "50%"
        }, 700);
    }

    this.hideMessage = function() {

        //hide the message-backdrop and message from the viewer and also reset the phase to 0
        $("#message-wrapper").velocity("reverse", {
            easing: "easeout"
        });
        $("#message-backdrop").hide();
        $("#message-wrapper").attr("style", ""); //clear the attribute (velocity adds the styles to the attribute maybe for optimization?)
    }
  
    this.clearLocationMessageBox = function(){
        $('#header1').nextAll().detach();
        
        ViewerMsg.$okButton.appendTo("#message .footer");
        $("#message > .header").css("height", "50px");
    }
    
    this.loadInput = function (address){
        document.getElementById('current-location').value = address;
    }
    
    this.showTerms = function(){
        var termsValue = app.getTermsValue();    
        
        if(termsValue == "0")//they have not agreed yet
        {
            //Navigator.setPage(3);
            ViewerMsg.showMessage(termsMessage,'','tiny');//show the agree to terms window
        }
    }
    
    this.insertTermsValue = function(){//will run on the agree button onclick
        app.insertTermsValue();
        this.clearLocationMessageBox();
        ViewerMsg.$termsDisagreeButton.detach();
        ViewerMsg.$termsAgreeButton.detach();
    }
    
    var clicked = false;
    this.toggleTestingInProgress = function(value){
        var page;
        if ($("#phase-0-test").hasClass("active")) {
            page = "page-y";
        } else {
            page = "page-final";
        }
        if (!clicked) {
            clicked = true;
            if (value === "true") {
                if (document.getElementById(page).innerHTML == "Connecting...") {
                    $('#' + page).show(1500, function(){
                        app.startTest();
                    });
                } else {        
                    $('#' + page).hide();
                    document.getElementById(page).innerHTML = "Connecting...";
                    $('#' + page).show(1500, function(){
                        app.startTest();
                    });
                }
            }
        } else if (value === "false") {
            $('#' + page).show();
            function hideText(callback){
                callback();
                document.getElementById(page).innerHTML = "No network connection or ping allowed. Contact your network administrator.";   
            }
            hideText(function(){
                $('#' + page).show();
            });
            //$('#h1h1').hide(2000);
            clicked = false;
            Navigator.setTestLock(false);
        }
    };
    
    this.hideConnection = function() {
        var page;
        page = "page-y";
        $('#' + page).hide();
        page = "page-final";
        $('#' + page).hide();   
    };
    
    this.clearFeedback = function(){
        $("#message .header").html(""); //clear html of header
        $("#message .body").html("");
        Feedback.$okButton.appendTo($("#message .footer")); //append ok button to footer
        //ViewerMsg.$okButton.show();
        
        //$("#message > .header").css("height", "50px");
    }
        
    this.clearHistoryMessage = function()
    {
        $("#message .body").html("");
        History.$okButton.appendTo($("#message .footer"));
    }
    
    this.resetClick = function() {
        clicked = false;
    };
};

//BECAUSE I'M LAZY!
function animateHeartbeat() {
    if ($(".jitter").hasClass("active")) {
        $(".heartbeat").css({
            strokeDashoffset: 2037.4080810546875
        });
        $(".heartbeat").velocity({
            strokeDashoffset: 0
        }, {
            complete: animateHeartbeat,
            duration: 1500,
            easing: "linear"
        });
    } else {
        $(".heartbeat").css({
            strokeDashoffset: 0
        });
    }
}

function animateDelayLoader() {
    if ($(".delay").hasClass("active")) {
        $(".delay-loader > div").velocity({
            left: "100%"
        }, {
            duration: 1000,
            easing: "linear",
            complete: reverseDelayLoader
        });
    }
}

function reverseDelayLoader() {
    $(".delay-loader > div").velocity("reverse", {
        complete: animateDelayLoader
    });
}
