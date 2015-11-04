package com.nutiteq.advancedmap3;

import java.io.File;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.core.MapVec;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.packagemanager.NutiteqPackageManager;
import com.nutiteq.packagemanager.PackageAction;
import com.nutiteq.packagemanager.PackageErrorType;
import com.nutiteq.packagemanager.PackageManager;
import com.nutiteq.packagemanager.PackageManagerListener;
import com.nutiteq.packagemanager.PackageStatus;
import com.nutiteq.routing.NutiteqOnlineRoutingService;
import com.nutiteq.routing.PackageManagerRoutingService;
import com.nutiteq.routing.RoutingAction;
import com.nutiteq.routing.RoutingInstruction;
import com.nutiteq.routing.RoutingRequest;
import com.nutiteq.routing.RoutingResult;
import com.nutiteq.routing.RoutingService;
import com.nutiteq.styles.BalloonPopupMargins;
import com.nutiteq.styles.BalloonPopupStyleBuilder;
import com.nutiteq.styles.LineJointType;
import com.nutiteq.styles.LineStyleBuilder;
import com.nutiteq.styles.MarkerStyle;
import com.nutiteq.styles.MarkerStyleBuilder;
import com.nutiteq.ui.ClickType;
import com.nutiteq.ui.MapClickInfo;
import com.nutiteq.ui.MapEventListener;
import com.nutiteq.ui.VectorElementsClickInfo;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.BalloonPopup;
import com.nutiteq.vectorelements.Line;
import com.nutiteq.vectorelements.Marker;
import com.nutiteq.vectorelements.NMLModel;
import com.nutiteq.wrappedcommons.MapPosVector;
import com.nutiteq.wrappedcommons.RoutingInstructionVector;

/**
 * A sample demonstrating how to use Nutiteq routing engine to calculate offline routes.
 * First a package is downloaded asynchronously. Until the package is fully downloaded,
 * online routing service is used as a fallback. Once the package is available, routing
 * works offline.
 */
public class OfflineRoutingActivity extends VectorMapSampleBaseActivity {

	/**
	 * This MapListener waits for two clicks on map - first to set routing start point, and then
	 * second to mark end point and start routing service.
	 */
	public class RouteMapEventListener extends MapEventListener {
	    private MapPos startPos;
	    private MapPos stopPos;

		@Override
	    public void onVectorElementClicked(VectorElementsClickInfo vectorElementsClickInfo) {
		}

		// Map View manipulation handlers
		@Override
		public void onMapClicked(MapClickInfo mapClickInfo) {

			if (mapClickInfo.getClickType() == ClickType.CLICK_TYPE_LONG) {
				MapPos clickPos = mapClickInfo.getClickPos();
				MapPos wgs84Clickpos = mapView.getOptions().getBaseProjection().toWgs84(clickPos);
				Log.d(Const.LOG_TAG,"onMapClicked " + wgs84Clickpos);

				if (startPos == null) {
					// set start, or start again
					startPos = clickPos;
					setStartMarker(clickPos);
				} else if (stopPos == null) {
					// set stop and calculate
					stopPos = clickPos;
					setStopMarker(clickPos);
					showRoute(startPos, stopPos);

					// restart to force new route next time
					startPos = null;
					stopPos = null;
				}
			}			
		}

		@Override
		public void onMapMoved() {
		}
	}

	/**
	 * Listener for package manager events. Contains only empty methods.
	 */
	class PackageListener extends PackageManagerListener {
    	@Override
    	public void onPackageListUpdated() {
			Log.d(Const.LOG_TAG, "Package list updated");
    		PackageStatus status = packageManager.getLocalPackageStatus(PACKAGE_ID, -1);
    		if (status == null) {
    			packageManager.startPackageDownload(PACKAGE_ID);
    		} else if (status.getCurrentAction() == PackageAction.PACKAGE_ACTION_READY) {
    			offlinePackageReady = true;
    		}
    	}
    	
		@Override
		public void onPackageListFailed() {
			Log.e(Const.LOG_TAG, "Package list update failed");
		}

		@Override
		public void onPackageStatusChanged(String id, int version, PackageStatus status) {
		}

		@Override
		public void onPackageCancelled(String id, int version) {
		}

    	@Override
    	public void onPackageUpdated(String id, int version) {
			Log.d(Const.LOG_TAG, "Offline package updated: " + id);
    		if (id.equals(PACKAGE_ID)) {
    			offlinePackageReady = true;        			
    		}
    	}

		@Override
		public void onPackageFailed(String id, int version, PackageErrorType errorType) {
			Log.e(Const.LOG_TAG, "Offline package update failed: " + id);
		}
	}
	
	private static String PACKAGE_ID = "EE-routing";
	private static String ROUTING_PACKAGEMANAGER_SOURCE = "routing:nutiteq.osm.car";
	private static String ROUTING_SERVICE_SOURCE = "nutiteq.osm.car";

    private RoutingService onlineRoutingService;
    private RoutingService offlineRoutingService;
    private PackageManager packageManager;
    private boolean shortestPathRunning;
    private boolean offlinePackageReady;
    private Marker startMarker;
    private Marker stopMarker;
    private MarkerStyle instructionUp;
    private MarkerStyle instructionLeft;
    private MarkerStyle instructionRight;
    private LocalVectorDataSource routeDataSource;
    private LocalVectorDataSource routeStartStopDataSource;
    private BalloonPopupStyleBuilder balloonPopupStyleBuilder;
    private NMLModel carModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // create PackageManager instance for dealing with offline packages
        File packageFolder = new File(getApplicationContext().getExternalFilesDir(null), "routingpackages");
        if (!(packageFolder.mkdirs() || packageFolder.isDirectory())) {
        	Log.e(Const.LOG_TAG, "Could not create package folder!");
        }
        packageManager = new NutiteqPackageManager(ROUTING_PACKAGEMANAGER_SOURCE, packageFolder.getAbsolutePath());
        packageManager.setPackageManagerListener(new PackageListener());
        packageManager.start();

        // fetch list of available packages from server. Note that this is asynchronous operation and listener will be notified via onPackageListUpdated when this succeeds.        
        packageManager.startPackageListDownload();
        
        // create offline routing service connected to package manager
        offlineRoutingService = new PackageManagerRoutingService(packageManager);
        
        // create additional online routing service that will be used when offline package is not yet downloaded or offline routing fails
        onlineRoutingService = new NutiteqOnlineRoutingService(ROUTING_SERVICE_SOURCE);

        // define layer and datasource for route line and instructions
        routeDataSource = new LocalVectorDataSource(baseProjection);
        VectorLayer routeLayer = new VectorLayer(routeDataSource);
        mapView.getLayers().add(routeLayer);


        // define layer and datasource for route start and stop markers
        routeStartStopDataSource = new LocalVectorDataSource(baseProjection);
        // Initialize a vector layer with the previous data source
        VectorLayer vectorLayer = new VectorLayer(routeStartStopDataSource);
        // Add the previous vector layer to the map
        mapView.getLayers().add(vectorLayer);
        // Set visible zoom range for the vector layer
        vectorLayer.setVisibleZoomRange(new MapRange(0, 22));


        // set route listener
        RouteMapEventListener mapListener = new RouteMapEventListener();
        mapView.setMapEventListener(mapListener);

        // create markers for start & end, and a layer for them
        MarkerStyleBuilder markerStyleBuilder = new MarkerStyleBuilder();
        markerStyleBuilder.setBitmap(BitmapUtils
                .createBitmapFromAndroidBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.olmarker)));
        markerStyleBuilder.setHideIfOverlapped(false);
        markerStyleBuilder.setSize(30);

        markerStyleBuilder.setColor(new com.nutiteq.graphics.Color(Color.GREEN));

        startMarker = new Marker(new MapPos(0, 0), markerStyleBuilder.buildStyle());
        startMarker.setVisible(false);


        markerStyleBuilder.setColor(new com.nutiteq.graphics.Color(Color.RED));

        stopMarker = new Marker(new MapPos(0, 0), markerStyleBuilder.buildStyle());
        stopMarker.setVisible(false);


        carModel = new NMLModel(new MapPos(0, 0), AssetUtils.loadBytes("tesla.nml"));
        carModel.setScale(5);

        routeStartStopDataSource.add(startMarker);
        routeStartStopDataSource.add(stopMarker);
        routeStartStopDataSource.add(carModel);

        markerStyleBuilder.setColor(new com.nutiteq.graphics.Color(Color.WHITE));
        markerStyleBuilder.setBitmap(BitmapUtils
                .createBitmapFromAndroidBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.direction_up)));
        instructionUp = markerStyleBuilder.buildStyle();

        markerStyleBuilder.setBitmap(BitmapUtils
                .createBitmapFromAndroidBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.direction_upthenleft)));
        instructionLeft = markerStyleBuilder.buildStyle();

        markerStyleBuilder.setBitmap(BitmapUtils
                .createBitmapFromAndroidBitmap(BitmapFactory.decodeResource(
                        getResources(), R.drawable.direction_upthenright)));

        instructionRight = markerStyleBuilder.buildStyle();
        
        // style for instruction balloons
        balloonPopupStyleBuilder = new BalloonPopupStyleBuilder();
        balloonPopupStyleBuilder.setTitleMargins(new BalloonPopupMargins(4,4,4,4));
        
        // finally animate map to Estonia
        mapView.setFocusPos(mapView.getOptions().getBaseProjection().fromWgs84(new MapPos(25.662893, 58.919365)), 0);
        mapView.setZoom(7, 0);
    }

    public void showRoute(final MapPos startPos, final MapPos stopPos) {
    	
        Log.d(Const.LOG_TAG, "calculating path " + startPos + " to " + stopPos);

        if (!offlinePackageReady) {
        	runOnUiThread(new Runnable() {
        		@Override
        		public void run() {
        			Toast.makeText(getApplicationContext(), "Offline package is not ready, using online routing", Toast.LENGTH_SHORT).show();
        		}
        	});
        }

        AsyncTask<Void, Void, RoutingResult> task = new AsyncTask<Void, Void, RoutingResult>() {

            protected RoutingResult doInBackground(Void... v) {
                MapPosVector poses = new MapPosVector();
                poses.add(startPos);
                poses.add(stopPos);
                RoutingRequest request = new RoutingRequest(mapView.getOptions().getBaseProjection(), poses);
                RoutingResult result;
                if (offlinePackageReady) {
            		result = offlineRoutingService.calculateRoute(request);
            	} else {
            		result = onlineRoutingService.calculateRoute(request);
            	}
                return result;
            }

            protected void onPostExecute(RoutingResult result) {
            	if (result == null) {
                    Toast.makeText(getApplicationContext(), "Routing failed", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(getApplicationContext(),
                    "The route is " + (int) (result.getTotalDistance() / 100) / 10f
                                + "km long, duration " + (int)result.getTotalTime()
                                + "s", Toast.LENGTH_LONG).show();

                routeDataSource.removeAll();

                startMarker.setVisible(false);

                routeDataSource.add(createPolyline(startMarker.getGeometry()
                        .getCenterPos(), stopMarker.getGeometry().getCenterPos(), result));

                // add instruction markers
                RoutingInstructionVector instructions = result.getInstructions();
                boolean first = true;
                for (int i = 0; i < instructions.size(); i++) {
                	RoutingInstruction instruction = instructions.get(i);
                    if (first) {
                        Log.d(Const.LOG_TAG,"first instruction");
                        // set car to first instruction position
                        first = false;
                        MapPos firstInstructionPos = result.getPoints().get(instruction.getPointIndex());
                        carModel.setPos(firstInstructionPos);

                        // rotate car based on first instruction leg azimuth
                        float azimuth =  (float) instruction.getAzimuth();
                        carModel.setRotation(new MapVec(0, 0, 1), 360 - azimuth);

                        // zoom and move map to the first position
                        mapView.setFocusPos(firstInstructionPos, 1);
                        mapView.setZoom(18, 1);
                        mapView.setMapRotation(360 - azimuth, firstInstructionPos, 1);
                        mapView.setTilt(30, 1);

                    } else {
                        Log.d(Const.LOG_TAG, instruction.toString());
                        createRoutePoint(result.getPoints().get(instruction.getPointIndex()), instruction.getStreetName(),
                                instruction.getTime(), instruction.getDistance(), instruction.getAction(), routeDataSource);
                    }

                }

                shortestPathRunning = false;
            }
        };

        if (!shortestPathRunning) {
        	shortestPathRunning = true;
        	task.execute();
        }
    }

    protected void createRoutePoint(MapPos pos, String name,
            double time, double distance, RoutingAction action, LocalVectorDataSource ds) {

        MarkerStyle style = instructionUp;
        String str = "";

        switch (action) {
        case ROUTING_ACTION_HEAD_ON:
            str = "head on";
            break;
         case ROUTING_ACTION_FINISH:
            str = "finish";
            break;
        case ROUTING_ACTION_TURN_LEFT:
            style = instructionLeft;
            str = "turn left";
            break;
        case ROUTING_ACTION_TURN_RIGHT:
            style = instructionRight;
            str = "turn right";
            break;
		case ROUTING_ACTION_UTURN:
			str = "u turn";
			break;
		case ROUTING_ACTION_NO_TURN:
        case ROUTING_ACTION_GO_STRAIGHT:
            style = instructionUp;
            str = "continue";
            break;
        case ROUTING_ACTION_REACH_VIA_LOCATION:
            style = instructionUp;
            str = "stopover";
            break;
		case ROUTING_ACTION_ENTER_AGAINST_ALLOWED_DIRECTION:
            str = "enter against allowed direction";
			break;
		case ROUTING_ACTION_LEAVE_AGAINST_ALLOWED_DIRECTION:
			break;
		case ROUTING_ACTION_ENTER_ROUNDABOUT:
			str = "enter roundabout";
			break;
		case ROUTING_ACTION_STAY_ON_ROUNDABOUT:
			str = "stay on roundabout";
			break;
		case ROUTING_ACTION_LEAVE_ROUNDABOUT:
			str = "leave roundabout";
			break;
		case ROUTING_ACTION_START_AT_END_OF_STREET:
			str = "start at end of street";
			break;
        }

        Marker marker = new Marker(pos, style);
        BalloonPopup popup2 = new BalloonPopup(marker, balloonPopupStyleBuilder.buildStyle(),
                str, "");
        ds.add(popup2);
        ds.add(marker);
    }

    // creates Nutiteq line from GraphHopper response
    protected Line createPolyline(MapPos start, MapPos end, RoutingResult result) {

        LineStyleBuilder lineStyleBuilder = new LineStyleBuilder();
        lineStyleBuilder.setColor(new com.nutiteq.graphics.Color(Color.DKGRAY));
        lineStyleBuilder.setLineJointType(LineJointType.LINE_JOINT_TYPE_ROUND);
        lineStyleBuilder.setStretchFactor(2);
        lineStyleBuilder.setWidth(12);

        return new Line(result.getPoints(), lineStyleBuilder.buildStyle());
    }

    public void setStartMarker(MapPos startPos) {
        routeDataSource.removeAll();
        stopMarker.setVisible(false);
        startMarker.setPos(startPos);

        //carModel.setPos(startPos);

        startMarker.setVisible(true);
    }

    public void setStopMarker(MapPos pos) {
        stopMarker.setPos(pos);
        stopMarker.setVisible(true);
    }

}
