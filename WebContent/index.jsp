<%@page import="java.util.HashMap"%>
<%@page import="com.influir.datacollectionbackend.entities.DetailedMovie"%>
<%@page import="java.util.ArrayList"%>
<%@page import="com.influir.datacollectionbackend.entities.Movie"%>
<%@page import="com.influir.api.InfluirImplementation"%>
<%@page import="com.influir.api.Influir"%>

<!DOCTYPE html>
<html>
	<head>
		<title>Influir</title>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link href="css/bootstrap.css" rel="stylesheet">
		<link href="css/css.css" rel="stylesheet">
	</head>


	<%

		ArrayList<Movie> movies;
		if (session.getAttribute("top250Movies") != null)
		{
			movies = (ArrayList<Movie>) session.getAttribute("top250Movies");
		}
		else
		{
			Influir influir = new InfluirImplementation();
			movies = influir.getTop250MovieMovies();
			session.setAttribute("top250Movies", movies);
		}
		int pageNumber = 1;
		if (request.getParameter("page") != null)
		{
			try
			{
				pageNumber = Integer.parseInt(request.getParameter("page"));
			}
			finally
			{
			}
		}

		int startIndex = (pageNumber - 1) * 25;
	%>

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

				<div class="span12">

					<div style="width: 150px;height: 30px;background: #fff;
						 border-right: #ccc solid thin;
						 font-size: 15px;font-family: calibri;
						 text-align: center;vertical-align: middle;line-height: 30px">
						IMDB Top 250 Movies</div>

					<div class="movie-thumb-container">

						<div class="pagination" style="text-align: center">
							<ul>
								<%
									if (pageNumber == 1){
								%>
									<li class="disabled"><a>Prev</a></li>
								<%  } else { %>
									<li><a href="./?page=<%=pageNumber - 1%>">Prev</a></li>
								<%  }
									for (int i = 1; i <= 10; i++)
									{
										if (i == pageNumber){
									%>
										<li class="active"><a><%=i%></a></li>
									<%  } else { %>
										<li><a href="./?page=<%=i%>"><%=i%></a></li>
									<%  }
									}
									
									if (pageNumber == 10){
								%>
									<li class="disabled"><a>Next</a></li>
								<%  } else { %>
									<li><a href="./?page=<%=pageNumber + 1%>">Next</a></li>
								<%  } %>
							</ul>
						</div>

						<%

							for (int i = startIndex; i < startIndex + 25 && i < movies.size(); i++)
							{
								String rating = (((DetailedMovie) movies.get(i)).audienceRating) + "%";
						%>
								<div class="movie-thumb" onclick='getMovieDetails("<%=movies.get(i).uri%>")'>
									<br/>
									<img src="<%=((DetailedMovie) movies.get(i)).posterURL%>"
										 class="img-rounded img-polaroid"
										 style="height: 150px;width: 100px" />
									<br/><br/>
									<span style="font-family: Calibri;font-weight: bold">
										<%=movies.get(i).imdbTitle%>
									</span>
									<br/>
									<div class="progress progress-danger">
										<div class="bar" style="width: <%=rating%>"></div>
									</div>              
								</div>

						<% } %>
					</div>

				</div>
			</div>
		</div>


		<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js"></script>  
		<script src="js/bootstrap.js"></script> 
		<script src="js/communication.js" ></script>
		<noscript></noscript>

		<script>
				var movies = [<%
	              	if(movies.size() > 0){
						for (int i = 0; i < movies.size() - 1; i++)
						{
							out.print("'" + movies.get(i).imdbTitle.replaceAll("'", "") + "',");
						}
						out.print("'" + movies.get(movies.size() - 1).imdbTitle + "'");
	              	}
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
				};
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