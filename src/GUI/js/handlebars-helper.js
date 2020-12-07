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