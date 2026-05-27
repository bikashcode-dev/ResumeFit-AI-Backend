# ResumeFit AI

ResumeFit AI is a full-stack resume optimization platform for job seekers who want to compare a resume against a job description, understand ATS gaps, improve sections, build a resume from structured details, and export recruiter-ready files.

The project has two main apps:

- Frontend: React + Vite app in `frontend resume builder/resumefit-ai`
- Backend: Spring Boot API in `backend resume builder`

No secrets or API keys are stored in this README. Use environment variables for all sensitive values.

## Project Structure

```text
resume-builder/
  backend resume builder/          Spring Boot backend API
  frontend resume builder/
    resumefit-ai/                  React + Vite frontend
  output/                          Local generated artifacts, not required for deploy
  tmp/                             Local helper scripts/artifacts, not required for deploy
```

## Main Features

- Resume upload and parsing for PDF/DOCX files.
- Job description analysis with keyword, tool, responsibility, and role-signal extraction.
- ATS-style resume matching with score breakdown, matched keywords, missing keywords, skill gaps, and truth/credibility checks.
- Resume optimizer that creates an improved draft while keeping content editable.
- Section editor with AI section improvement support.
- Resume builder with profile, skills, projects, education, experience, certifications, achievements, and custom sections.
- Reorder controls for skill groups, resume sections, projects, and experience entries.
- Before/after comparison for optimized drafts.
- Export support for DOCX, ATS PDF, minimal PDF, and plain text.
- Local browser session persistence for draft, optimizer state, generated resume, and editor flow.

## Frontend

Path:

```text
frontend resume builder/resumefit-ai
```

Tech stack:

- React 18
- Vite 5
- React Router
- Axios
- Lucide React icons
- CSS design system in `src/index.css`

Important frontend folders:

```text
src/api/            Backend API client
src/app/            Routes and app context
src/components/     Layout, resume preview, optimizer/editor UI, shared UI
src/hooks/          Local/session history helpers
src/pages/          Landing, dashboard, optimizer, builder, editor, exports, history
src/utils/          Normalizers, export helpers, resume helpers, diff/reorder helpers
```

### Frontend Setup

Install dependencies:

```bash
npm install
```

Run locally:

```bash
npm run dev
```

Build for production:

```bash
npm run build
```

Preview production build locally:

```bash
npm run preview
```

### Frontend Environment

Create a local `.env` file only when you need to override the API URL:

```env
VITE_API_URL=https://resumefit-ai-backend.onrender.com
```

Do not commit `.env` files. The repo already ignores environment files.

### Frontend Deployment

Recommended Netlify settings:

```text
Build command: npm run build
Publish directory: dist
```

For SPA routing, `public/_redirects` contains:

```text
/* /index.html 200
```

The current production frontend API fallback points to:

```text
https://resumefit-ai-backend.onrender.com
```

If the frontend domain changes, update backend CORS allowed origins before testing upload/API calls from the browser.

## Backend

Path:

```text
backend resume builder
```

Tech stack:

- Java 17
- Spring Boot 3.3.5
- Maven
- Lombok
- PDFBox for PDF parsing
- Apache POI for DOCX parsing/export support
- Dockerfile for Render-style deployment

Important backend folders:

```text
src/main/java/com/resumefit/controller/     REST controllers
src/main/java/com/resumefit/dto/            Request/response DTOs
src/main/java/com/resumefit/service/        Business logic and AI/fallback services
src/main/java/com/resumefit/util/           Parsing, cleaning, section editing, truth guard utilities
src/main/resources/                         Spring configuration
```

### Backend Setup

Run locally with Maven wrapper:

```bash
./mvnw spring-boot:run
```

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Build the backend:

```bash
./mvnw clean package
```

On Windows:

```powershell
.\mvnw.cmd clean package
```

If Maven clean fails because `target/` is locked, stop any running backend Java process and rerun the command.

### Backend Environment

Use environment variables for all sensitive configuration:

```env
SERVER_PORT=8080
APP_CORS_ALLOWED_ORIGINS=https://your-frontend-domain.netlify.app,http://localhost:5173
GEMINI_API_KEY=
GEMINI_MODEL=gemini-2.5-flash
OPENROUTER_API_KEY=
OPENROUTER_MODEL=openai/gpt-4o-mini
LOCAL_AI_ENABLED=false
LOCAL_AI_URL=http://localhost:11434
```

Never commit real API keys. Keep real values only in local environment files or deployment provider environment settings.

### Backend Deployment

The backend includes a Dockerfile:

```text
backend resume builder/Dockerfile
```

The Docker image:

- Builds with Maven.
- Runs Java 17.
- Uses the `prod` Spring profile.
- Reads the runtime port from `PORT`, falling back to `8080`.

For Render, set environment variables in the Render dashboard. At minimum, configure:

```text
APP_CORS_ALLOWED_ORIGINS
GEMINI_API_KEY or OPENROUTER_API_KEY, if AI provider output is required
```

If no AI provider key is configured, backend services can still use deterministic fallback behavior where implemented.

## API Overview

Base path:

```text
/api
```

Health:

```http
GET /api/health
```

Resume parsing:

```http
POST /api/resume/parse
Content-Type: multipart/form-data
Field: file
Supported files: PDF, DOCX
```

Job description analysis:

```http
POST /api/job-descriptions/analyze
```

Required JSON:

```json
{
  "jobDescription": "Full job description text..."
}
```

Resume match:

```http
POST /api/resume/match
```

Important fields:

- `resumeText`
- `jobDescription`
- `skills`
- `candidateStage`

Resume suggestions:

```http
POST /api/resume/suggestions
```

Uses the same core request shape as resume match.

Resume optimization:

```http
POST /api/resume/optimize
```

Important fields:

- `resumeText`
- `jobDescription`
- `skills`
- `roleType`
- `candidateStage`

Resume versions:

```http
POST /api/resume/versions
```

Important fields:

- `resumeText`
- `roleType`
- `jobDescription`
- `skills`
- `candidateStage`

Builder generation:

```http
POST /api/resume/builder/generate
```

Important required fields:

- `fullName`
- `skills`
- `roleType`
- `candidateLevel`

Section assist:

```http
POST /api/resume/builder/assist-section
```

Required fields:

- `sectionType`
- `currentContent`
- `roleType`
- `candidateLevel`

Exports:

```http
POST /api/resume/export/docx
POST /api/resume/export/pdf/{style}
```

Supported PDF styles:

- `ats`
- `minimal`
- `template`

Export request fields:

- `resumeText`
- `fileName`
- `documentTitle`
- `templateProfile`

## CORS Notes

The browser can only call the backend if the frontend origin is allowed by backend CORS configuration.

Update this environment variable when the deployed frontend domain changes:

```text
APP_CORS_ALLOWED_ORIGINS
```

Example:

```text
https://ai-resume-builder-cv-match.netlify.app,http://localhost:5173
```

After changing CORS settings, restart or redeploy the backend.

## Common Issues

`Cannot reach the backend`

Usually caused by one of these:

- Backend is sleeping or still starting on Render.
- Backend deployment failed.
- Frontend domain is missing from `APP_CORS_ALLOWED_ORIGINS`.
- Network request is blocked by browser CORS.

`Only PDF and DOCX resume files are supported`

The backend parser supports PDF and DOCX only. TXT upload is not supported.

`Job description must be between 80 and 12000 characters`

The backend validates job descriptions before analysis and matching. Paste the full job description, not only a title.

`Resume text must be between 80 and 12000 characters`

The parsed resume text is too short or too large. Scanned/image-only PDFs may not contain readable text.

`Maven clean fails on target folder`

Stop any running Java backend process, then rerun Maven. Windows may lock files inside `target/`.

## Local Artifacts

The root `output/` and `tmp/` folders are local helper artifacts. They are not required for frontend or backend deployment.

Example:

```text
tmp/pdfs/generate_sample_resume_pdf.py
```

This script generates a local sample PDF for testing and is not part of the production app.

## Security Notes

- Do not commit `.env`, `.env.local`, API keys, or provider secrets.
- Do not put real candidate resumes into the repository.
- Keep AI provider keys in deployment environment variables.
- Keep CORS origins limited to trusted frontend domains.
- Treat uploaded resumes and job descriptions as user data.

## Useful Commands

Frontend:

```bash
cd "frontend resume builder/resumefit-ai"
npm install
npm run dev
npm run build
```

Backend:

```bash
cd "backend resume builder"
./mvnw spring-boot:run
./mvnw clean package
```

Windows backend:

```powershell
cd "backend resume builder"
.\mvnw.cmd spring-boot:run
.\mvnw.cmd clean package
```

## Current Production URLs

Frontend:

```text
https://ai-resume-builder-cv-match.netlify.app
```

Backend:

```text
https://resumefit-ai-backend.onrender.com
```

These are public service URLs only. Sensitive provider keys are intentionally not documented here.
