package com.influir.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.influir.api.Influir.QueryType;
import com.influir.datacollectionbackend.entities.DetailedMovie;
import com.influir.datacollectionbackend.entities.InfluirScore;
import com.influir.datacollectionbackend.entities.Movie;
import com.influir.datacollectionbackend.entities.Person;
import com.influir.libraries.json.JSONException;
import com.influir.libraries.utils.Constants;
import com.influir.libraries.utils.InfluException;

/**
 * 
 * @author UTAMBE
 */
public class QueryUtility {
	private final static String USER_AGENT = "Mozilla/5.0";
	@SuppressWarnings("unused")
	private Influir.QueryType DIRECTOR_INFLUENCEDBY;
	@SuppressWarnings("unused")
	private Influir.QueryType INFLU_DATA;

	private String GetQuery(String indentifier, Influir.QueryType operation) {
		String sparqlQuery = "";
		switch (operation) {
		/* Unique Properties */
		case UNIQUE_PROPERTIES:
			sparqlQuery = "SELECT ?releasedDate ?rtTitle ?audienceRating ?criticRating ?revenue ?studio ?posterURL ?httpURL ?rtid ?tmdbid ?imdbid "
					+ "(group_concat(distinct ?tmdbGenre ; separator = \";\") AS ?tmdbGenres) "
					+ "(group_concat(distinct ?keyword ; separator = \";\") AS ?keywords)"
					+ "WHERE " + "{ " + "OPTIONAL{"
					+ indentifier
					+ " movieontology:releasedate ?releasedDate}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:title ?rtTitle}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:imdbrating ?audienceRating}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:criticsrating ?criticRating}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " dbp:gross ?revenue}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:tmdbid ?tmdbid}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " dcterms:identifier ?imdbid}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:rtid ?rtid}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:isFromStudio ?moviestudio. "
					+ "?moviestudio foaf:title ?studio}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " ontology:creationYear ?year}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:hasPoster ?poster. "
					+ "?poster movieontology:url ?posterURL}. "
					+ "OPTIONAL{"
					+ indentifier
					+ " movieontology:url ?httpURL}. "
					+ "OPTIONAL{ "
					+ indentifier
					+ "	movieontology:belongsToGenre ?genre."
					+ "?genre foaf:name ?tmdbGenre}. "
					+ "OPTIONAL{ "
					+ indentifier
					+ " movieontology:keyword ?keyword}. "
					+ "} LIMIT 1";
			break;
		/* Cast */
		case CAST:
			sparqlQuery = "SELECT ?cast ?castId " + "WHERE " + "{ "
					+ indentifier + " movieontology:hasActor ?actor. "
					+ "?actor foaf:name ?cast. "
					+ "?actor dcterms:identifier ?castId " + "}";
			break;
		/* Directors */
		case DIRECTORS:
			sparqlQuery = "SELECT ?director ?directorId " + "WHERE " + "{ "
					+ indentifier
					+ " movieontology:hasDirector ?movieDirector. "
					+ "?movieDirector foaf:name ?director. "
					+ "?movieDirector dc11:identifier ?directorId " + "}";
			break;
		/* Similar Movies RT */
		case SIMILAR_MOVIES_RT:
			sparqlQuery = "SELECT ?similarMovie " + "WHERE " + "{ "
					+ indentifier
					+ " movieontology:hasSimilarMovie ?similarMovie."
					// + "?simMovie movieontology:rtid ?similarMovie "
					+ "}";
			break;
		/* Top 250 Movies */
		case TOP250:
			sparqlQuery = "SELECT ?title ?year ?rtId ?rating ?posterURL ?movie  "
					+ "WHERE "
					+ "{ "
					+ "?movie movieontology:imdbrating ?rating. "
					+ "?movie ontology:creationYear ?year. "
					+ "?movie movieontology:rttitle ?title. "
					+ "?movie movieontology:rtid ?rtId "
					+ "OPTIONAL{?movie movieontology:hasPoster ?poster. "
					+ "?poster movieontology:url ?posterURL}. "
					+ "} ORDER BY DESC(?rating) LIMIT 250";
			break;
		/* Abstract Data */
		case MOVIE_ABSTRACTDATA:
			sparqlQuery = "SELECT ?title ?year  " + "WHERE " + "{ "
					+ indentifier + " movieontology:rttitle ?title. "
					+ indentifier + " ontology:creationYear ?year. " + "}";
			break;
		/* Trailers */
		case TRAILERS:
			sparqlQuery = "SELECT ?trailerId ?trailer " + " WHERE " + " { "
					+ indentifier + " movieontology:hasTrailer ?mvtrailer. "
					+ " ?mvtrailer foaf:name ?trailer. "
					+ " ?mvtrailer dc11:identifier ?trailerId. " + "  } ";
			break;
		/* Influenced By Director */
		case INFLUENCEDBY_DIRECTOR:
			sparqlQuery = "SELECT ?director1 WHERE "
					+ "{?director foaf:name \""
					+ indentifier
					+ "\".?director2 ontology:influencedBy ?director. ?director2 foaf:name ?director1}";
			break;
		/* Director Influenced By */
		case DIRECTOR_INFLUENCEDBY:
			sparqlQuery = "SELECT ?director1 WHERE "
					+ "{?director foaf:name \""
					+ indentifier
					+ "\".?director ontology:influencedBy ?director2. ?director2 foaf:name ?director1}";
			break;
		/* Influence Related Data */
		case INFLU_DATA:
			sparqlQuery = "SELECT ?rtTitle ?audienceRating ?posterURL ?httpURL ?movie "
					+ "WHERE "
					+ "{ "
					+ "?director foaf:name \""
					+ indentifier
					+ "\". "
					+ "?movie movieontology:hasDirector ?director. "
					+ "OPTIONAL{  ?movie   movieontology:title ?rtTitle}. "
					+ "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}. "
					+ "OPTIONAL{  ?movie   movieontology:hasPoster ?poster. "
					+ "?poster movieontology:url ?posterURL}. "
					+ "OPTIONAL{  ?movie   movieontology:url ?httpURL} "
					+ "} "
					+ "LIMIT 1";
			break;

		}
		return sparqlQuery;
	}

	ArrayList<Movie> RunTop250MovieQuery() throws InfluException, JSONException {
		String sparqlQuery = GetQuery("", Influir.QueryType.TOP250);
		ArrayList<Movie> top250 = new ArrayList<>();

		JSONArray result = null;
		result = executeQuery(sparqlQuery);
		if (result == null)
			return null;
		for (int i = 0; i < result.size(); i++) {

			JSONObject row = (JSONObject) result.get(i);
			DetailedMovie movie = new DetailedMovie();
			for (Object bindingName : row.keySet()) {
				String key = bindingName.toString();
				switch (key) {
				case "title":
					movie.imdbTitle = row.get(key).toString();
					break;
				case "year":
					try {
						movie.year = Integer.parseInt(row.get(key).toString());
					} catch (NullPointerException ex) {
						movie.year = -1;
					} catch (NumberFormatException e) {
						movie.year = -1;
					}
					break;
				case "rtId":
					try {
						movie.rtId = Integer.parseInt(row.get(key).toString());
					} catch (NullPointerException ex) {
						movie.rtId = -1;
					} catch (NumberFormatException e) {
						movie.rtId = -1;
					}
					break;
				case "rating":
					try {
						movie.audienceRating = Integer.parseInt(row.get(key)
								.toString());
					} catch (Exception ex) {
						movie.audienceRating = -1;
					}
					break;
				case "posterURL":
					movie.posterURL = row.get(key).toString();
					break;
				case "movie":
					movie.uri = "<" + row.get(key).toString() + ">";
					break;
				}
			}
			top250.add(movie);
		}
		return top250;
	}

	@SuppressWarnings("unchecked")
	private static JSONArray executeQuery(String query) {
		System.out.println(query);
		String url = "http://dydra.com/vspathak/influirold/sparql?auth_token=SKN620Vduyv1bhyPgtCD&user_id=vspathak&query=";
		try {
			query = URLEncoder.encode(query, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// System.out.println(query);
		BufferedReader in = null;
		StringBuffer response = null;
		try {
			URL URLobj = new URL(url + query);
			HttpURLConnection con = (HttpURLConnection) URLobj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept", "application/json");

			int responseCode = con.getResponseCode();

			if (responseCode == 200) {
				in = new BufferedReader(new InputStreamReader(
						con.getInputStream()));

				String inputLine;
				response = new StringBuffer();

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		JSONArray result = null;
		if (response != null) {
			String responseString = response.toString();
			JSONParser parser = new JSONParser();
			result = new JSONArray();

			try {

				Object obj = parser.parse(responseString);
				org.json.simple.JSONObject jsonObject = (org.json.simple.JSONObject) obj;
				// long count = (Long) jsonObject.get("total");
				JSONArray rows = (JSONArray) jsonObject.get("rows");
				JSONArray columns = (JSONArray) jsonObject.get("columns");
				for (int i = 0; i < rows.size(); i++) {
					JSONArray row = (JSONArray) rows.get(i);
					JSONObject resultObj = new JSONObject();
					for (int j = 0; j < row.size(); j++) {
						if (((JSONObject) row.get(j)).get("value") != null) {
							resultObj.put(columns.get(j).toString(),
									((JSONObject) row.get(j)).get("value"));
						}
					}
					result.add(resultObj);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	void RunQuery(DetailedMovie detailedMovie, Movie movie,
			Influir.QueryType operation) throws InfluException, JSONException {
		String sparqlQuery;
		sparqlQuery = GetQuery(movie.uri, operation);

		JSONArray result = executeQuery(sparqlQuery);
		if (result == null)
			return;

		String name = "";
		Integer id = 0;
		String strId = "";
		switch (operation) {
		case CAST:
			detailedMovie.cast = new ArrayList<>();
			break;
		case DIRECTORS:
			detailedMovie.directors = new ArrayList<>();
			break;
		case SIMILAR_MOVIES_RT:
			detailedMovie.rtSimilarMovies = new ArrayList<>();
			break;
		case TRAILERS:
			detailedMovie.trailers = new ArrayList<>();
			break;
		default:
			break;
		}
		for (int i = 0; i < result.size(); i++) {

			JSONObject row = (JSONObject) result.get(i);
			for (Object bindingName : row.keySet()) {
				String key = bindingName.toString();
				switch (operation) {
				/* Abstract Data */
				case MOVIE_ABSTRACTDATA: {
					switch (key) {
					case "title":
						movie.imdbTitle = row.get(key).toString();
						break;
					case "year":
						try {
							movie.year = Integer.parseInt(row.get(key)
									.toString());
						} catch (Exception ex) {
							movie.year = -1;
						}
						break;
					case "rtId":
						try {
							movie.imdbId = Integer.parseInt(row.get(key)
									.toString());
						} catch (Exception ex) {
							movie.imdbId = -1;
						}
						break;
					}
				}
					break;
				/* Unique Properties */
				case UNIQUE_PROPERTIES: {
					switch (key) {
					case "rtTitle":
						detailedMovie.rtTitle = row.get(key).toString();
						detailedMovie.imdbTitle = row.get(key).toString();
						break;
					case "releasedDate":
						detailedMovie.releasedDate = row.get(key).toString();
						break;
					case "audienceRating":
						try {
							detailedMovie.audienceRating = Integer.parseInt(row
									.get(key).toString());
						} catch (Exception ex) {
							detailedMovie.audienceRating = -1;
						}
						break;
					case "criticRating":
						try {
							detailedMovie.criticRating = Integer.parseInt(row
									.get(key).toString());
						} catch (Exception ex) {
							detailedMovie.criticRating = -1;
						}
						break;
					case "year":
						try {
							detailedMovie.year = Integer.parseInt(row.get(key)
									.toString());
						} catch (Exception ex) {
							detailedMovie.year = -1;
						}
						break;
					case "revenue":
						try {
							detailedMovie.revenue = Integer.parseInt(row.get(
									key).toString());
						} catch (Exception ex) {
							detailedMovie.revenue = -1;
						}
						break;
					case "studio":
						detailedMovie.studio = row.get(key).toString();
						break;
					case "criticsConsensus":
						detailedMovie.criticsConsensus = row.get(key)
								.toString();
						break;
					case "httpURL":
						detailedMovie.httpURL = row.get(key).toString();
						break;
					case "posterURL":
						detailedMovie.posterURL = row.get(key).toString();
						break;
					case "imdbId":
						try {
							detailedMovie.imdbId = Integer.parseInt(row
									.get(key).toString());
						} catch (Exception ex) {
							detailedMovie.imdbId = -1;
						}
						break;
					case "rtId":
						try {
							detailedMovie.rtId = Integer.parseInt(row.get(key)
									.toString());
						} catch (Exception ex) {
							detailedMovie.rtId = -1;
						}
						break;
					case "tmdbId":
						try {
							detailedMovie.tmdbId = Integer.parseInt(row
									.get(key).toString());
						} catch (Exception ex) {
							detailedMovie.tmdbId = -1;
						}
						break;
					case "tmdbGenres":
						String[] genres = row.get(key).toString().split(";");
						if (detailedMovie.tmdbGenres == null)
							detailedMovie.tmdbGenres = new ArrayList<>();
						for (String genre : genres) {
							detailedMovie.tmdbGenres.add(genre);
						}
						break;
					case "keyword":
						String[] keywords = row.get(key).toString().split(";");
						if (detailedMovie.keywords == null)
							detailedMovie.keywords = new ArrayList<>();
						for (String keyword : keywords) {
							detailedMovie.keywords.add(keyword);
						}
						break;
					}
					break;
				}
				/* Cast */
				case CAST: {
					switch (key) {
					case "cast":
						name = row.get(key).toString();
						break;
					case "castId":
						try {
							id = Integer.parseInt(row.get(key).toString());
						} catch (Exception ex) {
							id = -1;
						}
						break;
					}
					break;
				}
				/* Directors */
				case DIRECTORS: {
					switch (key) {
					case "director":
						name = row.get(key).toString();
						break;
					case "directorId":
						try {
							id = Integer.parseInt(row.get(key).toString());
						} catch (Exception ex) {
							id = -1;
						}
						break;
					}
					break;
				}
				/* Similar Movies RT */
				case SIMILAR_MOVIES_RT: {
					switch (key) {
					case "similarMovie":
						detailedMovie.rtSimilarMovies.add(row.get(key)
								.toString());
						break;
					}
					break;
				}
				/* TRAILERS */
				case TRAILERS: {
					switch (key) {
					case "trailer":
						name = row.get(key).toString();
						break;
					case "trailerId":
						strId = row.get(key).toString();
						break;
					}
				}
					break;
				/* INF MOVIE DATA */
				case INFLU_DATA: {
					switch (key) {
					case "rtTitle":
						detailedMovie.rtTitle = row.get(key).toString();
						detailedMovie.imdbTitle = row.get(key).toString();
						break;
					case "audienceRating":
						try {
							detailedMovie.audienceRating = Integer.parseInt(row
									.get(key).toString());
						} catch (Exception ex) {
							detailedMovie.audienceRating = -1;
						}
						break;
					case "httpURL":
						detailedMovie.httpURL = row.get(key).toString();
						break;
					case "posterURL":
						detailedMovie.posterURL = row.get(key).toString();
						break;
					case "year":
						try {
							detailedMovie.year = Integer.parseInt(row.get(key)
									.toString());
						} catch (Exception ex) {
							detailedMovie.year = -1;
						}
						break;
					case "movie":
						detailedMovie.uri = "<" + row.get(key).toString() + ">";
						break;

					}
				}
					break;
				default:
					break;
				}

			}
			switch (operation) {
			case CAST:
				detailedMovie.cast.add(new Person(name, id));
				break;
			case DIRECTORS:
				detailedMovie.directors.add(new Person(name, id));
				break;
			case TRAILERS:
				com.influir.libraries.json.JSONObject trailer = new com.influir.libraries.json.JSONObject();
				trailer.put(Constants.TRAILERTYPE, name);
				trailer.put(Constants.TRAILERID, strId);
				detailedMovie.trailers.add(trailer);
				break;
			default:
				break;
			}
		}

	}

	public void calculateInfluences(DetailedMovie detailedMovie,
			ArrayList<DetailedMovie> detailedMovieInfByList,
			ArrayList<DetailedMovie> detailedMovieInfList)
			throws InfluException, JSONException {
		// Get All Directors Inf and Inf by
		String sparqlQuery = "";
		int infMaxA = 0;
		Influir.QueryType operation = QueryType.DIRECTOR_INFLUENCEDBY;
		ArrayList<String> director_InfluencedBy = new ArrayList<>();
		HashMap<String, String> dirMap = new HashMap<>();
		for (int i = 0; i < detailedMovie.directors.size(); i++) {
			sparqlQuery = GetQuery(detailedMovie.directors.get(i).name,
					operation);
			getInfDirectors(operation, sparqlQuery, director_InfluencedBy,
					dirMap, detailedMovie.directors.get(i).name);
		}
		Influir.QueryType operation2 = QueryType.INFLUENCEDBY_DIRECTOR;
		ArrayList<String> influencedBy_Director = new ArrayList<>();
		for (int i = 0; i < detailedMovie.directors.size(); i++) {
			sparqlQuery = GetQuery(detailedMovie.directors.get(i).name,
					operation2);
			getInfDirectors(operation2, sparqlQuery, influencedBy_Director,
					dirMap, detailedMovie.directors.get(i).name);
		}

		String filter = "";
		JSONArray result;
		if (director_InfluencedBy.size() > 0) {
			for (int i = 0; i < director_InfluencedBy.size(); i++) {
				if (i != 0) {
					filter = filter + "|| regex(?dir, \"^"
							+ director_InfluencedBy.get(i) + "\")";
				} else {
					filter = filter + " regex(?dir, \"^"
							+ director_InfluencedBy.get(i) + "\")";
				}
			}

			sparqlQuery = "SELECT DISTINCT ?movie ?rtTitle ?audienceRating ?posterURL ?httpURL ?criticRating ?year ?dir "
					+ "(group_concat(distinct ?tmdbGenre ; separator = \";\") AS ?tmdbGenres) "
					+ "(group_concat(distinct ?keyword ; separator = \";\") AS ?keywords) "
					+ "WHERE "
					+ "{ "
					+ "?director foaf:name ?dir. "
					+ "?movie movieontology:hasDirector ?director. "
					+ "OPTIONAL{  ?movie   movieontology:title ?rtTitle}. "
					+ "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}. "
					+ "OPTIONAL{  ?movie   movieontology:criticsrating ?criticRating}. "
					+ "OPTIONAL{  ?movie   movieontology:hasPoster ?poster. "
					+ "?poster movieontology:url ?posterURL}. "
					+ "OPTIONAL{  ?movie   ontology:creationYear ?year}. "
					+ "OPTIONAL{  ?movie   movieontology:url ?httpURL}. "
					+ "OPTIONAL{  ?movie	movieontology:belongsToGenre ?genre."
					+ "?genre foaf:name ?tmdbGenre}. "
					+ "OPTIONAL{  ?movie	movieontology:keyword ?keyword}. "
					+ "FILTER(" + filter + ") " + "}";

			result = executeQuery(sparqlQuery);
			// detailedMovieInfByList = new ArrayList<>();

			for (int i = 0; i < result.size(); i++) {
				JSONObject row = (JSONObject) result.get(i);
				DetailedMovie detailedMovie1 = new DetailedMovie();
				detailedMovie1.influirScore = new InfluirScore();
				for (Object key : row.keySet()) {
					String bindingName = key.toString();
					switch (bindingName) {
					case "rtTitle":
						detailedMovie1.rtTitle = row.get(bindingName)
								.toString();
						detailedMovie1.imdbTitle = row.get(bindingName)
								.toString();
						break;
					case "audienceRating":
						try {
							detailedMovie1.audienceRating = Integer
									.parseInt(row.get(bindingName).toString());
							detailedMovie1.influirScore.audienceScore = detailedMovie1.audienceRating;
						} catch (Exception ex) {
							detailedMovie1.audienceRating = -1;
						}
						break;
					case "httpURL":
						detailedMovie1.httpURL = row.get(bindingName)
								.toString();
						break;
					case "dir":
						if (detailedMovie1.influirScore == null) {
							detailedMovie1.influirScore = new InfluirScore();
						}
						detailedMovie1.influirScore.influScore = 0;
						detailedMovie1.influirScore.influencedByDirector = row
								.get(bindingName).toString();
						break;
					case "posterURL":
						detailedMovie1.posterURL = row.get(bindingName)
								.toString();
						break;
					case "year":
						try {
							detailedMovie1.year = Integer.parseInt(row.get(
									bindingName).toString());
						} catch (Exception ex) {
							detailedMovie1.year = -1;
						}
						break;
					case "movie":
						detailedMovie1.uri = "<"
								+ row.get(bindingName).toString() + ">";
						break;
					case "criticRating":
						try {
							detailedMovie1.criticRating = Integer.parseInt(row
									.get(bindingName).toString());
							detailedMovie1.influirScore.criticScore = detailedMovie1.criticRating;
						} catch (Exception ex) {
							detailedMovie1.criticRating = -1;
						}
						break;
					case "tmdbGenres":
						String[] genres = row.get(key).toString().split(";");
						if (detailedMovie1.tmdbGenres == null)
							detailedMovie1.tmdbGenres = new ArrayList<>();
						for (String genre : genres) {
							detailedMovie1.tmdbGenres.add(genre);
						}
						break;
					case "keywords":
						String[] keywords = row.get(key).toString().split(";");
						if (detailedMovie1.keywords == null)
							detailedMovie1.keywords = new ArrayList<>();
						for (String keyword : keywords) {
							detailedMovie1.keywords.add(keyword);
						}
						break;

					}
				}

				if (detailedMovie1.imdbTitle != null
						&& !detailedMovie1.imdbTitle
								.equals("Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb")) {
					detailedMovieInfByList.add(detailedMovie1);
				}
			}

			for (DetailedMovie dm : detailedMovieInfByList) {
				dm.influirScore.genreScore = 0;
				if (detailedMovie.rtGenres != null) {
					for (String genre : dm.rtGenres) {
						if (detailedMovie.rtGenres != null
								&& detailedMovie.rtGenres.contains(genre)) {
							dm.influirScore.genreScore++;
						}
					}
				}

				dm.influirScore.keywordScore = 0;
				if (detailedMovie.keywords != null) {
					for (String keyword : dm.keywords) {
						if (detailedMovie.keywords != null
								&& detailedMovie.keywords.contains(keyword)) {
							dm.influirScore.keywordScore++;
						}
					}
				}
				if (dm.influirScore != null) {
					int gSize = 0, kSize = 0;
					if (detailedMovie.rtGenres != null) {
						gSize = detailedMovie.rtGenres.isEmpty() ? 1
								: detailedMovie.rtGenres.size();
					} else {
						gSize = 1;
					}
					if (dm.influirScore != null) {
						if (detailedMovie.keywords != null) {
							kSize = detailedMovie.keywords.isEmpty() ? 1
									: detailedMovie.keywords.size();
						} else {
							kSize = 1;
						}
					}
					dm.influirScore.movieScore = (float) ((0.4) * dm.influirScore.influScore / (infMaxA == 0 ? 1
							: infMaxA))
							+ (float) ((0.15) * (dm.influirScore.genreScore * 100 / gSize))
							+ (float) ((0.05) * (dm.influirScore.keywordScore * 100 / kSize))
							+ (float) ((0.2) * dm.influirScore.criticScore)
							+ (float) ((0.2) * dm.influirScore.audienceScore);
					int score = (int) (dm.influirScore.movieScore * 100);
					dm.influirScore.movieScore = ((float) score) / 100;
				}
			}

		}
		// Get Movie Details for those directors

		if (influencedBy_Director.size() > 0) {
			filter = "";
			for (int i = 0; i < influencedBy_Director.size(); i++) {
				if (i != 0) {
					filter = filter + "|| regex(?dir, \"^"
							+ influencedBy_Director.get(i) + "\")";
				} else {
					filter = filter + " regex(?dir, \"^"
							+ influencedBy_Director.get(i) + "\")";
				}
			}
			sparqlQuery = "SELECT ?rtTitle ?audienceRating ?posterURL ?httpURL ?movie ?criticRating ?year ?dir "
					+ "(group_concat(distinct ?tmdbGenre ; separator = \";\") AS ?tmdbGenres) "
					+ "(group_concat(distinct ?keyword ; separator = \";\") AS ?keywords)"
					+ "WHERE "
					+ "{ "
					+ "?director foaf:name ?dir. "
					+ "?movie movieontology:hasDirector ?director. "
					+ "OPTIONAL{  ?movie   movieontology:title ?rtTitle}. "
					+ "OPTIONAL{  ?movie   movieontology:imdbrating ?audienceRating}. "
					+ "OPTIONAL{  ?movie   movieontology:criticsrating ?criticRating}."
					+ "OPTIONAL{  ?movie   movieontology:hasPoster ?poster. "
					+ "?poster movieontology:url ?posterURL}. "
					+ "OPTIONAL{  ?movie   ontology:creationYear ?year}. "
					+ "OPTIONAL{  ?movie   movieontology:url ?httpURL}"
					+ "OPTIONAL{  ?movie	movieontology:belongsToGenre ?genre."
					+ "?genre foaf:name ?tmdbGenre}"
					+ "OPTIONAL{  ?movie	movieontology:keyword ?keyword}"
					+ ".FILTER(" + filter + ") " + "}";

			result = executeQuery(sparqlQuery);
			// detailedMovieInfByList = new ArrayList<>();

			for (int i = 0; i < result.size(); i++) {

				JSONObject row = (JSONObject) result.get(i);
				DetailedMovie detailedMovie1 = new DetailedMovie();
				detailedMovie1.influirScore = new InfluirScore();
				for (Object key : row.keySet()) {
					String bindingName = key.toString();
					switch (bindingName) {
					case "rtTitle":
						detailedMovie1.rtTitle = row.get(bindingName)
								.toString();
						detailedMovie1.imdbTitle = row.get(bindingName)
								.toString();
						break;
					case "audienceRating":
						try {
							detailedMovie1.audienceRating = Integer
									.parseInt(row.get(bindingName).toString());
							detailedMovie1.influirScore.audienceScore = detailedMovie1.audienceRating;
						} catch (NullPointerException ex) {
							detailedMovie1.audienceRating = -1;
						} catch (NumberFormatException e) {
							detailedMovie1.audienceRating = -1;
						}
						break;
					case "httpURL":
						detailedMovie1.httpURL = row.get(bindingName)
								.toString();
						break;
					case "dir":
					/*
					 * if (Inf_Lvl2b.containsKey((row.get(bindingName)
					 * .toString()))) { detailedMovie1.influirScore.influScore =
					 * Inf_Lvl2b .get(row.get(bindingName).toString()).score;
					 * detailedMovie1.influirScore.movieDirector = dirMap
					 * .get((row.get(bindingName).toString())); } else
					 */{
						if (detailedMovie1.influirScore == null) {
							detailedMovie1.influirScore = new InfluirScore();
						}
						detailedMovie1.influirScore.influScore = 0;
					}
						detailedMovie1.influirScore.influencingDirector = row
								.get(bindingName).toString();
						break;
					case "posterURL":
						detailedMovie1.posterURL = row.get(bindingName)
								.toString();
						break;
					case "year":
						try {
							detailedMovie1.year = Integer.parseInt(row.get(
									bindingName).toString());
						} catch (NullPointerException ex) {
							detailedMovie1.year = -1;
						} catch (NumberFormatException ex) {
							detailedMovie1.year = -1;
						}
						break;
					case "movie":
						detailedMovie1.uri = "<"
								+ row.get(bindingName).toString() + ">";
						break;
					case "criticRating":
						try {
							detailedMovie1.criticRating = Integer.parseInt(row
									.get(bindingName).toString());
							detailedMovie1.influirScore.criticScore = detailedMovie1.criticRating;
						} catch (NullPointerException ex) {
							detailedMovie1.criticRating = -1;
						} catch (NumberFormatException e) {
							detailedMovie1.criticRating = -1;
						}
						break;
					case "tmdbGenres":
						String[] genres = row.get(key).toString().split(";");
						if (detailedMovie1.tmdbGenres == null)
							detailedMovie1.tmdbGenres = new ArrayList<>();
						for (String genre : genres) {
							detailedMovie1.tmdbGenres.add(genre);
						}
						break;
					case "keywords":
						String[] keywords = row.get(key).toString().split(";");
						if (detailedMovie1.keywords == null)
							detailedMovie1.keywords = new ArrayList<>();
						for (String keyword : keywords) {
							detailedMovie1.keywords.add(keyword);
						}
						break;

					}

				}
				// /* Genres */
				// Movie movie = new Movie(detailedMovie1.imdbId,
				// detailedMovie1.imdbTitle, detailedMovie1.year,
				// detailedMovie1.uri);
				// RunQuery(detailedMovie1, movie, Influir.QueryType.GENRES);
				// /* Keywords */
				// RunQuery(detailedMovie1, movie, Influir.QueryType.KEYWORDS);
				if (detailedMovie1.imdbTitle != null
						&& !detailedMovie1.imdbTitle
								.equals("Dr. Strangelove or: How I Learned to Stop Worrying and Love the Bomb")) {
					detailedMovieInfList.add(detailedMovie1);
				}
			}
			for (DetailedMovie dm : detailedMovieInfList) {
				dm.influirScore.genreScore = 0;
				if (detailedMovie.rtGenres != null) {
					for (String genre : dm.rtGenres) {
						if (detailedMovie.rtGenres != null
								&& detailedMovie.rtGenres.contains(genre)) {
							dm.influirScore.genreScore++;
						}
					}
				}

				dm.influirScore.keywordScore = 0;
				if (detailedMovie.keywords != null) {
					for (String genre : dm.keywords) {
						if (detailedMovie.keywords != null
								&& detailedMovie.keywords.contains(genre)) {
							dm.influirScore.keywordScore++;
						}
					}
				}
				if (dm.influirScore != null) {
					int gSize = 0, kSize = 0;
					if (detailedMovie.rtGenres != null) {
						gSize = detailedMovie.rtGenres.isEmpty() ? 1
								: detailedMovie.rtGenres.size();
					} else {
						gSize = 1;
					}
					if (dm.influirScore != null) {
						if (detailedMovie.keywords != null) {
							kSize = detailedMovie.keywords.isEmpty() ? 1
									: detailedMovie.keywords.size();
						} else {
							kSize = 1;
						}
					}
					dm.influirScore.movieScore = (float) ((0.4) * dm.influirScore.influScore / (infMaxA == 0 ? 1
							: infMaxA))
							+ (float) ((0.15) * (dm.influirScore.genreScore * 100 / gSize))
							+ (float) ((0.05) * (dm.influirScore.keywordScore * 100 / kSize))
							+ (float) ((0.2) * dm.influirScore.criticScore)
							+ (float) ((0.2) * dm.influirScore.audienceScore);
					int score = (int) (dm.influirScore.movieScore * 100);
					dm.influirScore.movieScore = ((float) score) / 100;
				}
			}

		}
		// Collections.sort(Inf_Lvl2, new MyIntComparable());

		// Compute statistics

		// Compare Genre

	}

	private void getInfDirectors(Influir.QueryType operation,
			String sparqlQuery, ArrayList<String> directors,
			HashMap<String, String> dirMap, String director)
			throws JSONException {
		JSONArray result = executeQuery(sparqlQuery);

		for (int i = 0; i < result.size(); i++) {
			JSONObject row = (JSONObject) result.get(i);
			DetailedMovie detailedMovie1 = new DetailedMovie();
			detailedMovie1.influirScore = new InfluirScore();
			for (Object key : row.keySet()) {
				String bindingName = key.toString();
				if ("director1".equals(bindingName)) {
					directors.add(row.get(bindingName).toString());
					if (dirMap != null) {
						dirMap.put(row.get(bindingName).toString(), director);
					}
				}
			}
		}
	}

	class Influence {
		public HashMap<String, ArrayList<String>> director_InfluencedBy_Lvl2;
		public HashMap<String, ArrayList<String>> influencedBy_Director_Lvl2;
		public int score;

		/*
		 * @Override public int compareTo(Influence director) {
		 * 
		 * int compareQuantity = ((Influence) director).score;
		 * 
		 * return compareQuantity - this.score;
		 * 
		 * }
		 */
	}

	public class MyIntComparable implements Comparator<Influence> {
		@Override
		public int compare(Influence ob1, Influence ob2) {
			int o1 = ob1.score, o2 = ob2.score;
			return (o1 > o2 ? -1 : (o1 == o2 ? 0 : 1));
		}
	}
}
