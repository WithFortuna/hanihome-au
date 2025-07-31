/**
 * Google Map Component
 */

'use client';

import React from 'react';
import { useGoogleMaps, useMapEvents } from '@/hooks/use-google-maps';
import { MapPosition, MapEventHandlers } from '@/lib/maps/types';

interface GoogleMapProps {
  center?: MapPosition;
  zoom?: number;
  className?: string;
  style?: React.CSSProperties;
  onMapLoad?: (map: google.maps.Map) => void;
  onMapError?: (error: Error) => void;
  eventHandlers?: MapEventHandlers;
}

export default function GoogleMap({
  center,
  zoom,
  className = '',
  style,
  onMapLoad,
  onMapError,
  eventHandlers = {},
}: GoogleMapProps) {
  const { map, isLoaded, isLoading, error, mapRef } = useGoogleMaps({
    center,
    zoom,
    onLoad: onMapLoad,
    onError: onMapError,
  });

  // Set up event handlers
  useMapEvents(map, eventHandlers);

  const defaultStyle: React.CSSProperties = {
    width: '100%',
    height: '400px',
    ...style,
  };

  if (error) {
    return (
      <div 
        className={`flex items-center justify-center bg-gray-100 rounded-lg ${className}`}
        style={defaultStyle}
      >
        <div className="text-center p-4">
          <p className="text-red-600 font-medium mb-2">Map Loading Error</p>
          <p className="text-gray-600 text-sm">{error.message}</p>
        </div>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div 
        className={`flex items-center justify-center bg-gray-100 rounded-lg ${className}`}
        style={defaultStyle}
      >
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
          <p className="text-gray-600">Loading Map...</p>
        </div>
      </div>
    );
  }

  return (
    <div 
      ref={mapRef}
      className={`rounded-lg overflow-hidden ${className}`}
      style={defaultStyle}
    />
  );
}