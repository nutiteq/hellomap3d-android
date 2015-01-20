package com.nutiteq.advancedmap3.datasource;

import java.util.ArrayList;

import com.nutiteq.core.MapTile;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.core.TileData;

/**
 * A custom tile source that takes a list of tile data sources and uses tile frame number
 * for indexing the list when a tile is loaded.
 */
public class MyAnimatedTileDataSource extends TileDataSource {
	private ArrayList<TileDataSource> dataSources;
   
	public MyAnimatedTileDataSource(int minZoom, int maxZoom, ArrayList<TileDataSource> dataSources) {
		super(minZoom, maxZoom);
	    this.dataSources = dataSources;
	}

	public TileData loadTile(MapTile tile) {
		return dataSources.get(tile.getFrameNr()).loadTile(tile);
	}
}
