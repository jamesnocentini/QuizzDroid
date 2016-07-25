package com.raywenderlich.quizzdroid.adapter;

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

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.raywenderlich.quizzdroid.R;
import com.raywenderlich.quizzdroid.model.Question;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

  private List<Question> mQuestions;
  private OnItemClickListener mOnItemClickListener;

  public interface OnItemClickListener {
    void OnClick(View view, int position);
  }

  public HomeAdapter(List<Question> questions) {
    this.mQuestions = questions;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    Context context = parent.getContext();
    LayoutInflater inflater = LayoutInflater.from(context);
    View questionView = inflater.inflate(R.layout.row_questions, parent, false);
    return new ViewHolder(questionView);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, final int position) {
    // 1
    Question question = mQuestions.get(position);

    // 2
    switch (question.getTag()) {
      case "science":
        holder.itemView.setBackgroundColor(Color.parseColor("#EC5766"));
        break;
      case "geography":
        holder.itemView.setBackgroundColor(Color.parseColor("#FFC857"));
        break;
      case "android":
        holder.itemView.setBackgroundColor(Color.parseColor("#63ADF2"));
        break;
      case "logic":
        holder.itemView.setBackgroundColor(Color.parseColor("#86CD82"));
    }

    // 3
    holder.mQuestion.setText(question.getText());
    holder.mTag.setText(question.getTag());

    // 4
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mOnItemClickListener.OnClick(v, position);
      }
    });
  }

  @Override
  public int getItemCount() {
    return mQuestions.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {
    private TextView mQuestion;
    private TextView mTag;

    public ViewHolder(View itemView) {
      super(itemView);
      mQuestion = (TextView) itemView.findViewById(R.id.question);
      mTag = (TextView) itemView.findViewById(R.id.tagText);
    }
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    mOnItemClickListener = onItemClickListener;
  }

  public List<Question> getQuestions() {
    return mQuestions;
  }
}
