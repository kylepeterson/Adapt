// Configure Express application.
express = require('express');
app = express();
app.set('views', 'cloud/views');
app.set('view engine', 'ejs');
app.use(express.bodyParser());

var Hypothesis = Parse.Object.extend('Hypothesis');
var Question = Parse.Object.extend('Question');
var Answer = Parse.Object.extend('Answer');
var User = Parse.Object.extend('User');

// Returns a new function that is just like 'fn' except that the first
// 'var_args'.length arguments are fixed to the values in 'var_args'.
// Straight ripped off from Google Closure (see goog.bind docs).
function partial(fn, var_args) {
   var args = Array.prototype.slice.call(arguments, 1);
   return function() {
      var newArgs = args.slice();
      newArgs.push.apply(newArgs, arguments);
      return fn.apply(this, newArgs);
   };
};

// Returns a dictionary containing on the necessary fields from answer (question
// id, answer content value, submitted timestamp).
function serial(answer) {
   return {
      question: answer.get('question').id,
      value: answer.get('answerContent'),
      submitted: answer.get('submittedAt'),
      user: answer.get('user').id
   };
}

// Returns a object constructed by given 'constructor' prepopulated with given id.
function queryDummy(constructor, id) {
   var res = new constructor();
   res.id = id;
   return res;
}

// Renders the chart html template with the given list of 'answers' for data and
// given 'labels' map (from id to human-readable name).
function report(response, user, labels, answers) {
   var individual = answers.filter(function (a) {
      return user && a.get('user').id == user;
   });
   return response.render('chart', {
      individual: JSON.stringify(individual.map(serial)),
      all: JSON.stringify(answers.map(serial)),
      labels: JSON.stringify(labels)
   });
}

// Returns a query find Promise matching all Answers by the given 'user' (id) to
// any question in 'questions' (Parse.object list). Populates 'labels' with an
// id->human-readable name mapping for every question.
function retrieveAnswers(labels, questions) {
   // Populate name map.
   questions.forEach(function(question) {
      labels[question.id] = question.get('questionText');
   });

   // Generate answer list query.
   var query = new Parse.Query(Answer);
   query.containedIn('question', questions);
   return query.find();
}

// Returns a query find Promise matching all Questions for the given 'hypothesis'
// (id).
function retrieveQuestions(hypothesis) {
   var query = new Parse.Query(Question);
   query.equalTo('timeToAsk', 1)
   query.equalTo('hypothesis', queryDummy(Hypothesis, hypothesis));
   return query.find();
}

// Renders the chart page with data for given 'user' and 'hypothesis' id GET
// parameters. If the provided IDs do not exist, no matches will occur and []
// will be returned. If the IDs are not provided at all, responds with a 400.
app.get('/chart', function(req, response) {
   var user = req.param('user');
   var hypothesis = req.param('hypothesis');
   if (user == undefined || hypothesis == undefined) {
      response.status(400).send({
         error: 'user and hypothesis GET params required.'
      });
      return;
   }
   var error = function() {
      res.send('Parse query error. My bad...');
   };
   var labels = {};
   retrieveQuestions(hypothesis)
      .then(partial(retrieveAnswers, labels), error)
      .then(partial(report, response, user, labels), error);
});

app.listen();
