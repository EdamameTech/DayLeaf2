/*

Copyright (C) 2014 Green Soybean Technologies, LLC
<edamametech@gmail.com>

This program is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as
published by the Free Software Foundation, either version 3 of
the License, or (at your option) any later version.

*/

package com.edamametech.android.DayLeaf2;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class DayLeaf2WidgetProvider extends AppWidgetProvider {
    Intent intent;
    Integer layout;
    DayLeaf2Util.FilePath path;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int NWidgets = appWidgetIds.length;
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        RemoteViews views = new RemoteViews(context.getPackageName(), layout);
        for (int i = 0; i < NWidgets; i++) {
            int appWidgetId = appWidgetIds[i];
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
            views.setTextViewText(R.id.widget_label, path.filename());
            views.setOnClickPendingIntent(R.id.widget_label, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
