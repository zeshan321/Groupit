package com.groupit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
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
import java.util.ArrayList;
import java.util.List;

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private TextView chatName;
    private ImageView chatImage;
    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;

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

        if (chatMessageObj.image) {
            chatImage = (ImageView) row.findViewById(R.id.imageMsg);
            if (chatMessageObj.useByte) {
                byte[] decodedString = Base64.decode(chatMessageObj.message, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                chatImage.setImageBitmap(decodedByte);
            } else {
                chatImage.setImageURI(chatMessageObj.imageU);
            }
        } else {
            chatText = (TextView) row.findViewById(R.id.txtMsg);
            chatText.setText(chatMessageObj.message);
        }
        return row;
    }
}