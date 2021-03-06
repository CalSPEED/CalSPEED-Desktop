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

var Location = new function() {

    this.location;
    this.address;
    this.formatted_address;
    this.lat;
    this.long;
    this.formatted_latlong;
    this.latlong;
    this.zip = "N/A";
    this.city = "N/A";
    this.type;

    this.initializeLocation = function() {
        var input = /** @type {HTMLInputElement} */(
              document.getElementById('location-input'));
        var searchBox = new google.maps.places.SearchBox(
            /** @type {HTMLInputElement} */(input), {"types": ["regions"]});

          // Listen for the event fired when the user selects an item from the
          // pick list. Retrieve the matching places for that item.
          google.maps.event.addListener(searchBox, 'places_changed', function() {
            var potentialPlaces = searchBox.getPlaces();

            if (potentialPlaces.length < 1) {
              Tester.showMessage("<h1 class='text-center'>" + MESSAGES.SEARCH_ERROR + "</h1>", "fa-exclamation-triangle", "small");
            }

            //iterates through all of the places that were returned and makes sure that it is in california. if none exist in california than an error message should popup...
            Viewer.setPlaces(potentialPlaces, function(results) {
                if(results !== false) {                         
                    places.push(results[0]);
                    Location.getAdressFromResults(0);
                } else {
                    // Show an error message the place doesn't exist.
                    ViewerMsg.showMessage("<h1 class='text-center'>" + MESSAGES.SEARCH_ERROR + "</h1>", "fa-exclamation-triangle", "small");
                }
            });
          });
    };
 
    this.locationInfo = function() {
        address = lat = long = city = zip_code = carrier = "N/A";
        type = "0";
        address = document.getElementById('location-input').value;
        address = address.replace(/, United States\s*$/, "");
        existing_address = document.getElementById('current-location').value;
        carrier = document.getElementById('update-isp').value;
        ViewerMsg.$locationInput.val(address);

        //address = address.replace(", United States", "");
        //var latlong = document.getElementById('latlong-input').value;
        
        if (carrier === "") {
            carrier = document.getElementById('current-isp').value;
        }
        if (existing_address !== "") {
            if (address === "") {
                address = existing_address;
            }
            var url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + address 
                    + "&components=administrative_area:CA|country:US&sensor=false";
            $.getJSON(url, function(data) {
                app.log(url);
                app.log(data.status + " | " + JSON.stringify(data.results[0]));
                if (data.status === "OK") {
                    formatted_address = data.results[0].formatted_address;
                    app.log("formatted address: " + formatted_address);
                    //formatted_latlong = data.results[0].geometry.location.lat + ", " + data.results[0].geometry.location.lng; 
                    var cityFound = false;

                    for (var i = 0; i < data.results[0].address_components.length; i++) {
                        app.log(JSON.stringify(data.results[0].address_components[i]));
                        if ((data.results[0].address_components[i].types[0] === "sublocality_level_1" ||
                                data.results[0].address_components[i].types[0] === "locality") && !cityFound) {
                            //this is the object you are looking for
                            city = data.results[0].address_components[i].long_name;
                            app.log("long city name: " + city)
                            cityFound = true;
                        }
                        if (data.results[0].address_components[i].types[0] === "postal_code") {
                            zip_code = data.results[0].address_components[i].long_name;
                        }
                    }
                    document.getElementById("location-input").readOnly = false;

                    if (lat === "N/A" && long === "N/A") {
                        lat = data.results[0].geometry.location.lat;
                        long = data.results[0].geometry.location.lng;
                        type = "0";
                    }
                    else if (address === ""){
                        address = formatted_address.replace(/, USA\s*$/, "");
                        type = "1";
                        ViewerMsg.setGeneralLocation(city, zip_code);
                    }
                    Location.updateLocationInfo(address, lat, long, city, zip_code, carrier);
                } else {
                    Tester.hideMessage();
                    Tester.clearLocationMessageBox();
                    ViewerMsg.showMessage(errorMessage, '', 'tiny');
                }
            });
        } else {
            address = document.getElementById('current-location').value;
            Location.updateLocationInfo(address, lat, long, city, zip_code, carrier);
        } 
    };
    
    this.updateLocationInfo = function(address, lat, long, city, zip_code, carrier) {
        app.printLocationData(address, lat, long, city, zip_code, carrier);
        app.insertLocationData(address, lat, long, city, zip_code, type, carrier);
        app.updateSettings();
        //app.printLocationData(address, lat, long, city, zip);
        Tester.hideMessage();
        Tester.clearLocationMessageBox();
        ViewerMsg.showMessage(successMessage, '', 'tiny');
    };

    this.addressInput = function() {
        if (document.getElementById('location-input').value !== "") {
            document.getElementById("lat-input").readOnly = true;
            document.getElementById("long-input").readOnly = true;
        } else {
            document.getElementById("lat-input").readOnly = false;
            document.getElementById("long-input").readOnly = false;
        }
    };
    
    this.latlongInput = function() {

        if (document.getElementById('long-input').value !== "" ||
            document.getElementById('lat-input').value !== "") {
            document.getElementById("location-input").readOnly = true;
        } else {
            document.getElementById("location-input").readOnly = false;
        }
    };
    
    this.loadLocationInput = function (address){
        document.getElementById('current-location').value = address;
        $("location-input").attr("placeholder", "e.g. " + addressPlaceholder);
    };
    
    this.loadSettingsInput = function (currentIsp, updateIspPlaceholder) {
        document.getElementById('current-isp').value = currentIsp;
        if (currentIsp === updateIspPlaceholder) {
            updateIspPlaceholder = ispPlaceholder;
        }
        updateIspPlaceholder = "e.g. " + updateIspPlaceholder;
        $('#update-isp').attr("placeholder", updateIspPlaceholder);
    };
    
    this.getAdressFromResults = function(placeIndex) {
        currentPlace = Viewer.getPlace(placeIndex);
        ViewerMsg.$locationInput.val(currentPlace.formatted_address);
        ViewerMsg.showMessage(successMessage, '', 'tiny');
    };

    this.getAddress = function() {
        return address;
    };

    this.getLat = function() {
        return lat;
    };

    this.getLong = function() {
        return long;
    };

};

google.maps.event.addDomListener(window, 'load', Location.initializeLocation);
