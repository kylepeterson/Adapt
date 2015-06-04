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
   var foregroundColor = Colors.nextChartColor();
   var questionText = window.LABELS[answers[0]['question']];
   return {
      label: questionText,
      data: data,
      strokeColor: foregroundColor,
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

      // Each data set may have a corresponding aggregate line. Attempt to
      // find the aggregate line, then 
      if (answers.length > 0) {
         var corresponding = aggregateByQuestion[answers[0].question];
         if (corresponding) {
            var correspondingSet = CL.toDataset(dates, corresponding);
            correspondingSet.strokeColor
               = Colors.attachAlpha(set.strokeColor, 0.5);
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
   return aggregate
};

Util.min = function(array) {
   return Math.min.apply(null, array);
};

ProjectionSlider = PS = {};

ProjectionSlider.WRAPPER_SELECTOR = '#rangewrapper';
ProjectionSlider.DISPLAY_SELECTOR = '#projectiondisplay';
ProjectionSlider.INPUT_SELECTOR = '#projection';
ProjectionSlider.dates = [];

ProjectionSlider.init = function(dates, pivot) {
   PS.dates = dates;
   var maxDate = dates[dates.length - 1];
   if (!maxDate || pivot >= max) {
      // TODO(alfinoc): Hide the projection slider.
      $(PS.WRAPPER_SELECTOR).classList.add('hide');
      return;
   }
   PS.setRange(pivot, maxDate, dates[1] - dates[0]);
   PS.updateDisplay();
   PS.input().addEventListener('input', PS.updateDisplay);
   PS.input().addEventListener('change', PS.refreshChart);
};

ProjectionSlider.getValue = function() {
   return PS.input().value;
};

ProjectionSlider.setRange = function(min, max, step) {
   PS.input().setAttribute('min', min);
   PS.input().setAttribute('max', max);
   PS.input().setAttribute('step', step);
   PS.setValue(min);
};

ProjectionSlider.setValue = function(value) {
   PS.input().setAttribute('value', value);
   PS.refreshChart();
};

ProjectionSlider.refreshChart = function() {
   var maxDate = PS.getValue();
   var limited = PS.dates.filter(function(d) { return d <= maxDate; });
   var data = CL.getExternalData();
   CL.loadChart(data.individual, data.aggregate, limited);
};

ProjectionSlider.updateDisplay = function() {
   $(PS.DISPLAY_SELECTOR).innerHTML =
      Util.formattedDate(parseInt(PS.input().value));
};

ProjectionSlider.input = function() {
   var elt = null;
   return function() {
      return elt || (elt = $(PS.INPUT_SELECTOR));
   };
}();
