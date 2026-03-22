package com.hasbropulse.scrabble.model;

// holds the score and tile count for a single Scrabble letter, mapped from letter_data.json
public class LetterInfo {

    private int score;
    private int tiles;

    public LetterInfo() {}

    public LetterInfo(int score, int tiles) {
        this.score = score;
        this.tiles = tiles;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTiles() {
        return tiles;
    }

    public void setTiles(int tiles) {
        this.tiles = tiles;
    }
}
