/*

Copyright (C) 2015 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

 */

package com.edamametech.android.DayLeaf2;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextFileInfo implements Parcelable {
    private String mFilename;
    private String mDirname;
    private Date mPreviousDate;
    private Date mNextDate;
    private Boolean mScannedFiles;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDirname);
        dest.writeString(mFilename);
    }

    public static final Parcelable.Creator<TextFileInfo> CREATOR = new Parcelable.Creator<TextFileInfo>() {
        public TextFileInfo createFromParcel(Parcel src) {
            return new TextFileInfo(src.readString(), src.readString());
        }

        public TextFileInfo[] newArray(int size) {
            return new TextFileInfo[size];
        }
    };

    TextFileInfo(String dirname, Date d, String filename_format) {
        SimpleDateFormat formatter = new SimpleDateFormat(filename_format);
        mDirname = dirname;
        mFilename = formatter.format(d);
        mPreviousDate = null;
        mNextDate = null;
        mScannedFiles = false;
    }

    TextFileInfo(String dirname, String filename) {
        mDirname = dirname;
        mFilename = filename;
        mPreviousDate = null;
        mNextDate = null;
        mScannedFiles = false;
    }

    public String directory() {
        return mDirname;
    }

    public String filename() {
        return mFilename;
    }

    public Uri uri() {
        return Uri.parse("file://" + directory() + "/" + filename());
    }

    private Date parseFilename(String filename_format) {
        try {
            return new SimpleDateFormat(filename_format).parse(mFilename);
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    public String backup_filename(String filename_format, String backup_filename_format) {
        Date date = parseFilename(filename_format);
        if (date == null) return null;
        return new SimpleDateFormat(backup_filename_format, Locale.US).format(date);
    }

    public String textTemplate(String filename_format, String text_template) {
        Date date = parseFilename(filename_format);
        if (date == null) return "";
        return new SimpleDateFormat(text_template, Locale.US).format(date);
    }

    private void scanFiles(String filename_format) {
        mScannedFiles = true;
        Date date = parseFilename(filename_format);
        if (date == null) return;

        File directory = new File(mDirname);
        String[] filenames = directory.list();
        if (filenames == null) return;
        for (String filename : filenames) {
            Date file_date = parseFilename(filename_format);
            if (file_date != null) {
                int c = file_date.compareTo(date);
                if (c > 0) {
                    if (mNextDate == null) {
                        mNextDate = file_date;
                    } else if (file_date.compareTo(mNextDate) < 0) {
                        mNextDate = file_date;
                    }
                } else if (c < 0) {
                    if (mPreviousDate == null) {
                        mPreviousDate = file_date;
                    } else if (file_date.compareTo(mPreviousDate) > 0) {
                        mPreviousDate = file_date;
                    }
                }
            }
        }

        if (mNextDate == null) {
            try {
                SimpleDateFormat formatter = new SimpleDateFormat(filename_format, Locale.US);
                Date today = formatter.parse(formatter.format(new Date()));
                if (date.compareTo(today) < 0) {
                    mNextDate = today;
                }
            } catch (java.text.ParseException e) {
                // give up
            }
        }
    }

    public Date previousDate(String filename_format) {
        if (!mScannedFiles) scanFiles(filename_format);
        return mPreviousDate;
    }

    public Date nextDate(String filename_format) {
        if (!mScannedFiles) scanFiles(filename_format);
        return mNextDate;
    }
}
