package net.idoun.simplebanner;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.banner_appwidget);
            views.setOnClickPendingIntent(R.id.background, pendingIntent);

            if (prefs.contains(EditActivity.PREF_USER_TEXT)) {
                String text = prefs.getString(EditActivity.PREF_USER_TEXT, "");

                if (!text.isEmpty()) {
                    views.setTextViewText(R.id.text, text);
                }
            }

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
