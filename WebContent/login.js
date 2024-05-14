let login_form = $("#login_form");

/**
 * Handle the data returned by LoginServlet
 * @param resultDataString jsonObject
 */
function handleLoginResult(resultDataString) {
    let resultDataJson = JSON.parse(resultDataString);

    console.log("handle login response");
    console.log(resultDataJson);
    console.log(resultDataJson["status"]);

    // If login succeeds, it will redirect the user to main.html
    if (resultDataJson["status"] === "success") {
        window.location.replace("search-main.html");
        // window.location.replace("dashboard.html");
    }
    else {
        // If login fails, the web page will display
        // error messages on <div> with id "login_error_message"
        console.log("show error message");
        console.log(resultDataJson["message"]);
        $("#login_error_message").text(resultDataJson["message"]);
    }
}

/**
 * Submit the form content with POST method
 * @param formSubmitEvent
 */
function submitLoginForm(formSubmitEvent) {
    console.log("submit login form");
    formSubmitEvent.preventDefault();

    // Get the reCAPTCHA response
    const recaptchaResponse = grecaptcha.getResponse();

    // Check if the reCAPTCHA response is empty
    if (recaptchaResponse.length === 0) {
        $("#login_error_message").text("Please complete the reCAPTCHA first.");
        return;
    }

    // Send the form data with the reCAPTCHA response to your form-recaptcha endpoint
    $.post("form-recaptcha", login_form.serialize() + "&g-recaptcha-response=" + recaptchaResponse, function(data) {
        // Handle the response from your form-recaptcha endpoint
        // If the form-recaptcha validation is successful, send the form data to the other API endpoint
        if (data == 1 || recaptchaResponse.length != 0) {
            console.log("ready to login");
            $.ajax("api/login", {
                method: "POST",
                data: login_form.serialize(),
                success: (resultData) => handleLoginResult(resultData)
            });
        } else {
            $("#login_error_message").text("reCAPTCHA verification failed.");
        }
    });
}

// Bind the submit action of the form to a handler function
login_form.submit(submitLoginForm);
