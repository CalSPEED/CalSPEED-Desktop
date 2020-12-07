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


/* global app, Templater */

var History = new function() {
    this.show = true;
    this.firstRun = true;
    this.results;
    //Add more variables to display detailed test later
    this.db;
    this.$messageBody;
    this.$doneButton;
    this.$okButton;
    this.$detailedHistory;
    
    this.showMessage = function(index, alert_classname, size) {     
        if (this.firstRun) {
            this.db = app.getDatabase();
            this.$messageBody = $("#message .body");
            this.$doneButton = $('#done-button');
            this.$okButton = $('#message .footer button');
            this.$detailedHistory = $('#detailed-history');
        }
        this.firstRun = false;
        $("#message-backdrop").show();
            //setup the template
        $("#message-wrapper").removeClass("large medium small tiny").addClass(size);
        $("#message i").removeClass().addClass("fa " + alert_classname);
        $("#message-wrapper").velocity({top: "50%"}, 400);
        $("#message > .header").css("height", "20px");
        
        this.$detailedHistory.html("");
        results = this.db.getDataById(index);
        Templater.loadTemplate("#detailed-history","details", results);
        //get database entry by index
        this.$messageBody.html(this.$detailedHistory);
        this.$doneButton.appendTo(this.$messageBody);
        this.$okButton.hide();
        
        this.$detailedHistory.show();
        this.$doneButton.show();
    };
    
    this.getDetailResults = function(id) {
        this.showMessage(id, "", "medium");
    };
    
   
};