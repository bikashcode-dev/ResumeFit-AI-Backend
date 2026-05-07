<div align="center">

# ResumeFit AI — Backend

**Spring Boot service for resume parsing, ATS matching, optimization, builder, and export**

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=flat-square)

</div>

---

## What It Does

| Capability | Details |
|---|---|
| **Resume Parsing** | Upload PDF or DOCX → extract clean text, detect sections |
| **JD Analysis** | Parse job descriptions, extract ATS keywords |
| **ATS Matching** | Weighted keyword score against JD requirements |
| **Suggestions** | Section-level improvement recommendations |
| **Optimization** | AI-powered resume refinement |
| **Builder** | New resume from structured profile input |
| **Role Versions** | JD-targeted resume variants |
| **Export** | DOCX, ATS PDF, Minimal PDF, Template PDF |

---

## Tech Stack

- **Java 17** · Spring Boot 3 · Maven
- **Apache PDFBox** — PDF parsing
- **Apache POI** — DOCX read/write
- **Spring Validation** — request validation
- **AI** — Gemini · OpenRouter · OpenAI · rule-based fallback

---

## Request Flow

```
Upload resume + JD  →  Parse + Analyze  →  Match + Score  →  Optimize + Draft  →  Export
```

---

## AI Fallback Chain

```
Gemini  →  OpenRouter  →  OpenAI  →  Rule-based (always available)
```

If a provider fails, the backend automatically moves to the next. Rule-based fallback ensures the service never goes down.

### Resume Parsing
**Built with  by [Bikash Kumar](https://github.com/bikashcode-dev)**

### Intelligence
```
POST /api/resume/match
POST /api/resume/suggestions
POST /api/resume/optimize
POST /api/resume/versions
```

### Builder
```
POST /api/resume/builder/generate
POST /api/resume/builder/assist-section
```

### Export
```
POST /api/resume/export/docx
POST /api/resume/export/pdf/{style}
```

---

## Project Structure

```
src/main/java/com/resumefit/
├── config/        CORS, beans, AI provider config
├── controller/    REST endpoints — parsing, intelligence, builder, export
├── dto/           Request and response models
├── exception/     Global error handling
├── service/       Business logic — match, optimize, build, export
└── util/          KeywordExtractor · TextCleaner · SectionParser

src/main/resources/
├── application.properties
├── application-dev.properties
└── application-prod.properties
```

---

## Run Locally

```bash
# Linux / Mac
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Dev — `application-dev.properties`
```
GEMINI_API_KEY=
OPENROUTER_API_KEY=
OPENAI_API_KEY=
```

### Prod — environment variables
```
SPRING_PROFILES_ACTIVE=prod
APP_CORS_ALLOWED_ORIGIN=<frontend-url>
GEMINI_API_KEY=
OPENROUTER_API_KEY=
OPENAI_API_KEY=
```

---

## Deployment

| Layer | Recommended |
|---|---|
| Backend | Render · Railway |
| Frontend | Netlify · Vercel |
