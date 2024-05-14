function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating star info from resultData", resultData);

    // populate the star info h3
    // find the empty h3 body by id "star_info"
    let starInfoElement = jQuery("#star_info");
    // append two html <p> created to the h3 body, which will refresh the page

    //"<p><a href= "http://localhost:8080/cs122b_project1_api_example_war/\">Movie List</a ></p >" +
    starInfoElement.append(
        "<p>" + resultData[0]["title"] + " (" + resultData[0]["year"] + ")</p >");

    console.log("handleResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "movie_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");

    // Concatenate the html tags with resultData jsonObject to create table rows
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["director"] + "</th>";

        let genreRow = resultData[i]["genres"].split(",")
        let genreIdRow = resultData[i]["genres_id"].split(",")

        rowHTML += "<th>";
        for (let j= 0; j < genreIdRow.length; j++){
            console.log("check genreIdRow");
            console.log(genreIdRow[j]);
            rowHTML += '<a href="result-browse.html?genre=' + genreIdRow[j] + '">'
                + genreRow[j] +     // display star_name for the link text
                '</a >';

            if (j < genreRow.length-1){
                rowHTML += ', ';
            }
        }
        rowHTML += "</th>";

        let starRow = resultData[i]["stars"].split(",");
        let starIdRow = resultData[i]["starsId"].split(",");

        rowHTML += "<th>";
        for (let j= 0; j < starRow.length; j++){
            rowHTML += '<a href="single-star.html?id=' + starIdRow[j] + '">'
                + starRow[j] +     // display star_name for the link text
                '</a >';

            if (j < starRow.length-1){
                rowHTML += ', ';
            }
        }
        //Add a link to single-movie.html with id passed with GET url parameter
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += `<th><button style="padding: 8px 16px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px;" onclick="addToCart('${resultData[i]['movieId']}')">Add</button></th>`;
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */
function addToCart(movieId) {
    $.ajax({
        dataType:"json",
        method:"POST",
        url:"api/cart",
        data: {
            "movieId": movieId,
            "amount": "1"
        },
        success: window.alert("Movie added to your shopping cart.")
    });
}

// Get id from URL
let movieId = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/single-movie?id=" + movieId,
    success: (resultData) => handleResult(resultData),
});