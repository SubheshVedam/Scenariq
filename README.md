# Scenariq

Discover India-first routes that trade a few minutes of drive time for memorable views. The project ships a Spring Boot backend that fabricates scenic options and a Next.js frontend that lets travelers compare each option before launching Google Maps with the detour already configured.

## Project layout

```
backend/   → Java 17 + Spring Boot service that exposes `/api/routes`
frontend/  → Next.js 14 app router UI that calls the backend and opens Google Maps links
```

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 18+ with npm

## Running the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. Query it directly:

```
curl "http://localhost:8080/api/routes?start=San+Francisco&end=Big+Sur"
```

## Running the frontend

```bash
cd frontend
npm install
npm run dev
```

The UI expects `NEXT_PUBLIC_API_BASE_URL` (defaults to `http://localhost:8080`). Place both apps locally and open `http://localhost:3000`.

## How it works

1. The backend holds a catalog of hand-picked Indian scenic corridors (Konkan coast, Western Ghats, Nilgiris, Thar desert, Himalayan bends). For any start/end provided, it estimates a base duration/distance and produces three candidate detours with transparent tradeoffs (time, scenic score, fuel impact, comfort).
2. The frontend lets the user enter points A and B, surfaces the options with highlights and badges, and provides an "Open in Maps" button. The button deep-links into Google Maps using the provided origin/destination, forcing the same detour via waypoints.

Both layers are framework-agnostic: swap the heuristic catalog or replace the UI fetcher without touching the integration surface.
