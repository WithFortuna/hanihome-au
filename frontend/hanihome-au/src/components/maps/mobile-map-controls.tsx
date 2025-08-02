/**
 * Mobile-Optimized Map Controls
 */

'use client';

import React, { useState } from 'react';
import { 
  ZoomInIcon, 
  ZoomOutIcon, 
  MapIcon, 
  ViewfinderCircleIcon,
  ChevronUpIcon,
  ChevronDownIcon
} from '@heroicons/react/24/outline';
import { useMobileDetection } from '@/hooks/use-mobile-detection';

interface MobileMapControlsProps {
  map: google.maps.Map | null;
  propertyCount?: number;
  clusterCount?: number;
  currentZoom?: number;
  onLocationRequest?: () => void;
  onToggleMapType?: () => void;
  className?: string;
}

export default function MobileMapControls({
  map,
  propertyCount = 0,
  clusterCount = 0,
  currentZoom = 12,
  onLocationRequest,
  onToggleMapType,
  className = '',
}: MobileMapControlsProps) {
  const { isMobile, isTablet } = useMobileDetection();
  const [isStatsExpanded, setIsStatsExpanded] = useState(false);

  if (!isMobile && !isTablet) {
    return null; // Don't render on desktop
  }

  const handleZoomIn = () => {
    if (map) {
      const currentZoom = map.getZoom() || 12;
      map.setZoom(Math.min(currentZoom + 1, 21));
    }
  };

  const handleZoomOut = () => {
    if (map) {
      const currentZoom = map.getZoom() || 12;
      map.setZoom(Math.max(currentZoom - 1, 1));
    }
  };

  const handleMyLocation = () => {
    if (navigator.geolocation && map) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          const pos = {
            lat: position.coords.latitude,
            lng: position.coords.longitude,
          };
          
          map.setCenter(pos);
          map.setZoom(15);
          
          // Add a marker for user location
          new google.maps.Marker({
            position: pos,
            map: map,
            title: 'Your Location',
            icon: {
              path: google.maps.SymbolPath.CIRCLE,
              scale: 8,
              fillColor: '#4285F4',
              fillOpacity: 1,
              strokeColor: '#ffffff',
              strokeWeight: 2,
            },
          });
          
          onLocationRequest?.();
        },
        () => {
          console.error('Error: The Geolocation service failed.');
        }
      );
    }
  };

  return (
    <div className={`absolute inset-0 pointer-events-none ${className}`}>
      {/* Mobile Zoom Controls - Right Side */}
      <div className="absolute right-3 top-1/2 transform -translate-y-1/2 flex flex-col gap-1 pointer-events-auto">
        <button
          onClick={handleZoomIn}
          className="bg-white hover:bg-gray-50 active:bg-gray-100 shadow-lg rounded-lg p-3 transition-colors touch-manipulation"
          aria-label="Zoom in"
        >
          <ZoomInIcon className="w-5 h-5 text-gray-700" />
        </button>
        <button
          onClick={handleZoomOut}
          className="bg-white hover:bg-gray-50 active:bg-gray-100 shadow-lg rounded-lg p-3 transition-colors touch-manipulation"
          aria-label="Zoom out"
        >
          <ZoomOutIcon className="w-5 h-5 text-gray-700" />
        </button>
      </div>

      {/* Location Button - Right Bottom */}
      <div className="absolute right-3 bottom-20 pointer-events-auto">
        <button
          onClick={handleMyLocation}
          className="bg-white hover:bg-gray-50 active:bg-gray-100 shadow-lg rounded-lg p-3 transition-colors touch-manipulation"
          aria-label="My location"
        >
          <ViewfinderCircleIcon className="w-5 h-5 text-gray-700" />
        </button>
      </div>

      {/* Map Type Toggle - Right Bottom Above Location */}
      {onToggleMapType && (
        <div className="absolute right-3 bottom-32 pointer-events-auto">
          <button
            onClick={onToggleMapType}
            className="bg-white hover:bg-gray-50 active:bg-gray-100 shadow-lg rounded-lg p-3 transition-colors touch-manipulation"
            aria-label="Toggle map type"
          >
            <MapIcon className="w-5 h-5 text-gray-700" />
          </button>
        </div>
      )}

      {/* Collapsible Stats Panel - Bottom */}
      <div className="absolute bottom-0 left-0 right-0 pointer-events-auto">
        <div className="bg-white shadow-lg border-t border-gray-200">
          {/* Toggle Header */}
          <button
            onClick={() => setIsStatsExpanded(!isStatsExpanded)}
            className="w-full px-4 py-3 flex items-center justify-between text-sm font-medium text-gray-700 hover:bg-gray-50 active:bg-gray-100 transition-colors touch-manipulation"
          >
            <span>Map Info</span>
            {isStatsExpanded ? (
              <ChevronDownIcon className="w-4 h-4" />
            ) : (
              <ChevronUpIcon className="w-4 h-4" />
            )}
          </button>

          {/* Expandable Content */}
          {isStatsExpanded && (
            <div className="px-4 pb-4 border-t border-gray-100">
              <div className="grid grid-cols-3 gap-3 text-center">
                <div className="py-2">
                  <div className="text-lg font-semibold text-gray-900">{currentZoom}</div>
                  <div className="text-xs text-gray-500">Zoom Level</div>
                </div>
                <div className="py-2">
                  <div className="text-lg font-semibold text-gray-900">{propertyCount}</div>
                  <div className="text-xs text-gray-500">Properties</div>
                </div>
                <div className="py-2">
                  <div className="text-lg font-semibold text-gray-900">{clusterCount}</div>
                  <div className="text-xs text-gray-500">Clusters</div>
                </div>
              </div>
              
              {/* Quick Tips */}
              <div className="mt-3 p-2 bg-blue-50 rounded-lg">
                <p className="text-xs text-blue-700 text-center">
                  💡 Pinch to zoom • Tap clusters to expand • Double-tap to zoom in
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}