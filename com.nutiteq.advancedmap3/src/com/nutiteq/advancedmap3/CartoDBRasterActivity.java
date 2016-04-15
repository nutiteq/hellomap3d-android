package com.nutiteq.advancedmap3;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.layers.RasterTileLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * A sample demonstrating how to use CartoDB PostGIS Raster data, as tiled raster layer
 * Inspired by web sample http://bl.ocks.org/jorgeas80/4c7169c9b6356858f3cc
 */
public class CartoDBRasterActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        String cartoDbAccount = "nutiteq";
        String config = "";
        String layerGroupID = "";

        // define server config
        try {

            // you need to change these according to your DB

            String sql = "select * from table_46g";
            String cartoCss =
                    "#table_46g {raster-opacity: 0.5;}";

            // you probably do not need to change much of below
            JSONObject configJson = new JSONObject();

            configJson.put("version", "1.2.0");

            JSONArray layersArrayJson = new JSONArray();
            JSONObject layersJson = new JSONObject();
            layersJson.put("type", "cartodb");

            JSONObject optionsJson = new JSONObject();
            optionsJson.put("sql", sql);
            optionsJson.put("cartocss", cartoCss);
            optionsJson.put("cartocss_version", "2.3.0");
            optionsJson.put("geom_column", "the_raster_webmercator");
            optionsJson.put("geom_type", "raster");
            layersJson.put("options", optionsJson);
            layersArrayJson.put(layersJson);
            configJson.put("layers", layersArrayJson);

            config = configJson.toString();

            Log.i(Const.LOG_TAG, config);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Uri builtUri = Uri.parse("https://" + cartoDbAccount + ".cartodb.com/api/v1/map")
                .buildUpon()
                .appendQueryParameter("config", config)
                .build();

        GetCartoDBLayerGroupID getCartoDBLayerGroupID = new GetCartoDBLayerGroupID();

        try {
            // this get layerGroupID and it wait for result than it contiune below
            layerGroupID = getCartoDBLayerGroupID.getLayerGroupID(new URL(builtUri.toString()));
        } catch (InterruptedException e) {
        } catch (ExecutionException e2) {
        } catch (MalformedURLException e3) {
        }

        if (!layerGroupID.equals("")) {
            HTTPTileDataSource rasterDataSource = new HTTPTileDataSource(0, 18,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/" + cartoDbAccount + "/api/v1/map/" + layerGroupID + "/{zoom}/{x}/{y}.png");

            mapView.getLayers().add(new RasterTileLayer(rasterDataSource));

            // finally go map to the content area
            mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(22.7478235498916, 58.8330577553785)), 0);
            mapView.setZoom(11, 0);
        } else {
            Toast.makeText(getApplicationContext(), "Sorry but I can't get layerGroupID.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.setMapEventListener(null);

        super.onDestroy();
    }
}
