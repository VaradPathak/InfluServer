/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.api;

import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.libraries.utils.InfluException;
import java.util.ArrayList;

/**
 *
 * @author Pranit
 */
public interface Influir
{
    Movie getMovieDetails(String movie) throws InfluException;
    
    DetailedMovie getDetailedMovieDetails(int imdbId, String uri) throws InfluException;

    ArrayList<Movie> getInfluencedByMovies(String movie) throws InfluException;

    ArrayList<Movie> getInfluencingMovies(String movie) throws InfluException;

    ArrayList<Movie> getTop250MovieMovies() throws InfluException;
    
    void calculateInfluences(DetailedMovie detailedMovie, ArrayList<DetailedMovie> detailedMovieInfByList, ArrayList<DetailedMovie> detailedMovieInfList) ;
    
    public enum QueryType {UNIQUE_PROPERTIES, GENRES, CAST, DIRECTORS, SIMILAR_MOVIES_RT, KEYWORDS, TOP250, MOVIE_ABSTRACTDATA, TRAILERS, INFLUENCEDBY_DIRECTOR, DIRECTOR_INFLUENCEDBY,  INFLU_DATA} 
}
