package com.nutiteq.advancedmap3;

import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.MBTilesTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.hellomap3.util.AssetCopy;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.vectortiles.MBVectorTileDecoder;
import com.nutiteq.vectortiles.MBVectorTileStyleSet;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

/**
 * Uses sample offline vector map package from assets
 * 
 * @author jaak
 *
 */
public class OfflineVectorMapActivity extends MapSampleBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        // offline map data source
        String mbTileFile = "world_ntvt_0_4.mbtiles";

        try {
            String localDir = getExternalFilesDir(null).toString();
            AssetCopy.copyAssetToSDCard(getAssets(), mbTileFile, localDir);
            Log.i(Const.LOG_TAG,"copy done to " + localDir + "/"
                    + mbTileFile);
            MBTilesTileDataSource vectorTileDataSource = new MBTilesTileDataSource(0, 4, localDir + "/"
                    + mbTileFile);
            mapView.getLayers().remove(baseLayer);
            baseLayer = new VectorTileLayer(
                    vectorTileDataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
            
            mapView.getOptions().setZoomRange(new MapRange(0,6));
            mapView.setZoom(3, 0);
            
        } catch (IOException e) {
            Log.e(Const.LOG_TAG, "mbTileFile cannot be copied: "+mbTileFile);
            Log.e(Const.LOG_TAG, e.getLocalizedMessage());
        }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(final int featureId, final MenuItem item) {

        item.setChecked(true);

        switch (item.getItemId()) {

        // map styles
        case R.id.menu_osmbright:
            changeBaseMapStyle("osmbright.zip");
            break;
        case R.id.menu_osmbright_ru:
            changeBaseMapStyle("osmbright_ru.zip");
            break;
        case R.id.menu_highcontrast:
            changeBaseMapStyle("highcontrast.zip");
            break;
        case R.id.menu_looseleaf:
            changeBaseMapStyle("looseleaf.zip");
            break;
        case R.id.menu_pirates:
            changeBaseMapStyle("pirates.zip");
            break;

        }

        return true;

    }

    private void changeBaseMapStyle(String styleFile) {
        // re-define decoder
        UnsignedCharVector styleBytes = AssetUtils.loadBytes(styleFile);
        if(styleBytes != null){
            MBVectorTileStyleSet vectorTileStyleSet = new MBVectorTileStyleSet(styleBytes);
            vectorTileDecoder = new MBVectorTileDecoder(
                    vectorTileStyleSet);

            TileDataSource dataSource = baseLayer.getDataSource();
            
            // reload layer to refresh decoder
            mapView.getLayers().remove(baseLayer);
            baseLayer = new VectorTileLayer(
                    dataSource, vectorTileDecoder);
            mapView.getLayers().add(baseLayer);
            mapView.requestRender();
            
        }else{
            Log.e(Const.LOG_TAG, "map style file must be in project assets: "+styleFile);
        }
        
    }

    
}
