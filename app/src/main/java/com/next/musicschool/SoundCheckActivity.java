package com.next.musicschool;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AndroidAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;

/**
 * Created by abhilashb on 27/10/17.
 */

public class SoundCheckActivity extends AppCompatActivity {

    TextView noteText, pitchText;
    String[] noteNames = new String[]{"c", "db", "d", "e", "eb", "f", "gb", "g", "ab", "a", "bb", "b"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_check);

        noteText = (TextView) findViewById(R.id.noteText);
        pitchText = (TextView) findViewById(R.id.pitchText);

//        pitchDetectionAndDisplay();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        pitchDetectionAndDisplay();
        return super.onCreateOptionsMenu(menu);
    }

    public void pitchDetectionAndDisplay() {
        /*String s = findAudioRecord();
        String[] split = s.split(",");*/
        AudioDispatcher audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);

        PitchDetectionHandler pdh = new PitchDetectionHandler() {
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                final float pitchInHz = pitchDetectionResult.getPitch();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        processPitch(pitchInHz);
                    }
                });
            }
        };


        AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
        audioDispatcher.addAudioProcessor(pitchProcessor);
        Thread audioThread = new Thread(audioDispatcher, "Audio Thread");
        audioThread.start();
    }

    public int getBestSampleRate() {
        if (Build.VERSION.SDK_INT >= 17) {
            AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            String sampleRateString = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            int sampleRate = sampleRateString == null ? 22050 : Integer.parseInt(sampleRateString);
            return sampleRate;
        } else {
            return 22050;
        }
    }


    public double logBase2(float f) {
        double t;
        t = Math.log(f) / Math.log(2);
        return t;
    }

    public void processPitch(float pitchInHz) {
        pitchText.setText("" + pitchInHz);
        double rowOctave = logBase2((pitchInHz / 440));
        if (pitchInHz != -1) {
            int noteNameIndex = (int) (Math.floor((69 + 12 * rowOctave) % 12));
            System.out.println(noteNameIndex);
            noteText.setText(noteNames[noteNameIndex]);
        }
    }

    private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
    public String findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
                for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
                    try {
                        /*Log.d(C.TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);*/
                        int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

                            if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
                                return rate+","+bufferSize;
                        }
                    } catch (Exception e) {
                        /*Log.e(C.TAG, rate + "Exception, keep trying.",e);*/
                    }
                }
            }
        }
        return null;
    }
}
