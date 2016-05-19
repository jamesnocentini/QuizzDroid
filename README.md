## Table of contents

- [Models](#models)
- [Queries](#queries)
- [Bulk](#bulk)

## Models

Question document:

```js
{
    "_id": "1",
    "question": "What's the capital of France?",
    "options": ["Paris", "Rome", "London", "Madrid"],
    "answer": 0,
    "answer_type": "single",
    "tag": "geography",
    "type": "question",
    "theme": 2131689553
}
```

Answer document:

```js
{
    "_id": "11",
    "question_id": "1",
    "type": "answer",
    "answer": 2
}
```

## Queries

Results view:

```
{
    "views": {
        "answers": {
            "map": "function(doc) {if (doc.type == 'answer') {emit([doc.question_id, doc.answer], null);}}",
            "reduce": "function(keys, values, rereduce) {return values.length;}"
        }
    }
}
```

From the command line:

```
curl -H 'Content-Type: application/json' -vX PUT 'http://localhost:5984/quizzdroid/_design/quizzdroid' -d @view.json | prettyjson
```

```
http://localhost:5984/quizzdroid/_design/quizzdroid/_view/answers?group_level=2&startkey=[%221%22]&endkey=[%221%22,{}]&include_docs=true
```

## Bulk

```js
{
    "docs": [
        {
            "_id": "1",
            "question": "This picture shows an object in space. Which one is it?",
            "options": ["A star", "A comet", "An asteroid", "A moon"],
            "answer": 1,
            "tag": "image",
            "type": "question",
            "theme": "#FF4E00"
        },
        {
            "_id": "2",
            "question": "What is the capital of New Zealand?",
            "options": ["Astana", "Bristol", "Wellington", "St Louis"],
            "answer": 2,
            "tag": "geography",
            "type": "question",
            "theme": "#FEC601"
        },
        {
            "_id": "3",
            "question": "Before its release, what device was known as the Nexus Prime?",
            "options": ["Nexus 4", "Nexus S", "Nexus 10", "Galaxy Nexus"],
            "answer": 3,
            "tag": "android",
            "type": "question",
            "theme": "#2EC4B6"
        }
    ]
}
```

From the command line:

```
curl -H 'Content-Type: application/json' -vX POST 'http://localhost:5984/quizzdroid/_bulk_docs' -d @data.json | prettyjson
```

Start Sync Gateway with a database:

```
~/Downloads/couchbase-sync-gateway/bin/sync_gateway -dbname="quizzdroid"
```

Start the reverse port forwarding 4984:

```
adb reverse tcp:4984 tcp:4984
```