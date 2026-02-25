# Konfiguration: port (CLI → config-fil → default)

Det här projektet väljer vilken port servern ska starta på enligt följande prioritet:

1. **CLI-argument** (`--port <port>`) – högst prioritet
2. **Config-fil** (`application.yml`: `server.port`)
3. **Default** (`8080`) – används om port saknas i config eller om config-filen saknas

---

## 1) Default-värde

Om varken CLI eller config anger port används:

- **8080** (default för `server.port` i `AppConfig`)

---

## 2) Config-fil: `application.yml`

### Var ska filen ligga?
Standard:
- `src/main/resources/application.yml`

### Exempel
```yaml
server:
port: 9090
```

---

## 3) CLI-argument

CLI kan användas för att override:a config:

```bash
java -cp target/classes org.example.App --port 8000
```

---

## 4) Sammanfattning

Prioritet:

1. CLI (`--port`)
2. `application.yml` (`server.port`)
3. Default (`8080`)