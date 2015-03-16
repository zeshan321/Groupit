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
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class MessageActivity extends ActionBarActivity {

    public static String SocketAddress = "104.236.60.137";
    public static int SocketServerPORT = 47687;
    public static ListView chatMsg;
    public static ArrayAdapter myAdapter;
    public static Boolean allowReConnect = false;
    public static String display;
    public static ClientMessage cm = null;
    public static Context con;
    public static String currentGroup = "0";
    public static boolean isLooking = false;
    public static boolean finishedSetup = false;
    public static String groupName = null;

    EditText editTextSay;
    ImageButton buttonSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_display);

        setTitle(groupName);

        isLooking = true;
        con = this;
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

                try {
                    json = JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display, false);
                    MessageActivity.addMessage(true, JSONUtils.getMessage(json), JSONUtils.getName(json), currentGroup);
                    cm.sendData(JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display, false));
                    editTextSay.setText("");
                } catch (NullPointerException e) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println("RECONNECTING: 1");
                                ClientMessage.closeSocket();
                                cm = new ClientMessage(con);
                                cm.sendData(JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, msg, display, false));
                                editTextSay.setText("");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

        isLooking = true;
        if (ClientMessage.socket == null || ClientMessage.socket.isConnected() == false) {
            ClientMessage.closeSocket();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        System.out.println("RECONNECTING: 3");
                        cm = new ClientMessage(con);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.start();
        }
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

    public static void addMessage(boolean right, String text, String name, String group) {
        try {
            myAdapter.add(new ChatMessage(right, text, name, false, null, false));

            chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            chatMsg.setAdapter(myAdapter);
        } catch (NullPointerException e) {
            MessageHandler mh = new MessageHandler(group, null, ClientMessage.con);
            mh.saveMessage();
        }
    }

    public void showSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.dialog_locker, null);

        builder.setView(v)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

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
        menu.clear();

        if (GroupActivity.owns.contains(currentGroup)) {
            menu.add(0, 0, 0, "Settings");
        }
        menu.add(1, 1, 1, "Attach");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            if (requestCode == 1) {

                Uri currImageURI = data.getData();

                MessageActivity.myAdapter.add(new ChatMessage(true, "Image", display, true, currImageURI, false));

                MessageActivity.chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                Bitmap bitmap = BitmapFactory.decodeFile(getRealPathFromURI(currImageURI));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                byte[] byte_img_data = baos.toByteArray();
                String encodedImage = Base64.encodeToString(byte_img_data, Base64.DEFAULT);

                cm.sendData(JSONUtils.getJSONMessage(GroupActivity.ID, currentGroup, encodedImage, display, true));
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