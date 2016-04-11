package com.nutiteq.advancedmap3;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.vectortiles.CartoCSSStyleSet;
import com.nutiteq.vectortiles.MBVectorTileDecoder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A sample demonstrating how to use CartoDB Vector Tiles, using CartoCSS styling
 */
public class CartoDBVectorTileActivity extends VectorMapSampleBaseActivity {

    private String layerGroupID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        GetLayerGroupID getLayerGroupID = new GetLayerGroupID();
        getLayerGroupID.execute();
    }

    private class GetLayerGroupID extends AsyncTask<Void, Void, Boolean> {

        private URL url;
        private HttpURLConnection conn;
        private BufferedReader reader;


        /**
         * The system calls this to perform work in a worker thread and delivers
         * it the parameters given to AsyncTask.execute()
         *
         * @return
         */
        protected Boolean doInBackground(Void... q) {
            String link = "https://mforrest.cartodb.com/api/v1/map?config=%7B%22version%22%3A%221.0.1%22%2C%22stat_tag%22%3A%223c6f224a-c6ad-11e5-b17e-0e98b61680bf%22%2C%22layers%22%3A%5B%7B%22type%22%3A%22cartodb%22%2C%22options%22%3A%7B%22sql%22%3A%22select%20*%20from%20stations_1%22%2C%22cartocss%22%3A%22%5Cn%5Cn%2F**%20category%20visualization%20*%2F%5Cn%5Cn%23stations_1%20%7B%5Cn%20%20%20marker-fill-opacity%3A%200.9%3B%5Cn%20%20%20marker-line-color%3A%20%23FFF%3B%5Cn%20%20%20marker-line-width%3A%202%3B%5Cn%20%20%20marker-line-opacity%3A%201%3B%5Cn%20%20%20marker-placement%3A%20point%3B%5Cn%20%20%20marker-type%3A%20ellipse%3B%5Cn%20%20%20marker-width%3A%2010%3B%5Cn%20%20%20marker-allow-overlap%3A%20true%3B%5Cn%7D%5Cn%5Cn%23stations_1%5Bfield_7%3D%5C%22In%20Service%5C%22%5D%20%7B%5Cn%20%20%20marker-fill%3A%20%230F3B82%3B%5Cn%7D%5Cn%23stations_1%5Bfield_7%3D%5C%22Not%20In%20Service%5C%22%5D%20%7B%5Cn%20%20%20marker-fill%3A%20%23aaaaaa%3B%5Cn%7D%5Cn%5Cn%23stations_1%20%5B%20field_9%20%3D%20200%5D%20%7B%5Cn%20%20%20marker-width%3A%2080.0%3B%5Cn%7D%5Cn%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2049%5D%20%7B%5Cn%20%20%20marker-width%3A%2025.0%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2038%5D%20%7B%5Cn%20%20%20marker-width%3A%2022.8%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2034%5D%20%7B%5Cn%20%20%20marker-width%3A%2020.6%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2029%5D%20%7B%5Cn%20%20%20marker-width%3A%2018.3%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2025%5D%20%7B%5Cn%20%20%20marker-width%3A%2016.1%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2020.5%5D%20%7B%5Cn%20%20%20marker-width%3A%2013.9%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2016%5D%20%7B%5Cn%20%20%20marker-width%3A%2011.7%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%2012%5D%20%7B%5Cn%20%20%20marker-width%3A%209.4%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%208%5D%20%7B%5Cn%20%20%20marker-width%3A%207.2%3B%5Cn%7D%5Cn%23stations_1%20%5B%20field_9%20%3C%3D%204%5D%20%7B%5Cn%20%20%20marker-width%3A%205.0%3B%5Cn%7D%22%2C%22cartocss_version%22%3A%222.1.1%22%2C%22interactivity%22%3A%5B%22cartodb_id%22%5D%2C%22attributes%22%3A%7B%22id%22%3A%22cartodb_id%22%2C%22columns%22%3A%5B%22name%22%2C%22field_9%22%2C%22slot%22%5D%7D%7D%7D%5D%7D";

            boolean isOK = false;

            try {
                url = new URL(link);

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
                HTTPTileDataSource cdbVectorDataSource = new HTTPTileDataSource(0, 19,
                        "https://cartocdn-ashbu.global.ssl.fastly.net/mforrest/api/v1/map/"+layerGroupID+"/0/{zoom}/{x}/{y}.mvt");

                String cacheFile = getExternalFilesDir(null)+"/cdb_mvt_tile_cache.db";
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
                cartoCSS = cartoCSS.replace("#stations_1","#layer0");

                CartoCSSStyleSet cartoCSSStyleSet = new CartoCSSStyleSet(cartoCSS);
                MBVectorTileDecoder mbVectorTileDecoder = new MBVectorTileDecoder(cartoCSSStyleSet);

                VectorTileLayer vectorTileLayer = new VectorTileLayer(cachedDataSource, mbVectorTileDecoder);
                mapView.getLayers().add(vectorTileLayer);

                // finally animate map to the content area
                mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(-74.0059, 40.7127)), 1); // NYC
                mapView.setZoom(15, 1);
            } else {
                Toast.makeText(getApplicationContext(), "Sorry but I can't get layerGroupID.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
