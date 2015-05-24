/*

Copyright (C) 2014 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

*/

package com.edamametech.android.DayLeaf2;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Environment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DayLeaf2Util {
    @SuppressLint("SimpleDateFormat")
    static public class FilePath {
        private final String filenameFormat = "yyMMdd'.txt'";
        private final SimpleDateFormat filenameFormatter = new SimpleDateFormat(filenameFormat,
                Locale.US);
        Date date;

        FilePath(Date d) {
            date = d;
        }

        String directory() {
            return Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS).toString();
        }

        String filename() {
            return filenameFormatter.format(date);
        }

        String directoryName() {
            String[] directoryNameAry = directory().split("/");
            return directoryNameAry[directoryNameAry.length - 1];
        }

        Uri uri() {
            return Uri.parse("file://" + directory() + "/" + filename());
        }
    }

}
