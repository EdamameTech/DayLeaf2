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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

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
        private String mFilename;
        private String mBackupFilename;
        private String mTextTemplate;
        private Date mPreviousDate;
        private Date mNextDate;

        TextDate(Date d) {
            SimpleDateFormat filenameFormat;
            filenameFormat = new SimpleDateFormat(getString(R.string.filename_format));
            mFilename = filenameFormat.format(d);
            mBackupFilename = new SimpleDateFormat(getString(R.string.backup_filename_format), Locale.US).format(d);
            mTextTemplate = new SimpleDateFormat(getString(R.string.text_template_format), Locale.US).format(d);
            mPreviousDate = null;
            mNextDate = null;

            Date editing_file_date = null;
            try {
                editing_file_date = filenameFormat.parse(mFilename);
            } catch (java.text.ParseException e) {
                // we are maybe editing a generic file
            }

            Date today_date = null;
            try {
                today_date = filenameFormat.parse(filenameFormat.format(new Date()));
            } catch (java.text.ParseException e) {
                Log.e(LogTag, "Cannot detect today from filename", e);
            }

            // find dates for files in the same directory just before and after current one
            if (editing_file_date != null) {
                File app_directory;
                app_directory = new File(directory());
                String[] filenames;
                filenames = app_directory.list();
                if (filenames != null && filenames.length > 0) {
                    for (String filename : filenames) {
                        try {
                            Date file_date;
                            file_date = filenameFormat.parse(filename);
                            int c;
                            c = file_date.compareTo(editing_file_date);
                            if (c > 0) {
                                if (mNextDate == null) {
                                    mNextDate = file_date;
                                } else if (file_date.compareTo(mNextDate) < 0) {
                                    mNextDate = file_date;
                                }
                            } else if (c < 0) {
                                if (mPreviousDate == null) {
                                    mPreviousDate = file_date;
                                } else {
                                    if (file_date.compareTo(mPreviousDate) > 0) {
                                        mPreviousDate = file_date;
                                    }
                                }
                            }
                        } catch (java.text.ParseException e) {
                            // ignore the file
                        }
                    }
                }

                if (mNextDate == null && today_date != null && editing_file_date.compareTo(today_date) < 0) {
                    mNextDate = today_date;
                }
            }
        }

        public final String directory() {
            return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name);
        }

        public final String filename() {
            return mFilename;
        }

        public final String backup_filename() {
            return mBackupFilename;
        }

        public String textTemplate() {
            return mTextTemplate;
        }

        public Uri uri() {
            return Uri.parse("file://" + directory() + "/" + filename());
        }

        // Date with existing file or null
        public Date previousDate() {
            return mPreviousDate;
        }

        public Date nextDate() {
            return mNextDate;
        }
    }

    private EditText mEditText;
    boolean mTextEdited;    // true when needs to be saved
    private TextDate mTextDate;
    private Boolean mBackedUp;  // true once backup file is created

    private void loadText() {
        File file = new File(mTextDate.directory(), mTextDate.filename());
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
                mTextEdited = false;
            } catch (IOException e) {
                Log.e(LogTag, "reading file", e);
            }
        } else {
            mEditText.setText(mTextDate.textTemplate());
            mTextEdited = true;
        }
    }

    private void saveText() {
        if (mTextEdited)
            try {
                File appdir;
                appdir = new File(mTextDate.directory());
                if (!appdir.exists() && !appdir.mkdir())
                    Log.e(LogTag, "mkdir failed on " + mTextDate.directory());

                File file;
                file = new File(mTextDate.directory(), mTextDate.filename());

                // rename the target file as the back up file
                if (!mBackedUp) {
                    File backfile;
                    backfile = new File(mTextDate.directory(), mTextDate.backup_filename());
                    if (backfile.exists() && !backfile.delete()) {
                        Log.e(LogTag, "deleting " + mTextDate.backup_filename() + " failed");
                    }
                    if (!file.renameTo(backfile)) {
                        Log.e(LogTag, "reaname to " + mTextDate.backup_filename() + " failed");
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

    protected void loadContent(Date date) {
        mTextDate = new TextDate(date);
        mEditText = (EditText) findViewById(R.id.edit_text);
        setTitle(mTextDate.filename());
        this.invalidateOptionsMenu();

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

        loadText();
        mBackedUp = false;

        mEditText.setSelection(mEditText.getText().length());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_edit);
        loadContent(new Date());
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

        menu.findItem(R.id.action_edit_previous_date).setEnabled(mTextDate.previousDate() != null);
        menu.findItem(R.id.action_edit_next_date).setEnabled(mTextDate.nextDate() != null);
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
            intent.putExtra(Intent.EXTRA_STREAM, mTextDate.uri());
            intent.putExtra(Intent.EXTRA_TEXT, mEditText.getText());
            startActivity(intent);
            return true;
        }

        if (id == R.id.action_edit_previous_date && mTextDate.previousDate() != null) {
            saveText();
            loadContent(mTextDate.previousDate());
        }

        if (id == R.id.action_edit_next_date && mTextDate.nextDate() != null) {
            saveText();
            loadContent(mTextDate.nextDate());
        }

        if (id == R.id.action_edit_today) {
            saveText();
            loadContent(new Date());
        }

        return super.onOptionsItemSelected(item);
    }
}

