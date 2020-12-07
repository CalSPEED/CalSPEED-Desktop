;
/*
 *
 *  NAVIGATOR: strictly responsible for navigation of the application. Should be able to handle going between sections where applicable
 *             as well as navigating through pages...
 *
 */
var Navigator = function() {
    //PRIVATE
	var previous_page;
	var current_page;
	var page_count;
	var $pages;
        var test_lock;

    var calculateScrollTop = function() {
        return $pages.children(".page-" + current_page).position().top - $pages.children(".page-" + previous_page).position().top;
    };

    var calculateScrollLeft = function(_$current_phase, _$previous_phase) {
        return _$current_phase.position().left - _$previous_phase.position().left;
    }

    //PUBLIC
	return {
	    init : function() {
            current_page = 0;
            previous_page = 0;
            $pages = $("#pages");
            page_count = $pages.children().length;
            test_lock = false;

            //let's set it up so that we can have as many pages as we want and register listeners based on pages and sections...
            $.each($pages.children(), function(index) {
                //looks like we should be listening on this page for changes
                if($(this).find(".phases-wrapper").length > 0) {
                    var current_phase = $(this).find(".phases-wrapper").find(".active");
                    var phases = $(this).find(".phases");
                    phasesCount = phases.find("[class^=phase-]").length;
                    phases.children("[class^=phase-]").css("width", 100 / phasesCount + "%");
                    phases.width(phasesCount * 100 + "%");
                }
            });

            //setup a listener to make sure the pages doesn't act strange because of scrollTop or scollLeft
            $(window).resize(function() {
                $pages.scrollTop( $pages.scrollTop() +  $pages.children(".page-" + current_page).position().top);

                $.each($pages.children(), function() {
                     var phases = $(this).find(".phases");
                     if(phases !== undefined) {
                         var current_phase = phases.children(".active").first();
                         if(current_phase.position() !== undefined) {
                            phases.parent().scrollLeft(phases.scrollLeft() + current_phase.position().left);
                         }
                     }
                });
            });
	    },

        /**
         * setPage: sets the page that should be displayed
         */
        setPage : function(page) {
            if (!test_lock) {
                if(page != current_page) {
                    var animation_speed = 700;
                    previous_page = current_page;
                    current_page = page;
                    $("#menu").find(".item-" + previous_page).toggleClass("inactive").toggleClass("active");
                    $("#menu").find(".item-" + current_page).toggleClass("inactive").toggleClass("active");

                    $pages.velocity("scroll", { offset: calculateScrollTop(), container: $pages });
                }
            }
        },

        /**
         * setSection: set the section that a page should be on
         */
        setSection : function(section) {
            if (!test_lock) {
                var $current_page = $pages.find(".page-" + current_page);
                var sections = $current_page.find(".phases [class^=phase-]");
                var previous_section;
                var current_section;
                //we know this page has sections in it
                if(sections.length > 0) {
                    //now let's iterate through them and when the index == current_page we can set that one as active and the other as inactive
                    sections.each(function(index) {
                        if($(this).hasClass("active") && index != section) {
                            previous_section = $(this);
                            //let's assign it as active
                            $(this).toggleClass("inactive").toggleClass("active");
                        } else if(section == index && !$(this).hasClass("active")) {
                            current_section = $(this);
                            $(this).toggleClass("inactive").toggleClass("active");
                        }
                    });

                    if(previous_section !== undefined && current_section !== undefined) {
                        var scrollTopOffset = calculateScrollLeft(current_section, previous_section);
                        var scrollContainer = $pages.find(".page-" + current_page + " .phases-wrapper");
                        scrollContainer.velocity( "scroll", { axis: "x", offset: scrollTopOffset, container: scrollContainer });
                    }
                }
            }
        },
        
        setTestLock : function(state) {
            test_lock = state;
        },
    };
}();
