<%@page import="com.influir.libraries.json.JSONObject"%>
<%@page import="com.influir.datacollectionbackend.entities.Person"%>
<%@page import="com.influir.datacollectionbackend.entities.DetailedMovie"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.influir.datacollectionbackend.entities.Movie"%>
<%@page import="com.influir.api.InfluirImplementation"%>
<%@page import="com.influir.api.Influir"%>


<!--
To change this template, choose Tools | Templates
and open the template in the editor.
-->
<!DOCTYPE html>
<html>
	<head>
		<title>Influir</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link href="css/bootstrap.css" rel="stylesheet">
		<link href="css/css.css" rel="stylesheet">
	</head>

	<%ArrayList<Movie> movies = (ArrayList<Movie>) session.getAttribute("top250Movies");%>
	<body style="width: 100%;margin: auto;background-image: url('img/bg.png')">
		<div class="container-fluid">
			<div class="page-header large" style="background: #fff;padding:5px;vertical-align: middle">

				<div class="row-fluid">
					<div class="span8">
						<h1>
							<a href="/Influir"><img src="img/influir.png" style="height: 100px;"/></a>
						</h1>
					</div>

					<div class="span4" style="height: 120px;">
						<div class="input-append" style="padding-top: 40px;">
							<input id="searchBox" data-provide="typeahead" type="text" placeholder="Enter movie name" autocomplete="off">
							<button class="btn" type="button" onclick="getDetailsByName()">Search!</button>
						</div>            
					</div>
				</div>

			</div>

			<div class="row-fluid">
				<div class="span3">
					<div class="movie-detail-container">
						<%
							Influir influir = new InfluirImplementation();
							String imdbId = request.getParameter("imdbId");
							DetailedMovie movie = influir.getDetailedMovieDetails(0, imdbId);

							ArrayList<DetailedMovie> influencedByMovies = new ArrayList<DetailedMovie>();
							ArrayList<DetailedMovie> influencingMovies = new ArrayList<DetailedMovie>();
							
							influir.calculateInfluences(movie, influencedByMovies, influencingMovies);
						%>

						<a href="<%=movie.httpURL%>"><img src="<%=movie.posterURL%>" class="img-polaroid"></a>
						<br><br>
						<span style="font-family: Calibri;font-weight: bold;font-size: 20px;;margin-bottom: 5px;">
							<a href="<%=movie.httpURL%>" style="color: #333"><%=movie.rtTitle%>
						</span>
							<span style="font-family: Calibri;font-weight: bold;font-size:18px;">
								<%
									if (movie.year != 0)
									{
										out.print("[" + movie.year + "]");
									}
								%>
							</span>
						</a>
						<%
							if (movie.directors != null || !movie.directors.isEmpty())
							{
						%>
						<span style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<span style="width: 100px">Director</span>
							<%
								for (Person director : movie.directors)
								{
							%>
							<span class="label label-info" style="font-size: 13px;cursor: pointer"><%=director.name%></span>
							<%}%>
						</span>
						<%}%>

						<%
							if (movie.cast != null && !movie.cast.isEmpty())
							{
						%>
						<span style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<span>Cast</span>
							<%
								for (Person actor : movie.cast)
								{
							%>
							<span class="label label-info" style="font-size: 13px;cursor: pointer"><%=actor.name%></span>
							<%}%>
						</span>
						<%}%>

						<%
							if (movie.tmdbGenres != null && !movie.tmdbGenres.isEmpty())
							{
						%>
						<span style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<span>Genres</span>
							<%
								for (String genre : movie.tmdbGenres)
								{
							%>
							<span class="label label-info" style="font-size: 13px;cursor: pointer"><%=genre%></span>
							<%}%>
						</span>
						<%}%>

						<%if (movie.revenue != 0)
							{
						%>

						<span style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<span>Revenue</span>
							<span class="label label-info" style="font-size: 13px;cursor: pointer">$<%=movie.revenue%></span>
						</span>
						<%
							}
						%>

						<%String rating = movie.audienceRating + "%";%>
						<div style="font-family: Calibri;font-weight: bold;font-size: 12px">
							Audience Rating <span class="label label-info" style="font-size: 13px;cursor: pointer"><%=(float) (movie.audienceRating) / 10%></span>
							<div class="progress progress-danger" style="margin-bottom: 5px;">
								<div class="bar" style="width: <%=rating%>"></div>
							</div>   
						</div>
						<%if (movie.criticRating > 0)
							{
								rating = movie.criticRating + "%";%>
						<div style="font-family: Calibri;font-weight: bold;font-size: 12px">
							Critics Rating <span class="label label-info" style="font-size: 13px;cursor: pointer"><%=(float) movie.criticRating / 10%></span>
							<div class="progress progress-danger" style="margin-bottom: 5px;">
								<div class="bar" style="width: <%=rating%>"></div>
							</div>   
						</div>
						<%}%>
						
						<!--div style="font-family: Calibri;font-weight: bold;font-size: 12px">
							<br><div class='img-circle influgradient'>63%</div> 
							<br>Influmeter
						</div-->

						<%if (movie.criticsConsensus != null && !movie.criticsConsensus.trim().isEmpty())
							{
						%>

						<div style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<br>Critics Concensus
							<blockquote style="text-align: left;font-weight: normal;font-size: 12px;">movie.criticsConsensus.trim().
							</blockquote>
						</div>
						<%}%>

						<%
							if (movie.trailers != null && !movie.trailers.isEmpty())
							{
						%>
						<div style="font-family: Calibri;font-weight: bold;font-size: 12px;text-align: left">
							<br>Trailers

							<ul class="nav nav-pills">
								<%
									for (int i = 0; i < movie.trailers.size(); i++)
									{
										JSONObject trailor = movie.trailers.get(i);
										String tname = trailor.has("TRAILERTYPE") ? trailor.getString("TRAILERTYPE") : "Trailor " + (i + 1);
								%>
								<li class="active"><a href="#linkModal<%=i%>" role="button" data-toggle="modal"><%=tname%></a></li>
									<%}%>
							</ul>      

						</div>
						<%}%>

					</div>
				</div>

				<div class="span9">

					<div style="width: 150px;height: 30px;background: #fff;
						 border-right: #ccc solid thin;
						 font-size: 15px;font-family: calibri;
						 text-align: center;vertical-align: middle;line-height: 30px">
						Influencing Movies</div>

					<div class="movie-thumb-container">
						<%
							if (influencingMovies == null || influencingMovies.isEmpty())
							{
								out.print("No movies found!");
							}
							else
							{
								for (int i = 0; i < influencingMovies.size(); i++)
								{
									DetailedMovie influencingMovie = (DetailedMovie) influencingMovies.get(i);
									String rating1 = influencingMovie.audienceRating + "%";
						%>

						<div class="movie-thumb" id="movie1<%=i%>" rel="popover" onclick='getMovieDetails("<%=influencingMovie.uri%>")'>

							<br>
							<img src="<%=influencingMovie.posterURL%>"
								 class="img-rounded img-polaroid"
								 style="height: 150px;width: 100px" />
							<br><br>
							<span style="font-family: Calibri;font-weight: bold">
								<%=influencingMovie.imdbTitle%>
							</span>
							<br>
							<div class="progress progress-danger">
								<div class="bar" style="width: <%=rating1%>"></div>
							</div>              
						</div> 
						<%
								}
							}
						%>
					</div>

					<div style="width: 150px;height: 30px;background: #fff;
						 border-right: #ccc solid thin;
						 font-size: 15px;font-family: calibri;
						 text-align: center;vertical-align: middle;line-height: 30px">
						Influenced By Movies</div>

					<div class="movie-thumb-container">
						<%
							if (influencedByMovies == null || influencedByMovies.isEmpty())
							{
								out.print("No movies found!");
							}
							else
							{
								for (int i = 0; i < influencedByMovies.size(); i++)
								{
									DetailedMovie influencedByMovie = (DetailedMovie) influencedByMovies.get(i);
									String rating1 = influencedByMovie.audienceRating + "%";
						%>

						<div class="movie-thumb" id="movie2<%=i%>" rel="popover" onclick='getMovieDetails("<%=influencedByMovie.uri%>");'>

							<br>
							<img src="<%=influencedByMovie.posterURL%>"
								 class="img-rounded img-polaroid"
								 style="height: 150px;width: 100px" />
							<br><br>
							<span style="font-family: Calibri;font-weight: bold">
								<%=influencedByMovie.imdbTitle%>
							</span>
							<br>
							<div class="progress progress-danger">
								<div class="bar" style="width: <%=rating1%>"></div>
							</div>              
						</div> 
						<%
								}
							}
						%>
					</div>

				</div>
			</div>
		</div>

		<%
			if (movie.trailers != null && !movie.trailers.isEmpty())
			{

				for (int i = 0; i < movie.trailers.size(); i++)
				{
					JSONObject trailor = movie.trailers.get(i);
					String tname = trailor.has("TRAILERTYPE") ? trailor.getString("TRAILERTYPE") : "Trailor " + (i + 1);
		%>
		<div id="linkModal<%=i%>" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="linkModalLabel<%=i%>" aria-hidden="true"
			 style="height: 450px;width: 600px">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
				<h3 id="linkModalLabel<%=i%>" style="color: #444"><%=movie.imdbTitle%></h3>
			</div>
			<div class="modal-body">
				<iframe width="560" height="315" src="http://www.youtube.com/embed/<%=trailor.getString("TRAILERID")%>" frameborder="0"
						class="img-polaroid"></iframe>
			</div>
			<div class="modal-footer">
				<button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Close</button>
			</div>
		</div>
		<%}
			}%>


		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>  
		<script src="js/bootstrap.js"></script>  
		<script src="js/communication.js"></script>
		<script>
			$(function(){
			<%
				for (int i = 0; i < influencingMovies.size(); i++)
				{
					String influScore = influencingMovies.get(i).influirScore.movieScore + "%";
			%>
				$('#movie1<%=i%>').popover({
					trigger: 'hover',
					html: true,
					placement: 'left',
					title: "Influence Ratings",
					content: "<div style='text-align: center'><table><tr><td style='padding:20px'><div class='img-circle influgradient'><%=influScore%></div> <span style='font-family: Calibri;font-weight: bold;font-size: 12px'>Influmeter</span>\n</td><td style='padding-right:20px'><span class='label label-info' style='font-size: 13px; cursor: pointer'><%=((DetailedMovie) influencingMovies.get(i)).influirScore.influencingDirector%></span>\<br/><img src = 'img/uparrow.png' style='margin-bottom: 5px;'><br/>\
			<%
					if (movie.directors != null || !movie.directors.isEmpty())
					{
						for (int d = 0; d < movie.directors.size(); d++)
						{
			%>
						<span class='label label-info' style='font-size: 13px; cursor: pointer'><%=movie.directors.get(d).name%></span>\
			<%			}
					}
			%>
							<br/><br/>\
							</td></tr></table></div>"
										   });
			<%  }
				for (int j = 0; j < influencedByMovies.size(); j++)
				{
					String influScore2 = influencedByMovies.get(j).influirScore.movieScore + "%";
			%>

				   $("#movie2<%=j%>").popover({
					   trigger: 'hover',
					   html: true,
					   placement: 'left',
					   title: "Influence Ratings",
					   content: "<div style='text-align: center'>\
							<table><tr><td style='padding:20px'>\
							<div class='img-circle influgradient'><%=influScore2%></div> \
							<span style='font-family: Calibri;font-weight: bold;font-size: 12px'>Influmeter</span>\n\
							</td><td style='padding-right:20px'>\
							<span class='label label-info' style='font-size: 13px; cursor: pointer'><%=((DetailedMovie) influencedByMovies.get(j)).influirScore.influencedByDirector%></span>\
							<br/>\
							<img src = 'img/downarrow.png' style='margin-bottom: 5px;'>\
							<br/>\
			<%
					if (movie.directors != null || !movie.directors.isEmpty())
					{
						for (int d = 0; d < movie.directors.size(); d++){
			%>
							<span class='label label-info' style='font-size: 13px; cursor: pointer'><%=movie.directors.get(d).name%></span>\
			<%  		}
					}
			%>
							<br><br>\
							</td></tr></table></div>"
										});
			<%  } %>
			});
		</script>

		<script>
			var movies = [<%
				for (int i = 0; i < movies.size() - 1; i++)
				{
					out.print("'" + movies.get(i).imdbTitle.replaceAll("'", "") + "',");
				}
				out.print("'" + movies.get(movies.size() - 1).imdbTitle + "'");
			%>];
			$('#searchBox').typeahead({source: movies});
		</script> 

		<script>
			function getDetailsByName()
			{
				var name = document.getElementById("searchBox").value;
				switch (name)
				{
			<%
				for (int i = 0; i < movies.size(); i++)
				{

					out.print("case \"" + movies.get(i).imdbTitle.replaceAll("'", "") + "\":");
					out.print("getMovieDetails(\"" + movies.get(i).uri + "\");");
					out.print("break;");
				}
			%>
				}
			}
		</script>

		<script>
			$('#searchBox').on('keyup', function(e) {
				if (e.keyCode === 13) {
					getDetailsByName()();
				}
			});
		
		
		</script>
	</body>
</html>


