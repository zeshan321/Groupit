package com.groupit;

import android.graphics.Bitmap;
import android.net.Uri;

import java.sql.Timestamp;

public class ChatMessage {
    public boolean left;
    public String message;
    public String display;
    public boolean image;
    public Uri imageU;
    public boolean useByte;
    public Timestamp time;

    public ChatMessage(boolean left, String message, String display, boolean image, Uri imageU, boolean useByte, Timestamp time) {
        super();
        this.left = left;
        this.message = message;
        this.display = display;
        this.image = image;
        this.imageU = imageU;
        this.useByte = useByte;
        this.time = time;
    }
}