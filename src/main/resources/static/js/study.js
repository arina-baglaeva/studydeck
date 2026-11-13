document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".study-card");
    if (cards.length === 0) {
        return;
    }
    const study = document.querySelector(".study");
    const frontLabel = study.dataset.frontLabel.toLowerCase();
    const backLabel = study.dataset.backLabel.toLowerCase();
    const session = document.getElementById("study-session");
    const summary = document.getElementById("study-summary");
    const position = document.getElementById("study-position");
    const total = document.getElementById("study-total");
    const prevButton = document.getElementById("study-prev");
    const nextButton = document.getElementById("study-next");
    const flipButton = document.getElementById("study-flip");
    const knownButton = document.getElementById("study-known");
    const unknownButton = document.getElementById("study-unknown");
    const knownCount = document.getElementById("study-known-count");
    const unknownCount = document.getElementById("study-unknown-count");
    const repeatButton = document.getElementById("summary-repeat");
    let order = [0];
    let answers = [];
    let current = 0;

    function cardAt(index) {
        return cards[order[index]];
    }

    function setFlipped(card, flipped) {
        card.classList.toggle("flipped", flipped);
        card.querySelector(".study-front").setAttribute("aria-hidden", String(flipped));
        card.querySelector(".study-back").setAttribute("aria-hidden", String(!flipped));
        flipButton.textContent = "Показать: " + (flipped ? frontLabel : backLabel);
    }

    function show(index) {
        cardAt(current).hidden = true;
        setFlipped(cardAt(current), false);
        current = index;
        cardAt(current).hidden = false;
        position.textContent = String(current + 1);
        prevButton.disabled = current === 0;
        nextButton.disabled = current === order.length - 1;
    }

    function flip() {
        const card = cardAt(current);
        setFlipped(card, !card.classList.contains("flipped"));
    }

    function countAnswers(value) {
        return answers.filter(function (answer) {
            return answer === value;
        }).length;
    }

    function updateScore() {
        knownCount.textContent = String(countAnswers("known"));
        unknownCount.textContent = String(countAnswers("unknown"));
    }

    function unknownCards() {
        return order.filter(function (cardIndex, index) {
            return answers[index] === "unknown";
        });
    }

    function fillUnknownList(cardIndexes) {
        const list = document.getElementById("summary-unknown-list");
        list.replaceChildren();
        cardIndexes.forEach(function (cardIndex) {
            const item = document.createElement("li");
            item.textContent = cards[cardIndex].querySelector(".study-front .study-text").textContent;
            list.appendChild(item);
        });
        document.getElementById("summary-unknown-block").hidden = cardIndexes.length === 0;
        repeatButton.hidden = cardIndexes.length === 0;
    }

    function showSummary() {
        const known = countAnswers("known");
        const percent = Math.round(known * 100 / order.length);
        document.getElementById("summary-known").textContent = String(known);
        document.getElementById("summary-total").textContent = String(order.length);
        document.getElementById("summary-percent").textContent = String(percent);
        document.getElementById("summary-bar-fill").style.width = percent + "%";
        fillUnknownList(unknownCards());
        session.hidden = true;
        summary.hidden = false;
        summary.querySelector("h2").focus();
    }

    function firstUnanswered() {
        return answers.indexOf(null);
    }

    function mark(value) {
        answers[current] = value;
        cardAt(current).dataset.answer = value;
        updateScore();
        if (firstUnanswered() === -1) {
            showSummary();
        } else if (current < order.length - 1) {
            show(current + 1);
        } else {
            show(firstUnanswered());
        }
    }

    function start(cardIndexes) {
        cards.forEach(function (card) {
            card.hidden = true;
            setFlipped(card, false);
            delete card.dataset.answer;
        });
        order = cardIndexes;
        answers = new Array(order.length).fill(null);
        current = 0;
        total.textContent = String(order.length);
        updateScore();
        summary.hidden = true;
        session.hidden = false;
        show(0);
    }

    knownButton.addEventListener("click", function () {
        mark("known");
    });

    unknownButton.addEventListener("click", function () {
        mark("unknown");
    });

    prevButton.addEventListener("click", function () {
        if (current > 0) {
            show(current - 1);
        }
    });

    nextButton.addEventListener("click", function () {
        if (current < order.length - 1) {
            show(current + 1);
        }
    });

    flipButton.addEventListener("click", flip);

    repeatButton.addEventListener("click", function () {
        start(unknownCards());
    });

    cards.forEach(function (card) {
        card.addEventListener("click", flip);
    });

    function isFlipKey(event) {
        const onControl = event.target.tagName === "BUTTON" || event.target.tagName === "A";
        return (event.key === " " || event.key === "Enter") && !onControl;
    }

    document.addEventListener("keydown", function (event) {
        if (session.hidden) {
            return;
        }
        if (isFlipKey(event)) {
            event.preventDefault();
            flip();
        } else if (event.key === "ArrowLeft" && current > 0) {
            show(current - 1);
        } else if (event.key === "ArrowRight" && current < order.length - 1) {
            show(current + 1);
        }
    });

    start(Array.from(cards.keys()));
});
