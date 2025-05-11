package com.movieapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import com.movieapp.utils.StageManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.scene.layout.StackPane;

public class MovieSearchController {
    private static final String OMDB_API_URL = "https://www.omdbapi.com/";
    private static final String OMDB_API_KEY = "f8038d4d";

    @FXML
    private VBox movieGrid;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;
    @FXML
    private Button backButton;
    @FXML
    private ComboBox<String> genreComboBox;

    private final HttpClient httpClient;
    private final ExecutorService executorService;
    private List<JSONObject> allMovies = new ArrayList<>();

    private final List<String> genres = List.of(
            "All", "Action", "Adventure", "Animation", "Biography", "Comedy",
            "Crime", "Drama", "Family", "Fantasy", "History", "Horror",
            "Music", "Mystery", "Romance", "Sci-Fi", "Sport", "Thriller", "War", "Western");

    public MovieSearchController() {
        this.httpClient = HttpClient.newHttpClient();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    @FXML
    public void initialize() {
        searchButton.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch());
        backButton.setOnAction(e -> goBack());

        genreComboBox.getItems().addAll(genres);
        genreComboBox.setValue("All");
        genreComboBox.setOnAction(e -> filterMoviesByGenre(genreComboBox.getValue()));

        loadPopularMovies();
    }

    private void performSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            executorService.submit(() -> {
                try {
                    String response = searchMovies(query);
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(response);
                    JSONArray results = (JSONArray) json.get("Search");

                    if (results != null) {
                        List<JSONObject> movies = new ArrayList<>();
                        for (Object result : results) {
                            JSONObject item = (JSONObject) result;
                            String details = getMovieDetails((String) item.get("imdbID"));
                            JSONObject fullDetails = (JSONObject) parser.parse(details);
                            movies.add(fullDetails);
                        }
                        allMovies = movies;
                        Platform.runLater(() -> displayFilteredMovies());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private void loadPopularMovies() {
        String[] popular = {

                "The Shawshank Redemption", "The Godfather", "The Dark Knight", "Pulp Fiction", "Forrest Gump",

                "Inception", "The Matrix", "Interstellar", "The Lord of the Rings", "Avengers: Endgame",

                "The Green Mile", "Fight Club", "The Silence of the Lambs", "Goodfellas", "The Departed",

                "Star Wars", "Blade Runner", "The Prestige", "The Shining", "Back to the Future",

                "Die Hard", "Indiana Jones", "Gladiator", "The Terminator", "Mad Max: Fury Road",

                "Spirited Away", "Toy Story", "The Lion King", "Your Name", "Spider-Man: Into the Spider-Verse"
        };

        synchronized (allMovies) {
            allMovies.clear();
        }
        movieGrid.getChildren().clear();

        for (String movie : popular) {
            executorService.submit(() -> {
                try {
                    String response = searchMovies(movie);
                    JSONParser parser = new JSONParser();
                    JSONObject json = (JSONObject) parser.parse(response);
                    JSONArray results = (JSONArray) json.get("Search");

                    if (results != null && !results.isEmpty()) {
                        JSONObject movieData = (JSONObject) results.get(0);
                        String details = getMovieDetails((String) movieData.get("imdbID"));
                        JSONObject fullDetails = (JSONObject) parser.parse(details);

                        synchronized (allMovies) {
                            allMovies.add(fullDetails);

                            List<JSONObject> moviesCopy = new ArrayList<>(allMovies);
                            Platform.runLater(() -> displayFilteredMovies(moviesCopy));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    private String searchMovies(String query) throws Exception {
        String encoded = URLEncoder.encode(query, "UTF-8");
        String url = OMDB_API_URL + "?s=" + encoded + "&apikey=" + OMDB_API_KEY;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private String getMovieDetails(String imdbID) throws Exception {
        String url = OMDB_API_URL + "?i=" + imdbID + "&apikey=" + OMDB_API_KEY;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private void displayFilteredMovies() {
        displayFilteredMovies(allMovies);
    }

    private void displayFilteredMovies(List<JSONObject> movies) {
        String selectedGenre = genreComboBox.getValue();
        movieGrid.getChildren().clear();

        for (JSONObject movie : movies) {
            if (selectedGenre.equals("All") || genreMatches(movie, selectedGenre)) {
                HBox card = createMovieCard(movie);
                movieGrid.getChildren().add(card);
            }
        }
    }

    private boolean genreMatches(JSONObject movie, String selectedGenre) {
        String genreStr = (String) movie.get("Genre");
        if (genreStr == null)
            return false;
        return Arrays.stream(genreStr.split(","))
                .map(String::trim)
                .anyMatch(g -> g.equalsIgnoreCase(selectedGenre));
    }

    private HBox createMovieCard(JSONObject movie) {
        HBox card = new HBox(10);
        card.getStyleClass().add("movie-card");
        card.setOnMouseClicked(e -> showMovieDetails(movie));

        ImageView posterView = new ImageView();
        posterView.setFitHeight(150);
        posterView.setFitWidth(100);
        posterView.setPreserveRatio(true);

        String posterUrl = (String) movie.get("Poster");
        if (posterUrl != null && !posterUrl.equals("N/A")) {
            try {
                posterView.setImage(new Image(posterUrl, true));
            } catch (Exception ignored) {
            }
        }

        VBox infoBox = new VBox(5);
        Label titleLabel = new Label((String) movie.get("Title"));
        titleLabel.getStyleClass().add("movie-title");

        Label yearLabel = new Label((String) movie.get("Year"));
        yearLabel.getStyleClass().add("movie-info");

        String rating = (String) movie.get("imdbRating");
        if (rating != null && !rating.equals("N/A")) {
            Label ratingLabel = new Label("Rating: " + rating + "/10");
            ratingLabel.getStyleClass().add("movie-info");
            infoBox.getChildren().addAll(titleLabel, yearLabel, ratingLabel);
        } else {
            infoBox.getChildren().addAll(titleLabel, yearLabel);
        }

        card.getChildren().addAll(posterView, infoBox);
        return card;
    }

    private void showMovieDetails(JSONObject movie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/movieapp/view/MovieDetailsDialog.fxml"));
            Parent detailsView = loader.load();

            MovieDetailsController controller = loader.getController();
            controller.setMovieData(movie);

            StackPane mainStackPane = (StackPane) searchField.getScene().getRoot();
            mainStackPane.getChildren().add(detailsView);

            detailsView.setOnMouseClicked(e -> {
                if (e.getTarget() == detailsView) {
                    mainStackPane.getChildren().remove(detailsView);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterMoviesByGenre(String genre) {
        Platform.runLater(this::displayFilteredMovies);
    }

    private void goBack() {
        StageManager.getInstance().loadScene(
                "/com/movieapp/view/MovieAppScreen.fxml",
                "/com/movieapp/styles/movieApp.css",
                "Movie Night");
    }
}
