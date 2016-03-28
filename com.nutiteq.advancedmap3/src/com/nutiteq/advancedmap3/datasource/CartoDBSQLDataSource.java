package com.nutiteq.advancedmap3.datasource;

import android.net.Uri;
import android.util.Log;

import com.nutiteq.advancedmap3.Const;
import com.nutiteq.core.MapBounds;
import com.nutiteq.core.MapEnvelope;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapTile;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.VectorDataSource;
import com.nutiteq.geometry.GeoJSONGeometryReader;
import com.nutiteq.geometry.Geometry;
import com.nutiteq.geometry.LineGeometry;
import com.nutiteq.geometry.PointGeometry;
import com.nutiteq.geometry.PolygonGeometry;
import com.nutiteq.projections.Projection;
import com.nutiteq.renderers.components.CullState;
import com.nutiteq.styles.BalloonPopupStyle;
import com.nutiteq.styles.BalloonPopupStyleBuilder;
import com.nutiteq.styles.LineStyle;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.PointStyle;
import com.nutiteq.styles.PolygonStyle;
import com.nutiteq.styles.Style;
import com.nutiteq.vectorelements.BalloonPopup;
import com.nutiteq.vectorelements.Line;
import com.nutiteq.vectorelements.Marker;
import com.nutiteq.vectorelements.Point;
import com.nutiteq.vectorelements.Polygon;
import com.nutiteq.vectorelements.VectorElement;
import com.nutiteq.wrappedcommons.VectorElementVector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Locale;

/**
 * A custom vector data source making queries to http://docs.cartodb.com/cartodb-platform/sql-api/
 * 
*/
public class CartoDBSQLDataSource extends VectorDataSource {


    private String baseUrl;
    private String query;
    private Style style;


	public CartoDBSQLDataSource(Projection proj, String baseUrl, String query, Style style) {
		super(proj);
        this.baseUrl = baseUrl;
        this.style = style;
        this.query = query;
	}


    @Override
    public VectorElementVector loadElements(CullState cullState) {
        VectorElementVector elements = new VectorElementVector();

        MapEnvelope mapViewBounds = cullState.getProjectionEnvelope(this.getProjection());
        MapPos min = mapViewBounds.getBounds().getMin();
        MapPos max = mapViewBounds.getBounds().getMax();

        //run query here
        loadData(elements, min,max,cullState.getViewState().getZoom());

        return elements;

    }

    private void loadData(VectorElementVector elements, MapPos min, MapPos max, float zoom) {

        // load and parse JSON
        String bbox = String.format(Locale.US, "ST_SetSRID(ST_MakeEnvelope(%f,%f,%f,%f),3857) && the_geom_webmercator", min.getX(), min.getY(), max.getX(), max.getY());

        String unencodedQuery = query.replace("!bbox!", bbox);

        Log.d(Const.LOG_TAG, "SQL query: " + query);

        unencodedQuery = unencodedQuery.replace("zoom('!scale_denominator!')", String.valueOf(zoom));

        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(unencodedQuery, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String urlAddress = baseUrl + "?format=GeoJSON&q="+ encodedQuery;

        Log.d(Const.LOG_TAG, "SQL API: " + urlAddress);

        try {

            URL url = new URL(urlAddress);

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);

            JSONObject json = new JSONObject(responseStrBuilder.toString());

            GeoJSONGeometryReader geoJsonParser = new GeoJSONGeometryReader();

            JSONArray features = json.getJSONArray("features");
            for (int i = 0; i < features.length(); i++) {
                JSONObject feature = (JSONObject) features.get(i);
                JSONObject geometry = feature.getJSONObject("geometry");

                // use SDK GeoJSON parser
                Geometry ntGeom = geoJsonParser.readGeometry(geometry.toString());

                JSONObject properties = feature.getJSONObject("properties");
                VectorElement element;

                // create object based on given style
                if(style instanceof PointStyle){
                    element = new Point((PointGeometry) ntGeom,(PointStyle) style);
                }else if(style instanceof MarkerStyle){
                    element = new Marker(ntGeom, (MarkerStyle) style);
                }else if(style instanceof LineStyle) {
                    element = new Line((LineGeometry) ntGeom, (LineStyle) style);
                }else if(style instanceof PolygonStyle) {
                    element = new Polygon((PolygonGeometry) ntGeom, (PolygonStyle) style);
                }else{
                    Log.e(Const.LOG_TAG, "Object creation not implemented yet for style: " + style.swigGetClassName());
                    break;
                }

                // add all properties as MetaData, so you can use it with click handling
                for (Iterator<String> j = properties.keys(); j.hasNext();){
                    String key = j.next();
                    String val = properties.getString(key);
                    element.setMetaDataElement(key,val);
                }

                elements.add(element);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
