<<<<<<< HEAD
# Fitscore AI

Spring Boot backend that scores how well a resume (PDF) matches a job description.

**Flow:** resume PDF + JD text -> extract text (PDFBox) -> LLM extracts skills and years of experience (Spring AI structured output, cached in Redis) -> plain Java scoring -> save in PostgreSQL -> return score, matched skills, missing skills.

The LLM is used only for extraction. The score itself is calculated in Java, so the same input always gives the same result.

## Scoring
- Skill score (max 70) = matched JD skills / total JD skills x 70
- Experience score (max 30) = 30 if resume years >= required years, otherwise proportional
- Skill names are normalized ("Spring Boot" = "SpringBoot" = "spring-boot")

## AI provider
Spring AI's OpenAI client pointed at Groq's OpenAI-compatible API (free tier). The code only depends on Spring AI's `ChatClient`,
so switching to OpenAI or another provider is a dependency/config change, not a code change.

## Caching
Extracted profiles are cached in Redis under `fitscore:profile:<resume|jd>:<sha256 of text>` with a 24h TTL.
The same resume or JD text never hits the LLM twice. If Redis is down the app still works, it just skips the cache.

## Database
- `fit_score_results` with an index on `created_at` (history is newest-first)
- `matched_skills` / `missing_skills` indexed on `result_id`

## Stack
Java 17, Spring Boot 3.5, Spring AI 1.1, Groq (OpenAI-compatible), PostgreSQL 15, Redis 7, PDFBox, Swagger, Docker

## Setup
1. Create a free API key at console.groq.com
2. Copy `.env.example` to `.env` and paste the key (the app reads it from the project folder; an environment variable also works)

## Run (everything in Docker)
1. `docker compose up --build`
2. Open http://localhost:8080/swagger-ui.html

## Run (app from IntelliJ / Maven)
1. `docker compose up -d postgres redis` (Postgres is exposed on host port 5433 to avoid clashing with a local install)
2. Make sure `.env` contains `GROQ_API_KEY`
3. Run `FitscoreApplication` or `mvn spring-boot:run`

## API
| Method | Path | Description |
|---|---|---|
| POST | /api/fitscore | multipart: `resume` (PDF), `jobDescription` (text) |
| GET | /api/fitscore?page=0&size=10 | scan history, newest first |
| GET | /api/fitscore/{id} | one saved result |

## Tests
`mvn test` - scoring logic, service flow with a mocked LLM, cache key behaviour. No API key needed.

## Possible next steps
Async processing, embedding-based semantic matching.
=======

>>>>>>> 0df3403559969c582e35b02df4726f5c4dbf59a1
