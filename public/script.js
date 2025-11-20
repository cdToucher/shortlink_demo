document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('shortenForm');
    const longUrlInput = document.getElementById('longUrl');
    const resultDiv = document.getElementById('result');
    const shortUrlLink = document.getElementById('shortUrl');
    const originalUrlSpan = document.getElementById('originalUrl');
    const clickCountSpan = document.getElementById('clickCount');
    const createdAtSpan = document.getElementById('createdAt');
    const errorDiv = document.getElementById('error');
    const copyBtn = document.getElementById('copyBtn');
    
    // Handle form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const longUrl = longUrlInput.value.trim();
        
        if (!longUrl) {
            showError('Please enter a URL');
            return;
        }
        
        try {
            showLoading();
            
            const response = await fetch('/api/shorten', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ originalUrl: longUrl })
            });
            
            const data = await response.json();
            
            if (response.ok) {
                showResult(data);
            } else {
                showError(data.error || 'Failed to shorten URL');
            }
        } catch (error) {
            console.error('Error:', error);
            showError('An error occurred. Please try again.');
        }
    });
    
    // Handle copy button click
    copyBtn.addEventListener('click', function() {
        const shortUrl = shortUrlLink.textContent;
        
        navigator.clipboard.writeText(shortUrl)
            .then(() => {
                // Show temporary success message
                const originalText = copyBtn.textContent;
                copyBtn.textContent = 'Copied!';
                setTimeout(() => {
                    copyBtn.textContent = originalText;
                }, 2000);
            })
            .catch(err => {
                console.error('Failed to copy: ', err);
                alert('Failed to copy URL. Please try manually.');
            });
    });
    
    function showLoading() {
        resultDiv.classList.add('hidden');
        errorDiv.classList.add('hidden');
        // Disable form while processing
        document.querySelector('button[type="submit"]').disabled = true;
        document.querySelector('button[type="submit"]').textContent = 'Processing...';
    }
    
    function hideLoading() {
        document.querySelector('button[type="submit"]').disabled = false;
        document.querySelector('button[type="submit"]').textContent = 'Shorten URL';
    }
    
    function showResult(data) {
        shortUrlLink.textContent = data.shortUrl;
        shortUrlLink.href = data.shortUrl;
        originalUrlSpan.textContent = data.originalUrl;
        
        // Get stats for the shortened URL
        getStats(data.shortCode);
        
        resultDiv.classList.remove('hidden');
        errorDiv.classList.add('hidden');
        longUrlInput.value = '';
        
        hideLoading();
    }
    
    function showError(message) {
        errorDiv.querySelector('p').textContent = message;
        errorDiv.classList.remove('hidden');
        resultDiv.classList.add('hidden');
        hideLoading();
    }
    
    async function getStats(shortCode) {
        try {
            const response = await fetch(`/api/stats/${shortCode}`);
            
            if (response.ok) {
                const data = await response.json();
                clickCountSpan.textContent = data.clicks;
                createdAtSpan.textContent = new Date(data.createdAt).toLocaleString();
            } else {
                console.error('Failed to get stats');
            }
        } catch (error) {
            console.error('Error getting stats:', error);
        }
    }
});