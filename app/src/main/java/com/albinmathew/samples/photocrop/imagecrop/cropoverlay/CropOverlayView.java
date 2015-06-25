package com.albinmathew.samples.photocrop.imagecrop.cropoverlay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.albinmathew.samples.photocrop.R;
import com.albinmathew.samples.photocrop.imagecrop.cropoverlay.edge.Edge;
import com.albinmathew.samples.photocrop.imagecrop.cropoverlay.utils.PaintUtil;
import com.albinmathew.samples.photocrop.imagecrop.photoview.PhotoViewAttacher;


/**
 * @author GT
 *         Modified/stripped down Code from cropper library : https://github.com/edmodo/cropper
 */
public class CropOverlayView extends View implements PhotoViewAttacher.IGetImageBounds {

    private static final float CORNER_RADIUS = 6;
    //Defaults
    private boolean DEFAULT_GUIDELINES = true;
    private int DEFAULT_MARGINTOP = 100;
    private int DEFAULT_MARGINSIDE = 50;
    private int DEFAULT_MIN_WIDTH = 500;
    private int DEFAULT_MAX_WIDTH = 700;
    // we are croping square image so width and height will always be equal
    private int DEFAULT_CROPWIDTH = 600;
    // The Paint used to darken the surrounding areas outside the crop area.
    private Paint mBackgroundPaint;

    // The Paint used to draw the white rectangle around the crop area.
    private Paint mBorderPaint;

    // The Paint used to draw the guidelines within the crop area.
    private Paint mGuidelinePaint;

    // The bounding box around the Bitmap that we are cropping.
    private Rect mBitmapRect;

    private int cropHeight = DEFAULT_CROPWIDTH;
    private int cropWidth = DEFAULT_CROPWIDTH;


    private boolean mGuidelines;
    private int mMarginTop;
    private int mMarginSide;
    private int mMinWidth;
    private int mMaxWidth;
    private Context mContext;

    public CropOverlayView(Context context) {
        super(context);
        init(context);
        this.mContext = context;
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropOverlayView, 0, 0);
        try {
            mGuidelines = ta.getBoolean(R.styleable.CropOverlayView_guideLines, DEFAULT_GUIDELINES);
            mMarginTop = ta.getDimensionPixelSize(R.styleable.CropOverlayView_marginTop, DEFAULT_MARGINTOP);
            mMarginSide = ta.getDimensionPixelSize(R.styleable.CropOverlayView_marginSide, DEFAULT_MARGINSIDE);
            mMinWidth = ta.getDimensionPixelSize(R.styleable.CropOverlayView_minWidth, DEFAULT_MIN_WIDTH);
            mMaxWidth = ta.getDimensionPixelSize(R.styleable.CropOverlayView_maxWidth, DEFAULT_MAX_WIDTH);
        } finally {
            ta.recycle();
        }

        init(context);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        final float radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, CORNER_RADIUS, mContext.getResources().getDisplayMetrics());

        RectF rectF = new RectF(Edge.LEFT.getCoordinate(),
                Edge.TOP.getCoordinate(),
                Edge.RIGHT.getCoordinate(),
                Edge.BOTTOM.getCoordinate());
        float cx = (Edge.LEFT.getCoordinate() + Edge.RIGHT.getCoordinate()) / 2;
        float cy = (Edge.TOP.getCoordinate() + Edge.BOTTOM.getCoordinate()) / 2;
        float radius2 = (Edge.RIGHT.getCoordinate() - Edge.LEFT.getCoordinate()) / 2;

        Path clipPath = new Path();
        clipPath.addCircle(cx, cy, radius2, Path.Direction.CW);
        canvas.clipPath(clipPath, Region.Op.DIFFERENCE);
        canvas.drawARGB(204, 41, 48, 63);
        canvas.restore();
        canvas.drawCircle(cx, cy, radius2, mBorderPaint);
        //drawRuleOfThirdsGuidelines(canvas);
    }

    public Rect getImageBounds() {
        return new Rect((int) Edge.LEFT.getCoordinate(), (int) Edge.TOP.getCoordinate(), (int) Edge.RIGHT.getCoordinate(), (int) Edge.BOTTOM.getCoordinate());
    }


    // Private Methods /////////////////////////////////////////////////////////
    private void init(Context context) {
        int w = context.getResources().getDisplayMetrics().widthPixels;
        cropWidth = w - 2 * mMarginSide;
        cropHeight = cropWidth;
        int edgeT = mMarginTop;
        int edgeB = mMarginTop + cropHeight;
        int edgeL = mMarginSide;
        int edgeR = mMarginSide + cropWidth;
        mBackgroundPaint = PaintUtil.newBackgroundPaint(context);
        mBorderPaint = PaintUtil.newBorderPaint(context);
        mGuidelinePaint = PaintUtil.newGuidelinePaint();
        Edge.TOP.setCoordinate(edgeT);
        Edge.BOTTOM.setCoordinate(edgeB);
        Edge.LEFT.setCoordinate(edgeL);
        Edge.RIGHT.setCoordinate(edgeR);
        new Rect(edgeL, edgeT, edgeR, edgeB);
        mBitmapRect = new Rect(0, 0, w, w);
    }


    private void drawRuleOfThirdsGuidelines(Canvas canvas) {

        final float left = Edge.LEFT.getCoordinate();
        final float top = Edge.TOP.getCoordinate();
        final float right = Edge.RIGHT.getCoordinate();
        final float bottom = Edge.BOTTOM.getCoordinate();

        // Draw vertical guidelines.
        final float oneThirdCropWidth = Edge.getWidth() / 3;

        final float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, mGuidelinePaint);
        final float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, mGuidelinePaint);

        // Draw horizontal guidelines.
        final float oneThirdCropHeight = Edge.getHeight() / 3;

        final float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, mGuidelinePaint);
        final float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, mGuidelinePaint);
    }

}
