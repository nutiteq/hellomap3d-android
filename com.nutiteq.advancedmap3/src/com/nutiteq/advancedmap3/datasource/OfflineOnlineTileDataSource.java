package com.nutiteq.advancedmap3.datasource;


import com.nutiteq.core.MapTile;
import com.nutiteq.core.TileData;
import com.nutiteq.datasources.TileDataSource;

/**
 * A custom raster tile data source that loads tiles from two sources: uses second one if tile does not exist in first.
 * Useful for offline-online hybrid source - if tile is not in offline, use online data, for example:
 * hybridDataSource = new OfflineOnlineTileDataSource(new PackageManagerTileDataSource(packageManager), new NutiteqOnlineTileDataSource("nutiteq.osm"));
 */
public class OfflineOnlineTileDataSource extends TileDataSource {
    private TileDataSource dataSource1;
    private TileDataSource dataSource2;

    public OfflineOnlineTileDataSource(TileDataSource dataSource1, TileDataSource dataSource2) {
        super(Math.min(dataSource1.getMinZoom(), dataSource2.getMinZoom()), Math.max(dataSource1.getMaxZoom(), dataSource2.getMaxZoom()));
        this.dataSource1 = dataSource1;
        this.dataSource2 = dataSource2;
    }

    public TileData loadTile(MapTile tile) {
        TileData tileData1 = dataSource1.loadTile(tile);

        if (tileData1 == null || tileData1.getData() == null || tileData1.isReplaceWithParent()) {
            return dataSource2.loadTile(tile);
        } else {
            return tileData1;
        }
    }

}
