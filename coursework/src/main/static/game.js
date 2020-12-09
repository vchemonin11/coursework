"use strict"; // jshint ;_;
const settings = {
    modes: ["net", "hotseat"],
    currentMode: "net",
    startRed: false,
    color: 'blue',
    server: 'http://localhost:8080',
    currentUser: 2,
    currentGame: "",
    useSound: true,
    size: 3
}

function getRandomInt(max) {
    return Math.floor(Math.random() * Math.floor(max));
}

const playSound = (elem) => {
    if (!elem) return;
    elem.play();
}


function presenterFunc(matrix_result, settings, player1, player2, result, isRed) {
    const size = settings.size;
    const size_sqr = size * size;
    const label = getRandomInt(1000);
    const digits = [];
    let activeCellIndex = -1;
    let activeDigitIndex = -1;
    let lastCompMove = -1;
    let lastUserMove = -1;

    let bestDigit = -1;
    let bestPos = -1;
    let step = fill_digits(matrix_result, digits);
    let currentUserIsRed = (step % 2) != isRed;
    const player_moves = Array(size_sqr).fill(false);
    const comp_moves = Array(size_sqr).fill(false);

    let playerCount = 0;
    if (player1) {
        ++playerCount;
    }
    if (player2) {
        ++playerCount;
    }

    const getLastCompMove = () => lastCompMove;

    const getLastUserMove = () => lastUserMove;

    const isLastMove = (m) => lastCompMove === m || lastUserMove === m;

    const isBestDigit = (m) => bestDigit === m;
    const isBestPosition = (m) => bestPos === m;

    const clearState = () => {
        currentUserIsRed = !currentUserIsRed;
        step = fill_digits(matrix_result, digits);

        activeCellIndex = -1;
        activeDigitIndex = -1;
        bestPos = -1;
        bestDigit = -1;
    }

    function fill_digits(matrix, digits) {
        let step = 0;
        for (let i = 0; i < size_sqr; ++i) {
            digits[i] = false;
        }
        for (let i = 0; i < size_sqr; ++i) {
            const value = matrix[i];
            if (value > 0) {
                ++step;
                let index = value - 1;
                digits[index] = true;
            }
        }
        return step;
    }

    const setMove = function (position, digit, isRed) {
        if (position < 0) {
            return false;
        }

        if (digit < 0) {
            return false;
        }

        if (matrix_result[position]) {
            console.log("State error");
            return false;
        }
        matrix_result[position] = digit + 1;
        if (!isRed) {
            player_moves[position] = true;
            lastUserMove = position;
        } else {
            comp_moves[position] = true;
            lastCompMove = position;
        }
        step = fill_digits(matrix_result, digits);
        return true;
    }

    const tryMove = function () {
        return setMove(getActivePosition(), getActiveDigitIndex(), currentUserIsRed);
    }

    const setActiveDigitIndex = function (ind) {
        if (currentUserIsRed && settings.currentMode !== "hotseat") {
            return;
        }
        if (ind >= 0) {
            if (digits[ind]) {
                activeDigitIndex = -1;
                return;
            }
        }
        activeDigitIndex = ind;
    };

    const getActiveDigitIndex = () => activeDigitIndex;

    const setActivePosition = function (pos) {
        if (currentUserIsRed && settings.currentMode !== "hotseat") {
            return;
        }
        if (pos >= 0) {
            if (matrix_result[pos] > 0) {
                activeCellIndex = -1;
                return;
            }
        }
        activeCellIndex = pos;
    };

    const getActivePosition = () => activeCellIndex;
    const getActivePositionX = () => getActivePosition() % size;
    const getActivePositionY = () => Math.floor(getActivePosition() / size);

    const getDigits = () => digits;

    const getState = () => step + playerCount * 1000;

    const isWin = (startRed) => {
        return result > 0 !== startRed;
    }

    const hasCurrentPlayer = (currentUser) => {
        if (player1) {
            if (player1.id == currentUser) {
                return true;
            }
        }
        if (player2) {
            if (player2.id == currentUser) {
                return true;
            }
        }
        return false;
    }

    const isCurrentRed = () => currentUserIsRed;

    const lessThanTwoMoves = () => {
        return step + 2 > matrix_result.length;
    }

    const dropState = () => step = 500;

    const getLabel = () => label;

    return {
        matrix_result: matrix_result,
        player_moves: player_moves,
        comp_moves: comp_moves,
        player1: player1,
        player2: player2,
        result: result,
        isRed: isRed,
        getLastCompMove: getLastCompMove,
        getLastUserMove: getLastUserMove,
        isLastMove: isLastMove,
        getActiveDigitIndex: getActiveDigitIndex,
        setActiveDigitIndex: setActiveDigitIndex,
        getActivePosition: getActivePosition,
        getActivePositionX: getActivePositionX,
        getActivePositionY: getActivePositionY,
        setActivePosition: setActivePosition,
        getDigits: getDigits,
        getState: getState,
        hasCurrentPlayer: hasCurrentPlayer,
        dropState: dropState,
        isWin: isWin,
        lessThanTwoMoves: lessThanTwoMoves,
        tryMove: tryMove,
        isCurrentRed: isCurrentRed,
        isBestDigit: isBestDigit,
        isBestPosition: isBestPosition,
        clearState: clearState,
        getLabel: getLabel
    }
}

function stub() {
}

const handleClick = function (evt, parent) {
    const getIndex = function (e, parent) {
        const target = e.target || e.srcElement;
        for (let i = 0; i < parent.children.length; i++) {
            if (parent.children[i] === target) return i;
        }
        return -1;
    };

    evt.preventDefault();
    if (!(evt.target.classList.contains('cell') || evt.target.classList.contains('digit'))) {
        return;
    }
    return getIndex(evt, parent);
};

function onGameEnd(window, document, settings, presenter) {
    const overlay = document.getElementsByClassName("overlay")[0];
    const close = document.getElementsByClassName("close")[0];

    const isWin = presenter.isWin(presenter.isRed);
    if (isWin) {
        if (settings.useSound) {
            const tada = document.getElementById("tada");
            playSound(tada);
        }
    }
    const message = isWin ? "You win" : "You lose";
    const h2 = overlay.querySelector('h2');
    h2.textContent = message;
    const content = overlay.querySelector('.content');
    content.textContent = "Determinant =  " + presenter.result;
    overlay.classList.add('show');

    close.addEventListener("click", function (e) {
        e.preventDefault();
        overlay.classList.remove("show");
    }, {once: true});
}

function drawMatrix(presenter, box) {
    for (let i = 0; i < presenter.matrix_result.length; i++) {
        const tile = box.childNodes[i];
        const val = presenter.matrix_result[i];
        tile.textContent = (val) ? val.toString() : "0";

        if (val) {
            tile.className = 'cell disabled';
        } else {
            tile.className = 'cell hole';
        }
        if (presenter.getActivePosition() === i) {
            tile.classList.add('active');
            if (presenter.isCurrentRed()) {
                tile.classList.add('comp');
            } else {
                tile.classList.add('player');
            }
        }
        if (presenter.comp_moves[i]) {
            tile.classList.add('comp');
        }
        if (presenter.player_moves[i]) {
            tile.classList.add('player');
        }
        if (presenter.isLastMove(i)) {
            tile.classList.add('last');
        }
        if (presenter.isBestPosition(i)) {
            tile.classList.add('best');
        }
    }
}

function drawDigits(presenter, digits, disableClass) {
    const digits_local = presenter.getDigits();
    for (let i = 0; i < presenter.matrix_result.length; i++) {
        const tile = digits.childNodes[i];
        const used = digits_local[i];
        const val = i + 1;
        tile.textContent = val.toString();
        tile.className = 'digit';

        if (used) {
            tile.classList.add(disableClass);
        }
        if (presenter.getActiveDigitIndex() === i) {
            tile.classList.add('active');
            if (presenter.isCurrentRed()) {
                tile.classList.add('comp');
            } else {
                tile.classList.add('player');
            }
        }
        if (i === presenter.matrix_result[presenter.getLastCompMove()] - 1) {
            tile.classList.add('comp');
        }
        if (i === presenter.matrix_result[presenter.getLastUserMove()] - 1) {
            tile.classList.add('player');
        }
        if (presenter.isBestDigit(i)) {
            tile.classList.add('best');
        }
    }
}

function draw(presenter, box, digits, disableClass = 'disabled') {
    drawMatrix(presenter, box);
    drawDigits(presenter, digits, disableClass);
}

function drawAnimate(presenter, box, digits, disableClass = 'disabled-animate') {
    for (let i = 0; i < presenter.matrix_result.length; i++) {
        const tile = box.childNodes[i];
        const val = presenter.matrix_result[i];
        if (presenter.getActivePosition() === i) {
            tile.textContent = (val) ? val.toString() : "0";
            if (val) {
                tile.className = 'cell disabled';
            } else {
                tile.className = 'cell hole';
            }
            tile.classList.add('active');
            if (presenter.isCurrentRed()) {
                tile.classList.add('comp');
            } else {
                tile.classList.add('player');
            }
            if (presenter.comp_moves[i]) {
                tile.classList.add('comp');
            }
            if (presenter.player_moves[i]) {
                tile.classList.add('player');
            }
            if (presenter.isLastMove(i)) {
                tile.classList.add('last');
            }
            if (presenter.isBestPosition(i)) {
                tile.classList.add('best');
            }
        }
    }

    const digits_local = presenter.getDigits();
    for (let i = 0; i < presenter.matrix_result.length; i++) {
        const tile = digits.childNodes[i];
        const used = digits_local[i];
        const val = i + 1;
        if (presenter.getActiveDigitIndex() === i) {
            tile.textContent = val.toString();
            tile.className = 'digit';

            if (used) {
                tile.classList.add(disableClass);
            }
            tile.classList.add('active');
            if (presenter.isCurrentRed()) {
                tile.classList.add('comp');
            } else {
                tile.classList.add('player');
            }
            if (i === presenter.matrix_result[presenter.getLastCompMove()] - 1) {
                tile.classList.add('comp');
            }
            if (i === presenter.matrix_result[presenter.getLastUserMove()] - 1) {
                tile.classList.add('player');
            }
            if (presenter.isBestDigit(i)) {
                tile.classList.add('best');
            }
        }
    }
}


async function postData(url) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    });
    // console.log(response);
    return await response.json();
}

function starter(window, document, settings, f) {
    function stringToBoolean(string) {
        switch (string.toLowerCase().trim()) {
            case "true":
            case "yes":
            case "1":
                return true;
            case "false":
            case "no":
            case "0":
            case null:
                return false;
            default:
                return Boolean(string);
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

function removeElemInFiled(field, className) {
    const old = field.querySelector("." + className);
    if (old) {
        old.remove();
    }
}


function drawState(window, document, settings, presenter) {
    const filed = document.querySelector(".field");
    removeElemInFiled(filed, "join");
    const box = newElemInFiled(document, filed, "box");
    const digits = newElemInFiled(document, filed, "digits");
    const player1Html = document.querySelector(".player1");
    const player2Html = document.querySelector(".player2");

    function joinBot(button, evt) {
        evt.preventDefault();
        button.classList.add("disabled");
        postData(settings.server + "/game/joinbot/" + settings.currentGame + "?userId=" + settings.currentUser).then((data) => {
        });
    }

    function joinToGame(button, evt) {
        evt.preventDefault();
        button.classList.add("disabled");
        postData(settings.server + "/game/join/" + settings.currentGame + "?userId=" + settings.currentUser)
            .then(data => {
                // presenter.dropState();
                // document.location.href = "index.html?currentGame=" + game.key + "&currentUser=" + settings.currentUser
            });
    }

    let playersCount = 0;

    const hasCurrentPlayer = presenter.hasCurrentPlayer(settings.currentUser);

    function noPlayer(playerHtml, hasCurrentPlayer) {
        playerHtml.classList.add("clickable");
        if (hasCurrentPlayer) {
            playerHtml.innerText = "Waiting for opponent. Play with bot?"
            playerHtml.onclick = (evt) => joinBot(playerHtml, evt);
        } else {
            playerHtml.innerText = "Waiting for player. Join?"
            playerHtml.onclick = (evt) => joinToGame(playerHtml, evt);
        }
    }

    if (presenter.player1) {
        player1Html.innerText = presenter.player1.name;
        ++playersCount;
    } else {
        noPlayer(player1Html, hasCurrentPlayer);
    }
    if (presenter.player2) {
        player2Html.innerText = presenter.player2.name;
        ++playersCount;
    } else {
        noPlayer(player2Html, hasCurrentPlayer);
    }
    const bothDefined = playersCount === 2;

    function initField(fieldSize, className, elem) {
        for (let i = 0; i < fieldSize; i++) {
            const cell = document.createElement('div');
            cell.className = className;
            elem.appendChild(cell);
        }
    }

    initField(presenter.matrix_result.length, 'cell', box);
    drawMatrix(presenter, box);
    if (bothDefined) {
        initField(presenter.matrix_result.length, 'digit', digits);
        drawDigits(presenter, digits, 'disabled');
    }

    function doStep(presenter) {
        if (presenter.tryMove()) {
            postData(settings.server + "/game/" + settings.currentGame + "/move" + "?userId=" + settings.currentUser + "&digit=" + (presenter.getActiveDigitIndex() + 1) +
                "&x=" + presenter.getActivePositionX() + "&y=" + presenter.getActivePositionY()).then((data) => {
            });
            drawAnimate(presenter, box, digits);
            presenter.clearState();
            setTimeout(() => presenter.dropState(), 1000);
        } else {
            draw(presenter, box, digits);
        }
    }

    const handleBox = function (evt) {
        presenter.setActivePosition(handleClick(evt, box));
        doStep(presenter);
    };

    const handleClickDigits = function (evt) {
        presenter.setActiveDigitIndex(handleClick(evt, digits));
        doStep(presenter);
    };


    if (bothDefined) {
        box.addEventListener("click", handleBox, false);
        digits.addEventListener("click", handleClickDigits, false);
    }

    if (presenter.result != null) {
        onGameEnd(window, document, settings, presenter);
    }

}

async function mainLoop(window, document, settings, prevGame) {
    let presenter = prevGame;
    if (settings.currentGame) {
        const resp = await fetch(settings.server + "/game/" + settings.currentGame + "?userId=" + settings.currentUser);
        try {
            const newGame = await resp.json();
            const newPresenter = presenterFunc(newGame.arr, settings, newGame.player1, newGame.player2, newGame.result, newGame.isRed);
            if (!prevGame) {
                console.log("No prev", newPresenter.getLabel());
                presenter = newPresenter;
                drawState(window, document, settings, newPresenter);
            } else {
                if (newPresenter.getState() !== prevGame.getState()) {
                    presenter = newPresenter;
                    console.log("Diff steps");
                    drawState(window, document, settings, presenter);
                }
            }
        } catch (e) {
            console.log(e);
        }
    }
    await delay(2000);
    await mainLoop(window, document, settings, presenter)
}

function menuFunction(window, document, settings) {
    const rewindLink = "player.html?" + "currentGame=" + settings.currentGame + "&rewind=" + 0 + "&userId=" + settings.currentUser;
    const menu = document.querySelector('aside');

    function menuItem(action, link) {
        const startFirst = menu.querySelector("." + action);
        startFirst.href = link;
    }

    menuItem("moves-link", rewindLink);
}

async function main(window, document, settings) {
    const menu = menuFunction(window, document, settings);
    await mainLoop(window, document, settings, null);
}

launchWithUrlParse(window, document, settings, main);
