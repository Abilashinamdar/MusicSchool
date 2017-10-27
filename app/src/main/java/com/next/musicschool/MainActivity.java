package com.next.musicschool;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    static int DELAY_IN_MILLISEC = 250;

    RelativeLayout mLayoutMain;
    LinearLayout mLayoutButtons;
    Button buttonRepeat, buttonReset, buttonCheck;
    Spinner mSpinnerChooseNote;
    RadioGroup mRadioGroupScale;
    NumberPicker mNumberPicker;
    RadioButton mRadioButtonMajor;
    SeekBar mSeekBarTempo;
    Button mButtonPlay;
    MediaPlayer mMediaPlayer;

    String[] notesArray;
    List<String> notesList;
    int mCurrentIndex = 0;
    String[] mFileNames;

    Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        notesArray = getResources().getStringArray(R.array.notes);
        notesList = Arrays.asList(notesArray);


        mLayoutMain = (RelativeLayout) findViewById(R.id.layoutMain);
        mLayoutButtons = (LinearLayout) findViewById(R.id.layoutButtons);
        mSpinnerChooseNote = (Spinner) findViewById(R.id.spinnerChooseNote);
        mRadioGroupScale = (RadioGroup) findViewById(R.id.radioGroupScale);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mRadioButtonMajor = (RadioButton) findViewById(R.id.radioButtonMajor);
        mSeekBarTempo = (SeekBar) findViewById(R.id.seekBarTempo);
        mSeekBarTempo.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int value = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                value = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                System.out.println(value);
                int tempo = 60 + 2 * value; //in BPM
                int time = (int) 1000 / (tempo / 60);
                DELAY_IN_MILLISEC = time;


            }
        });

        buttonRepeat = (Button) findViewById(R.id.buttonRepeat);
        buttonRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayoutButtons.setVisibility(View.GONE);
                createPlayer(mFileNames);
            }
        });

        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reset();
            }
        });

        buttonCheck = (Button) findViewById(R.id.buttonCheck);
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SoundCheckActivity.class));
            }
        });

        mButtonPlay = (Button) findViewById(R.id.buttonPlay);
        mButtonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                play();
            }
        });

        mNumberPicker.setMinValue(2);
        mNumberPicker.setMaxValue(10);

    }

    private void reset() {
        mLayoutButtons.setVisibility(View.GONE);
        mButtonPlay.setVisibility(View.VISIBLE);

        mSeekBarTempo.setProgress(0);
        mNumberPicker.setValue(2);
        mRadioButtonMajor.setChecked(true);
        mSpinnerChooseNote.setSelection(0);
    }

    private void play() {
        // first check user selected note or not
        String selectedItem = (String) mSpinnerChooseNote.getSelectedItem();
        if (selectedItem == null || selectedItem.isEmpty()) {
            showSnackBar("Please select note.");
            return;
        }

        int index = notesList.indexOf(selectedItem);

        try {
            String[] scaleArray = NotePicker.scaleMaker(mRadioButtonMajor.isChecked(), index, notesArray);
            System.out.println("Scale array = " + Arrays.toString(scaleArray));

            scaleArray = NotePicker.notePick(scaleArray, mNumberPicker.getValue());

            System.out.println("Final notes to play = " + Arrays.toString(scaleArray));

            mFileNames = getFileNames(scaleArray);

            createPlayer(mFileNames);

//            createSoundPool(mFileNames);


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            releasePlayer();
            if (mFileNames.length == mCurrentIndex) {
                mCurrentIndex = 0;

                mLayoutButtons.setVisibility(View.VISIBLE);
                return;
            }

            createPlayer(mFileNames);
        }

    };

    private void releasePlayer() {
        try {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            mCurrentIndex--;
        }
    }

    private void createSoundPool(String[] files) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            SoundPool soundPool = new SoundPool.Builder()
                    .setMaxStreams(files.length)
                    .build();
            final float playbackSpeed = 2.0f;
            int raw = getResources().getIdentifier(files[mCurrentIndex], "raw", getPackageName());
            final int soundId = soundPool.load(this, raw, 1);
            soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    soundPool.play(soundId, 1.0f, 1.0f, 1, 0, playbackSpeed);
                }
            });
        }
    }

    private void createPlayer(String[] files) {
        /*mMediaPlayer = MediaPlayer.create(this, getResources().getIdentifier(files[mCurrentIndex], "raw", getPackageName()));
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer = mediaPlayer;
                mCurrentIndex ++;
                mHandler.postDelayed(runnable, DELAY_IN_MILLISEC);
            }
        });
        mMediaPlayer.start();*/

        MediaPlayer mediaPlayer = MediaPlayer.create(this, getResources().getIdentifier(files[mCurrentIndex], "raw", getPackageName()));
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mCurrentIndex++;
                mHandler.postDelayed(runnable, DELAY_IN_MILLISEC);
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        mediaPlayer.start();


    }

    private String[] getFileNames(String[] names) {
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            name = name.toLowerCase();
            if (name.length() > 1) {
                name = name.charAt(0) + "_" + name.charAt(1);
            }
            names[i] = name;
        }
        return names;
    }

    public void showSnackBar(String msg) {
        Snackbar.make(mLayoutMain, msg, Snackbar.LENGTH_LONG).show();
    }
}
