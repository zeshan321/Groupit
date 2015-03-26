package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.NfcF;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;
import java.util.UUID;

public class MessageActivity extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback {

    public static ListView chatMsg;
    public static ChatArrayAdapter myAdapter;
    public static String display;
    public static Context con;
    public static String currentGroup = "0";
    public static boolean isLooking = false;
    public static String groupName = null;

    EditText editTextSay;
    ImageButton buttonSend;
    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] mIntentFilters;
    String[][] mNFCTechLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView tv = (TextView) findViewById(R.id.groupTitle);
        tv.setText(groupName);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter != null) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            mPendingIntent = PendingIntent.getActivity(this, 0,
                    new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

            IntentFilter ndefIntent = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
            try {
                ndefIntent.addDataType("*/*");
                mIntentFilters = new IntentFilter[] { ndefIntent };
            } catch (Exception e) {
                e.printStackTrace();
            }

            mNFCTechLists = new String[][] { new String[] { NfcF.class.getName() } };
        }

        isLooking = true;
        con = this;
        ClientMessage.con = this;

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

                json = new JSONUtils().getJSONMessage(GroupActivity.ID, currentGroup, msg, display, false);
                MessageActivity.addMessage(true, new JSONUtils().getMessage(json), new JSONUtils().getName(json), currentGroup);
                ClientMessage.sendData(new JSONUtils().getJSONMessage(GroupActivity.ID, currentGroup, msg, display, false));
                editTextSay.setText("");
            }
        });

        chatMsg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View v = inflater.inflate(R.layout.dialog_delete, null);

                TextView tv = (TextView) v.findViewById(R.id.dialog);
                tv.setText("Delete Message");
                builder.setView(v)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                myAdapter.removeChat(position);
                                chatMsg.setAdapter(myAdapter);

                                new MessageHandler(currentGroup, null, con).removeGroup(position);
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
                return true;
            }
        });
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        NdefMessage msg = null;

        if (isLooking) {
            String text = (new JSONUtils().nfcGroup(groupName, currentGroup));
            msg = new NdefMessage(
                    new NdefRecord[]{
                            NdefRecord.createMime(
                                    "application/com.groupit",
                                    text.getBytes())
                    });
        }

        return msg;
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(
                    NfcAdapter.EXTRA_NDEF_MESSAGES);

            NdefMessage message = (NdefMessage) rawMessages[0];
            String s = new String(message.getRecords()[0].getPayload());

            if (!new GroupHandler(con).groupExists(new JSONUtils().getJSOnGroup(new JSONUtils().nfcGetDisplay(s), new JSONUtils().nfcGetID(s)))) {
                new GroupHandler(con).addGroup(new JSONUtils().nfcGetDisplay(s), new JSONUtils().nfcGetID(s), false);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isLooking = true;

        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mNFCTechLists);

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
        final String[] settings = GroupActivity.settings.get(currentGroup).split(" , ");

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

                                GroupActivity.settings.put(currentGroup, settings[0] + " , " + p1 + " , " + p2.replaceAll("\\s+$", ""));
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
                overridePendingTransition(R.transition.activity_from_1, R.transition.activity_from_2);
                return true;
            case 0:
                showSettings();
                return true;
            case 1:
                CharSequence colors[] = new CharSequence[] {"Photo", "Video", "Audio"};

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Attach");
                builder.setItems(colors, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent inte = new Intent();
                                inte.setType("image/*");
                                inte.setAction(Intent.ACTION_GET_CONTENT);
                                ((Activity)con).startActivityForResult(Intent.createChooser(inte, "Select Picture"), 1);
                                break;
                        }
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        overridePendingTransition(R.transition.activity_from_1, R.transition.activity_from_2);
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

                File file = new File(getRealPathFromURI(currImageURI));
                String ID = UUID.randomUUID().toString();

                FTPHandler ftp = new FTPHandler(ID, FTPHandler.Type.Image, file, con, true);
                ftp.uploadFile();
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