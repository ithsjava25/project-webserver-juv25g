# Konfiguration: port (CLI → config-fil → default)

Det här projektet väljer vilken port servern ska starta på enligt följande prioritet:

1. **CLI-argument** (högst prioritet)  
2. **Config-fil** (`ConnectionConfig.properties`)  
3. **Default** (`8080`) (lägst prioritet)

Det betyder: om du skickar `--port 80` så ska den alltid vinna, även om config-filen säger något annat.

---

## 1) Default-värde

Om varken CLI eller config-fil anger port används:

- `DEFAULT_PORT = 8080`

Detta finns i `ServerPortResolver`.

---

## 2) Config-fil (ConnectionConfig.properties)

### Var ska filen ligga?
Filen måste ligga i:

- `src/main/resources/ConnectionConfig.properties`

Allt som ligger i `src/main/resources` hamnar på **classpath** när du kör appen (både i IDE och från byggd JAR).  
Därför kan Java hitta filen via `getResourceAsStream(...)`.

### Innehåll
Exempel:
