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

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.View;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.javascript.JavaScriptViewCompiler;
import com.couchbase.lite.listener.Credentials;
import com.couchbase.lite.listener.LiteListener;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;
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

public class Manager {
    private static Manager instance = null;
    public Database database;
    private String SYNC_GATEWAY_URL = "http://localhost:4984/quizzdroid";

    protected Manager(Context context) {
        com.couchbase.lite.Manager.enableLogging(com.couchbase.lite.util.Log.TAG, com.couchbase.lite.util.Log.VERBOSE);
        com.couchbase.lite.Manager.enableLogging(Log.TAG_QUERY, com.couchbase.lite.util.Log.VERBOSE);
        com.couchbase.lite.Manager.enableLogging(Log.TAG_LISTENER, com.couchbase.lite.util.Log.VERBOSE);
        com.couchbase.lite.Manager.enableLogging(Log.TAG_ROUTER, com.couchbase.lite.util.Log.VERBOSE);
        com.couchbase.lite.Manager.enableLogging(Log.TAG_VIEW, com.couchbase.lite.util.Log.VERBOSE);

        copyAssetFolder(context.getAssets(), "quizzdroid.cblite2",
                    "/data/data/com.raywenderlich.quizzdroid/files/quizzdroid.cblite2");

        View.setCompiler(new JavaScriptViewCompiler());
        com.couchbase.lite.Manager manager = null;
        try {
            manager = new com.couchbase.lite.Manager(new AndroidContext(context), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            database = manager.getExistingDatabase("quizzdroid");
        } catch (CouchbaseLiteException e) {
            e.printStackTrace();
        }
        Credentials credentials = new Credentials(null, null);
        LiteListener liteListener = new LiteListener(manager, 5984, credentials);
        Thread thread = new Thread(liteListener);
        thread.start();

        URL syncGatewayURL = null;
        try {
            syncGatewayURL = new URL(SYNC_GATEWAY_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Replication push = database.createPushReplication(syncGatewayURL);
        push.setContinuous(true);
        push.start();

        Replication pull = database.createPullReplication(syncGatewayURL);
        pull.setContinuous(true);
        pull.start();
    }

    public final String path = "/data/data/com.raywenderlich.quizzdroid/files/quizzdroid.cblite2/";
    public final String Name = "db.sqlite3";

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static Manager getSharedInstance(Context context) {
        if (instance == null) {
            instance = new Manager(context);
        }
        return instance;
    }
}
