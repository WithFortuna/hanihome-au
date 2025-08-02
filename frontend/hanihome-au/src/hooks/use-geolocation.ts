/**
 * Geolocation Hook
 * Provides access to user's current location with error handling and permission management
 */

import { useState, useEffect, useCallback } from 'react';
import { MapPosition } from '@/lib/maps/types';

export type GeolocationStatus = 'idle' | 'loading' | 'success' | 'error' | 'denied';

export interface GeolocationError {
  code: number;
  message: string;
  type: 'PERMISSION_DENIED' | 'POSITION_UNAVAILABLE' | 'TIMEOUT' | 'NOT_SUPPORTED';
}

export interface GeolocationOptions extends PositionOptions {
  /**
   * Whether to automatically get location on mount
   * @default false
   */
  enableHighAccuracy?: boolean;
  /**
   * Maximum time in milliseconds to wait for location
   * @default 10000
   */
  timeout?: number;
  /**
   * Maximum age of cached position in milliseconds
   * @default 300000 (5 minutes)
   */
  maximumAge?: number;
  /**
   * Whether to watch position changes
   * @default false
   */
  watch?: boolean;
}

export interface UseGeolocationReturn {
  /** Current user position */
  position: MapPosition | null;
  /** Current status of geolocation request */
  status: GeolocationStatus;
  /** Error information if location request failed */
  error: GeolocationError | null;
  /** Whether geolocation is currently loading */
  isLoading: boolean;
  /** Whether location was successfully obtained */
  isSuccess: boolean;
  /** Whether location request failed */
  isError: boolean;
  /** Whether geolocation is supported by browser */
  isSupported: boolean;
  /** Manually trigger location request */
  getCurrentLocation: () => Promise<MapPosition>;
  /** Clear current position and error state */
  clearLocation: () => void;
  /** Check and request geolocation permission */
  requestPermission: () => Promise<PermissionState>;
}

const DEFAULT_OPTIONS: GeolocationOptions = {
  enableHighAccuracy: true,
  timeout: 10000,
  maximumAge: 300000, // 5 minutes
  watch: false,
};

export function useGeolocation(options: GeolocationOptions = {}): UseGeolocationReturn {
  const [position, setPosition] = useState<MapPosition | null>(null);
  const [status, setStatus] = useState<GeolocationStatus>('idle');
  const [error, setError] = useState<GeolocationError | null>(null);
  const [watchId, setWatchId] = useState<number | null>(null);

  const opts = { ...DEFAULT_OPTIONS, ...options };

  // Check if geolocation is supported
  const isSupported = typeof navigator !== 'undefined' && 'geolocation' in navigator;

  // Clear location state
  const clearLocation = useCallback(() => {
    setPosition(null);
    setStatus('idle');
    setError(null);
    
    if (watchId !== null) {
      navigator.geolocation.clearWatch(watchId);
      setWatchId(null);
    }
  }, [watchId]);

  // Convert GeolocationPositionError to our error format
  const createGeolocationError = useCallback((err: GeolocationPositionError): GeolocationError => {
    switch (err.code) {
      case err.PERMISSION_DENIED:
        return {
          code: err.code,
          message: 'Location access denied by user',
          type: 'PERMISSION_DENIED',
        };
      case err.POSITION_UNAVAILABLE:
        return {
          code: err.code,
          message: 'Location information unavailable',
          type: 'POSITION_UNAVAILABLE',
        };
      case err.TIMEOUT:
        return {
          code: err.code,
          message: 'Location request timed out',
          type: 'TIMEOUT',
        };
      default:
        return {
          code: err.code,
          message: err.message || 'Unknown geolocation error',
          type: 'POSITION_UNAVAILABLE',
        };
    }
  }, []);

  // Success handler
  const handleSuccess = useCallback((pos: GeolocationPosition) => {
    const newPosition: MapPosition = {
      lat: pos.coords.latitude,
      lng: pos.coords.longitude,
    };
    
    setPosition(newPosition);
    setStatus('success');
    setError(null);
  }, []);

  // Error handler
  const handleError = useCallback((err: GeolocationPositionError) => {
    const geolocationError = createGeolocationError(err);
    setError(geolocationError);
    setStatus(geolocationError.type === 'PERMISSION_DENIED' ? 'denied' : 'error');
    setPosition(null);
  }, [createGeolocationError]);

  // Get current location
  const getCurrentLocation = useCallback(async (): Promise<MapPosition> => {
    if (!isSupported) {
      const error: GeolocationError = {
        code: -1,
        message: 'Geolocation is not supported by this browser',
        type: 'NOT_SUPPORTED',
      };
      setError(error);
      setStatus('error');
      throw error;
    }

    setStatus('loading');
    setError(null);

    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (pos) => {
          handleSuccess(pos);
          resolve({
            lat: pos.coords.latitude,
            lng: pos.coords.longitude,
          });
        },
        (err) => {
          handleError(err);
          reject(createGeolocationError(err));
        },
        {
          enableHighAccuracy: opts.enableHighAccuracy,
          timeout: opts.timeout,
          maximumAge: opts.maximumAge,
        }
      );
    });
  }, [isSupported, opts, handleSuccess, handleError, createGeolocationError]);

  // Request permission
  const requestPermission = useCallback(async (): Promise<PermissionState> => {
    if (!isSupported) {
      throw new Error('Geolocation is not supported');
    }

    if ('permissions' in navigator) {
      try {
        const permission = await navigator.permissions.query({ name: 'geolocation' });
        return permission.state;
      } catch (err) {
        console.warn('Permission API not supported, trying direct geolocation request');
      }
    }

    // Fallback: try to get location to determine permission
    try {
      await getCurrentLocation();
      return 'granted';
    } catch (err) {
      const geolocationError = err as GeolocationError;
      return geolocationError.type === 'PERMISSION_DENIED' ? 'denied' : 'prompt';
    }
  }, [isSupported, getCurrentLocation]);

  // Watch position changes
  useEffect(() => {
    if (!isSupported || !opts.watch) return;

    const id = navigator.geolocation.watchPosition(
      handleSuccess,
      handleError,
      {
        enableHighAccuracy: opts.enableHighAccuracy,
        timeout: opts.timeout,
        maximumAge: opts.maximumAge,
      }
    );

    setWatchId(id);

    return () => {
      navigator.geolocation.clearWatch(id);
      setWatchId(null);
    };
  }, [isSupported, opts.watch, opts.enableHighAccuracy, opts.timeout, opts.maximumAge, handleSuccess, handleError]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      if (watchId !== null) {
        navigator.geolocation.clearWatch(watchId);
      }
    };
  }, [watchId]);

  const isLoading = status === 'loading';
  const isSuccess = status === 'success';
  const isError = status === 'error' || status === 'denied';

  return {
    position,
    status,
    error,
    isLoading,
    isSuccess,
    isError,
    isSupported,
    getCurrentLocation,
    clearLocation,
    requestPermission,
  };
}