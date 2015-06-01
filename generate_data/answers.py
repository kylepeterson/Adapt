from datetime import datetime, timedelta
from functools import partial
from random import choice
from trends import randomTrend

# Generate dates between FIRST_DAY and FIRST_DAY + SAMPLE_SPACE_LENGTH.
FIRST_DAY = datetime.now()
SAMPLE_SPACE_LENGTH = 365

# Possible numbers of answers per question by a single user.
ANSWERS_PER_QUESTION = range(10, 50)

# The maximum answer content value.
ANSWER_CONTENT_MAX = 10

"""
Returns a dictionary in Parse JSON format for a Parse.Object with the given
className and id.
"""
def parsePointer(className, id):
   return {
      '__type': 'Pointer',
      'className': className,
      'objectId': id
   }

"""
Returns a dictionary in Parse JSON format for a Parse Date column value with
representing the given datetime.
"""
def parseDate(date):
   return {
      '__type': 'Date',
      'iso': date.isoformat() + 'Z'
   }

"""
Returns a dictionary in Parse JSON format for a Parse.Answer object with the
given submission 'date' and answerContent 'value'. 'questionPtr' and 'userPtr'
should be existing Parse object ids for the question to answer as the user.
"""
def answer(date, value, questionPtr, userPtr):
   return {
      'question': parsePointer('Question', questionPtr),
      'user': parsePointer('_User', userPtr),
      'submittedAt': parseDate(date),
      'answerContent': value
   }

"""
Returns a vector of answerContent values of size 'length' with values between 0
and ANSWER_CONTENT_MAX. The trend visible in the vector is determined by
util.randomTrend.
"""
def valueTrajectory(length):
   normal = map(lambda val : float(val) / length, range(length))
   trended = map(randomTrend(), normal)
   return map(lambda val : val * ANSWER_CONTENT_MAX, trended)

"""
Returns a list of lists l, where each l is a list of Parse JSON formatted Answer
dictionaries for a question in 'questionPtrs'. All returned answers are tagged
as entries by the user with given 'userPtr' id. Trends are randomly sampled in
plausible ranges.
"""
def answers(questionPtrs, userPtr):
   start = choice(range(SAMPLE_SPACE_LENGTH))
   end = min(start + choice(ANSWERS_PER_QUESTION), SAMPLE_SPACE_LENGTH)

   dates = map(lambda delta : FIRST_DAY + timedelta(days=delta),
               range(start, end))
   res = []
   for questionPtr in questionPtrs:
      values = valueTrajectory(len(dates))
      toAnswer = partial(answer, questionPtr=questionPtr, userPtr=userPtr)
      res.append(map(lambda pt : toAnswer(*pt), zip(dates, values)))
   return res
