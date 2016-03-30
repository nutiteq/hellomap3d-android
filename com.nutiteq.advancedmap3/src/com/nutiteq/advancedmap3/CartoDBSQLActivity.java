package com.nutiteq.advancedmap3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.nutiteq.advancedmap3.datasource.CartoDBSQLDataSource;
import com.nutiteq.advancedmap3.listener.MyMapEventListener;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.graphics.Color;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.styles.PointStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Marker;

/**
 * A sample demonstrating how to use CartoDB SQL API to get data
 * and how to create custom VectorDataSource
 */
public class CartoDBSQLActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);


        // define style for vector objects. Note that all objects must have same style here, which can be big limitation
        PointStyleBuilder pointStyleBuilder = new PointStyleBuilder();
        pointStyleBuilder.setColor(new Color(0x800000ff)); // blue
        pointStyleBuilder.setSize(10);

        // Initialize a local vector data source
        CartoDBSQLDataSource vectorDataSource1 = new CartoDBSQLDataSource(baseProjection,"https://mforrest.cartodb.com/api/v2/sql","SELECT cartodb_id,the_geom_webmercator AS the_geom,name,address,bikes,slot,field_7,field_8,field_9,field_16,field_17,field_18 FROM stations_1 WHERE !bbox!",pointStyleBuilder.buildStyle());
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new VectorLayer(vectorDataSource1);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer1);
        // Set visible zoom range for the vector layer
        vectorLayer1.setVisibleZoomRange(new MapRange(14, 23));


        // set listener to get point click popups

        // 1. Initialize a local vector data source and layer for click Balloons
        LocalVectorDataSource vectorDataSource = new LocalVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer = new VectorLayer(vectorDataSource);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer);

        mapView.setMapEventListener(new MyMapEventListener(mapView, vectorDataSource));


        // finally animate map to the marker
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-74.0059, 40.7127)), 1);
        mapView.setZoom(15, 1);
    }
}
