package com.example.android.mygarden;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentUris;
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

    public static final String ACTION_WATER_PLANT =
            "com.example.android.mygarden.action.water_plants";
    public static final String ACTION_UPDATE_PLANT_WIDGETS =
            "com.example.android.mygarden.action.update_plant_widgets";

    public static final String EXTRA_PLANT_ID = "plant_id";

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
        intent.setAction(ACTION_WATER_PLANT);
        context.startService(intent);
    }

    /**
     * Starts this service to perform UpdatePlantWidgets action with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionUpdatePlantWidgets(Context context)
    {
        Intent intent = new Intent(context, PlantWateringService.class);
        intent.setAction(ACTION_UPDATE_PLANT_WIDGETS);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();

            final long plantId = intent.getLongExtra(EXTRA_PLANT_ID, PlantContract.INVALID_PLANT_ID);
            Log.d(TAG, "id from intent is: " + plantId);

            if (ACTION_WATER_PLANT.equals(action)) {
                handleActionWaterPlants(plantId);
            }
            else if (ACTION_UPDATE_PLANT_WIDGETS.equals(action))
            {
                handleActionUpdatePlantWidgets();
            }
        }
    }

    /**
     * Handle action WaterPlant in the provided background thread with the provided
     * parameters.
     */
    private void handleActionWaterPlants(long plantId) {
        /*Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();*/
        Uri SINGLE_PLANT_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build(), plantId);
        Log.d(TAG, "single plant content uri is: " + SINGLE_PLANT_URI.toString());
        ContentValues contentValues = new ContentValues();
        long timeNow = System.currentTimeMillis();
        contentValues.put(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME, timeNow);
        // Update only plants that are still alive
        /* getContentResolver().update(
                PLANTS_URI,
                contentValues,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME + ">?",
                new String[]{String.valueOf(timeNow - PlantUtils.MAX_AGE_WITHOUT_WATER)}); */

        // update only the plant that needs watering the most

        // check if already dead then can't water
        Cursor cursor = getContentResolver().query(SINGLE_PLANT_URI, null, null, null, null);
        if (cursor == null || cursor.getCount() < 1)
            return; //can't find this plant!
        cursor.moveToFirst();
        long lastWatered = cursor.getLong(cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME));
        if ((timeNow - lastWatered) > PlantUtils.MAX_AGE_WITHOUT_WATER)
            return; // plant already dead

        getContentResolver().update(
                SINGLE_PLANT_URI,
                contentValues,
                null,
                null);

        Log.d(TAG, "updated water timestamp on plant with id " + plantId);

        cursor.close();
    }

    /**
     * Handle action UpdatePlantWidgets in the provided background thread
     */
    private void handleActionUpdatePlantWidgets()
    {
        // Query to get the plant that's most in need for water (last watered)
        Uri PLANT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();
        Cursor cursor = getContentResolver().query(
                PLANT_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);

        // Default image in case our garden is empty
        int plantImageRes = R.drawable.grass;

        // Extract the plant details
        if (cursor != null && cursor.getCount() > 0)
        {
            cursor.moveToFirst();

            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            long timeNow = System.currentTimeMillis();

            // details of the plant
            long createdAt = cursor.getLong(createTimeIndex);
            long wateredAt = cursor.getLong(waterTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);

            /*Log.d(TAG, "created at: " + PlantUtils.getDisplayAgeInt(createdAt));
            Log.d(TAG, "watered at: " + PlantUtils.getDisplayAgeInt(wateredAt));
            Log.d(TAG, "type: " + PlantUtils.getPlantTypeName(this, plantType));

            Log.d(TAG, "plant age: " + PlantUtils.getDisplayAgeInt(timeNow - createdAt));
            Log.d(TAG, "water age: " + PlantUtils.getDisplayAgeInt(timeNow - wateredAt));*/


            int plantIdIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
            long plantId = cursor.getLong(plantIdIndex);
            Log.d(TAG, "plant id is: " + plantId);

            // can't water the plant if it’s been less than MIN_AGE_BETWEEN_WATER since it was last watered
            boolean canWater = timeNow - wateredAt >= PlantUtils.MIN_AGE_BETWEEN_WATER;
            Log.d(TAG, "Can water: " + canWater);

            // close the cursor once we're done using it
            cursor.close();

            plantImageRes = PlantUtils.getPlantImageRes(
                    this, timeNow - createdAt, timeNow - wateredAt, plantType);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            ComponentName name = new ComponentName(this, PlantWidgetProvider.class);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(name);

            // Update all widgets
            PlantWidgetProvider.updatePlantWidgets(this, appWidgetManager,
                    plantImageRes, plantId, canWater, appWidgetIds);
        }
    }
}
