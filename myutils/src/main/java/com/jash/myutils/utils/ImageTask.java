package com.jash.myutils.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

class ImageTask extends AsyncTask<String, Void, Bitmap> {
    private ImageView img;
    private String url;
    private int id;
    public ImageTask(ImageView img, int id) {
        this.img = img;
        img.setTag(this);
        this.id = id;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        img.setImageResource(id);
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try {
            url = params[0];
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            int code = connection.getResponseCode();
            if (code == 200) {
                return BitmapFactory.decodeStream(connection.getInputStream());
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            img.setImageBitmap(bitmap);
            ImageUtils.cache.put(url, bitmap);
        } else {
            img.setImageResource(id);
        }
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        if (bitmap != null) {
            ImageUtils.cache.put(url, bitmap);
        }
    }
}
