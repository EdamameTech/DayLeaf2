/*

Copyright (C) 2015 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.DayLeaf2;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import android.util.Log;

public class TextEditActivity extends AppCompatActivity {

    private static final String LogTag = "DayLeaf2";

    private EditText mEditText;
    private TextFileInfo mTextFileInfo;
    private final String STATE_TEXTFILEINFO = "textFileInfo";

    private Boolean mTextEdited;    // true when needs to be saved
    private Boolean mBackedUp;  // true once backup file is created
    private final String STATE_TEXTEDITED = "textEdited";
    private final String STATE_BACKEDUP = "backedUp";


    private Boolean mNeedContent;  // true if text box is empty

    private String appDirectory() {
        return Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(LogTag, "onSavedInstanceState()");
        savedInstanceState.putParcelable(STATE_TEXTFILEINFO, mTextFileInfo);
        savedInstanceState.putBoolean(STATE_TEXTEDITED, mTextEdited);
        savedInstanceState.putBoolean(STATE_BACKEDUP, mBackedUp);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LogTag, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);

        if (savedInstanceState == null) {
            mTextFileInfo = new TextFileInfo(appDirectory(), new Date(), getString(R.string.filename_format));
            mTextEdited = false;
            mBackedUp = false;
            mNeedContent = true;
        } else {
            mTextFileInfo = savedInstanceState.getParcelable(STATE_TEXTFILEINFO);
            mTextEdited = savedInstanceState.getBoolean(STATE_TEXTEDITED);
            mBackedUp = savedInstanceState.getBoolean(STATE_BACKEDUP);
            mNeedContent = false;   // mEditText should already have text loaded
        }
    }

    @Override
    protected void onResume() {
        Log.d(LogTag, "onResume()");
        super.onResume();

        mEditText = (EditText) findViewById(R.id.edit_text);
        if (mNeedContent) {
            loadText();
            moveToBottom();
            mNeedContent = false;
        }
        setTitle(mTextFileInfo.filename());
    }

    private void loadText() {
        File file = new File(mTextFileInfo.directory(), mTextFileInfo.filename());
        if (file.exists() && file.canRead()) {
            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                mEditText.setText(stringBuilder.toString());
                bufferedReader.close();
            } catch (IOException e) {
                Log.e(LogTag, "reading file", e);
            }
        } else {
            mEditText.setText(mTextFileInfo.textTemplate(getString(R.string.filename_format), getString(R.string.text_template_format)));
        }
        mBackedUp = false;

        this.invalidateOptionsMenu();

        mTextEdited = false;
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                mTextEdited = true;
            }
        });
    }

    private void saveText() {
        if (mTextEdited)
            try {
                File directory = new File(mTextFileInfo.directory());
                if (!directory.exists() && !directory.mkdir())
                    Log.e(LogTag, "mkdir failed on " + mTextFileInfo.directory());

                File file;
                file = new File(mTextFileInfo.directory(), mTextFileInfo.filename());

                // rename the target file as the back up file
                if (file.exists() && !mBackedUp) {
                    String backup_filename = mTextFileInfo.backup_filename(getString(R.string.filename_format), getString(R.string.backup_filename_format));
                    File backfile = new File(mTextFileInfo.directory(), backup_filename);
                    if (backfile.exists() && !backfile.delete()) {
                        Log.e(LogTag, "deleting " + backup_filename + " failed");
                    }
                    if (!file.renameTo(backfile)) {
                        Log.e(LogTag, "reaname to " + backup_filename + " failed");
                    }
                    mBackedUp = true;
                }

                // write the new content
                FileWriter fileWriter;
                fileWriter = new FileWriter(file);
                fileWriter.write(mEditText.getText().toString());
                fileWriter.close();
                mTextEdited = false;
            } catch (IOException e) {
                Log.e(LogTag, "saving file", e);
            }
    }

    protected void moveToBottom() {
        mEditText.setSelection(mEditText.getText().length());
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

        menu.findItem(R.id.action_edit_previous_date).setEnabled(mTextFileInfo.previousDate(getString(R.string.filename_format)) != null);
        menu.findItem(R.id.action_edit_next_date).setEnabled(mTextFileInfo.nextDate(getString(R.string.filename_format)) != null);
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
            saveText();
            Intent intent;
            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_STREAM, mTextFileInfo.uri());
            intent.putExtra(Intent.EXTRA_TEXT, mEditText.getText());
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_edit_previous_date && mTextFileInfo.previousDate(getString(R.string.filename_format)) != null) {
            saveText();
            mTextFileInfo = new TextFileInfo(appDirectory(), mTextFileInfo.previousDate(getString(R.string.filename_format)), getString(R.string.filename_format));
            saveText();
        }

        if (id == R.id.action_edit_next_date && mTextFileInfo.nextDate(getString(R.string.filename_format)) != null) {
            saveText();
            mTextFileInfo = new TextFileInfo(appDirectory(), mTextFileInfo.nextDate(getString(R.string.filename_format)), getString(R.string.filename_format));
            saveText();
        }

        if (id == R.id.action_edit_today) {
            saveText();
            mTextFileInfo = new TextFileInfo(appDirectory(), new Date(), getString(R.string.filename_format));
            saveText();
            moveToBottom();
        }

        return super.onOptionsItemSelected(item);
    }
}

