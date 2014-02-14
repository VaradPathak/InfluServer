/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.influir.api;

import com.influir.datacollectionbackend.entities.*;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.utils.InfluException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author UTAMBE
 */
public class InfluirImplementation implements Influir {
	private QueryInterface query;

	{
		try {
			query = new QueryInterface();
		} catch (InfluException ex) {
			Logger.getLogger(InfluirImplementation.class.getName()).log(
					Level.SEVERE, null, ex);
		}
	}

	@Override
	public DetailedMovie getDetailedMovieDetails(int imdbId, String uri)
			throws InfluException {
		try {

			Movie movie = new Movie(imdbId, null, 0, uri);
			DetailedMovie detailedMovie = query.getDetailedMovieDetails(movie);
			return detailedMovie;

		} catch (JSONException ex) {
			Logger.getLogger(InfluirImplementation.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new InfluException("Unable to get details of movie"
					+ ex.getMessage());
		}

	}

	@Override
	public Movie getMovieDetails(String movieJson) throws InfluException {
		try {
			Movie movie = new Movie(movieJson);
			Movie detailedMovie = query.getMovieDetails(movie);
			return detailedMovie;

		} catch (JSONException ex) {
			Logger.getLogger(InfluirImplementation.class.getName()).log(
					Level.SEVERE, null, ex);
			throw new InfluException("Unable to get details of movie"
					+ ex.getMessage());
		}

	}

	@Override
	public ArrayList<Movie> getInfluencedByMovies(String movieJson)
			throws InfluException {
		// try {
		// Movie movie = new Movie(movieJson);
		// ArrayList<Movie> movies = query.getInfluencedByMovies(movie);
		// return movies;
		// } catch (JSONException ex) {
		// Logger.getLogger(InfluirImplementation.class.getName()).log(Level.SEVERE,
		// null, ex);
		// throw new InfluException("Unable to get Influenced By movies");
		// }

		ArrayList<Movie> movies = new ArrayList<>();
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_99685_12924_GoodFellas_Goodfellas_769_http3A2F2Fwww_rottentomatoes_com2Fm2F1032176_goodfellas2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_338564_13489_Mou_gaan_dou_28Infernal_Affairs29_Infernal_Affairs_10775_http3A2F2Fwww_rottentomatoes_com2Fm2Finfernal_affairs2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_76759_11292_Star_Wars3A_Episode_IV___A_New_Hope_Star_Wars_11_http3A2F2Fwww_rottentomatoes_com2Fm2Fstar_wars2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_102926_16286_The_Silence_of_the_Lambs_The_Silence_of_the_Lambs_274_http3A2F2Fwww_rottentomatoes_com2Fm2Fsilence_of_the_lambs2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_75314_16625_Taxi_Driver_Taxi_Driver_103_http3A2F2Fwww_rottentomatoes_com2Fm2Ftaxi_driver2F>"));
		return movies;
	}

	@Override
	public ArrayList<Movie> getInfluencingMovies(String movieJson)
			throws InfluException {
		// try
		// {
		// Movie movie = new Movie(movieJson);
		// ArrayList<Movie> movies = query.getInfluencingMovies(movie);
		// return movies;
		// }
		// catch (JSONException ex)
		// {
		// Logger.getLogger(InfluirImplementation.class.getName()).log(Level.SEVERE,
		// null, ex);
		// throw new InfluException("Unable to get Influenced By movies");
		// }
		ArrayList<Movie> movies = new ArrayList<>();
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_99685_12924_GoodFellas_Goodfellas_769_http3A2F2Fwww_rottentomatoes_com2Fm2F1032176_goodfellas2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_41959_18250_The_Third_Man_The_Third_Man_1092_http3A2F2Fwww_rottentomatoes_com2Fm2Fthe_third_man2F>"));
		movies.add(getDetailedMovieDetails(
				0,
				"<http://influir.com/Movie_338013_12860_Eternal_Sunshine_Of_The_Spotless_Mind_Eternal_Sunshine_of_the_Spotless_Mind_38_http3A2F2Fwww_rottentomatoes_com2Fm2Feternal_sunshine_of_the_spotless_mind2F>"));
		return movies;
	}

	@Override
	public ArrayList<Movie> getTop250MovieMovies() throws InfluException {
		ArrayList<Movie> movies = null;
		try {
			movies = query.getTop250MovieMovies();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return movies;
	}

	@Override
	public void calculateInfluences(DetailedMovie detailedMovie,
			ArrayList<DetailedMovie> detailedMovieInfByList,
			ArrayList<DetailedMovie> detailedMovieInfList) {
		query.calculateInfluences(detailedMovie, detailedMovieInfByList,
				detailedMovieInfList);
		Collections.sort(detailedMovieInfByList);
		Collections.sort(detailedMovieInfList);
	}
}
