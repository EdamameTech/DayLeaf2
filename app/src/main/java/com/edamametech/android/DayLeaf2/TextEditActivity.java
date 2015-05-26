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
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class TextEditActivity extends AppCompatActivity {

    private static final String LogTag = "DayLeaf2";

    private class TextDate {
        private Date mDate;
        private Date mPreviousDate;
        private Date mNextDate;
        private SimpleDateFormat mFileNameFormat;

        TextDate(Date d) {
            mDate = d;
            mFileNameFormat = new SimpleDateFormat(getString(R.string.filename_format));
            mPreviousDate = null;
            mNextDate = null;

            Date current_date;
            current_date = null;
            try {
                current_date = mFileNameFormat.parse(filename());
            } catch (java.text.ParseException e) {
                // we are maybe editing a generic file
            }

            // find dates for files in the same directory just before and after current one
            if (current_date != null) {
                File app_directory;
                app_directory = new File(directory());
                String[] filenames;
                filenames = app_directory.list();
                if (filenames != null && filenames.length > 0) {
                    for (String filename : filenames) {
                        try {
                            Date file_date;
                            file_date = mFileNameFormat.parse(filename);
                            int c;
                            c = file_date.compareTo(current_date);
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
                // TODO: mNextDate should be today when current_date is in the past
            }
        }

        public final String directory() {
            return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getString(R.string.app_name);
        }

        public final String filename() {
            return new SimpleDateFormat(getString(R.string.filename_format), Locale.US).format(mDate);
        }

        public final String backup_filename() {
            return new SimpleDateFormat(getString(R.string.backup_filename_format), Locale.US).format(mDate);
        }

        public String textTemplate() {
            return new SimpleDateFormat(getString(R.string.text_template_format), Locale.US).format(mDate);
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

    private void loadText() {
        File file;
        file = new File(mTextDate.directory(), mTextDate.filename());
        if (file.exists() && file.canRead()) {
            try {
                File backfile;

                // rename the target file as the back up file
                backfile = new File(mTextDate.directory(), mTextDate.backup_filename());
                if (!file.renameTo(backfile)) {
                    backfile = file;    // refer to original file when rename is not successful
                    file = null;
                }

                // read the back up file
                BufferedReader bufferedReader;
                bufferedReader = new BufferedReader(new FileReader(backfile));
                StringBuilder stringBuilder;
                stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append("\n");
                }
                mEditText.setText(stringBuilder.toString());
                bufferedReader.close();
                mTextEdited = false;

                // copy the content into the (now new) target file
                if (file != null) {
                    FileWriter fileWriter;
                    fileWriter = new FileWriter(file);
                    fileWriter.write(stringBuilder.toString());
                    fileWriter.close();
                }
            } catch (IOException e) {
                Log.e(LogTag, e.toString());
            }
        } else {
            mEditText.setText(mTextDate.textTemplate());
            mTextEdited = true;
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
            mTextEdited = false;
        } catch (IOException e) {
            Log.e(LogTag, e.toString());
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

        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
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

        if (mTextEdited) saveText();
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
