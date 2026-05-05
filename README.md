<div align="center">
# ResumeFit AI
 
**ATS resume optimization + builder · Java + React · AI-powered**
 
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-Java_17-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React_19-Vite-61DAFB?style=flat-square&logo=react&logoColor=black)
![AI](https://img.shields.io/badge/AI-Gemini_%7C_OpenAI-7F77DD?style=flat-square)
![Status](https://img.shields.io/badge/Status-Complete-brightgreen?style=flat-square)
 
> Parse, score, improve, and export job-ready resumes — with weighted ATS matching and AI refinement.
 
</div>
---
 
## Problem This Solves
 
Most resumes fail for two reasons — weak alignment to the job description, and poor structure that ATS tools can't parse. ResumeFit AI fixes both.
 
---
 
## Two Core Workflows
 
**Optimizer** — Upload resume → Add JD → Parse + Analyze → Score + Match → Refined Draft → Export
 
**Builder** — Enter details → Skills + Projects → Generate base → AI refinement → Live canvas → Export
 
---
 
## Tech Stack
 
| Layer | Technologies |
|---|---|
| **Frontend** | React 19, Vite, Axios, dark/light theme |
| **Backend** | Java 17, Spring Boot 3, Jakarta Validation |
| **Parsing** | Apache PDFBox (PDF), Apache POI (DOCX) |
| **AI Layer** | Gemini → OpenRouter → OpenAI → Rule-based fallback |
| **Export** | TXT, DOCX, ATS PDF, Minimal PDF, Template PDF |
 
---
 
## Match Scoring Logic
 
Not simple keyword counting — weighted by category:
 
| Category | Weight |
|---|---|
| Tools | 5 |
| Skills | 4 |
| Role signals | 3 |
| General terms | 1 |
 
```
score = matched_keyword_weight / total_JD_keyword_weight × 100
```
 
Easy to explain in interviews. Easy to defend to users.
 
---
 
## Backend Architecture
 
### Controllers
- `ResumeParsingController` — file uploads, text extraction
- `ResumeIntelligenceController` — match, suggestions, optimize, builder, export, versions
- `ResumeController` — tailor endpoint
### Services
- `ResumeParsingService` · `ResumeMatchingService` · `ResumeSuggestionService`
- `ResumeOptimizationService` · `ResumeBuilderService` · `ResumeVersionService`
- `ResumePdfExportService` · `ResumeExportService` · `ResumeSectionAssistService`
### Utility Layer
`KeywordExtractor` · `ResumeTextCleaner` · `ResumeSectionParser` · `ResumeSectionEditor` · `ResumeTruthGuard`
 
---
 
## API Endpoints
 
```
POST /api/resume/parse
POST /api/job-descriptions/analyze
POST /api/resume/match
POST /api/resume/suggestions
POST /api/resume/optimize
POST /api/resume/versions
POST /api/resume/tailor
POST /api/resume/builder/generate
POST /api/resume/builder/assist-section
POST /api/resume/export/docx
POST /api/resume/export/pdf/ats
POST /api/resume/export/pdf/minimal
POST /api/resume/export/pdf/template
```
 
---
 
## Run Locally
 
**Backend**
```bash
./mvnw spring-boot:run
# Windows: mvnw.cmd spring-boot:run
```
 
**Frontend**
```bash
npm install && npm run dev
```
 
**Env variables (prod)**
```
SPRING_PROFILES_ACTIVE=prod
APP_CORS_ALLOWED_ORIGIN=<frontend-url>
GEMINI_API_KEY / OPENROUTER_API_KEY / OPENAI_API_KEY
VITE_API_URL=<backend-url>
```
 
---
 
## What Makes This Interview-Ready
 
- Solves a real, common problem with a full end-to-end implementation
- Combines file parsing, weighted scoring, AI fallback, and multi-format export
- Deterministic logic + optional AI enhancement — both paths are explainable
- Truth-guard design prevents fake skill inflation — a deliberate architectural choice
→ [Interview Prep Guide](./INTERVIEW_PREP.md)
 
---
 
## Roadmap
 
- [x] Core optimizer + builder flows
- [x] Weighted ATS scoring
- [x] Multi-format export
- [ ] User auth + resume history
- [ ] Cover letter generation
---
 
<div align="center">
*Built with Java 17 · Spring Boot 3 · React 19 · No fake profiles generated.*
 
</div>
 
