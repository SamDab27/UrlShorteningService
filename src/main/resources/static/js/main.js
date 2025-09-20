const expirationInput = document.getElementById('expirationInput');
const urlInputEl = document.getElementById('urlInput');
const shortenButton = document.getElementById('shortenButton');
const resultDiv = document.getElementById('result');

expirationInput.addEventListener('keydown', (e) => e.preventDefault());
expirationInput.addEventListener('paste', (e) => e.preventDefault());

expirationInput.addEventListener('input', () => {
    const val = expirationInput.value;
    if (!val) {
        resultDiv.textContent = '';
        return;
    }

    const expirationDate = new Date(val);
    if (expirationDate < new Date()) {
        resultDiv.textContent = 'Expiration date/time cannot be in the past.';
        return;
    }

    resultDiv.textContent = '';
});

shortenButton.addEventListener('click', async () => {
    const urlInput = urlInputEl.value.trim();
    const expirationVal = expirationInput.value;

    resultDiv.textContent = '';

    if (!urlInput) {
        resultDiv.textContent = 'Please enter a URL.';
        return;
    }

    let expirationUtc = null;
    if (expirationVal) {
        const localDate = new Date(expirationVal);
        if (localDate < new Date()) {
            resultDiv.textContent = 'Expiration date/time cannot be in the past.';
            return;
        }
        expirationUtc = localDate.toISOString(); // convert local time to UTC
    }

    try {
        const response = await fetch('/api/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ url: urlInput, expirationDate: expirationUtc })
        });

        const data = await response.json();

        if (response.ok && data.shortLink) {
            const shortUrl = window.location.origin + '/' + data.shortLink;
            resultDiv.innerHTML = `Shortened URL: <a href="${shortUrl}" target="_blank" rel="noopener">${shortUrl}</a>`;
        } else if (data.error) {
            resultDiv.textContent = `Error: ${data.error}`;
        } else {
            resultDiv.textContent = 'Unexpected error occurred.';
        }
    } catch (error) {
        resultDiv.textContent = 'Error connecting to the server.';
        console.error(error);
    }
});

