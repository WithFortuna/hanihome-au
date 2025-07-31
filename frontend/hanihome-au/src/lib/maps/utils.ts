/**
 * Google Maps Utility Functions
 */

import { MapPosition, MapBounds, AddressComponent } from './types';

/**
 * Calculate distance between two points using Haversine formula
 */
export function calculateDistance(
  point1: MapPosition,
  point2: MapPosition
): number {
  const R = 6371; // Earth's radius in kilometers
  const dLat = toRadians(point2.lat - point1.lat);
  const dLng = toRadians(point2.lng - point1.lng);
  
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(point1.lat)) *
      Math.cos(toRadians(point2.lat)) *
      Math.sin(dLng / 2) *
      Math.sin(dLng / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Convert degrees to radians
 */
function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Check if a point is within bounds
 */
export function isWithinBounds(point: MapPosition, bounds: MapBounds): boolean {
  return (
    point.lat >= bounds.south &&
    point.lat <= bounds.north &&
    point.lng >= bounds.west &&
    point.lng <= bounds.east
  );
}

/**
 * Create bounds from center point and radius
 */
export function createBoundsFromRadius(
  center: MapPosition,
  radiusKm: number
): MapBounds {
  const latOffset = radiusKm / 111; // Approximate km per degree latitude
  const lngOffset = radiusKm / (111 * Math.cos(toRadians(center.lat)));

  return {
    north: center.lat + latOffset,
    south: center.lat - latOffset,
    east: center.lng + lngOffset,
    west: center.lng - lngOffset,
  };
}

/**
 * Extract address components from Google Places result
 */
export function extractAddressComponents(
  addressComponents: AddressComponent[]
): {
  streetNumber?: string;
  route?: string;
  locality?: string;
  administrativeAreaLevel1?: string;
  postalCode?: string;
  country?: string;
} {
  const components: Record<string, string> = {};

  addressComponents.forEach((component) => {
    const types = component.types;
    
    if (types.includes('street_number')) {
      components.streetNumber = component.long_name;
    } else if (types.includes('route')) {
      components.route = component.long_name;
    } else if (types.includes('locality')) {
      components.locality = component.long_name;
    } else if (types.includes('administrative_area_level_1')) {
      components.administrativeAreaLevel1 = component.short_name;
    } else if (types.includes('postal_code')) {
      components.postalCode = component.long_name;
    } else if (types.includes('country')) {
      components.country = component.long_name;
    }
  });

  return components;
}

/**
 * Format address for display
 */
export function formatAddress(
  streetNumber?: string,
  route?: string,
  locality?: string,
  state?: string,
  postalCode?: string
): string {
  const parts = [];
  
  if (streetNumber && route) {
    parts.push(`${streetNumber} ${route}`);
  } else if (route) {
    parts.push(route);
  }
  
  if (locality) {
    parts.push(locality);
  }
  
  if (state) {
    parts.push(state);
  }
  
  if (postalCode) {
    parts.push(postalCode);
  }
  
  return parts.join(', ');
}

/**
 * Generate a unique marker ID
 */
export function generateMarkerId(prefix: string = 'marker'): string {
  return `${prefix}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
}

/**
 * Debounce function for map events
 */
export function debounce<T extends (...args: unknown[]) => void>(
  func: T,
  wait: number
): T {
  let timeout: NodeJS.Timeout;
  
  return ((...args: Parameters<T>) => {
    clearTimeout(timeout);
    timeout = setTimeout(() => func(...args), wait);
  }) as T;
}