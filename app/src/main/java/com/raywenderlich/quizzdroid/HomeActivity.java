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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Document;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.raywenderlich.quizzdroid.adapter.HomeAdapter;
import com.raywenderlich.quizzdroid.model.Question;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static String TAG = "log";
    public static String EXTRA_INTENT_ID = "id";
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Manager manager = Manager.getSharedInstance(getApplicationContext());
        recyclerView = (RecyclerView) findViewById(R.id.rvQuestions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        QueryEnumerator questions = null;
        try {
            questions = Question.getQuestions(manager.database).run();
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }

        List<Question> data = new ArrayList<>();
        for (QueryRow question : questions) {
            Document document = question.getDocument();
            Question model = ModelHelper.modelForDocument(document, Question.class);
            data.add(model);
        }

        final HomeAdapter adapter = new HomeAdapter(data);
        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void OnClick(View view, int position) {
                Log.d(TAG, String.format("Click question at position %d", position));
                Intent intent = new Intent(getApplicationContext(), QuestionActivity.class);
                Question selected = (Question) adapter.getmQuestions().get(position);
                intent.putExtra(EXTRA_INTENT_ID, selected.get_id());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);
    }
}
