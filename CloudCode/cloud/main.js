require('cloud/app.js');

Parse.initialize("clUhGCWWLq3hJTAF80lNZuzCuB6FLnnRy0eN2W0d", "bDHXDi080LHjrVmCSpXKM8TUjyZPhClzobXoUiXG");

// Default Answer submit timestamp to the Parse.Object creation time.
Parse.Cloud.beforeSave("Answer", function(request, response) {
  if (!request.object.get("submittedAt")) {
    request.object.set("submittedAt", request.object.get("createdAt"));
  }
  response.success();
});

// Use Parse.Cloud.define to define as many butt functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});

Parse.Cloud.job("aggregateRatings", function(request, status) {
  // find all ratings
  status.message("Getting all ratings...");

  var counts = {};
  var processedCount = 0;

  var HypothesisRatingObject = Parse.Object.extend("HypothesisRating");
  var query = new Parse.Query(HypothesisRatingObject);

  query.each(function(rating) {
    var id = rating.get("hypothesis").id;
    var rating = parseInt(rating.get("rating"));

    if (!counts.hasOwnProperty(id)) {
      counts[id] = new Array();
    }
    counts[id].push(rating);

    processedCount++;
    if (processedCount % 10 === 0) {
      status.message("Processed " + processedCount + " ratings.");
    }
    //return;
  }).then(function() {
    status.message("Saving counts...");

    var HypothesisObject = Parse.Object.extend("Hypothesis");
    var hypo = new Parse.Query(HypothesisObject);

    hypo.each(function(hypothesis) {
      var id = hypothesis.id;

      var finalRating = 4.2;
      if (counts.hasOwnProperty(id)) {
        var ratings = counts[id];

        var total = 0;
        for (var rating in ratings) {
          total += parseInt(rating);
        }

        finalRating = total / ratings.length;
      }

      hypothesis.set("rating", finalRating);
      return hypothesis.save();
    }).then(function() {
      status.success("rating aggregation successful");
    }, function(error) {
      status.error("ERROR IN HYPOTHESIS EACH: " + error);
    });
  }, function(error) {
    status.error("ERROR IN RATING EACH: " + error);
  });
});

Parse.Cloud.job("createSearchString", function(request, status) {
  // find all users
  status.message("Getting all Hypotheses...");

  var HypothesisObject = Parse.Object.extend("Hypothesis");
  var hypoQuery = new Parse.Query(HypothesisObject);

  hypoQuery.each(function(hypothesis) {
    var descriptionText = hypothesis.get("description").toLowerCase();
    var ifText = hypothesis.get("ifDescription").toLowerCase();
    var thenText = hypothesis.get("thenDescription").toLowerCase();

    var searchText = descriptionText + " " + ifText + " " + thenText;

    var words = searchText.split(/[ \t,.?]+/);

    var stopWords = ["the", "in", "and", "a", "an"];

    var isNotStopWord = function(term) {
      if (!term) {
        return false;
      }
      for (var stopWord in stopWords) {
        if (term.localeCompare(stopWord) == 0) {
          return false;
        }
      }
      return true;
    }

    words = words.filter(isNotStopWord);

    hypothesis.set("searchTerms", words);
    return hypothesis.save();
  }).then(function() {
    // Set the job's success status
    status.success("Migration to lower case completed successfully.");
  }, function(error) {
    // Set the job's error status
    status.error("Uh oh, something went wrong.");
  });
});