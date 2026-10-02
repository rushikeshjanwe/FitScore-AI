# Fitscore AI - quick start (Windows / IntelliJ)

## One-time installs
- IntelliJ IDEA (Community is fine), JDK 17 or newer
- Docker Desktop (must be running)

## 1. Get a free Groq key
console.groq.com -> API Keys -> Create API Key. Copy the key (starts with `gsk_`).

## 2. Create the `.env` file
In this folder (the one containing `pom.xml`), copy `.env.example` to `.env` and put your key in it:

    GROQ_API_KEY=gsk_your_key_here

No spaces, no quotes. `.env` is git-ignored, so it will not be pushed to GitHub.

## 3. Start Postgres and Redis
Open a terminal in THIS folder (you should see docker-compose.yml when you run `dir`) and run:

    docker compose up -d postgres redis

Postgres runs on port 5433 and Redis on 6379.

## 4. Run the tests (optional but recommended)
    mvn test        (or right-click src/test in IntelliJ -> Run Tests)

## 5. Start the app
Open this folder in IntelliJ as a Maven project (File > Open > select the folder with pom.xml),
then run `FitscoreApplication`.
Wait for: Started FitscoreApplication

## 6. Try it
Open http://localhost:8080/swagger-ui.html
POST /api/fitscore -> Try it out -> choose a text-based resume PDF, paste a job description -> Execute.
Run the same request twice: the second one is faster because the extraction is cached in Redis.

## Troubleshooting
| Problem | Fix |
|---|---|
| "no configuration file provided: not found" | Wrong folder. Run `dir`; you must see docker-compose.yml. Check for a nested fitscore-ai\fitscore-ai folder. |
| Could not resolve placeholder GROQ_API_KEY | `.env` missing, in the wrong folder, or has no key. It must sit next to pom.xml. |
| 401 from the AI call | Key is wrong or has extra spaces. |
| 404 / model not found | Groq renamed the model. Check console.groq.com/docs/models and add GROQ_MODEL=<model> to .env |
| 429 | Free-tier rate limit. Wait a minute. |
| Postgres password/connection error | `docker compose down -v` then `docker compose up -d postgres redis` |
| Port 8080 / 6379 / 5433 in use | Stop the program using it, or change the port |

## Run everything in Docker instead
    docker compose up --build
(`.env` with GROQ_API_KEY is picked up by docker compose automatically.)
