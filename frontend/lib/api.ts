import type { RouteOption } from './types';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? 'http://localhost:8080';

export async function fetchRoutes(start: string, end: string): Promise<RouteOption[]> {
  const url = new URL('/api/routes', API_BASE);
  if (start) {
    url.searchParams.set('start', start);
  }
  if (end) {
    url.searchParams.set('end', end);
  }

  const response = await fetch(url.toString(), {
    headers: {
      'Content-Type': 'application/json'
    },
    cache: 'no-store'
  });

  if (!response.ok) {
    throw new Error('Unable to retrieve scenic routes');
  }

  return response.json();
}
