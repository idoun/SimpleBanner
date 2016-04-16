/*
    Copyright 2016 idoun

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package net.idoun.simplebanner;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.widget.RemoteViews;

public class SimpleBannerAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (EditActivity.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName provider = new ComponentName(context, SimpleBannerAppWidgetProvider.class);
            int[] ids = appWidgetManager.getAppWidgetIds(provider);
            onUpdate(context, appWidgetManager, ids);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, EditActivity.class);
            intent.putExtra(EditActivity.EXTRA_WIDGET_ID, appWidgetId);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.banner_appwidget);
            views.setOnClickPendingIntent(R.id.background, pendingIntent);

            String widgetKey = EditActivity.PREF_USER_TEXT + appWidgetId;

            if (prefs.contains(widgetKey)) {
                String text = prefs.getString(widgetKey, "");

                if (!text.isEmpty()) {
                    views.setTextViewText(R.id.text, text);
                }
            }

            String fontSizeKey = EditActivity.PREF_USER_TEXT_SIZE + appWidgetId;
            if (prefs.contains(fontSizeKey)) {
                int textSize = prefs.getInt(fontSizeKey, EditActivity.DEFAULT_FONT_SIZE);

                views.setTextViewTextSize(R.id.text, TypedValue.COMPLEX_UNIT_SP, textSize);
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();

        for (int appWidgetId : appWidgetIds) {
            String widgetKey = EditActivity.PREF_USER_TEXT + appWidgetId;

            if (prefs.contains(widgetKey)) {
                editor.remove(widgetKey);
            }
        }

        editor.apply();
    }
}
