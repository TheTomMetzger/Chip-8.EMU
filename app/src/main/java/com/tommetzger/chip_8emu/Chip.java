/**
 * Created by Tom on 4/2/17.
 */

package com.tommetzger.chip_8emu;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Random;





public class Chip
{
    private char[] memory; // Chip-8 has 4kB of 8-bit memory, used char array to represent memory since chars can be unsigned
    private char[] V; //Chip-8 has 16 resistors; Each one is referred to as "V"
    private char I; //Address pointer; 16 bit, but only 12 bits used
    private char pc; //Program Counter; The initial point where each program will start

    private char stack[]; //Address pointer array
    private int stackPointer;

    private int delay_timer; //Undocumented how many bits these use, so a basic integer should be fine
    private int sound_timer; //Tick down at 60hz = 8 seconds, 60 updates

    private byte[] keys;// Input; the input is a hexadecimal keyboard = 16 keys

    private byte[] display; //No set way of doing this; We can choose how we want to do it; 0 = black, 1 = white

    private boolean needRedraw;


    /***Mods For Android***/
    public Context context;
    private MediaPlayer mediaPlayer;

    SharedPreferences preferences;
    boolean CAN_PLAY_SOUND;
    boolean PLAY_REMOTE_SOUND;
    String remoteSound;
    /**********************/




    public void init(Context context)
    {
        memory= new char [4096]; //kB = 1024 bytes, so 4kB = 1024 X 4 = 4096; Each spot in the array = 1 byte
        V = new char [16]; //each spot in the array = 1 resistor
        I= 0x0; //
        pc = 0x200; //Each program loaded in at the position of hexadecimal 200 (slot 512)

        stack = new char [16]; //Can use 16 levels of nesting; can call up to 16 subroutines and will crash upon 17 (or override other stuff)
        stackPointer = 0;

        delay_timer = 0;
        sound_timer = 0;

        keys = new byte[16]; //One space for each key

        display = new byte[64 * 32]; //Display resolution = 64x32; one slot for each pixel

        needRedraw = false;

        loadFontSet();


        /***Mods For Android***/
        this.context = context;

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        CAN_PLAY_SOUND = preferences.getBoolean("sound", true);
        PLAY_REMOTE_SOUND = preferences.getBoolean("useCustomBeep", false);
        remoteSound = preferences.getString("customBeep", "");
		
        if (PLAY_REMOTE_SOUND && remoteSound != "")
        {
			Log.d("CHIP", "Using Remote Sound");
            mediaPlayer = new MediaPlayer();
            try
            {
                mediaPlayer.setDataSource(remoteSound);
				mediaPlayer.prepare();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                Log.d("CHIP", "ERROR: Remote Beep Not Found. Defaulting To Default Beep.");
                mediaPlayer = MediaPlayer.create(context, R.raw.beep);
            }
        }
        else
        {
            mediaPlayer = MediaPlayer.create(context, R.raw.beep);
        }
        /**********************/
	}




    public void loadROM(String romFile)
    {
        DataInputStream inputStream = null;

        try
        {
            inputStream = new DataInputStream((new FileInputStream(new File(romFile))));

            int offset = 0;

            while (inputStream.available() > 0)
            {
                memory[0x200 + offset] = (char)(inputStream.readByte() & 0xFF);
                offset++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            /* TODO
                • Alert User ROM not loaded & quit to ROM List
             */
        }
        finally
        {
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException e)
                {
                    //Do Nothing.
                }
            }
        }
    }




    public void loadFontSet() //Loads fontset into memory
    {
        for (int i = 0; i < ChipData.fontset.length; i++)
        {
            memory[0x50 + i] = (char)(ChipData.fontset[i] & 0xFF);
        }
    }




    public void setKeyBuffer(int[] keyBuffer)
    {
        for(int i = 0; i < keys.length; i++)
        {
            keys[i] = (byte)keyBuffer[i];
        }
    }




    public void startEmulation()
    {
        //fetch Opcode
        char opcode = (char) ((memory[pc] << 8) | memory[pc +1]); //Contains 16 bit value, since memory is only 8 bit, merge 2 memory slots into one opcode; Checks if either value has bit set to true, if yes add it to the new value; Shifts the first one left 8 positions (entire byte) to represent new value
        System.out.print(Integer.toHexString(opcode).toUpperCase() + ": ");

        //decode Opcode
        switch(opcode & 0xF000)
        {
            case 0x0000: //Multi-case
            {
                switch(opcode & 0x00FF)
                {
                    case 0x00E0: //00E0: Clear Screen
                    {
                        for(int i = 0; i < display.length; i++)
                        {
                            display[i] = 0;
                        }
                        pc += 2;
                        needRedraw = true;
                        break;
                    }

                    case 0x00EE: //00EE: Returns From  Subroutine
                    {
                        stackPointer--;
                        pc = (char)(stack[stackPointer] + 2);
                        Log.d("CHIP", "Returning to " +Integer.toHexString(pc).toUpperCase());
                        break;
                    }

                    default: //0NNN: Calls RCA 1802 Program At Address NNN
                    {
                        System.err.println("Unsupported Opcode!");
                        System.exit(0);
                        break;
                    }
                }
                break;
            }

            case 0x1000: //1NNN: Jumps To Address NNN
            {
                int nnn = opcode & 0x0FFF;
                pc = (char) nnn;
                Log.d("CHIP", "Jumping to "+ Integer.toHexString(pc).toUpperCase());
                break;
            }

            case 0x2000: //2NNN: Calls Subroutine at NNN
            {
                stack[stackPointer] = pc;
                stackPointer++;
                pc = (char)(opcode & 0x0FFF);
                Log.d("CHIP", "Calling " + Integer.toHexString(pc).toUpperCase() + " from " + Integer.toHexString(stack[stackPointer - 1]).toUpperCase());
                break;
            }

            case 0x3000: //3XNN: Skips The Next Instruction If VX = NN
            {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                if(V[x] == nn)
                {
                    pc += 4;
                    Log.d("CHIP", "Skipping Next Instruction (V["+ x +"] == "+ nn +")");
                }
                else
                {
                    pc += 2;
                    Log.d("CHIP", "Not Skipping Next Instruction (V["+ x +"] != "+ nn +")");
                }
                break;
            }

            case 0x4000: //Skip the next instruction if VX != NN
            {
                int x = (opcode & 0x0F00) >> 8;
                int nn = opcode & 0x00FF;
                if(V[x] != nn)
                {
                    pc += 4;
                    Log.d("CHIP", "Skipping next instruction V["+ x +"] != "+ nn);
                }
                else
                {
                    pc += 2;
                    Log.d("CHIP", "Not skipping next instruction V["+ x +"] = "+ (int)V[x] +" == "+ nn);
                }
                break;
            }

            case 0x5000: //Skips the next instruction if VX = VY
            {
                int x = (opcode & 0x0F00) >> 8;
                int y = (opcode & 0x00F0) >> 4;
                if(V[x] == V[y])
                {
                    pc += 4;
                    Log.d("CHIP", "Skipping next instruction V["+ x +"] == "+ "V["+ y +"]");
                }
                else
                {
                    pc += 2;
                    Log.d("CHIP", "Not skipping next instruction V["+ x +"] != "+ "V["+ y +"]");
                }
                break;
            }

            case 0x6000: //6xNN: Set VX To NN
            {
                int x = (opcode & 0x0F00) >> 8;
                V[x] = (char)(opcode & 0x00FF);
                pc += 2;
                Log.d("CHIP", "Setting V["+ x +"] = " + (int)V[x]);
                break;
            }

            case 0x7000: //7XNN: Adds NN to VX
            {
                int x = (opcode & 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                V[x] = (char)((V[x] + nn) & 0xFF);
                pc += 2;
                Log.d("CHIP", "Adding " + nn + " To V["+ x +"] = " + V[x]);
                break;
            }

            case 0x8000: //Contains more data in last nibble
            {
                switch (opcode & 0x000F)
                {
                    case 0x0000: //Set VX to the value of VY
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        Log.d("CHIP", "Setting V["+ x +"] to "+ (int)V[y]);
                        V[x] = V[y];
                        pc += 2;
                        break;
                    }

                    case 0x0001: //sets VX to VX or VY
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        Log.d("CHIP", "Setting V["+ x +"] = V["+ x +"] | V["+ y +"]");
                        V[x] = (char) ((V[x] | V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }

                    case 0x0002: //Sets VX to VX AND VY
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        V[x] = (char) (V[x] & V[y]);
                        Log.d("CHIP", "Set V[" + x +"] to "+ (int)V[x] +" & "+ (int)V[y] +" = "+ (int)(V[x] & V[y]));
                        pc += 2;
                        break;
                    }

                    case 0x0003: //Sets VX to VX xor VY
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        Log.d("CHIP", "Setting V["+ x +"] = V["+ x +"] ^ V["+ y +"]");
                        V[x] = (char) ((V[x] ^ V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }

                    case 0x0004: //Adds VY to VX, VF is set to 1 when carry applies else to 0
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        Log.d("CHIP", "Adding V["+ x +"] to V["+ y +"] = "+ ((V[x] + V[y]) & 0xFF) +", Apply Carry If Needed");
                        if(V[y] > 255 - V[x])
                        {
                            V[0xF] = 1;
                        }
                        else
                        {
                            V[0xF] = 0;
                        }
                        V[x] = (char)((V[x] + V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }

                    case 0x0005: //VY is subtracted from VX; VF is set to 0 when there is a borrow, else 1
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        Log.d("CHIP", "V["+ x +"] = "+ (int)V[x] +" V["+ y +"] = "+ (int)V[y] +",");
                        if(V[x] > V[y])
                        {
                            V[0xF] = 1;
                            Log.d("CHIP", "No Borrow");
                        }
                        else
                        {
                            V[0xF] = 0;
                            Log.d("CHIP", "Borrow");
                        }
                        V[x] = (char) ((V[x] - V[y]) & 0xFF);
                        pc += 2;
                        break;
                    }

                    case 0x0006: //Shift VX right by one, VF is set to the least significant bit of VX
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        V[0xF] = (char)(V[x] & 0x1);
                        V[x] = (char)(V[x] >> 1);
                        pc += 2;
                        Log.d("CHIP", "Shift V["+ x +"] >> 1 and VF to LSB of VX");
                        break;
                    }

                    case 0x0007: //Sets VX to VY minus VX; VF is set to 0 when there's a borrow, and 1 when there isn't
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int y = (opcode & 0x00F0) >> 4;
                        if(V[x] > V[y])
                        {
                            V[0xF] = 0;
                        }
                        else
                        {
                            V[0xF] = 1;
                        }
                        V[x] = (char)((V[y] - V[x]) & 0xFF);
                        Log.d("CHIP", "V["+ x +"] = V["+ y +"] - V["+ x +"], applies borrow, if needed");
                        pc += 2;
                        break;
                    }

                    case 0x000E: //Shift VX left by one, VF is set to the most significant bit of VX
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        V[0xF] = (char)(V[x] & 0x80);
                        V[x] = (char)(V[x] << 1);
                        pc += 2;
                        Log.d("CHIP", "Shift V["+ x +"] << 1 and VF to MSB of VX");
                        break;
                    }

                    default:
                    {
                        System.err.println("Unsupported Opcode!");
                        System.exit(0);
                        break;
                    }
                }
                break;
            }

            case 0x9000:
            {
                int x = (opcode & 0x0F00) >> 8;
                int y = (opcode & 0x00F0) >> 4;
                if(V[x] != V[y])
                {
                    pc += 4;
                    Log.d("CHIP", "Skipping next instruction V["+ x +"] != "+ "V["+ y +"]");
                }
                else
                {
                    pc += 2;
                    Log.d("CHIP", "Not skipping next instruction V["+ x +"] == "+ "V["+ y +"]");
                }
                break;
            }

            case 0xA000: //ANNN: Set I to NNN
            {
                I = (char)(opcode & 0x0FFF);
                pc += 2;
                Log.d("CHIP", "Set I To " + Integer.toHexString(I).toUpperCase());
                break;
            }

            case 0xB000: //Jumps to the address NNN plus V0
            {
                int nnn = opcode & 0x0FFF;
                int extra = V[0] & 0xFF;
                pc = (char)(nnn + extra);
                break;
            }

            case 0xC000: //Set VX to a random number and NN
            {
                int x = (opcode = 0x0F00) >> 8;
                int nn = (opcode & 0x00FF);
                int randomNumber = new Random().nextInt(255) & nn;
                Log.d("CHIP", "V["+ x +"] has been set to (randomized) " + randomNumber);
                V[x] = (char)randomNumber;
                pc += 2;
                break;
            }

            case 0xD000: //DXYN: Draw A Sprite (X, Y) size (8, N). Sprite Is Located At I
            {
                //Drawing by XOR-ing to the screen
                //Check collision and set V[0xF] (resistor 16)
                //Read image from I
                int x = V[(opcode & 0x0F00) >> 8];
                int y = V[(opcode & 0x00F0) >> 4];
                int height = opcode & 0x000F;

                V[0xF] = 0;

                for (int _y = 0; _y < height; _y++)
                {
                    int line = memory[I + _y];
                    for (int _x = 0; _x < 8; _x++)
                    {
                        int pixel = line & (0x80 >> _x);
                        if(pixel != 0)
                        {
                            int totalX = x + _x;
                            int totalY = y + _y;
                            totalX = totalX % 64;
                            totalY = totalY % 32;
                            int index = totalY * 64 + totalX;
                            if(display[index] == 1)
                            {
                                V[0xF] = 1;
                            }
                            display[index] ^= 1;
                        }
                    }
                }
                pc += 2;
                needRedraw = true;
                Log.d("CHIP", "Drawing at V["+ ((opcode & 0x0F00) >> 8) +"] = "+ x +", V["+ ((opcode & 0x00F0) >> 4) +"] = "+ y);
                break;
            }

            case 0xE000:
            {
                switch (opcode & 0x00FF)
                {
                    case 0x009E: //Skip the next instruction if the VX is pressed
                    {
                        int x = (opcode &0x0F00) >> 8;
                        int key = V[x];
                        if(keys[key] == 1)
                        {
                            pc += 4;
                        }
                        else
                        {
                            pc += 2;
                        }
                        Log.d("CHIP", "Skipping Next Instruction if V["+ x +"] = "+ (int)V[x] +" is Pressed");
                        break;
                    }

                    case 0x00A1: //Skip the next instruction if the VX is NOT pressed
                    {
                        int x = (opcode &0x0F00) >> 8;
                        int key = V[x];
                        if(keys[key] == 0)
                        {
                            pc += 4;
                        }
                        else
                        {
                            pc += 2;
                        }
                        Log.d("CHIP", "Skipping Next Instruction if V["+ x +"] = "+ (int)V[x] +" is NOT Pressed");
                        break;
                    }

                    default:
                    {
                        System.err.println("Unsupported Opcode!");
                        System.exit(0);
                    }
                }
                break;
            }

            case 0xF000:
            {
                switch(opcode & 0x00FF)
                {
                    case 0x0007: //Set VX to the value of delay_timer
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        V[x] = (char) delay_timer;
                        pc += 2;
                        Log.d("CHIP", "V["+ x +"] has been set to "+ delay_timer);
                        break;
                    }

                    case 0x000A: // A key press is awaited, and then stored in VX
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        for (int i = 0; i < keys.length; i++)
                        {
                            if(keys[i] == 1)
                            {
                                V[x] = (char)i;
                                pc += 2;
                                break;
                            }
                        }
                        Log.d("CHIP", "Awaiting key press to be store in V["+ x +"]");
                        break;
                    }

                    case 0x0015: //Set delay timer to V[x]
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        delay_timer = V[x];
                        pc += 2;
                        Log.d("CHIP", "Set delay_timer to V["+ x +"] = "+ (int)V[x]);
                        break;
                    }

                    case 0x0018: //Set sound timer to V[x]
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        sound_timer = V[x];
                        pc += 2;
                        break;
                    }

                    case 0x001E: //Add VX to I
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        I = (char)(I + V[x]);
                        Log.d("CHIP", "Adding V["+ x +"] = "+ (int)V[x] +"to I");
                        pc += 2;
                        break;
                    }

                    case 0x0029: //Sets I to the location of the sprite for character VX (Fontset)
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int character = V[x];
                        I = (char) (0x050 + (character * 5));
                        Log.d("CHIP", "Setting I to Character V["+ x +"] = "+ (int)V[x] +" Offset to 0x"+ Integer.toHexString(I).toUpperCase());
                        pc += 2;
                        break;
                    }

                    case 0x0033: //FX33 Store a binary-coded decimal value VX in I, I+1, and I+2
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        int value = V[x];
                        int hundreds = (value - (value % 100)) / 100;
                        value -= hundreds * 100;
                        int tens = (value - (value % 10)) / 10;
                        value -= tens * 10;
                        //int ones = x;
                        memory[I] = (char) hundreds;
                        memory[I + 1] = (char) tens;
                        memory[I + 2] = (char) value;
                        Log.d("CHIP", "Storing Binary-Coded Decimal At V["+ x +"] = "+ (int)(V[(opcode & 0x0F00) >> 8]) +" as {"+ hundreds +", "+ tens +", "+ value +"}");
                        pc += 2;
                        break;
                    }

                    case 0x0055: //Stores V0 to VX in memory starting at address I
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        for(int i = 0; i <= x; i++)
                        {
                            memory[I + i] = V[I];
                        }
                        Log.d("CHIP", "Setting Memory["+ Integer.toHexString(I & 0xFFFF).toUpperCase() +" + n] = V[0] tp V[X]");
                        pc += 2;
                        break;
                    }

                    case 0x0065: //Fx65 Files V0 to VX with values from I
                    {
                        int x = (opcode & 0x0F00) >> 8;
                        for(int i = 0; i <= x; i++)
                        {
                            V[i] = memory[I + i];
                        }
                        Log.d("CHIP", "Setting V[0] to V["+ x +"] to the values of memory["+ Integer.toHexString(I & 0xFFFF).toUpperCase() +"]");
                        I = (char)(I + x + 1); //As noted by note 4 in documentation
                        pc += 2;
                        break;
                    }

                    default:
                    {
                        System.err.println("Unsupported Opcode!");
                        System.exit(0);
                    }
                }
                break;
            }

            default:
            {
                System.err.println("Unsupported Opcode!");
                System.exit(0);
            }
        }

        if(sound_timer > 0)
        {
            sound_timer--;
            /***Mods For Android***/
            if (CAN_PLAY_SOUND)
            {
                mediaPlayer.start();
            }
            /**********************/
        }

        if(delay_timer > 0)
        {
            delay_timer--;
        }
    }




    public void stopEmulation()
    {
        mediaPlayer.release();
    }




    public byte[] getDisplay()
    {
        return display;
    }




    public boolean needsRedraw()
    {
        return needRedraw;
    }




    public void removeDrawFlag()
    {
        needRedraw = false;
    }
}
