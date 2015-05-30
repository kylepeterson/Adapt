window.Colors = {};

// Copied from Adapt/app/src/main/res/values/colors.xml.
Colors.Adapt = {
   green: 'rgb(122, 162, 118)',
   green_alpha: 'rgb(166, 122, 162)',
   blue: 'rgb(94, 137, 133)',
   mustard: 'rgb(214, 205, 144)',
   dark_grey: 'rgb(58, 52, 49)',
   light_grey: 'rgb(94, 93, 91)',
   light_grey_alpha: 'rgba(166, 94, 93, 91)',
   zebra_list_grey: 'rgb(219, 219, 219)',
   red: 'rgb(198, 65, 51)',
   white: 'rgb(255, 255, 255)'
};

// Chart line colors.
Colors.ChartForeground = ['blue', 'red', 'mustard', 'green',];

// Returns RGB colors in order round robin from ChartForeground on consecutive
// calls.
Colors.nextChartColor = function() {
   var next = -1;
   return function() {
      next++;
      next %= Colors.ChartForeground.length;
      return Colors.Adapt[Colors.ChartForeground[next]];
   }
}();
