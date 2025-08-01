'use client';

import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { MapPin, Navigation2, Clock, Filter, SortAsc, SortDesc, Compass } from 'lucide-react';
import Image from 'next/image';

interface PropertyWithDistance {
  id: number;
  title: string;
  description: string;
  address: string;
  city: string;
  district: string;
  propertyType: string;
  rentalType: string;
  deposit: number;
  monthlyRent: number;
  area: number;
  rooms: number;
  bathrooms: number;
  floor: number;
  availableDate: string;
  latitude: number;
  longitude: number;
  distanceKm: number;
  distanceDisplay: string;
  direction: string;  // N, NE, E, SE, S, SW, W, NW
  bearing: number;
  pricePerSqm: number;
  priceRange: string;
  parkingAvailable: boolean;
  petAllowed: boolean;
  furnished: boolean;
  imageUrls: string[];
  createdDate: string;
}

interface SearchResponse {
  content: PropertyWithDistance[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

interface PropertySearchResultsProps {
  searchLocation?: {
    latitude: number;
    longitude: number;
  };
  maxDistance: number;
  filters: {
    minPrice?: number;
    maxPrice?: number;
    propertyType?: string;
    rentalType?: string;
    minRooms?: number;
    maxRooms?: number;
    parkingRequired?: boolean;
    petAllowed?: boolean;
    furnished?: boolean;
  };
  sortBy: string;
  sortDirection: string;
  onSortChange: (sortBy: string, direction: string) => void;
  className?: string;
}

export function PropertySearchResultsWithDistance({
  searchLocation,
  maxDistance,
  filters,
  sortBy,
  sortDirection,
  onSortChange,
  className = ''
}: PropertySearchResultsProps) {
  const [properties, setProperties] = useState<PropertyWithDistance[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Search properties with distance filter
  const searchProperties = async (page = 0) => {
    if (!searchLocation) return;

    setLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({
        latitude: searchLocation.latitude.toString(),
        longitude: searchLocation.longitude.toString(),
        maxDistanceKm: maxDistance.toString(),
        sortBy,
        sortDir: sortDirection,
        page: page.toString(),
        size: '20'
      });

      // Add filters
      if (filters.minPrice) params.append('minPrice', filters.minPrice.toString());
      if (filters.maxPrice) params.append('maxPrice', filters.maxPrice.toString());
      if (filters.propertyType) params.append('propertyType', filters.propertyType);
      if (filters.rentalType) params.append('rentalType', filters.rentalType);
      if (filters.minRooms) params.append('minRooms', filters.minRooms.toString());
      if (filters.maxRooms) params.append('maxRooms', filters.maxRooms.toString());
      if (filters.parkingRequired !== undefined) params.append('parkingRequired', filters.parkingRequired.toString());
      if (filters.petAllowed !== undefined) params.append('petAllowed', filters.petAllowed.toString());
      if (filters.furnished !== undefined) params.append('furnished', filters.furnished.toString());

      const response = await fetch(`/api/properties/distance-filter?${params}`);
      
      if (!response.ok) {
        throw new Error('검색 중 오류가 발생했습니다.');
      }

      const data = await response.json();
      const searchResult: SearchResponse = data.data;

      setProperties(searchResult.content);
      setTotalElements(searchResult.totalElements);
      setTotalPages(searchResult.totalPages);
      setCurrentPage(searchResult.number);
    } catch (error) {
      console.error('Search error:', error);
      setError(error instanceof Error ? error.message : '검색 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // Format price
  const formatPrice = (price: number): string => {
    if (price >= 1000000) {
      return `${(price / 1000000).toFixed(1)}백만원`;
    } else if (price >= 10000) {
      return `${(price / 10000).toFixed(0)}만원`;
    }
    return `${price.toLocaleString()}원`;
  };

  // Get direction emoji
  const getDirectionEmoji = (direction: string): string => {
    const directionMap: { [key: string]: string } = {
      'N': '⬆️', 'NE': '↗️', 'E': '➡️', 'SE': '↘️',
      'S': '⬇️', 'SW': '↙️', 'W': '⬅️', 'NW': '↖️'
    };
    return directionMap[direction] || '📍';
  };

  // Get price range color
  const getPriceRangeColor = (priceRange: string): string => {
    switch (priceRange) {
      case 'Budget-friendly': return 'bg-green-100 text-green-800';
      case 'Mid-range': return 'bg-blue-100 text-blue-800';
      case 'Premium': return 'bg-purple-100 text-purple-800';
      case 'Luxury': return 'bg-gold-100 text-gold-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // Sort options
  const sortOptions = [
    { value: 'distance', label: '거리순', icon: Navigation2 },
    { value: 'price', label: '가격순', icon: Filter },
    { value: 'area', label: '면적순', icon: Filter },
    { value: 'date', label: '등록일순', icon: Filter },
  ];

  // Handle sort change
  const handleSortChange = (newSortBy: string) => {
    if (newSortBy === sortBy) {
      // Toggle direction if same sort field
      const newDirection = sortDirection === 'asc' ? 'desc' : 'asc';
      onSortChange(sortBy, newDirection);
    } else {
      // New sort field, default to appropriate direction
      const direction = newSortBy === 'distance' ? 'asc' : 'desc';
      onSortChange(newSortBy, direction);
    }
  };

  // Effect to search when parameters change
  useEffect(() => {
    if (searchLocation) {
      searchProperties(0);
    }
  }, [searchLocation, maxDistance, filters, sortBy, sortDirection]);

  // Load more results
  const loadMore = () => {
    if (currentPage < totalPages - 1) {
      searchProperties(currentPage + 1);
    }
  };

  if (!searchLocation) {
    return (
      <Card className={className}>
        <CardContent className="flex items-center justify-center h-40">
          <div className="text-center text-muted-foreground">
            <MapPin className="h-8 w-8 mx-auto mb-2 opacity-50" />
            <p>위치를 설정하면 검색 결과를 확인할 수 있습니다</p>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className={`space-y-4 ${className}`}>
      {/* Search Summary and Sort Controls */}
      <Card>
        <CardHeader className="pb-3">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <MapPin className="h-4 w-4" />
              반경 {maxDistance}km 내 총 {totalElements.toLocaleString()}건
            </div>
            
            <div className="flex items-center gap-2">
              {sortOptions.map((option) => {
                const Icon = option.icon;
                const isActive = sortBy === option.value;
                return (
                  <Button
                    key={option.value}
                    variant={isActive ? "default" : "outline"}
                    size="sm"
                    onClick={() => handleSortChange(option.value)}
                    className="text-xs"
                  >
                    <Icon className="h-3 w-3 mr-1" />
                    {option.label}
                    {isActive && (
                      sortDirection === 'asc' ? 
                        <SortAsc className="h-3 w-3 ml-1" /> : 
                        <SortDesc className="h-3 w-3 ml-1" />
                    )}
                  </Button>
                );
              })}
            </div>
          </div>
        </CardHeader>
      </Card>

      {/* Error State */}
      {error && (
        <Card>
          <CardContent className="flex items-center justify-center h-40">
            <div className="text-center text-red-500">
              <p>{error}</p>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Loading State */}
      {loading && properties.length === 0 && (
        <div className="space-y-4">
          {[...Array(3)].map((_, i) => (
            <Card key={i}>
              <CardContent className="p-4">
                <div className="flex gap-4">
                  <Skeleton className="w-24 h-24 rounded" />
                  <div className="flex-1 space-y-2">
                    <Skeleton className="h-4 w-3/4" />
                    <Skeleton className="h-3 w-1/2" />
                    <Skeleton className="h-3 w-2/3" />
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      {/* Property Results */}
      {properties.length > 0 && (
        <div className="space-y-4">
          {properties.map((property) => (
            <Card key={property.id} className="hover:shadow-md transition-shadow">
              <CardContent className="p-4">
                <div className="flex gap-4">
                  {/* Property Image */}
                  <div className="relative w-24 h-24 rounded overflow-hidden bg-gray-100">
                    {property.imageUrls && property.imageUrls.length > 0 ? (
                      <Image
                        src={property.imageUrls[0]}
                        alt={property.title}
                        fill
                        className="object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center bg-gray-200">
                        <MapPin className="h-6 w-6 text-gray-400" />
                      </div>
                    )}
                  </div>

                  {/* Property Info */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between mb-2">
                      <h3 className="font-medium text-sm truncate mr-2">
                        {property.title}
                      </h3>
                      <div className="flex flex-col items-end gap-1">
                        <Badge className={`text-xs ${getPriceRangeColor(property.priceRange)}`}>
                          {property.priceRange}
                        </Badge>
                      </div>
                    </div>

                    <div className="space-y-1 text-xs text-muted-foreground">
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3" />
                        {property.district}, {property.city}
                      </div>
                      
                      <div className="flex items-center gap-1">
                        <Navigation2 className="h-3 w-3" />
                        {property.distanceDisplay} 
                        <span className="mx-1">•</span>
                        <Compass className="h-3 w-3" />
                        {getDirectionEmoji(property.direction)} {property.direction}
                      </div>

                      <div className="flex items-center gap-1">
                        <Clock className="h-3 w-3" />
                        도보 {Math.round(property.distanceKm * 12)}분
                      </div>
                    </div>

                    <div className="flex items-center justify-between mt-2">
                      <div className="flex items-center gap-2 text-xs">
                        <span className="font-medium">
                          월세 {formatPrice(property.monthlyRent)}
                        </span>
                        {property.deposit > 0 && (
                          <span className="text-muted-foreground">
                            (보증금 {formatPrice(property.deposit)})
                          </span>
                        )}
                      </div>

                      <div className="flex items-center gap-1 text-xs text-muted-foreground">
                        <span>{property.rooms}룸</span>
                        <span>•</span>
                        <span>{property.area}m²</span>
                        {property.pricePerSqm && (
                          <>
                            <span>•</span>
                            <span>{formatPrice(property.pricePerSqm)}/m²</span>
                          </>
                        )}
                      </div>
                    </div>

                    {/* Property Features */}
                    <div className="flex items-center gap-1 mt-2">
                      {property.parkingAvailable && (
                        <Badge variant="outline" className="text-xs">주차</Badge>
                      )}
                      {property.petAllowed && (
                        <Badge variant="outline" className="text-xs">펫</Badge>
                      )}
                      {property.furnished && (
                        <Badge variant="outline" className="text-xs">가구</Badge>
                      )}
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}

          {/* Load More Button */}
          {currentPage < totalPages - 1 && (
            <div className="flex justify-center pt-4">
              <Button 
                variant="outline" 
                onClick={loadMore} 
                disabled={loading}
              >
                {loading ? '로딩 중...' : '더 보기'}
              </Button>
            </div>
          )}
        </div>
      )}

      {/* No Results */}
      {!loading && properties.length === 0 && !error && searchLocation && (
        <Card>
          <CardContent className="flex items-center justify-center h-40">
            <div className="text-center text-muted-foreground">
              <MapPin className="h-8 w-8 mx-auto mb-2 opacity-50" />
              <p>검색 조건에 맞는 매물이 없습니다</p>
              <p className="text-sm">검색 범위나 조건을 조정해보세요</p>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}