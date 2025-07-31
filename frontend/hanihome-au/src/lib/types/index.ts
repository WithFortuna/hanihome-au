// User Types
export interface User {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  avatar?: string;
  phone?: string;
  preferredLocations?: string[];
  createdAt: Date;
  updatedAt: Date;
}

export enum UserRole {
  TENANT = 'TENANT',
  LANDLORD = 'LANDLORD', 
  AGENT = 'AGENT',
  ADMIN = 'ADMIN',
}

// Property Types
export interface Property {
  id: string;
  title: string;
  description: string;
  address: string;
  suburb: string;
  state: string;
  postcode: string;
  coordinates: {
    lat: number;
    lng: number;
  };
  propertyType: PropertyType;
  price: number;
  bedrooms: number;
  bathrooms: number;
  parking: number;
  availableDate: Date;
  features: PropertyFeature[];
  images: PropertyImage[];
  status: PropertyStatus;
  landlordId: string;
  agentId?: string;
  createdAt: Date;
  updatedAt: Date;
}

export enum PropertyType {
  APARTMENT = 'APARTMENT',
  HOUSE = 'HOUSE',
  TOWNHOUSE = 'TOWNHOUSE',
  STUDIO = 'STUDIO',
  SHARED_ROOM = 'SHARED_ROOM',
}

export enum PropertyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE', 
  RENTED = 'RENTED',
  PENDING = 'PENDING',
}

export interface PropertyFeature {
  id: string;
  name: string;
  category: FeatureCategory;
}

export enum FeatureCategory {
  SECURITY = 'SECURITY',
  CONVENIENCE = 'CONVENIENCE',
  ENVIRONMENT = 'ENVIRONMENT',
}

export interface PropertyImage {
  id: string;
  url: string;
  alt: string;
  order: number;
  isThumbnail: boolean;
}

// Search Types
export interface SearchFilters {
  location?: string;
  minPrice?: number;
  maxPrice?: number;
  propertyType?: PropertyType[];
  bedrooms?: number;
  bathrooms?: number;
  features?: string[];
  sortBy?: SortOption;
}

export enum SortOption {
  PRICE_LOW = 'PRICE_LOW',
  PRICE_HIGH = 'PRICE_HIGH', 
  DATE_NEW = 'DATE_NEW',
  DATE_OLD = 'DATE_OLD',
  DISTANCE = 'DISTANCE',
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  message: string;
  success: boolean;
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}