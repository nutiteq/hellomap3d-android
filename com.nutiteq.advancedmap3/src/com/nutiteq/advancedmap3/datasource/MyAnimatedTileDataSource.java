package com.nutiteq.advancedmap3.datasource;

import java.util.ArrayList;

import com.nutiteq.core.MapTile;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.core.TileData;

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
