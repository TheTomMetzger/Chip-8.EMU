/**
 * Created by Tom on 4/2/17.
 */

package com.tommetzger.chip_8emu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.File;






public class EmulationActivity extends AppCompatActivity /***AKA ChipFrame***/
{
    private Toolbar navBar;

    private Button buttonA;
    private Button buttonB;
    private Button buttonC;
    private Button buttonD;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button buttonE;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button buttonF;
    private Button button7;
    private Button button8;
    private Button button9;
    private Button buttonSHIFT;
    private Button button0;
    private Button buttonLEFT;
    private Button buttonRIGHT;


    private ChipPanel panel;
    private int[] keyBuffer;
    private int[] keyIdToKey;

    private File ROM;
    private Chip chip8;

    Emulator emulator;

    private Context context;
	


	
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulation);


        context = this;


        Bundle data = getIntent().getExtras();
        ROM = new File((String) data.get("ROM"));


        navBar = (Toolbar) findViewById(R.id.navBar);
        navBar.setNavigationIcon(R.drawable.ic_back);
        setSupportActionBar(navBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        button0 = (Button) findViewById(R.id.button0);
        button0.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('0');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('0');
                        return true;
                }
                return false;
            }
        });

        button1 = (Button) findViewById(R.id.button1);
        button1.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('1');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('1');
                        return true;
                }
                return false;
            }
        });

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('2');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('2');
                        return true;
                }
                return false;
            }
        });

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('3');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('3');
                        return true;
                }
                return false;
            }
        });

        button4 = (Button) findViewById(R.id.button4);
        button4.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('4');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('4');
                        return true;
                }
                return false;
            }
        });

        button5 = (Button) findViewById(R.id.button5);
        button5.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('5');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('5');
                        return true;
                }
                return false;
            }
        });

        button6 = (Button) findViewById(R.id.button6);
        button6.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('6');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('6');
                        return true;
                }
                return false;
            }
        });

        button7 = (Button) findViewById(R.id.button7);
        button7.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('7');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('7');
                        return true;
                }
                return false;
            }
        });

        button8 = (Button) findViewById(R.id.button8);
        button8.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('8');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('8');
                        return true;
                }
                return false;
            }
        });

        button9 = (Button) findViewById(R.id.button9);
        button9.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('9');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('9');
                        return true;
                }
                return false;
            }
        });

        buttonA = (Button) findViewById(R.id.buttonA);
        buttonA.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('A');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('A');
                        return true;
                }
                return false;
            }
        });

        buttonB = (Button) findViewById(R.id.button0);
        buttonB.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('B');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('B');
                        return true;
                }
                return false;
            }
        });

        buttonC = (Button) findViewById(R.id.buttonC);
        buttonC.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('C');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('C');
                        return true;
                }
                return false;
            }
        });

        buttonD = (Button) findViewById(R.id.buttonD);
        buttonD.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('D');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('D');
                        return true;
                }
                return false;
            }
        });

        buttonE = (Button) findViewById(R.id.buttonE);
        buttonE.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('E');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('E');
                        return true;
                }
                return false;
            }
        });

        buttonF = (Button) findViewById(R.id.buttonF);
        buttonF.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        keyPressed('F');
                        return true;
                    case MotionEvent.ACTION_UP:
                        keyReleased('F');
                        return true;
                }
                return false;
            }
        });


        keyIdToKey = new int[256]; //byte size
        keyBuffer = new int[16]; //Chip-8 has 16 keys
        fillKeyIds();

        chip8 = new Chip();
        chip8.init(getApplicationContext());

        /*TODO
            • Assign ROM and chip8 When switching to Activity
         */
//        panel = new ChipPanel(this, chip8);
        panel = (ChipPanel) findViewById(R.id.chipPanel);
        panel.injectChip(chip8);


        emulator = new Emulator();
        emulator.start();
    }




    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        emulator.control = false;
        chip8.stopEmulation();
    }




    private void fillKeyIds()
    {
        for(int i = 0; i < keyIdToKey.length; i++)
        {
            keyIdToKey[i] = -1;
        }

        keyIdToKey['1'] = 1;
        keyIdToKey['2'] = 2;
        keyIdToKey['3'] = 3;
        keyIdToKey['4'] = 4;
        keyIdToKey['5'] = 5;
        keyIdToKey['6'] = 6;
        keyIdToKey['7'] = 7;
        keyIdToKey['8'] = 8;
        keyIdToKey['9'] = 9;
        keyIdToKey['0'] = 0;
        keyIdToKey['A'] = 0xA;
        keyIdToKey['B'] = 0xB;
        keyIdToKey['C'] = 0xC;
        keyIdToKey['D'] = 0xD;
        keyIdToKey['E'] = 0xE;
        keyIdToKey['F'] = 0xF;
    }




    private void keyPressed(char keyCode)
    {
        if (keyIdToKey[keyCode] != -1)
        {
            keyBuffer[keyIdToKey[keyCode]] = 1;
        }
    }




    private void keyReleased(char keyCode)
    {
        if (keyIdToKey[keyCode] != -1)
        {
            keyBuffer[keyIdToKey[keyCode]] = 0;
        }
    }




    public int[] getKeyBuffer()
    {
        return keyBuffer;
    }





    public class Emulator extends Thread
    {
        public boolean control;




        public Emulator()
        {
            chip8 = new Chip();
            chip8.init(getApplicationContext());
            chip8.loadROM(ROM.getAbsolutePath());

            panel = (ChipPanel) findViewById(R.id.chipPanel);
            panel.injectPreferenceContext(getApplicationContext());
            panel.injectChip(chip8);

            control = true;
        }




        public void run()
        {
            while(control)
            {
                chip8.setKeyBuffer(getKeyBuffer());
                chip8.startEmulation();
                if (chip8.needsRedraw())
                {
                    runOnUiThread(new Runnable()
                    {
                          @Override
                          public void run()
                          {
                              panel.invalidate();
                          }
                      });
                    chip8.removeDrawFlag();
                }
                try
                {
                    Thread.sleep(2);
                }
                catch (InterruptedException e)
                {
                    //Do Nothing
                }
            }
        }
    }
}
