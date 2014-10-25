/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.datacollectionbackend.entities;

import com.influir.libraries.json.JSONException;
import com.influir.libraries.json.JSONObject;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.InfluException;

/**
 *
 * @author Pranit
 */
public class Movie
{
    public String imdbTitle;//throughout we will maintain rotten tomatoes id
    public int year;
    public int imdbId;
    public String uri;
    
    public Movie(int id, String name, int year, String uri)
    {
        this.imdbTitle = name;
        this.year = year;
        this.imdbId = id;
        this.uri = uri;
    }

    public Movie()
    {
    }

    public Movie(String jsonMovie) throws JSONException
    {
        JSONObject jsonObject = new JSONObject(jsonMovie);
        this.imdbId = jsonObject.getInt(Constants.IMDBID);
        this.year = jsonObject.getInt(Constants.YEAR);
        this.imdbTitle = jsonObject.getString(Constants.IMDBTITLE);
        this.uri = jsonObject.getString(Constants.URI);
    }

    public JSONObject toJSON() throws InfluException, JSONException
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.IMDBTITLE, imdbTitle);
        jsonObject.put(Constants.YEAR, year);
        jsonObject.put(Constants.IMDBID, imdbId);
        jsonObject.put(Constants.URI, uri);
        return jsonObject;
        // throw new InfluException("Incorrect usage of the mathod");
    }
}
