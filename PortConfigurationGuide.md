# Konfiguration: port (CLI → config-fil → default)

Det här projektet väljer vilken port servern ska starta på enligt följande prioritet:

1. **CLI-argument** (`--port`) -högst prioritet  
2. **Config-fil** (`application.yml` : `server.port`)  
3. **Default** (`8080`) - lägst prioritet

Det betyder: om du skickar `--port 80` så ska den alltid vinna, även om config-filen säger något annat.

---

## 1) Default-värde

Om varken CLI eller config-fil anger port används:

- **8080** (default för `server.port` i `AppConfig`.)

---

### 2) Config-fil: `application.yml`

### Var ska filen ligga?
Standard:
- `src/main/resources/application.yml`

Allt i `src/main/resources` hamnar på **classpath** vid körning (IDE och byggd JAR), vilket gör att Java kan läsa filen via `getResourceAsStream(...)`.

**Exempel:**

yaml server: port:9090


---

### 3) CLI-argument

**Exempel:**

```bash 
java -jar app.jar --port 80
```

--- 

## 4) Sammanfattning

Prioritet:

1. CLI (`--port`)
2. `application.yml` (`server.port`)
3. Default (`8080`)



