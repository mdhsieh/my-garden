package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.ui.GridWidgetService;
import com.example.android.mygarden.ui.MainActivity;
import com.example.android.mygarden.ui.PlantDetailActivity;

/**
 * Implementation of App Widget functionality.
 */
public class PlantWidgetProvider extends AppWidgetProvider {

    public static final String TAG = PlantWidgetProvider.class.getSimpleName();

    public static final int SINGLE_PLANT_WIDTH = 300;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {

        // TODO (4): separate the updateAppWidget logic into getGardenGridRemoteView and getSinglePlantRemoteView
        // TODO (5): Use getAppWidgetOptions to get widget width and use the appropriate RemoteView method
        // TODO (6): Set the PendingIntent template in getGardenGridRemoteView to launch PlantDetailActivity

        Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidth = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        // change the view to a single plant or grid view depending on the widget's size
        RemoteViews remoteViews;
        if (minWidth < SINGLE_PLANT_WIDTH)
        {
            remoteViews = getSinglePlantRemoteViews(context, imgRes, plantId, showWater);
        }
        else
        {
            remoteViews = getGardenGridRemoteViews(context);
        }

        /*
        // Set the click handler to open the DetailActivity for plant ID,
        // or the MainActivity if plant ID is invalid
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID)
        {
            intent = new Intent(context, MainActivity.class);
        }
        else
        {
            // Set on click to open the corresponding detail activity
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        // Create an Intent to launch MainActivity or DetailActivity when clicked
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
        }
        */

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    private static RemoteViews getSinglePlantRemoteViews(Context context, int imgRes, long plantId, boolean showWater)
    {
        // Set the click handler to open the DetailActivity for plant ID,
        // or the MainActivity if plant ID is invalid
        Intent intent;
        if (plantId == PlantContract.INVALID_PLANT_ID)
        {
            intent = new Intent(context, MainActivity.class);
        }
        else
        {
            // Set on click to open the corresponding detail activity
            intent = new Intent(context, PlantDetailActivity.class);
            intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, plantId);
        }

        // Create an Intent to launch MainActivity or DetailActivity when clicked
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
        }

        return views;
    }

    private static RemoteViews getGardenGridRemoteViews(Context context)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        // set intent to act as an adapter to the GridView
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.garden_grid_view, intent);

        // set the PlantDetailActivity to launch when clicked
        Intent appIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.garden_grid_view, appPendingIntent);

        // handle empty gardens
        views.setEmptyView(R.id.garden_grid_view, R.id.empty_view);

        return views;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action. The service takes care of updating the widgets' UI.
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        // update widgets when a widget size changes
        PlantWateringService.startActionUpdatePlantWidgets(context);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
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

