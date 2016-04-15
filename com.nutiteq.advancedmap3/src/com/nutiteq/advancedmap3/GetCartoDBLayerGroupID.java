package com.nutiteq.advancedmap3;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class GetCartoDBLayerGroupID {

    private URL url;
    private HttpURLConnection conn;
    private BufferedReader reader;

    public String getLayerGroupID(URL url)
            throws InterruptedException, ExecutionException {
        this.url = url;

        ExecutorService exService = Executors.newSingleThreadExecutor();
        FutureTask<String> futureTask = new FutureTask<String>(
                new GetIDTask());

        exService.execute(futureTask);

        return futureTask.get();
    }

    class GetIDTask implements Callable<String> {

        public String call() {
            String layerGroupID = "";

            try {
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(30000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                conn.connect();

                reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }

                String result = sb.toString();

                JSONObject json = new JSONObject(result);

                layerGroupID = json.getString("layergroupid");

                if (layerGroupID == null) {
                    layerGroupID = "";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            return layerGroupID;
        }
    }
}

