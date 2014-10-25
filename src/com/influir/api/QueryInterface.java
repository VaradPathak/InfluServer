package com.influir.api;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.utils.InfluException;

/**
 * 
 * @author Varad
 */
public class QueryInterface {
	QueryUtility queryUtility = new QueryUtility();

	public QueryInterface() throws InfluException {
	}

	DetailedMovie getDetailedMovieDetails(Movie movie) throws InfluException,
			JSONException {
		DetailedMovie detailedMovie = new DetailedMovie(movie.imdbId,
				movie.imdbTitle, movie.year, movie.uri);

		try {
			/* Get Unique Properties */
			queryUtility.RunQuery(detailedMovie, movie,
					Influir.QueryType.UNIQUE_PROPERTIES);
			/* Cast */
			queryUtility.RunQuery(detailedMovie, movie, Influir.QueryType.CAST);
			/* Directors */
			queryUtility.RunQuery(detailedMovie, movie,
					Influir.QueryType.DIRECTORS);
			/* Similar Movies */
			queryUtility.RunQuery(detailedMovie, movie,
					Influir.QueryType.SIMILAR_MOVIES_RT);
			/* Trailers */
			queryUtility.RunQuery(detailedMovie, movie,
					Influir.QueryType.TRAILERS);
		} catch (InfluException ex) {
			Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new InfluException("Unable to get details of movie"
					+ ex.getMessage());
		}

		return detailedMovie;
	}

	Movie getMovieDetails(Movie movie) throws InfluException, JSONException {

		try {
			/* Get Abstract Data */
			queryUtility.RunQuery(null, movie,
					Influir.QueryType.MOVIE_ABSTRACTDATA);

		} catch (InfluException ex) {
			Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE,
					null, ex);
			throw new InfluException("Unable to get details of movie"
					+ ex.getMessage());
		}

		return movie;
	}

	ArrayList<Movie> getInfluencedByMovies(Movie movie) {
		return null;
	}

	ArrayList<Movie> getInfluencingMovies(Movie movie) {
		return null;
	}

	ArrayList<Movie> getTop250MovieMovies() throws InfluException,
			JSONException {
		return queryUtility.RunTop250MovieQuery();
	}

	void calculateInfluences(DetailedMovie detailedMovie,
			ArrayList<DetailedMovie> detailedMovieInfByList,
			ArrayList<DetailedMovie> detailedMovieInfList) {
		try {
			queryUtility.calculateInfluences(detailedMovie,
					detailedMovieInfByList, detailedMovieInfList);
		} catch (InfluException ex) {
			Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE,
					null, ex);
		} catch (JSONException ex) {
			Logger.getLogger(QueryInterface.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

}
