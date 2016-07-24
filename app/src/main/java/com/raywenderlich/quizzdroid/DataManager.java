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

import android.content.Context;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Manager;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.javascript.JavaScriptReplicationFilterCompiler;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.ZipUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class DataManager {

  public Database database;

  private static DataManager instance = null;
  private Replication mPush;
  private Replication mPull;

  protected DataManager(Context context) {
    Manager manager = null;
    try {
      // 1
      manager = new Manager(new AndroidContext(context), Manager.DEFAULT_OPTIONS);
      // 2
      database = manager.getExistingDatabase("quizzdroid");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (CouchbaseLiteException e) {
      e.printStackTrace();
    }

    // 3
    if (database == null) {
      try {
        ZipUtils.unzip(context.getAssets().open("quizzdroid.cblite2.zip"), manager.getContext().getFilesDir());
        // 4
        database = manager.getDatabase("quizzdroid");
      } catch (IOException e) {
        e.printStackTrace();
      } catch (CouchbaseLiteException e) {
        e.printStackTrace();
      }
    }

    View.setCompiler(new JavaScriptViewCompiler());
    Database.setFilterCompiler(new JavaScriptReplicationFilterCompiler());

    Credentials credentials = new Credentials(null, null);
    LiteListener liteListener = new LiteListener(manager, 5984, credentials);

    Thread thread = new Thread(liteListener);
    thread.start();

    // 1
    URL syncGatewayURL = null;
    try {
      String SYNC_GATEWAY_URL = "http://localhost:4984/quizzdroid";
      syncGatewayURL = new URL(SYNC_GATEWAY_URL);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }

    // 2
    mPush = database.createPushReplication(syncGatewayURL);
    mPush.setContinuous(true);
    mPush.start();

    // 3
    mPull = database.createPullReplication(syncGatewayURL);
    mPull.setContinuous(true);
    mPull.start();

  }

  public static DataManager getSharedInstance(Context context) {
    if (instance == null) {
      instance = new DataManager(context);
    }
    return instance;
  }
}
