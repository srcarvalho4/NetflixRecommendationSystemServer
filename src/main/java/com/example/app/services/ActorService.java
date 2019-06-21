package com.example.app.services;

import com.example.app.models.Actor;
import com.example.app.models.Movie;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ActorService {

    private static String apiKey = "5d837b926bca6fa72fb4cf1b287a1bfa";
    private static String apiBaseUri = "https://api.themoviedb.org/3/";
    private static String imageServerPath = "https://image.tmdb.org/t/p/w500";

    private ActorService() { }

    public static List<Actor> searchActors(String query, String lang, String region, String pageNo) {
        List<Actor> searchResults = new ArrayList<>();

        String urlString = apiBaseUri + "search/person";
        urlString += "?api_key=" + apiKey;
        urlString += (lang != null) ? ("&language=" + lang) : "";
        urlString += (region != null) ? ("&region=" + region) : "";
        urlString += (query != null) ? ("&query=" + query) : "";
        urlString += (pageNo != null) ? ("&page=" + pageNo) : "";

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if(conn.getResponseCode() == 200) {
                StringBuilder responseString = new StringBuilder();
                Scanner sc = new Scanner(url.openStream());

                while(sc.hasNext()) {
                    responseString.append(sc.nextLine());
                }
                sc.close();

                JSONObject responseJson = new JSONObject(responseString.toString());
                JSONArray results = responseJson.getJSONArray("results");

                for(Object actor : results) {
                    JSONObject actorJsonObj = (JSONObject) actor;

                    Long actorId = actorJsonObj.has("id") ? new Long(actorJsonObj.getInt("id")) : null;
                    String actorName = actorJsonObj.has("name") ? actorJsonObj.getString("name") : null;
                    String profilePicture = imageServerPath +
                            (actorJsonObj.has("profile_path") ? actorJsonObj.getString("profile_path") : null);
                    String actorPopularity = "" +
                            (actorJsonObj.has("popularity") ? actorJsonObj.getFloat("popularity") : null);
                    JSONArray knownFor = actorJsonObj.has("known_for") ? actorJsonObj.getJSONArray("known_for") : null;
                    List<Movie> movies = new ArrayList<>();

                    for(Object movie : knownFor) {
                        JSONObject movieJsonObj = (JSONObject) movie;

                        Long movieId = actorJsonObj.has("id") ? new Long(movieJsonObj.getInt("id")) : null;
                        String title = actorJsonObj.has("title") ? movieJsonObj.getString("title") : null;
                        String posterUrl = imageServerPath +
                                (actorJsonObj.has("poster_path") ? movieJsonObj.getString("poster_path"): null);
                        String overview = actorJsonObj.has("overview") ? movieJsonObj.getString("overview") : null;
                        String releaseDate = actorJsonObj.has("release_date") ? movieJsonObj.getString("release_date") : null;

                        movies.add(new Movie(movieId, title, null, posterUrl, overview, null,
                                releaseDate, null, null));
                    }

                    searchResults.add(new Actor(actorId, actorName, null, null, null, null,
                            actorPopularity, profilePicture, null, movies));
                }
            }
        } catch(Exception e) {
            System.out.println(e.toString());
        } finally {
            if(conn != null) {
                conn.disconnect();
            }
        }

        return searchResults;
    }

}