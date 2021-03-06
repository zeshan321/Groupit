package com.groupit;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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

    public void clearList() { chatMessageList.clear(); }

    public View getView(int position, View convertView, ViewGroup parent) {
        GroupMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        row = inflater.inflate(R.layout.groups_layout, parent, false);

        chatText = (TextView) row.findViewById(R.id.chatListItemName);
        chatText.setText(Html.fromHtml(chatMessageObj.message));

        chatName = (TextView) row.findViewById(R.id.chatListItemHints);
        chatName.setText(Html.fromHtml(chatMessageObj.id));

        image = (ImageView) row.findViewById(R.id.chatListItemImage);

        ColorGenerator generator = ColorGenerator.MATERIAL;
        String replaceHTML = chatMessageObj.message.replaceAll("<(.*?)>", "");
        String s = null;
        if (replaceHTML.length() <= 0) {
            s = String.valueOf(chatMessageObj.message.charAt(0));
        } else {
            s = String.valueOf(replaceHTML.charAt(0));
        }

        int color2 = generator.getColor(s);

        TextDrawable drawable = TextDrawable.builder()
                .beginConfig()
                .width(97)  // width in px
                .height(97) // height in px
                .endConfig()
                .buildRound(s, color2);

        image.setImageDrawable(drawable);

        // Display count
        if (MessageService.count.containsKey(chatMessageObj.message)) {
            if (MessageService.count.get(chatMessageObj.message) != 0) {
                TextView count = (TextView) row.findViewById(R.id.icNewCount);
                count.setVisibility(View.VISIBLE);
                count.setText(String.valueOf(MessageService.count.get(chatMessageObj.message)));
            }
        } else {
            TextView count = (TextView) row.findViewById(R.id.icNewCount);
            count.setVisibility(View.GONE);
        }

        TextView count = (TextView) row.findViewById(R.id.chatListItemDate);
        count.setVisibility(View.GONE);
        return row;
    }
}