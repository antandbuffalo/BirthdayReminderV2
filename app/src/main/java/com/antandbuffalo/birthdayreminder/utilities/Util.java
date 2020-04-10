package com.antandbuffalo.birthdayreminder.utilities;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by i677567 on 5/10/15.
 */
public class Util {

    public static File getCachedFile(String defaultFileName) {
        Context context = DataHolder.getInstance().getAppContext();
        File cacheFile = new File(DataHolder.getInstance().getAppContext().getCacheDir(), "dob.txt");
        try {
            InputStream inputStream = context.getAssets().open("cse.txt");
            try {
                FileOutputStream outputStream = new FileOutputStream(cacheFile);
                try {
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        outputStream.write(buf, 0, len);
                    }
                } finally {
                    outputStream.close();
                }
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            Log.e("BR", e.getLocalizedMessage());
        }
        return cacheFile;

    }

}
