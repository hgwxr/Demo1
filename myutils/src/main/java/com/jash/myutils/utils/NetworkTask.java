package com.jash.myutils.utils;

import android.os.AsyncTask;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkTask<T> extends AsyncTask<NetworkTask.Callback<T>, Void, Object> {
    private String url;
    private Callback<T> callback;
    private Class<T> type;

    NetworkTask(String url, Class<T> type) {
        this.url = url;
        this.type = type;
    }

    @Override
    protected Object doInBackground(Callback<T>... params) {
        callback = params[0];
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            int code = connection.getResponseCode();
            if (code == 200) {
                byte[] buffer = new byte[100 << 10];
                int length;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                InputStream is = connection.getInputStream();
                while ((length = is.read(buffer)) != -1){
                    bos.write(buffer, 0, length);
                }
                Gson gson = new Gson();
                return gson.fromJson(bos.toString("UTF-8"), type);
            } else {
                return new RuntimeException("ResponseCode: " + code);
            }
        } catch (IOException e) {
            return e;
        }
    }
    public void call(Callback<T> callback) {
        execute(callback);
    }

    @Override
    protected void onPostExecute(Object o) {
        if (type.isInstance(o)) {
            callback.onSuccess((T) o);
        } else {
            callback.onFailure((Exception) o);
        }
    }

    public static interface Callback<S>{
        void onSuccess(S data);
        void onFailure(Exception e);
    }
}
