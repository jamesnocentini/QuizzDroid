package com.raywenderlich.quizzdroid;

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

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.support.LazyJsonArray;
import com.couchbase.lite.util.ArrayUtils;
import com.raywenderlich.quizzdroid.adapter.OptionsQuestionAdapter;
import com.raywenderlich.quizzdroid.model.Answer;
import com.raywenderlich.quizzdroid.model.Question;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestionActivity extends AppCompatActivity {

    private TextView textView;
    private TextView resultTextView;
    private GridView questionOptions;
    private int selectedOption;
    private Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        textView = (TextView) findViewById(R.id.question_view);
        resultTextView = (TextView) findViewById(R.id.resultTextView);

        Intent intent = getIntent();
        String questionId = intent.getStringExtra(HomeActivity.EXTRA_INTENT_ID);
        Log.d(HomeActivity.TAG, String.format("Recieved %s", questionId));

        Manager manager = Manager.getSharedInstance(getApplicationContext());
        Document document = manager.database.getDocument(questionId);

        question = ModelHelper.modelForDocument(document, Question.class);
        textView.setText(question.getQuestion());

        questionOptions = (GridView) findViewById(R.id.question_options);
        questionOptions.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        questionOptions.setNumColumns(2);
        questionOptions.setSelector(R.drawable.selector_button);
        questionOptions.setAdapter(new OptionsQuestionAdapter(question.getOptions()));
        questionOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = position;
            }
        });
    }

    public void onButtonClicked(View view) {
        Answer answer = new Answer(question.get_id(), "answer", selectedOption);
        ModelHelper.save(Manager.getSharedInstance(getApplicationContext()).database, answer);
        showAnswers();
    }

    private void showAnswers() {
        Manager manager = Manager.getSharedInstance(getApplicationContext());
        Query answers = Answer.getAnswersForQuestion(manager.database, question.get_id());

        QueryEnumerator result = null;
        try {
            result = answers.run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        String text = "";

        for (QueryRow row : result) {
            LazyJsonArray<Object> object = (LazyJsonArray<Object>) row.getKey();
            int answerPosition = (int) object.get(1);
            List<String> options = Arrays.asList(question.getOptions());
            text = text.concat(String.format("%s: %s \n", options.get(answerPosition), row.getValue().toString()));
        }
        resultTextView.setText(text);
    }
}
