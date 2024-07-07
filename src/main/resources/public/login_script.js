

document.addEventListener("DOMContentLoaded", function(){

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

    // types = 'info, 'success', 'error'];
    const toasts = document.getElementById('toasts');

    const loginButton =
        document.getElementById("loginButton");

    const xmlhttp = new XMLHttpRequest();

    let loginForm = document.getElementById("loginForm");

    loginForm.addEventListener("submit", event => {
        event.preventDefault();
    })

    loginButton.onclick = function(){

        let accountId = document.getElementById("account_id").value;
        let password = document.getElementById("password").value;

        const payload = {
            "accountId": accountId,
            "password": password
        };

        xmlhttp.open("POST", "http://localhost:7070/login");
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
                        createNotification("info", "Youre missing data.");
                        break;

                    case "ACCOUNT_NOT_FOUND":
                        createNotification('info', "Account with ID: " + accountId + " doesn't exist.");
                        break;
                    case "INVALID_CREDENTIALS":
                        createNotification('error', "The given credentials do not match.");
                        break;
                    case "LOGIN_SUCCESSFUL":
                    createNotification('success', "You have been logged in.");
                        setTimeout(function(){
                            window.location.replace("http://localhost:7070/index");
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

