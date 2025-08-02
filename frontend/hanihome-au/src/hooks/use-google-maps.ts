/**
 * Google Maps React Hook
 */

import { useState, useEffect, useRef, useCallback } from 'react';
import { loadGoogleMaps, isGoogleMapsLoaded } from '@/lib/maps/loader';
import { MAP_OPTIONS, MOBILE_MAP_OPTIONS, DESKTOP_MAP_OPTIONS } from '@/lib/maps/config';
import { MapPosition, MapBounds, MapEventHandlers } from '@/lib/maps/types';

interface UseGoogleMapsOptions {
  center?: MapPosition;
  zoom?: number;
  options?: Partial<google.maps.MapOptions>;
  onLoad?: (map: google.maps.Map) => void;
  onError?: (error: Error) => void;
  isMobile?: boolean;
}

interface UseGoogleMapsReturn {
  map: google.maps.Map | null;
  isLoaded: boolean;
  isLoading: boolean;
  error: Error | null;
  mapRef: React.RefObject<HTMLDivElement | null>;
}

export function useGoogleMaps(
  options: UseGoogleMapsOptions = {}
): UseGoogleMapsReturn {
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const [isLoaded, setIsLoaded] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<Error | null>(null);
  const mapRef = useRef<HTMLDivElement>(null);

  const {
    center = MAP_OPTIONS.center,
    zoom = MAP_OPTIONS.zoom,
    options: mapOptions,
    onLoad,
    onError,
    isMobile = false,
  } = options;

  const initializeMap = useCallback(async () => {
    if (!mapRef.current || map || isLoading) return;

    try {
      setIsLoading(true);
      setError(null);

      // Load Google Maps API if not already loaded
      if (!isGoogleMapsLoaded()) {
        await loadGoogleMaps();
      }

      // Create map instance with device-specific options
      const deviceSpecificOptions = isMobile ? MOBILE_MAP_OPTIONS : DESKTOP_MAP_OPTIONS;
      const mapInstance = new google.maps.Map(mapRef.current, {
        ...MAP_OPTIONS,
        ...deviceSpecificOptions,
        ...mapOptions,
        center,
        zoom,
      });

      setMap(mapInstance);
      setIsLoaded(true);
      onLoad?.(mapInstance);
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Failed to load Google Maps');
      setError(error);
      onError?.(error);
    } finally {
      setIsLoading(false);
    }
  }, [map, isLoading, center, zoom, mapOptions, onLoad, onError]);

  useEffect(() => {
    initializeMap();
  }, [initializeMap]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (map) {
        // Clear any listeners or cleanup map resources
        google.maps.event.clearInstanceListeners(map);
      }
    };
  }, [map]);

  return {
    map,
    isLoaded,
    isLoading,
    error,
    mapRef,
  };
}

/**
 * Hook for handling map events
 */
export function useMapEvents(
  map: google.maps.Map | null,
  handlers: MapEventHandlers
) {
  useEffect(() => {
    if (!map) return;

    const listeners: google.maps.MapsEventListener[] = [];

    if (handlers.onBoundsChanged) {
      const listener = map.addListener('bounds_changed', () => {
        const bounds = map.getBounds();
        if (bounds) {
          const mapBounds: MapBounds = {
            north: bounds.getNorthEast().lat(),
            south: bounds.getSouthWest().lat(),
            east: bounds.getNorthEast().lng(),
            west: bounds.getSouthWest().lng(),
          };
          handlers.onBoundsChanged!(mapBounds);
        }
      });
      listeners.push(listener);
    }

    if (handlers.onCenterChanged) {
      const listener = map.addListener('center_changed', () => {
        const center = map.getCenter();
        if (center) {
          handlers.onCenterChanged!({
            lat: center.lat(),
            lng: center.lng(),
          });
        }
      });
      listeners.push(listener);
    }

    if (handlers.onZoomChanged) {
      const listener = map.addListener('zoom_changed', () => {
        const zoom = map.getZoom();
        if (zoom !== undefined) {
          handlers.onZoomChanged!(zoom);
        }
      });
      listeners.push(listener);
    }

    if (handlers.onMapClick) {
      const listener = map.addListener('click', (event: google.maps.MapMouseEvent) => {
        if (event.latLng) {
          handlers.onMapClick!({
            lat: event.latLng.lat(),
            lng: event.latLng.lng(),
          });
        }
      });
      listeners.push(listener);
    }

    return () => {
      listeners.forEach(listener => listener.remove());
    };
  }, [map, handlers]);
}