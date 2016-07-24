package com.raywenderlich.quizzdroid.model;

import com.couchbase.lite.Database;
import com.couchbase.lite.Emitter;
import com.couchbase.lite.Mapper;
import com.couchbase.lite.Query;
import com.couchbase.lite.Reducer;
import com.couchbase.lite.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Answer {

  private String _id;
  private String _rev;
  private String question_id;
  private String type;

  public Answer(String question_id, String type, String user_answer) {
    this.question_id = question_id;
    this.type = type;
    this.user_answer = user_answer;
  }

  public static Query getAnswersForQuestion(Database database, String questionId) {
    View view = database.getView("app/answers");
    if (view.getMap() == null) {
      view.setMapReduce(new Mapper() {
        @Override
        public void map(Map<String, Object> document, Emitter emitter) {
          if (document.get("type").equals("answer")) {
            List<Object> keys = new ArrayList<>();
            keys.add((String) document.get("question_id"));
            keys.add((String) document.get("user_answer"));
            emitter.emit(keys, null);
          }
        }
      }, new Reducer() {
        @Override
        public Object reduce(List<Object> keys, List<Object> values, boolean rereduce) {
          return values.size();
        }
      }, "1");
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

  public String getUser_answer() {
    return user_answer;
  }

  public void setUser_answer(String user_answer) {
    this.user_answer = user_answer;
  }

  private String user_answer;

}
