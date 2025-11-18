'use client';

import { FormEvent, useState } from 'react';

import { RouteCard } from '@/components/RouteCard';
import { fetchRoutes } from '@/lib/api';
import type { RouteOption } from '@/lib/types';

const defaultStart = 'Mumbai';
const defaultEnd = 'Goa';

export default function HomePage() {
  const [start, setStart] = useState(defaultStart);
  const [end, setEnd] = useState(defaultEnd);
  const [routes, setRoutes] = useState<RouteOption[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setIsLoading(true);
    setError(null);
    try {
      const options = await fetchRoutes(start, end);
      setRoutes(options);
    } catch (err) {
      console.error(err);
      setError('Could not load scenic suggestions. Try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const openMaps = (url: string) => {
    window.open(url, '_blank');
  };

  return (
    <main>
      <section className="hero">
        <p>Scenariq · India scenic detour curator</p>
        <h1>Swap a short detour for Sahyadri peaks and palm-lined highways</h1>
        <p>Pick a route across India, then jump straight into Google Maps with the detour already queued.</p>
      </section>
      <form className="search-panel" onSubmit={handleSubmit}>
        <label>
          Start
          <input value={start} onChange={(event) => setStart(event.target.value)} placeholder="Origin" required />
        </label>
        <label>
          Destination
          <input value={end} onChange={(event) => setEnd(event.target.value)} placeholder="Destination" required />
        </label>
        <label>
          Mode
          <select defaultValue="driving" disabled>
            <option value="driving">Driving</option>
            <option value="cycling">Cycling</option>
          </select>
        </label>
        <div className="actions">
          <button type="submit">{isLoading ? 'Searching…' : 'Find scenic routes'}</button>
        </div>
      </form>

      {error && <p role="alert">{error}</p>}

      <section className="routes-grid">
        {routes.map((route) => (
          <RouteCard key={route.id} route={route} onLaunch={openMaps} />
        ))}
        {!routes.length && !isLoading && (
          <p style={{ textAlign: 'center', color: '#7d89c2' }}>
            Enter your endpoints and we will surface a few curated detours.
          </p>
        )}
      </section>
    </main>
  );
}
