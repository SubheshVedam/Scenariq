export type RouteOption = {
  id: string;
  name: string;
  totalMinutes: number;
  distanceKm: number;
  scenicScore: number;
  trafficImpactMinutes: number;
  fuelImpactPercent: number;
  comfortScore: number;
  description: string;
  highlights: string[];
  views: string[];
  thumbnail: string;
  googleMapsUrl: string;
};
