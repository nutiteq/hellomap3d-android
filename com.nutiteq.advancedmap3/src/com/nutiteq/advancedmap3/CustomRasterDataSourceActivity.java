package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.advancedmap3.datasource.MyMergedRasterTileDataSource;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.RasterTileLayer;

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
	}
}
