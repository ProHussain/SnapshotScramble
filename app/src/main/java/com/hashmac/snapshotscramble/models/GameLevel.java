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
        gameLevels.add(new GameLevel(3, 0, TYPE_TIME, 250, "Level 3"));
        gameLevels.add(new GameLevel(4, 0, TYPE_MOVE, 180, "Level 4"));
        gameLevels.add(new GameLevel(5, 0, TYPE_TIME, 200, "Level 5"));
        gameLevels.add(new GameLevel(6, 0, TYPE_MOVE, 160, "Level 6"));
        gameLevels.add(new GameLevel(7, 1, TYPE_TIME, 170, "Level 7"));
        gameLevels.add(new GameLevel(8, 1, TYPE_MOVE, 140, "Level 8"));
        gameLevels.add(new GameLevel(9, 1, TYPE_TIME, 150, "Level 9"));
        gameLevels.add(new GameLevel(10, 1, TYPE_MOVE, 120, "Level 10"));
        gameLevels.add(new GameLevel(11, 1, TYPE_TIME, 130, "Level 11"));
        gameLevels.add(new GameLevel(12, 1, TYPE_MOVE, 100, "Level 12"));
        gameLevels.add(new GameLevel(13, 2, TYPE_TIME, 110, "Level 13"));
        gameLevels.add(new GameLevel(14, 2, TYPE_MOVE, 100, "Level 14"));
        gameLevels.add(new GameLevel(15, 2, TYPE_TIME, 100, "Level 15"));
        gameLevels.add(new GameLevel(16, 2, TYPE_MOVE, 120, "Level 16"));
        gameLevels.add(new GameLevel(17, 2, TYPE_TIME, 120, "Level 17"));
        gameLevels.add(new GameLevel(18, 2, TYPE_MOVE, 140, "Level 18"));
        gameLevels.add(new GameLevel(19, 3, TYPE_TIME, 140, "Level 19"));
        gameLevels.add(new GameLevel(20, 3, TYPE_MOVE, 160, "Level 20"));
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
