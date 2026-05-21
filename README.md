# MediSense

AI-powered health intelligence platform. Upload medical reports and hospital bills to get plain-language summaries, insurance claim verdicts, and a conversational health assistant — all powered by Anthropic Claude.

## Features

- **MediSummarize** — Upload a medical PDF and get a structured summary in your language (English, Hindi, Tamil, Telugu, Kannada, Malayalam, Bengali, Marathi). Ask follow-up questions about the report.
- **ClaimSense** — Upload hospital bill(s) against a policy to get a CLAIMABLE / PARTIAL / EXCLUDED verdict with line-item breakdown and patient-facing explanation.
- **MediChat** — Conversational assistant that handles medical reports, bill analysis, coverage questions, and general health queries in a persistent chat session.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18 + Vite + TypeScript |
| Backend | Spring Boot 3.5 + Spring AI 1.0 |
| LLM | Anthropic Claude (claude-sonnet) |
| PDF parsing | Apache PDFBox |
| Database | PostgreSQL 16 + Flyway |
| File storage | Local FS (dev) / AWS S3 (prod) |
| Containerization | Docker Compose |

## Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- Anthropic API key

## Getting Started

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL on port `5432` and MinIO on ports `9000`/`9001`.

### 2. Configure environment variables

```bash
export DB_URL=jdbc:postgresql://localhost:5432/medisense
export DB_USERNAME=medisense
export DB_PASSWORD=medisense
export ANTHROPIC_API_KEY=your_key_here
```

### 3. Run the backend

```bash
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`. Flyway migrations run automatically on startup.

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI starts on `http://localhost:5173` and proxies API calls to the backend.

## Supported Insurers (ClaimSense & MediChat)

| Insurer | Policy Tiers |
|---|---|
| Star Health | Silver, Gold |
| HDFC ERGO | Optima, MyHealth |
| Niva Bupa | Reassure, Companion |

## API Endpoints

| Method | Route | Description |
|---|---|---|
| GET | `/api/health` | Liveness probe |
| POST | `/api/summarize` | Summarize a medical PDF |
| POST | `/api/summarize/chat` | Follow-up chat on a summary |
| POST | `/api/claims/analyze` | Analyze bill(s) against a policy |
| GET | `/api/claims/policies` | List available policies |
| GET | `/api/claims/recent` | Recent claim results |
| GET | `/api/recent` | Recent summarize results |
| POST | `/api/chat/{sessionId}` | MediChat turn (multipart) |

## Architecture

See [`design/architecture.md`](design/architecture.md) for the full architecture diagram, agent catalogue, database schema, and data flow walkthroughs.