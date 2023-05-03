package com.hashmac.snapshotscramble.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hashmac on 3/5/2023
 * Model for the game level
 * Purpose: To store the game level data
 * variables: number, image, type, target, name
 * number: the level number
 * image: the image of the level
 * type: the type of the level (1 = step, 2 = time)
 * target: the target of the level (step = number of steps, time = time in seconds)
 * name: the name of the level
 */

public class GameLevel {
    public static final int TYPE_TIME = 1;
    public static final int TYPE_MOVE = 0;
    private int number, image, type, target;
    private String name;
    static List<GameLevel> gameLevels = new ArrayList<>();

    public GameLevel(int number, int image, int type, int target, String name) {
        this.number = number;
        this.image = image;
        this.type = type;
        this.target = target;
        this.name = name;
    }

    public static void setGameLevels() {
        gameLevels.add(new GameLevel(1, 0, TYPE_TIME, 300, "Level 1"));
        gameLevels.add(new GameLevel(2, 0, TYPE_MOVE, 200, "Level 2"));
        gameLevels.add(new GameLevel(3, 0, TYPE_TIME, 50, "Level 3"));
        gameLevels.add(new GameLevel(4, 0, TYPE_MOVE, 50, "Level 4"));
        gameLevels.add(new GameLevel(5, 0, TYPE_TIME, 40, "Level 5"));
        gameLevels.add(new GameLevel(6, 0, TYPE_MOVE, 40, "Level 6"));
        gameLevels.add(new GameLevel(7, 0, TYPE_TIME, 30, "Level 7"));
        gameLevels.add(new GameLevel(8, 0, TYPE_MOVE, 30, "Level 8"));
        gameLevels.add(new GameLevel(9, 0, TYPE_TIME, 20, "Level 9"));
        gameLevels.add(new GameLevel(10, 0, TYPE_MOVE, 20, "Level 10"));
    }

    public static List<GameLevel> getGameLevels() {
        return gameLevels;
    }

    public static GameLevel getGameLevel(int level) {
        return gameLevels.get(level - 1);
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
