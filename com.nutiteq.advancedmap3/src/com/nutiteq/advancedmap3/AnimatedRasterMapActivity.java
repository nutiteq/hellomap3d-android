package com.nutiteq.advancedmap3;

import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;

import com.nutiteq.advancedmap3.datasource.MyAnimatedTileDataSource;
import com.nutiteq.datasources.CompressedCacheTileDataSource;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.RasterTileLayer;
import com.nutiteq.layers.TileLoadListener;

public class AnimatedRasterMapActivity extends MapSampleBaseActivity {

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private static final int ANIMATION_FRAME_TIME_MS = 300;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);
        
        mapView.setZoom(6, 0);
        
        // Animated raster tile datasource
        final int[] hours = new int[]{5, 7, 9, 11, 15, 19, 23, 27};
        ArrayList<TileDataSource> animatedRasterTileDataSources = new ArrayList<TileDataSource>();
        for (int hour : hours) {
            CompressedCacheTileDataSource dataSource = new CompressedCacheTileDataSource( 
                    new HTTPTileDataSource(4, 7, "http://www.openportguide.org/tiles/actual/wind_vector/" + hour + "/{zoom}/{x}/{y}.png"));
            // Reduce the size a bit (default is 6 mb)
            dataSource.setCapacity((long) (0.5f * 1024 * 1024));
            animatedRasterTileDataSources.add(dataSource);
        }
        MyAnimatedTileDataSource animatedRasterTileDataSource = new MyAnimatedTileDataSource(0, 24, animatedRasterTileDataSources);
        
        // Initialize an animated raster layer
        final RasterTileLayer animatedRasterTileLayer = new RasterTileLayer( animatedRasterTileDataSource);
        animatedRasterTileLayer.setSynchronizedRefresh(true);
        animatedRasterTileLayer.setPreloading(false);
        // Set the tile load listener, which will be used to change the animation frames
        animatedRasterTileLayer.setTileLoadListener(new TileLoadListener() {
            private boolean inProgress;
            
            public void onVisibleTilesLoaded() {
                // All visible tiles have been loaded, change the frame 
                Runnable task = new Runnable() {
                    public void run() {
                        synchronized (worker) {
                            inProgress = false;
                            animatedRasterTileLayer.setFrameNr((animatedRasterTileLayer.getFrameNr() + 1) % hours.length);
                        }
                    }
                };
                synchronized (worker) {
                    if (!inProgress) {
                        inProgress = true;
                        worker.schedule(task, ANIMATION_FRAME_TIME_MS, TimeUnit.MILLISECONDS);
                    }
                }
            }
            
            public void onPreloadingTilesLoaded() {}
        });
        // Add the previous raster layer to the map
        mapView.getLayers().add(animatedRasterTileLayer);
        
    }
}
