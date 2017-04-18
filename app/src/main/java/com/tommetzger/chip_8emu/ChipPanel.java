/**
 * Created by Tom on 4/2/17.
 */
package com.tommetzger.chip_8emu;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.preference.PreferenceManager;

import java.util.Random;


public class ChipPanel extends View
{
    private byte[] display;
    private Chip chip;
    Paint paint;


    SharedPreferences preferences;

    int PRIMARY_COLOR;
    int SECONDARY_COLOR;
    
    int FRAMESKIP;
    int frameCount = 0;




    public ChipPanel(Context context, Chip chip)
    {
        super(context);


//        this.chip = chip;
    }


//    public ChipPanel(Context context)
//    {
//        super(context);
//    }
//
    public ChipPanel(Context context, AttributeSet attrs)
    {
        super(context, attrs);


        paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
    }




    public void injectChip(Chip chip)
    {
        this.chip = chip;
    }




    public void injectPreferenceContext(Context context)
    {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        PRIMARY_COLOR = preferences.getInt("primaryColor", -11746580);
        SECONDARY_COLOR = preferences.getInt("secondaryColor", -16777216);
        
        FRAMESKIP = Integer.parseInt(preferences.getString("frameskip", "0"));
    }




    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);


        if (frameCount == FRAMESKIP)
        {
            display = chip.getDisplay();
    
            for (int i = 0; i < display.length; i++)
            {
                if (display[i] == 0)
                {
                    paint.setColor(SECONDARY_COLOR);
                }
                else
                {
                    paint.setColor(PRIMARY_COLOR);
                }
        
                int x = (i % 64);
                int y = (int) Math.floor(i / 64);
        
                float totalPixelWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, getResources().getDisplayMetrics());
                float totalPixelHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 160, getResources().getDisplayMetrics());
        
                float upscale = totalPixelWidth / 64;
        
                canvas.drawRect(x * upscale, y * upscale, (x + 1) * upscale, (y + 1) * upscale, paint); //everything upscaled;
                
                frameCount = 0;
            }
        }
        else
        {
            frameCount++;
        }
    }
}
