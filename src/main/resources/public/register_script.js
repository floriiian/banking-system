document.addEventListener("DOMContentLoaded", function(){

    // types = 'info, 'success', 'error'];
    const toasts = document.getElementById('toasts');

    function createNotification(type = null, message = null) {
        const notification = document.createElement('div');
        notification.classList.add('toast');
        notification.classList.add(type);

        notification.innerText = message;

        toasts.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    const registerButton =
        document.getElementById("registerButton");

    const xmlhttp = new XMLHttpRequest();

    let registerForm = document.getElementById("registerForm");

    registerForm.addEventListener("submit", event => {
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

                let response = xmlhttp.responseText.split(":");

                switch(response[0]) {
                    case "INSUFFICIENT_DATA":
                        // TODO: Handle
                        createNotification("info", "Youre missing data.");
                        break;

                    case "WEAK_PASSWORD":
                        // TODO: Handle
                        createNotification('info', "Your password is too weak.");
                        break;
                    case "INVALID_AGE":
                        createNotification('info', "You must be at least 18 years old to use this service.");
                        // TODO: Handle
                        break;
                    case "REGISTRATION_SUCCESSFUL":
                    // TODO: Handle
                    createNotification('success', "You have successfully registered with the ID: " + response[1]);
                        setTimeout(function(){
                            window.location.replace("http://localhost:7070/login");
                        }, 3000);
                    break;
                }
            }
            else {
                console.error("Request failed: ", xmlhttp.status);
                createNotification('error', "Server Error");
            }
        };

        xmlhttp.send(JSON.stringify(payload));
    };


});

