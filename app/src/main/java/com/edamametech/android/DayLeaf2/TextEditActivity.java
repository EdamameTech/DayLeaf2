/*

Copyright (C) 2015 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.DayLeaf2;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.edamametech.android.DayLeaf2.R;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class TextEditActivity extends AppCompatActivity {

    private static final String LogTag = "DayLeaf2";

    private class TextDate {
        Date mDate;

        TextDate(Date d) {
            mDate = d;
        }

        public final String directory() {
            return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name);
        }

        public final String filename() {
            return new SimpleDateFormat(getString(R.string.filename_format), Locale.US).format(mDate);
        }

        public String textTemplate() {
            return new SimpleDateFormat(getString(R.string.text_template_format), Locale.US).format(mDate);
        }

    }

    private EditText mEditText;
    private TextDate mTextDate;

    private void loadText() {
        File file;
        file = new File(mTextDate.directory(), mTextDate.filename());
        if (file.exists() && file.canRead()) {
            try {
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder;
                stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                mEditText.setText(stringBuilder.toString());
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(LogTag, e.toString());
            }
        } else {
            mEditText.setText(mTextDate.textTemplate());
        }
    }

    private void saveText() {
        try {
            File appdir;
            appdir = new File(mTextDate.directory());
            if (!appdir.exists() && !appdir.mkdir())
                Log.e(LogTag, "mkdir failed on " + mTextDate.directory());
            File file;
            file = new File(mTextDate.directory(), mTextDate.filename());
            FileWriter fileWriter;
            fileWriter = new FileWriter(file);
            fileWriter.write(mEditText.getText().toString());
            fileWriter.close();
        } catch (IOException e) {
            Log.e(LogTag, e.toString());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTextDate = new TextDate(new Date());
        mEditText = (EditText) findViewById(R.id.edit_text);
        loadText();
        setTitle(mTextDate.filename());

        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveText();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_text_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_send) {
            Log.d(LogTag, "Menu send");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
