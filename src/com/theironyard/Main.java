package com.theironyard;

import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.util.HashMap;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) {
        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    HashMap m = new HashMap();
                    return new ModelAndView(m, "login.html");
                },
                new MustacheTemplateEngine()
        );
    }
}
