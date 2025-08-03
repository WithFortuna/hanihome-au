'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  MapPin, 
  Navigation, 
  RefreshCw, 
  Search, 
  Train, 
  Bus, 
  Car,
  Walking,
  Bike,
  Building,
  School,
  ShoppingCart,
  Hospital,
  Coffee
} from 'lucide-react';

declare global {
  interface Window {
    daum: any;
    kakao: any;
  }
}

export interface AddressInfo {
  fullAddress: string;
  detailAddress?: string;
  zipCode?: string;
  city?: string;
  district?: string;
  coordinates: {
    lat: number;
    lng: number;
  };
  roadAddress?: string;
  jibunAddress?: string;
}

export interface NearbyFacility {
  id: string;
  name: string;
  type: 'subway' | 'bus' | 'school' | 'hospital' | 'mart' | 'cafe' | 'park';
  distance: number; // meters
  walkTime: number; // minutes
  coordinates: {
    lat: number;
    lng: number;
  };
}

interface EnhancedAddressSearchProps {
  onAddressChange: (addressInfo: AddressInfo) => void;
  onNearbyFacilitiesChange?: (facilities: NearbyFacility[]) => void;
  initialAddress?: string;
  initialCoordinates?: { lat: number; lng: number };
  showDetailAddressInput?: boolean;
  showNearbyFacilities?: boolean;
  className?: string;
}

export function EnhancedAddressSearch({
  onAddressChange,
  onNearbyFacilitiesChange,
  initialAddress = '',
  initialCoordinates,
  showDetailAddressInput = true,
  showNearbyFacilities = true,
  className = '',
}: EnhancedAddressSearchProps) {
  const [addressInput, setAddressInput] = useState(initialAddress);
  const [detailAddress, setDetailAddress] = useState('');
  const [selectedAddress, setSelectedAddress] = useState<AddressInfo | null>(null);
  const [nearbyFacilities, setNearbyFacilities] = useState<NearbyFacility[]>([]);
  const [isSearching, setIsSearching] = useState(false);
  const [isLoadingFacilities, setIsLoadingFacilities] = useState(false);
  const [activeTab, setActiveTab] = useState('search');

  // Initialize Daum Postcode API
  useEffect(() => {
    if (typeof window !== 'undefined' && !window.daum) {
      const script = document.createElement('script');
      script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  // Initialize Kakao Map API
  useEffect(() => {
    if (typeof window !== 'undefined' && !window.kakao) {
      const script = document.createElement('script');
      script.src = `//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_API_KEY}&libraries=services,clusterer`;
      script.async = true;
      document.head.appendChild(script);
    }
  }, []);

  const openDaumPostcode = useCallback(() => {
    if (!window.daum) {
      alert('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
      return;
    }

    new window.daum.Postcode({
      oncomplete: function(data: any) {
        // 도로명 주소 우선, 없으면 지번 주소 사용
        const fullAddress = data.roadAddress || data.jibunAddress;
        const addressInfo: AddressInfo = {
          fullAddress,
          roadAddress: data.roadAddress,
          jibunAddress: data.jibunAddress,
          zipCode: data.zonecode,
          city: data.sido,
          district: data.sigungu,
          coordinates: { lat: 0, lng: 0 }, // Will be filled by geocoding
          detailAddress,
        };

        setAddressInput(fullAddress);
        
        // Get coordinates using Kakao Geocoder
        if (window.kakao && window.kakao.maps) {
          const geocoder = new window.kakao.maps.services.Geocoder();
          geocoder.addressSearch(fullAddress, function(result: any, status: any) {
            if (status === window.kakao.maps.services.Status.OK) {
              const coords = {
                lat: parseFloat(result[0].y),
                lng: parseFloat(result[0].x),
              };
              
              const updatedAddressInfo = {
                ...addressInfo,
                coordinates: coords,
              };
              
              setSelectedAddress(updatedAddressInfo);
              onAddressChange(updatedAddressInfo);
              
              if (showNearbyFacilities) {
                searchNearbyFacilities(coords);
              }
              
              setActiveTab('map');
            }
          });
        }
      },
      width: '100%',
      height: '100%',
    }).open();
  }, [detailAddress, onAddressChange, showNearbyFacilities]);

  const searchByKeyword = useCallback(async () => {
    if (!addressInput.trim() || !window.kakao) return;

    setIsSearching(true);

    const geocoder = new window.kakao.maps.services.Geocoder();
    geocoder.addressSearch(addressInput, function(result: any, status: any) {
      setIsSearching(false);
      
      if (status === window.kakao.maps.services.Status.OK) {
        const coords = {
          lat: parseFloat(result[0].y),
          lng: parseFloat(result[0].x),
        };
        
        const addressInfo: AddressInfo = {
          fullAddress: result[0].address_name,
          roadAddress: result[0].road_address?.address_name,
          jibunAddress: result[0].address?.address_name,
          coordinates: coords,
          detailAddress,
        };
        
        setSelectedAddress(addressInfo);
        onAddressChange(addressInfo);
        
        if (showNearbyFacilities) {
          searchNearbyFacilities(coords);
        }
        
        setActiveTab('map');
      } else {
        alert('주소 검색 결과가 없습니다. 정확한 주소를 입력해주세요.');
      }
    });
  }, [addressInput, detailAddress, onAddressChange, showNearbyFacilities]);

  const searchNearbyFacilities = useCallback(async (coordinates: { lat: number; lng: number }) => {
    if (!window.kakao || !showNearbyFacilities) return;

    setIsLoadingFacilities(true);
    const facilities: NearbyFacility[] = [];

    try {
      const places = new window.kakao.maps.services.Places();
      
      // Search for different types of facilities
      const searchQueries = [
        { keyword: '지하철역', type: 'subway' as const },
        { keyword: '버스정류장', type: 'bus' as const },
        { keyword: '학교', type: 'school' as const },
        { keyword: '병원', type: 'hospital' as const },
        { keyword: '마트', type: 'mart' as const },
        { keyword: '카페', type: 'cafe' as const },
      ];

      const searchPromises = searchQueries.map(({ keyword, type }) => {
        return new Promise<void>((resolve) => {
          places.keywordSearch(keyword, (result: any[], status: any) => {
            if (status === window.kakao.maps.services.Status.OK) {
              result.slice(0, 3).forEach((place: any) => {
                const distance = calculateDistance(
                  coordinates.lat,
                  coordinates.lng,
                  parseFloat(place.y),
                  parseFloat(place.x)
                );
                
                if (distance <= 1000) { // Within 1km
                  facilities.push({
                    id: place.id,
                    name: place.place_name,
                    type,
                    distance: Math.round(distance),
                    walkTime: Math.round(distance / 80), // Approximate walking time
                    coordinates: {
                      lat: parseFloat(place.y),
                      lng: parseFloat(place.x),
                    },
                  });
                }
              });
            }
            resolve();
          }, {
            location: new window.kakao.maps.LatLng(coordinates.lat, coordinates.lng),
            radius: 1000,
          });
        });
      });

      await Promise.all(searchPromises);
      
      // Sort by distance
      facilities.sort((a, b) => a.distance - b.distance);
      
      setNearbyFacilities(facilities);
      onNearbyFacilitiesChange?.(facilities);
    } catch (error) {
      console.error('Failed to search nearby facilities:', error);
    } finally {
      setIsLoadingFacilities(false);
    }
  }, [showNearbyFacilities, onNearbyFacilitiesChange]);

  const calculateDistance = (lat1: number, lng1: number, lat2: number, lng2: number): number => {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = lat1 * Math.PI/180;
    const φ2 = lat2 * Math.PI/180;
    const Δφ = (lat2-lat1) * Math.PI/180;
    const Δλ = (lng2-lng1) * Math.PI/180;

    const a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ/2) * Math.sin(Δλ/2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

    return R * c;
  };

  const getCurrentLocation = useCallback(() => {
    if (!navigator.geolocation) {
      alert('브라우저에서 위치 서비스를 지원하지 않습니다.');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const coords = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
        };

        // Reverse geocoding to get address
        if (window.kakao) {
          const geocoder = new window.kakao.maps.services.Geocoder();
          geocoder.coord2Address(coords.lng, coords.lat, (result: any, status: any) => {
            if (status === window.kakao.maps.services.Status.OK) {
              const address = result[0].address || result[0].road_address;
              const addressInfo: AddressInfo = {
                fullAddress: address.address_name,
                coordinates: coords,
                detailAddress,
              };
              
              setAddressInput(address.address_name);
              setSelectedAddress(addressInfo);
              onAddressChange(addressInfo);
              
              if (showNearbyFacilities) {
                searchNearbyFacilities(coords);
              }
            }
          });
        }
      },
      (error) => {
        console.error('Geolocation error:', error);
        alert('현재 위치를 가져올 수 없습니다. 위치 권한을 확인해주세요.');
      }
    );
  }, [detailAddress, onAddressChange, showNearbyFacilities, searchNearbyFacilities]);

  const resetForm = useCallback(() => {
    setAddressInput('');
    setDetailAddress('');
    setSelectedAddress(null);
    setNearbyFacilities([]);
    setActiveTab('search');
  }, []);

  const getFacilityIcon = (type: NearbyFacility['type']) => {
    switch (type) {
      case 'subway': return <Train className="w-4 h-4" />;
      case 'bus': return <Bus className="w-4 h-4" />;
      case 'school': return <School className="w-4 h-4" />;
      case 'hospital': return <Hospital className="w-4 h-4" />;
      case 'mart': return <ShoppingCart className="w-4 h-4" />;
      case 'cafe': return <Coffee className="w-4 h-4" />;
      default: return <Building className="w-4 h-4" />;
    }
  };

  const getFacilityColor = (type: NearbyFacility['type']) => {
    switch (type) {
      case 'subway': return 'bg-blue-100 text-blue-800';
      case 'bus': return 'bg-green-100 text-green-800';
      case 'school': return 'bg-purple-100 text-purple-800';
      case 'hospital': return 'bg-red-100 text-red-800';
      case 'mart': return 'bg-orange-100 text-orange-800';
      case 'cafe': return 'bg-amber-100 text-amber-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className={`space-y-6 ${className}`}>
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="search">주소 검색</TabsTrigger>
          <TabsTrigger value="map" disabled={!selectedAddress}>지도 확인</TabsTrigger>
          <TabsTrigger value="facilities" disabled={!showNearbyFacilities || nearbyFacilities.length === 0}>
            주변 시설
          </TabsTrigger>
        </TabsList>

        <TabsContent value="search" className="space-y-4">
          <div className="space-y-4">
            <div>
              <Label htmlFor="address-search">주소 검색 *</Label>
              <div className="flex gap-2 mt-2">
                <Input
                  id="address-search"
                  value={addressInput}
                  onChange={(e) => setAddressInput(e.target.value)}
                  placeholder="주소를 입력하세요"
                  onKeyPress={(e) => e.key === 'Enter' && searchByKeyword()}
                />
                <Button 
                  type="button"
                  variant="outline"
                  onClick={searchByKeyword}
                  disabled={isSearching}
                >
                  {isSearching ? (
                    <RefreshCw className="w-4 h-4 animate-spin" />
                  ) : (
                    <Search className="w-4 h-4" />
                  )}
                </Button>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-2">
              <Button
                type="button"
                variant="outline"
                onClick={openDaumPostcode}
                className="flex items-center gap-2"
              >
                <MapPin className="w-4 h-4" />
                우편번호 검색
              </Button>
              
              <Button
                type="button"
                variant="outline"
                onClick={getCurrentLocation}
                className="flex items-center gap-2"
              >
                <Navigation className="w-4 h-4" />
                현재 위치
              </Button>
            </div>

            {showDetailAddressInput && (
              <div>
                <Label htmlFor="detail-address">상세 주소</Label>
                <Input
                  id="detail-address"
                  value={detailAddress}
                  onChange={(e) => setDetailAddress(e.target.value)}
                  placeholder="동, 호수 등 상세 주소 입력"
                />
              </div>
            )}
          </div>
        </TabsContent>

        <TabsContent value="map" className="space-y-4">
          {selectedAddress && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <MapPin className="w-5 h-5" />
                  선택된 주소
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <p className="font-medium">{selectedAddress.fullAddress}</p>
                  {selectedAddress.detailAddress && (
                    <p className="text-sm text-gray-600">{selectedAddress.detailAddress}</p>
                  )}
                </div>
                
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="text-gray-500">위도:</span>
                    <span className="ml-2 font-mono">{selectedAddress.coordinates.lat.toFixed(6)}</span>
                  </div>
                  <div>
                    <span className="text-gray-500">경도:</span>
                    <span className="ml-2 font-mono">{selectedAddress.coordinates.lng.toFixed(6)}</span>
                  </div>
                </div>

                {selectedAddress.zipCode && (
                  <div className="text-sm">
                    <span className="text-gray-500">우편번호:</span>
                    <span className="ml-2">{selectedAddress.zipCode}</span>
                  </div>
                )}

                <div className="flex gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={resetForm}
                  >
                    <RefreshCw className="w-4 h-4 mr-1" />
                    다시 선택
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        <TabsContent value="facilities" className="space-y-4">
          {isLoadingFacilities ? (
            <div className="text-center py-8">
              <RefreshCw className="w-8 h-8 animate-spin mx-auto mb-4 text-blue-500" />
              <p className="text-gray-600">주변 시설을 검색하고 있습니다...</p>
            </div>
          ) : nearbyFacilities.length > 0 ? (
            <div className="space-y-4">
              <h3 className="font-medium text-gray-900">주변 시설 정보</h3>
              <div className="grid gap-3">
                {nearbyFacilities.map((facility) => (
                  <div key={facility.id} className="flex items-center justify-between p-3 border rounded-lg">
                    <div className="flex items-center gap-3">
                      <div className={`p-2 rounded-full ${getFacilityColor(facility.type)}`}>
                        {getFacilityIcon(facility.type)}
                      </div>
                      <div>
                        <p className="font-medium text-sm">{facility.name}</p>
                        <div className="flex items-center gap-2 text-xs text-gray-500">
                          <span>{facility.distance}m</span>
                          <span>•</span>
                          <div className="flex items-center gap-1">
                            <Walking className="w-3 h-3" />
                            <span>{facility.walkTime}분</span>
                          </div>
                        </div>
                      </div>
                    </div>
                    <Badge variant="outline" className="text-xs">
                      {facility.type === 'subway' && '지하철'}
                      {facility.type === 'bus' && '버스'}
                      {facility.type === 'school' && '학교'}
                      {facility.type === 'hospital' && '병원'}
                      {facility.type === 'mart' && '마트'}
                      {facility.type === 'cafe' && '카페'}
                    </Badge>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div className="text-center py-8 text-gray-500">
              <Building className="w-12 h-12 mx-auto mb-4 text-gray-300" />
              <p>주변 시설 정보가 없습니다.</p>
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}