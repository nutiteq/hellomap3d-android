package com.nutiteq.advancedmap3;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.layers.TileLayer;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.projections.Projection;
import com.nutiteq.ui.MapView;

/**
 * Base activity for map samples. Includes simple lifecycle management
 * 
 * @author jaak
 *
 */
public class MapSampleBaseActivity extends Activity {

    protected MapView mapView;
    protected Projection baseProjection;
    protected TileLayer baseLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 1. Basic map setup
        // Create map view 
        mapView = (MapView) this.findViewById(R.id.map_view);
        com.nutiteq.utils.Log.SetShowDebug(true);
        com.nutiteq.utils.Log.SetShowInfo(true);
        
        // Set the base projection, that will be used for most MapView, MapEventListener and Options methods
        baseProjection = new EPSG3857();
        mapView.getOptions().setBaseProjection(baseProjection);
        
        // General options
        mapView.getOptions().setTileDrawSize(256);
//        mapView.getOptions().setTileThreadPoolSize(4); 

        // Review following and change if needed
        mapView.getOptions().setRotatable(true);
        mapView.getOptions().setZoomRange(new MapRange(0, 18));

        Log.d(Const.LOG_TAG, "autoconfigured DPI="+mapView.getOptions().getDPI());

        // Set default location
//      mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(24.650415, 59.420773)), 0); // tallinn
//      mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(19.04468, 47.4965)), 0); // budapest
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(13.38933, 52.51704)), 0); // berlin
        mapView.setZoom(2, 0);
        mapView.setMapRotation(0, 0);
        mapView.setTilt(90, 0);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle bundle) {
    	MapPos focusPos = new MapPos(bundle.getDouble("focusX"), bundle.getDouble("focusY"));
    	mapView.setFocusPos(focusPos, 0);
    	mapView.setZoom(bundle.getFloat("zoom"), 0);
    	mapView.setMapRotation(bundle.getFloat("rotation"), 0);
    	mapView.setTilt(bundle.getFloat("tilt"), 0);
    }
    
    @Override
    public void onSaveInstanceState(Bundle bundle) {
    	MapPos focusPos = mapView.getFocusPos();
    	bundle.putDouble("focusX", focusPos.getX());
    	bundle.putDouble("focusY", focusPos.getY());
    	bundle.putFloat("zoom", mapView.getZoom());
    	bundle.putFloat("rotation", mapView.getMapRotation());
    	bundle.putFloat("tilt", mapView.getTilt());
    }
}
