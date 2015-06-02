// Require: util.js, chart_config.js.

ChartLoader = CL = {};

ChartLoader.CANVAS_SELECTOR = 'canvas';
ChartLoader.LEGEND_SELECTOR = '.legend';

// Loads the chart data in window.ANSWERS into the Chart.js module.
window.onload = ChartLoader.init = function() {
   if (window.ANSWERS.length == 0) {
      document.querySelector('.missing').classList.remove('hide');
      return;
   }

   var chart = document.querySelector(CL.CANVAS_SELECTOR);
   var ctx = chart.getContext('2d');
   var data = window.ANSWERS.map(Util.partial(Util.convertDate, 'submitted'));
   var answers = CL.chartData(data);
   var chart = new Chart(ctx).Line(answers, CConfig.options);
   document.querySelector(CL.LEGEND_SELECTOR).innerHTML
      = chart.generateLegend();
}

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
      pointColor: foregroundColor,
      fillColor: foregroundColor
  }
}

// Converts a list of Parse Answer 'answers' to a list of Chart.js data
// datasets.
ChartLoader.chartData = function(answers) {
   var dates = Util.extract(answers, 'submitted');
   dates = Util.fullDateRange(dates);
   var datasets = Util.values(Util.groupBy(answers, 'question'))
      .map(Util.partial(CL.toDataset, dates));
   return {
      labels: Util.makeSparse(dates.map(Util.formattedDate),
                              CConfig.labelsPerXAxis, ''),
      datasets: datasets
   }
}
