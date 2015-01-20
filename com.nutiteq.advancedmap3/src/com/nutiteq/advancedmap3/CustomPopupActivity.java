package com.nutiteq.advancedmap3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.nutiteq.advancedmap3.R;
import com.nutiteq.advancedmap3.vectorelements.CustomPopup;
import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Marker;

/**
 * A sample demonstrating how to create and use custom popups.
 * Note that Nutiteq SDK has built-in customizable BalloonPopup class that provides
 * uniform functionality and look across different platforms. But In some cases
 * more customization is needed and Popup subclassing can be used in that case.
 */
public class CustomPopupActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // Add a marker and a popup to the map
        // 1. Initialize a local vector data source
        LocalVectorDataSource vectorDataSource1 = new LocalVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new VectorLayer(vectorDataSource1);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer1);
        
        // 2. Create marker style
        Bitmap androidMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
        com.nutiteq.graphics.Bitmap markerBitmap = BitmapUtils.CreateBitmapFromAndroidBitmap(androidMarkerBitmap);
        
        MarkerStyleBuilder markerStyleBuilder = new MarkerStyleBuilder();
        markerStyleBuilder.setBitmap(markerBitmap);
        markerStyleBuilder.setSize(30);
        MarkerStyle markerStyle = markerStyleBuilder.buildStyle();
        
        // 3. Add marker
        MapPos markerPos = mapView.getOptions().getBaseProjection().fromWgs84(new MapPos(13.38933, 52.51704)); // Berlin
        Marker marker1 = new Marker(markerPos, markerStyle);
        vectorDataSource1.add(marker1);
        
        // 5. Add popup
        CustomPopup popup1 = new CustomPopup(marker1, "custom popup");
        vectorDataSource1.add(popup1);
        
        // finally animate map to the marker
        mapView.setFocusPos(markerPos, 1);
        mapView.setZoom(12, 1);
    }
}
