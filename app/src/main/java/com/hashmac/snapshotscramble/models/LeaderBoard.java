package com.hashmac.snapshotscramble.models;

public class LeaderBoard {
    private String name;
    private int score;

    public LeaderBoard() {
    }

    public LeaderBoard(String name, int score) {
        this.name = name;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }
}
