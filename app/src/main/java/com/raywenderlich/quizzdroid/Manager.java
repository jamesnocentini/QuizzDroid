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
import android.content.res.AssetManager;
import android.util.Log;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Query;
import com.couchbase.lite.QueryEnumerator;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.javascript.JavaScriptReplicationFilterCompiler;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.support.FileDirUtils;
import com.couchbase.lite.util.ZipUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Manager {
    private static Manager instance = null;
    public Database database;
    private static String TAG = "Manager";
    public Replication push;
    public Replication pull;

    protected Manager(Context context) {

        com.couchbase.lite.Manager manager = null;
        try {
            // 1
            manager = new com.couchbase.lite.Manager(new AndroidContext(context), null);
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                database = manager.getDatabase("quizzdroid");
            } catch (CouchbaseLiteException e) {
                e.printStackTrace();
            }
        }

        Credentials credentials = new Credentials(null, null);
        View.setCompiler(new JavaScriptViewCompiler());
        Database.setFilterCompiler(new JavaScriptReplicationFilterCompiler());
        LiteListener liteListener = new LiteListener(manager, 5984, credentials);
        Thread thread = new Thread(liteListener);
        thread.start();

        URL syncGatewayURL = null;
        try {
            String SYNC_GATEWAY_URL = "http://localhost:4985/quizzdroid";
            syncGatewayURL = new URL(SYNC_GATEWAY_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        push = database.createPushReplication(syncGatewayURL);
        push.setContinuous(true);
        push.start();

        pull = database.createPullReplication(syncGatewayURL);
        pull.setContinuous(true);
        pull.start();
    }

    public final String path = "/data/data/com.raywenderlich.quizzdroid/files/quizzdroid.cblite2/";
    public final String Name = "db.sqlite3";

    public static Manager getSharedInstance(Context context) {
        if (instance == null) {
            instance = new Manager(context);
        }
        return instance;
    }
}
