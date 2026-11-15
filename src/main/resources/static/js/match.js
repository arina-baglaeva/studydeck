document.addEventListener("DOMContentLoaded", function () {
    const board = document.querySelector(".match-board");
    if (board === null) {
        return;
    }
    const items = board.querySelectorAll(".match-item");
    const foundCount = document.getElementById("match-found");
    const doneMessage = document.getElementById("match-done");
    const selected = { term: null, definition: null };
    let found = 0;

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
        item.classList.add("matched");
        item.disabled = true;
    }

    function finishGame() {
        doneMessage.hidden = false;
        doneMessage.focus();
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
            if (found === items.length / 2) {
                finishGame();
            }
        } else {
            clearSelection();
        }
    }

    function choose(item) {
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
