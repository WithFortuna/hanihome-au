/**
 * Google Maps API Configuration
 */

export const GOOGLE_MAPS_CONFIG = {
  apiKey: process.env.NEXT_PUBLIC_GOOGLE_MAPS_API_KEY || '',
  libraries: ['places', 'geometry', 'drawing'] as const,
  language: 'en',
  region: 'AU',
} as const;

export const MAP_OPTIONS: google.maps.MapOptions = {
  zoom: 12,
  center: { lat: -37.8136, lng: 144.9631 }, // Melbourne, Australia
  mapTypeId: google.maps.MapTypeId.ROADMAP,
  gestureHandling: 'auto', // Changed from 'cooperative' for better mobile experience
  zoomControl: true,
  mapTypeControl: false,
  scaleControl: true,
  streetViewControl: false,
  rotateControl: false,
  fullscreenControl: true,
  styles: [
    {
      featureType: 'poi',
      elementType: 'labels',
      stylers: [{ visibility: 'off' }],
    },
  ],
};

// Mobile-specific map options
export const MOBILE_MAP_OPTIONS: Partial<google.maps.MapOptions> = {
  gestureHandling: 'greedy', // Allows single-finger panning on mobile
  zoomControl: false, // Hide default zoom controls on mobile
  scaleControl: false, // Hide scale control on mobile for more space
  fullscreenControl: false, // Hide fullscreen on mobile
  streetViewControl: false,
  mapTypeControl: false,
  rotateControl: false,
};

// Desktop-specific map options
export const DESKTOP_MAP_OPTIONS: Partial<google.maps.MapOptions> = {
  gestureHandling: 'cooperative',
  zoomControl: true,
  scaleControl: true,
  fullscreenControl: true,
};

export const AUSTRALIA_BOUNDS = {
  north: -10.0,
  south: -44.0,
  east: 154.0,
  west: 112.0,
};

export const MAJOR_CITIES = {
  melbourne: { lat: -37.8136, lng: 144.9631 },
  sydney: { lat: -33.8688, lng: 151.2093 },
  brisbane: { lat: -27.4698, lng: 153.0251 },
  perth: { lat: -31.9505, lng: 115.8605 },
  adelaide: { lat: -34.9285, lng: 138.6007 },
  canberra: { lat: -35.2809, lng: 149.1300 },
  darwin: { lat: -12.4634, lng: 130.8456 },
  hobart: { lat: -42.8821, lng: 147.3272 },
};