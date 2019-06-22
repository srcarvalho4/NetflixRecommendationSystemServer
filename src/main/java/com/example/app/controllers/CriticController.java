package com.example.app.controllers;

import com.example.app.models.*;
import com.example.app.repositories.CriticRepository;
import com.example.app.repositories.FanRepository;
import com.example.app.repositories.MovieRepository;
import com.example.app.repositories.ReviewRepository;
import com.example.app.services.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600, allowCredentials = "true")
public class CriticController extends Utils {

    private CriticRepository criticRepository;
    private MovieRepository movieRepository;
    private FanRepository fanRepository;

    @Autowired
    public CriticController(CriticRepository criticRepository, MovieRepository movieRepository,
                            ReviewRepository reviewRepository, FanRepository fanRepository) {
        this.criticRepository = criticRepository;
        this.movieRepository = movieRepository;
        this.fanRepository = fanRepository;
    }

    @PostMapping("/api/register/critic")
    public Critic createCritic(@RequestBody Critic critic, HttpServletResponse response) {
        try {
            response.setStatus(201);
            return criticRepository.save(critic);
        } catch(DataIntegrityViolationException e) {
            response.setStatus(409);
        }

        return null;
    }

    @GetMapping("/api/critic")
    public List<Critic> findAllCritics(
            @RequestParam(name = "username", required = false) String username,
            @RequestParam(name = "password", required = false) String password) {
        if (username != null && password != null)
            return (List<Critic>) criticRepository.findCriticByCredentials(username, password);
        return (List<Critic>) criticRepository.findAll();
    }

    @PostMapping("/api/recommend/critic/{username}/movie/{movieId}")
    public void recommendMovie(
            @PathVariable("username") String username,
            @PathVariable("movieId") long movieId) {
        if(movieRepository.findById(movieId).isPresent()
                && criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Movie movie = movieRepository.findById(movieId).get();
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            critic.recommends(movie);
            criticRepository.save(critic);
        }
    }

//    @PostMapping("/api/reviews/critic/{username}/review/{reviewId}")
//    public void reviewMovie(
//            @PathVariable("username") String username,
//            @PathVariable("reviewId") long reviewId) {
//        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()
//                && reviewRepository.findById(reviewId).isPresent()) {
//            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
//            Review review = reviewRepository.findById(reviewId).get();
//            critic.reviews(review);
//            criticRepository.save(critic);
//        }
//    }

    @PostMapping("/api/review/critic/{username}/movie/{movieId}")
    public void reviewMovie(@PathVariable("username") String username,
                            @PathVariable("movieId") Long movieId,
                            @RequestBody Review review) {
        if(movieRepository.findById(movieId).isPresent()
                && criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            Movie movie = movieRepository.findById(movieId).get();
            review.setCritic(critic);
            review.setRmovie(movie);
            critic.reviews(review);
            criticRepository.save(critic);
        }
    }

    @GetMapping("/api/follow/critic/{username}/followedby")
    public List<Fan> listOfFansFollowing(
            @PathVariable("username") String username) {
        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            return critic.getFansFollowingCritics();
        }
        return null;
    }

    @GetMapping("/api/check/follow/fan/{fanUsername}/critic/{criticUsername}")
    public Fan checkIfFanFollowsCritic(
            @PathVariable("fanUsername") String fanUsername,
            @PathVariable("criticUsername") String criticUsername) {
        if(criticRepository.findById(criticRepository.findCriticIdByUsername(criticUsername)).isPresent() &&
                fanRepository.findById(fanRepository.findFanIdByUsername(fanUsername)).isPresent()) {
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(criticUsername)).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(fanUsername)).get();
            List <Fan> fanlist = critic.getFansFollowingCritics();
            if (fanlist.contains(fan)) {
                return fan;
            }
        }

        return null;
    }

    @GetMapping("/api/recommend/critic/{username}/recommendedmovies")
    public List<Movie> listOfRecommendedMovies(
            @PathVariable("username") String username){
        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            return critic.getRecommendedMovies();
        }
        return null;
    }

//    @GetMapping("/api/review/critic/{username}/reviewedmovies")
//    public List<Review> listOfReviewsGiven(
//            @PathVariable("username") String username){
//        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
//            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
//            return critic.getReviewedMovie();
//        }
//        return null;
//    }

    @GetMapping("/api/review/critic/{username}/reviewedmovies")
    @ResponseBody
    public List<ReviewJson> listOfReviewsGiven(@PathVariable("username") String username){
        List<ReviewJson> result = new ArrayList<>();

        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username)).isPresent()) {
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username)).get();
            List<Review> reviews = critic.getReviewedMovie();

            for(Review r : reviews) {
                ReviewJson reviewJson = new ReviewJson();
                reviewJson.setReviewId(r.getReviewId());
                reviewJson.setReview(r.getReview());
                reviewJson.setRating(r.getRating());
                reviewJson.setId(r.getRmovie().getId());
                reviewJson.setTitle(r.getRmovie().getTitle());
                reviewJson.setPosterUrl(r.getRmovie().getPosterUrl());
                result.add(reviewJson);
            }
        }

        return result;
    }

    @PostMapping("/api/remove/critic/{username1}/fan/{username2}")
    public void deleteFans(
            @PathVariable("username1") String username1,
            @PathVariable("username2") String username2){
        if(criticRepository.findById(criticRepository.findCriticIdByUsername(username1)).isPresent()
                && fanRepository.findById(fanRepository.findFanIdByUsername(username2)).isPresent()){
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(username1)).get();
            Fan fan = fanRepository.findById(fanRepository.findFanIdByUsername(username2)).get();
            critic.getFansFollowingCritics().remove(fan);
            fan.getCriticsFollowed().remove(critic);
            criticRepository.save(critic);
        }
    }

    @PostMapping("/api/delete/recommend/critic/{criticName}/movie/{movieId}")
    public void deleteRecommendMovie(
            @PathVariable("criticName") String criticName,
            @PathVariable("movieId") long movieId){
        if (criticRepository.findById(criticRepository.findCriticIdByUsername(criticName)).isPresent()
                && movieRepository.findById(movieId).isPresent()){
            Critic critic = criticRepository.findById(criticRepository.findCriticIdByUsername(criticName)).get();
            Movie movie = movieRepository.findById(movieId).get();
            critic.getRecommendedMovies().remove(movie);
            movie.getRecommendedBy().remove(critic);
            criticRepository.save(critic);
        }
    }
}
