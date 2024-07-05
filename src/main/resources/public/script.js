

const registerButton =
    document.getElementById("registerButton");

const xmlhttp = new XMLHttpRequest();


registerButton.onclick = function(){

    let name = document.getElementById("name").value;
    let age = document.getElementById("age").value;
    let password = document.getElementById("password").value;

    const registerResponse = xmlhttp.responseText;
    const payload = {
        "name": name,
        "age": age,
        "password": password
    };

    xmlhttp.open("POST", "http://localhost/7070/register");
    xmlhttp.setRequestHeader("Content-Type", "application/json;charset=UTF-8");

    // After the request is done
    xmlhttp.onload = function() {
        if (xmlhttp.status >= 200 && xmlhttp.status < 300) {
            console.log("Register Response: ", xmlhttp.responseText);
        } else {
            console.error("Request failed with status: ", xmlhttp.status);
        }
    };

    xmlhttp.send(JSON.stringify(payload));
    console.log("Register Response: ", registerResponse);
};


