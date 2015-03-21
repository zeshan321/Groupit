package com.groupit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
    Context con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        con = this;
        new GroupActivity().ID = new UserData(con).getID();

        NameHandler nh = new NameHandler(null, con);

        if (nh.getName() != null) {
            new MessageActivity().display = nh.getName();
            startActivity(new Intent(MainActivity.this, GroupActivity.class));
            return;
        }

        firstButton(findViewById(R.id.button));

    }

    public void firstButton(View v) {

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText text = (EditText) findViewById(R.id.editText);

                if (text.getText().toString().length() < 5 && text.getText().toString().startsWith(" ") == false) {
                    Toast.makeText(MainActivity.this, "Display name needs to be greater then 5 characters.", Toast.LENGTH_LONG).show();
                    return;
                }

                new MessageActivity().display = text.getText().toString().replaceAll("\\s+$", "");

                NameHandler nh = new NameHandler(text.getText().toString().replaceAll("\\s+$", ""), con);
                nh.saveName();

                startActivity(new Intent(MainActivity.this, GroupActivity.class));

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}