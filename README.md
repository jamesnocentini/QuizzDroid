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
            "question": "What's the capital of France?",
            "options": ["Paris", "Rome", "London", "Madrid"],
            "answer": 0,
            "tag": "geography",
            "type": "question",
            "theme": "#FF4E00"
        },
        {
            "_id": "2",
            "question": "Which of these countries is not in Europe?",
            "options": ["Bolivia", "United States", "Croatia", "Sweden"],
            "answer": 1,
            "tag": "geography",
            "type": "question",
            "theme": "#FEC601"
        },
        {
            "_id": "3",
            "question": "What's the Android version that preceded Lollipop?",
            "options": ["KitKat", "Lollipop", "Tetris", "Jumengi"],
            "answer": 0,
            "tag": "dev",
            "type": "question",
            "theme": "#2EC4B6"
        },
        {
            "_id": "11",
            "question_id": "1",
            "type": "answer",
            "answer": 2
        },
        {
            "_id": "12",
            "question_id": "1",
            "type": "answer",
            "answer": 3
        },
        {
            "_id": "21",
            "question_id": "2",
            "type": "answer",
            "answer": 2
        },
        {
            "_id": "22",
            "question_id": "2",
            "type": "answer",
            "answer": 3
        },
        {
            "_id": "23",
            "question_id": "2",
            "type": "answer",
            "answer": 2
        },
        {
            "_id": "31",
            "question_id": "3",
            "type": "answer",
            "answer": 0
        },
        {
            "_id": "32",
            "question_id": "3",
            "type": "answer",
            "answer": 1
        },
        {
            "_id": "33",
            "question_id": "3",
            "type": "answer",
            "answer": 2
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