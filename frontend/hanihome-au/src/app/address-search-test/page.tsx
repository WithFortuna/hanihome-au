'use client';

import React, { useState } from 'react';
import { AddressSearch } from '@/components/maps/address-search';
import { PlaceResult } from '@/lib/maps/types';
import { extractAddressComponents, formatAddress } from '@/lib/maps/places';

export default function AddressSearchTestPage() {
  const [selectedPlace, setSelectedPlace] = useState<PlaceResult | null>(null);
  const [searchHistory, setSearchHistory] = useState<PlaceResult[]>([]);

  const handlePlaceSelect = (place: PlaceResult) => {
    console.log('Selected place:', place);
    setSelectedPlace(place);
    
    // Add to search history (keep last 5)
    setSearchHistory(prev => {
      const updated = [place, ...prev.filter(p => p.place_id !== place.place_id)];
      return updated.slice(0, 5);
    });
  };

  const clearSelection = () => {
    setSelectedPlace(null);
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-4xl mx-auto px-4">
        <div className="bg-white rounded-lg shadow-lg p-6">
          <h1 className="text-3xl font-bold text-gray-900 mb-6">
            Address Search Test
          </h1>
          
          {/* Main Search Component */}
          <div className="mb-8">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              Search for Australian Addresses
            </h2>
            <AddressSearch
              onPlaceSelect={handlePlaceSelect}
              placeholder="Enter an Australian address..."
              className="max-w-md"
              types={['address']}
              showClearButton={true}
              autoFocus={false}
            />
          </div>

          {/* Selected Place Details */}
          {selectedPlace && (
            <div className="mb-8 p-6 bg-blue-50 border border-blue-200 rounded-lg">
              <div className="flex justify-between items-start mb-4">
                <h2 className="text-xl font-semibold text-blue-900">
                  Selected Address Details
                </h2>
                <button
                  onClick={clearSelection}
                  className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                >
                  Clear
                </button>
              </div>
              
              <div className="grid md:grid-cols-2 gap-6">
                {/* Basic Information */}
                <div>
                  <h3 className="font-medium text-gray-900 mb-3">Basic Information</h3>
                  <div className="space-y-2 text-sm">
                    <div>
                      <span className="font-medium text-gray-600">Place ID:</span>
                      <span className="ml-2 text-gray-800 break-all">{selectedPlace.place_id}</span>
                    </div>
                    <div>
                      <span className="font-medium text-gray-600">Full Address:</span>
                      <span className="ml-2 text-gray-800">{selectedPlace.formatted_address}</span>
                    </div>
                    {selectedPlace.name && (
                      <div>
                        <span className="font-medium text-gray-600">Name:</span>
                        <span className="ml-2 text-gray-800">{selectedPlace.name}</span>
                      </div>
                    )}
                    {selectedPlace.types && (
                      <div>
                        <span className="font-medium text-gray-600">Types:</span>
                        <span className="ml-2 text-gray-800">{selectedPlace.types.join(', ')}</span>
                      </div>
                    )}
                  </div>
                </div>

                {/* Location Information */}
                {selectedPlace.geometry && (
                  <div>
                    <h3 className="font-medium text-gray-900 mb-3">Location</h3>
                    <div className="space-y-2 text-sm">
                      <div>
                        <span className="font-medium text-gray-600">Coordinates:</span>
                        <span className="ml-2 text-gray-800">
                          {selectedPlace.geometry.location.lat.toFixed(6)}, {selectedPlace.geometry.location.lng.toFixed(6)}
                        </span>
                      </div>
                      {selectedPlace.geometry.viewport && (
                        <div>
                          <span className="font-medium text-gray-600">Viewport:</span>
                          <div className="ml-2 text-gray-800 text-xs mt-1">
                            <div>North: {selectedPlace.geometry.viewport.north.toFixed(6)}</div>
                            <div>South: {selectedPlace.geometry.viewport.south.toFixed(6)}</div>
                            <div>East: {selectedPlace.geometry.viewport.east.toFixed(6)}</div>
                            <div>West: {selectedPlace.geometry.viewport.west.toFixed(6)}</div>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>
                )}
              </div>

              {/* Address Components */}
              {selectedPlace.address_components && (
                <div className="mt-6">
                  <h3 className="font-medium text-gray-900 mb-3">Address Components</h3>
                  <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                    {selectedPlace.address_components.map((component, index) => (
                      <div key={index} className="bg-white p-3 rounded border">
                        <div className="text-sm font-medium text-gray-900">
                          {component.long_name}
                        </div>
                        <div className="text-xs text-gray-500 mt-1">
                          {component.types.join(', ')}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Formatted Address Variations */}
              <div className="mt-6">
                <h3 className="font-medium text-gray-900 mb-3">Formatted Address Variations</h3>
                <div className="space-y-2 text-sm">
                  <div>
                    <span className="font-medium text-gray-600">Full:</span>
                    <span className="ml-2 text-gray-800">{formatAddress(selectedPlace, 'full')}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-600">Short:</span>
                    <span className="ml-2 text-gray-800">{formatAddress(selectedPlace, 'short')}</span>
                  </div>
                  <div>
                    <span className="font-medium text-gray-600">Suburb:</span>
                    <span className="ml-2 text-gray-800">{formatAddress(selectedPlace, 'suburb')}</span>
                  </div>
                </div>
              </div>

              {/* Extracted Components */}
              <div className="mt-6">
                <h3 className="font-medium text-gray-900 mb-3">Extracted Components</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-3 text-sm">
                  {Object.entries(extractAddressComponents(selectedPlace)).map(([key, value]) => (
                    <div key={key} className="bg-white p-2 rounded border">
                      <div className="font-medium text-gray-600 capitalize">
                        {key.replace(/([A-Z])/g, ' $1').trim()}:
                      </div>
                      <div className="text-gray-800">{value || 'N/A'}</div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Search History */}
          {searchHistory.length > 0 && (
            <div className="mb-8">
              <h2 className="text-xl font-semibold text-gray-800 mb-4">
                Recent Searches
              </h2>
              <div className="space-y-2">
                {searchHistory.map((place, index) => (
                  <button
                    key={`${place.place_id}-${index}`}
                    onClick={() => setSelectedPlace(place)}
                    className="w-full text-left p-3 bg-gray-50 hover:bg-gray-100 rounded border transition-colors"
                  >
                    <div className="font-medium text-gray-900">
                      {place.name || formatAddress(place, 'short')}
                    </div>
                    <div className="text-sm text-gray-600">
                      {place.formatted_address}
                    </div>
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Feature Test Section */}
          <div className="mt-8 p-6 bg-gray-50 rounded-lg">
            <h2 className="text-xl font-semibold text-gray-800 mb-4">
              Test Different Features
            </h2>
            
            <div className="grid md:grid-cols-2 gap-6">
              {/* Different Types */}
              <div>
                <h3 className="font-medium text-gray-900 mb-3">Search by Type</h3>
                <div className="space-y-3">
                  <AddressSearch
                    onPlaceSelect={handlePlaceSelect}
                    placeholder="Search establishments..."
                    types={['establishment']}
                    className="w-full"
                  />
                  <AddressSearch
                    onPlaceSelect={handlePlaceSelect}
                    placeholder="Search geocoded addresses..."
                    types={['geocode']}
                    className="w-full"
                  />
                </div>
              </div>

              {/* Location Biased Search */}
              <div>
                <h3 className="font-medium text-gray-900 mb-3">Location Biased (Melbourne)</h3>
                <AddressSearch
                  onPlaceSelect={handlePlaceSelect}
                  placeholder="Search near Melbourne..."
                  location={{ lat: -37.8136, lng: 144.9631 }}
                  radius={20000}
                  className="w-full"
                />
              </div>
            </div>
          </div>

          {/* Debug Information */}
          <div className="mt-8 p-4 bg-yellow-50 border border-yellow-200 rounded">
            <h3 className="font-medium text-yellow-800 mb-2">Debug Information</h3>
            <div className="text-sm text-yellow-700">
              <p>• Make sure NEXT_PUBLIC_GOOGLE_MAPS_API_KEY is set in your environment</p>
              <p>• Places API must be enabled in Google Cloud Console</p>
              <p>• Check browser console for any errors</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}