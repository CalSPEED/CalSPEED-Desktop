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

/* global Handlebars */

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
            $(sourceSelector).html(finalResults).trigger("contentChange");
        },

        prependTemplate : function(sourceSelector, template, results) {
            source = $("#" + template + "-template").html();
            template = Handlebars.compile(source);
            finalResults = template( formatResult(results) );
            $(sourceSelector).prepend(finalResults).trigger("contentChange");
        },

        appendTemplate : function(sourceSelector, template, results) {
            source = $("#" + template + "-template").html();
            template = Handlebars.compile(source);
            finalResults = template( formatResult(results) );
            $(sourceSelector).append(finalResults).trigger("contentChange");
        }
    };
}();