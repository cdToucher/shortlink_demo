const express = require('express');
const path = require('path');
const crypto = require('crypto');
const fs = require('fs').promises;
const app = express();
const PORT = process.env.PORT || 8080;

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(express.static('public'));

// Simple file-based storage
const DATA_FILE = './links.json';

// Initialize data file if it doesn't exist
async function initStorage() {
  try {
    await fs.access(DATA_FILE);
  } catch (err) {
    await fs.writeFile(DATA_FILE, JSON.stringify({}));
  }
}

// Load links from file
async function loadLinks() {
  try {
    const data = await fs.readFile(DATA_FILE, 'utf8');
    return JSON.parse(data);
  } catch (err) {
    return {};
  }
}

// Save links to file
async function saveLinks(links) {
  await fs.writeFile(DATA_FILE, JSON.stringify(links, null, 2));
}

// Initialize storage
initStorage();

// Generate short code
function generateShortCode() {
  return crypto.randomBytes(4).toString('hex');
}

// Create short link
app.post('/api/shorten', async (req, res) => {
  try {
    const { originalUrl } = req.body;

    if (!originalUrl) {
      return res.status(400).json({ error: 'Original URL is required' });
    }

    // Load existing links
    const links = await loadLinks();

    // Check if URL already exists
    const existingShortCode = Object.keys(links).find(code => links[code].originalUrl === originalUrl);
    if (existingShortCode) {
      return res.json({ 
        shortCode: existingShortCode,
        shortUrl: `${req.protocol}://${req.get('host')}/${existingShortCode}`,
        originalUrl: links[existingShortCode].originalUrl
      });
    }

    // Generate unique short code
    let shortCode;
    let unique = false;
    while (!unique) {
      shortCode = generateShortCode();
      if (!links[shortCode]) {
        unique = true;
      }
    }

    // Create new short link
    links[shortCode] = {
      originalUrl,
      shortCode,
      clicks: 0,
      createdAt: new Date().toISOString()
    };

    await saveLinks(links);

    res.json({ 
      shortCode: shortCode,
      shortUrl: `${req.protocol}://${req.get('host')}/${shortCode}`,
      originalUrl: links[shortCode].originalUrl
    });
  } catch (error) {
    console.error('Error creating short link:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Redirect to original URL
app.get('/:shortCode', async (req, res) => {
  try {
    const { shortCode } = req.params;

    const links = await loadLinks();
    const shortLink = links[shortCode];
    
    if (!shortLink) {
      return res.status(404).send('Short link not found');
    }

    // Increment click count
    shortLink.clicks += 1;
    await saveLinks(links);

    res.redirect(shortLink.originalUrl);
  } catch (error) {
    console.error('Error redirecting:', error);
    res.status(500).send('Server error');
  }
});

// Get stats for a short link
app.get('/api/stats/:shortCode', async (req, res) => {
  try {
    const { shortCode } = req.params;

    const links = await loadLinks();
    const shortLink = links[shortCode];
    
    if (!shortLink) {
      return res.status(404).json({ error: 'Short link not found' });
    }

    res.json({
      shortCode: shortLink.shortCode,
      originalUrl: shortLink.originalUrl,
      clicks: shortLink.clicks,
      createdAt: shortLink.createdAt
    });
  } catch (error) {
    console.error('Error getting stats:', error);
    res.status(500).json({ error: 'Server error' });
  }
});

// Serve the main page
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'index.html'));
});

app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});