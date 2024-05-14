let search_form = $("#search_form");
let tuning = $("#tuning");
let resultTableBody = jQuery("#result_table_body");

var state = {
    'page':1,
    'rows':10,
    'window':5,
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
        $('#result_table_body').empty()
        state.page = Number($(this).val())
        handleSearchResult(resultData)
    })
}

// document.addEventListener('DOMContentLoaded', function () {
//     let clickButton = document.querySelector('input[type="submit"]');
//
//     clickButton.addEventListener('click', function() {
//         // Assuming you want to go to the next page
//         fetchMovies(state.page, state.rows); // fetch the new page data
//     });
// });


function handleSearchResult(resultData) {
    state.querySet = resultData
    console.log("handleSearchResult: ");

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
            '<a href="single-movie.html?id=' + newList[i]['movie_id'] + '">'
            + newList[i]["movie_title"] +     // display star_name for the link text
            '</a>' +
            "</th>";
        rowHTML += "<th>" + newList[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + newList[i]["movie_director"] + "</th>";

        let genreIdRow = newList[i]["genres_id"].split(",");
        let genreRow = newList[i]["genres"].split(",");
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
        let starRow = newList[i]["stars"].split(",");
        let starIdRow = newList[i]["starsId"].split(",");
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
        rowHTML += "<th>" + newList[i]["rating"] + "</th>";
        rowHTML += `<th><button style="padding: 8px 16px; background-color: #4CAF50; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px;" onclick="addToCart('${newList[i]['movie_id']}')">Add</button></th>`;
        rowHTML += "</tr>";

        resultTableBody.append(rowHTML);
        pageButtons(data.pages,resultData);
    }
}
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
        success: window.alert("Movie added to your shopping cart!")
    });
}
function submitSearchForm(formSubmitEvent){

    formSubmitEvent.preventDefault();
    resultTableBody.html("");
    console.log("submit search form");
    $.ajax(
        "api/search", {
            method: "GET",
            data: search_form.serialize(),
            success: (resultData) => handleSearchResult(resultData)
        }
    );
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
        "api/search", {
            method: "GET",
            // Serialize the login form to the data sent by POST request
            data: tuning.serialize(),
            success: (resultData) => handleSearchResult(resultData)
        });
}

search_form.submit(submitSearchForm);

tuning.submit(submitSortingForm);
