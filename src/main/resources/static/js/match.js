document.addEventListener("DOMContentLoaded", function () {
    const board = document.querySelector(".match-board");
    if (board === null) {
        return;
    }
    const items = board.querySelectorAll(".match-item");
    const foundCount = document.getElementById("match-found");
    const doneMessage = document.getElementById("match-done");
    const mistakesCount = document.getElementById("match-mistakes");
    const feedback = document.getElementById("match-feedback");
    const timer = document.getElementById("match-timer");
    const finalTime = document.getElementById("match-final-time");
    const bestTime = document.getElementById("match-best");
    const recordMessage = document.getElementById("match-record");
    const wrongDelay = 700;
    const selected = { term: null, definition: null };
    let found = 0;
    let mistakes = 0;
    let locked = false;
    let startedAt = null;
    let timerId = null;

    function formatTime(milliseconds) {
        const totalSeconds = Math.floor(milliseconds / 1000);
        const minutes = Math.floor(totalSeconds / 60);
        const seconds = totalSeconds % 60;
        return String(minutes).padStart(2, "0") + ":" + String(seconds).padStart(2, "0");
    }

    function elapsed() {
        return Date.now() - startedAt;
    }

    function updateTimer() {
        timer.textContent = formatTime(elapsed());
    }

    function startTimer() {
        if (startedAt !== null) {
            return;
        }
        startedAt = Date.now();
        timerId = setInterval(updateTimer, 250);
    }

    function stopTimer() {
        clearInterval(timerId);
        updateTimer();
    }

    function sideOf(item) {
        return item.closest(".match-column").dataset.side;
    }

    function setSelected(item, value) {
        item.classList.toggle("selected", value);
        item.setAttribute("aria-pressed", String(value));
    }

    function clearSelection() {
        ["term", "definition"].forEach(function (side) {
            if (selected[side] !== null) {
                setSelected(selected[side], false);
                selected[side] = null;
            }
        });
    }

    function markMatched(item) {
        setSelected(item, false);
        item.classList.add("matched", "correct");
        item.disabled = true;
    }

    function csrfHeaders() {
        const token = document.querySelector("meta[name='_csrf']");
        const header = document.querySelector("meta[name='_csrf_header']");
        const headers = { "Content-Type": "application/x-www-form-urlencoded" };
        if (token !== null && header !== null) {
            headers[header.content] = token.content;
        }
        return headers;
    }

    function showResult(result) {
        bestTime.textContent = result.bestTime;
        recordMessage.hidden = !result.newRecord;
    }

    function saveResult(timeMillis) {
        fetch(board.dataset.resultUrl, {
            method: "POST",
            headers: csrfHeaders(),
            body: new URLSearchParams({ timeMillis: String(timeMillis) })
        }).then(function (response) {
            if (response.ok) {
                return response.json().then(showResult);
            }
        }).catch(function () {
            feedback.textContent = "Не удалось сохранить результат";
        });
    }

    function finishGame() {
        stopTimer();
        const timeMillis = elapsed();
        finalTime.textContent = formatTime(timeMillis);
        doneMessage.hidden = false;
        doneMessage.focus();
        saveResult(timeMillis);
    }

    function showWrong(term, definition) {
        locked = true;
        mistakes = mistakes + 1;
        mistakesCount.textContent = String(mistakes);
        feedback.textContent = "Неверно, попробуйте ещё раз";
        term.classList.add("wrong");
        definition.classList.add("wrong");
        setTimeout(function () {
            term.classList.remove("wrong");
            definition.classList.remove("wrong");
            clearSelection();
            locked = false;
        }, wrongDelay);
    }

    function checkPair() {
        const term = selected.term;
        const definition = selected.definition;
        if (term.dataset.pair === definition.dataset.pair) {
            markMatched(term);
            markMatched(definition);
            selected.term = null;
            selected.definition = null;
            found = found + 1;
            foundCount.textContent = String(found);
            feedback.textContent = "Верно!";
            if (found === items.length / 2) {
                finishGame();
            }
        } else {
            showWrong(term, definition);
        }
    }

    function choose(item) {
        if (locked) {
            return;
        }
        startTimer();
        const side = sideOf(item);
        if (selected[side] === item) {
            setSelected(item, false);
            selected[side] = null;
            return;
        }
        if (selected[side] !== null) {
            setSelected(selected[side], false);
        }
        selected[side] = item;
        setSelected(item, true);
        if (selected.term !== null && selected.definition !== null) {
            checkPair();
        }
    }

    items.forEach(function (item) {
        item.setAttribute("aria-pressed", "false");
        item.addEventListener("click", function () {
            choose(item);
        });
    });
});
