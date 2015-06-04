window.Colors = {};

// Copied from Adapt/app/src/main/res/values/colors.xml.
Colors.Adapt = {
   green: '122, 162, 118',
   green_alpha: '166, 122, 162',
   blue: '94, 137, 133',
   mustard: '214, 205, 144',
   dark_grey: '58, 52, 49',
   light_grey: '94, 93, 91',
   light_grey_alpha: '166, 94, 93, 91',
   zebra_list_grey: '219, 219, 219',
   red: '198, 65, 51',
   white: '255, 255, 255'
};

// Chart line colors.
Colors.ChartForeground = ['blue', 'red', 'mustard', 'green',];

Util.mixin(Colors, function() {
   var next = -1;
   return {
      // Returns RGB colors in order round robin from ChartForeground on
      // consecutive calls.
      nextChartColor: function() {
         next++;
         next %= Colors.ChartForeground.length;
         return 'rgb(' + Colors.Adapt[Colors.ChartForeground[next]] + ')';
      },
      // Sets the next color returned by nextChartColor to the initial value
      // returned when the page reloads.
      resetColorCycle: function() {
         next = -1;
      }
   }
}());

Colors.attachAlpha = function(rgb, alpha) {
   var nums = rgb.substring(4, rgb.length - 1);
   return 'rgba(' + nums + ', ' + alpha + ')';
};
