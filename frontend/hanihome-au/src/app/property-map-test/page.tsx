'use client';

import React, { useState, useMemo } from 'react';
import PropertyMap from '@/components/maps/property-map';
import { PropertyMarker, MapPosition } from '@/lib/maps/types';
import { MAJOR_CITIES } from '@/lib/maps/config';

// Generate sample property data for testing
const generateSampleProperties = (count: number = 100): PropertyMarker[] => {
  const properties: PropertyMarker[] = [];
  const propertyTypes = ['house', 'apartment', 'townhouse', 'studio', 'room'];
  const melbourneCenter = MAJOR_CITIES.melbourne;

  for (let i = 0; i < count; i++) {
    const angle = Math.random() * 2 * Math.PI;
    const distance = Math.random() * 0.5; // 0.5 degrees radius
    
    properties.push({
      id: `property-${i}`,
      position: {
        lat: melbourneCenter.lat + (Math.cos(angle) * distance),
        lng: melbourneCenter.lng + (Math.sin(angle) * distance),
      },
      title: `Property ${i + 1}`,
      price: Math.floor(300 + Math.random() * 1000),
      propertyType: propertyTypes[Math.floor(Math.random() * propertyTypes.length)],
      bedrooms: Math.floor(1 + Math.random() * 4),
      bathrooms: Math.floor(1 + Math.random() * 3),
      imageUrl: `https://picsum.photos/300/200?random=${i}`,
    });
  }

  return properties;
};

export default function PropertyMapTestPage() {
  const [propertyCount, setPropertyCount] = useState(100);
  const [clustering, setClustering] = useState(true);
  const [selectedCity, setSelectedCity] = useState<keyof typeof MAJOR_CITIES>('melbourne');
  const [selectedProperty, setSelectedProperty] = useState<PropertyMarker | null>(null);

  // Generate properties based on current settings
  const properties = useMemo(() => {
    return generateSampleProperties(propertyCount);
  }, [propertyCount]);

  // Get center for selected city
  const mapCenter = MAJOR_CITIES[selectedCity];

  const handlePropertyClick = (property: PropertyMarker) => {
    setSelectedProperty(property);
    console.log('Property clicked:', property);
  };

  const handleBoundsChanged = (bounds: any) => {
    console.log('Bounds changed:', bounds);
  };

  const handleZoomChanged = (zoom: number) => {
    console.log('Zoom changed:', zoom);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Controls Panel */}
      <div className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">
            Property Map with Clustering Test
          </h1>
          
          <div className="flex flex-wrap items-center gap-6">
            {/* Property Count */}
            <div className="flex items-center gap-2">
              <label className="text-sm font-medium text-gray-700">
                Properties:
              </label>
              <select
                value={propertyCount}
                onChange={(e) => setPropertyCount(Number(e.target.value))}
                className="px-3 py-1 border border-gray-300 rounded text-sm"
              >
                <option value={50}>50</option>
                <option value={100}>100</option>
                <option value={200}>200</option>
                <option value={500}>500</option>
                <option value={1000}>1000</option>
              </select>
            </div>

            {/* Clustering Toggle */}
            <div className="flex items-center gap-2">
              <label className="text-sm font-medium text-gray-700">
                Clustering:
              </label>
              <button
                onClick={() => setClustering(!clustering)}
                className={`px-3 py-1 rounded text-sm font-medium transition-colors ${
                  clustering 
                    ? 'bg-green-100 text-green-800 border border-green-300' 
                    : 'bg-gray-100 text-gray-800 border border-gray-300'
                }`}
              >
                {clustering ? 'Enabled' : 'Disabled'}
              </button>
            </div>

            {/* City Selection */}
            <div className="flex items-center gap-2">
              <label className="text-sm font-medium text-gray-700">
                City:
              </label>
              <select
                value={selectedCity}
                onChange={(e) => setSelectedCity(e.target.value as keyof typeof MAJOR_CITIES)}
                className="px-3 py-1 border border-gray-300 rounded text-sm"
              >
                {Object.keys(MAJOR_CITIES).map(city => (
                  <option key={city} value={city}>
                    {city.charAt(0).toUpperCase() + city.slice(1)}
                  </option>
                ))}
              </select>
            </div>

            {/* Reset Button */}
            <button
              onClick={() => window.location.reload()}
              className="px-4 py-1 bg-blue-600 text-white rounded text-sm font-medium hover:bg-blue-700 transition-colors"
            >
              Reset
            </button>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto p-4">
        <div className="grid lg:grid-cols-4 gap-6">
          {/* Map */}
          <div className="lg:col-span-3">
            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
              <PropertyMap
                properties={properties}
                center={mapCenter}
                zoom={12}
                clustering={clustering}
                clusteringOptions={{
                  gridSize: 60,
                  maxZoom: 15,
                  minimumClusterSize: 2,
                }}
                onPropertyClick={handlePropertyClick}
                onBoundsChanged={handleBoundsChanged}
                onZoomChanged={handleZoomChanged}
                style={{ height: '600px' }}
                showInfoWindow={true}
              />
            </div>
          </div>

          {/* Sidebar */}
          <div className="lg:col-span-1">
            {/* Selected Property Details */}
            {selectedProperty && (
              <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
                <h3 className="text-lg font-semibold text-gray-900 mb-4">
                  Selected Property
                </h3>
                
                {selectedProperty.imageUrl && (
                  <img
                    src={selectedProperty.imageUrl}
                    alt={selectedProperty.title}
                    className="w-full h-32 object-cover rounded-lg mb-4"
                  />
                )}
                
                <div className="space-y-2 text-sm">
                  <div>
                    <span className="font-medium text-gray-600">Title:</span>
                    <span className="ml-2 text-gray-900">{selectedProperty.title}</span>
                  </div>
                  
                  {selectedProperty.price && (
                    <div>
                      <span className="font-medium text-gray-600">Price:</span>
                      <span className="ml-2 text-green-600 font-semibold">
                        ${selectedProperty.price}/week
                      </span>
                    </div>
                  )}
                  
                  <div>
                    <span className="font-medium text-gray-600">Type:</span>
                    <span className="ml-2 text-gray-900 capitalize">
                      {selectedProperty.propertyType}
                    </span>
                  </div>
                  
                  {selectedProperty.bedrooms && (
                    <div>
                      <span className="font-medium text-gray-600">Bedrooms:</span>
                      <span className="ml-2 text-gray-900">{selectedProperty.bedrooms}</span>
                    </div>
                  )}
                  
                  {selectedProperty.bathrooms && (
                    <div>
                      <span className="font-medium text-gray-600">Bathrooms:</span>
                      <span className="ml-2 text-gray-900">{selectedProperty.bathrooms}</span>
                    </div>
                  )}
                  
                  <div>
                    <span className="font-medium text-gray-600">Location:</span>
                    <div className="ml-2 text-gray-900 text-xs">
                      {selectedProperty.position.lat.toFixed(6)}, {selectedProperty.position.lng.toFixed(6)}
                    </div>
                  </div>
                </div>
              </div>
            )}

            {/* Statistics */}
            <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Statistics
              </h3>
              
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Total Properties:</span>
                  <span className="text-sm font-medium text-gray-900">{properties.length}</span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Clustering:</span>
                  <span className={`text-sm font-medium ${clustering ? 'text-green-600' : 'text-red-600'}`}>
                    {clustering ? 'Enabled' : 'Disabled'}
                  </span>
                </div>
                
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Selected City:</span>
                  <span className="text-sm font-medium text-gray-900 capitalize">{selectedCity}</span>
                </div>
              </div>
            </div>

            {/* Property Type Distribution */}
            <div className="bg-white rounded-lg shadow-lg p-6">
              <h3 className="text-lg font-semibold text-gray-900 mb-4">
                Property Types
              </h3>
              
              {Object.entries(
                properties.reduce((acc, prop) => {
                  const type = prop.propertyType || 'unknown';
                  acc[type] = (acc[type] || 0) + 1;
                  return acc;
                }, {} as Record<string, number>)
              ).map(([type, count]) => (
                <div key={type} className="flex justify-between items-center mb-2">
                  <span className="text-sm text-gray-600 capitalize">{type}:</span>
                  <span className="text-sm font-medium text-gray-900">{count}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Instructions */}
      <div className="max-w-7xl mx-auto px-4 pb-8">
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-6">
          <h3 className="text-lg font-semibold text-blue-900 mb-3">
            How to Test
          </h3>
          <ul className="text-sm text-blue-800 space-y-2">
            <li>• Change property count to see clustering performance</li>
            <li>• Toggle clustering on/off to compare views</li>
            <li>• Click on clusters to zoom in or expand at max zoom</li>
            <li>• Click on individual property markers to see details</li>
            <li>• Switch between different Australian cities</li>
            <li>• Check browser console for event logs</li>
          </ul>
        </div>
      </div>
    </div>
  );
}