'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { AddressSearch } from '@/components/maps/address-search';
import GoogleMap from '@/components/maps/google-map';
import { PlaceResult, MapPosition } from '@/lib/maps/types';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { MapPin, Navigation, RefreshCw } from 'lucide-react';

interface AddressInfo {
  fullAddress: string;
  detailAddress?: string;
  zipCode?: string;
  city?: string;
  district?: string;
  coordinates: {
    lat: number;
    lng: number;
  };
}

interface AddressSearchWithMapProps {
  onAddressChange: (addressInfo: AddressInfo) => void;
  initialAddress?: string;
  initialCoordinates?: { lat: number; lng: number };
  showDetailAddressInput?: boolean;
  className?: string;
}

export function AddressSearchWithMap({
  onAddressChange,
  initialAddress = '',
  initialCoordinates,
  showDetailAddressInput = true,
  className = '',
}: AddressSearchWithMapProps) {
  const [selectedPlace, setSelectedPlace] = useState<PlaceResult | null>(null);
  const [mapCenter, setMapCenter] = useState<MapPosition>(
    initialCoordinates || { lat: 37.5665, lng: 126.9780 } // Seoul default
  );
  const [mapZoom, setMapZoom] = useState(15);
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const [marker, setMarker] = useState<google.maps.Marker | null>(null);
  const [detailAddress, setDetailAddress] = useState<string>('');
  const [isUsingCurrentLocation, setIsUsingCurrentLocation] = useState(false);

  // Initialize with existing coordinates if provided
  useEffect(() => {
    if (initialCoordinates) {
      setMapCenter(initialCoordinates);
      if (map) {
        map.setCenter(initialCoordinates);
        if (marker) {
          marker.setPosition(initialCoordinates);
        } else {
          createMarker(initialCoordinates);
        }
      }
    }
  }, [initialCoordinates, map, marker]);

  const createMarker = useCallback((position: MapPosition) => {
    if (!map) return;

    // Remove existing marker
    if (marker) {
      marker.setMap(null);
    }

    // Create new marker
    const newMarker = new google.maps.Marker({
      position,
      map,
      title: '매물 위치',
      draggable: true,
      animation: google.maps.Animation.DROP,
    });

    // Handle marker drag
    newMarker.addListener('dragend', (event: google.maps.MapMouseEvent) => {
      if (event.latLng) {
        const newPosition = {
          lat: event.latLng.lat(),
          lng: event.latLng.lng(),
        };
        reverseGeocode(newPosition);
      }
    });

    setMarker(newMarker);
  }, [map, marker]);

  const reverseGeocode = useCallback(async (position: MapPosition) => {
    if (!map) return;

    const geocoder = new google.maps.Geocoder();
    
    try {
      const response = await geocoder.geocode({ location: position });
      
      if (response.results && response.results.length > 0) {
        const result = response.results[0];
        const addressInfo: AddressInfo = {
          fullAddress: result.formatted_address,
          coordinates: position,
          detailAddress,
        };

        // Extract address components
        result.address_components.forEach(component => {
          const types = component.types;
          if (types.includes('postal_code')) {
            addressInfo.zipCode = component.long_name;
          } else if (types.includes('administrative_area_level_1')) {
            addressInfo.city = component.long_name;
          } else if (types.includes('sublocality_level_1') || types.includes('administrative_area_level_2')) {
            addressInfo.district = component.long_name;
          }
        });

        onAddressChange(addressInfo);
      }
    } catch (error) {
      console.error('Reverse geocoding failed:', error);
    }
  }, [map, detailAddress, onAddressChange]);

  const handlePlaceSelect = useCallback((place: PlaceResult) => {
    setSelectedPlace(place);
    
    const position = {
      lat: place.geometry.location.lat(),
      lng: place.geometry.location.lng(),
    };

    setMapCenter(position);
    setMapZoom(17);

    if (map) {
      map.setCenter(position);
      map.setZoom(17);
      createMarker(position);
    }

    // Parse address components
    const addressInfo: AddressInfo = {
      fullAddress: place.formatted_address,
      coordinates: position,
      detailAddress,
    };

    place.address_components.forEach(component => {
      const types = component.types;
      if (types.includes('postal_code')) {
        addressInfo.zipCode = component.long_name;
      } else if (types.includes('administrative_area_level_1')) {
        addressInfo.city = component.long_name;
      } else if (types.includes('sublocality_level_1') || types.includes('administrative_area_level_2')) {
        addressInfo.district = component.long_name;
      }
    });

    onAddressChange(addressInfo);
  }, [map, createMarker, detailAddress, onAddressChange]);

  const handleMapClick = useCallback((event: google.maps.MapMouseEvent) => {
    if (event.latLng) {
      const position = {
        lat: event.latLng.lat(),
        lng: event.latLng.lng(),
      };

      createMarker(position);
      reverseGeocode(position);
    }
  }, [createMarker, reverseGeocode]);

  const handleMapLoad = useCallback((loadedMap: google.maps.Map) => {
    setMap(loadedMap);
    
    // Create marker if we have initial coordinates
    if (initialCoordinates) {
      createMarker(initialCoordinates);
    }
  }, [initialCoordinates, createMarker]);

  const getCurrentLocation = useCallback(() => {
    if (!navigator.geolocation) {
      alert('브라우저에서 위치 서비스를 지원하지 않습니다.');
      return;
    }

    setIsUsingCurrentLocation(true);

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const coords = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };

        setMapCenter(coords);
        setMapZoom(17);

        if (map) {
          map.setCenter(coords);
          map.setZoom(17);
          createMarker(coords);
        }

        reverseGeocode(coords);
        setIsUsingCurrentLocation(false);
      },
      (error) => {
        console.error('Geolocation error:', error);
        alert('현재 위치를 가져올 수 없습니다. 위치 권한을 확인해주세요.');
        setIsUsingCurrentLocation(false);
      }
    );
  }, [map, createMarker, reverseGeocode]);

  const resetMap = useCallback(() => {
    setSelectedPlace(null);
    setMapCenter({ lat: 37.5665, lng: 126.9780 });
    setMapZoom(15);
    setDetailAddress('');
    
    if (marker) {
      marker.setMap(null);
      setMarker(null);
    }

    if (map) {
      map.setCenter({ lat: 37.5665, lng: 126.9780 });
      map.setZoom(15);
    }
  }, [map, marker]);

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Address Search */}
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-2">
            주소 검색 *
          </label>
          <AddressSearch
            onPlaceSelect={handlePlaceSelect}
            placeholder="주소를 검색해주세요 (예: 서울시 강남구 테헤란로)"
            defaultValue={initialAddress}
            className="w-full"
          />
        </div>

        {/* Detail Address Input */}
        {showDetailAddressInput && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              상세 주소
            </label>
            <input
              type="text"
              value={detailAddress}
              onChange={(e) => setDetailAddress(e.target.value)}
              placeholder="동, 호수 등 상세 주소를 입력해주세요"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            />
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex gap-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={getCurrentLocation}
            disabled={isUsingCurrentLocation}
            className="flex items-center gap-2"
          >
            {isUsingCurrentLocation ? (
              <RefreshCw className="w-4 h-4 animate-spin" />
            ) : (
              <Navigation className="w-4 h-4" />
            )}
            현재 위치
          </Button>
          
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={resetMap}
            className="flex items-center gap-2"
          >
            <RefreshCw className="w-4 h-4" />
            초기화
          </Button>
        </div>
      </div>

      {/* Map */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <MapPin className="w-5 h-5" />
            지도에서 위치 확인
            {selectedPlace && (
              <Badge variant="secondary" className="ml-2">
                위치 선택됨
              </Badge>
            )}
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <GoogleMap
              center={mapCenter}
              zoom={mapZoom}
              onMapLoad={handleMapLoad}
              eventHandlers={{
                click: handleMapClick,
              }}
              style={{ height: '400px' }}
              className="border rounded-lg"
            />

            {/* Instructions */}
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
              <h4 className="font-medium text-blue-800 mb-2">지도 사용법</h4>
              <ul className="text-sm text-blue-700 space-y-1">
                <li>• 지도를 클릭하여 위치를 직접 선택할 수 있습니다</li>
                <li>• 마커를 드래그하여 정확한 위치로 이동할 수 있습니다</li>
                <li>• "현재 위치" 버튼을 클릭하여 내 위치를 찾을 수 있습니다</li>
                <li>• 주소 검색을 통해 빠르게 위치를 찾을 수 있습니다</li>
              </ul>
            </div>

            {/* Selected Address Info */}
            {selectedPlace && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-4">
                <h4 className="font-medium text-green-800 mb-2">선택된 주소</h4>
                <div className="text-sm text-green-700 space-y-1">
                  <p><strong>주소:</strong> {selectedPlace.formatted_address}</p>
                  <p><strong>좌표:</strong> {selectedPlace.geometry.location.lat().toFixed(6)}, {selectedPlace.geometry.location.lng().toFixed(6)}</p>
                  {detailAddress && (
                    <p><strong>상세주소:</strong> {detailAddress}</p>
                  )}
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );
}