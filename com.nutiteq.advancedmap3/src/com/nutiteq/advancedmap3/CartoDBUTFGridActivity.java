package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.widget.Toast;

import com.nutiteq.advancedmap3.listener.MyMapEventListener;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.UTFGridRasterTileLayer;
import com.nutiteq.layers.VectorLayer;

import java.util.concurrent.ExecutionException;

/**
 * A sample demonstrating how to use CartoDB Maps API with Raster tiles and UTFGrid
 */
public class CartoDBUTFGridActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        GetCartoDBLayerGroupID getCartoDBLayerGroupID = new GetCartoDBLayerGroupID();
        String layerGroupID = "";

        try {
            // this get layerGroupID and it wait for result than it contiune below
            layerGroupID = getCartoDBLayerGroupID.getLayerGroupID();
        } catch (InterruptedException e) {
        } catch (ExecutionException e2) {
        }

        if (!layerGroupID.equals("")) {
            HTTPTileDataSource rasterDataSource = new HTTPTileDataSource(0, 19,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/mforrest/api/v1/map/" + layerGroupID + "/0/{zoom}/{x}/{y}.png");

            HTTPTileDataSource utfGridDataSource = new HTTPTileDataSource(0, 19,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/mforrest/api/v1/map/" + layerGroupID + "/0/{zoom}/{x}/{y}.grid.json");

            UTFGridRasterTileLayer utfGridRasterTileLayer = new UTFGridRasterTileLayer(rasterDataSource, utfGridDataSource);

            mapView.getLayers().add(utfGridRasterTileLayer);

            // Set visible zoom range for the vector layer
            utfGridRasterTileLayer.setVisibleZoomRange(new MapRange(10, 23));

            // set listener to get point click popups

            // 1. Initialize a local vector data source and layer for click Balloons
            LocalVectorDataSource vectorDataSource = new LocalVectorDataSource(baseProjection);
            // Initialize a vector layer with the previous data source
            VectorLayer vectorLayer = new VectorLayer(vectorDataSource);
            // Add the previous vector layer to the map
            mapView.getLayers().add(vectorLayer);

            MyMapEventListener mapListener = new MyMapEventListener(mapView, vectorDataSource);
            mapView.setMapEventListener(mapListener);

            mapListener.setGridLayer(utfGridRasterTileLayer);

            // finally animate map to the content area
            mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-74.0059, 40.7127)), 1); // NYC
            mapView.setZoom(15, 1);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.layer_group_id_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.setMapEventListener(null);

        super.onDestroy();
    }
}
