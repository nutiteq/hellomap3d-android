package com.nutiteq.advancedmap3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.nutiteq.advancedmap3.R;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.UnculledVectorDataSource;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Marker;

public class PinMapActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // Add a pin marker to map
        // 1. Initialize an unculled vector data source
        UnculledVectorDataSource vectorDataSource1 = new UnculledVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new VectorLayer(vectorDataSource1);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer1);
        // Set visible zoom range for the vector layer
        vectorLayer1.setVisibleZoomRange(new MapRange(0, 18));
        
        // 2. Create marker style
        Bitmap androidMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        com.nutiteq.graphics.Bitmap markerBitmap = BitmapUtils.CreateBitmapFromAndroidBitmap(androidMarkerBitmap);
        
        MarkerStyleBuilder markerStyleBuilder = new MarkerStyleBuilder();
        markerStyleBuilder.setBitmap(markerBitmap);
        //markerStyleBuilder.setHideIfOverlapped(false);
        markerStyleBuilder.setSize(30);
        MarkerStyle sharedMarkerStyle = markerStyleBuilder.buildStyle();
        
        // 3. Add marker
        MapPos markerPos = mapView.getOptions().getBaseProjection().fromWgs84(new MapPos(13.38933, 52.51704)); // Berlin
        Marker marker1 = new Marker(markerPos, sharedMarkerStyle);
        vectorDataSource1.add(marker1);
        
        // finally animate map to the marker
        mapView.setFocusPos(markerPos, 1);
        mapView.setZoom(12, 1);
        
    }
}
