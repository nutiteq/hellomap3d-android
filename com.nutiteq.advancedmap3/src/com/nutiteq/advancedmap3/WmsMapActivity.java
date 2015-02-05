package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.advancedmap3.datasource.HttpWmsTileDataSource;
import com.nutiteq.core.MapPos;
import com.nutiteq.layers.RasterTileLayer;

/**
 * A sample demonstrating how to use WMS service raster on top of
 * the vector base map
 */
public class WmsMapActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        String url = "http://kaart.maakaart.ee/geoserver/wms?transparent=true&";
     
        String layers = "topp:states";

        HttpWmsTileDataSource wms = new HttpWmsTileDataSource(0, 24, baseProjection, false, url, "", layers, "image/png");
        RasterTileLayer wmsLayer = new RasterTileLayer(wms);
        mapView.getLayers().add(wmsLayer);
        
        // finally animate map to map coverage
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-100, 40)), 1);
        mapView.setZoom(5, 1);
    }
}
