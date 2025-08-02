/**
 * Viewport-based Map Optimization Utilities
 */

import { PropertyMarker, MapBounds, MapPosition } from './types';

// Performance thresholds
export const PERFORMANCE_THRESHOLDS = {
  MAX_VISIBLE_MARKERS: 200, // Maximum markers to show at once
  CLUSTERING_THRESHOLD: 50, // Start clustering above this count
  VIEWPORT_BUFFER: 0.1, // 10% buffer around viewport
  DEBOUNCE_DELAY: 300, // Milliseconds to debounce viewport changes
} as const;

/**
 * Check if a position is within viewport bounds with buffer
 */
export function isWithinViewport(
  position: MapPosition, 
  bounds: MapBounds, 
  bufferRatio: number = PERFORMANCE_THRESHOLDS.VIEWPORT_BUFFER
): boolean {
  const latBuffer = (bounds.north - bounds.south) * bufferRatio;
  const lngBuffer = (bounds.east - bounds.west) * bufferRatio;

  return (
    position.lat >= bounds.south - latBuffer &&
    position.lat <= bounds.north + latBuffer &&
    position.lng >= bounds.west - lngBuffer &&
    position.lng <= bounds.east + lngBuffer
  );
}

/**
 * Filter properties within current viewport
 */
export function filterPropertiesInViewport(
  properties: PropertyMarker[],
  bounds: MapBounds,
  bufferRatio?: number
): PropertyMarker[] {
  return properties.filter(property => 
    isWithinViewport(property.position, bounds, bufferRatio)
  );
}

/**
 * Prioritize properties by zoom level and distance from center
 */
export function prioritizeProperties(
  properties: PropertyMarker[],
  center: MapPosition,
  zoom: number,
  maxVisible: number = PERFORMANCE_THRESHOLDS.MAX_VISIBLE_MARKERS
): PropertyMarker[] {
  // Calculate distance from center for each property
  const propertiesWithDistance = properties.map(property => ({
    ...property,
    distanceFromCenter: calculateDistance(property.position, center),
  }));

  // Sort by distance and priority factors
  propertiesWithDistance.sort((a, b) => {
    // High priority properties first
    if (a.priority !== b.priority) {
      const priorityOrder = { high: 3, medium: 2, low: 1 };
      return (priorityOrder[b.priority] || 1) - (priorityOrder[a.priority] || 1);
    }
    
    // Then by distance from center
    return a.distanceFromCenter - b.distanceFromCenter;
  });

  // Limit to max visible count
  return propertiesWithDistance.slice(0, maxVisible);
}

/**
 * Calculate distance between two positions (Haversine formula)
 */
function calculateDistance(pos1: MapPosition, pos2: MapPosition): number {
  const R = 6371; // Earth's radius in kilometers
  const dLat = toRadians(pos2.lat - pos1.lat);
  const dLng = toRadians(pos2.lng - pos1.lng);
  
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(toRadians(pos1.lat)) * Math.cos(toRadians(pos2.lat)) *
    Math.sin(dLng / 2) * Math.sin(dLng / 2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

function toRadians(degrees: number): number {
  return degrees * (Math.PI / 180);
}

/**
 * Debounce utility for viewport changes
 */
export function debounce<T extends (...args: any[]) => any>(
  func: T,
  delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: NodeJS.Timeout;
  
  return (...args: Parameters<T>) => {
    clearTimeout(timeoutId);
    timeoutId = setTimeout(() => func(...args), delay);
  };
}

/**
 * Memory optimization for marker management
 */
export class MarkerPool {
  private pool: google.maps.Marker[] = [];
  private activeMarkers = new Set<google.maps.Marker>();
  
  getMarker(): google.maps.Marker | null {
    // Try to reuse a marker from the pool
    const marker = this.pool.pop();
    if (marker) {
      this.activeMarkers.add(marker);
      return marker;
    }
    return null;
  }
  
  releaseMarker(marker: google.maps.Marker): void {
    if (this.activeMarkers.has(marker)) {
      // Clean up marker
      marker.setMap(null);
      google.maps.event.clearInstanceListeners(marker);
      
      // Return to pool
      this.activeMarkers.delete(marker);
      this.pool.push(marker);
    }
  }
  
  releaseAllMarkers(): void {
    this.activeMarkers.forEach(marker => {
      marker.setMap(null);
      google.maps.event.clearInstanceListeners(marker);
      this.pool.push(marker);
    });
    this.activeMarkers.clear();
  }
  
  getActiveCount(): number {
    return this.activeMarkers.size;
  }
  
  getPoolSize(): number {
    return this.pool.length;
  }
}

/**
 * Performance monitor for map operations
 */
export class MapPerformanceMonitor {
  private metrics = {
    markerRenderTime: 0,
    clusteringTime: 0,
    viewportUpdateCount: 0,
    lastUpdateTime: Date.now(),
  };
  
  startTimer(operation: string): () => void {
    const startTime = performance.now();
    
    return () => {
      const endTime = performance.now();
      const duration = endTime - startTime;
      
      switch (operation) {
        case 'markerRender':
          this.metrics.markerRenderTime = duration;
          break;
        case 'clustering':
          this.metrics.clusteringTime = duration;
          break;
      }
    };
  }
  
  recordViewportUpdate(): void {
    this.metrics.viewportUpdateCount++;
    this.metrics.lastUpdateTime = Date.now();
  }
  
  getMetrics() {
    return { ...this.metrics };
  }
  
  reset(): void {
    this.metrics = {
      markerRenderTime: 0,
      clusteringTime: 0,
      viewportUpdateCount: 0,
      lastUpdateTime: Date.now(),
    };
  }
}