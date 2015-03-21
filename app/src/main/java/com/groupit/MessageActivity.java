package com.groupit;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;

public class MessageActivity extends ActionBarActivity {

    public ListView chatMsg;
    public ArrayAdapter myAdapter;
    public String display;
    public Context con;
    public String currentGroup = "0";
    public boolean isLooking = false;
    public String groupName = null;

    EditText editTextSay;
    ImageButton buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final GroupActivity GA = new GroupActivity();
        final ClientMessage CM = new ClientMessage();

        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_display);

        setTitle(groupName);

        isLooking = true;
        con = this;
        CM.con = this;

        myAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.list_item_message_left);
        chatMsg = (ListView) findViewById(R.id.list_view_messages);

        MessageHandler mh = new MessageHandler(currentGroup, null, con);
        mh.loadMessages();

        editTextSay = (EditText) findViewById(R.id.say);
        buttonSend = (ImageButton) findViewById(R.id.send);

        ImageButton b = (ImageButton) findViewById(R.id.send);
        b.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                final String msg = editTextSay.getText().toString();
                String json = null;
                if (msg.equals("")) {
                    return;
                }

                json = new JSONUtils().getJSONMessage(GA.ID, currentGroup, msg, display, false);
                addMessage(true, new JSONUtils().getMessage(json), new JSONUtils().getName(json), currentGroup);
                CM.sendData(new JSONUtils().getJSONMessage(GA.ID, currentGroup, msg, display, false));
                editTextSay.setText("");
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        isLooking = true;
    }

    @Override
    protected void onPause() {
        isLooking = false;

        super.onPause();
    }

    public static void sendToast(String message, Context con) {
        Toast toast = Toast.makeText(con, message, Toast.LENGTH_LONG);
        toast.show();
    }

    public void addMessage(boolean right, String text, String name, String group) {
        final ClientMessage CM = new ClientMessage();

        try {
            myAdapter.add(new ChatMessage(right, text, name, false, null, false));

            chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            chatMsg.setAdapter(myAdapter);
        } catch (NullPointerException e) {
            MessageHandler mh = new MessageHandler(group, null, CM.con);
            mh.saveMessage();
        }
    }

    public void showSettings() {
        final GroupActivity GA = new GroupActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_locker, null);
        final String[] settings = GA.settings.get(currentGroup).split(" , ");

        final Switch sw = (Switch) v.findViewById(R.id.switch1);
        final EditText et = (EditText) v.findViewById(R.id.passCode);

        if (settings[1].equalsIgnoreCase("true")) {
            sw.setChecked(true);
        }

        if (settings.length >= 3) {
        if (!(settings[2].equals("null"))) {
                et.setText(settings[2]);
            }
        }

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                       boolean isPrivate = false;
                       String password = "";

                        if (sw.isChecked()) {
                            isPrivate = true;
                            password = et.getText().toString();
                            if (password.length() < 1 && password.startsWith(" ") == false) {
                                Toast.makeText(con, "Password needs to be greater then 1 character.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }

                        final boolean p1 = isPrivate;
                        final String p2 = password;

                        ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");
                        query.getInBackground(settings[0], new GetCallback<ParseObject>() {
                            @Override
                            public void done(ParseObject values, ParseException e) {
                                values.put("locked", p1);
                                values.put("pass", p2.replaceAll("\\s+$", ""));
                                values.saveInBackground();

                                GA.settings.put(currentGroup, settings[0] + " , " + p1 + " , " + p2.replaceAll("\\s+$", ""));
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        builder.create();
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                isLooking = false;
                Intent intent = new Intent(this, GroupActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                startActivity(intent);
                return true;
            case 0:
                showSettings();
                return true;
            case 1:
                Intent inte = new Intent();
                inte.setType("image/*");
                inte.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(inte, "Select Picture"), 1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final GroupActivity GA = new GroupActivity();

        menu.clear();

        if (GA.owns.contains(currentGroup)) {
            menu.add(0, 0, 0, "Settings");
        }
        menu.add(1, 1, 1, "Attach");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final GroupActivity GA = new GroupActivity();
        final ClientMessage CM = new ClientMessage();

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {

                Uri currImageURI = data.getData();

               myAdapter.add(new ChatMessage(true, "Image", display, true, currImageURI, false));

                chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                chatMsg.setAdapter(myAdapter);

                Bitmap bitmap = BitmapFactory.decodeFile(getRealPathFromURI(currImageURI));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] byte_img_data = baos.toByteArray();
                String encodedImage = Base64.encodeToString(byte_img_data, Base64.DEFAULT);

                CM.sendData(new JSONUtils().getJSONMessage(GA.ID, currentGroup, encodedImage, display, true));
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {

        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj,
                null,
                null,
                null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();

        return cursor.getString(column_index);
    }
}