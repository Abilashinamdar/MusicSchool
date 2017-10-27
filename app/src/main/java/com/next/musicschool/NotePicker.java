package com.next.musicschool;

import java.util.Arrays;

/**
 * Created by abhilashb on 27/10/17.
 */

public class NotePicker {

    public static String[] notePick(String[] scaleChosen, int numberOfNotes) {
        String[] notes = new String[numberOfNotes];
        for (int i = 0; i < numberOfNotes; i++) {
            int indexRand = (int) (Math.random() * scaleChosen.length);
            notes[i] = scaleChosen[indexRand];
        }
        return notes;
    }

    public static String[] scaleMaker(boolean majorScale, int index, String[] notes) throws java.lang.Exception {
        if (notes.length != 12)
            throw new java.lang.Exception();

        //the eight note should be same as first, but double its frequency
        String[] scale = new String[8];
        int[] major = {2, 2, 1, 2, 2, 2, 1};
        int[] minor = {2, 1, 2, 2, 1, 2, 2};
        scale[0] = notes[index];
        scale[7] = scale[0];
        for (int i = 0; i < 7; i++) {
            if (majorScale) {
                index = (index + major[i]) % 12;
                scale[i + 1] = notes[index];
            } else {
                System.out.println(index);
                index = (index + minor[i]) % 12;
                // .out.println(index);
                scale[i + 1] = notes[index];
            }
        }
        return scale;
    }

    public static void main(String[] args) throws java.lang.Exception {
        String[] notes = {"C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N"};
        String[] scale = NotePicker.scaleMaker(false, 1, notes);
        System.out.println(Arrays.toString(scale));
        String[] play = NotePicker.notePick(scale, 11);
        System.out.println(Arrays.toString(play));
    }
}


