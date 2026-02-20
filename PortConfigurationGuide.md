# Konfiguration: port (config-fil → default)

Det här projektet väljer vilken port servern ska starta på enligt följande prioritet:
 
1. **Config-fil** (`application.yml` : `server.port`)  
2. **Default** (`8080`) - – används om port saknas i config eller om config-filen saknas


---

## 1) Default-värde

Om config-fil saknas, eller om `server.port` inte är satt, används:

- **8080** (default för `server.port` i `AppConfig`.)

---

### 2) Config-fil: `application.yml`

### Var ska filen ligga?
Standard:
- `src/main/resources/application.yml`


### Exempel

yaml server:
port:9090

---

## 3) Sammanfattning

Prioritet:

1. `application.yml` (`server.port`)
2. Default (`8080`)

---
