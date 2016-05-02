package com.raywenderlich.quizzdroid.model;

/*
 * Copyright (c) 2016 Razeware LLC
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.View;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Answer {
    @JsonIgnore
    private static String ANSWER_VIEW_NAME = "answers";
    private String _id;
    private String _rev;
    private String question_id;
    private String type;
    private int answer;

    public Answer(String question_id, String type, int answer) {
        this.question_id = question_id;
        this.type = type;
        this.answer = answer;
    }

    public static Query getAnswersForQuestion(Database database, String questionId) {
        View view = database.getView(ANSWER_VIEW_NAME);
        if (view.getMap() == null) {
            view.setMapReduce(new Mapper() {
                @Override
                public void map(Map<String, Object> document, Emitter emitter) {
                    if (document.get("type").equals("answer")) {
                        List<Object> keys = new ArrayList<>();
                        keys.add((String) document.get("question_id"));
                        keys.add((int) document.get("answer"));
                        emitter.emit(keys, null);
                    }
                }
            }, new Reducer() {
                @Override
                public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
                    return values.size();
                }
            }, "4");
        }
        Query query = view.createQuery();
        query.setGroupLevel(2);
        query.setStartKey(Arrays.asList(questionId));
        query.setEndKey(Arrays.asList(questionId, new HashMap<String, Object>()));
        return query;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_rev() {
        return _rev;
    }

    public void set_rev(String _rev) {
        this._rev = _rev;
    }

    public String getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(String question_id) {
        this.question_id = question_id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }
}
