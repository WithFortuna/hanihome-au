/**
 * Google Map Component
 */

'use client';

import React from 'react';
import { useGoogleMaps, useMapEvents } from '@/hooks/use-google-maps';
import { useMobileDetection } from '@/hooks/use-mobile-detection';
import MapSkeleton from './map-skeleton';
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
  const { isMobile, isTablet, screenSize, height: screenHeight } = useMobileDetection();
  
  const { map, isLoaded, isLoading, error, mapRef } = useGoogleMaps({
    center,
    zoom,
    onLoad: onMapLoad,
    onError: onMapError,
    isMobile,
  });

  // Set up event handlers
  useMapEvents(map, eventHandlers);

  // Responsive height calculation
  const getResponsiveHeight = (): string => {
    if (isMobile) {
      // On mobile, use a percentage of screen height to be more flexible
      return `${Math.min(screenHeight * 0.4, 300)}px`;
    } else if (isTablet) {
      return '350px';
    } else {
      return '400px';
    }
  };

  const defaultStyle: React.CSSProperties = {
    width: '100%',
    height: getResponsiveHeight(),
    minHeight: isMobile ? '250px' : '300px',
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
      <MapSkeleton
        className={className}
        style={defaultStyle}
        showControls={!isMobile}
        showLegend={!isMobile}
      />
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