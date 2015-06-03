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

Parse.Cloud.job("countUsersJoined", function(request, status) {
  // find all users
  status.message("Getting all users...");

  var counts = {};

  var userCount = 0;

  var query = new Parse.Query(Parse.User);
  query.each(function(user) {
    var joined = user.get("joined");
    if (joined) { // user joined some hypotheses
      for (var i = 0; i < joined.length; i++) {
        var ID = joined[i];
        if (!counts.hasOwnProperty(ID)) {
          counts[ID] = 0;
        }
        counts[ID] = counts[ID] + 1;
      }
    }
    userCount++;
    if (userCount % 10 === 0) {
      status.message("Processed " + userCount + " users.");
    }
  }).then(function() {
    status.message("Saving counts...");

    var HypothesisObject = Parse.Object.extend("Hypothesis");
    var hypoQuery = new Parse.Query(HypothesisObject);

    hypoQuery.each(function(hypothesis) {
      var hypothesisID = hypothesis.id;
      var userCount = 0;
      if (hypothesisID in counts) {
        userCount = counts[hypothesisID];
      }
      hypothesis.set("usersJoined", userCount);
      hypothesis.save();
    }).then(function() {
        status.success("All hypotheses saved.");
      }, function(error) {
        status.error("ERROR IN HYPOTHESIS EACH: " + error);
      });
  }, function(error) {
    status.error("ERROR IN USER EACH: " + error);
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

    var words = searchText.split(/[ \t,.]+/);

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