package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.tech.NfcF;
import android.os.Build;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

public class MessageActivity extends ActionBarActivity implements NfcAdapter.CreateNdefMessageCallback {

    public static ListView chatMsg;
    public static ChatArrayAdapter myAdapter;
    public static String display;
    public static Context con;
    public static String currentGroup = "0";
    public static boolean isLooking = false;
    public static String groupName = null;
    public static int CURRENT = 0;
    public static View lastView;
    public static int temp = 0;

    EditText editTextSay;
    ImageButton buttonSend;
    NfcAdapter mNfcAdapter;
    PendingIntent mPendingIntent;
    IntentFilter[] mIntentFilters;
    String[][] mNFCTechLists;
    Boolean userTouched = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        MessageService.count.remove(groupName);
        CURRENT = 0;
        temp = 0;

        // Remove open notifications
        if (groupName != null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(GroupActivity.groups.indexOf(groupName));
        }

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
        MessageService.con = this;

        myAdapter = new ChatArrayAdapter(this, R.layout.list_item_message_left);
        chatMsg = (ListView) findViewById(R.id.list_view_messages);

        editTextSay = (EditText) findViewById(R.id.say);
        buttonSend = (ImageButton) findViewById(R.id.send);

        buttonSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                final String msg = editTextSay.getText().toString();
                String json = null;
                if (msg.equals("")) {
                    return;
                }
                Timestamp ts = new Timestamp(new Date().getTime());

                json = new JSONUtils().getJSONMessage(ts, GroupActivity.ID, currentGroup, msg, display, false);
                MessageActivity.addMessage(true, new JSONUtils().getMessage(json), new JSONUtils().getName(json), currentGroup, ts, json);
                MessageService.sendData(currentGroup, new JSONUtils().getJSONMessage(ts, GroupActivity.ID, currentGroup, msg, display, false));

                if (msg.equalsIgnoreCase("Dev mode: on")) {
                    new VoiceChat(con).startStreaming();
                }

                editTextSay.setText("");
            }
        });

        chatMsg.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
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

                                TextView json = (TextView) view.findViewById(R.id.jsonMsg);

                                DatabaseHandler db = new DatabaseHandler(con);
                                db.deleteMessage(json.getText().toString());
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

        chatMsg.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                if (arg1.findViewById(R.id.filePath) != null) {
                    TextView image = (TextView) arg1.findViewById(R.id.filePath);

                    Intent intent = new Intent(con, ImageActivity.class);
                    intent.putExtra("image", image.getText().toString());
                    startActivity(intent);
                }

            }
        });

        chatMsg.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if(scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    userTouched = true;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                if (firstVisibleItem == 0) {
                    View v = chatMsg.getChildAt(0);
                    int offset = (v == null) ? 0 : v.getTop();
                    if (offset == 0) {
                        DatabaseHandler db = new DatabaseHandler(con);
                        int count = db.getCount() - temp;
                        if (count == chatMsg.getCount() || chatMsg.getCount() > count) {
                            if (myAdapter.getCount() != 0)
                            new UserData(con).sendToast("No more messages!");
                            return;
                        }

                        MessageActivity.myAdapter.clear();
                        MessageActivity.chatMsg.setAdapter(MessageActivity.myAdapter);

                        CURRENT = CURRENT + 20;
                        for (String s : db.getMessages(CURRENT)) {
                            if (s != null)
                            new MessageHandler(con).addMSG(s);
                        }

                    }
                }
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

            if (!new GroupHandler(con).groupCodeExists(new JSONUtils().nfcGetID(s))) {
                new GroupHandler(con).addGroup(new JSONUtils().nfcGetDisplay(s), new JSONUtils().nfcGetID(s));
                new UserData(con).updateGroups();
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

    public static void addMessage(boolean right, String text, String name, String group, Timestamp ts, String json) {
        try {
            if (myAdapter.getCount() > 0) {
                String ID1 = new JSONUtils().getID(json);
                String ID2 = new JSONUtils().getID(myAdapter.getItem(myAdapter.getCount() - 1).json);

                if (ID1.equals(ID2) && !myAdapter.getItem(myAdapter.getCount() - 1).image) {
                    String pre = myAdapter.getItem(myAdapter.getCount() - 1).message;
                    String time = new Time(myAdapter.getItem(myAdapter.getCount() - 1).time.getTime()).getString();

                    pre = pre + "<br><font size=\"2\" color=\"#d7d7d7\"> &#9472;&#9472;&#9472; </font><br>" + text;
                    myAdapter.set(myAdapter.getCount() - 1, new ChatMessage(right, pre, name, false, null, false, ts, json));
                    chatMsg.setAdapter(myAdapter);
                    temp++;
                    return;
                }
            }

            myAdapter.add(new ChatMessage(right, text, name, false, null, false, ts, json));

            chatMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            chatMsg.setAdapter(myAdapter);
        } catch (NullPointerException e) {
            e.printStackTrace();
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
                                new UserData(con).sendToast("Password needs to be greater then 1 character.");
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
                CharSequence colors[] = new CharSequence[] {"Photo"};

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
                            case 1:
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType("*/*");
                                intent.addCategory(Intent.CATEGORY_OPENABLE);
                                ((Activity)con).startActivityForResult(
                                        Intent.createChooser(intent, "Select a File to Upload"),
                                        2);
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
            Uri currImageURI;
            File file;
            switch (requestCode) {
                case 1:
                    currImageURI = data.getData();

                    file = new File(new FilePath(this, currImageURI).getPath());
                    String ID = UUID.randomUUID().toString();

                    FTPHandler ftp = new FTPHandler(ID, FTPHandler.Type.Image, file, con, true);
                    ftp.uploadFile();
                    break;
                case 2:
                    currImageURI = data.getData();

                    file = new File(new FilePath(this, currImageURI).getPath());
                    System.out.println("File Uri: " + file.getPath());
                    break;
            }
        }
    }
}