package com.hashmac.snapshotscramble.models;

/**
 * Created by hash on 5/3/2023.
 * User model class
 * Purpose: To provide the user model for the app
 * 1. User id to identify the user
 * 2. User name to show the user name
 * 3. User email to show the user email
 * 4. User photo url to show the user photo
 * Data get from the google login
 */

public class User {
    private String id, name, email, photoUrl;

    public User() {
    }

    public User(String id, String name, String email, String photoUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
