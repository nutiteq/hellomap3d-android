package com.nutiteq.advancedmap3;

import java.io.File;
import java.io.FileFilter;

import android.os.Bundle;

import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.MBTilesTileDataSource;
import com.nutiteq.filepicker.FilePickerActivity;
import com.nutiteq.layers.RasterTileLayer;
import com.nutiteq.layers.VectorTileLayer;

/**
 * A sample that uses a specified MBTiles file for the base layer.
 * The sample assumes that the file name is specified using the Intent "selectedFile" extra field.
 */
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
        
        MBTilesTileDataSource tileDataSource = new MBTilesTileDataSource(
                0, 14, filePath);
        mapView.getLayers().remove(baseLayer);
        
        baseLayer = new RasterTileLayer(tileDataSource
                );
        mapView.getLayers().add(baseLayer);

        mapView.getOptions().setZoomRange(new MapRange(0, 18));
        mapView.setZoom(3, 0);

    }

    @Override
    public String getFileSelectMessage() {
        return "Select MBTiles file (raster)";
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
