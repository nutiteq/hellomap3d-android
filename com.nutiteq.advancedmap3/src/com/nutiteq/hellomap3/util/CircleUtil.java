package com.nutiteq.hellomap3.util;

import android.location.Location;

import com.nutiteq.core.MapPos;
import com.nutiteq.projections.Projection;
import com.nutiteq.wrappedcommons.MapPosVector;

/**
 * Created by jaak on 16/10/15.
 */
public class CircleUtil {


    /**
     * Calculates points for location circle, useful for "My Location" feature
     *
     * @param location from Android Location API
     * @param proj     map projection, usually EPSG3857
     * @return MapPosVector to construct Polygon object, and add it to DataSource and Layer
     */
    public static MapPosVector createLocationCircle(Location location, Projection proj) {

        // number of points of circle
        int N = 50;
        int EARTH_RADIUS = 6378137;

        float radius = location.getAccuracy();
        double centerLat = location.getLatitude();
        double centerLon = location.getLongitude();

        MapPosVector points = new MapPosVector();
        for (int i = 0; i <= N; i++) {
            double angle = Math.PI * 2 * (i % N) / N;
            double dx = radius * Math.cos(angle);
            double dy = radius * Math.sin(angle);
            double lat = centerLat + (180 / Math.PI) * (dy / EARTH_RADIUS);
            double lon = centerLon + (180 / Math.PI) * (dx / EARTH_RADIUS) / Math.cos(centerLat * Math.PI / 180);
            points.add(proj.fromWgs84(new MapPos(lon, lat)));
        }
        return points;
    }
}
