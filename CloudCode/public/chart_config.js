ChartConfiguration = CConfig = {};

ChartConfiguration.options = {
   // Fix scale to 0 through 5;
   scaleOverride: true,
   scaleStartValue: 0,
   scaleStepWidth: 1,
   scaleSteps: 10,

   // Simplify style a bit.
   datasetFill: false,
   showTooltips: false,
   scaleShowVerticalLines: false,
   scaleFontFamily: "Roboto, sans-serif",

   // Maintain original CSS fixed height but let width be browser dependent.
   responsive: true,
   maintainAspectRatio: false,

   // The worst...
   legendTemplate : "<ul class=\"<%=name.toLowerCase()%>-legend\"><% for (var i=0; i<datasets.length; i++){%><li><span style=\"background-color:<%=datasets[i].strokeColor%>\"></span><span class=label><%if(datasets[i].label){%><%=datasets[i].label%><%}%></span></li><%}%></ul>"
};

ChartConfiguration.labelsPerXAxis = 5;
