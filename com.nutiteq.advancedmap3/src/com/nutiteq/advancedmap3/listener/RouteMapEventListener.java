package com.nutiteq.advancedmap3.listener;

import android.util.Log;

import com.nutiteq.advancedmap3.Const;
import com.nutiteq.advancedmap3.GraphhopperRouteActivity;
import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.ui.MapClickInfo;
import com.nutiteq.ui.MapEventListener;
import com.nutiteq.ui.MapView;
import com.nutiteq.ui.VectorElementsClickInfo;

/**
 * 
 * This MapListener waits for two clicks on map - first to set routing start point, and then
 * second to mark end point and start routing service.
 * 
 * @author jaak
 *
 */
public class RouteMapEventListener extends MapEventListener {

	private GraphhopperRouteActivity activity;
    private MapPos startPos;
    private MapPos stopPos;
    private MapView mapView;
    private LocalVectorDataSource vectorDataSource;

	// activity is often useful to handle click events
	public RouteMapEventListener(GraphhopperRouteActivity activity, MapView mapView, LocalVectorDataSource vectorDataSource) {
		this.activity = activity;
	    this.mapView = mapView;
	    this.vectorDataSource = vectorDataSource;
	}

	@Override
    public void onVectorElementClicked(VectorElementsClickInfo vectorElementsClickInfo) {
	}

	// Map View manipulation handlers
	@Override
	public void onMapClicked(MapClickInfo mapClickInfo) {
		// x and y are in base map projection, we convert them to the familiar
		// WGS84
	    MapPos clickPos = mapClickInfo.getClickPos();
	    MapPos wgs84Clickpos = mapView.getOptions().getBaseProjection().toWgs84(clickPos);
		Log.d(Const.LOG_TAG,"onMapClicked " + wgs84Clickpos);
		
		if(startPos == null){
		    // set start, or start again
		    startPos = wgs84Clickpos;
		    activity.setStartMarker(clickPos);
		}else if(stopPos == null){
		    // set stop and calculate
		    stopPos = wgs84Clickpos;
		    activity.setStopMarker(clickPos);
	        activity.showRoute(startPos, stopPos);
		 
	        // restart to force new route next time
	        startPos = null;
	        stopPos = null;
		}
		
	}

	@Override
	public void onMapMoved() {
	}

}
