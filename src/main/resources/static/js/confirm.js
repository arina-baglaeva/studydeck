document.addEventListener("submit", function (event) {
    const form = event.target;
    const question = form.dataset.confirm;
    if (question && !window.confirm(question)) {
        event.preventDefault();
    }
});
