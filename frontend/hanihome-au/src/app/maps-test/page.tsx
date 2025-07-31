/**
 * Google Maps Test Page
 * This page is for testing the Google Maps integration
 * Remove this file after Maps features are fully implemented
 */

'use client';

import React, { useState } from 'react';
import { GoogleMap } from '@/components/maps';
import { MapPosition, MapBounds } from '@/lib/maps/types';
import { MAJOR_CITIES } from '@/lib/maps/config';

export default function MapsTestPage() {
  const [currentCenter, setCurrentCenter] = useState<MapPosition>(MAJOR_CITIES.melbourne);
  const [mapBounds, setMapBounds] = useState<MapBounds | null>(null);
  const [clickedPosition, setClickedPosition] = useState<MapPosition | null>(null);

  return (
    <div className="container mx-auto p-4">
      <h1 className="text-2xl font-bold mb-6">Google Maps Integration Test</h1>
      
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Map Controls */}
        <div className="lg:col-span-1">
          <div className="bg-white p-4 rounded-lg shadow">
            <h2 className="text-lg font-semibold mb-4">Map Controls</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium mb-2">Jump to City:</label>
                <select 
                  className="w-full p-2 border rounded"
                  onChange={(e) => {
                    const city = e.target.value as keyof typeof MAJOR_CITIES;
                    if (city && MAJOR_CITIES[city]) {
                      setCurrentCenter(MAJOR_CITIES[city]);
                    }
                  }}
                >
                  <option value="">Select a city</option>
                  <option value="melbourne">Melbourne</option>
                  <option value="sydney">Sydney</option>
                  <option value="brisbane">Brisbane</option>
                  <option value="perth">Perth</option>
                  <option value="adelaide">Adelaide</option>
                  <option value="canberra">Canberra</option>
                  <option value="darwin">Darwin</option>
                  <option value="hobart">Hobart</option>
                </select>
              </div>

              <div>
                <h3 className="font-medium mb-2">Current Center:</h3>
                <p className="text-sm text-gray-600">
                  Lat: {currentCenter.lat.toFixed(4)}<br/>
                  Lng: {currentCenter.lng.toFixed(4)}
                </p>
              </div>

              {mapBounds && (
                <div>
                  <h3 className="font-medium mb-2">Map Bounds:</h3>
                  <p className="text-xs text-gray-600">
                    North: {mapBounds.north.toFixed(4)}<br/>
                    South: {mapBounds.south.toFixed(4)}<br/>
                    East: {mapBounds.east.toFixed(4)}<br/>
                    West: {mapBounds.west.toFixed(4)}
                  </p>
                </div>
              )}

              {clickedPosition && (
                <div>
                  <h3 className="font-medium mb-2">Last Clicked:</h3>
                  <p className="text-sm text-gray-600">
                    Lat: {clickedPosition.lat.toFixed(4)}<br/>
                    Lng: {clickedPosition.lng.toFixed(4)}
                  </p>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Map Display */}
        <div className="lg:col-span-2">
          <div className="bg-white p-4 rounded-lg shadow">
            <h2 className="text-lg font-semibold mb-4">Interactive Map</h2>
            
            <GoogleMap
              center={currentCenter}
              zoom={12}
              className="border rounded-lg"
              style={{ height: '500px' }}
              onMapLoad={(map) => {
                console.log('Map loaded successfully:', map);
              }}
              onMapError={(error) => {
                console.error('Map loading error:', error);
              }}
              eventHandlers={{
                onBoundsChanged: (bounds) => {
                  setMapBounds(bounds);
                },
                onCenterChanged: (center) => {
                  setCurrentCenter(center);
                },
                onMapClick: (position) => {
                  setClickedPosition(position);
                  console.log('Map clicked at:', position);
                },
              }}
            />

            <p className="text-sm text-gray-500 mt-2">
              Click on the map to see coordinates. Use the city selector to jump to different locations.
            </p>
          </div>
        </div>
      </div>

      <div className="mt-6 p-4 bg-yellow-50 rounded-lg">
        <h3 className="font-medium text-yellow-800 mb-2">Test Instructions:</h3>
        <ul className="text-sm text-yellow-700 space-y-1">
          <li>• Check that the map loads correctly with Melbourne as the default center</li>
          <li>• Test city selection to ensure map centers update properly</li>
          <li>• Click on the map to verify click event handling</li>
          <li>• Move the map around to test bounds change events</li>
          <li>• Check browser console for any JavaScript errors</li>
          <li>• Test on mobile devices for responsive behavior</li>
        </ul>
      </div>
    </div>
  );
}