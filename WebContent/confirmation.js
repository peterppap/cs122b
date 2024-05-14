function handleConfirmationResult(resultData) {
    console.log("handleConfirmResult");

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let confirmationTableBodyElement = jQuery("#confirmation_table_body");
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>" + resultData[i]["saleid"] + "</th>";


        let movieIdRow = resultData[i]["movie_id"].split(",");
        let movieRow = resultData[i]["movie_title"].split(",");

        rowHTML += "<th>";
        for (let j= 0; j < movieRow.length; j++){
            rowHTML += '<a href="single-movie.html?id=' + movieIdRow[j] + '">'
                + movieRow[j] +     // display star_name for the link text
                '</a >';

            if (j < movieRow.length-1){
                rowHTML += ', ';
            }
        }
        rowHTML += "</th>";
        rowHTML += "<th>" + resultData[i]["quantity"] + "</th>";
        rowHTML += "<th>" + resultData[i]["price"] + "</th>";
        rowHTML += "<th>" + resultData[i]["total"] + "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        confirmationTableBodyElement.append(rowHTML);
    }
}


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/confirmation", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleConfirmationResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});