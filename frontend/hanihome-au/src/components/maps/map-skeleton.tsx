/**
 * Map Skeleton Loading Component
 */

'use client';

import React from 'react';

interface MapSkeletonProps {
  className?: string;
  style?: React.CSSProperties;
  showControls?: boolean;
  showLegend?: boolean;
}

export default function MapSkeleton({
  className = '',
  style,
  showControls = true,
  showLegend = true,
}: MapSkeletonProps) {
  return (
    <div 
      className={`relative bg-gray-100 rounded-lg overflow-hidden ${className}`}
      style={style}
    >
      {/* Main map area skeleton */}
      <div className="absolute inset-0 bg-gradient-to-br from-gray-200 to-gray-300">
        {/* Animated loading bars to simulate map tiles */}
        <div className="grid grid-cols-4 h-full opacity-30">
          {Array.from({ length: 16 }).map((_, i) => (
            <div
              key={i}
              className="bg-gray-400 animate-pulse"
              style={{
                animationDelay: `${i * 100}ms`,
                animationDuration: '2s',
              }}
            />
          ))}
        </div>
        
        {/* Simulated map features */}
        <div className="absolute inset-0">
          {/* Simulated roads */}
          <div className="absolute top-1/4 left-0 right-0 h-0.5 bg-gray-400 opacity-60"></div>
          <div className="absolute top-3/4 left-0 right-0 h-0.5 bg-gray-400 opacity-60"></div>
          <div className="absolute left-1/4 top-0 bottom-0 w-0.5 bg-gray-400 opacity-60"></div>
          <div className="absolute left-3/4 top-0 bottom-0 w-0.5 bg-gray-400 opacity-60"></div>
          
          {/* Simulated property markers */}
          <div className="absolute top-1/3 left-1/5 w-4 h-4 bg-blue-400 rounded-full animate-pulse"></div>
          <div className="absolute top-1/2 left-3/5 w-4 h-4 bg-green-400 rounded-full animate-pulse" style={{ animationDelay: '0.5s' }}></div>
          <div className="absolute top-2/3 right-1/4 w-4 h-4 bg-red-400 rounded-full animate-pulse" style={{ animationDelay: '1s' }}></div>
          <div className="absolute top-1/4 right-1/3 w-4 h-4 bg-purple-400 rounded-full animate-pulse" style={{ animationDelay: '1.5s' }}></div>
        </div>
      </div>

      {/* Controls skeleton */}
      {showControls && (
        <div className="absolute top-4 right-4 flex flex-col gap-2">
          <div className="w-16 h-6 bg-white rounded shadow-lg animate-pulse"></div>
          <div className="w-20 h-6 bg-white rounded shadow-lg animate-pulse"></div>
          <div className="w-16 h-6 bg-white rounded shadow-lg animate-pulse"></div>
        </div>
      )}

      {/* Legend skeleton */}
      {showLegend && (
        <div className="absolute bottom-4 left-4 w-48 bg-white p-3 rounded-lg shadow-lg">
          <div className="h-4 bg-gray-300 rounded animate-pulse mb-2"></div>
          <div className="grid grid-cols-2 gap-2">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="flex items-center gap-2">
                <div className="w-3 h-3 rounded-full bg-gray-300 animate-pulse"></div>
                <div className="h-3 bg-gray-300 rounded flex-1 animate-pulse"></div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Loading overlay */}
      <div className="absolute inset-0 flex items-center justify-center bg-white bg-opacity-80">
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-2"></div>
          <p className="text-gray-600 text-sm font-medium">Loading Map...</p>
          <p className="text-gray-500 text-xs mt-1">Fetching location data</p>
        </div>
      </div>
    </div>
  );
}