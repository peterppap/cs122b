let addStar_form = $("#addStar_form");
function handleAddingResult(resultDataString) {
    console.log("handle add star response")
    $("#adding_information_message").text(resultDataString["message"]);
}

function submitAddStarForm(formSubmitEvent){
    formSubmitEvent.preventDefault();
    console.log("submit add star form");
    $.ajax(
        "api/addStar", {
            method: "POST",
            data: addStar_form.serialize(),
            success: (resultData) => handleAddingResult(resultData)
        }
    );
}

addStar_form.submit(submitAddStarForm);