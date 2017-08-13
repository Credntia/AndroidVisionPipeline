package online.devliving.mobilevisionpipeline;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 12/27/16.
 */

public abstract class FrameGraphic extends GraphicOverlay.Graphic implements Util.FrameSizeProvider {
    private Paint borderPaint = null;
    private int frameWidth = 0, frameHeight = 0;

    public FrameGraphic(GraphicOverlay overlay) {
        super(overlay);

        borderPaint = new Paint();
        borderPaint.setColor(Color.parseColor("#41fa97"));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeCap(Paint.Cap.ROUND);
        borderPaint.setStrokeJoin(Paint.Join.ROUND);
        borderPaint.setStrokeWidth(8);
    }

    abstract protected RectF getFrameRect(float canvasWidth, float canvasHeight);

    /**
     * Draw the graphic on the supplied canvas.  Drawing should use the following methods to
     * convert to view coordinates for the graphics that are drawn:
     * <ol>
     * <li>{@link GraphicOverlay.Graphic#scaleX(float)} and {@link GraphicOverlay.Graphic#scaleY(float)} adjust the size of
     * the supplied value from the preview scale to the view scale.</li>
     * <li>{@link GraphicOverlay.Graphic#translateX(float)} and {@link GraphicOverlay.Graphic#translateY(float)} adjust the
     * coordinate from the preview's coordinate system to the view coordinate system.</li>
     * </ol>
     *
     * @param canvas drawing canvas
     */
    @Override
    public void draw(Canvas canvas) {
        float width = canvas.getWidth();
        float height = canvas.getHeight();

        Log.d("FRAME-GRAPHIC", "canvas size w: " + width + ", h: " + height);
        RectF rect = getFrameRect(width, height);

        frameWidth = Float.valueOf(rect.width()).intValue();
        frameHeight = Float.valueOf(rect.height()).intValue();
        Log.d("FRAME-GRAPHIC", "frame width " + frameWidth);
        canvas.drawRoundRect(rect, 8, 8, borderPaint);
    }

    public int getScaledFrameWidth() {
        return Float.valueOf(frameWidth / mOverlay.getWidthScaleFactor()).intValue();
    }
    public int getScaledFrameHeight(){
        return Float.valueOf(frameHeight / mOverlay.getHeightScaleFactor()).intValue();
    }

    @Override
    public int frameHeight() {
        return getScaledFrameHeight();
    }

    @Override
    public int frameWidth() {
        return getScaledFrameWidth();
    }
}