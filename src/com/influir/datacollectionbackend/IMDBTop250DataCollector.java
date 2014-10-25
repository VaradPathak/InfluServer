/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.datacollectionbackend.entities.Person;
import com.influir.libraries.http.HttpResponseGetter;
import com.influir.libraries.json.JSONArray;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.FileManager;
import com.influir.libraries.utils.InfluException;
import com.influir.libraries.utils.InfluLogger;

/**
 *
 * @author Pranit
 */
public class IMDBTop250DataCollector
{
    final private static String RT_KEY = "apikey=xj6tr26gjgnshjfbab7nggkr";
    final private static String TMDB_KEY = "api_key=64cbd56967e9c36613fa52869d9c847d";
    final private static String RT_MOVIE_SEARCH = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?" + RT_KEY + "&q=";
    final private static String TMDB_MOVIE_SEARCH = "http://api.themoviedb.org/3/search/movie?" + TMDB_KEY + "&query=";
    final private static String TMDB_MOVIE_DETAIL = "http://api.themoviedb.org/3/movie/";

    /**
     * @param args the command line arguments
     * @throws IOException
     * @throws JSONException
     */
    public static void main(String[] args) throws IOException, JSONException, InfluException
    {
        ArrayList<DetailedMovie> top250Movies = new ArrayList<>();
        String arrayString = FileManager.readFile("refined.json");
        JSONArray jsonArray = new JSONArray(arrayString);

        Calendar startTime = Calendar.getInstance();

        for (int i = 0; i < jsonArray.length(); i++)
        {
            if (i == 0)
            {
                InfluLogger.print("Now fetching movie: " + (i + 1));
            }
            else
            {
                Calendar now = Calendar.getInstance();
                float difference = (now.getTimeInMillis() - startTime.getTimeInMillis()) / 60000f;
                difference = (difference / i) * (250 - i);
                InfluLogger.print("Now fetching movie: " + (i + 1) + ".\tEstimated time remaining: " + (int) difference + " minutes.");
            }
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Movie movie = new Movie(jsonObject.getInt("imdbId"), jsonObject.getString("title"), jsonObject.getInt("year"), jsonObject.getString("uri"));

            String query = URLEncoder.encode(movie.imdbTitle, "UTF-8");
            String url = RT_MOVIE_SEARCH + query;//+ SEARCH_RESULT_LIMIT;
            String response;
            try
            {
                response = HttpResponseGetter.getHttpResponse(url);
            }
            catch (InfluException ex)
            {
                InfluLogger.print("Movie unsuccessful: " + movie.imdbTitle);
                continue;
            }
            String matchedMovieRTURL = "";
            try
            {
                matchedMovieRTURL = matchMovieFromRT(response, movie);
                if (matchedMovieRTURL != null)
                {
                    DetailedMovie detailedMovie;
                    try
                    {
                        detailedMovie = getDetailedMovieFromRTByURL(matchedMovieRTURL + "?" + RT_KEY, movie);
                    }
                    catch (InfluException ex)
                    {
                        continue;
                    }
                    matchMovieAndGetDetailsFromTMDB(detailedMovie);
                    InfluLogger.print("Movie Sucessfull: " + movie.imdbTitle);
                    top250Movies.add(detailedMovie);
                }
                else
                {
                    InfluLogger.print("URL null for: " + movie.imdbTitle);
                }
            }
            catch (JSONException e)
            {
                InfluLogger.print("JSON Exception for: " + movie.imdbTitle + "URL: " + matchedMovieRTURL);
            }
        }

        String top250MoviesJSON = getJSONMovies(top250Movies);
        FileManager.writeToFile("TOP250Movies.data", top250MoviesJSON);
    }

    public static String matchMovieFromRT(String jsonResponse, Movie movie)
            throws JSONException
    {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        JSONArray jsonArray = jsonObject.getJSONArray("movies");
        for (int i = 0; i < jsonArray.length(); i++)
        {
            JSONObject resultMovie = jsonArray.getJSONObject(i);
            int year = resultMovie.getInt("year");
            if (resultMovie.has("alternate_ids"))
            {
                int id = resultMovie.getJSONObject("alternate_ids").getInt("imdb");
                if (id == movie.imdbId)
                {
                    JSONObject links = resultMovie.getJSONObject("links");
                    return links.getString("self");
                }
            }
            else
            {
                String title = resultMovie.getString("title");
                title = title.trim();
                if ((year == movie.year) && title.contains(movie.imdbTitle))
                {
                    JSONObject links = resultMovie.getJSONObject("links");
                    return links.getString("self");
                }
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
//                    SimpleDateFormat dt = new SimpleDateFormat("yyyyy-mm-dd");
//                    try
//                    {
//                        dt.parse(jsonObject.getString("release_date"));
//                        detailedMovie.releasedDate = dt.getCalendar();
//                    }
//                    catch (ParseException ex)
//                    {
//                        Logger.getLogger(IMDBTop250DataCollector.class.getName()).log(Level.SEVERE, null, ex);
//                    }
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

    public static DetailedMovie getDetailedMovieFromRTByURL(String matchedMovieURL, Movie imdbMovie) throws JSONException, InfluException
    {
        String response = HttpResponseGetter.getHttpResponse(matchedMovieURL);
        JSONObject jsonObject = new JSONObject(response);
        DetailedMovie movie = new DetailedMovie(imdbMovie.imdbId, imdbMovie.imdbTitle, imdbMovie.year, imdbMovie.uri);

        movie.rtId = jsonObject.getInt("id");
        movie.rtTitle = jsonObject.getString("title");

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
            Logger.getLogger(IMDBTop250DataCollector.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException(ex.getMessage());
        }
    }
}
