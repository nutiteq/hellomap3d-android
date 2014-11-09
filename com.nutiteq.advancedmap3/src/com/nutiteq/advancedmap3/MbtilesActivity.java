package com.nutiteq.advancedmap3;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import android.os.Bundle;
import android.util.Log;

import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.MBTilesTileDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.filepicker.FilePickerActivity;
import com.nutiteq.hellomap3.util.AssetCopy;
import com.nutiteq.layers.VectorTileLayer;
import com.nutiteq.utils.AssetUtils;
import com.nutiteq.vectortiles.MBVectorTileDecoder;
import com.nutiteq.vectortiles.MBVectorTileStyleSet;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

public class MbtilesActivity extends VectorMapSampleBaseActivity implements
        FilePickerActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // read filename from extras
        Bundle b = getIntent().getExtras();
        String filePath = b.getString("selectedFile");

        // TODO: detect if raster or vector mtiles
        // TODO: read metadata: zoom range, default zoom and center
        //    query from data if metadata not available
        
        // replace baseLayer with new datasource
        
        MBTilesTileDataSource vectorTileDataSource = new MBTilesTileDataSource(
                0, 14, filePath);
        mapView.getLayers().remove(baseLayer);
        
        baseLayer = new VectorTileLayer(vectorTileDataSource,
                vectorTileDecoder);
        mapView.getLayers().add(baseLayer);

        mapView.getOptions().setZoomRange(new MapRange(0, 18));
        mapView.setZoom(3, 0);

    }

    @Override
    public String getFileSelectMessage() {
        return "Select MBTiles file (vector tiles or raster)";
    }

    @Override
    public FileFilter getFileFilter() {
        return new FileFilter() {
            @Override
            public boolean accept(File file) {
                // accept only readable files
                if (file.canRead()) {
                    if (file.isDirectory()) {
                        // allow to select any directory
                        return true;
                    } else if (file.isFile()
                            && file.getName().endsWith(".mbtiles")) {
                        // accept files with given extension
                        return true;
                    }
                }
                return false;
            };
        };
    }

}
