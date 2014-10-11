package com.nutiteq.advancedmap3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.nutiteq.advancedmap3.R;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.UnculledVectorDataSource;
import com.nutiteq.layers.RasterTileLayer;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Marker;

public class RasterOverlayActivity extends MapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // Initialize hillshading raster data source, better visible in mountain ranges
        HTTPTileDataSource hillsRasterTileDataSource = new HTTPTileDataSource(0, 24, "http://tiles.wmflabs.org/hillshading/{zoom}/{x}/{y}.png");

        // add persistent caching into datasource 
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
