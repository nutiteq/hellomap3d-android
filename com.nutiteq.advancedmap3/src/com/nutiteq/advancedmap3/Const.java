package com.nutiteq.advancedmap3;

public class Const {
	public static final String LOG_TAG = "nutiteq-android-sample";
	public static final String NUTITEQ_API_KEY = "15cd9131072d6df68b8a54feda5b0496";
	public static final String NUTITEQ_VECTOR_URL = "http://api.nutiteq.com/v1/nutiteq.mbstreets/{zoom}/{x}/{y}.vt?user_key=" + NUTITEQ_API_KEY;
	//public static final String NUTITEQ_VECTOR_URL = "http://212.71.237.126/v1/nutiteq.mbstreets/{zoom}/{x}/{y}.vt";
	//public static final String NUTITEQ_VECTOR_URL = "http://a.tiles.mapbox.com/v4/mapbox.mapbox-streets-v5/{zoom}/{x}/{y}.vector.pbf?access_token=pk.eyJ1IjoibnV0aXRlcSIsImEiOiJVQnF2Wk9jIn0.EKSkygY5CkVp-I8byaquBQ";
	public static final String MAPBOX_RASTER_URL = "http://api.tiles.mapbox.com/v3/nutiteq.map-j6a1wkx0/{zoom}/{x}/{y}.png";
	public static final String HILLSHADE_RASTER_URL = "http://tiles.wmflabs.org/hillshading/{zoom}/{x}/{y}.png";
}
