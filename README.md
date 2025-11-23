# Scenariq

Discover India-first routes that trade a few minutes of drive time for memorable views. The project ships a Spring Boot backend that calls the Google Directions API for real routing data and a Next.js frontend that lets travelers compare each option before launching Google Maps with the detour already configured.

## Project layout

```
backend/   → Java 17 + Spring Boot service that exposes `/api/routes`
frontend/  → Next.js 14 app router UI that calls the backend and opens Google Maps links
```

## Requirements

- Java 17+
- Maven 3.9+
- Node.js 18+ with npm

## Google Maps API setup

1. Create a Google Cloud project (or reuse an existing one) and enable the **Directions API**.
2. Create an API key that is allowed to call the Directions API.
3. Copy `backend/.env.example` to `backend/.env` and place your key there, or export it manually before running:  
   `export GOOGLE_MAPS_API_KEY=your-key-here`

## Running the backend

```bash
cd backend
# load env vars if you stored them in backend/.env
# set -a; source .env; set +a
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

1. The backend proxies the Google Directions API with `alternatives=true`, grabs the top three driving routes, and derives trade-off metadata (time deltas, distance deltas, heuristic scenic/comfort scores) plus a Google Maps deep-link anchored to coordinates along each path.
2. The frontend lets the user enter points A and B, surfaces the options with highlights and badges, and provides an "Open in Maps" button. The button deep-links into Google Maps using the provided origin/destination with preserved waypoints taken from the underlying Google route.

Both layers are framework-agnostic: swap the heuristic catalog or replace the UI fetcher without touching the integration surface.
