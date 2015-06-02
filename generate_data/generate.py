import util as gen
import json
import sys

if len(sys.argv) < 2:
   sys.exit('Usage: generate.py question_file.json')

users = [
   'QpuXnQIAev',  # Kyle
   'UHmmyxlAPM'   # Roee
]

questions = json.load(file(sys.argv[1]))['results']
byHypothesis = {}
for q in questions:
   h = q['hypothesis']['objectId']
   # TODO: This check only necessary when this field is optional.
   if 'timeToAsk' in q and q['timeToAsk'] == 1:
      if h not in byHypothesis:
         byHypothesis[h] = []
      byHypothesis[h].append(q['objectId'])
questions = byHypothesis.values()

answers = []
for u in users:
   for qs in questions:
      for questionAnswers in gen.answers(qs, u):
         answers.extend(questionAnswers)
print json.dumps({'results': answers}, indent=3)
