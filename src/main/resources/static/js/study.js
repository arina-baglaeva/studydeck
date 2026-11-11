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

    function show(index) {
        cards[current].hidden = true;
        cards[current].classList.remove("flipped");
        current = index;
        cards[current].hidden = false;
        position.textContent = String(current + 1);
        prevButton.disabled = current === 0;
        nextButton.disabled = current === cards.length - 1;
    }

    function flip() {
        cards[current].classList.toggle("flipped");
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

    document.addEventListener("keydown", function (event) {
        if (event.key === "ArrowLeft" && current > 0) {
            show(current - 1);
        } else if (event.key === "ArrowRight" && current < cards.length - 1) {
            show(current + 1);
        }
    });

    show(0);
});
