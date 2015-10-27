package com.albinmathew.samples.photocrop;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;

/*
 * The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder
 */

public class InternalStorageContentProvider extends ContentProvider {
    public static final Uri CONTENT_URI = Uri.parse("content://com.albinmathew.samples.photocrop/");
    private static final HashMap<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
    }

    @Override
    public boolean onCreate() {
        try {
            File mFile = new File(getContext().getFilesDir(), ImageCropActivity.TEMP_PHOTO_FILE_NAME);
            if (!mFile.exists()) {
                mFile.createNewFile();
                getContext().getContentResolver().notifyChange(CONTENT_URI, null);
            }
            return (true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        String path = uri.toString();
        for (String extension : MIME_TYPES.keySet()) {
            if (path.endsWith(extension)) {
                return (MIME_TYPES.get(extension));
            }
        }
        return (null);
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        File f = new File(getContext().getFilesDir(), ImageViewActivity.TEMP_PHOTO_FILE_NAME);
        if (f.exists()) {
            return (ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_WRITE));
        }
        throw new FileNotFoundException(uri.getPath());
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}