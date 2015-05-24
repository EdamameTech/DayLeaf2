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

import java.util.Date;

public class DayLeaf2EditWidgetProvider extends DayLeaf2WidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        path = new DayLeaf2Util.FilePath(new Date());
        intent = new Intent(Intent.ACTION_EDIT);
        intent.setDataAndType(path.uri(), "text/plain");
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        layout = R.layout.edit;
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
