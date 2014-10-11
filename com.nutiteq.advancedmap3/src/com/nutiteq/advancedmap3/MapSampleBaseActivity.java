package com.nutiteq.advancedmap3;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.TileLayer;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.projections.EPSG3857;
import com.nutiteq.ui.MapView;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.vectortiles.MBVectorTileDecoder;
import com.nutiteq.vectortiles.MBVectorTileStyleSet;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

/**
 * Base activity for map samples. Includes basic MapView configurations.
 * Do not use for production - it does not have full lifecycle management
 * 
 * @author jaak
 *
 */
public class MapSampleBaseActivity extends Activity {

    protected MapView mapView;
    protected EPSG3857 baseProjection;
    protected TileLayer baseLayer;
    protected MBVectorTileDecoder vectorTileDecoder;

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
        mapView.setFocusPos(
                baseProjection.fromWgs84(new MapPos(13.38933, 52.51704)), 0); // berlin
        mapView.setZoom(2, 0);
        mapView.setMapRotation(0, 0);
        mapView.setTilt(90, 0);
        
        // Set default base map - online vector with persistent caching
        UnsignedCharVector styleBytes = AssetUtils.loadBytes(Const.VECTOR_STYLE);
        if(styleBytes != null){
            MBVectorTileStyleSet vectorTileStyleSet = new MBVectorTileStyleSet(styleBytes);
            vectorTileDecoder = new MBVectorTileDecoder(
                    vectorTileStyleSet);
            TileDataSource vectorTileDataSource = new HTTPTileDataSource(
                    0, 14, Const.NUTITEQ_URL                    
                    );

            // we don't use vectorTileDataSource directly (this would be also option),
            // but via caching to cache data locally persistently
            // Note that this requires WRITE_EXTERNAL_STORAGE permission
            // FIXME jaak: persistent caching seems to be unstable currently, disabled here
            String cacheFile = getExternalFilesDir(null)+"/mapcache.db";
            Log.i(Const.LOG_TAG,"cacheFile = "+cacheFile);
            PersistentCacheTileDataSource cachedDataSource = 
                    new PersistentCacheTileDataSource(vectorTileDataSource, cacheFile);
            
            baseLayer = new VectorTileLayer(
                    vectorTileDataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
        }else
        {
            Log.e(Const.LOG_TAG, "map style file must be in project assets: "+Const.VECTOR_STYLE);
        }
        
    }
}
