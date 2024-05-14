let tuning = $("#tuning");
let resultTableBody = jQuery("#result_table_body");

var state = {
    'page':1,
    'rows':10,
    'window':5,
    'total':0
}

function updateRows() {
    var selectElement = document.getElementById('rows-select');
    state.rows = parseInt(selectElement.value, 10)
}

function pagination(querySet, page, rows) {
    var trimStart = (page - 1) * rows
    var trimEnd = trimStart + rows
    var trimmedData = querySet.slice(trimStart, trimEnd)
    var pages = Math.ceil(querySet.length/rows)
    return {
        'querySet':trimmedData,
        'pages':pages
    }
}

function pageButtons(pages, resultData) {
    var wrap = document.getElementById('pagination-button')
    wrap.innerHTML = ''

    if (state.page > 1) {
        wrap.innerHTML += `<button value=${state.page-1} class="page btn btn-sm btn-info">&#171; Prev</button>`
    }
    wrap.innerHTML += `<button value=${state.page} class="page btn btn-sm btn-info">${state.page}</button>`
    if (state.page < pages) {
        wrap.innerHTML += `<button value=${state.page+1} class="page btn btn-sm btn-info">Next &#187;</button>`
    }

    $('.page').on('click', function() {
        console.log("click function");
        $('#result_table_body').empty();
        state.page = Number($(this).val());
        // history.pushState({page: state.page}, "title", "?page=" + state.page + (genre ? "&genre=" + genre : "") + (prefix ? "&prefix=" + prefix : "") + (sorting ? "&sorting=" + sorting : ""));
        fetchMovies(state.page, state.rows);
    })
}


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

function handleBrowseResult(resultData){
    $('#result_table_body').empty();
    state.querySet = resultData
    console.log("handleBrowseResult: ");
    var data = pagination(state.querySet, state.page, state.rows)
    console.log('Data:', data)
    var newList = data.querySet
    for (let i = 0; i < newList.length; i++) {
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
        let genreIdRow = resultData[i]["genres_id"].split(",");
        let genreRow = resultData[i]["genres"].split(",");
        rowHTML += "<th>";
        for (let k= 0; k < genreIdRow.length; k++){
            rowHTML += '<a href="result-browse.html?genre=' + genreIdRow[k] + '">'
                + genreRow[k] +
                '</a >';

            if (k < genreRow.length-1){
                rowHTML += ', ';
            }
        }
        rowHTML += "</th>";

        let starRow = resultData[i]["stars"].split(",");
        let starIdRow = resultData[i]["starsId"].split(",");
        rowHTML += "<th>";
        for (let j = 0; j < 3; j++){
            rowHTML += '<a href="single-star.html?id=' + starIdRow[j] + '">'
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
        resultTableBody.append(rowHTML);
        pageButtons(data.pages,resultData);
    }
}

// Function to fetch the movies for a specific page
function fetchMovies(pageNumber, pageSize) {
    let genre = getParameterByName('genre');
    let prefix = getParameterByName('prefix');
    let sorting = getParameterByName('sorting');

    // Construct the URL with the necessary query parameters
    let url = `api/result?`;
    if (genre) {
        url += `genre=${genre}`;
    }
    if (prefix) {
        url += `prefix=${prefix}`;
    }
    if (sorting) {
        url += `sorting=${sorting}`;
    }
    url += `&page=${pageNumber}`;

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: url,
        success: (resultData) => {
            console.log("Data fetched for page: ", pageNumber);
            handleBrowseResult(resultData);
        },
        error: function(error) {
            console.log("Error fetching data: ", error);
        }
    });
}

//
// window.onpopstate = function(event) {
//     console.log("Location: " + document.location + ", state: " + JSON.stringify(event.state));
//     state.page = getParameterByName('page') || 1; // Default to page 1 if no page param
//     fetchMovies(state.page, state.rows);
// };


/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

function addToCart(movieId){
    $.ajax({
        dataType:"json",
        method: "POST",
        url:"api/cart",
        data: {
            "movieId": movieId,
            "amount" : "1"
        },
        success: window.alert("Add Successfully")
    });
}


function submitSortingForm(formSubmitEvent) {
    console.log("submit sorting form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    formSubmitEvent.preventDefault();
    $.ajax(
        "api/result", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: tuning.serialize(),
            success: (resultData) => handleBrowseResult(resultData)
        });
}

let genre = getParameterByName('genre')
let prefix = getParameterByName('prefix')
let sorting = getParameterByName('sorting')
let currPage = getParameterByName('page')
console.log("get Genre: " + genre)
console.log("get Prefix: " + prefix)
console.log("get sorting: " + sorting)
console.log("get currPage: " + currPage)

// tuning.submit(submitSortingForm);
resultTableBody.submit(submitSortingForm);
if (sorting != null){
    console.log("Sorting");
}else if (genre == null) {
    console.log("tuningSubmit prefix");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/result?prefix=" + prefix + "&page=" + currPage, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleBrowseResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
} else {
    console.log("tuningSubmit genre");
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET",// Setting request method
        url: "api/result?genre=" + genre + "&page=" + currPage, // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleBrowseResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
    });
}
