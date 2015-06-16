package com.nutiteq.advancedmap3.datasource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nutiteq.core.MapTile;
import com.nutiteq.core.TileData;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.utils.Log;

import java.io.IOException;
import java.net.URL;

/**
 * A minimal custom raster tile data source which uses app-level HTTP requests
 * It supports e.g. HTTPS
 */
public class MyHttpTileDataSource extends HTTPTileDataSource {

	public MyHttpTileDataSource(int minZoom, int maxZoom, String baseURL) {
		super(minZoom, maxZoom, baseURL);
	}

	public TileData loadTile(MapTile tile) {

		String urlString = super.buildTileUrl(tile);

        Log.debug("requesting tile: "+urlString);

        Bitmap bmp = null;
        try {
            URL url = new URL(urlString);
            bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new TileData(BitmapUtils.createBitmapFromAndroidBitmap(bmp).compressToInternal());
	}
	
}
