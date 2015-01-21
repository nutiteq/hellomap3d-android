package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.advancedmap3.datasource.MyMergedRasterTileDataSource;
import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.RasterTileLayer;

/**
 * A sample demonstrating how to create and use custom raster tile data source.
 * MyMergedRasterTileDataSource uses two input tile data sources to
 * create blended tile bitmaps. This can be faster than using two separate raster layers
 * and takes less memory.
 * 
 * Compare with RasterOverlayActivity which shows same rasters as separate layers
 * 
 */
public class CustomRasterDataSourceActivity extends MapSampleBaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize base and hillshade data sources
        TileDataSource baseTileDataSource = new HTTPTileDataSource(0, 24, Const.MAPBOX_RASTER_URL);
        TileDataSource hillshadeTileDataSource = new HTTPTileDataSource(0, 24, Const.HILLSHADE_RASTER_URL);
        
        // Create merged raster data source
        TileDataSource mergedTileDataSource = new MyMergedRasterTileDataSource(baseTileDataSource, hillshadeTileDataSource);

        // Create raster layer
        baseLayer = new RasterTileLayer(mergedTileDataSource);
        mapView.getLayers().add(baseLayer);
        
        // finally animate map to a nice place
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-122.4323, 37.7582)), 1);
        mapView.setZoom(13, 1);
        
	}
}
