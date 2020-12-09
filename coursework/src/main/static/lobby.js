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

function menuFunction(window, document, settings) {
    const menu = document.querySelector('aside');

    function menuItem(action) {
        const startFirst = menu.querySelector("." + action);
        const handleStart = async function (evt) {
            evt.preventDefault();
            postData(settings.server + "/game/" + action + "?userId=" + settings.currentUser + "&size=" + settings.size).then((data) => {
                console.log(data); // JSON data parsed by `response.json()` call
                settings.currentGame = data.key;
            });
        };
        startFirst.addEventListener("click", handleStart, false);
    }

    menuItem("new1");
    menuItem("new2");
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

function drawOneGame(game) {
    const playerName1 = game.player1 ? game.player1.name : "???";
    const playerName2 = game.player2 ? game.player2.name : "???";

    return game.gameId + ") " + playerName1 + " vs " + playerName2;
}


async function drawGames(settings, document) {
    const resp = await fetch(settings.server + "/game/list" + "?userId=" + settings.currentUser);
    const games = await resp.json();
    const lobby = newElemInFiled(document, document.querySelector(".content-lobby"), "lobby-all");
    const name = document.createElement('h2');
    name.innerText = "Last games";
    lobby.appendChild(name);
    for (const game of games) {
        const newEl = document.createElement('div');
        newEl.classList.add("game-line");
        newEl.classList.add("clickable");
        newEl.innerText = drawOneGame(game);
        newEl.onclick = function () {
            document.location.href = "index.html?currentGame=" + game.key + "&currentUser=" + settings.currentUser
        }
        lobby.appendChild(newEl);
    }

    console.log(games);
}

async function drawGamesToJoin(settings, document) {
    const resp = await fetch(settings.server + "/game/joinlist" + "?userId=" + settings.currentUser);
    const games = await resp.json();
    const lobby = newElemInFiled(document, document.querySelector(".content-lobby"), "lobby");
    const name = document.createElement('h2');
    name.innerText = "Games to join";
    lobby.appendChild(name);
    for (const game of games) {
        const newEl = document.createElement('div');
        newEl.classList.add("game-line");
        newEl.classList.add("clickable");
        newEl.innerText = drawOneGame(game);
        newEl.onclick = function () {
            postData(settings.server + "/game/join/" + game.key + "?userId=" + settings.currentUser)
                .then(data => {
                    document.location.href = "index.html?currentGame=" + game.key + "&currentUser=" + settings.currentUser
                });
        }
        lobby.appendChild(newEl);
    }

    console.log(games);
}


async function mainLoop(window, document, settings) {
    await drawGamesToJoin(settings, document);
    await drawGames(settings, document);
    await delay(5000);
    await mainLoop(window, document, settings)
}

async function main(window, document, settings) {
    const menu = menuFunction(window, document, settings);
    await mainLoop(window, document, settings, null)
}

launchWithUrlParse(window, document, settings, main);
