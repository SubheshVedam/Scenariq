'use client';

import type { RouteOption } from '@/lib/types';

type Props = {
  route: RouteOption;
  onLaunch: (url: string) => void;
};

export function RouteCard({ route, onLaunch }: Props) {
  const scenicPercent = Math.min(100, Math.round(route.scenicScore * 10));
  const comfortPercent = Math.min(100, route.comfortScore * 10);
  const detourPercent = Math.min(100, (route.trafficImpactMinutes / route.totalMinutes) * 100);

  return (
    <article className="route-card">
      <div
        className="preview"
        style={{
          backgroundImage: `linear-gradient(180deg, rgba(0, 0, 0, 0) 30%, rgba(0, 0, 0, 0.75)), url(${route.thumbnail})`,
          backgroundSize: 'cover',
          backgroundPosition: 'center'
        }}
      />
      <div>
        <div className="route-meta">
          <span>{route.distanceKm.toFixed(1)} km total</span>
          <span>{route.totalMinutes} min door-to-door</span>
          <span className="score-pill">Scenic score {route.scenicScore.toFixed(1)}</span>
        </div>
        <h3>{route.name}</h3>
        <p>{route.description}</p>
        <div className="badges">
          {route.views.map((view) => (
            <span key={view} className="badge">
              {view}
            </span>
          ))}
        </div>
        <div className="tradeoff-grid">
          <TradeoffCard label="Scenic" value={`${route.scenicScore.toFixed(1)}/10`} percent={scenicPercent} tone="#7be4ff" />
          <TradeoffCard label="Comfort" value={`${route.comfortScore}/10`} percent={comfortPercent} tone="#c0f28e" />
          <TradeoffCard label="Time impact" value={`+${route.trafficImpactMinutes} min`} percent={detourPercent} tone="#f8d66d" />
          <TradeoffCard label="Fuel impact" value={`+${route.fuelImpactPercent.toFixed(1)} %`} percent={Math.min(100, route.fuelImpactPercent)} tone="#f58ad0" />
        </div>
        <ul>
          {route.highlights.map((highlight) => (
            <li key={highlight}>{highlight}</li>
          ))}
        </ul>
        <div className="route-footer">
          <span>Opens Google Maps with this detour</span>
          <button type="button" onClick={() => onLaunch(route.googleMapsUrl)}>
            Open in Maps
          </button>
        </div>
      </div>
    </article>
  );
}

type TradeoffProps = {
  label: string;
  value: string;
  percent: number;
  tone: string;
};

function TradeoffCard({ label, value, percent, tone }: TradeoffProps) {
  return (
    <div className="tradeoff-card">
      <strong>{label}</strong>
      <span>{value}</span>
      <div className="progress-bar" aria-hidden="true">
        <div className="progress-value" style={{ width: `${percent}%`, background: tone }} />
      </div>
    </div>
  );
}
