/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend.entities;

import com.influir.libraries.json.JSONArray;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.InfluException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pranit
 */
public class DetailedMovie extends Movie implements Comparable<DetailedMovie>, Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2102258767597754817L;
	/*
     * 
     * RottenTomatoes
     */
    public int rtId;//id mapping
    public String rtTitle;
    public ArrayList<String> rtGenres;//duplicate
    public String criticsConsensus;
    public int audienceRating;
    public int criticRating;
    public String posterURL;
    public ArrayList<Person> cast;
    public ArrayList<Person> directors;
    public String studio;
    public String httpURL;
    public ArrayList<String> rtSimilarMovies;
    /*
     * 
     * TheMovieDB
     */
    public int tmdbId;
    public ArrayList<String> tmdbGenres;//duplicate
    //public Calendar releasedDate;
    public String releasedDate;
    public int revenue;
    public ArrayList<String> keywords;
    public ArrayList<JSONObject> trailers;
    public ArrayList<String> tmdbSimilarMovies;
    public InfluirScore influirScore;

    public DetailedMovie(int id, String name, int year, String uri)
    {
        super(id, name, year, uri);
    }

    public DetailedMovie()
    {
    }

    @Override
    public JSONObject toJSON() throws InfluException
    {
        try
        {
            JSONObject jsonObject = new JSONObject();

            jsonObject.put(Constants.IMDBID, imdbId);
            jsonObject.put(Constants.IMDBTITLE, imdbTitle);
            jsonObject.put(Constants.YEAR, year);
            jsonObject.put(Constants.RTID, rtId);
            jsonObject.put(Constants.RTTITLE, rtTitle);

            if (rtGenres != null && !rtGenres.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (String rtGenre : rtGenres)
                {
                    array.put(rtGenre);
                }
                jsonObject.put(Constants.RTGENRES, array);
            }
            if (criticsConsensus != null && !criticsConsensus.isEmpty())
            {
                jsonObject.put(Constants.CRITICSCONCENSUS, criticsConsensus);
            }
            if (audienceRating != 0)
            {
                jsonObject.put(Constants.AUDIENCERATING, audienceRating);
            }
            if (criticRating != 0)
            {
                jsonObject.put(Constants.CRITICSRATING, criticRating);
            }
            if (posterURL != null && !posterURL.isEmpty())
            {
                jsonObject.put(Constants.POSTERURL, posterURL);
            }
            if (cast != null && !cast.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (Person person : cast)
                {
                    JSONObject object = person.toJSON();
                    array.put(object);
                }
                jsonObject.put(Constants.CAST, array);
            }
            if (directors != null && !directors.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (Person director : directors)
                {
                    JSONObject object = director.toJSON();
                    array.put(object);
                }
                jsonObject.put(Constants.DIRECTORS, array);
            }
            if (studio != null && !studio.isEmpty())
            {
                jsonObject.put(Constants.STUDIO, studio);
            }
            if (httpURL != null && !httpURL.isEmpty())
            {
                jsonObject.put(Constants.HTTPURL, httpURL);
            }
            if (rtSimilarMovies != null && !rtSimilarMovies.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (String similarMovie : rtSimilarMovies)
                {
                    array.put(similarMovie);
                }
                jsonObject.put(Constants.RTSIMILARMOVIES, array);
            }

            if (tmdbId != 0)
            {
                jsonObject.put(Constants.TMDBID, tmdbId);
            }

            if (revenue != 0)
            {
                jsonObject.put(Constants.REVENUE, revenue);
            }

            if (releasedDate != null)
            {
                jsonObject.put(Constants.RELEASEDDATE, releasedDate);
//                JSONObject dateObject = new JSONObject();
//                dateObject.put(Constants.RELEASEDDAY, releasedDate.get(Calendar.DAY_OF_MONTH));
//                dateObject.put(Constants.RELEASEDMONTH, releasedDate.get(Calendar.MONTH));
//                dateObject.put(Constants.RELEASEDYEAR, releasedDate.get(Calendar.YEAR));
//                jsonObject.put(Constants.RELEASEDDATE, dateObject);
            }

            if (tmdbGenres != null && !tmdbGenres.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (String tmdbGenre : tmdbGenres)
                {
                    array.put(tmdbGenre);
                }
                jsonObject.put(Constants.TMDBGENRES, array);
            }

            if (keywords != null && !keywords.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (String keyword : keywords)
                {
                    array.put(keyword);
                }
                jsonObject.put(Constants.KEYWORDS, array);
            }

            if (trailers != null && !trailers.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (JSONObject trailer : trailers)
                {
                    array.put(trailer);
                }
                jsonObject.put(Constants.TRAILERS, array);
            }

            if (tmdbSimilarMovies != null && !tmdbSimilarMovies.isEmpty())
            {
                JSONArray array = new JSONArray();
                for (String tmdbSimilarMovie : tmdbSimilarMovies)
                {
                    array.put(tmdbSimilarMovie);
                }
                jsonObject.put(Constants.TMDBSIMILARMOVIES, array);
            }

            return jsonObject;
        }
        catch (JSONException ex)
        {
            Logger.getLogger(DetailedMovie.class.getName()).log(Level.SEVERE, null, ex);
            throw new InfluException(ex.getMessage());
        }
    }

    @Override
    public int compareTo(DetailedMovie o)
    {

        if (this.influirScore != null && o.influirScore != null)
        {
            float a = o.influirScore.movieScore - this.influirScore.movieScore;
            if (a < 0)
            {
                return -1;
            }
            else if (a > 0)
            {
                return 1;
            }
        }
        return 0;
    }
}
