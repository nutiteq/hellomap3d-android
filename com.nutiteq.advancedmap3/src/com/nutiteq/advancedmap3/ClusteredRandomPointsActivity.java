package com.nutiteq.advancedmap3;

import android.annotation.SuppressLint;
import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Align;
import android.os.Bundle;

import com.nutiteq.core.MapPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.ClusterElementBuilder;
import com.nutiteq.layers.ClusteredVectorLayer;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Marker;
import com.nutiteq.vectorelements.VectorElement;
import com.nutiteq.wrappedcommons.VectorElementVector;

import java.util.HashMap;
import java.util.Map;

/**
 * A sample demonstrating how to use marker clustering on the map. This demo creates
 * 1000 randomly positioned markers on the map and uses SDKs built-in clustering layer
 * with custom 'ClusterElementBuilder' implementation that draws the number of cluster elements
 * dynamically on a marker bitmap.
 *
 * The custom cluster element builder also caches created marker styles, to conserve memory
 * usage and to avoid redundant calculations.
 *
 * The sample also uses a custom map listener that expands cluster elements that are clicked on, if
 * the number of elements in the cluster is less than or equal to 5.
 */
public class ClusteredRandomPointsActivity extends VectorMapSampleBaseActivity {

	private static class MyClusterElementBuilder extends ClusterElementBuilder {
		@SuppressLint("UseSparseArrays")
		private Map<Integer, MarkerStyle> markerStyles = new HashMap<Integer, MarkerStyle>();
		private android.graphics.Bitmap markerBitmap;
		
		MyClusterElementBuilder(Application context) {
			markerBitmap = android.graphics.Bitmap.createBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.marker_black));
		}

		@Override
		public VectorElement buildClusterElement(MapPos pos, VectorElementVector elements) {
			// Try to reuse existing marker styles
			MarkerStyle style = markerStyles.get((int) elements.size());
			if (elements.size() == 1) {
				style = ((Marker) elements.get(0)).getStyle();
			}
			if (style == null) {
				android.graphics.Bitmap canvasBitmap = markerBitmap.copy(android.graphics.Bitmap.Config.ARGB_8888, true);
				android.graphics.Canvas canvas = new android.graphics.Canvas(canvasBitmap); 
				android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
				paint.setTextAlign(Align.CENTER);
				paint.setTextSize(12);
				paint.setColor(android.graphics.Color.argb(255, 0, 0, 0));
				canvas.drawText(Integer.toString((int) elements.size()), markerBitmap.getWidth() / 2, markerBitmap.getHeight() / 2 - 5, paint);
				MarkerStyleBuilder styleBuilder = new MarkerStyleBuilder();
				styleBuilder.setBitmap(BitmapUtils.createBitmapFromAndroidBitmap(canvasBitmap));
				styleBuilder.setSize(30);
		        styleBuilder.setPlacementPriority((int)-elements.size());
				style = styleBuilder.buildStyle();
				markerStyles.put((int) elements.size(), style);
			}

			// Create marker for the cluster
			Marker marker = new Marker(pos, style);
			return marker;
		}
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        // Move to zoom
        mapView.setZoom(7.5f, 0.0f);
        mapView.setFocusPos(baseProjection.fromWgs84(new MapPos(24.646469, 59.426939)), 0.0f);

        // 1. Initialize a local vector data source
        LocalVectorDataSource vectorDataSource1 = new LocalVectorDataSource(baseProjection);
        
        // 2. Create marker style
        Bitmap androidMarkerBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.marker_red);
        com.nutiteq.graphics.Bitmap markerBitmap = BitmapUtils.createBitmapFromAndroidBitmap(androidMarkerBitmap);
        
        MarkerStyleBuilder markerStyleBuilder = new MarkerStyleBuilder();
        markerStyleBuilder.setBitmap(markerBitmap);
        markerStyleBuilder.setHideIfOverlapped(false);
        markerStyleBuilder.setSize(30);
        MarkerStyle sharedMarkerStyle = markerStyleBuilder.buildStyle();

        // Create 1000 random points
        for (int i = 0; i < 1000; i++) {
            double x = Math.random();
            double y = Math.random();
            MapPos pos = baseProjection.fromWgs84(new MapPos(24.646469 + x, 59.426939 + y)); // Tallinn
            Marker marker = new Marker(pos, sharedMarkerStyle);
            vectorDataSource1.add(marker);
        }
        
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer1 = new ClusteredVectorLayer(vectorDataSource1, new MyClusterElementBuilder(this.getApplication()));

        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer1);
    }
}
