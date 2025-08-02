/**
 * Property Filters Component
 */

'use client';

import React from 'react';
import { 
  PropertyStatus, 
  PropertyType, 
  RentalType,
  PropertyTypeDisplayNames,
  RentalTypeDisplayNames
} from '@/lib/types/property';
import { PropertyFilters as FilterType } from '../property-dashboard';

interface PropertyFiltersProps {
  filters: FilterType;
  onFiltersChange: (filters: Partial<FilterType>) => void;
}

const statusOptions = [
  { value: 'ALL', label: '전체 상태' },
  { value: PropertyStatus.ACTIVE, label: '활성' },
  { value: PropertyStatus.PENDING_APPROVAL, label: '승인 대기' },
  { value: PropertyStatus.INACTIVE, label: '비활성' },
  { value: PropertyStatus.SUSPENDED, label: '일시정지' },
  { value: PropertyStatus.REJECTED, label: '거절됨' },
  { value: PropertyStatus.COMPLETED, label: '완료' },
];

const propertyTypeOptions = [
  { value: 'ALL', label: '전체 유형' },
  ...Object.entries(PropertyTypeDisplayNames).map(([value, label]) => ({
    value,
    label,
  })),
];

const rentalTypeOptions = [
  { value: 'ALL', label: '전체 임대유형' },
  ...Object.entries(RentalTypeDisplayNames).map(([value, label]) => ({
    value,
    label,
  })),
];

const sortOptions = [
  { value: 'updatedAt', label: '최근 수정일' },
  { value: 'createdAt', label: '등록일' },
  { value: 'views', label: '조회수' },
  { value: 'inquiries', label: '문의수' },
  { value: 'title', label: '제목' },
];

export default function PropertyFilters({ filters, onFiltersChange }: PropertyFiltersProps) {
  const handleDateChange = (field: 'from' | 'to', value: string) => {
    const date = value ? new Date(value) : undefined;
    onFiltersChange({
      dateRange: {
        ...filters.dateRange,
        [field]: date,
      },
    });
  };

  const clearFilters = () => {
    onFiltersChange({
      search: '',
      status: 'ALL',
      propertyType: 'ALL',
      rentalType: 'ALL',
      sortBy: 'updatedAt',
      sortOrder: 'desc',
      dateRange: {},
    });
  };

  const hasActiveFilters = 
    filters.search ||
    filters.status !== 'ALL' ||
    filters.propertyType !== 'ALL' ||
    filters.rentalType !== 'ALL' ||
    filters.dateRange.from ||
    filters.dateRange.to;

  return (
    <div className="bg-gray-50 rounded-lg p-4 border border-gray-200">
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-6">
        {/* Status Filter */}
        <div>
          <label htmlFor="status" className="block text-xs font-medium text-gray-700 mb-1">
            상태
          </label>
          <select
            id="status"
            name="status"
            className="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
            value={filters.status}
            onChange={(e) => onFiltersChange({ status: e.target.value as PropertyStatus | 'ALL' })}
          >
            {statusOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Property Type Filter */}
        <div>
          <label htmlFor="propertyType" className="block text-xs font-medium text-gray-700 mb-1">
            매물 유형
          </label>
          <select
            id="propertyType"
            name="propertyType"
            className="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
            value={filters.propertyType}
            onChange={(e) => onFiltersChange({ propertyType: e.target.value as PropertyType | 'ALL' })}
          >
            {propertyTypeOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Rental Type Filter */}
        <div>
          <label htmlFor="rentalType" className="block text-xs font-medium text-gray-700 mb-1">
            임대 유형
          </label>
          <select
            id="rentalType"
            name="rentalType"
            className="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
            value={filters.rentalType}
            onChange={(e) => onFiltersChange({ rentalType: e.target.value as RentalType | 'ALL' })}
          >
            {rentalTypeOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Sort By */}
        <div>
          <label htmlFor="sortBy" className="block text-xs font-medium text-gray-700 mb-1">
            정렬 기준
          </label>
          <select
            id="sortBy"
            name="sortBy"
            className="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
            value={filters.sortBy}
            onChange={(e) => onFiltersChange({ sortBy: e.target.value as FilterType['sortBy'] })}
          >
            {sortOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        {/* Sort Order */}
        <div>
          <label htmlFor="sortOrder" className="block text-xs font-medium text-gray-700 mb-1">
            정렬 순서
          </label>
          <select
            id="sortOrder"
            name="sortOrder"
            className="mt-1 block w-full rounded-md border-gray-300 py-2 pl-3 pr-10 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
            value={filters.sortOrder}
            onChange={(e) => onFiltersChange({ sortOrder: e.target.value as 'asc' | 'desc' })}
          >
            <option value="desc">내림차순</option>
            <option value="asc">오름차순</option>
          </select>
        </div>

        {/* Clear Filters Button */}
        <div className="flex items-end">
          <button
            type="button"
            onClick={clearFilters}
            disabled={!hasActiveFilters}
            className="inline-flex justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
          >
            필터 초기화
          </button>
        </div>
      </div>

      {/* Date Range Filters */}
      <div className="mt-4 pt-4 border-t border-gray-200">
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <div>
            <label htmlFor="dateFrom" className="block text-xs font-medium text-gray-700 mb-1">
              등록일 시작
            </label>
            <input
              type="date"
              id="dateFrom"
              name="dateFrom"
              className="mt-1 block w-full rounded-md border-gray-300 py-2 px-3 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
              value={filters.dateRange.from ? filters.dateRange.from.toISOString().split('T')[0] : ''}
              onChange={(e) => handleDateChange('from', e.target.value)}
            />
          </div>
          <div>
            <label htmlFor="dateTo" className="block text-xs font-medium text-gray-700 mb-1">
              등록일 끝
            </label>
            <input
              type="date"
              id="dateTo"
              name="dateTo"
              className="mt-1 block w-full rounded-md border-gray-300 py-2 px-3 text-sm focus:border-blue-500 focus:outline-none focus:ring-blue-500"
              value={filters.dateRange.to ? filters.dateRange.to.toISOString().split('T')[0] : ''}
              onChange={(e) => handleDateChange('to', e.target.value)}
            />
          </div>
          <div className="flex items-end">
            <div className="text-sm text-gray-600">
              {hasActiveFilters && (
                <span className="inline-flex items-center rounded-full bg-blue-100 px-2.5 py-0.5 text-xs font-medium text-blue-800">
                  필터 활성화
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}