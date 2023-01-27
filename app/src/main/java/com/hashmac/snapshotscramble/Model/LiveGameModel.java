package com.hashmac.snapshotscramble.Model;

public class LiveGameModel {
    private String GameID,UserOne,UserTwo,Status,Winner;

    public LiveGameModel() {
    }

    public LiveGameModel(String gameID, String userOne, String userTwo, String status, String winner) {
        GameID = gameID;
        UserOne = userOne;
        UserTwo = userTwo;
        Status = status;
        Winner = winner;
    }

    public String getGameID() {
        return GameID;
    }

    public void setGameID(String gameID) {
        GameID = gameID;
    }

    public String getUserOne() {
        return UserOne;
    }

    public void setUserOne(String userOne) {
        UserOne = userOne;
    }

    public String getUserTwo() {
        return UserTwo;
    }

    public void setUserTwo(String userTwo) {
        UserTwo = userTwo;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getWinner() {
        return Winner;
    }

    public void setWinner(String winner) {
        Winner = winner;
    }
}
