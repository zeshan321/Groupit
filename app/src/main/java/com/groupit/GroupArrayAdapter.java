package com.groupit;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.ArrayList;
import java.util.List;

class GroupArrayAdapter extends ArrayAdapter<GroupMessage> {

    private TextView chatText;
    private TextView chatName;
    private ImageView image;
    private List<GroupMessage> chatMessageList = new ArrayList<GroupMessage>();
    private Context context;

    @Override
    public void add(GroupMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public GroupArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public GroupMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public void removeChat(int index) {
        chatMessageList.remove(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        GroupMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        row = inflater.inflate(R.layout.groups_layout, parent, false);

        chatText = (TextView) row.findViewById(R.id.chatListItemName);
        chatText.setText(chatMessageObj.message);

        chatName = (TextView) row.findViewById(R.id.chatListItemHints);
        chatName.setText(chatMessageObj.id);

        image = (ImageView) row.findViewById(R.id.chatListItemImage);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        String s = String.valueOf(chatMessageObj.message.charAt(0)).toUpperCase();
        int color2 = generator.getColor(s);

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(97)  // width in px
                .height(97) // height in px
                .endConfig()
                .buildRound(s, color2);

        image.setImageDrawable(drawable);
        return row;
    }
}