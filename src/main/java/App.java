import com.google.gson.Gson;
import dao.Sql2oFoodtypeDao;
import dao.Sql2oRestaurantDao;
import dao.Sql2oReviewDao;
import exceptions.ApiException;
import models.Foodtype;
import models.Restaurant;
import models.Review;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class App {
    public static void main(String[] args) {
        Sql2oFoodtypeDao foodtypeDao;
        Sql2oRestaurantDao restaurantDao;
        Sql2oReviewDao reviewDao;
        Connection conn;
        Gson gson = new Gson();

        String connectionString = "jdbc:h2:~/jadle.db;INIT=RUNSCRIPT from 'classpath:db/create.sql'"; //check me!

        Sql2o sql2o = new Sql2o(connectionString, "", "");
        restaurantDao = new Sql2oRestaurantDao(sql2o);
        foodtypeDao = new Sql2oFoodtypeDao(sql2o);
        reviewDao = new Sql2oReviewDao(sql2o);
        conn = sql2o.open();

        //CREATE
        post("/restaurants/new", "application/json", (req, res) -> {
            Restaurant restaurant = gson.fromJson(req.body(), Restaurant.class);
            restaurantDao.add(restaurant);
            res.status(201);
            return gson.toJson(restaurant);
        });

        //READ
        get("/restaurants", "application/json", (req, res) -> { //accept a request in format JSON from an app
            return gson.toJson(restaurantDao.getAll());//send it back to be displayed
        });

        get("/restaurants/:id", "application/json", (req, res) -> { //accept a request in format JSON from an app
            res.type("application/json");
            int restaurantId = Integer.parseInt(req.params("id"));
            Restaurant restaurantToFind = restaurantDao.findById(restaurantId);
            if (restaurantToFind == null){
                throw new RuntimeException(String.format("No restaurant with the id: %s exists", req.params("id")));
            }

            return gson.toJson(restaurantToFind);
        });

        //CREATE
        post("/restaurants/:restaurantId/reviews/new", "application/json", (req, res) -> {
            int restaurantId = Integer.parseInt(req.params("restaurantId"));
            Review review = gson.fromJson(req.body(), Review.class);
            review.setRestaurantId(restaurantId);
            reviewDao.add(review);
            res.status(201);
            return gson.toJson(review);
        });

        //READ
        get("/restaurants/:restaurantId/reviews", "application/json", (req, res) -> { //accept a request in format JSON from an app
            int restaurantId = Integer.parseInt(req.params("restaurantId"));
            return gson.toJson(reviewDao.getAllReviewsByRestaurant(restaurantId));//send it back to be displayed
        });

        get("/reviews/:id", "application/json", (req, res) -> { //accept a request in format JSON from an app
            res.type("application/json");
            int reviewId = Integer.parseInt(req.params("id"));
            return gson.toJson(reviewDao.findById(reviewId));
        });

        //CREATE
        post("/foodtype/new", "application/json", (req, res) -> {
            Foodtype foodtype = gson.fromJson(req.body(), Foodtype.class);
            foodtypeDao.add(foodtype);
            res.status(201);
            return gson.toJson(foodtype);
        });

        //READ
        get("/foodtype", "application/json", (req, res) -> { //accept a request in format JSON from an app
            return gson.toJson(foodtypeDao.getAll());//send it back to be displayed
        });

//        get("/foodtype/:id", "application/json", (req, res) -> { //accept a request in format JSON from an app
//            int foodtypeId = Integer.parseInt(req.params("id"));
//            return gson.toJson(foodtypeDao.(restaurantId));
//        });


//        FILTERS
        exception(ApiException.class, (exc, req, res) -> {
            RuntimeException err = exc;
            Map<String, Object> jsonMap = new HashMap<>();
//            jsonMap.put("status", err.getStatusCode());
            jsonMap.put("errorMessage", err.getMessage());
            res.type("application/json"); //after does not run in case of an exception.
//            res.status(err.getStatusCode()); //set the status
            res.body(gson.toJson(jsonMap));  //set the output.
        });

        after((req, res) ->{
            res.type("application/json");
        });
    }
}
