/**
 * Property Management Dashboard
 */

'use client';

import React, { useState, useMemo } from 'react';
import { 
  PlusIcon, 
  FunnelIcon, 
  MagnifyingGlassIcon,
  AdjustmentsHorizontalIcon,
  ViewColumnsIcon,
  ChartBarIcon,
} from '@heroicons/react/24/outline';
import { PropertyFormData, PropertyStatus, PropertyType, RentalType } from '@/lib/types/property';
import PropertyStatsCards from './dashboard/property-stats-cards';
import PropertyListTable from './dashboard/property-list-table';
import PropertyFilters from './dashboard/property-filters';
import { useMobileDetection } from '@/hooks/use-mobile-detection';

interface PropertyDashboardProps {
  properties: PropertyWithStats[];
  onCreateProperty?: () => void;
  onEditProperty?: (property: PropertyWithStats) => void;
  onDeleteProperty?: (propertyId: string) => void;
  onStatusChange?: (propertyId: string, status: PropertyStatus) => void;
  isLoading?: boolean;
}

export interface PropertyWithStats extends PropertyFormData {
  id: string;
  status: PropertyStatus;
  createdAt: Date;
  updatedAt: Date;
  views: number;
  inquiries: number;
  favorites: number;
  userId: string;
  isActive: boolean;
}

export interface PropertyFilters {
  search: string;
  status: PropertyStatus | 'ALL';
  propertyType: PropertyType | 'ALL';
  rentalType: RentalType | 'ALL';
  sortBy: 'createdAt' | 'updatedAt' | 'views' | 'inquiries' | 'title';
  sortOrder: 'asc' | 'desc';
  dateRange: {
    from?: Date;
    to?: Date;
  };
}

export default function PropertyDashboard({
  properties = [],
  onCreateProperty,
  onEditProperty,
  onDeleteProperty,
  onStatusChange,
  isLoading = false,
}: PropertyDashboardProps) {
  const { isMobile, isTablet } = useMobileDetection();
  const [filters, setFilters] = useState<PropertyFilters>({
    search: '',
    status: 'ALL',
    propertyType: 'ALL',
    rentalType: 'ALL',
    sortBy: 'updatedAt',
    sortOrder: 'desc',
    dateRange: {},
  });
  const [showFilters, setShowFilters] = useState(false);
  const [viewMode, setViewMode] = useState<'table' | 'cards'>('table');

  // Filter and sort properties
  const filteredProperties = useMemo(() => {
    let filtered = [...properties];

    // Apply search filter
    if (filters.search) {
      const searchLower = filters.search.toLowerCase();
      filtered = filtered.filter(property =>
        property.title.toLowerCase().includes(searchLower) ||
        property.description.toLowerCase().includes(searchLower) ||
        property.address.toLowerCase().includes(searchLower)
      );
    }

    // Apply status filter
    if (filters.status !== 'ALL') {
      filtered = filtered.filter(property => property.status === filters.status);
    }

    // Apply property type filter
    if (filters.propertyType !== 'ALL') {
      filtered = filtered.filter(property => property.propertyType === filters.propertyType);
    }

    // Apply rental type filter
    if (filters.rentalType !== 'ALL') {
      filtered = filtered.filter(property => property.rentalType === filters.rentalType);
    }

    // Apply date range filter
    if (filters.dateRange.from) {
      filtered = filtered.filter(property => property.createdAt >= filters.dateRange.from!);
    }
    if (filters.dateRange.to) {
      filtered = filtered.filter(property => property.createdAt <= filters.dateRange.to!);
    }

    // Apply sorting
    filtered.sort((a, b) => {
      const aValue = a[filters.sortBy];
      const bValue = b[filters.sortBy];
      
      let comparison = 0;
      if (aValue < bValue) comparison = -1;
      if (aValue > bValue) comparison = 1;
      
      return filters.sortOrder === 'desc' ? -comparison : comparison;
    });

    return filtered;
  }, [properties, filters]);

  // Calculate statistics
  const stats = useMemo(() => {
    const total = properties.length;
    const active = properties.filter(p => p.status === PropertyStatus.ACTIVE).length;
    const pending = properties.filter(p => p.status === PropertyStatus.PENDING_APPROVAL).length;
    const inactive = properties.filter(p => p.status === PropertyStatus.INACTIVE).length;
    const totalViews = properties.reduce((sum, p) => sum + p.views, 0);
    const totalInquiries = properties.reduce((sum, p) => sum + p.inquiries, 0);

    return {
      total,
      active,
      pending,
      inactive,
      totalViews,
      totalInquiries,
    };
  }, [properties]);

  const handleFilterChange = (newFilters: Partial<PropertyFilters>) => {
    setFilters(prev => ({ ...prev, ...newFilters }));
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Dashboard Header */}
      <div className="mb-8">
        <div className="sm:flex sm:items-center sm:justify-between">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">매물 관리</h1>
            <p className="mt-2 text-gray-600">등록된 매물들을 관리하고 성과를 추적하세요.</p>
          </div>
          <div className="mt-4 sm:mt-0 sm:ml-16 sm:flex-none">
            <button
              type="button"
              onClick={onCreateProperty}
              className="inline-flex items-center justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600"
            >
              <PlusIcon className="-ml-0.5 mr-1.5 h-5 w-5" aria-hidden="true" />
              새 매물 등록
            </button>
          </div>
        </div>
      </div>

      {/* Statistics Cards */}
      <PropertyStatsCards stats={stats} isLoading={isLoading} />

      {/* Search and Filter Controls */}
      <div className="mb-6">
        <div className="sm:flex sm:items-center sm:justify-between">
          {/* Search Bar */}
          <div className="relative flex-1 max-w-lg">
            <div className="pointer-events-none absolute inset-y-0 left-0 flex items-center pl-3">
              <MagnifyingGlassIcon className="h-5 w-5 text-gray-400" aria-hidden="true" />
            </div>
            <input
              type="text"
              name="search"
              id="search"
              className="block w-full rounded-md border-0 py-1.5 pl-10 pr-3 text-gray-900 ring-1 ring-inset ring-gray-300 placeholder:text-gray-400 focus:ring-2 focus:ring-inset focus:ring-blue-600 sm:text-sm sm:leading-6"
              placeholder="매물명, 주소로 검색..."
              value={filters.search}
              onChange={(e) => handleFilterChange({ search: e.target.value })}
            />
          </div>

          {/* Action Buttons */}
          <div className="mt-4 sm:mt-0 flex items-center space-x-2">
            {/* View Mode Toggle */}
            <div className="hidden sm:flex rounded-md shadow-sm">
              <button
                type="button"
                onClick={() => setViewMode('table')}
                className={`relative inline-flex items-center rounded-l-md px-3 py-2 text-sm font-semibold ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-10 ${
                  viewMode === 'table'
                    ? 'bg-blue-50 text-blue-700 ring-blue-600'
                    : 'bg-white text-gray-900'
                }`}
              >
                <ViewColumnsIcon className="h-4 w-4" />
                <span className="ml-1">테이블</span>
              </button>
              <button
                type="button"
                onClick={() => setViewMode('cards')}
                className={`relative -ml-px inline-flex items-center rounded-r-md px-3 py-2 text-sm font-semibold ring-1 ring-inset ring-gray-300 hover:bg-gray-50 focus:z-10 ${
                  viewMode === 'cards'
                    ? 'bg-blue-50 text-blue-700 ring-blue-600'
                    : 'bg-white text-gray-900'
                }`}
              >
                <ChartBarIcon className="h-4 w-4" />
                <span className="ml-1">카드</span>
              </button>
            </div>

            {/* Filter Toggle */}
            <button
              type="button"
              onClick={() => setShowFilters(!showFilters)}
              className={`inline-flex items-center rounded-md px-3 py-2 text-sm font-semibold ring-1 ring-inset ring-gray-300 hover:bg-gray-50 ${
                showFilters
                  ? 'bg-blue-50 text-blue-700 ring-blue-600'
                  : 'bg-white text-gray-900'
              }`}
            >
              <FunnelIcon className="h-4 w-4" />
              <span className="ml-1">필터</span>
            </button>

            {/* Sort Button */}
            <button
              type="button"
              className="inline-flex items-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 ring-1 ring-inset ring-gray-300 hover:bg-gray-50"
            >
              <AdjustmentsHorizontalIcon className="h-4 w-4" />
              <span className="ml-1">정렬</span>
            </button>
          </div>
        </div>

        {/* Filters Panel */}
        {showFilters && (
          <div className="mt-4">
            <PropertyFilters
              filters={filters}
              onFiltersChange={handleFilterChange}
            />
          </div>
        )}
      </div>

      {/* Results Summary */}
      <div className="mb-4">
        <p className="text-sm text-gray-700">
          총 <span className="font-medium">{filteredProperties.length}</span>개의 매물이 있습니다.
          {filters.search && (
            <span className="ml-1">
              &ldquo;<span className="font-medium">{filters.search}</span>&rdquo; 검색 결과
            </span>
          )}
        </p>
      </div>

      {/* Property List */}
      <PropertyListTable
        properties={filteredProperties}
        viewMode={viewMode}
        isLoading={isLoading}
        onEdit={onEditProperty}
        onDelete={onDeleteProperty}
        onStatusChange={onStatusChange}
        filters={filters}
        onFiltersChange={handleFilterChange}
      />
    </div>
  );
}