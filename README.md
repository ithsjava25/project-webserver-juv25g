# ⚡ HTTP Web Server ⚡
### **Class JUV25G** | Lightweight • Configurable • Secure

<div align="center">

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=openjdk)
![HTTP](https://img.shields.io/badge/HTTP-1.1-blue?style=for-the-badge)
![Status](https://img.shields.io/badge/Status-Active-success?style=for-the-badge)

*A modern, high-performance HTTP web server built from scratch in Java*

[Features](#features) • [Quick Start](#quick-start) • [Configuration](#configuration) • [Documentation](#documentation)

---

</div>

## ✨ Features

- 🚀 **High Performance** - Virtual threads for handling thousands of concurrent connections
- 📁 **Static File Serving** - HTML, CSS, JavaScript, images, PDFs, fonts, and more
- 🎨 **Smart MIME Detection** - Automatic Content-Type headers for 20+ file types
- ⚙️ **Flexible Configuration** - YAML or JSON config files with sensible defaults
- 🔒 **Security First** - Path traversal protection and input validation
- 🐳 **Docker Ready** - Multi-stage Dockerfile for easy deployment
- ⚡ **HTTP/1.1 Compliant** - Proper status codes, headers, and responses
- 🎯 **Custom Error Pages** - Branded 404 pages and error handling

## 📋 Requirements

| Tool | Version | Purpose |
|------|---------|---------|
| ☕ **Java** | 21+ | Runtime environment |
| 📦 **Maven** | 3.6+ | Build tool |
| 🐳 **Docker** | Latest | Optional - for containerization |

##  Quick Start

```
┌─────────────────────────────────────────────┐
│  Ready to launch your web server?          │
│  Follow these simple steps!                │
└─────────────────────────────────────────────┘
```

### 1. Clone the repository
```bash
git clone <repository-url>
cd project-webserver
```

### 2. Build the project
```bash
mvn clean compile
```

### 3. Run the server

**Option A: Run directly with Maven (recommended for development)**
```bash
mvn exec:java@run
```

**Option B: Run compiled classes directly**
```bash
mvn clean compile
java -cp target/classes org.example.App
```

**Option C: Using Docker**
```bash
docker build -t webserver .
docker run -p 8080:8080 -v $(pwd)/www:/www webserver
```

The server will start on the default port **8080** and serve files from the `www/` directory.

### 4. Access in browser
Open your browser and navigate to:
```
http://localhost:8080
```

## Configuration

The server can be configured using a YAML or JSON configuration file located at:
```
src/main/resources/application.yml
```

### Configuration File Format (YAML)

```yaml
server:
  port: 8080
  rootDir: "./www"

logging:
  level: "INFO"
```

### Configuration File Format (JSON)

```json
{
  "server": {
    "port": 8080,
    "rootDir": "./www"
  },
  "logging": {
    "level": "INFO"
  }
}
```

### Configuration Options

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `server.port` | Integer | `8080` | Port number the server listens on (1-65535) |
| `server.rootDir` | String | `"./www"` | Root directory for serving static files |
| `logging.level` | String | `"INFO"` | Logging level (INFO, DEBUG, WARN, ERROR) |

### Default Values

If no configuration file is provided or values are missing, the following defaults are used:

- **Port:** 8080
- **Root Directory:** ./www
- **Logging Level:** INFO

## Directory Structure

```
project-webserver/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       ├── App.java                  # Main entry point
│   │   │       ├── TcpServer.java            # TCP server implementation
│   │   │       ├── ConnectionHandler.java    # HTTP request handler
│   │   │       ├── StaticFileHandler.java    # Static file server
│   │   │       ├── config/                   # Configuration classes
│   │   │       ├── http/                     # HTTP response builder & MIME detection
│   │   │       ├── httpparser/               # HTTP request parser
│   │   │       └── filter/                   # Filter chain (future feature)
│   │   └── resources/
│   │       └── application.yml               # Configuration file
│   └── test/                                 # Unit tests
├── www/                                      # Web root (static files)
│   ├── index.html
│   ├── pageNotFound.html                     # Custom 404 page
│   └── ...                                   # Other static files
├── pom.xml
└── README.md
```

## Serving Static Files

Place your static files in the `www/` directory (or the directory specified in `server.rootDir`).

### Supported File Types

The server automatically detects and serves the correct `Content-Type` for:

**Text & Markup:**
- HTML (`.html`, `.htm`)
- CSS (`.css`)
- JavaScript (`.js`)
- JSON (`.json`)
- XML (`.xml`)
- Plain text (`.txt`)

**Images:**
- PNG (`.png`)
- JPEG (`.jpg`, `.jpeg`)
- GIF (`.gif`)
- SVG (`.svg`)
- WebP (`.webp`)
- ICO (`.ico`)

**Documents:**
- PDF (`.pdf`)

**Fonts:**
- WOFF (`.woff`)
- WOFF2 (`.woff2`)
- TrueType (`.ttf`)
- OpenType (`.otf`)

**Media:**
- MP4 video (`.mp4`)
- WebM video (`.webm`)
- MP3 audio (`.mp3`)
- WAV audio (`.wav`)

Unknown file types are served as `application/octet-stream`.

## URL Handling

The server applies the following URL transformations:

| Request URL | Resolved File |
|-------------|---------------|
| `/` | `index.html` |
| `/about` | `about.html` |
| `/contact` | `contact.html` |
| `/styles.css` | `styles.css` |
| `/page.html` | `page.html` |

**Note:** URLs ending with `/` are resolved to `index.html`, and URLs without an extension get `.html` appended automatically.

## Error Pages

### 404 Not Found

If a requested file doesn't exist, the server returns:
1. `pageNotFound.html` (if it exists in the web root)
2. Otherwise: Plain text "404 Not Found"

To customize your 404 page, create `www/pageNotFound.html`.

### 403 Forbidden

Returned when a path traversal attack is detected (e.g., `GET /../../etc/passwd`).

## Security Features

### Path Traversal Protection

The server validates all file paths to prevent directory traversal attacks:

```
✅ Allowed:  /index.html
✅ Allowed:  /docs/guide.pdf
❌ Blocked:  /../../../etc/passwd
❌ Blocked:  /www/../../../secret.txt
```

All blocked requests return `403 Forbidden`.

## Running Tests

```bash
mvn test
```

Test coverage includes:
- HTTP request parsing
- Response building
- MIME type detection
- Configuration loading
- Static file serving
- Path traversal security

## Building for Production

### Using Docker (recommended)

```bash
docker build -t webserver .
docker run -d -p 8080:8080 -v $(pwd)/www:/www --name my-webserver webserver
```

### Running on a server

```bash
# Compile the project
mvn clean compile

# Run with nohup for background execution
nohup java -cp target/classes org.example.App > server.log 2>&1 &

# Or use systemd (create /etc/systemd/system/webserver.service)
```

## Examples

### Example 1: Serving a Simple Website

**Directory structure:**
```
www/
├── index.html
├── styles.css
├── app.js
└── images/
    └── logo.png
```

**Access:**
- Homepage: `http://localhost:8080/`
- Stylesheet: `http://localhost:8080/styles.css`
- Logo: `http://localhost:8080/images/logo.png`

### Example 2: Custom Port

**application.yml:**
```yaml
server:
  port: 3000
  rootDir: "./public"
```

Access at: `http://localhost:3000/`

### Example 3: Different Web Root

**application.yml:**
```yaml
server:
  rootDir: "./dist"
```

Server will serve files from `dist/` instead of `www/`.

## Architecture

### Request Flow

1. **TcpServer** accepts incoming TCP connections
2. **ConnectionHandler** creates a virtual thread for each request
3. **HttpParser** parses the HTTP request line and headers
4. **StaticFileHandler** resolves the file path and reads the file
5. **HttpResponseBuilder** constructs the HTTP response with correct headers
6. Response is written to the client socket

### Filter Chain (Future Feature)

The project includes a filter chain interface for future extensibility:
- Request/response filtering
- Authentication
- Logging
- Compression

## Troubleshooting

### Port already in use
```
Error: Address already in use
```
**Solution:** Change the port in `application.yml` or kill the process using port 8080:
```bash
# Linux/Mac
lsof -ti:8080 | xargs kill -9

# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

### File not found but file exists
**Solution:** Check that the file is in the correct directory (`www/` by default) and that the filename matches exactly (case-sensitive on Linux/Mac).

### Binary files (images/PDFs) are corrupted
**Solution:** This should not happen with the current implementation. The server uses `byte[]` internally to preserve binary data. If you see this issue, please report it as a bug.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -m 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Open a Pull Request

<div align="center">

### 👨‍💻 Built by Class JUV25G

**Made with ❤️ and ☕ in Sweden**
</div>
