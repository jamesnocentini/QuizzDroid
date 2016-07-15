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
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.couchbase.lite.Attachment;
import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.LiveQuery;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.Revision;
import com.couchbase.lite.support.LazyJsonArray;
import com.raywenderlich.quizzdroid.adapter.QuestionOptionsAdapter;
import com.raywenderlich.quizzdroid.model.Answer;
import com.raywenderlich.quizzdroid.model.Question;

import java.io.InputStream;

public class QuestionActivity extends AppCompatActivity {

    private TextView textView;
    private TextView resultTextView;
    private GridView questionOptions;
    private ImageView imageQuestion;

    private int selectedOption;
    private Question question;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        textView = (TextView) findViewById(R.id.question_view);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        imageQuestion = (ImageView) findViewById(R.id.imageQuestion);

        // 1
        Intent intent = getIntent();
        String questionId = intent.getStringExtra(HomeActivity.EXTRA_INTENT_ID);
        Log.d(HomeActivity.TAG, String.format("Received %s", questionId));

        // 2
        Manager manager = Manager.getSharedInstance(getApplicationContext());
        Document document = manager.database.getDocument(questionId);

        // 3
        question = ModelHelper.modelForDocument(document, Question.class);
        textView.setText(question.getText());

        // 1
        questionOptions = (GridView) findViewById(R.id.question_options);
        // 2
        questionOptions.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        // 3
        questionOptions.setNumColumns(2);
        // 4
        questionOptions.setSelector(R.drawable.selector_button);
        // 5
        questionOptions.setAdapter(new QuestionOptionsAdapter(question.getOptions()));
        // 6
        questionOptions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = position;
            }
        });

        Revision revision = document.getCurrentRevision();
        Attachment attachment = revision.getAttachment("image");
        if (attachment != null) {
            InputStream is = null;
            try {
                is = attachment.getContent();
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
            Drawable drawable = Drawable.createFromStream(is, "image");
            imageQuestion.setImageDrawable(drawable);
        }

        showAnswers();
    }

    public void onButtonClicked(View view) {
        Answer answer = new Answer(question.get_id(), "answer", question.getOptions().get(selectedOption));
        ModelHelper.save(Manager.getSharedInstance(getApplicationContext()).database, answer);
    }

    private void showAnswers() {
        Manager manager = Manager.getSharedInstance(getApplicationContext());
        Query answersQuery = Answer.getAnswersForQuestion(manager.database, question.get_id());
        LiveQuery liveQuery = answersQuery.toLiveQuery();

        liveQuery.addChangeListener(new LiveQuery.ChangeListener() {
            @Override
            public void changed(LiveQuery.ChangeEvent event) {
                QueryEnumerator result = event.getRows();
                String text = "";

                for (QueryRow row : result) {
                    LazyJsonArray<Object> key = (LazyJsonArray<Object>) row.getKey();
                    Log.d("Debug", String.format("Row %s", (String) key.get(1)));
                    String answer = (String) key.get(1);
                    String content = String.format("%s: %s \n", answer, row.getValue().toString());
                    text = text.concat(content);
                }

                final String finalText = text;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultTextView.setText(finalText);
                    }
                });

            }
        });
        liveQuery.start();
    }
}
