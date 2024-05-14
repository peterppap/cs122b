function handleAddingResult(resultDataString) {
    console.log("handle add movie response")

    $("#adding_information_message").text(resultDataString["message"]);
}

function submitAddMovieForm(formSubmitEvent){
    formSubmitEvent.preventDefault();
    console.log("submit add movie form");
    $.ajax(
        "api/addMovie", {
            method: "POST",
            data: addMovie_form.serialize(),
            success: (resultData) => handleAddingResult(resultData),
        }
    );
}
let addMovie_form = $("#addMovie_form");

addMovie_form.submit(submitAddMovieForm);