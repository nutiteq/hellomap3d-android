package com.nutiteq.advancedmap3;

import android.os.Bundle;

import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.geometry.GeoJSONGeometryReader;
import com.nutiteq.geometry.Geometry;
import com.nutiteq.layers.ClusterElementBuilder;
import com.nutiteq.layers.ClusteredVectorLayer;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.BalloonPopupStyle;
import com.nutiteq.styles.BalloonPopupStyleBuilder;
import com.nutiteq.vectorelements.BalloonPopup;
import com.nutiteq.vectorelements.VectorElement;
import com.nutiteq.wrappedcommons.VectorElementVector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * A sample demonstrating how to read data from GeoJSON and add clustered Markers to map.
 * Both points from GeoJSON, and cluster markers are shown as Ballons which have dynamic texts
 *
 * NB! Suggestions if you have a lot of points (tens or hundreds of thousands) and clusters:
 * 1. Use Point geometry instead of Balloon or Marker
 * 2. Instead of Balloon with text generate dynamically Point bitmap with cluster numbers
 * 3. Make sure you reuse cluster style bitmaps. Creating new bitmap in rendering has technical cost
 */
public class ClusteredGeoJsonActivity extends VectorMapSampleBaseActivity {

    static class MyClusterElementBuilder extends ClusterElementBuilder {
        BalloonPopupStyle balloonPopupStyle;

        public MyClusterElementBuilder(){
        	balloonPopupStyle = new BalloonPopupStyleBuilder().buildStyle();
        }
        
        @Override
        public VectorElement buildClusterElement(MapPos pos, VectorElementVector elements) {

            // Cluster popup has just a number of cluster elements, and default style
            // You can create here also Marker, Point etc. Point is suggested for big number of objects
            // Note: pos has center of the cluster coordinates

            BalloonPopup popup = new BalloonPopup(
                    pos,
                    balloonPopupStyle,
                    Long.toString(elements.size()), "");
            return popup;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);


        // 1. Initialize a local vector data source
        LocalVectorDataSource vectorDataSource1 = new LocalVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new ClusteredVectorLayer(vectorDataSource1, new MyClusterElementBuilder());
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer1);
        // Set visible zoom range for the vector layer
        vectorLayer1.setVisibleZoomRange(new MapRange(0, 18));


        // read GeoJSON
        // parse it as normal JSON, then use SDK parser for GeoJSON geometries

        try {
            String jsonStr = loadJSONFromAsset();
            JSONObject json = new JSONObject(jsonStr);

            GeoJSONGeometryReader geoJsonParser = new GeoJSONGeometryReader();
            BalloonPopupStyle balloonPopupStyle = new BalloonPopupStyleBuilder().buildStyle();

            JSONArray features = json.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject geometry = feature.getJSONObject("geometry");

                // use SDK GeoJSON parser
                Geometry ntGeom = geoJsonParser.readGeometry(geometry.toString());

                JSONObject properties = feature.getJSONObject("properties");

                // create popup for each object
                BalloonPopup popup = new BalloonPopup(
					ntGeom,
					balloonPopupStyle,
                    properties.getString("Capital"),
                    properties.getString("Country"));

                // add all properties as MetaData, so you can use it with click handling
                for (Iterator<String> j = properties.keys(); j.hasNext();){
                    String key = j.next();
                    String val = properties.getString(key);
                    popup.setMetaDataElement(key,val);
                }

                vectorDataSource1.add(popup);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("capitals_3857.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
