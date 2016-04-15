package com.nutiteq.advancedmap3;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.nutiteq.advancedmap3.listener.MyMapEventListener;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.UTFGridRasterTileLayer;
import com.nutiteq.layers.VectorLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

/**
 * A sample demonstrating how to use CartoDB Maps API with Raster tiles and UTFGrid
 */
public class CartoDBUTFGridActivity extends VectorMapSampleBaseActivity {

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

            String sql = "select * from stations_1";
            String statTag = "3c6f224a-c6ad-11e5-b17e-0e98b61680bf";
            String[] columns = new String[]{"name", "field_9", "slot"};
            String cartoCss =
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

            // you probably do not need to change much of below
            JSONObject configJson = new JSONObject();

            configJson.put("version", "1.0.1");
            configJson.put("stat_tag", statTag);

            JSONArray layersArrayJson = new JSONArray();
            JSONObject layersJson = new JSONObject();
            layersJson.put("type", "cartodb");

            JSONObject optionsJson = new JSONObject();
            optionsJson.put("sql", sql);
            optionsJson.put("cartocss", cartoCss);
            optionsJson.put("cartocss_version", "2.1.1");
            JSONArray interactivityJson = new JSONArray();
            interactivityJson.put("cartodb_id");
            optionsJson.put("interactivity", interactivityJson);
            JSONObject attributesJson = new JSONObject();
            attributesJson.put("id", "cartodb_id");
            JSONArray columnsJson = new JSONArray();
            for (String col : columns) {
                columnsJson.put(col);
            }

            attributesJson.put("columns", columnsJson);
            optionsJson.put("attributes", attributesJson);
            layersJson.put("options", optionsJson);
            layersArrayJson.put(layersJson);
            configJson.put("layers", layersArrayJson);

            config = configJson.toString();
        } catch (JSONException e) {
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
        }catch (MalformedURLException e3){
        }

        if (!layerGroupID.equals("")) {
            HTTPTileDataSource rasterDataSource = new HTTPTileDataSource(0, 19,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/" + cartoDbAccount + "/api/v1/map/" + layerGroupID + "/0/{zoom}/{x}/{y}.png");

            HTTPTileDataSource utfGridDataSource = new HTTPTileDataSource(0, 19,
                    "https://cartocdn-ashbu.global.ssl.fastly.net/" + cartoDbAccount + "/api/v1/map/" + layerGroupID + "/0/{zoom}/{x}/{y}.grid.json");

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
            Toast.makeText(getApplicationContext(), "Sorry but I can't get layerGroupID.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        mapView.setMapEventListener(null);

        super.onDestroy();
    }
}
