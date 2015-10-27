package com.albinmathew.samples.photocrop;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.albinmathew.photocrop.cropoverlay.CropOverlayView;
import com.albinmathew.photocrop.cropoverlay.edge.Edge;
import com.albinmathew.photocrop.cropoverlay.utils.ImageViewUtil;
import com.albinmathew.photocrop.photoview.PhotoView;
import com.albinmathew.photocrop.photoview.PhotoViewAttacher;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * @author albin
 * @date 23/6/15
 */
public class ImageCropActivity extends AppCompatActivity {

    public static final String TAG = "ImageCropActivity";
    public static final String TEMP_PHOTO_FILE_NAME = "temp_photo.jpg";
    public static final int REQUEST_CODE_PICK_GALLERY = 0x1;
    public static final int REQUEST_CODE_TAKE_PICTURE = 0x2;
    public static final int REQUEST_CODE_CROPPED_PICTURE = 0x3;
    public static final String ERROR_MSG = "error_msg";
    public static final String ERROR = "error";
    private static final int PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 218;
    private final int IMAGE_MAX_SIZE = 1024;
    private final Bitmap.CompressFormat mOutputFormat = Bitmap.CompressFormat.JPEG;
    private PhotoView mImageView;
    private CropOverlayView mCropOverlayView;
    private Button btnCancel;
    private Button btnSend;
    private ContentResolver mContentResolver;
    private float minScale = 1f;
    //Temp file to save cropped image
    private String mImagePath;
    private Uri mSaveUri = null;
    private Uri mImageUri = null;
    //File for capturing camera images
    private File mFileTemp;
    private View.OnClickListener btnCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            userCancelled();
        }
    };
    private View.OnClickListener btnSendListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveUploadCroppedImage();
        }
    };

    private static void copyStream(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static int getCameraPhotoOrientation(@NonNull Context context, Uri imageUri) {
        int rotate = 0;
        try {
            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(
                    imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        mContentResolver = getContentResolver();
        mImageView = (PhotoView) findViewById(R.id.iv_photo);
        mCropOverlayView = (CropOverlayView) findViewById(R.id.crop_overlay);
        btnSend = (Button) findViewById(R.id.sendBtn);
        btnSend.setOnClickListener(btnSendListener);
        btnCancel = (Button) findViewById(R.id.cancelBtn);
        btnCancel.setOnClickListener(btnCancelListener);

        mImageView.addListener(new PhotoViewAttacher.IGetImageBounds() {
            @Override
            public Rect getImageBounds() {
                return new Rect((int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(), (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate());
            }
        });
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            }else{
                proceedToCrop(savedInstanceState);
            }
        } else {
            proceedToCrop(savedInstanceState);
        }
    }

    private void proceedToCrop(Bundle savedInstanceState) {
        createTempFile();
        if (savedInstanceState == null || !savedInstanceState.getBoolean("restoreState")) {
            String action = getIntent().getStringExtra("ACTION");
            if (null != action) {
                switch (action) {
                    case Constants.IntentExtras.ACTION_CAMERA:
                        getIntent().removeExtra("ACTION");
                        takePic();
                        return;
                    case Constants.IntentExtras.ACTION_GALLERY:
                        getIntent().removeExtra("ACTION");
                        pickImage();
                        return;
                }
            }
        }
        mImagePath = mFileTemp.getPath();
        mSaveUri = Utils.getImageUri(mImagePath);
        mImageUri = Utils.getImageUri(mImagePath);
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void init() {
        Bitmap b = getBitmap(mImageUri);
        Drawable bitmap = new BitmapDrawable(getResources(), b);
        int h = bitmap.getIntrinsicHeight();
        int w = bitmap.getIntrinsicWidth();
        final float cropWindowWidth = Edge.getWidth();
        final float cropWindowHeight = Edge.getHeight();
        if (h <= w) {
            minScale = (cropWindowHeight + 1f) / h;
        } else if (w < h) {
            minScale = (cropWindowWidth + 1f) / w;
        }

        mImageView.setMaximumScale(minScale * 3);
        mImageView.setMediumScale(minScale * 2);
        mImageView.setMinimumScale(minScale);
        mImageView.setImageDrawable(bitmap);
        mImageView.setScale(minScale);

    }

    private void saveUploadCroppedImage() {
        boolean saved = saveOutput();
        if (saved) {
            //USUALLY Upload image to server here
            Intent intent = new Intent();
            intent.putExtra(Constants.IntentExtras.IMAGE_PATH, mImagePath);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "Unable to save Image into your device.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    proceedToCrop(null);
                } else {
                    Toast.makeText(this, "No permission granted to access the external storage", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void createTempFile() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mFileTemp = new File(Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE_NAME);
        } else {
            mFileTemp = new File(getFilesDir(), TEMP_PHOTO_FILE_NAME);
        }
    }

    private void takePic() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            Uri mImageCaptureUri = null;
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mImageCaptureUri = Uri.fromFile(mFileTemp);
            } else {
                /*
                 * The solution is taken from here: http://stackoverflow.com/questions/10042695/how-to-get-camera-result-as-a-uri-in-data-folder
	        	 */
                mImageCaptureUri = InternalStorageContentProvider.CONTENT_URI;
            }
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            takePictureIntent.putExtra("return-data", true);
            startActivityForResult(takePictureIntent, REQUEST_CODE_TAKE_PICTURE);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Can't take picture", e);
            Toast.makeText(this, "Can't take picture", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("restoreState", true);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
        try {
            startActivityForResult(intent, REQUEST_CODE_PICK_GALLERY);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No image source available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        createTempFile();
        if (requestCode == REQUEST_CODE_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mImagePath = mFileTemp.getPath();
                mSaveUri = Utils.getImageUri(mImagePath);
                mImageUri = Utils.getImageUri(mImagePath);
                init();
            } else if (resultCode == RESULT_CANCELED) {
                userCancelled();
            } else {
                errored();
            }

        } else if (requestCode == REQUEST_CODE_PICK_GALLERY) {
            if (resultCode == RESULT_CANCELED) {
                userCancelled();
                return;
            } else if (resultCode == RESULT_OK) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(result.getData()); // Got the bitmap .. Copy it to the temp file for cropping
                    FileOutputStream fileOutputStream = new FileOutputStream(mFileTemp);
                    copyStream(inputStream, fileOutputStream);
                    fileOutputStream.close();
                    inputStream.close();
                    mImagePath = mFileTemp.getPath();
                    mSaveUri = Utils.getImageUri(mImagePath);
                    mImageUri = Utils.getImageUri(mImagePath);
                    init();
                } catch (Exception e) {
                    errored();
                }
            } else {
                errored();
            }

        }
    }

    private Bitmap getBitmap(Uri uri) {
        InputStream in = null;
        Bitmap returnedBitmap = null;
        try {
            in = mContentResolver.openInputStream(uri);
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();
            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = mContentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, o2);
            in.close();
            returnedBitmap = fixOrientationBugOfProcessedBitmap(bitmap);
            return returnedBitmap;
        } catch (FileNotFoundException e) {
            Log.d(TAG, "FileNotFoundException");
        } catch (IOException e) {
            Log.d(TAG, "IOException");
        }
        return null;
    }

    private Bitmap fixOrientationBugOfProcessedBitmap(Bitmap bitmap) {
        try {
            if (getCameraPhotoOrientation(this, Uri.parse(mFileTemp.getPath())) == 0) {
                return bitmap;
            } else {
                Matrix matrix = new Matrix();
                matrix.postRotate(getCameraPhotoOrientation(this, Uri.parse(mFileTemp.getPath())));
                // recreate the new Bitmap and set it back
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Bitmap getCurrentDisplayedImage() {
        Bitmap result = Bitmap.createBitmap(mImageView.getWidth(), mImageView.getHeight(), Bitmap.Config.RGB_565);
        Canvas c = new Canvas(result);
        mImageView.draw(c);
        return result;
    }

    public Bitmap getCroppedImage() {

        Bitmap mCurrentDisplayedBitmap = getCurrentDisplayedImage();
        Rect displayedImageRect = ImageViewUtil.getBitmapRectCenterInside(mCurrentDisplayedBitmap, mImageView);

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for width.
        float actualImageWidth = mCurrentDisplayedBitmap.getWidth();
        float displayedImageWidth = displayedImageRect.width();
        float scaleFactorWidth = actualImageWidth / displayedImageWidth;

        // Get the scale factor between the actual Bitmap dimensions and the
        // displayed dimensions for height.
        float actualImageHeight = mCurrentDisplayedBitmap.getHeight();
        float displayedImageHeight = displayedImageRect.height();
        float scaleFactorHeight = actualImageHeight / displayedImageHeight;

        // Get crop window position relative to the displayed image.
        float cropWindowX = Edge.LEFT.getCoordinate() - displayedImageRect.left;
        float cropWindowY = Edge.TOP.getCoordinate() - displayedImageRect.top;
        float cropWindowWidth = Edge.getWidth();
        float cropWindowHeight = Edge.getHeight();

        // Scale the crop window position to the actual size of the Bitmap.
        float actualCropX = cropWindowX * scaleFactorWidth;
        float actualCropY = cropWindowY * scaleFactorHeight;
        float actualCropWidth = cropWindowWidth * scaleFactorWidth;
        float actualCropHeight = cropWindowHeight * scaleFactorHeight;

        // Crop the subset from the original Bitmap.
        return Bitmap.createBitmap(mCurrentDisplayedBitmap, (int) actualCropX, (int) actualCropY, (int) actualCropWidth, (int) actualCropHeight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean saveOutput() {
        Bitmap croppedImage = getCroppedImage();
        if (mSaveUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = mContentResolver.openOutputStream(mSaveUri);
                if (outputStream != null) {
                    croppedImage.compress(mOutputFormat, 90, outputStream);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return false;
            } finally {
                closeSilently(outputStream);
            }
        } else {
            Log.e(TAG, "not defined image url");
            return false;
        }
        croppedImage.recycle();
        return true;
    }


    public void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // do nothing
        }
    }

    public void userCancelled() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    public void errored() {
        Intent intent = new Intent();
        intent.putExtra(ERROR, true);
        if ("Error while opening the image file. Please try again." != null) {
            intent.putExtra(ERROR_MSG, "Error while opening the image file. Please try again.");
        }
        finish();
    }


}
