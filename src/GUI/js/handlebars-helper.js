Handlebars.registerHelper("viewerResult", function(value) {
    name = " < RESULT < ";
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

Handlebars.registerHelper("mosGrader", function(mos) {
    mos = parseFloat(mos).toFixed(2);
    switch (true) {
        case (mos > 4.37):
            grade = "Excellent";
            break;
        case (mos > 4.27 && mos <= 4.37):
            grade = "Very Good";
            break;
        case (mos > 3.99 && mos <= 4.27):
            grade = "Acceptable";
            break;
        case (mos >= 2.5 && mos <= 3.99):
            grade = "Concerning";
            break;
        case (mos < 2.5):
            grade = "Very Poor";
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
        if(this.MUP) {
            return (this.MUP / max * 100) - 9.09090909091;
        } else if(this.MAXADUP) {
            return (this.MAXADUP / max * 100) - 9.09090909091;
        }
    } else {
        if(this.MDOWN) {
            return (this.MDOWN / max * 100) - 9.09090909091;
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