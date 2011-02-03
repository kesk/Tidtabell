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
	private Paint mPaint = new Paint();
	private Path mPath = new Path();
	int mWidth = 0, mHeight = 0;
	float[] mOrientation;
	
	public DirectionNeedle(Context context, AttributeSet attrs, int defStyle)
    {
	    super(context, attrs, defStyle);
	    
        mPath.moveTo(0, -50);
        mPath.lineTo(-20, 60);
        mPath.lineTo(0, 50);
        mPath.lineTo(20, 60);
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
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        
        // Center position
        int x = mWidth / 2;
        int y = mHeight / 2;
        canvas.translate(x, y);
        
        // Scale it down
        canvas.scale(0.2f, 0.2f);
        
        if (mOrientation != null)
        	canvas.rotate(-mOrientation[0]);
        
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
	
	public void setOrientation(float[] orientation)
	{
		mOrientation = orientation;
	}
}
