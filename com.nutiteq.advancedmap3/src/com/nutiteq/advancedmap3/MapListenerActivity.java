package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.advancedmap3.listener.MyMapEventListener;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.UnculledVectorDataSource;
import com.nutiteq.layers.VectorLayer;

/**
 * 
 * Add MapListener for click detections on map and map vector objects
 * Vector objects are added in Overlays2DActivity
 * 
 * @author jaak
 *
 */
public class MapListenerActivity extends Overlays2DActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        // Overlays2DActivity adds 2D vector elements to map  
        super.onCreate(savedInstanceState);
        
        // 1. Initialize an unculled vector data source and layer for click Balloons
        
        UnculledVectorDataSource vectorDataSource = new UnculledVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer = new VectorLayer(vectorDataSource);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer);
        // Set visible zoom range for the vector layer
        vectorLayer.setVisibleZoomRange(new MapRange(10, 24));
        
        // 2. Create and set a map event listener, 
        // it needs the data source for balloons
        mapView.getOptions().setMapEventListener(new MyMapEventListener(mapView, vectorDataSource));
        
    }
}
