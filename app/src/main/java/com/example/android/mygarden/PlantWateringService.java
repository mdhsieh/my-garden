package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class PlantWateringService extends IntentService {

    public static final String TAG = PlantWateringService.class.getSimpleName();

    public static final String ACTION_WATER_PLANTS =
            "com.example.android.mygarden.action.water_plants";
    // TODO (3): Create a new action ACTION_UPDATE_PLANT_WIDGETS to handle updating widget UI and
    // implement handleActionUpdatePlantWidgets to query the plant closest to dying and call
    // updatePlantWidgets to refresh widgets
    public static final String ACTION_UPDATE_PLANT_WIDGET =
            "com.example.android.mygarden.action.update_plant_widget";

    public PlantWateringService() {
        super(PlantWateringService.class.getSimpleName());
    }

    /**
     * Starts this service to perform WaterPlants action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionWaterPlants(Context context) {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_WATER_PLANTS);
        context.startService(intent);
    }

    public static void startActionUpdatePlantWidget(Context context)
    {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGET);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_WATER_PLANTS.equals(action)) {
                handleActionWaterPlants();
            }
            else if (ACTION_UPDATE_PLANT_WIDGET.equals(action))
            {
                handleActionUpdatePlantWidget();
            }
        }
    }

    /**
     * Handle action WaterPlant in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlants() {
        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only plants that are still alive
        getContentResolver().update(
                PLANTS_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME+">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)});
        Log.d(TAG, "handled action water plants");
    }

    private void handleActionUpdatePlantWidget()
    {
        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANTS_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        int plantImageRes = R.drawable.grass;

        // get the plant closest to dying
        if (cursor != null && cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            int createdAtIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int wateredAtIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int typeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            // get info of the plant
            long createdAt = cursor.getLong(createdAtIndex);
            long wateredAt = cursor.getLong(wateredAtIndex);
            int type = cursor.getInt(typeIndex);

            long timeNow = System.currentTimeMillis();

            /*Log.d(TAG, "created at: " + PlantUtils.getDisplayAgeInt(createdAt));
            Log.d(TAG, "watered at: " + PlantUtils.getDisplayAgeInt(wateredAt));
            Log.d(TAG, "type: " + PlantUtils.getPlantTypeName(this, type));

            Log.d(TAG, "plant age: " + PlantUtils.getDisplayAgeInt(timeNow - createdAt));*/

            plantImageRes = PlantUtils.getPlantImageRes(
                    this, timeNow - createdAt, wateredAt, type);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName name = new ComponentName(this, PlantWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(name);

            // update all widgets
            PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager, plantImageRes, appWidgetIds);

            cursor.close();
        }
    }
}
