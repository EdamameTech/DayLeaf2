/*

Copyright (C) 2014 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

*/

package com.edamametech.android.DayLeaf2;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

public class DayLeaf2SendWidgetProvider extends DayLeaf2WidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        path = new DayLeaf2Util.FilePath(new Date());
        File outputFile;
        outputFile = new File(path.directory(), path.filename());
        Log.d("DayLeaf2", outputFile.toString());
        if (!outputFile.exists()) {
            try {
                FileWriter fileWriter;
                Log.d("DayLeaf2", "Creating file");
                fileWriter = new FileWriter(outputFile, false);
                fileWriter.write("Hello World\n");
                fileWriter.close();
                Log.d("DayLeaf2", "Created file");
            }catch(java.io.IOException e) {
                Log.d("DayLeaf2", e.toString());
                Toast.makeText(context, "Could not create " + path, Toast.LENGTH_LONG);
            }
        } else {
            Log.d("DayLeaf2", "File exists");
        }
        intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_STREAM, path.uri());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        layout = R.layout.send;
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
