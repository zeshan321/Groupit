package com.groupit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class GroupActivity  extends ActionBarActivity {

    public static ListView groupsList;
    public static GroupArrayAdapter myAdapter;
    public static List<String> groups = new ArrayList<String>();
    private static final int TIME_INTERVAL = 2000;
    public static List<String> owns = new ArrayList<String>();
    public static String ID = null;
    public static HashMap<String, String> settings = new HashMap<String, String>();

    private long mBackPressed;

    Context con;
    public static boolean finishedSetup = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_group);

        con = this;

        setup();

        groupsList = (ListView) findViewById(R.id.list_group);
        myAdapter = new GroupArrayAdapter(getApplicationContext(), R.layout.groups_layout);

        loadGroups();

        if (MessageActivity.myAdapter != null) {
            MessageActivity.myAdapter.clear();
        }

        if (finishedSetup == false) {

            finishedSetup = true;

            // Open socket
            ClientMessage.con = this;
            startService(new Intent(this, ClientMessage.class));

            ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");

            query.whereEqualTo("owner", ID);

            query.findInBackground(new FindCallback<ParseObject>() {

                @Override
                public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                    for (ParseObject groups : parseObjects) {
                        if (groups != null) {
                            String post = groups.getString("groupID");
                            String post1 = groups.getObjectId();
                            boolean post2 = groups.getBoolean("locked");
                            String pass = groups.getString("pass");

                            owns.add(post);
                            settings.put(post, post1 + " , " + post2 + " , " + pass);
                        }
                    }
                    if (MessageActivity.con != null) {
                        ActivityCompat.invalidateOptionsMenu((Activity) MessageActivity.con);
                    }
                }
            });
        }

        groupsList.setClickable(true);
        groupsList.setLongClickable(true);
        groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {

                TextView textView = (TextView) arg1.findViewById(R.id.secondLine);
                TextView textView1 = (TextView) arg1.findViewById(R.id.firstLine);

                String group = textView.getText().toString().replace("Code: ", "");
                String name = textView1.getText().toString();

                MessageActivity.groupName = name;
                Intent intent = new Intent(GroupActivity.this, MessageActivity.class);
                startActivity(intent);
                MessageActivity.currentGroup = group;

            }
        });

        groupsList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                TextView textView = (TextView) view.findViewById(R.id.secondLine);
                TextView textView1 = (TextView) view.findViewById(R.id.firstLine);

                final String group = textView.getText().toString().replace("Code: ", "");
                final String name = textView1.getText().toString();

                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View v = inflater.inflate(R.layout.dialog_delete, null);
                builder.setView(v)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                myAdapter.removeChat(position);
                                groupsList.setAdapter(myAdapter);

                                removeGroup(new JSONUtils().getJSOnGroup(name, group));

                                groups.remove(group);

                                ClientMessage.sendData(new JSONUtils().getJSONList());
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

        Button b = (Button) findViewById(R.id.Create);
        b.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View v = inflater.inflate(R.layout.dialog_creategroup, null);

                builder.setView(v)
                        .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText e1 = (EditText) v.findViewById(R.id.CreateName);
                                EditText e2 = (EditText) v.findViewById(R.id.CreateCode);

                                final String es1 = e1.getText().toString();
                                final String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {

                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");
                                    query.whereEqualTo("groupID", es2);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                            if (e == null) {
                                                if (parseObjects.size() > 0) {
                                                    Toast toast = Toast.makeText(con, "Group already exists!", Toast.LENGTH_LONG);
                                                    toast.show();
                                                } else {
                                                    addGroup(es1, es2, true);
                                                    addMessage(es1, "Code: " + es2);

                                                    ParseObject addGroup = new ParseObject("groups");
                                                    addGroup.put("groupID", es2);
                                                    addGroup.put("owner", ID);
                                                    addGroup.put("locked", false);
                                                    addGroup.put("pass", "null");
                                                    addGroup.saveInBackground();

                                                    owns.add(es2);
                                                    settings.put(es2, addGroup.getObjectId() + " , " + "false" + " , " + "null");
                                                }
                                            } else {
                                                Toast toast = Toast.makeText(con, "Oops! Something went wrong.", Toast.LENGTH_LONG);
                                                toast.show();
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.create();
                builder.show();
            }
        });

        Button b1 = (Button) findViewById(R.id.Join);
        b1.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(con);
                LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                final View v = inflater.inflate(R.layout.dialog_joingroup, null);

                builder.setView(v)
                        .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                EditText e1 = (EditText) v.findViewById(R.id.JoinName);
                                EditText e2 = (EditText) v.findViewById(R.id.JoinCode);

                                final String es1 = e1.getText().toString();
                                final String es2 = e2.getText().toString();

                                if (es1.length() > 0 && es2.length() > 0 && es1.startsWith(" ") == false && es2.startsWith(" ") == false) {

                                    if (groupExists(new JSONUtils().getJSOnGroup(es1, es2))) {
                                        Toast toast = Toast.makeText(con, "Group already exists!", Toast.LENGTH_LONG);
                                        toast.show();
                                        return;
                                    }

                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");
                                    query.whereEqualTo("groupID", es2);
                                    query.findInBackground(new FindCallback<ParseObject>() {
                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                            if (e == null) {
                                                if (parseObjects.size() > 0) {
                                                    ParseQuery<ParseObject> query = ParseQuery.getQuery("groups");

                                                    query.whereEqualTo("groupID", es2);

                                                    query.findInBackground(new FindCallback<ParseObject>() {

                                                        @Override
                                                        public void done(List<ParseObject> parseObjects, com.parse.ParseException e) {
                                                            for (ParseObject groups : parseObjects) {
                                                                if (groups != null) {
                                                                    boolean post = groups.getBoolean("locked");
                                                                    String password = groups.getString("pass");

                                                                    if (post == false) {
                                                                        addGroup(es1, es2, true);
                                                                        addMessage(es1, "Code: " + es2);
                                                                    } else {
                                                                        hasPasswrod(password, es1, es2);
                                                                    }
                                                                }
                                                            }
                                                            if (MessageActivity.con != null) {
                                                                ActivityCompat.invalidateOptionsMenu((Activity) MessageActivity.con);
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Toast toast = Toast.makeText(con, "Group not found!", Toast.LENGTH_LONG);
                                                    toast.show();
                                                }
                                            } else {
                                                Toast toast = Toast.makeText(con, "Oops! Something went wrong.", Toast.LENGTH_LONG);
                                                toast.show();
                                            }
                                        }
                                    });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });

                builder.create();
                builder.show();
            }
        });

    }

    public void setup() {
        File file = new File(this.getFilesDir(), "groups");

        if (file.exists() == false) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            addGroup("GroupIt", "0", false);
        }
    }

    public void addGroup(String display, String group, boolean update) {
        File file = new File(this.getFilesDir(), "groups");

        BufferedWriter stream = null;
        try {
            stream = new BufferedWriter(new FileWriter(file, true));
            stream.write(new JSONUtils().getJSOnGroup(display, group) + "\n");
            stream.close();
            groups.add(group);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (update) {
            ClientMessage.sendData(new JSONUtils().getJSONList());
        }
    }

    public void loadGroups() {
        File file = new File(this.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String display = new JSONUtils().getGroupDisplay(line);
                String id = new JSONUtils().getGroupID(line);

                groups.add(id);
                addMessage(display, "Code: " + id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadGroupMem(Context con) {
        File file = new File(con.getFilesDir(), "groups");
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader isr = new InputStreamReader(fis);
        BufferedReader bufferedReader = new BufferedReader(isr);

        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                String id = new JSONUtils().getGroupID(line);
                groups.add(id);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void removeGroup(String lineToRemove) {

        try {

            File inFile = new File(this.getFilesDir(), "groups");

            if (!inFile.isFile()) {
                System.out.println("Parameter is not an existing file");
                return;
            }

            File tempFile = new File(inFile.getAbsolutePath() + ".tmp");

            BufferedReader br = new BufferedReader(new FileReader(inFile));
            PrintWriter pw = new PrintWriter(new FileWriter(tempFile));

            String line = null;

            while ((line = br.readLine()) != null) {

                if (!line.trim().equals(lineToRemove)) {

                    pw.println(line);
                    pw.flush();
                }
            }
            pw.close();
            br.close();

            //Delete the original file
            if (!inFile.delete()) {
                System.out.println("Could not delete file");
                return;
            }

            //Rename the new file to the filename the original file had.
            if (!tempFile.renameTo(inFile))
                System.out.println("Could not rename file");

        }
        catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public boolean groupExists(String json) {
        File file = new File(this.getFilesDir(), "groups");
        try {
            Scanner scanner = new Scanner(file);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if(line.equals(json)) {
                    return true;
                }
            }
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean addMessage(String text, String name) {
        myAdapter.add(new GroupMessage(text, name));

        groupsList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        groupsList.setAdapter(myAdapter);
        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis())
        {
            super.onBackPressed();
            finish();
            return;
        }
        else {
            Toast.makeText(getBaseContext(), "Press again to quit", Toast.LENGTH_SHORT).show();
        }

        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                changeName();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Display name");
        return super.onCreateOptionsMenu(menu);
    }

    public void changeName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View v = inflater.inflate(R.layout.dialog_changename, null);

        EditText et = (EditText) v.findViewById(R.id.ChangeName);
        et.setText(MessageActivity.display);
        builder.setView(v)
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        EditText text = (EditText) v.findViewById(R.id.ChangeName);

                        if (text.getText().toString().length() < 5 && text.getText().toString().startsWith(" ") == false) {
                            Toast.makeText(con, "Display names need to be longer than 5 characters.", Toast.LENGTH_LONG).show();
                            changeName();
                            return;
                        }

                        MessageActivity.display = text.getText().toString().replaceAll("\\s+$", "");

                        NameHandler nh = new NameHandler(text.getText().toString().replaceAll("\\s+$", ""), con);
                        nh.saveName();
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

    public void hasPasswrod(final String password, final String es1, final String es2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(con);
        LayoutInflater inflater = (LayoutInflater) con.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        final View v = inflater.inflate(R.layout.dialog_password, null);

        final EditText et = (EditText) v.findViewById(R.id.GroupPassCode);

        builder.setView(v)
                .setPositiveButton("Join", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String text = et.getText().toString().replaceAll("\\s+$", "");

                        if (text.equals(password)) {
                            addGroup(es1, es2, true);
                            addMessage(es1, "Code: " + es2);
                        } else {
                            Toast.makeText(con, "Incorrect password!", Toast.LENGTH_LONG).show();
                            hasPasswrod(password, es1, es2);
                        }
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
}
