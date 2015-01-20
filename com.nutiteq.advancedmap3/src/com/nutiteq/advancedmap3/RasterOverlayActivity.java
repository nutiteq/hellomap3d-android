package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.layers.RasterTileLayer;

/**
 * A sample demonstrating how to use raster layer on top of
 * the vector base map to provide height information.
 */
public class RasterOverlayActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // Initialize hillshading raster data source, better visible in mountain ranges
        HTTPTileDataSource hillsRasterTileDataSource = new HTTPTileDataSource(0, 24, Const.HILLSHADE_RASTER_URL);

        // Add persistent caching datasource, tiles will be stored locally on persistent storage
        PersistentCacheTileDataSource cachedDataSource = 
                new PersistentCacheTileDataSource(hillsRasterTileDataSource, getExternalFilesDir(null)+"/mapcache_hills.db");
        
        // Initialize a raster layer with the previous data source
        RasterTileLayer hillshadeLayer = new RasterTileLayer(cachedDataSource);
        // Add the previous raster layer to the map
        mapView.getLayers().add(hillshadeLayer);

        // finally animate map to the marker
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-122.4323, 37.7582)), 1);
        mapView.setZoom(13, 1);
    }
}
