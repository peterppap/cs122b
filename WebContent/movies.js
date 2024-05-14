function handleStarResult(resultData) {
    console.log("handleStarResult: populating movie table from resultData");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#movie_table_body");

    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML +=
            "<th>" +
            // Add a link to single-movie.html with id passed with GET url parameter
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        rowHTML += "<th>" + resultData[i]["genres"] + "</th>";

        let starRow = resultData[i]["stars"].split(",");
        let starIdRow = resultData[i]["starsId"].split(",");
        rowHTML += "<th>";
        for (let j = 0; j < 3; j++){
            rowHTML += '<a href="single-star.html?id="' + starIdRow[j] + '">'
                + starRow[j] +     // display star_name for the link text
                '</a>';
            if (j < 2){
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>";
        rowHTML += `<th><button style="padding: 8px 16px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px;" onclick="addToCart('${resultData[i]['movie_id']}')">Add</button></th>`;
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}


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

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/movies", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});