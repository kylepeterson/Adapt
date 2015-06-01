from math import exp
from functools import partial
from random import random, choice
from itertools import chain

"""
Returns the value of a comfy logistic function for codomain values between 0 and
1 on the domain 0 to 1.
"""
def logistic(x, steepness):
   return 2 / (1 + exp(- steepness * x)) - 1

"""
An ease-out function with easeout(0) ~ start and easeout(1) ~ end. Higher
steepness values will increase the rate of initial increase or decrease.
"""
def easeout(x, start=0, end=1, steepness=5):
   return (end - start) * logistic(x, steepness) + start

"""
Returns a random set of easeout fixed parameters for gradual improvement across
the 0 to 1 domain.
"""
def randomImproveConfiguration():
   start = random() / 2
   return {
      'start': start,
      'end': start + (1 - start) * random(),
      'steepness': choice(range(3, 8))
   }

"""
Returns a random set of easeout fixed parameters for gradual decline across the
0 to 1 domain.
"""
def randomDeclineConfiguration():
   config = randomImproveConfiguration()
   temp = config['start']
   config['start'] = config['end']
   config['end'] = temp
   return config

"""
Returns a function that improves steadily, more at first than later. Domain and
codomain both 0 to 1.
"""
def improve():
   return partial(easeout, **randomImproveConfiguration())

"""
Returns a function that declines steadily, more at first than later. Domain and
codomain both 0 to 1.
"""
def decline():
   return partial(easeout, **randomDeclineConfiguration())

"""
Returns a function that always returns 1.
"""
def constant():
   return lambda x : 1

"""
Returns a function that behaves just as trend, but with uniformly random errors
in the plus/minus 'maxDifference' range.
"""
def noisy(trend, maxDifference=0.03):
   return lambda x : trend(x) + random() * choice([1, -1]) * maxDifference

"""
Returns one of the trend function choices, skewing odds according to the second
pair element.
"""
TREND_CHOICES = [
   (improve, 4),
   (decline, 2),
   (constant, 1)
]
def randomTrend():
   expanded = map(lambda (c, r) : [c] * r, TREND_CHOICES)
   return noisy(choice(list(chain.from_iterable(expanded)))())
