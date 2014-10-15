package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.vectortiles.MBVectorTileDecoder;
import com.nutiteq.vectortiles.MBVectorTileStyleSet;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

/**
 * Base activity for vector map samples. Adds menu with multiple style choices.
 * 
 * @author jaak
 *
 */
public class VectorMapSampleBaseActivity extends MapSampleBaseActivity {

    protected MBVectorTileDecoder vectorTileDecoder;
    protected String tileUrl = Const.NUTITEQ_VECTOR_URL;
    protected String vectorStyle = Const.VECTOR_STYLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default base map - online vector with persistent caching
        UnsignedCharVector styleBytes = AssetUtils.loadBytes(vectorStyle);
        if(styleBytes != null){
            MBVectorTileStyleSet vectorTileStyleSet = new MBVectorTileStyleSet(styleBytes);
            vectorTileDecoder = new MBVectorTileDecoder(vectorTileStyleSet, "bright2d");
            TileDataSource vectorTileDataSource = new HTTPTileDataSource(
                    0, 14, tileUrl                    
                    );

            // we don't use vectorTileDataSource directly (this would be also option),
            // but via caching to cache data locally persistently
            // Note that this requires WRITE_EXTERNAL_STORAGE permission
            String cacheFile = getExternalFilesDir(null)+"/mapcache.db";
            Log.i(Const.LOG_TAG,"cacheFile = "+cacheFile);
            PersistentCacheTileDataSource cachedDataSource = 
                    new PersistentCacheTileDataSource(vectorTileDataSource, cacheFile);
            
            baseLayer = new VectorTileLayer(
                    cachedDataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
        } else {
            Log.e(Const.LOG_TAG, "map style file must be in project assets: "+vectorStyle);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add ("Basic").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	        vectorTileDecoder.setCurrentStyle("basic");
    	        return true;
    	    }
    	});
    	menu.add ("OSM Bright").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	        vectorTileDecoder.setCurrentStyle("bright2d");
    	        return true;
    	    }
    	});
    	menu.add ("OSM Bright 3D").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	        vectorTileDecoder.setCurrentStyle("bright3d");
    	        return true;
    	    }
    	});
    	menu.add ("Loose Leaf").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	        vectorTileDecoder.setCurrentStyle("looseleaf/style");
    	        return true;
    	    }
    	});
    	
    	return true;
    }
}
