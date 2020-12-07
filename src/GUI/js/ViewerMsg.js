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

/* global locationMessage, successMessage, locationChangeSuccessMessage, ispChangeSuccessMessage, errorMessage, 
 * SUCESS_TYPE, SUCCESS_TYPE, app, termsMessage, connMessage, blankSurveyMessage, viewerMessage, blankAddressMessage,
 *  blankIspMessagekAddressAndIspMessage, blankAddressAndIspMessage, blankIspMessage, Feedback, google, numberOnlyMessage */

var ViewerMsg = new function () {
    this.viewerShow = true;
    this.initializeLocation = true;
    this._city;
    this._zip;
    this.$cancelButton;
    this.$updateButton;
    this.$locationInput;
    this.$currentIsp;
    this.$updateIsp;
    //this.$latlongInput;
    this.$okButton;
    this.$header1;
    this.$currentLocation;
    this.$latInput;
    this.$longInput;
    this.$div1;
    this.$span1;
    this.$span2;
    this.$span3;
    this.$span4;
    this.$cancelButtonn;
    this.$dismissButton;
    this.$termsAgreeButton;
    this.$termsDisagreeButton;

    function initialize()
    {
        var options = {
            componentRestrictions: {country: "us"}
        };
        var autocomplete = new google.maps.places.Autocomplete(document.getElementById('location-input'), options);
    }
    if (navigator.online)
    {
        google.maps.event.addDomListener(window, 'load', initialize);
    }


    this.setMessageDisplay = function (show) {
        this.viewerShow = show;
    };

    this.repeatedMessageDisplay = function () {
        this.$okButton = $('#message .footer button');
        if (this.viewerShow === true) {
            this.showMessage(viewerMessage, '', '');
            this.viewerShow = false;
        }
    };

    this.setLocationMessageDisplay = function (locationShow) {
        this.locationShow = locationShow;
    };

    this.showMessage = function (message, alert_classname, size)
    {
        this.$okButton = $('#message .footer button');
        if ((message === viewerMessage)) {
            $("#message .body").html("");
            if (this.$okButton.html() === undefined || this.$okButton === null) {
                this.$okButton = Feedback.$okButton;
            }
            this.$okButton.appendTo($("#message .footer"));
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 700);
            $("#message > .header").css("height", "0px");
            $("#message > .footer").css("height", "50px");
            this.$okButton.show();
            this.viewerShow = false;

        } else if (message === locationMessage) {
            if (this.initializeLocation) { //only location variables will be initialized
                this.$cancelButton = $('#cancel-button');
                this.$cancelButtonn = $('#cancel-buttonn');
                this.$updateButton = $('#update-button');
                this.$locationInput = $('#location-input');
                this.$latlongInput = $('#latlong-input');
                this.$okButton = $('#message .footer button');
                this.$header1 = $('#h1');
                this.$currentLocation = $('#current-location');
                this.$currentIsp = $('#current-isp');
                this.$updateIsp = $('#update-isp');
                this.$latInput = $('#lat-input');
                this.$longInput = $('#long-input');
                this.$span1 = $('#d1');
                this.$span2 = $('#d2');
                this.$span3 = $('#d3');
                this.$span4 = $('#d4');
                this.$dismissButton = $('#dismiss-button');
            }

            //jQuery(this).prev("li").attr("message-backdrop", "message-backdrop-location");
            //document.getElementById("button2").style.visibility = "hidden";
            $("#message-backdrop").show();
            //setup the template

            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);

            $("#message .body").html("");
            $("#message-wrapper").velocity({top: "50%"}, 400);

            $("#message > .header").css("height", "0px");
            $("#message > .footer").css("height", "10px");

            $("#location").css("background-color", "#B1B1B0");

            this.$header1.appendTo("#message .body");
            this.$span1.appendTo("#message .body");
            this.$currentIsp.appendTo("#message .body");
            this.$span2.appendTo("#message .body");
            this.$updateIsp.appendTo("#message .body");
            this.$span3.appendTo("#message .body");
            this.$currentLocation.appendTo("#message .body");
            this.$span4.appendTo("#message .body");
            this.$locationInput.appendTo("#message .body");
            this.$updateButton.appendTo("#message .body");
            this.$cancelButtonn.appendTo("#message .body");
            this.$okButton.detach();
            this.initializeLocation = false;
        } else if (message === successMessage) {
            $("#message-backdrop").show();
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message > .header").css("height", "20px");
            var location = this.$locationInput.val();
            var currentLocation = this.$currentLocation.val();
            var currentIsp = this.$currentIsp.val();
            var lat = this.$latInput.val();
            var lng = this.$longInput.val();
            var newIsp = this.$updateIsp.val();
            var existingIsp = this.$currentIsp;
            var existingAddress = this.$currentIsp;
            var ispMissing = false;
            var addressMissing = false;
            if (currentIsp === "N/A" && newIsp === "") {
                ispMissing = true;
            }
            if (location === "" && currentLocation === "N/A") {
                addressMissing = true;
            }

            if (addressMissing && ispMissing) {
                message = blankAddressAndIspMessage;
            } else if (ispMissing) {
                message = blankIspMessage;
            } else if (addressMissing) {
                message = blankAddressMessage;
            }
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 700);

            if (message !== successMessage) {
                this.$cancelButton.appendTo("#message .body");
                this.$cancelButton.show();
            } else {
                if (location !== "") {
                    this.$currentLocation.val(location);
                    this.$locationInput.val("");
                }
                if (newIsp !== "") {
                    this.$currentIsp.val(newIsp);
                    this.$updateIsp.val("");
                }
                this.$latInput.val("");
                this.$longInput.val("");
                this.$locationInput.val("");
                this.$dismissButton.appendTo("#message .body");
                this.$dismissButton.show();
            }
            this.$okButton.detach();
        } else if (message === errorMessage) {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);

            $("#message i").removeClass().addClass("fa " + alert_classname);

            $("#message > .header").css("height", "20px");

            var location = this.$locationInput.val();
            var lat = this.$latInput.val();
            var lng = this.$longInput.val();

            if (location !== "") {
                $("#message .body").html(message + "Your address, " +
                        this.$locationInput.val() + ", shows no results.");
            } else {
                $("#message .body").html(message + "There was a problem processing your changes. Please try again.");
            }

            //$("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 500);

            this.$okButton.detach();
            this.$cancelButton.appendTo("#message .body");
            this.$cancelButton.show();
        } else if (message === blankAddressAndIspMessage
                || message === blankAddressMessage
                || message === blankIspMessage) {
            app.printResult("Show blank message");
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 500);
            $("#message > .header").css("height", "20px");
            this.$okButton.detach();
            this.$cancelButton.appendTo("#message .body");
            this.$cancelButton.show();

        } else if (message === termsMessage) {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);

            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 500);

            $("#message > .header").css("height", "20px");

            this.$okButton = $('#message .footer button');
            this.$okButton.detach();

            this.$termsAgreeButton = $('#agree-button');
            this.$termsDisagreeButton = $('#exit-button');

            this.$termsAgreeButton.appendTo("#message .body");
            this.$termsAgreeButton.show();
            this.$termsDisagreeButton.appendTo("#message .body");
            this.$termsDisagreeButton.show();
        } else if (message === connMessage) {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 500);
            $("#message > .header").css("height", "20px");


            this.$okButton.detach();
            this.$cancelButton.appendTo("#message .body");
            this.$cancelButton.show();
        } else {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);

            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 700);

            this.show = false;

        }
        /**/
    };

    this.setGeneralLocation = function (city, zip) {
        this._city = city;
        this._zip = zip;
    };

};
