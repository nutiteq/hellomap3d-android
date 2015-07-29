package com.nutiteq.advancedmap3;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nutiteq.filepicker.FilePicker;
import com.nutiteq.filepicker.FilePickerActivity;

/**
 * Shows list of demo Activities. Enables to open pre-launch activity for file picking.
 * This is the "main" of samples
 */
public class LauncherList extends ListActivity{

    // list of demos: MapActivity, ParameterSelectorActivity (can be null)
    // if parameter selector is given, then this is launched first to get a parameter (file path)
    
    private Object[][] samples={
            {PinMapActivity.class,null},
            {Overlays2DActivity.class,null},
            {MapListenerActivity.class,null},
            {Overlays3DActivity.class,null},
            {OfflineVectorMapActivity.class,null},
            {MbtilesActivity.class,FilePicker.class},
            {GroundOverlayActivity.class,null},
            {AnimatedRasterMapActivity.class,null},
            {RasterOverlayActivity.class,null},
            {AerialMapActivity.class,null},
            {CustomRasterDataSourceActivity.class,null},
            {CustomPopupActivity.class,null},
            {GraphhopperRouteActivity.class,FilePicker.class},
    		{PackageManagerActivity.class,null},
            {WmsMapActivity.class,null},
            {ClusteredRandomPointsActivity.class,null},
            {ClusteredGeoJsonActivity.class,null}
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.list);

        ListView lv = this.getListView();
        lv.setAdapter(new ArrayAdapter<String>(
                this, 
                android.R.layout.simple_list_item_1, 
                getStringArray()));
    }
    
    private String[] getStringArray() {
        String[] sampleNames = new String[samples.length];
        for(int i=0; i < samples.length; i++) {
            sampleNames[i] = ""+(i+1)+". "+((Class<?>) samples[i][0]).getSimpleName();
        }
        return sampleNames;
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
        if (samples[position][1] != null) {

            try {

                Intent myIntent = new Intent(LauncherList.this,
                        (Class<?>) samples[position][1]);

                Class<?> activityToRun = (Class<?>) samples[position][0];
                FilePickerActivity activityInstance = (FilePickerActivity) activityToRun
                        .newInstance();

                FilePicker.setFileSelectMessage(activityInstance
                        .getFileSelectMessage());
                FilePicker.setFileDisplayFilter(activityInstance
                        .getFileFilter());

                Bundle b = new Bundle();
                b.putString("class", ((Class<?>) samples[position][0]).getName());
                myIntent.putExtras(b);
                startActivityForResult(myIntent, 1);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        } else {
            Intent myIntent = new Intent(LauncherList.this,
                    (Class<?>) samples[position][0]);
            this.startActivity(myIntent);
        }
    }
    
    
    // gets fileName from FilePicker and starts Map Activity with fileName as parameter
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null){
            return;
        }
        String fileName = data.getStringExtra("selectedFile");
        String className = data.getStringExtra("class");
        if(fileName != null && className != null){
            try {
                Intent myIntent = new Intent(LauncherList.this,
                            Class.forName(className));
    
                Bundle b = new Bundle();
                b.putString("selectedFile", fileName);
                myIntent.putExtras(b);
                this.startActivity(myIntent);
            
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
}
