package com.nutiteq.advancedmap3;

import android.os.Bundle;
import android.util.Log;

import com.nutiteq.advancedmap3.listener.MyMapEventListener;
import com.nutiteq.core.MapPos;
import com.nutiteq.core.MapRange;
import com.nutiteq.datasources.HTTPTileDataSource;
import com.nutiteq.datasources.LocalVectorDataSource;
import com.nutiteq.datasources.PersistentCacheTileDataSource;
import com.nutiteq.datasources.TileDataSource;
import com.nutiteq.layers.TorqueTileLayer;
import com.nutiteq.layers.UTFGridRasterTileLayer;
import com.nutiteq.layers.VectorLayer;
import com.nutiteq.vectortiles.CartoCSSStyleSet;
import com.nutiteq.vectortiles.TorqueTileDecoder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A sample demonstrating how to use CartoDB Torque tiles with CartoCSS styling
 */
public class CartoDBTorqueActivity extends VectorMapSampleBaseActivity {

    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private static final long TORQUE_FRAMETIME_MS = 100;
    private TorqueTileLayer torqueTileLayer;
    private boolean inProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // MapSampleBaseActivity creates and configures mapView  
        super.onCreate(savedInstanceState);

        String cartoCss = "#layer {\n"+
                "  comp-op: lighten;\n"+
                "  marker-type:ellipse;\n"+
                "  marker-width: 10;\n"+
                "  marker-fill: #FEE391;\n"+
                "  [value > 2] { marker-fill: #FEC44F; }\n"+
                "  [value > 3] { marker-fill: #FE9929; }\n"+
                "  [value > 4] { marker-fill: #EC7014; }\n"+
                "  [value > 5] { marker-fill: #CC4C02; }\n"+
                "  [value > 6] { marker-fill: #993404; }\n"+
                "  [value > 7] { marker-fill: #662506; }\n"+
                "\n"+
                "  [frame-offset = 1] {\n"+
                "    marker-width: 20;\n"+
                "    marker-fill-opacity: 0.1;\n"+
                "  }\n"+
                "  [frame-offset = 2] {\n"+
                "    marker-width: 30;\n"+
                "    marker-fill-opacity: 0.05;\n"+
                "  }\n"+
                "}\n";


        // magic query to create torque tiles
        String query = "WITH par \n" +
                "AS (SELECT Cdb_xyz_resolution({zoom}) * 1   AS res,\n" +
                "256 / 1 AS tile_size,\n" +
                "Cdb_xyz_extent({x}, {y}, {zoom}) AS ext),\n" +
                "cte\n" +
                "AS (SELECT St_snaptogrid(i.the_geom_webmercator, p.res) g,\n" +
                " Count(cartodb_id) c,\n" +
                " Floor(( Date_part('epoch', date) - -1796072400 ) / 476536.5) d\n" +
                "FROM (SELECT *\n" +
                " FROM ow) i,\n" +
                "  par p\n" +
                " WHERE i.the_geom_webmercator && p.ext\n" +
                " GROUP BY g, d)\n" +
                "SELECT ( St_x(g) - St_xmin(p.ext) ) / p.res x__uint8,\n" +
                " ( St_y(g) - St_ymin(p.ext) ) / p.res y__uint8,\n" +
                " Array_agg(c) vals__uint8,\n" +
                " Array_agg(d) dates__uint16\n" +
                "FROM cte,\n" +
                "  par p\n" +
                "WHERE  ( St_y(g) - St_ymin(p.ext) ) / p.res < tile_size\n" +
                " AND ( St_x(g) - St_xmin(p.ext) ) / p.res < tile_size\n" +
                "GROUP BY x__uint8,\n" +
                " y__uint8 ";

        String encodedQuery = null;
        try {
            encodedQuery = URLEncoder.encode(query.replace("\n",""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        encodedQuery = "WITH%20par%20AS%20(%20%20SELECT%20CDB_XYZ_Resolution({zoom})*1%20as%20res%2C%20%20256%2F1%20as%20tile_size%2C%20CDB_XYZ_Extent({x}%2C%20{y}%2C%20{zoom})%20as%20ext%20)%2Ccte%20AS%20(%20%20%20SELECT%20ST_SnapToGrid(i.the_geom_webmercator%2C%20p.res)%20g%2C%20count(cartodb_id)%20c%2C%20floor((date_part(%27epoch%27%2C%20date)%20-%20-1796072400)%2F476536.5)%20d%20%20FROM%20(select%20*%20from%20ow)%20i%2C%20par%20p%20%20%20WHERE%20i.the_geom_webmercator%20%26%26%20p.ext%20%20%20GROUP%20BY%20g%2C%20d)%20SELECT%20(st_x(g)-st_xmin(p.ext))%2Fp.res%20x__uint8%2C%20%20%20%20%20%20%20%20(st_y(g)-st_ymin(p.ext))%2Fp.res%20y__uint8%2C%20array_agg(c)%20vals__uint8%2C%20array_agg(d)%20dates__uint16%20FROM%20cte%2C%20par%20p%20where%20(st_y(g)-st_ymin(p.ext))%2Fp.res%20%3C%20tile_size%20and%20(st_x(g)-st_xmin(p.ext))%2Fp.res%20%3C%20tile_size%20GROUP%20BY%20x__uint8%2C%20y__uint8&last_updated=1970-01-01T00%3A00%3A00.000Z";
        // define datasource with the query
        HTTPTileDataSource torqueDataSource = new HTTPTileDataSource(0, 14,
                "http://viz2.cartodb.com/api/v2/sql?q="+encodedQuery+"&cache_policy=persist");

        // create persistent cache to make it faster
        String cacheFile = getExternalFilesDir(null)+"/torque_tile_cache.db";
        Log.i(Const.LOG_TAG, "cacheFile = " + cacheFile);
        TileDataSource cacheDataSource = new PersistentCacheTileDataSource(torqueDataSource, cacheFile);

        // Create CartoCSS style from Torque points
        CartoCSSStyleSet torqueStyleSet = new CartoCSSStyleSet(cartoCss);

        // Create tile decoder and Torque layer
        TorqueTileDecoder torqueDecoder = new TorqueTileDecoder(torqueStyleSet);

        torqueTileLayer = new TorqueTileLayer(cacheDataSource, torqueDecoder);

        mapView.getLayers().add(torqueTileLayer);

        // Start updating frames for animation

        synchronized (worker) {
            if (!inProgress) {
                inProgress = true;
               worker.schedule(task, TORQUE_FRAMETIME_MS, TimeUnit.MILLISECONDS);
            }
        }

        mapView.setZoom(1, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private Runnable task = new Runnable() {
        public void run() {
                synchronized (worker) {
                        inProgress = false;
                        torqueTileLayer.setFrameNr((torqueTileLayer.getFrameNr()+1) % torqueTileLayer.getFrameCount());
                        Log.d(Const.LOG_TAG, "torque frame " + torqueTileLayer.getFrameNr()+ " of "+torqueTileLayer.getFrameCount());
                        worker.schedule(task, TORQUE_FRAMETIME_MS, TimeUnit.MILLISECONDS);
                }
        }
    };

}
