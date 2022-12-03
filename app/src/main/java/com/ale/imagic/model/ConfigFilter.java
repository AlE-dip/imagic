package com.ale.imagic.model;

import java.util.ArrayList;

public class ConfigFilter {
    public ArrayList<Selection> selections;
    public ArrayList<SeekBar> seekBars;
    public int selected;

    public ConfigFilter() {
        this.selections = null;
        this.seekBars = null;
        selected = 0;
    }

    public synchronized void setSelected(int selected) {
        this.selected = selected;
    }

    public void createSelection(int value, String name) {
        Selection selection = new Selection(value, name);
        if (selections != null) {
            selections.add(selection);
        } else {
            selections = new ArrayList<>();
            selections.add(selection);
        }
    }

    public class Selection {

        public int value;
        public String name;

        public Selection(int value, String name) {
            this.value = value;
            this.name = name;
        }
    }

    public void createSeekBar(int value, int minSeekBar, int maxSeekBar, String name) {
        SeekBar seekBar = new SeekBar(value, minSeekBar, maxSeekBar, name);
        if (seekBars != null) {
            seekBars.add(seekBar);
        } else {
            seekBars = new ArrayList<>();
            seekBars.add(seekBar);
        }
    }

    public class SeekBar {

        public int value;
        public int minSeekBar;
        public int maxSeekBar;
        public String name;

        public synchronized void setValue(int value) {
            this.value = value;
        }

        public SeekBar(int value, int minSeekBar, int maxSeekBar, String name) {
            this.value = value;
            this.minSeekBar = minSeekBar;
            this.maxSeekBar = maxSeekBar;
            this.name = name;
        }
    }
}
