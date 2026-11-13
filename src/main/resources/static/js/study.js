document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".study-card");
    if (cards.length === 0) {
        return;
    }
    const study = document.querySelector(".study");
    const frontLabel = study.dataset.frontLabel.toLowerCase();
    const backLabel = study.dataset.backLabel.toLowerCase();
    const position = document.getElementById("study-position");
    const prevButton = document.getElementById("study-prev");
    const nextButton = document.getElementById("study-next");
    const flipButton = document.getElementById("study-flip");
    const knownButton = document.getElementById("study-known");
    const unknownButton = document.getElementById("study-unknown");
    const knownCount = document.getElementById("study-known-count");
    const unknownCount = document.getElementById("study-unknown-count");
    const answers = new Array(cards.length).fill(null);
    let current = 0;

    function setFlipped(card, flipped) {
        card.classList.toggle("flipped", flipped);
        card.querySelector(".study-front").setAttribute("aria-hidden", String(flipped));
        card.querySelector(".study-back").setAttribute("aria-hidden", String(!flipped));
        flipButton.textContent = "Показать: " + (flipped ? frontLabel : backLabel);
    }

    function show(index) {
        cards[current].hidden = true;
        setFlipped(cards[current], false);
        current = index;
        cards[current].hidden = false;
        position.textContent = String(current + 1);
        prevButton.disabled = current === 0;
        nextButton.disabled = current === cards.length - 1;
    }

    function flip() {
        const card = cards[current];
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

    function mark(value) {
        answers[current] = value;
        cards[current].dataset.answer = value;
        updateScore();
        if (current < cards.length - 1) {
            show(current + 1);
        }
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
        if (current < cards.length - 1) {
            show(current + 1);
        }
    });

    flipButton.addEventListener("click", flip);

    cards.forEach(function (card) {
        card.addEventListener("click", flip);
    });

    function isFlipKey(event) {
        const onControl = event.target.tagName === "BUTTON" || event.target.tagName === "A";
        return (event.key === " " || event.key === "Enter") && !onControl;
    }

    document.addEventListener("keydown", function (event) {
        if (isFlipKey(event)) {
            event.preventDefault();
            flip();
        } else if (event.key === "ArrowLeft" && current > 0) {
            show(current - 1);
        } else if (event.key === "ArrowRight" && current < cards.length - 1) {
            show(current + 1);
        }
    });

    show(0);
});
