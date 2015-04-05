package com.groupit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView chatName;
    private ImageView chatImage;
    private TextView timeStamp;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;
    private TextView location;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public void removeChat(int index) {
        chatMessageList.remove(index);
    }

    @Override
    public void clear() {
        this.chatMessageList.clear();

        super.clear();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (chatMessageObj.left) {
            if (chatMessageObj.image) {
                row = inflater.inflate(R.layout.list_item_message_right_image, parent, false);
            } else {
                row = inflater.inflate(R.layout.list_item_message_right, parent, false);
            }

        } else {
            if (chatMessageObj.image) {
                row = inflater.inflate(R.layout.list_item_message_left_image, parent, false);
            } else {
                row = inflater.inflate(R.layout.list_item_message_left, parent, false);
            }
        }

        chatName = (TextView) row.findViewById(R.id.lblMsgFrom);
        chatName.setText(chatMessageObj.display);

        timeStamp = (TextView) row.findViewById(R.id.lblMsgFromTime);
        timeStamp.setText(convertTime(chatMessageObj.time.getTime()));

        if (chatMessageObj.image) {
            chatImage = (ImageView) row.findViewById(R.id.imageMsg);
            chatImage.setImageBitmap(chatMessageObj.imageU);

            location = (TextView) row.findViewById(R.id.filePath);
            location.setText(chatMessageObj.message);
            location.setVisibility(View.GONE);
        } else {
            chatText = (TextView) row.findViewById(R.id.txtMsg);
            chatText.setText(chatMessageObj.message);
            chatText.setLinkTextColor(Color.BLUE);
            Linkify.addLinks(chatText, Linkify.WEB_URLS);
        }
        return row;
    }

    public String convertTime(long time){
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getDefault());
        cal.setTimeInMillis(time);

        String type = "AM";
        if (cal.get(Calendar.AM_PM) == 1) {
            type = "PM";
        }

        String min = String.valueOf(cal.get(Calendar.MINUTE));
        if (min.length() == 1) {
            min = min + "0";
        }

        return (cal.get(Calendar.HOUR) + ":"
                + min + " " + type);

    }
}