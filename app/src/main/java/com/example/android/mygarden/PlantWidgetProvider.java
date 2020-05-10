package com.example.android.mygarden;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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

    // when a widget's width in dps is larger than this, display a GridView,
    // otherwise display single plant
    public static final int SINGLE_PLANT_WIDTH = 300;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int imgRes, long plantId, boolean showWater, int appWidgetId) {

        // Get current width to decide on single plant vs garden grid view
        Bundle widgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidth = widgetOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);

        // change the view to a single plant or grid view depending on the widget's current width
        RemoteViews remoteViews;
        if (minWidth < SINGLE_PLANT_WIDTH)
        {
            remoteViews = getSinglePlantRemoteViews(context, imgRes, plantId, showWater);
        }
        else
        {
            remoteViews = getGardenGridRemoteViews(context);
        }

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Start the intent service update widget action. The service takes care of updating the widgets' UI.
        PlantWateringService.startActionUpdatePlantWidgets(context);
    }

    /**
     * Updates all widget instances given the widget Ids and display information
     *
     * @param context          The calling context
     * @param appWidgetManager The widget manager
     * @param imgRes           The image resource for single plant mode
     * @param plantId          The database ID for that plant
     * @param showWater        Boolean to show/hide water drop button
     * @param appWidgetIds     Array of widget Ids to be updated
     */

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static void updatePlantWidgets(Context context, AppWidgetManager appWidgetManager,
                                          int imgRes, long plantId, boolean showWater, int[] appWidgetIds)
    {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, imgRes, plantId, showWater, appWidgetId);
        }
    }

    /**
     * Creates and returns the RemoteViews to be displayed in the single plant mode widget
     *
     * @param context   The context
     * @param imgRes    The image resource of the plant image to be displayed
     * @param plantId   The database plant Id for watering button functionality
     * @param showWater Boolean to either show/hide the water drop
     * @return The RemoteViews for the single plant mode widget
     */
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
        if (showWater)
        {
            views.setViewVisibility(R.id.widget_water_button, View.VISIBLE);

            // Add the watering service click handler
            Intent wateringIntent = new Intent(context, PlantWateringService.class);
            wateringIntent.setAction(PlantWateringService.ACTION_WATER_PLANT);
            // Add the plant ID as extra to water only that plant when clicked
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

    /**
     * Creates and returns the RemoteViews to be displayed in the GridView mode widget
     *
     * @param context The context
     * @return The RemoteViews for the GridView mode widget
     */
    private static RemoteViews getGardenGridRemoteViews(Context context)
    {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_grid_view);

        // Set the GridWidgetService intent to act as the adapter for the GridView
        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.garden_grid_view, intent);

        // Set the PlantDetailActivity to launch when clicked
        Intent appIntent = new Intent(context, PlantDetailActivity.class);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.garden_grid_view, appPendingIntent);

        // Handle empty gardens
        views.setEmptyView(R.id.garden_grid_view, R.id.empty_view);

        return views;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
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

