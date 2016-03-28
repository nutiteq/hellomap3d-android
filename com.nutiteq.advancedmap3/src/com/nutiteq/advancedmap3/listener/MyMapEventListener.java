package com.nutiteq.advancedmap3.listener;

import java.util.Locale;

import android.util.Log;

import com.nutiteq.advancedmap3.Const;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.ScreenPos;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.UTFGridRasterTileLayer;
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
import com.nutiteq.wrappedcommons.StringMap;

/**
 * A custom map event listener that displays information about map events and creates pop-ups.
 */
public class MyMapEventListener extends MapEventListener {
	private MapView mapView;
	private LocalVectorDataSource vectorDataSource;
	
	private BalloonPopup oldClickLabel;
	private UTFGridRasterTileLayer gridLayer;

	public MyMapEventListener(MapView mapView, LocalVectorDataSource vectorDataSource) {
		this.mapView = mapView;
		this.vectorDataSource = vectorDataSource;
	}

	@Override
	public void onMapMoved() {

        final MapPos topLeft = mapView.screenToMap(new ScreenPos(0, 0));
        final MapPos bottomRight = mapView.screenToMap(new ScreenPos(mapView.getWidth(), mapView.getHeight()));


		MapPos mapPos = mapView.getOptions().getBaseProjection().fromWgs84(new MapPos(0, 0));
		ScreenPos screenPos = mapView.mapToScreen(mapPos);

		Log.d(Const.LOG_TAG, mapView.getOptions().getBaseProjection().toWgs84(topLeft)
                + " " + mapView.getOptions().getBaseProjection().toWgs84(bottomRight));

		Log.d(Const.LOG_TAG, "screen for 0,0 : " + screenPos.getX()+ " "+screenPos.getY());


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


		String msg = "";

		// if UtfGridLayer is set, then try to show all metadata from it
		if(this.gridLayer != null){

			StringMap utfData = this.gridLayer.getTooltips(clickPos,true);
			for (int i=0; i<utfData.size();i++){
				msg += utfData.get_key(i)+ ": "+utfData.get(utfData.get_key(i))+ "\n";
			}

		}

		// finally show click coordinates also
		MapPos wgs84Clickpos = mapView.getOptions().getBaseProjection().toWgs84(clickPos);
		msg  += String.format(Locale.US, "%.4f, %.4f", wgs84Clickpos.getY(), wgs84Clickpos.getX());



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

		// show all metadata elements
		StringMap stringMap = vectorElement.getMetaData();
		StringBuilder msgBuilder = new StringBuilder();
		if (stringMap.size() > 0) {
			for (int i = 0; i < stringMap.size(); i++) {
				Log.d(Const.LOG_TAG, "" + stringMap.get_key(i) + " = " + stringMap.get(stringMap.get_key(i)));
				if(!stringMap.get_key(i).equals("ClickText")){
					msgBuilder.append(stringMap.get_key(i));
					msgBuilder.append("=");
					msgBuilder.append(stringMap.get(stringMap.get_key(i)));
					msgBuilder.append("\n");
				}
			}
		}
		String desc = msgBuilder.toString().trim();

//		if ((clickText == null || clickText.length() == 0) && desc.length() == 0) {
//			return;
//		}

		if (vectorElement instanceof Billboard) {
			// If the element is billboard, attach the click label to the billboard element
			Billboard billboard = (Billboard) vectorElement;
			clickPopup = new BalloonPopup(billboard, 
										  styleBuilder.buildStyle(),
		                    			  clickText, 
		                    			  desc);
		} else {
			// for lines and polygons set label to click location
			clickPopup = new BalloonPopup(clickInfo.getElementClickPos(),
										  styleBuilder.buildStyle(),
		                   				  clickText,
					desc);
		}
		vectorDataSource.add(clickPopup);
		oldClickLabel = clickPopup;
	}

	public void setGridLayer(UTFGridRasterTileLayer gridLayer) {
		this.gridLayer = gridLayer;
	}
}
