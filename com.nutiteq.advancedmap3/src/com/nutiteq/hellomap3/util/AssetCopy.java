package com.nutiteq.hellomap3.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.nutiteq.advancedmap3.Const;

import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;


public class AssetCopy {
    public static void copyAssetToSDCard(AssetManager assetManager, String fileName) throws IOException {
            InputStream in = assetManager.open(fileName);
            File outFile = new File(Environment.getExternalStorageDirectory(), fileName);
            // TODO jaak - check if storage is available and has enough space 
            if(outFile.exists()){
                Log.d(Const.LOG_TAG, "file already exits: "+outFile.getAbsolutePath());
                return;
            }
                
            OutputStream out = new FileOutputStream(outFile);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}
