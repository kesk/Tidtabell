package com.bender.tidtabell;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class DirectionNeedle extends View
{
	public static final int COLOR_ACTIVE = Color.parseColor("#FF9ABC0D");
	public static final int COLOR_INACTIVE = Color.argb(255, 255, 0, 0);
	private int mColor = COLOR_INACTIVE;
	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	int mWidth = 0, mHeight = 0;
	float mRotation = 0;
	
	public DirectionNeedle(Context context, AttributeSet attrs, int defStyle)
    {
	    super(context, attrs, defStyle);
	    
        mPath.moveTo(0, -35);
        mPath.lineTo(-10, 30);
        mPath.lineTo(0, 40);
        mPath.lineTo(10, 30);
        mPath.close();
    }
	
	public DirectionNeedle(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}
	
	public DirectionNeedle(Context context)
	{
		this(context, null, 0);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
        // Background color
		canvas.drawColor(Color.TRANSPARENT);

        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mPaint.setStyle(Paint.Style.FILL);
        
        // Center position
        int x = mWidth / 2;
        int y = mHeight / 2;
        canvas.translate(x, y);
        
        // Scale it down
        canvas.scale(0.6f, 0.6f);
        
        canvas.rotate(-mRotation);
        
        canvas.drawPath(mPath, mPaint);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		mWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		mHeight = View.MeasureSpec.getSize(heightMeasureSpec);
		//setMeasuredDimension(mWidth, mHeight);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void setRotation(float rotation)
	{
		mRotation = rotation;
	}
	
	public void setActive(boolean b)
	{
		if (b)
			mColor = COLOR_ACTIVE;
		else
			mColor = COLOR_INACTIVE;
		
		invalidate();
	}
}
