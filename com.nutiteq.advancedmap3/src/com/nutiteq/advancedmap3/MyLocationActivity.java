package com.nutiteq.advancedmap3;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.graphics.Color;
import com.nutiteq.hellomap3.util.CircleUtil;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.projections.Projection;
import com.nutiteq.styles.PolygonStyle;
import com.nutiteq.styles.PolygonStyleBuilder;
import com.nutiteq.utils.Log;
import com.nutiteq.vectorelements.Polygon;
import com.nutiteq.wrappedcommons.MapPosVector;

/**
 * Shows user GPS location on map.
 * Make sure your app has location permission in Manifest file
 */
public class MyLocationActivity extends VectorMapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // MapSampleBaseActivity creates and configures mapView
        super.onCreate(savedInstanceState);

        final Projection proj = super.baseProjection;

        // Initialize an local vector data source where to put my location circle
        LocalVectorDataSource vectorDataSource = new LocalVectorDataSource(proj);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer = new VectorLayer(vectorDataSource);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer);


        // style for GPS My Location circle
        PolygonStyleBuilder polygonStyleBuilder = new PolygonStyleBuilder();
        polygonStyleBuilder.setColor(new Color(0xAAFF0000));
        PolygonStyle gpsStyle = polygonStyleBuilder.buildStyle();

        MapPosVector gpsCirclePoses = new MapPosVector();
        final Polygon locationCircle = new Polygon(gpsCirclePoses, gpsStyle);
        // initially empty and invisible
        locationCircle.setVisible(false);

        vectorDataSource.add(locationCircle);

        final LocationListener locationListener = new LocationListener(){

            @Override
            public void onLocationChanged(Location location) {
                Log.debug("GPS onLocationChanged " + location);
                if (locationCircle != null) {
                    locationCircle.setPoses(CircleUtil.createLocationCircle(location, proj));
                    locationCircle.setVisible(true);
                    mapView.setFocusPos(proj.fromWgs84(new MapPos(location.getLongitude(), location.getLatitude())), 0.5f);
                    mapView.setZoom(14, 1.0f); // zoom 2, duration 0 seconds (no animation)
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // user has maybe disabled location services / GPS
        if(locationManager.getProviders(true).size() == 0){
            Toast.makeText(this, "Cannot get location, no location providers enabled. Check device settings", Toast.LENGTH_LONG).show();
        }

        // use all enabled device providers with same parameters
        for(String provider : locationManager.getProviders(true)){
            Log.debug("adding location provider " + provider);
            locationManager.requestLocationUpdates(provider, 1000, 50, locationListener);
        }





    }
}
