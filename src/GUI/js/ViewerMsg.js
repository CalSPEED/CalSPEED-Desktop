var ViewerMsg = new function() {
    this.show = true;
    
    this.setMessageDisplay = function(show) {
        this.show = show;
    };
    
    this.showMessage = function(message, alert_classname, size) {
        if (this.show) {
            $("#message-backdrop").show();
            //setup the template
            $("#message-wrapper").removeClass("large small").addClass(size);
            $("#message i").removeClass().addClass("fa " + alert_classname);
            $("#message .body").html(message);
            $("#message-wrapper").velocity({top: "50%"}, 700);
            this.show = false;
        }
    };
};