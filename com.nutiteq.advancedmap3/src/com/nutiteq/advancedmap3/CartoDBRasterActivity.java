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
import com.nutiteq.layers.RasterTileLayer;
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
 * A sample demonstrating how to use CartoDB PostGIS Raster data, as tiled raster layer
 * Inspired by web sample http://bl.ocks.org/jorgeas80/4c7169c9b6356858f3cc
 */
public class CartoDBRasterActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        // define server config

        try {

            // you need to change these according to your DB

            String cartoDbAccount = "nutiteq";
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
        private String layerGroupID;

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
                HTTPTileDataSource rasterDataSource = new HTTPTileDataSource(0, 18,
                        "https://cartocdn-ashbu.global.ssl.fastly.net/"+cartoDbAccount+"/api/v1/map/"+layerGroupID+"/{zoom}/{x}/{y}.png");

                mapView.getLayers().add(new RasterTileLayer(rasterDataSource));

                // finally go map to the content area
                mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(22.7478235498916, 58.8330577553785)), 0);
                mapView.setZoom(11, 0);
            } else {
                Toast.makeText(getApplicationContext(),"Sorry but I can't get layerGroupID.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
