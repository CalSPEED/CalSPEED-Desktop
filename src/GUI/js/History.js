/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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