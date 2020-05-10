package com.example.android.mygarden.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.mygarden.R;
import com.example.android.mygarden.provider.PlantContract;
import com.example.android.mygarden.utils.PlantUtils;

import static com.example.android.mygarden.provider.PlantContract.BASE_CONTENT_URI;
import static com.example.android.mygarden.provider.PlantContract.PATH_PLANTS;

public class GridWidgetService extends RemoteViewsService
{
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    // needed to access ContentResolver later
    Context context;
    // needed to get the plant data from the database
    Cursor cursor;

    public GridRemoteViewsFactory(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        Uri PLANTS_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_PLANTS).build();

        // get all plants by order they were created
        cursor = context.getContentResolver().query(
                PLANTS_URI,
                null,
                null,
                null,
                PlantContract.PlantEntry.COLUMN_CREATION_TIME);
    }

    @Override
    public void onDestroy() {
        cursor.close();
    }

    @Override
    public int getCount() {
        if (cursor != null) {
            return cursor.getCount();
        }
        else
        {
            return 0;
        }
    }

    @Override
    public RemoteViews getViewAt(int i) {

        // extract details of the plant at index i
        int plantImageRes = R.drawable.grass;
        long plantId = PlantContract.INVALID_PLANT_ID;
        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToPosition(i);

            int idIndex = cursor.getColumnIndex(PlantContract.PlantEntry._ID);
            int createTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_CREATION_TIME);
            int waterTimeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_LAST_WATERED_TIME);
            int plantTypeIndex = cursor.getColumnIndex(PlantContract.PlantEntry.COLUMN_PLANT_TYPE);

            plantId = cursor.getLong(idIndex);
            long timeNow = System.currentTimeMillis();
            long createdAt = cursor.getLong(createTimeIndex);
            long wateredAt = cursor.getLong(waterTimeIndex);
            int plantType = cursor.getInt(plantTypeIndex);

            plantImageRes = PlantUtils.getPlantImageRes(
                    context, timeNow - createdAt, timeNow - wateredAt, plantType);
        }
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.plant_widget);

        // update widget plant image
        views.setImageViewResource(R.id.widget_plant_image, plantImageRes);
        // update ID text
        views.setTextViewText(R.id.widget_plant_id, String.valueOf(plantId));

        // always hide the water drop button in GridView mode.
        // we don't want it cluttering the GridView
        views.setViewVisibility(R.id.widget_water_button, View.GONE);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        // treat all views as the same
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
