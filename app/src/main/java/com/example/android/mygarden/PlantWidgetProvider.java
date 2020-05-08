package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    public static final String TAG = PlantWidgetProvider.class.getSimpleName();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {

        // Set the click handler to open the DetailActivity for plant ID,
        // or the MainActivity if plant ID is invalid
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID)
        {
            Log.d(TAG, "plant dead or no plants, on click launch MainActivity");
            intent = new Intent(context, MainActivity.class);
        }
        else
        {
            // Set on click to open the corresponding detail activity
            Log.d(TAG, "on click launch DetailActivity of plant with id " + plantId);
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        // Create an Intent to launch MainActivity or DetailActivity when clicked
        /* Intent intent = new Intent(context, MainActivity.class); */
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // Update image
        views.setImageViewResource(R.id.widget_plant_image, imgRes);

        // Update plant ID text
        views.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));

        // Widgets allow click handlers to only launch pending intents
        views.setOnClickPendingIntent(R.id.widget_plant_image, pendingIntent);

        // Show/hide the water drop button
        if (showWater) {

            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);
            Log.d(TAG, "button visible, can water the plant with id " + plantId);

            // Add the watering service click handler
            Intent wateringIntent = new Intent(context, PlantWateringService.class);
            wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
            wateringIntent.putExtra(PlantWateringService.EXTRA_PLANT_ID, plantId);
            PendingIntent wateringPendingIntent = PendingIntent.getService(
                    context,
                    0,
                    wateringIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.widget_water_button, wateringPendingIntent);
        }
        else
        {
            views.setViewVisibility(R.id.widget_water_button, View.INVISIBLE);
            Log.d(TAG, "button invisible, can't water the plant with id " + plantId);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action. The service takes care of updating the widgets' UI.
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

