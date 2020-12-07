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

Handlebars.registerHelper("viewerResult", function(value) {
    name = " < RESULT < ";
    print(value);
    switch(value) {
        case 1:
            result = "RESULT < 200kbs";
        case 2:
            result = "RESULT < 768kbs";
        case 3:
            result = "768kbs" + name + "1.5mbs";
        break;
        case 4:
            result = "1.5mbs" + name + "3mbs";
        break;
        case 5:
            result = "3mbs" + name + "6mbs";
        break;
        case 6:
            result = "6mbs" + name + "10mbs";
        break;
        case 7:
            result = "10mbs" + name + "25mbs";
        break;
        case 8:
            result = "25mbs" + name + "50mbs";
        break;
        case 9:
            result = "50mbs" + name + "100mbs";
        break;
        case 10:
            result = "100mbs" + name + "1gbs";
        break;
        case 11:
            result = "RESULT > 1gbs";
        break;
        default:
            result = "N/A";
        break;
    }

    return result;
});

//helper function to format when there are or aren't results from the database
Handlebars.registerHelper("formatResult", function(result, metric) {
    result = parseFloat(result).toFixed(2);
    if(result > 0.0) {
        return result + " " + metric;
    } else {
        return "N/A";
    }
});

Handlebars.registerHelper("formatVideo", function(videoStream, videoConference, voip) {
   newline = "<br>";
   return  "Video Streaming: " + videoStream + newline + "Video Conference: "
           + videoConference + newline + "VoIP: " + voip;
});

Handlebars.registerHelper("formatAddress", function(address) {
    newline = "<br>";
    splitAddress = address.split("(");
    if (splitAddress.length === 2) {
        street = splitAddress[0];
        longLat = "(" + splitAddress[1];
        return street + newline + longLat;
    } else {
        return address;
    }
});

Handlebars.registerHelper("mosGrader", function(mos) {
    mos = parseFloat(mos).toFixed(2);
    switch (true) {
        case (mos >= 4.0):
            grade = "Satisfactory";
            break;
        case (mos < 4.0 && mos > 0.0):
            grade = "Unsatisfactory";
            break;
        default:
            grade = "N/A";
            break;
    }
    return grade;
});

//kind of a dumb helper but just returns
Handlebars.registerHelper("getPercentage", function(type) {
    max = 11;

    if(type == "upload") {
        if(this.UploadKey) {
            return (this.UploadKey / max * 100) - 9.09090909091;
        } else if(this.MAXADUP) {
            return (this.MAXADUP / max * 100) - 9.09090909091;
        }
    } else {
        if(this.DownloadKey) {
            return (this.DownloadKey / max * 100) - 9.09090909091;
        } else if(this.MAXADDOWN) {
            return (this.MAXADDOWN / max * 100) - 9.09090909091;
        }
    }

    return "0";
});

Handlebars.registerHelper("currentDay", function() {
    var day = (new Date()).getDate();
    if (day < 10) {
        day = "0" + day;
    }
    return day;
});