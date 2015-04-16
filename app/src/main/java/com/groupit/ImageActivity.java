package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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

    private Bitmap getResizedBitmap(Bitmap bm) {
        if (bm == null) {
            return bm;
        }

        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width <= 2048 && height <= 2048) {
            return bm;
        }

        float scaleWidth = ((float) 2048) / width;
        float scaleHeight = ((float) 2048) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        bm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return bm;
    }
}
