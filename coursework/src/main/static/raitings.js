"use strict"; // jshint ;_;
const settings = {
    modes: ["net", "hotseat"],
    currentMode: "net",
    startRed: false,
    color: 'blue',
    server: 'http://localhost:8080',
    currentUser: 3,
    currentGame: "",
    size: 3
}


function stub() {
}

async function postData(url) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    console.log(response);
    return await response.json();
}

function starter(window, document, settings, f) {
    function stringToBoolean(string) {
        switch(string.toLowerCase().trim()){
            case "true": case "yes": case "1": return true;
            case "false": case "no": case "0": case null: return false;
            default: return Boolean(string);
        }
    }
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    for (const [key, value] of urlParams) {
        if (typeof settings[key] === "number") {
            settings[key] = parseInt(value, 10);
        } else if (typeof settings[key] === "boolean") {
            settings[key] = stringToBoolean(value);
        } else {
            settings[key] = value;
        }
    }
    f(window, document, settings);
}

function launch(f, window, document, settings, afterUrlParse) {
    if (document.readyState !== 'loading') {
        f(window, document, settings, afterUrlParse);
    } else {
        document.addEventListener("DOMContentLoaded", function (event) {
            f(window, document, settings, afterUrlParse);
        });
    }
}

const delay = ms => new Promise(resolve => setTimeout(resolve, ms));

function launchWithUrlParse(window, document, settings, afterUrlParse) {
    launch(starter, window, document, settings, afterUrlParse);
}

function newElemInFiled(document, field, className) {
    const old = field.querySelector("." + className);
    if (old) {
        old.remove();
    }
    const newEl = document.createElement('div');
    newEl.className = className;
    field.appendChild(newEl);
    return newEl;
}

function drawOneLine(game) {
    return "(" + game.result + ") " + game.name + " " + game.date;
}

async function drawRaitings(settings, document) {
    const resp = await fetch(settings.server + "/raitings/all" + "?userId=" + settings.currentUser + "&offset=0");
    const games = await resp.json();
    const lobby = newElemInFiled(document, document.querySelector(".content-lobby"), "lobby");
    const name = document.createElement('h2');
    name.innerText = "Raitings";
    lobby.appendChild(name);
    for (const game of games) {
        const newEl = document.createElement('div');
        newEl.classList.add("game-line");
        newEl.innerText = drawOneLine(game);
        lobby.appendChild(newEl);
    }
    console.log(games);
}


async function mainLoop(window, document, settings) {
    await drawRaitings(settings, document);
    // await delay(5000);
    // await mainLoop(window, document, settings)
}

async function main(window, document, settings) {
    await mainLoop(window, document, settings, null)
}

launchWithUrlParse(window, document, settings, main);
