package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

public class ImageActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Image Viewer");

        ImageView imageView = (ImageView) findViewById(R.id.imageView);

        File file = new File(getIntent().getExtras().getString("image"));
        try {
            imageView.setImageBitmap(getResizedBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(file))));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PhotoViewAttacher attacher = new PhotoViewAttacher(imageView);
        attacher.update();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Bitmap getResizedBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return bitmap;
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int newWidth = 2048;
        int newHeight = 2048;

        if (w <= 2048 && h <= 2048) {
            return bitmap;
        }

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));


        return scaledBitmap;
    }
}
