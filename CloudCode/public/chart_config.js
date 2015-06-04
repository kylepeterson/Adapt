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
   animation: false,
   pointDot: false,

   // Maintain original CSS fixed height but let width be browser dependent.
   responsive: true,
   maintainAspectRatio: false,

   // The worst...
   legendTemplate: "<ul class=\"<%=name.toLowerCase()%>-legend\">" +
         "<% for (var i = 0; i < datasets.length; i++) {%>" +
            // TODO(alfinoc): Huge hack. The way to check if a dataset is aggregate
            // is to (wait for it...) check if the strokeColor as an 'a' for alpha!
            "<li><span style=\"background-color:<%=datasets[i].strokeColor%>\"></span>" +
            "<span class=label>" +
            "<%if (datasets[i].label && datasets[i].strokeColor.indexOf('a') == -1) {%>" +
            "<%=datasets[i].label%><%} else {%> Aggregate <%}%></span></li><%}%>" +
      "</ul>"
};

ChartConfiguration.labelsPerXAxis = 5;
