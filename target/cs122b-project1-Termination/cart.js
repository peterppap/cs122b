
let cart = $("#cart");

function handleCartResult(resultArray) {
    console.log(resultArray);

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let cartTableBodyElement = jQuery("#shopping_cart_body");
    let proceedElement = jQuery("#proceed");
    let total = 0;
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultArray.length; i++) {
        let qty = resultArray[i]['quantity'];
        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<th>" + '<a href="single-movie.html?id=' + resultArray[i]['id'] + '">'
            + resultArray[i]['title'] + '</a>' + "</th>";
        // rowHTML += "<th>" + resultArray[i]['quantity'] + "</th>";

        rowHTML += `<th><a href="javascript:changeqty('${resultArray[i]['id']}', '-1')">-</a>
            ${resultArray[i]['quantity']}
            <a href="javascript:changeqty('${resultArray[i]['id']}', '1')">+</a></th>`;


        rowHTML += "<th>" + resultArray[i]['price'] + "</th>";
        rowHTML += `<th><a href="javascript:changeqty('${resultArray[i]['id']}', '-${qty}')">Delete</a></th>`
        rowHTML += "</tr>";
        total += resultArray[i]['quantity'] * resultArray[i]['price'];
        // Append the row created to the table body, which will refresh the page
        cartTableBodyElement.append(rowHTML);
    }
    const totalElement = document.getElementById("total");

    totalElement.textContent = `$${total}`;
    proceedElement.append(`<a href="javascript:sendTotal('${total}')">Proceed to Payment</a>`);
}

function sendTotal(total){
    $.ajax({
        dataType: "json",
        method: "Get",
        url: "api/payment",
        data:{
            "total": total
        },
        success: window.location.href = "payment.html?total=" + total
    })
}
function changeqty(id, qty){
    $.ajax({
        dataType: "json",
        method: "POST",
        url: "api/cart",
        data:{
            "movieId": id,
            "amount": qty
        },
        success: (resultData) => handleCartResult(resultData)
    });
    location.reload();
}
function handleCartInfo(cartEvent) {
    console.log("submit cart form");
    /**
     * When users click the submit button, the browser will not direct
     * users to the url defined in HTML form. Instead, it will call this
     * event handler when the event is triggered.
     */
    cartEvent.preventDefault();

    $.ajax("api/index", {
        method: "POST",
        data: cart.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartResult(resultDataJson["previousItems"]);
        }
    });

    // clear input form
    cart[0].reset();
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/cart", // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleCartResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

cart.submit(handleCartInfo);