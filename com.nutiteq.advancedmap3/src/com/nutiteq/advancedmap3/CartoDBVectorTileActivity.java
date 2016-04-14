package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.vectortiles.CartoCSSStyleSet;
import com.nutiteq.vectortiles.MBVectorTileDecoder;

import java.util.concurrent.ExecutionException;

/**
 * A sample demonstrating how to use CartoDB Vector Tiles, using CartoCSS styling
 */
public class CartoDBVectorTileActivity extends VectorMapSampleBaseActivity {

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
            HTTPTileDataSource cdbVectorDataSource = new HTTPTileDataSource(0, 19,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/mforrest/api/v1/map/" + layerGroupID + "/0/{zoom}/{x}/{y}.mvt");

            String cacheFile = getExternalFilesDir(null) + "/cdb_mvt_tile_cache.db";
            Log.i(Const.LOG_TAG, "cacheFile = " + cacheFile);
            PersistentCacheTileDataSource cachedDataSource = new PersistentCacheTileDataSource(cdbVectorDataSource, cacheFile);

            // CartoCSS is taken from the viz.json, see URL above
            String cartoCSS =
                    "#stations_1{marker-fill-opacity:0.9;marker-line-color:#FFF;marker-line-width:2;marker-line-opacity:1;marker-placement:point;marker-type:ellipse;marker-width:10;marker-allow-overlap:true;}\n" +
                            "#stations_1[field_7='In Service']{marker-fill:#0F3B82;}\n" +
                            "#stations_1[field_7='Not In Service']{marker-fill:#aaaaaa;}\n" +
                            "#stations_1 [ field_9 = 200]{marker-width:80.0;}\n" +
                            "#stations_1 [ field_9 <= 49]{marker-width:25.0;}\n" +
                            "#stations_1 [ field_9 <= 38]{marker-width:22.8;}\n" +
                            "#stations_1 [ field_9 <= 34]{marker-width:20.6;}\n" +
                            "#stations_1 [ field_9 <= 29]{marker-width:18.3;}\n" +
                            "#stations_1 [ field_9 <= 25]{marker-width:16.1;}\n" +
                            "#stations_1 [ field_9 <= 20.5]{marker-width:13.9;}\n" +
                            "#stations_1 [ field_9 <= 16]{marker-width:11.7;}\n" +
                            "#stations_1 [ field_9 <= 12]{marker-width:9.4;}\n" +
                            "#stations_1 [ field_9 <= 8]{marker-width:7.2;}\n" +
                            "#stations_1 [ field_9 <= 4]{marker-width:5.0;}";

            // workaround for unamed layer names in CartoDB MVT tiles
            cartoCSS = cartoCSS.replace("#stations_1", "#layer0");

            CartoCSSStyleSet cartoCSSStyleSet = new CartoCSSStyleSet(cartoCSS);
            MBVectorTileDecoder mbVectorTileDecoder = new MBVectorTileDecoder(cartoCSSStyleSet);

            VectorTileLayer vectorTileLayer = new VectorTileLayer(cachedDataSource, mbVectorTileDecoder);
            mapView.getLayers().add(vectorTileLayer);

            // finally animate map to the content area
            mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-74.0059, 40.7127)), 1); // NYC
            mapView.setZoom(15, 1);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.layer_group_id_error), Toast.LENGTH_LONG).show();
        }
    }
}
