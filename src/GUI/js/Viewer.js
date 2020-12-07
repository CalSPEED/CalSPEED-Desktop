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

;
/*
 *  VIEWER: a javascript object that is meant to handle all of the interaction for the CalSPEED viewer (minus navigation)
 */
var Viewer = function() {
    //PRIVATE
    var marker;
    var previousMarker;
    var map;
    var places = [];
    var currentPlace;
    var infoWindow;
    var MESSAGES = {
            SEARCH_ERROR: "Search returned no results within California",
            OUT_OF_BOUNDS: "The location is outside of California."
    };

    //PUBLIC
    return {
        /**
         * initialize: meant to setup the google map and any listeners needed to be set in place
         */
        initialize : function() {
           var mapOptions = {
               componentRestrictions: {country:"US"},
               zoom: 7,
               center: new google.maps.LatLng(36.653001, -121.800250), //doesn't get used currently
               mapTypeControl: false,
               panControl: true,
               panControlOptions: {
                   position: google.maps.ControlPosition.TOP_RIGHT
               },
               zoomControl: true,
               zoomControlOptions: {
                   position: google.maps.ControlPosition.TOP_RIGHT
               },
               scaleControl: false,
               streetViewControl: false
           }

          map = new google.maps.Map(document.getElementById('google-map'), mapOptions);

          //get the map to show all of california because we are definitely biased
//          var defaultBounds = new google.maps.LatLngBounds(
//              new google.maps.LatLng(41.987822, -124.299316),
//              new google.maps.LatLng(32.828539, -114.213867));
//          map.fitBounds(defaultBounds);

          //allows the user to click the map and check a location that way...
          google.maps.event.addListener(map, 'click', function(event) {
                Viewer.checkLocation(event.latLng, function(result) {
                    if(result !== false) {
                        currentPlace = result;
          		        Viewer.setMarker(event.latLng);
                    } else {
                        Tester.showMessage("<br><h1 class='text-center'>" + MESSAGES.OUT_OF_BOUNDS + "</h1>", "fa-exclamation-triangle", '');
                    }
                })
          });

          // Create the search box and link it to the UI element.
          var input = /** @type {HTMLInputElement} */(
              document.getElementById('pac-input'));
          map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

          var searchBox = new google.maps.places.SearchBox(
            /** @type {HTMLInputElement} */(input), {"types": ["regions"]});

          // Listen for the event fired when the user selects an item from the
          // pick list. Retrieve the matching places for that item.
          google.maps.event.addListener(searchBox, 'places_changed', function() {
            var potentialPlaces = searchBox.getPlaces();

            if (potentialPlaces.length < 1) {
              Tester.showMessage("<br><h1 class='text-center'>" + MESSAGES.SEARCH_ERROR + "</h1>", "fa-exclamation-triangle", "");
            }

            //iterates through all of the places that were returned and makes sure that it is in california. if none exist in california than an error message should popup...
            Viewer.setPlaces(potentialPlaces, function(results) {
                if(results !== false) {
                    if(results.length > 1) {
                        //fill the message area with options yippee! (maybe better to make resultTemplater do this...
                        var messageContent  = '<h1 class="text-center">Choose a location</h1>';

                        for(var i = 0, place; place = results[i]; i++) {
                            messageContent += '<div>';
                            messageContent += '<h2><button class="btn btn-primary pull-left" ';
                            messageContent += 'onClick="Viewer.setMarkerFromPlaceIndex(' + i + ');Tester.hideMessage();">';
                            messageContent += place.formatted_address;
                            messageContent += '&nbsp;<i class="fa fa-map-marker"></i>';
                            messageContent += "</button></h2>";
                            messageContent += "</div><br>";
                        }
                        $('#message .footer button').detach();
                        Tester.showMessage(messageContent, "fa-globe", "medium");
                    } else {
                        //let's just place the marker
                        places.push(results[0]);
                        Viewer.setMarkerFromPlaceIndex(0);
                    }

                } else {
                    //tell the user that they suck at searching...
                    Tester.showMessage("<br><h1 class='text-center'>" + MESSAGES.SEARCH_ERROR + "</h1>", "fa-exclamation-triangle", "");
                }
            });
          });

        }, //end of initialize function

        /**
         * setMarker: meant to initialize the marker if it hasn't already been set and to move it if it already has.
         */
        setMarker : function(latLng) {
            if(previousMarker) {
                marker.setPosition(latLng);
                previousMarker.setMap(null);
            }
            marker = new google.maps.Marker({
                map: map,
                position: latLng,
                draggable: true,
            });
            previousMarker = marker;
           
            var windowContent = $("<button class='btn btn-primary' onClick='Viewer.goToResult();'>" + currentPlace.formatted_address + " <i class='fa fa-chevron-right'></i></button>")[0];

            if(infoWindow) {
                infoWindow.setContent(windowContent);
            } else {
               infoWindow = new google.maps.InfoWindow({
                  content: windowContent
               });
            }

             //auto open
             infoWindow.open(map, marker);

             google.maps.event.addListener(marker, "click", function() {
                infoWindow.open(map, this);
             });
             google.maps.event.addListener(marker, 'drag', function() {
                 setMarkerFromPlaceIndex(0);
                 infoWindow.open(map, this);
             });


             map.panTo(latLng);
             
        },

        goToResult: function() {
            if($(".viewer-address").text() != currentPlace.formatted_address) {
                 result = JSON.stringify({"address": currentPlace.formatted_address});
                 Templater.loadTemplate("#viewer-results", "viewer", result);
                 app.getViewerResults(currentPlace.geometry.location.lat(), currentPlace.geometry.location.lng());
            }
            //                    console.log('clicked');
            var resultTabs = new SimpleTabs(document.getElementById('resultTabs'));
            Navigator.setSection(1); //move on to the section...
        },

        /**
         *
         */
        setMarkerFromPlaceIndex: function(placeIndex) {
            currentPlace = Viewer.getPlace(placeIndex);
            if(currentPlace !== false) {
                Viewer.setMarker(currentPlace.geometry.location);
            }
        },

        /*
         * getPlace: returns the place from the placesArray by index (else returns false)
         */
        getPlace : function(placeIndex) {
            if(places[placeIndex]) {
                return places[placeIndex];
            }

            return false;
        },

        /**
         * setPlaces: a way to handle the asynchronous nature of the google map requests so we can ultimate only
         *            set the places array to be places within california.
         */
        setPlaces : function(/* places, callback, results, index */) {

            //argument handling
            var originalPlaces = arguments[0];
            var callback = arguments[1];
            if(arguments.length === 2) {
                places = []; //initialize the results to be an empty array
                var index = 0; //because the first time doesn't set this we need to set it here...
            } else {
                places = arguments[2];
                var index = arguments[3];
            } //end of argument handling

            if(index < originalPlaces.length) {
                Viewer.checkLocation(originalPlaces[index].geometry.location, function(result) {
                    if(result !== false) {
                        places.push(originalPlaces[index]); //the place is valid because it's within California
                    }
                    Viewer.setPlaces(originalPlaces, callback, places, index + 1); //let's go again
                });
            } else { //end of the line bub
                if(places.length > 0) {
                    //only should show results that were from california
                    callback(places);
                } else {
                    callback(false); //don't even try to continue...
                }
            }
        },

        /**
         * checkLocation: takes a location and sets the callback to true or false whether or not the location information
         *                is within California or not
         */
        checkLocation : function(latLng, callback) {
            geocoder = new google.maps.Geocoder();
            geocoder.geocode({'latLng': latLng}, function(results, status) {
                  if (status == google.maps.GeocoderStatus.OK)
                  {
                      if(results[1]) //if there are actually results returned
                      {
                          //forces me to iterate through every result
                          for(var i = 0, result; result = results[i]; i++)
                          {
                              //then iterate through every component to see if i can pull the state
                              for(var j = 0, component; component = result.address_components[j]; j++)
                              {
                                  if(component.types[0] === "administrative_area_level_1")
                                  {
                                      //now i can finally check if it is California or not...
                                      if(component.long_name == "California")
                                      {
                                          callback(result);
                                          return; //so we can go to the next result
                                      }
                                  }
                              }
                          }
                      }
                  }
                  callback(false);
            });
        }
    };
}();

google.maps.event.addDomListener(window, 'load', Viewer.initialize);