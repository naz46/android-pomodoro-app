package com.nazlcanozturk46.pomodoro;

/**
 * Created by nazlican on 6.01.2018.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
public class AsyncLoadImage  extends AsyncTask<String, String, Bitmap> {
    private final static String TAG = "AsyncTaskLoadImage";
    private ImageView imageView;
    public AsyncLoadImage(ImageView imageView) {
        this.imageView = imageView;
    }
    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap bitmap = null;
        try {
            URL url = new URL(params[0]);
            bitmap = BitmapFactory.decodeStream((InputStream)url.getContent());
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }
        return bitmap;
    }
    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
    }
}
