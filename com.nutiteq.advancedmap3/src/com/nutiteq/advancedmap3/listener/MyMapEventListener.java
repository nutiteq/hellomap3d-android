package com.nutiteq.advancedmap3.listener;

import java.util.Locale;

import android.util.Log;

import com.nutiteq.advancedmap3.Const;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.ScreenPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.styles.BalloonPopupMargins;
import com.nutiteq.styles.BalloonPopupStyleBuilder;
import com.nutiteq.ui.ClickType;
import com.nutiteq.ui.MapClickInfo;
import com.nutiteq.ui.MapEventListener;
import com.nutiteq.ui.MapView;
import com.nutiteq.ui.VectorElementClickInfo;
import com.nutiteq.ui.VectorElementsClickInfo;
import com.nutiteq.vectorelements.BalloonPopup;
import com.nutiteq.vectorelements.Billboard;
import com.nutiteq.vectorelements.VectorElement;

/**
 * A custom map event listener that displays information about map events and creates pop-ups.
 */
public class MyMapEventListener extends MapEventListener {
	private MapView mapView;
	private LocalVectorDataSource vectorDataSource;
	
	private BalloonPopup oldClickLabel;
	
	public MyMapEventListener(MapView mapView, LocalVectorDataSource vectorDataSource) {
		this.mapView = mapView;
		this.vectorDataSource = vectorDataSource;
	}

	@Override
	public void onMapMoved() {

        final MapPos topLeft = mapView.screenToMap(new ScreenPos(0, 0));
        final MapPos bottomRight = mapView.screenToMap(new ScreenPos(mapView.getWidth(), mapView.getHeight()));
        Log.d(Const.LOG_TAG, mapView.getOptions().getBaseProjection().toWgs84(topLeft)
                + " " + mapView.getOptions().getBaseProjection().toWgs84(bottomRight));

	}

	@Override
	public void onMapClicked(MapClickInfo mapClickInfo) {
		Log.d(Const.LOG_TAG, "Map click!");
		
		// Remove old click label
		if (oldClickLabel != null) {
			vectorDataSource.remove(oldClickLabel);
			oldClickLabel = null;
		}
		
		BalloonPopupStyleBuilder styleBuilder = new BalloonPopupStyleBuilder();
	    // Make sure this label is shown on top all other labels
	    styleBuilder.setPlacementPriority(10);
		
		// Check the type of the click
		String clickMsg = null;
		if (mapClickInfo.getClickType() == ClickType.CLICK_TYPE_SINGLE) {
			clickMsg = "Single map click!";
		} else if (mapClickInfo.getClickType() == ClickType.CLICK_TYPE_LONG) {
			clickMsg = "Long map click!";
		} else if (mapClickInfo.getClickType() == ClickType.CLICK_TYPE_DOUBLE) {
			clickMsg = "Double map click!";
		} else if (mapClickInfo.getClickType() == ClickType.CLICK_TYPE_DUAL) {
			clickMsg ="Dual map click!";
		}
	
		MapPos clickPos = mapClickInfo.getClickPos();
		MapPos wgs84Clickpos = mapView.getOptions().getBaseProjection().toWgs84(clickPos);
		String msg = String.format(Locale.US, "%.4f, %.4f", wgs84Clickpos.getY(), wgs84Clickpos.getX());
		BalloonPopup clickPopup = new BalloonPopup(mapClickInfo.getClickPos(),
												   styleBuilder.buildStyle(),
		                						   clickMsg,
		                						   msg);
		vectorDataSource.add(clickPopup);
		oldClickLabel = clickPopup;
	}

	@Override
	public void onVectorElementClicked(VectorElementsClickInfo vectorElementsClickInfo) {
		Log.d(Const.LOG_TAG, "Vector element click!");
		
		// Remove old click label
		if (oldClickLabel != null) {
			vectorDataSource.remove(oldClickLabel);
			oldClickLabel = null;
		}
		
		// Multiple vector elements can be clicked at the same time, we only care about the one
		// closest to the camera
		VectorElementClickInfo clickInfo = vectorElementsClickInfo.getVectorElementClickInfos().get(0);
		
		// Check the type of vector element
		BalloonPopup clickPopup = null;
		BalloonPopupStyleBuilder styleBuilder = new BalloonPopupStyleBuilder();
	    // Configure style
	    styleBuilder.setLeftMargins(new BalloonPopupMargins(0, 0, 0, 0));
	    styleBuilder.setTitleMargins(new BalloonPopupMargins(6, 3, 6, 3));
	    // Make sure this label is shown on top all other labels
	    styleBuilder.setPlacementPriority(10);

		VectorElement vectorElement = clickInfo.getVectorElement();
		String clickText = vectorElement.getMetaDataElement("ClickText");
		if (clickText == null || clickText.length() == 0) {
			return;
		}

		if (vectorElement instanceof Billboard) {
			// If the element is billboard, attach the click label to the billboard element
			Billboard billboard = (Billboard) vectorElement;
			clickPopup = new BalloonPopup(billboard, 
										  styleBuilder.buildStyle(),
		                    			  clickText, 
		                    			  "");
		} else {
			// for lines and polygons set label to click location
			clickPopup = new BalloonPopup(clickInfo.getElementClickPos(),
										  styleBuilder.buildStyle(),
		                   				  clickText,
		                    			  "");
		}
		vectorDataSource.add(clickPopup);
		oldClickLabel = clickPopup;
	}
}
