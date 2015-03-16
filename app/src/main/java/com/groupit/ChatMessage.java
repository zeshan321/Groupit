package com.groupit;

import android.net.Uri;

public class ChatMessage {
    public boolean left;
    public String message;
    public String display;
    public boolean image;
    public Uri imageU;
    public boolean useByte;

    public ChatMessage(boolean left, String message, String display, boolean image, Uri imageU, boolean useByte) {
        super();
        this.left = left;
        this.message = message;
        this.display = display;
        this.image = image;
        this.imageU = imageU;
        this.useByte = useByte;
    }
}