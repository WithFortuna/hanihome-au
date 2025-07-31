/**
 * Google Places API Utilities
 */

import { PlaceResult, MapPosition, AddressComponent } from './types';
import { AUSTRALIA_BOUNDS } from './config';

export class PlacesService {
  private service: google.maps.places.PlacesService | null = null;
  private autocompleteService: google.maps.places.AutocompleteService | null = null;

  constructor(map?: google.maps.Map) {
    if (map) {
      this.service = new google.maps.places.PlacesService(map);
    }
    this.autocompleteService = new google.maps.places.AutocompleteService();
  }

  /**
   * Get place predictions for autocomplete
   */
  async getPlacePredictions(
    input: string,
    options?: {
      location?: MapPosition;
      radius?: number;
      types?: string[];
      componentRestrictions?: { country: string };
    }
  ): Promise<google.maps.places.AutocompletePrediction[]> {
    if (!this.autocompleteService || !input.trim()) {
      return [];
    }

    return new Promise((resolve, reject) => {
      const request: google.maps.places.AutocompletionRequest = {
        input: input.trim(),
        componentRestrictions: options?.componentRestrictions || { country: 'AU' },
        types: options?.types || ['address'],
      };

      if (options?.location) {
        request.location = new google.maps.LatLng(
          options.location.lat,
          options.location.lng
        );
        request.radius = options.radius || 50000; // 50km default
      }

      this.autocompleteService!.getPlacePredictions(
        request,
        (predictions, status) => {
          if (status === google.maps.places.PlacesServiceStatus.OK && predictions) {
            resolve(predictions);
          } else if (status === google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
            resolve([]);
          } else {
            reject(new Error(`Places service error: ${status}`));
          }
        }
      );
    });
  }

  /**
   * Get detailed place information by Place ID
   */
  async getPlaceDetails(placeId: string): Promise<PlaceResult> {
    if (!this.service) {
      throw new Error('PlacesService not initialized with map');
    }

    return new Promise((resolve, reject) => {
      const request: google.maps.places.PlaceDetailsRequest = {
        placeId,
        fields: [
          'place_id',
          'formatted_address',
          'geometry.location',
          'geometry.viewport',
          'address_components',
          'name',
          'types'
        ],
      };

      this.service!.getDetails(request, (place, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && place) {
          const placeResult: PlaceResult = {
            place_id: place.place_id || '',
            formatted_address: place.formatted_address || '',
            geometry: place.geometry ? {
              location: {
                lat: place.geometry.location?.lat() || 0,
                lng: place.geometry.location?.lng() || 0,
              },
              viewport: place.geometry.viewport ? {
                north: place.geometry.viewport.getNorthEast().lat(),
                south: place.geometry.viewport.getSouthWest().lat(),
                east: place.geometry.viewport.getNorthEast().lng(),
                west: place.geometry.viewport.getSouthWest().lng(),
              } : undefined,
            } : undefined,
            address_components: place.address_components?.map(component => ({
              long_name: component.long_name,
              short_name: component.short_name,
              types: component.types,
            })),
            name: place.name,
            types: place.types,
          };
          resolve(placeResult);
        } else {
          reject(new Error(`Place details error: ${status}`));
        }
      });
    });
  }

  /**
   * Search for places nearby
   */
  async nearbySearch(
    location: MapPosition,
    radius: number = 5000,
    type?: string
  ): Promise<PlaceResult[]> {
    if (!this.service) {
      throw new Error('PlacesService not initialized with map');
    }

    return new Promise((resolve, reject) => {
      const request: google.maps.places.PlaceSearchRequest = {
        location: new google.maps.LatLng(location.lat, location.lng),
        radius,
        type: type as any,
      };

      this.service!.nearbySearch(request, (results, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && results) {
          const placeResults: PlaceResult[] = results.map(place => ({
            place_id: place.place_id || '',
            formatted_address: place.vicinity || '',
            geometry: place.geometry ? {
              location: {
                lat: place.geometry.location?.lat() || 0,
                lng: place.geometry.location?.lng() || 0,
              },
            } : undefined,
            name: place.name,
            types: place.types,
          }));
          resolve(placeResults);
        } else if (status === google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
          resolve([]);
        } else {
          reject(new Error(`Nearby search error: ${status}`));
        }
      });
    });
  }

  /**
   * Text search for places
   */
  async textSearch(query: string, options?: {
    location?: MapPosition;
    radius?: number;
  }): Promise<PlaceResult[]> {
    if (!this.service) {
      throw new Error('PlacesService not initialized with map');
    }

    return new Promise((resolve, reject) => {
      const request: google.maps.places.TextSearchRequest = {
        query,
      };

      if (options?.location) {
        request.location = new google.maps.LatLng(
          options.location.lat,
          options.location.lng
        );
        request.radius = options.radius || 50000;
      }

      this.service!.textSearch(request, (results, status) => {
        if (status === google.maps.places.PlacesServiceStatus.OK && results) {
          const placeResults: PlaceResult[] = results.map(place => ({
            place_id: place.place_id || '',
            formatted_address: place.formatted_address || '',
            geometry: place.geometry ? {
              location: {
                lat: place.geometry.location?.lat() || 0,
                lng: place.geometry.location?.lng() || 0,
              },
            } : undefined,
            name: place.name,
            types: place.types,
          }));
          resolve(placeResults);
        } else if (status === google.maps.places.PlacesServiceStatus.ZERO_RESULTS) {
          resolve([]);
        } else {
          reject(new Error(`Text search error: ${status}`));
        }
      });
    });
  }
}

/**
 * Extract specific address components from place result
 */
export function extractAddressComponents(place: PlaceResult) {
  const components = place.address_components || [];
  
  const getComponent = (types: string[]) => {
    return components.find(component => 
      types.some(type => component.types.includes(type))
    );
  };

  return {
    streetNumber: getComponent(['street_number'])?.long_name || '',
    streetName: getComponent(['route'])?.long_name || '',
    suburb: getComponent(['locality', 'sublocality'])?.long_name || '',
    city: getComponent(['administrative_area_level_2'])?.long_name || '',
    state: getComponent(['administrative_area_level_1'])?.short_name || '',
    postcode: getComponent(['postal_code'])?.long_name || '',
    country: getComponent(['country'])?.long_name || '',
    countryCode: getComponent(['country'])?.short_name || '',
  };
}

/**
 * Format address for display
 */
export function formatAddress(place: PlaceResult, format: 'full' | 'short' | 'suburb' = 'full'): string {
  if (format === 'full') {
    return place.formatted_address;
  }

  const components = extractAddressComponents(place);
  
  if (format === 'short') {
    return [components.streetNumber, components.streetName]
      .filter(Boolean)
      .join(' ') || place.formatted_address;
  }

  if (format === 'suburb') {
    return [components.suburb, components.state, components.postcode]
      .filter(Boolean)
      .join(', ') || place.formatted_address;
  }

  return place.formatted_address;
}

/**
 * Check if place is within Australia bounds
 */
export function isWithinAustralia(position: MapPosition): boolean {
  return (
    position.lat >= AUSTRALIA_BOUNDS.south &&
    position.lat <= AUSTRALIA_BOUNDS.north &&
    position.lng >= AUSTRALIA_BOUNDS.west &&
    position.lng <= AUSTRALIA_BOUNDS.east
  );
}

/**
 * Calculate distance between two positions (in kilometers)
 */
export function calculateDistance(pos1: MapPosition, pos2: MapPosition): number {
  const R = 6371; // Earth's radius in kilometers
  const dLat = (pos2.lat - pos1.lat) * Math.PI / 180;
  const dLng = (pos2.lng - pos1.lng) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(pos1.lat * Math.PI / 180) * Math.cos(pos2.lat * Math.PI / 180) * 
    Math.sin(dLng/2) * Math.sin(dLng/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  return R * c;
}