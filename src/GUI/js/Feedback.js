/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/* global app, blankSurveyMessage, feedbackMessage, Tester, Navigator, Database, numberOnlyMessage, onlyOneInputMessage */

var Feedback = new function () {
    this.show = true;
    this.firstRun = true;

    //Add more variables to display detailed test later
    this.$feedback1;
    this.$feedback2;
    this.$feedback3;
    this.$feedback4;
    this.$doneButton;
    this.$closeButton;
    this.$okButton;
    this.$isSatisfied;
    this.$retryButton;
    this.$lastUploadResult;
    this.$lastDownloadResult;
    this.$lastUploadResultElement = document.getElementById('upload-speed-result');;
    this.$lastDownloadResultElement = document.getElementById('download-speed-result');
    var empty = false;
    var doNotAsk = false;
    var showSurvey = true;
    
    this.showMessage = function (message, alert_classname, size) {
        if (message === feedbackMessage) {
            if (this.firstRun) {
                this.$okButton = $('#okButton');
                this.$closeButton = $('#close-feedback');
                this.$feedback1 = $('#feedback-1');
                this.$feedback2 = $('#feedback-2');
                this.$feedback3 = $('#feedback-3');
                this.$feedback4 = $('#feedback-4');    
                this.$retryButton = $("#retry-button");
            }
            Feedback.showLastResults();

            $("#message-backdrop").show();
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({
                top: "50%"
            }, 700);
            $("#message > .header").css("height", "0px");
            $("#message > .footer").css("height", "10px");
            Feedback.$okButton = this.$okButton.detach();
        //        $("#message .header").html();
        //        this.$closeButton.appendTo("#message .header");

            this.setFormPage(1);
            //$("#message .footer").appendTo();
            this.firstRun = false;

        } else if (message === blankSurveyMessage
                || message === numberOnlyMessage
                || message === onlyOneInputMessage) {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 500);
            $("#message > .header").css("height", "20px");
            this.$okButton.detach();
            this.$retryButton.appendTo("#message .body");
            this.$retryButton.show();
        }   
    };
    
    this.showLastResults = function() {
        if ((this.$lastUploadResultElement === null 
                    && this.$lastDownloadResultElement === null)
                || (this.$lastUploadResultElement === undefined 
                    && this.$lastDownloadResultElement === undefined)) {
            this.$lastUploadResultElement = document.getElementById('upload-speed-result');
            this.$lastDownloadResultElement = document.getElementById('download-speed-result');
        }
        this.$lastUploadResultElement.value = this.$lastUploadResult;
        this.$lastDownloadResultElement.value = this.$lastDownloadResult;
    };
    
    //go through the 4 pages of customer feedback
    this.setFormPage = function(page) {
//        $("#close-feedback").show();
        $("#message .body").html('');
        this.$feedback1.appendTo("#message .body");
        this.$feedback1.show();
    };
    
    this.loadLastResult = function (uploadResult, downloadResult) {
        this.$lastUploadResult = uploadResult;
        this.$lastDownloadResult = downloadResult;
    };
    
    this.setShowSurvey = function(survey) {
        this.showSurvey = survey;
    };
    
    this.shouldShowSurvey = function() {
        if (this.showSurvey === true) {
            Navigator.setPage(4);
            Feedback.showMessage(feedbackMessage, '', 'medium');
        }
    };
    
    this.handleDoNotAsk = function() {
        var checkbox = document.getElementsByName("doNotAsk");
        if (checkbox.length === 1) {
            if (checkbox[0].checked) {
                this.doNotAsk = true;
            }
        }
        if (this.doNotAsk) {
            this.showSurvey = false;
            app.disableSurvey();
        }
    };
    
    this.handleSkip = function() {
        Feedback.handleDoNotAsk();
        $('#do-not-ask').prop("checked", false);
        app.skippedSurvey();
        Navigator.setPage(0);
        Feedback.resetPage();
    };
    
    this.saveResponse = function() {
        var incompleteInput = false;
        if (($("#upload-speed-input").val() === ""
                && $("#download-speed-input").val() !== "")
            || (($("#upload-speed-input").val() !== ""
                && $("#download-speed-input").val() === ""))) {
            incompleteInput = true;
        }
        var uploadSpeed = $("#upload-speed-input").val() || "0.0";
        var downloadSpeed = $("#download-speed-input").val() || "0.0";
        var invalidInput = isNaN(uploadSpeed) || isNaN(downloadSpeed);
        var additionalComments = $("#additional-comments").val() || "";
        this.getSatisfaction();
        if (this.empty) {
            Tester.hideMessage();
            Feedback.showMessage(blankSurveyMessage, '', 'tiny');
        } else if (invalidInput) {
            Tester.hideMessage();
            Feedback.showMessage(numberOnlyMessage, '', 'tiny');
        } else if (incompleteInput) {
            Tester.hideMessage();
            Feedback.showMessage(onlyOneInputMessage, '', 'tiny');
        } else {
            Feedback.handleDoNotAsk();
            app.saveSurveyForm(this.$isSatisfied, uploadSpeed, downloadSpeed, additionalComments);
            Tester.hideMessage();
            Navigator.setPage(0);
            Feedback.resetPage();
        }
         $('#do-not-ask').prop("checked", false);
    };
    
    this.getSatisfaction = function() {
        var radioButtons = document.getElementsByName("satisfied");
        for (var i = 0; i < radioButtons.length; i++) {
            if (radioButtons[i].checked 
                    && radioButtons[i].value === "yes") {
                this.$isSatisfied = true;
                this.empty = false;
                break;
            } else if (radioButtons[i].checked 
                    && radioButtons[i].value === "no") {
                this.$isSatisfied = false;
                this.empty = false;
                break;
            } else {
                this.empty = true;
            }
        }
    };
    
    this.resetPage = function() {
        $('input[type=checkbox]').removeAttr('checked');
        $('input[type=radio]').removeAttr('checked');
        $("#upload-speed-input").val("");
        $("#download-speed-input").val("");
        $("#additional-comments").val("");
        $("div .body").scrollTop(0);
    };
};