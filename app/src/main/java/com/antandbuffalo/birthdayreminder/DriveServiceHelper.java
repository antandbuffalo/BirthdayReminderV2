package com.antandbuffalo.birthdayreminder;

import android.util.Log;
import android.util.Pair;

import com.antandbuffalo.birthdayreminder.utilities.Util;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Creates a text file in the user's My Drive folder and returns its file ID.
     */
    public Task<String> createFile() {
        return Tasks.call(mExecutor, () -> {
            FileContent fileContent = new FileContent("text/plain", Util.getCachedFile(""));

            File metadata = new File()
                    .setParents(Collections.singletonList("appDataFolder"))
                    .setMimeType("text/plain")
                    .setName("dob.txt");
            File googleFile = mDriveService.files().create(metadata, fileContent).execute();

            //File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("Null result when requesting file creation.");
            }

            return googleFile.getId();
        });
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its name and
     * contents.
     */
    public Task<Pair<String, String>> readFile(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metadata as a File object.
            File metadata = mDriveService.files().get(fileId).execute();
            String name = metadata.getName();

            // Stream the file contents to a String.
            try (InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                String contents = stringBuilder.toString();
                return Pair.create(name, contents);
            }
        });
    }

    public Task<Boolean> downloadFileWithFileId(String fileId, FileOutputStream fileOutputStream) {
        return Tasks.call(mExecutor, () -> {
            try {
                mDriveService.files().get(fileId)
                        .executeMediaAndDownloadTo(fileOutputStream);
                return true;
            }
            catch (Exception e) {
                Log.e("BR", "Not able to download file: " + e.getLocalizedMessage());
                return false;
            }
        });
        //https://stackoverflow.com/questions/17488534/create-a-file-from-a-bytearrayoutputstream
    }

    //https://github.com/mesadhan/google-drive-app

}
