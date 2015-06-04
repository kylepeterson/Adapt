Util = {};

Util.MS_PER_DAY = 24 * 60 * 60 * 1000;

// Returns a new function that is just like 'fn' except that the first
// 'var_args'.length arguments are fixed to the values in 'var_args'.
// Straight ripped off from Google Closure (see goog.bind docs).
Util.partial = function(fn, var_args) {
   var args = Array.prototype.slice.call(arguments, 1);
   return function() {
      var newArgs = args.slice();
      newArgs.push.apply(newArgs, arguments);
      return fn.apply(this, newArgs);
   };
};

// Returns a dictionary with keys for each possible value for dicts[i][key].
// Each such key k maps to a list of all dicts d in dictList such that
// d[key] = k.
Util.groupBy = function(dicts, key) {
   var res = {};
   dicts.forEach(function(dict) {
      var value = dict[key];
      if (!res[value])
         res[value] = [];
      res[value].push(dict);
   });
   return res;
};

// Returns a new array identical to 'list' but with all values set to 'value'
// except for 'leave' number of evenly spaced entries.
Util.makeSparse = function(list, leave, value) {
   var i = 0;
   var step = Math.max(parseInt(list.length / leave), 1);
   return list.map(function(elt) {
      return i++ % step == 0 ? elt : value;
   });
};

// Returns a list of values dict[k] for all keys k, with possible duplicates.
Util.values = function(dict) {
   return Object.keys(dict).map(function(k) {
      return dict[k];
   });
};

// Returns a list of all obj[i][key] for valid i.
Util.extract = function(objs, key) {
   return objs.map(function(o) {
      return o[key];
   });
};

// Returns the mean of all values in the list.
Util.mean = function(list) {
   if (list.length == 0)
      return 0;
   return list.reduce(function(a, b) { return a + b; }) / list.length;
};

// Returns a list of all millisecond times between 'from' to 'to', inclusive,
// with a step size of one day in milliseconds.
Util.dayRange = function(from, to) {
   var min = Math.min(from, to);
   var max = Math.max(from, to);
   var res = [];
   for (var i = min; i <= max; i += Util.MS_PER_DAY)
      res.push(i);
   return res;
};

// Returns a sorted list of millisecond times starting with the minimal value
// in 'dates' and ending with the maximal, with a step size of one day in
// milliseconds.
Util.fullDateRange = function(dates) {
   if (dates.length == 0)
      return [];
   var min = max = dates[0];
   dates.forEach(function(d) {
      min = Math.min(d, min);
      max = Math.max(d, min);
   });
   return Util.dayRange(min, max);
};

// Returns a reference to 'obj'. obj[key] must be a Parse timestamp, and is
// replaced by the millisecond time corresponding the last whole day for
// obj[key].
Util.convertDate = function(key, obj) {
   var parseTimestamp = obj[key];
   // Round down to the nearest whole day.
   parseTimestamp = parseTimestamp.split('T')[0];
   obj[key] = new Date(parseTimestamp).getTime();
   return obj;
};

// Given a millisecond time 'd', returns a formatted date string mm/dd/yyyy.
Util.formattedDate = function(d) {
   d = new Date(d);
   // Month and day are zero indexed.
   return [(d.getMonth() + 1), (d.getDate() + 1), d.getFullYear()].join('/');
};

// Returns the minimum value in the array.
Util.min = function(array) {
   return Math.min.apply(null, array);
};

// Sets obj1[k] = obj2[k] for all keys k of obj1.
Util.mixin = function(obj1, obj2) {
   for (var k in obj2)
      obj1[k] = obj2[k];
};

// Document selector aliases.
$ = function(selector) {
   return document.querySelector(selector);
};
$$ = function(selector) {
   return document.querySelectorAll(selector);
};
