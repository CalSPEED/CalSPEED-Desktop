; //requires jquery and handlebars

/*
 * WE SHOULD CONSIDER DOING STUFF TO MAKE THIS MORE OF A GENERAL HANDLER
 */
var Templater = function() {
    //PRIVATE
    var formatResult = function(results) {
        if(results !== undefined) {
             if(results instanceof Array) {
                 results = {"results": results };
             } else { //we know we need to parse the json because it's not an array
                 results = JSON.parse(results);
                 if(results instanceof Array) {
                        results = {"results": results};
                 } else {
                        results = {"results": [results] };
                 }
             }
        }

        return results; //could return undefined but i think that's alright with handlebars...
    };

    //PUBLIC
    return {
        //GENERAL CASE VERSIONS OF THE ABOVE METHODS
        loadTemplate : function(sourceSelector, template, results) {
            source = $("#" + template + "-template").html();
            template = Handlebars.compile(source);
            finalResults = template( formatResult(results) );
            $(sourceSelector).html(finalResults);
        },

        prependTemplate : function(sourceSelector, template, results) {
            source = $("#" + template + "-template").html();
            template = Handlebars.compile(source);
            finalResults = template( formatResult(results) );
            $(sourceSelector).prepend(finalResults);
        },

        appendTemplate : function(sourceSelector, template, results) {
            source = $("#" + template + "-template").html();
            template = Handlebars.compile(source);
            finalResults = template( formatResult(results) );
            $(sourceSelector).append(finalResults);
        }
	}
}();