package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

import com.nutiteq.datasources.CompressedCacheTileDataSource;
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
    protected boolean persistentTileCache = false;
    
    // Style parameters
    protected String vectorStyleName = "osmbright"; // default style name, each style has corresponding .zip asset
    protected boolean vectorStyleBuildings3D = false; // OSM Bright style can be optionally used with 3D buildings
    protected String vectorStyleLang = "en"; // default map language

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set default base map - online vector with persistent caching
        updateBaseLayer();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
    	Menu langMenu = menu.addSubMenu("Language");
    	langMenu.add ("English").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleLang = "en";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	langMenu.add ("German").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleLang = "de";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	langMenu.add ("French").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleLang = "fr";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	langMenu.add ("Russian").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleLang = "ru";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
   	
    	Menu styleMenu = menu.addSubMenu("Style");
    	styleMenu.add ("Basic").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleName = "basic";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	styleMenu.add ("OSM Bright 2D").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleName = "osmbright";
    	    	vectorStyleBuildings3D = false;
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	styleMenu.add ("OSM Bright 3D").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleName = "osmbright";
    	    	vectorStyleBuildings3D = true;
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	styleMenu.add ("Loose Leaf").setOnMenuItemClickListener (new OnMenuItemClickListener(){
    	    @Override
    	    public boolean onMenuItemClick (MenuItem item){
    	    	vectorStyleName = "looseleaf";
    	    	updateBaseLayer();
    	        return true;
    	    }
    	});
    	
    	return true;
    }
    
    private void updateBaseLayer() {
        UnsignedCharVector styleBytes = AssetUtils.loadBytes(vectorStyleName + ".zip");
        if (styleBytes != null){
        	// Create style set
            MBVectorTileStyleSet vectorTileStyleSet = new MBVectorTileStyleSet(styleBytes);
            vectorTileDecoder = new MBVectorTileDecoder(vectorTileStyleSet);
            
            // Set language, language-specific texts from vector tiles will be used
            vectorTileDecoder.setStyleParameter("lang", vectorStyleLang);
            
            // OSM Bright style set supports choosing between 2d/3d buildings. Set corresponding parameter.
            if (vectorStyleName.equals("osmbright")) {
            	vectorTileDecoder.setStyleParameter("buildings3d", vectorStyleBuildings3D);
            }
            
            // Create tile data source for vector tiles
            TileDataSource vectorTileDataSource = new HTTPTileDataSource(0, 14, tileUrl);

            // We don't use vectorTileDataSource directly (this would be also option),
            // but via caching to cache data locally persistently/non-persistently
            // Note that persistent cache requires WRITE_EXTERNAL_STORAGE permission
            TileDataSource cacheDataSource;
            if (persistentTileCache) {
            	String cacheFile = getExternalFilesDir(null)+"/mapcache.db";
            	Log.i(Const.LOG_TAG,"cacheFile = "+cacheFile);
            	cacheDataSource = new PersistentCacheTileDataSource(vectorTileDataSource, cacheFile);
            } else {
            	cacheDataSource = new CompressedCacheTileDataSource(vectorTileDataSource);
            }

            // Remove old base layer, create new base layer
            if (baseLayer != null) {
            	mapView.getLayers().remove(baseLayer);
            }
            baseLayer = new VectorTileLayer(cacheDataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
        } else {
            Log.e(Const.LOG_TAG, "map style file must be in project assets: "+vectorStyleName);        	
        }
    }
}
