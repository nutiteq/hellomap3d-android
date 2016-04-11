package com.nutiteq.advancedmap3;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A sample demonstrating how to use CartoDB Maps API with Raster tiles and UTFGrid
 */
public class CartoDBUTFGridActivity extends VectorMapSampleBaseActivity {

    private String layerGroupID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        // define server config

        try {

            // you need to change these according to your DB

            String cartoDbAccount = "nutiteq";
            String sql = "select * from stations_1";
            String statTag = "3c6f224a-c6ad-11e5-b17e-0e98b61680bf";
            String[] columns = new String[]{"name","field_9","slot"};
            String cartoCss =
                    "/** category visualization */\n" +
                    "\n" +
                    "#stations_1 {\n" +
                    "   marker-fill-opacity: 0.9;\n" +
                    "   marker-line-color: #FFF;\n" +
                    "   marker-line-width: 2;\n" +
                    "   marker-line-opacity: 1;\n" +
                    "   marker-placement: point;\n" +
                    "   marker-type: ellipse;\n" +
                    "   marker-width: 10;\n" +
                    "   marker-allow-overlap: true;\n" +
                    "}\n" +
                    "\n" +
                    "#stations_1[field_7=\"In Service\"] {\n" +
                    "   marker-fill: #0F3B82;\n" +
                    "}\n" +
                    "#stations_1[field_7=\"Not In Service\"] {\n" +
                    "   marker-fill: #aaaaaa;\n" +
                    "}\n" +
                    "\n" +
                    "#stations_1 [ field_9 = 200] {\n" +
                    "   marker-width: 80.0;\n" +
                    "}\n" +
                    "\n" +
                    "#stations_1 [ field_9 <= 49] {\n" +
                    "   marker-width: 25.0;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 38] {\n" +
                    "   marker-width: 22.8;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 34] {\n" +
                    "   marker-width: 20.6;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 29] {\n" +
                    "   marker-width: 18.3;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 25] {\n" +
                    "   marker-width: 16.1;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 20.5] {\n" +
                    "   marker-width: 13.9;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 16] {\n" +
                    "   marker-width: 11.7;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 12] {\n" +
                    "   marker-width: 9.4;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 8] {\n" +
                    "   marker-width: 7.2;\n" +
                    "}\n" +
                    "#stations_1 [ field_9 <= 4] {\n" +
                    "   marker-width: 5.0;\n" +
                    "}";


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
            for(String col: columns){
                columnsJson.put(col);
            }

            attributesJson.put("columns", columnsJson);
            optionsJson.put("attributes",attributesJson);
            layersJson.put("options",optionsJson);
            layersArrayJson.put(layersJson);
            configJson.put("layers", layersArrayJson);

            String config = configJson.toString();

            Log.i(Const.LOG_TAG, config);

            Uri builtUri = Uri.parse("https://"+cartoDbAccount+".cartodb.com/api/v1/map")
                    .buildUpon()
                    .appendQueryParameter("config", config)
                    .build();
            URL url = new URL(builtUri.toString());

            GetLayerGroupID getLayerGroupID = new GetLayerGroupID(url, cartoDbAccount);
            getLayerGroupID.execute();

        } catch (MalformedURLException e) {
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        mapView.setMapEventListener(null);

        super.onDestroy();
    }

    private class GetLayerGroupID extends AsyncTask<Void, Void, Boolean> {

        private final String cartoDbAccount;
        private URL url;
        private HttpURLConnection conn;
        private BufferedReader reader;

        public GetLayerGroupID(URL url, String cartoDbAccount) {
            this.cartoDbAccount = cartoDbAccount;
            this.url = url;
        }


        /**
         * The system calls this to perform work in a worker thread and delivers
         * it the parameters given to AsyncTask.execute()
         *
         * @return
         */
        protected Boolean doInBackground(Void... q) {

            boolean isOK = false;

            try {


                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();

                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                String result = sb.toString();

                JSONObject json = new JSONObject(result);

                layerGroupID = json.getString("layergroupid");

                if (layerGroupID == null) {
                    layerGroupID = "";
                }

                isOK = true;
            } catch (IOException e) {
                isOK = false;
                e.printStackTrace();
            } catch (JSONException e) {
                isOK = false;
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return isOK;
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        protected void onPostExecute(Boolean isOK) {
            if (isOK && !layerGroupID.equals("")) {
                HTTPTileDataSource rasterDataSource = new HTTPTileDataSource(0, 19,
                        "https://cartocdn-ashbu.global.ssl.fastly.net/"+cartoDbAccount+"/api/v1/map/"+layerGroupID+"/0/{zoom}/{x}/{y}.png");

                HTTPTileDataSource utfGridDataSource = new HTTPTileDataSource(0, 19,
                        "https://cartocdn-ashbu.global.ssl.fastly.net/"+cartoDbAccount+"/api/v1/map/"+layerGroupID+"/0/{zoom}/{x}/{y}.grid.json");

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
                Toast.makeText(getApplicationContext(),"Sorry but I can't get layerGroupID.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
