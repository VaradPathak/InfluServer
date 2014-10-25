/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend;

import com.influir.datacollectionbackend.entities.*;
import com.influir.libraries.json.*;
import com.influir.libraries.http.*;
import com.influir.libraries.utils.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Pranit
 */
public class MoviesCollector
{
    final private static String RT_KEY = "apikey=xj6tr26gjgnshjfbab7nggkr";
    final private static String TMDB_KEY = "api_key=64cbd56967e9c36613fa52869d9c847d";
    final private static String RT_MOVIE_SEARCH = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?" + RT_KEY + "&q=";
    final private static String TMDB_MOVIE_SEARCH = "http://api.themoviedb.org/3/search/movie?" + TMDB_KEY + "&query=";
    final private static String TMDB_MOVIE_DETAIL = "http://api.themoviedb.org/3/movie/";

    public static void main(String[] args) throws FileNotFoundException, IOException, JSONException
    {
        Set<String> top250MoviesSet = new HashSet<>();
        String fileBase = FileManager.readFile("TOP250Movies.data");
        JSONArray top250MoviesJson = new JSONArray(fileBase);
        for (int i = 0; i < top250MoviesJson.length(); i++)
        {
            top250MoviesSet.add(top250MoviesJson.getJSONObject(i).getString("IMDBTITLE"));
        }

        Map<String, JSONObject> movies = new HashMap<>();
        JSONArray allMovies = new JSONArray();
        for (int i = 1; i <= 6; i++)
        {
            System.out.println("Now fetching : " + i + ".txt");
            String file = FileManager.readFile(i + ".txt");
            JSONArray current = new JSONArray(file);
            for (int j = 0; j < current.length(); j++)
            {
                JSONObject currentMovie = current.getJSONObject(j);
                if (!top250MoviesSet.contains(currentMovie.getString("Title"))
                        && !movies.containsKey(currentMovie.getString("Title"))
                        && !currentMovie.isNull("Director"))
                {
                    movies.put(currentMovie.getString("Title"), currentMovie);
                    allMovies.put(currentMovie);
                }
            }
        }

        System.out.println(movies.size());
        FileManager.writeToFile("AllMovies.txt", allMovies.toString(4));

        //Code from IMDBTop250MoviesCollector
        ArrayList<DetailedMovie> allMovieObjects = new ArrayList<>();
        JSONArray jsonArray = allMovies;

        Calendar startTime = Calendar.getInstance();

        for (int i = 3502; i < jsonArray.length(); i++)
        {
            if (i == 0)
            {
                InfluLogger.print("Now fetching movie: " + (i + 1));
            }
            else
            {
                Calendar now = Calendar.getInstance();
                float difference = (now.getTimeInMillis() - startTime.getTimeInMillis()) / 60000f;
                difference = (difference / i) * (jsonArray.length() - i);
                InfluLogger.print("Now fetching movie: " + (i + 1) + ".\tEstimated time remaining: " + (int) difference + " minutes.");
            }
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String movieTitle = jsonObject.getString("Title");
            int movieYear = 0;
            if (!jsonObject.isNull("Year"))
            {
                try
                {
                    movieYear = jsonObject.getInt("Year");
                }
                catch (Exception e)
                {
                }
            }

            String query = URLEncoder.encode(movieTitle, "UTF-8");
            String url = RT_MOVIE_SEARCH + query;//+ SEARCH_RESULT_LIMIT;
            String response;
            try
            {
                response = HttpResponseGetter.getHttpResponse(url);
            }
            catch (InfluException ex)
            {
                InfluLogger.print("Movie unsuccessful: " + movieTitle);
                continue;
            }
            String matchedMovieRTURL = "";
            try
            {
                matchedMovieRTURL = matchMovieFromRT(response, movieTitle, movieYear);
                if (matchedMovieRTURL != null)
                {
                    DetailedMovie detailedMovie;
                    try
                    {
                        detailedMovie = getDetailedMovieFromRTByURL(matchedMovieRTURL + "?" + RT_KEY, movieTitle, movieYear, null);
                    }
                    catch (InfluException ex)
                    {
                        continue;
                    }
                    matchMovieAndGetDetailsFromTMDB(detailedMovie);
                    InfluLogger.print("Movie Sucessfull: " + movieTitle);
                    allMovieObjects.add(detailedMovie);
                }
                else
                {
                    InfluLogger.print("URL null for: " + movieTitle);
                }
            }
            catch (JSONException e)
            {
                InfluLogger.print("JSON Exception for: " + movieTitle + "URL: " + matchedMovieRTURL);
            }

            if ((i + 1) % 250 == 0)
            {
                String allMoviesJson;
                try
                {
                    allMoviesJson = getJSONMovies(allMovieObjects);
                    allMovieObjects.clear();
                    FileManager.writeToFile("AllMoviesDetailed_" + ((i / 250) + 1) + ".data", allMoviesJson);
                }
                catch (InfluException ex)
                {
                    InfluLogger.print("Unable to save files");
                }
            }
        }


    }

    public static String matchMovieFromRT(String jsonResponse, String movieTitle, int movieYear)
            throws JSONException
    {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray jsonArray = jsonObject.getJSONArray("movies");
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject resultMovie = jsonArray.getJSONObject(i);
            int year = resultMovie.getInt("year");
            String title = resultMovie.getString("title");
            title = title.trim();

            if ((year == movieYear || movieYear == 0) && title.toLowerCase().contains(movieTitle.toLowerCase()))
            {
                JSONObject links = resultMovie.getJSONObject("links");
                return links.getString("self");
            }
        }
        return null;
    }

    public static void matchMovieAndGetDetailsFromTMDB(DetailedMovie detailedMovie) throws IOException, JSONException
    {
        String response;
        try
        {
            response = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_SEARCH + URLEncoder.encode(detailedMovie.imdbTitle, "UTF-8"));
        }
        catch (InfluException ex)
        {
            return;
        }
        JSONObject jsonObject = new JSONObject(response);
        JSONArray results = jsonObject.getJSONArray("results");
        for (int i = 0; i < results.length(); i++)
        {
            boolean isCurrentMatching = false;
            try
            {
                response = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_DETAIL + results.getJSONObject(i).getInt("id") + "?" + TMDB_KEY);
            }
            catch (InfluException ex)
            {
                continue;
            }
            jsonObject = new JSONObject(response);
            if (detailedMovie.imdbId != 0 && jsonObject.has("imdb_id") && jsonObject.get("imdb_id") != JSONObject.NULL)
            {
                if (jsonObject.getString("imdb_id").contains(Integer.toString(detailedMovie.imdbId)))
                {
                    //Now we have found a matching movie in TMDB
                    isCurrentMatching = true;
                }
            }
            else if (detailedMovie.year != 0 && jsonObject.has("release_date"))
            {
                if (jsonObject.getString("release_date").contains(Integer.toString(detailedMovie.year))
                        && (jsonObject.getString("title").toLowerCase().contains(detailedMovie.imdbTitle.toLowerCase())
                        || detailedMovie.imdbTitle.toLowerCase().contains(jsonObject.getString("title").toLowerCase())))
                {
                    isCurrentMatching = true;
                }
            }
            if (isCurrentMatching)
            {
                detailedMovie.tmdbId = jsonObject.getInt("id");

                if (jsonObject.has("release_date"))
                {
                    detailedMovie.releasedDate = jsonObject.getString("release_date");
                }

                if (jsonObject.has("revenue"))
                {
                    detailedMovie.revenue = jsonObject.getInt("revenue");
                }

                if (jsonObject.has("genres"))
                {
                    JSONArray tempArray = jsonObject.getJSONArray("genres");
                    ArrayList<String> tempArrayList = new ArrayList<>();
                    for (int j = 0; j < tempArray.length(); j++)
                    {
                        try
                        {
                            tempArrayList.add(tempArray.getJSONObject(j).getString("name"));
                        }
                        catch (JSONException exception)
                        {
                        }
                    }
                    detailedMovie.tmdbGenres = tempArrayList;
                }


                // get keywords from separete service
                String tempResponse;
                try
                {
                    tempResponse = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_DETAIL + detailedMovie.tmdbId + "/keywords?" + TMDB_KEY);
                    JSONObject tempObject = new JSONObject(tempResponse);
                    JSONArray tempArray = tempObject.getJSONArray("keywords");
                    ArrayList<String> tempArrayList = new ArrayList<>();
                    for (int j = 0; j < tempArray.length(); j++)
                    {
                        try
                        {
                            tempArrayList.add(tempArray.getJSONObject(j).getString("name"));
                        }
                        catch (JSONException exception)
                        {
                        }
                    }
                    detailedMovie.keywords = tempArrayList;
                }
                catch (InfluException ex)
                {
                }
                try
                {
                    // get trailers from separete service
                    tempResponse = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_DETAIL + detailedMovie.tmdbId + "/trailers?" + TMDB_KEY);
                    JSONObject tempObject = new JSONObject(tempResponse);
                    JSONArray tempArray = tempObject.getJSONArray("youtube");
                    ArrayList<JSONObject> tempArrayListJSON = new ArrayList<>();
                    for (int j = 0; j < tempArray.length(); j++)
                    {
                        JSONObject videoLink = new JSONObject();
                        videoLink.put(Constants.TRAILERTYPE, tempArray.getJSONObject(j).getString("name"));
                        videoLink.put(Constants.TRAILERID, tempArray.getJSONObject(j).getString("source"));
                        tempArrayListJSON.add(videoLink);
                    }
                    detailedMovie.trailers = tempArrayListJSON;
                }
                catch (InfluException ex)
                {
                }
                try
                {
                    // get similar movies from separete service
                    tempResponse = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_DETAIL + detailedMovie.tmdbId + "/similar_movies?" + TMDB_KEY);
                    JSONObject tempObject = new JSONObject(tempResponse);
                    JSONArray tempArray = tempObject.getJSONArray("results");
                    detailedMovie.tmdbSimilarMovies = new ArrayList<>();
                    for (int j = 0; j < tempArray.length(); j++)
                    {
                        try
                        {
                            detailedMovie.tmdbSimilarMovies.add(tempArray.getJSONObject(j).getString("uri"));
                        }
                        catch (JSONException exception)
                        {
                        }
                    }
                    // repeat for all pages of the result
                    int totalPages = tempObject.getInt("total_pages");
                    for (int currentPage = 2; currentPage <= totalPages; currentPage++)
                    {
                        tempResponse = HttpResponseGetter.getHttpResponse(TMDB_MOVIE_DETAIL + detailedMovie.tmdbId + "/similar_movies?page=" + currentPage + "&" + TMDB_KEY);
                        tempObject = new JSONObject(tempResponse);
                        tempArray = tempObject.getJSONArray("results");
                        for (int j = 0; j < tempArray.length(); j++)
                        {
                            try
                            {
                                detailedMovie.tmdbSimilarMovies.add(tempArray.getJSONObject(j).getString("uri"));
                            }
                            catch (JSONException exception)
                            {
                            }
                        }
                    }
                }
                catch (InfluException ex)
                {
                }

                break;
            }
        }
    }

    public static DetailedMovie getDetailedMovieFromRTByURL(String matchedMovieURL, String movieTitle, int movieYear, String movieUri) throws JSONException, InfluException
    {
        String response = HttpResponseGetter.getHttpResponse(matchedMovieURL);
        JSONObject jsonObject = new JSONObject(response);
        DetailedMovie movie = new DetailedMovie(0, movieTitle, movieYear, movieUri);

        movie.rtId = jsonObject.getInt("id");
        movie.rtTitle = jsonObject.getString("title");

        if (movieYear == 0 && jsonObject.has("year"))
        {
            movie.year = jsonObject.getInt("year");
        }

        if (jsonObject.has("alternate_ids") && jsonObject.getJSONObject("alternate_ids").has("imdb"))
        {
            movie.imdbId = jsonObject.getJSONObject("alternate_ids").getInt("imdb");
        }

        if (jsonObject.has("genres"))
        {
            ArrayList<String> genresList = new ArrayList<>();
            JSONArray genres = jsonObject.getJSONArray("genres");
            for (int i = 0; i < genres.length(); i++)
            {
                genresList.add(genres.getString(i));
            }
            movie.rtGenres = genresList;
        }

        if (jsonObject.has("critics_consensus"))
        {
            movie.criticsConsensus = jsonObject.getString("critics_consensus");
        }

        if (jsonObject.has("release_dates") && jsonObject.getJSONObject("release_dates").has("theater"))
        {
//            String date = jsonObject.getJSONObject("release_dates").getString("theater");
//            String dates[] = date.split("-");
//            movie.releasedDate = new GregorianCalendar(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]) - 1, Integer.parseInt(dates[2]));
            String date = jsonObject.getJSONObject("release_dates").getString("theater");
            movie.releasedDate = date;
        }

        movie.audienceRating = jsonObject.getJSONObject("ratings").getInt("audience_score");
        movie.criticRating = jsonObject.getJSONObject("ratings").getInt("critics_score");
        movie.posterURL = jsonObject.getJSONObject("posters").getString("detailed");

        JSONArray cast = jsonObject.getJSONArray("abridged_cast");
        ArrayList<Person> abridgedCast = new ArrayList<>();
        for (int i = 0; i < cast.length(); i++)
        {
            JSONObject actor = cast.getJSONObject(i);
            if (actor.has("id"))
            {
                abridgedCast.add(new Person(actor.getString("name"), actor.getInt("id")));
            }
            else
            {
                abridgedCast.add(new Person(actor.getString("name")));
            }

        }
        movie.cast = abridgedCast;

        JSONArray directors = jsonObject.getJSONArray("abridged_directors");
        ArrayList<Person> abridgedDirectors = new ArrayList<>();
        for (int i = 0; i < directors.length(); i++)
        {
            JSONObject director = directors.getJSONObject(i);
            if (director.has("id"))
            {
                abridgedDirectors.add(new Person(director.getString("name"), director.getInt("id")));
            }
            else
            {
                abridgedDirectors.add(new Person(director.getString("name")));
            }
        }
        movie.directors = abridgedDirectors;

        if (jsonObject.has("studio"))
        {
            movie.studio = jsonObject.getString("studio");
        }
        movie.httpURL = jsonObject.getJSONObject("links").getString("alternate");

        movie.rtSimilarMovies = getSimilarMovies(jsonObject.getJSONObject("links").getString("similar") + "?" + RT_KEY);

        return movie;


    }

    public static ArrayList<String> getSimilarMovies(String url) throws JSONException
    {
        try
        {
            String response = HttpResponseGetter.getHttpResponse(url);

            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("movies");
            ArrayList<String> similarMovies = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++)
            {
                JSONObject movie = jsonArray.getJSONObject(i);
                similarMovies.add(movie.getString("uri"));
            }

            return similarMovies;
        }
        catch (InfluException ex)
        {
            return new ArrayList<>();
        }
    }

    public static void waiting(int n)
    {

        long t0, t1;

        t0 = System.currentTimeMillis();

        do
        {
            t1 = System.currentTimeMillis();
        }
        while ((t1 - t0) < (n * 1000));
    }

    public static String getJSONMovies(ArrayList<DetailedMovie> top250Movies) throws InfluException
    {
        JSONArray jsonArray = new JSONArray();
        for (DetailedMovie movie : top250Movies)
        {
            JSONObject jsonObject = movie.toJSON();
            jsonArray.put(jsonObject);
        }
        try
        {
            return jsonArray.toString(2);
        }
        catch (JSONException ex)
        {
            throw new InfluException(ex.getMessage());
        }
    }
}
