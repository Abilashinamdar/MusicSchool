package com.next.musicschool;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    static final int DELAY_IN_MILLISEC = 1500;

    RelativeLayout mLayoutMain;
    Spinner mSpinnerChooseNote;
    RadioGroup mRadioGroupScale;
    NumberPicker mNumberPicker;
    RadioButton mRadioButtonMajor;
    Button buttonPlay;
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


        mSpinnerChooseNote = (Spinner) findViewById(R.id.spinnerChooseNote);
        mRadioGroupScale = (RadioGroup) findViewById(R.id.radioGroupScale);
        mNumberPicker = (NumberPicker) findViewById(R.id.numberPicker);
        mRadioButtonMajor = (RadioButton) findViewById(R.id.radioButtonMajor);
        buttonPlay = (Button) findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play();
            }
        });

        mNumberPicker.setMinValue(2);
        mNumberPicker.setMaxValue(10);

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


        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            releasePlayer();
            if (mFileNames.length == mCurrentIndex) {
                mCurrentIndex = 0;
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
            mCurrentIndex --;
        }
    }

    private void createPlayer(String[] files) {
        mMediaPlayer = MediaPlayer.create(this, getResources().getIdentifier(files[mCurrentIndex], "raw", getPackageName()));
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mMediaPlayer = mediaPlayer;
                mCurrentIndex ++;
                mHandler.postDelayed(runnable, DELAY_IN_MILLISEC);
            }
        });
        mMediaPlayer.start();
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
