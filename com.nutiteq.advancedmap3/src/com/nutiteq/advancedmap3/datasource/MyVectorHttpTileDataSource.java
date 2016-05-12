package com.nutiteq.advancedmap3.datasource;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.nutiteq.core.MapTile;
import com.nutiteq.core.TileData;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.utils.Log;
import com.nutiteq.wrappedcommons.UnsignedCharVector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * A minimal custom vector tile data source which uses app-level HTTP requests
 * It supports e.g. HTTPS
 */
public class MyVectorHttpTileDataSource extends HTTPTileDataSource {

	public MyVectorHttpTileDataSource(int minZoom, int maxZoom, String baseURL) {
		super(minZoom, maxZoom, baseURL);
	}

	public TileData loadTile(MapTile tile) {

		String urlString = super.buildTileUrl(tile);

        Log.debug("requesting tile: "+urlString);

        try {
            URL url = new URL(urlString);
            UnsignedCharVector tileBinary = new UnsignedCharVector();

            InputStream is = url.openConnection().getInputStream();

            byte[] buffer = new byte[4096];
            int n;
            int len = 0;
            while ((n = is.read(buffer)) != -1) {
                for(int i=0;i<n;i++){
                    tileBinary.add(buffer[i]);
                }
                len+=n;
            }
            Log.debug("loaded bytes " + len);
            return new TileData(tileBinary);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
	}
}
