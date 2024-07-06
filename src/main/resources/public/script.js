

document.addEventListener("DOMContentLoaded", function(){

    const registerButton =
        document.getElementById("registerButton");

    const xmlhttp = new XMLHttpRequest();

    let form = document.getElementById("registerForm");
    form.addEventListener("submit", event => {
        event.preventDefault();
    })

    registerButton.onclick = function(){

        let name = document.getElementById("name").value;
        let age = document.getElementById("age").value;
        let password = document.getElementById("password").value;

        const payload = {
            "name": name,
            "age": age,
            "password": password
        };

        xmlhttp.open("POST", "http://localhost:7070/register");
        xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

        // After the request is done
        xmlhttp.onload = function() {
            if (xmlhttp.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            if (xmlhttp.status >= 200 && xmlhttp.status < 300) {

                console.log(xmlhttp.responseText);
                switch(xmlhttp.responseText) {
                    case "INSUFFICIENT_DATA":
                        // TODO: Handle
                        break;
                    case "REGISTRATION_SUCCESSFUL":
                        // TODO: Handle
                        break;
                    case "WEAK_PASSWORD":
                        // TODO: Handle
                        break;
                    case "INVALID_AGE":
                        // TODO: Handle
                        break;
                }
            }
            else {
                console.error("Request failed: ", xmlhttp.status);
            }
        };

        xmlhttp.send(JSON.stringify(payload));
    };
});

