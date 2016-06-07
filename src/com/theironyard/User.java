package com.theironyard;

import java.util.ArrayList;

/**
 * Created by zach on 6/7/16.
 */
public class User {
    String name;
    String password;
    ArrayList<Restaurant> restaurants = new ArrayList<>();

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
}
