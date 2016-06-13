package com.theironyard;

import com.sun.org.apache.regexp.internal.RE;
import org.h2.tools.Server;
import spark.ModelAndView;
import spark.Session;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void insertRstnts(Connection conn, String name, String location, Integer rating, String comment) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO rstnts VALUES(NULL, ?, ?, ?, ?)");
        stmt.setString(1, name);
        stmt.setString(2, location);
        stmt.setInt(3, rating);
        stmt.setString(4, comment);
        stmt.execute();
    }

    public static void deleteRstnts(Connection conn, int id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM rstnts WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();

    }

    public static ArrayList<Restaurant> selectRstnts(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM  rstnts");
        ResultSet results = stmt.executeQuery();
        ArrayList<Restaurant> res = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String name = results.getString("name");
            String location = results.getString("location");
            int rating = results.getInt("rating");
            String comment = results.getNString("comment");
            Restaurant restaurant = new Restaurant(id, name, location, rating, comment);
            res.add(restaurant);
        }
        return res;
    }

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args) throws SQLException {
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");

        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS rstnts (id IDENTITY, name VARCHAR, location VARCHAR, rating INT, comment VARCHAR)");

        Spark.init();
        Spark.get(
                "/",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");

                    HashMap m = new HashMap();
                    if (username == null) {
                        return new ModelAndView(m, "login.html");
                    }
                    else {
                        User user = users.get(username);
                        m.put("restaurants", selectRstnts(conn));
                        return new ModelAndView(m, "home.html");
                    }
                },
                new MustacheTemplateEngine()
        );
        Spark.post(
                "/login",
                (request, response) -> {
                    String name = request.queryParams("username");
                    String pass = request.queryParams("password");
                    if (name == null || pass == null) {
                        throw new Exception("Name or pass not sent");
                    }

                    User user = users.get(name);
                    if (user == null) {
                        user = new User(name, pass);
                        users.put(name, user);
                    }
                    else if (!pass.equals(user.password)) {
                        throw new Exception("Wrong password");
                    }

                    Session session = request.session();
                    session.attribute("username", name);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/create-restaurant",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }

                    String name = request.queryParams("name");
                    String location = request.queryParams("location");
                    int rating = Integer.valueOf(request.queryParams("rating"));
                    String comment = request.queryParams("comment");
                    if (name == null || location == null || comment == null) {
                        throw new Exception("Invalid form fields");
                    }

                    User user = users.get(username);
                    if (user == null) {
                        throw new Exception("User does not exist");
                    }

//                    Restaurant r = new Restaurant(name, location, rating, comment);
//                    user.restaurants.add(r);
                    insertRstnts(conn, name, location, rating, comment);

                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/logout",
                (request, response) -> {
                    Session session = request.session();
                    session.invalidate();
                    response.redirect("/");
                    return "";
                }
        );
        Spark.post(
                "/delete-restaurant",
                (request, response) -> {
                    Session session = request.session();
                    String username = session.attribute("username");
                    if (username == null) {
                        throw new Exception("Not logged in");
                    }

                    int id = Integer.valueOf(request.queryParams("id"));
                    deleteRstnts(conn, id);

//                    User user = users.get(username);
//                    if (id <= 0 || id - 1 >= user.restaurants.size()) {
//                        throw new Exception("Invalid id");
//                    }
//                    user.restaurants.remove(id - 1);

                    response.redirect("/");
                    return "";
                }
        );
    }
}
