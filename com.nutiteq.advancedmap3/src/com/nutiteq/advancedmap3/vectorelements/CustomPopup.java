package com.nutiteq.advancedmap3.vectorelements;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.nutiteq.core.ScreenPos;
import com.nutiteq.graphics.Bitmap;
import com.nutiteq.styles.PopupStyle;
import com.nutiteq.styles.PopupStyleBuilder;
import com.nutiteq.utils.BitmapUtils;
import com.nutiteq.vectorelements.Billboard;
import com.nutiteq.vectorelements.Popup;

/**
 * Simple text-based popup.
 */
public class CustomPopup extends Popup {
	static final int SCREEN_PADDING = 10;
	static final int POPUP_PADDING = 10;
	static final int FONT_SIZE = 15;
	static final int STROKE_WIDTH = 2;
	static final int TRIANGLE_SIZE = 10;
	static final int TEXT_COLOR = android.graphics.Color.BLACK;
	static final int STROKE_COLOR = android.graphics.Color.BLACK;
	static final int BACKGROUND_COLOR = android.graphics.Color.WHITE;
	static final PopupStyle STYLE;
	static {
		// Create custom style, set attachment anchor point a bit to the right from the center
		PopupStyleBuilder styleBuilder = new PopupStyleBuilder();
		styleBuilder.setAttachAnchorPoint(0.5f, 0);
		STYLE = styleBuilder.buildStyle();
	}
    
	private String text;

	public CustomPopup(Billboard baseBillboard, String text) {
		super(baseBillboard, STYLE);
		this.text = text;
	}
	
	public synchronized String getText() {
		return text;
	}
	
	public void setText(String text) {
		synchronized (this) {
			this.text = text;
		}
    	notifyElementChanged();
    }
    
	@Override
	public Bitmap drawBitmap(ScreenPos anchorScreenPos, float screenWidth, float screenHeight, float dpToPX) {
		// Calculate scaled dimensions
		float pxToDP = 1 / dpToPX;
	    if (STYLE.isScaleWithDPI()) {
	        dpToPX = 1;
	    } else {
	        pxToDP = 1;
	    }
	    
	    screenWidth *= pxToDP;
	    screenHeight *= pxToDP;
	    
	    int fontSize = (int) (FONT_SIZE * dpToPX);	    
	    
	    int triangleWidth = (int) (TRIANGLE_SIZE * dpToPX);
	    int triangleHeight = (int) (TRIANGLE_SIZE * dpToPX);
	    
	    int strokeWidth = (int) (STROKE_WIDTH * dpToPX);
	    int screenPadding = (int) (SCREEN_PADDING * dpToPX);
	    
	    // Get fonts
	    Typeface font = Typeface.create("HelveticaNeue-Light", Typeface.NORMAL);
	    
	    // Calculate the maximum popup size, adjust with dpi
	    int maxPopupWidth = (int) Math.min(screenWidth, screenHeight);
	    
	    float halfStrokeWidth = strokeWidth * 0.5f;
	    int maxTextWidth = maxPopupWidth - screenPadding * 2 - strokeWidth;
	    	    
	    // Create paint object
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    
	    // Measure text size
	    TextPaint textPaint = new TextPaint();
	    textPaint.setColor(TEXT_COLOR);
	    textPaint.setTextSize(fontSize);
	    textPaint.setTypeface(font);
	    android.graphics.Point textSize = new  android.graphics.Point(0, 0);
	    StaticLayout textLayout = null;
	    if (text != "") {
	    	textLayout = new StaticLayout(text, textPaint, maxTextWidth, Alignment.ALIGN_NORMAL, 1, 0, false);
	        textSize.set((int) Math.min(textPaint.measureText(text), textLayout.getWidth()), textLayout.getHeight());
	    }
	    	    
	    // Calculate bitmap size and create graphics context
	    int popupWidth = textSize.x;
	    popupWidth += 2 * POPUP_PADDING + strokeWidth + triangleWidth;
	    int popupHeight = textSize.y;
	    popupHeight += 2 * POPUP_PADDING + strokeWidth;
	    
	    // Create bitmap and canvas
	    android.graphics.Bitmap bitmap = android.graphics.Bitmap.createBitmap(popupWidth, popupHeight, android.graphics.Bitmap.Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    
	    // Prepare triangle path
		Path trianglePath = new Path();
	    trianglePath.moveTo(triangleWidth, 0);
	    trianglePath.lineTo(halfStrokeWidth, triangleHeight * 0.5f);
	    trianglePath.lineTo(triangleWidth, triangleHeight);
	    trianglePath.close();
	    
	    // Calculate anchor point and triangle position
	    int triangleOffsetX = 0;
	    int triangleOffsetY = (popupHeight - triangleHeight) / 2;
		setAnchorPoint(-1, 0);

	    // Stroke background
	    paint.setStyle(Style.STROKE);
	    paint.setStrokeWidth(strokeWidth);
		paint.setColor(STROKE_COLOR);
		RectF backgroundRect = new RectF(
				triangleWidth, halfStrokeWidth,
				popupWidth - strokeWidth, popupHeight - strokeWidth);
		canvas.drawRect(backgroundRect, paint);
		
		// Stroke triangle
		canvas.save();
	    canvas.translate(triangleOffsetX, triangleOffsetY);
	    canvas.drawPath(trianglePath, paint);
	    canvas.restore();
	
		// Fill background
		paint.setStyle(Style.FILL);
		paint.setColor(BACKGROUND_COLOR);
		canvas.drawRect(backgroundRect, paint);
		
		// Fill triangle
		canvas.save();
	    canvas.translate(triangleOffsetX, triangleOffsetY);
	    canvas.drawPath(trianglePath, paint);
	    canvas.restore();
	    
	    if (textLayout != null) {
	    	// Draw text
	    	canvas.save();
	    	canvas.translate(halfStrokeWidth + triangleWidth + POPUP_PADDING,
	                     	 halfStrokeWidth + POPUP_PADDING);
	    	textLayout.draw(canvas);
	    	canvas.restore();
	    }
	    
		return BitmapUtils.createBitmapFromAndroidBitmap(bitmap);
	}
}
