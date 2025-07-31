/**
 * Google Maps TypeScript Definitions
 */

export interface MapPosition {
  lat: number;
  lng: number;
}

export interface MapBounds {
  north: number;
  south: number;
  east: number;
  west: number;
}

export interface PropertyMarker {
  id: string;
  position: MapPosition;
  title: string;
  price?: number;
  propertyType?: string;
  bedrooms?: number;
  bathrooms?: number;
  imageUrl?: string;
}

export interface MapCluster {
  count: number;
  position: MapPosition;
  markers: PropertyMarker[];
}

export interface AddressComponent {
  long_name: string;
  short_name: string;
  types: string[];
}

export interface PlaceResult {
  place_id: string;
  formatted_address: string;
  geometry?: {
    location: MapPosition;
    viewport?: MapBounds;
  };
  address_components?: AddressComponent[];
  name?: string;
  types?: string[];
}

export interface SearchFilters {
  bounds?: MapBounds;
  radius?: number;
  center?: MapPosition;
  minPrice?: number;
  maxPrice?: number;
  propertyType?: string;
  bedrooms?: number;
  bathrooms?: number;
}

export interface MapEventHandlers {
  onBoundsChanged?: (bounds: MapBounds) => void;
  onCenterChanged?: (center: MapPosition) => void;
  onZoomChanged?: (zoom: number) => void;
  onMarkerClick?: (marker: PropertyMarker) => void;
  onMapClick?: (position: MapPosition) => void;
}