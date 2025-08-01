'use client';

import React, { useState, useEffect } from 'react';
import { Slider } from '@/components/ui/slider';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { MapPin, Navigation, Clock } from 'lucide-react';

interface DistanceRangeFilterProps {
  userLocation?: {
    latitude: number;
    longitude: number;
  };
  onDistanceChange: (maxDistance: number) => void;
  onLocationChange: (location: { latitude: number; longitude: number }) => void;
  maxDistance: number;
  propertyType?: string;
  rentalType?: string;
  minPrice?: number;
  maxPrice?: number;
  className?: string;
}

interface DistanceRangeData {
  [key: string]: number;
}

export function DistanceRangeFilter({
  userLocation,
  onDistanceChange,
  onLocationChange,
  maxDistance,
  propertyType,
  rentalType,
  minPrice,
  maxPrice,
  className = ''
}: DistanceRangeFilterProps) {
  const [currentLocation, setCurrentLocation] = useState(userLocation);
  const [isGettingLocation, setIsGettingLocation] = useState(false);
  const [locationError, setLocationError] = useState<string | null>(null);
  const [distanceRanges, setDistanceRanges] = useState<DistanceRangeData>({});
  const [isLoadingRanges, setIsLoadingRanges] = useState(false);

  // Distance presets in kilometers
  const distancePresets = [
    { value: 1, label: '1km', description: '도보 12분' },
    { value: 2, label: '2km', description: '도보 24분' },
    { value: 5, label: '5km', description: '자전거 15분' },
    { value: 10, label: '10km', description: '대중교통 30분' },
    { value: 20, label: '20km', description: '차량 30분' },
  ];

  // Get user's current location
  const getCurrentLocation = async () => {
    if (!navigator.geolocation) {
      setLocationError('위치 서비스가 지원되지 않습니다.');
      return;
    }

    setIsGettingLocation(true);
    setLocationError(null);

    try {
      const position = await new Promise<GeolocationPosition>((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, {
          enableHighAccuracy: true,
          timeout: 10000,
          maximumAge: 300000, // 5 minutes
        });
      });

      const location = {
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
      };

      setCurrentLocation(location);
      onLocationChange(location);
    } catch (error) {
      console.error('Location error:', error);
      setLocationError('위치를 가져올 수 없습니다. 위치 권한을 확인해주세요.');
    } finally {
      setIsGettingLocation(false);
    }
  };

  // Fetch property count by distance ranges
  const fetchDistanceRanges = async () => {
    if (!currentLocation) return;

    setIsLoadingRanges(true);
    try {
      const params = new URLSearchParams({
        latitude: currentLocation.latitude.toString(),
        longitude: currentLocation.longitude.toString(),
      });

      if (propertyType) params.append('propertyType', propertyType);
      if (rentalType) params.append('rentalType', rentalType);
      if (minPrice) params.append('minPrice', minPrice.toString());
      if (maxPrice) params.append('maxPrice', maxPrice.toString());

      const response = await fetch(`/api/properties/distance-ranges?${params}`);
      if (response.ok) {
        const data = await response.json();
        setDistanceRanges(data.data.distanceRanges || {});
      }
    } catch (error) {
      console.error('Failed to fetch distance ranges:', error);
    } finally {
      setIsLoadingRanges(false);
    }
  };

  // Format distance display
  const formatDistance = (km: number): string => {
    if (km < 1) {
      return `${Math.round(km * 1000)}m`;
    }
    return `${km}km`;
  };

  // Calculate walking time
  const calculateWalkingTime = (km: number): string => {
    const minutes = Math.round(km * 12); // 5 km/h = 12 minutes per km
    if (minutes < 60) {
      return `도보 ${minutes}분`;
    }
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `도보 ${hours}시간 ${remainingMinutes}분`;
  };

  // Effect to fetch distance ranges when location or filters change
  useEffect(() => {
    if (currentLocation) {
      fetchDistanceRanges();
    }
  }, [currentLocation, propertyType, rentalType, minPrice, maxPrice]);

  return (
    <Card className={`w-full ${className}`}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <MapPin className="h-5 w-5" />
          거리 필터
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Location Section */}
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-sm font-medium">현재 위치</span>
            <Button
              variant="outline"
              size="sm"
              onClick={getCurrentLocation}
              disabled={isGettingLocation}
              className="text-xs"
            >
              <Navigation className="h-3 w-3 mr-1" />
              {isGettingLocation ? '위치 확인 중...' : '내 위치'}
            </Button>
          </div>

          {locationError && (
            <p className="text-xs text-red-500">{locationError}</p>
          )}

          {currentLocation && (
            <div className="text-xs text-muted-foreground">
              위도: {currentLocation.latitude.toFixed(6)}<br />
              경도: {currentLocation.longitude.toFixed(6)}
            </div>
          )}
        </div>

        {/* Distance Slider */}
        {currentLocation && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-sm font-medium">검색 반경</span>
              <Badge variant="secondary">
                {formatDistance(maxDistance)}
              </Badge>
            </div>

            <div className="px-2">
              <Slider
                value={[maxDistance]}
                onValueChange={(values) => onDistanceChange(values[0])}
                max={20}
                min={0.5}
                step={0.5}
                className="w-full"
              />
              <div className="flex justify-between text-xs text-muted-foreground mt-1">
                <span>0.5km</span>
                <span>20km</span>
              </div>
            </div>

            <div className="text-xs flex items-center gap-1 text-muted-foreground">
              <Clock className="h-3 w-3" />
              {calculateWalkingTime(maxDistance)}
            </div>
          </div>
        )}

        {/* Distance Presets */}
        {currentLocation && (
          <div className="space-y-3">
            <span className="text-sm font-medium">빠른 선택</span>
            <div className="grid grid-cols-2 gap-2">
              {distancePresets.map((preset) => (
                <Button
                  key={preset.value}
                  variant={maxDistance === preset.value ? "default" : "outline"}
                  size="sm"
                  onClick={() => onDistanceChange(preset.value)}
                  className="flex flex-col h-auto py-2 px-3"
                >
                  <span className="font-medium">{preset.label}</span>
                  <span className="text-xs opacity-70">{preset.description}</span>
                </Button>
              ))}
            </div>
          </div>
        )}

        {/* Property Count by Distance */}
        {currentLocation && Object.keys(distanceRanges).length > 0 && (
          <div className="space-y-3">
            <span className="text-sm font-medium">
              거리별 매물 수 {isLoadingRanges && '(업데이트 중...)'}
            </span>
            <div className="space-y-2">
              {Object.entries(distanceRanges).map(([range, count]) => (
                <div
                  key={range}
                  className="flex items-center justify-between text-sm"
                >
                  <span className="text-muted-foreground">{range}</span>
                  <Badge variant="outline" className="text-xs">
                    {count}개
                  </Badge>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* No Location State */}
        {!currentLocation && (
          <div className="text-center py-6 text-muted-foreground">
            <MapPin className="h-8 w-8 mx-auto mb-2 opacity-50" />
            <p className="text-sm">위치를 설정하면</p>
            <p className="text-sm">거리 기반 검색이 가능합니다</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}