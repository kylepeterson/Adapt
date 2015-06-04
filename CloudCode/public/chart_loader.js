// Require: util.js, chart_config.js.

ChartLoader = CL = {};

ChartLoader.CANVAS_SELECTOR = 'canvas';
ChartLoader.LEGEND_SELECTOR = '.legend';

window.onload = function() {
   var data = CL.getExternalData();
   var xAxis = CL.xAxisInfo(data.individual, data.aggregate);
   CL.loadChart(data.individual, data.aggregate, xAxis.dates);

   // Set the projection slider to start on the last individual data point,
   // but allow sliding up until the final aggregate data point.
   PS.init(xAxis.dates, xAxis.pivot);
};

ChartLoader.loadChart = function(individualAnswers, aggregateAnswers, dates) {
   if (individualAnswers.length == 0) {
      $('.missing').classList.remove('hide');
      return;
   }
   Colors.resetColorCycle();

   var chart = $(CL.CANVAS_SELECTOR);
   var ctx = chart.getContext('2d');
   var data = CL.chartData(individualAnswers, aggregateAnswers, dates);
   var chart = new Chart(ctx).Line(data, CConfig.options);
   $(CL.LEGEND_SELECTOR).innerHTML = chart.generateLegend();
};

// Returns the complete list of JSON formatted answers loaded with the page.
// Submitted timestamps are converted to milliseconds.
ChartLoader.getExternalData = function() {
   var i = window.INDIVIDUAL.map(Util.partial(Util.convertDate, 'submitted'));
   var a = window.AGGREGATE.map(Util.partial(Util.convertDate, 'submitted'));
   var user = i.length > 0 ? i[0]['user'] : null;

   return function() {
      return {
         individual: i,
         aggregate: CL.adjustSubmittedToBaseline(user, a)
      };
   };
}();

// Returns a Chart.js dataset for the given list of Parse Answer objects
// 'answers' and given complete list of 'xAxis' points. Answer objects are in
// the format given by cloud chart_data service.
// If multiple answers exist to the same question on the same day the mean
// of the answerContent values for that day is plotted.
ChartLoader.toDataset = function(xAxis, answers) {
   if (answers.length == 0)
      return {};
   var answersByDate = Util.groupBy(answers, 'submitted');
   var data = xAxis.map(function(date) {
      var points = answersByDate[date];
      return points ? Util.mean(Util.extract(points, 'value')) : null;
   });

   var questionText = window.LABELS[answers[0]['question']];
   return {
      label: questionText,
      data: data,
      pointColor: 'white'
  }
};

// Converts a list of Parse Answer 'answers' to a list of Chart.js data
// datasets.
ChartLoader.chartData = function(individualAnswers, aggregateAnswers, dates) {
   var aggregateByQuestion = Util.groupBy(aggregateAnswers, 'question')
   individualAnswers = Util.values(Util.groupBy(individualAnswers, 'question'));
   var aggregateDatasets = [];
   var individualDatasets = individualAnswers.map(function(answers) {
      var set = CL.toDataset(dates, answers);
      var color = set.strokeColor = Colors.nextChartColor();

      // Each data set may have a corresponding aggregate line. Attempt to
      // find the aggregate line, then match the color as hackily as possible.
      if (answers.length > 0) {
         var corresponding = aggregateByQuestion[answers[0].question];
         if (corresponding) {
            var correspondingSet = CL.toDataset(dates, corresponding);
            correspondingSet.strokeColor = Colors.attachAlpha(color, 0.5);
            aggregateDatasets.push(correspondingSet);
         }
      }
      return set;
   });

   return {
      labels: Util.makeSparse(dates.map(Util.formattedDate),
                              CConfig.labelsPerXAxis, ''),
      datasets: individualDatasets.concat(aggregateDatasets)
   };
};

// Returns a list of complete 'dates' (for all available data points including
// aggregates) and a 'pivot' representing the last date with an entry in 
// 'individualAnswers'.
ChartLoader.xAxisInfo = function(individualAnswers, aggregateAnswers) {
   var individual = Util.extract(individualAnswers, 'submitted');
   var aggregate = Util.extract(aggregateAnswers, 'submitted');
   var all = Util.extract(aggregateAnswers.concat(individualAnswers),
                          'submitted');
   var emphasis = Util.fullDateRange(individual);
   return {
      dates: Util.fullDateRange(all.sort()),
      pivot: emphasis[emphasis.length - 1]
   };
}

// Adjusts the submitted fields in 'aggregate' to be consistent with the day
// deltas for 'baselineUser'. In essence, adjust all other user answer times
// so that they appear to have begun being reported on the same day that
// 'baselineUser' began reporting.
ChartLoader.adjustSubmittedToBaseline = function(baselineUser, aggregate) {
   // TODO(alfinoc): This assumes that all users answer all questions for a
   // hypothesis the first time they submit an answers. To get around this
   // assumption, we'll want to not only sort 'byUser' as below, but by
   // question as well.
   if (baselineUser != null) {
      var byUser = Util.groupBy(aggregate, 'user');
      var newStart = Util.min(Util.extract(byUser[baselineUser], 'submitted'));
      for (var user in byUser) {
         var answers = byUser[user];
         var trueStart = Util.min(Util.extract(answers, 'submitted'));
         answers.forEach(function(a) { 
            a['submitted'] = a['submitted'] - trueStart + newStart;
         });
      }
   }
   return aggregate;
};

ProjectionSlider = PS = {};

ProjectionSlider.WRAPPER_SELECTOR = '#rangewrapper';
ProjectionSlider.DISPLAY_SELECTOR = '#projectiondisplay';
ProjectionSlider.INPUT_SELECTOR = '#projection';
ProjectionSlider.dates = [];

// Sets the range to cover the full sequence of given dates (must be sorted),
// less the values that come before pivot. Initializes PS event listeners.
ProjectionSlider.init = function(dates, pivot) {
   PS.dates = dates;
   var maxDate = dates[dates.length - 1];
   if (!maxDate || pivot >= max) {
      $(PS.WRAPPER_SELECTOR).classList.add('hide');
      return;
   }
   PS.setRange(pivot, maxDate, dates[1] - dates[0]);
   PS.updateDisplay();
   PS.input().addEventListener('input', PS.updateDisplay);
   PS.input().addEventListener('input', PS.refreshChart);
};

// Returns the range input value.
ProjectionSlider.getValue = function() {
   return PS.input().value;
};

// Sets the min, max, and step fields of the input range to those provided, then
// sets the value to the min.
ProjectionSlider.setRange = function(min, max, step) {
   PS.input().setAttribute('min', min);
   PS.input().setAttribute('max', max);
   PS.input().setAttribute('step', step);
   PS.setValue(min);
};

// Sets the slider value to the given value.
ProjectionSlider.setValue = function(value) {
   PS.input().setAttribute('value', value);
   PS.refreshChart();
};

// Reloads the chart module with the upper bound from the slider.
ProjectionSlider.refreshChart = function() {
   var data = CL.getExternalData();
   CL.loadChart(data.individual, data.aggregate, PS.leftOf(PS.getValue()));
};

// Returns a list of dates that are all less or equal to maxDate.
ProjectionSlider.leftOf = Util.partial(function(cache, maxDate) {
   if (cache[maxDate])
      return cache[maxDate];
   else
      return cache[maxDate] = PS.dates.filter(function(d) {
         return d <= maxDate;
      });
}, {});

// Updates the display element to contain a date formatted copy of the value in
// the slider input.
ProjectionSlider.updateDisplay = function() {
   $(PS.DISPLAY_SELECTOR).innerHTML =
      Util.formattedDate(parseInt(PS.input().value));
};

// Returns the input range element for the slider, or null if it hasn't loaded.
ProjectionSlider.input = function() {
   var elt = null;
   return function() {
      return elt || (elt = $(PS.INPUT_SELECTOR));
   };
}();
