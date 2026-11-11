document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".study-card");
    if (cards.length === 0) {
        return;
    }
    const position = document.getElementById("study-position");
    const prevButton = document.getElementById("study-prev");
    const nextButton = document.getElementById("study-next");
    const flipButton = document.getElementById("study-flip");
    let current = 0;

    function setFlipped(card, flipped) {
        card.classList.toggle("flipped", flipped);
        card.querySelector(".study-front").setAttribute("aria-hidden", String(flipped));
        card.querySelector(".study-back").setAttribute("aria-hidden", String(!flipped));
        flipButton.textContent = flipped ? "Показать термин" : "Показать определение";
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
