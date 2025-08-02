'use client';

import React, { useState } from 'react';
import {
  PencilIcon,
  TrashIcon,
  EyeIcon,
  MapPinIcon,
  CurrencyDollarIcon,
  CalendarIcon,
  ChartBarIcon,
  ExclamationTriangleIcon,
  CheckCircleIcon,
  XCircleIcon,
} from '@heroicons/react/24/outline';
import { PropertyWithStats, PropertyFilters } from '../property-dashboard';
import { PropertyStatus, PropertyType, RentalType } from '@/lib/types/property';
import PropertyEditModal from './property-edit-modal';
import PropertyDeleteDialog from './property-delete-dialog';

interface PropertyListTableProps {
  properties: PropertyWithStats[];
  viewMode: 'table' | 'cards';
  isLoading: boolean;
  onEdit?: (property: PropertyWithStats) => void;
  onDelete?: (propertyId: string) => void;
  onStatusChange?: (propertyId: string, status: PropertyStatus) => void;
  filters: PropertyFilters;
  onFiltersChange: (filters: Partial<PropertyFilters>) => void;
}

const getStatusColor = (status: PropertyStatus) => {
  switch (status) {
    case PropertyStatus.ACTIVE:
      return 'bg-green-100 text-green-800';
    case PropertyStatus.PENDING_APPROVAL:
      return 'bg-yellow-100 text-yellow-800';
    case PropertyStatus.INACTIVE:
      return 'bg-gray-100 text-gray-800';
    case PropertyStatus.REJECTED:
      return 'bg-red-100 text-red-800';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

const getStatusIcon = (status: PropertyStatus) => {
  switch (status) {
    case PropertyStatus.ACTIVE:
      return <CheckCircleIcon className="h-4 w-4" />;
    case PropertyStatus.PENDING_APPROVAL:
      return <ExclamationTriangleIcon className="h-4 w-4" />;
    case PropertyStatus.INACTIVE:
      return <XCircleIcon className="h-4 w-4" />;
    case PropertyStatus.REJECTED:
      return <XCircleIcon className="h-4 w-4" />;
    default:
      return <XCircleIcon className="h-4 w-4" />;
  }
};

const getStatusText = (status: PropertyStatus) => {
  switch (status) {
    case PropertyStatus.ACTIVE:
      return '활성';
    case PropertyStatus.PENDING_APPROVAL:
      return '승인 대기';
    case PropertyStatus.INACTIVE:
      return '비활성';
    case PropertyStatus.REJECTED:
      return '거부됨';
    default:
      return '알 수 없음';
  }
};

const formatCurrency = (amount: number) => {
  return new Intl.NumberFormat('ko-KR', {
    style: 'currency',
    currency: 'KRW',
    minimumFractionDigits: 0,
  }).format(amount);
};

const formatDate = (date: Date) => {
  return new Intl.DateTimeFormat('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  }).format(date);
};

export default function PropertyListTable({
  properties,
  viewMode,
  isLoading,
  onEdit,
  onDelete,
  onStatusChange,
  filters,
  onFiltersChange,
}: PropertyListTableProps) {
  const [editingProperty, setEditingProperty] = useState<PropertyWithStats | null>(null);
  const [deletingProperty, setDeletingProperty] = useState<PropertyWithStats | null>(null);

  const handleEdit = (property: PropertyWithStats) => {
    setEditingProperty(property);
    onEdit?.(property);
  };

  const handleDelete = (property: PropertyWithStats) => {
    setDeletingProperty(property);
  };

  const handleConfirmDelete = () => {
    if (deletingProperty) {
      onDelete?.(deletingProperty.id);
      setDeletingProperty(null);
    }
  };

  const handleStatusToggle = (property: PropertyWithStats) => {
    const newStatus = property.status === PropertyStatus.ACTIVE 
      ? PropertyStatus.INACTIVE 
      : PropertyStatus.ACTIVE;
    onStatusChange?.(property.id, newStatus);
  };

  if (isLoading) {
    return (
      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-4">
          <div className="animate-pulse">
            <div className="h-4 bg-gray-200 rounded w-1/4 mb-4"></div>
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-12 bg-gray-200 rounded"></div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (properties.length === 0) {
    return (
      <div className="bg-white shadow rounded-lg">
        <div className="px-6 py-12 text-center">
          <ChartBarIcon className="mx-auto h-12 w-12 text-gray-400" />
          <h3 className="mt-2 text-sm font-semibold text-gray-900">매물이 없습니다</h3>
          <p className="mt-1 text-sm text-gray-500">
            새로운 매물을 등록하여 시작해보세요.
          </p>
        </div>
      </div>
    );
  }

  if (viewMode === 'cards') {
    return (
      <>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {properties.map((property) => (
            <div key={property.id} className="bg-white overflow-hidden shadow rounded-lg">
              <div className="px-4 py-5 sm:p-6">
                <div className="flex items-center justify-between">
                  <div className="flex-1 min-w-0">
                    <h3 className="text-lg font-medium text-gray-900 truncate">
                      {property.title}
                    </h3>
                    <div className="mt-1 flex items-center text-sm text-gray-500">
                      <MapPinIcon className="flex-shrink-0 mr-1.5 h-4 w-4" />
                      <span className="truncate">{property.address}</span>
                    </div>
                  </div>
                  <div className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(property.status)}`}>
                    {getStatusIcon(property.status)}
                    <span className="ml-1">{getStatusText(property.status)}</span>
                  </div>
                </div>

                <div className="mt-4">
                  <div className="flex items-center justify-between text-sm text-gray-500">
                    <div className="flex items-center">
                      <CurrencyDollarIcon className="h-4 w-4 mr-1" />
                      <span>{formatCurrency(property.rent)}</span>
                    </div>
                    <div className="flex items-center">
                      <CalendarIcon className="h-4 w-4 mr-1" />
                      <span>{formatDate(property.updatedAt)}</span>
                    </div>
                  </div>
                  
                  <div className="mt-2 flex items-center justify-between text-sm text-gray-500">
                    <div className="flex items-center space-x-4">
                      <div className="flex items-center">
                        <EyeIcon className="h-4 w-4 mr-1" />
                        <span>{property.views}</span>
                      </div>
                      <div>
                        문의 {property.inquiries}
                      </div>
                    </div>
                  </div>
                </div>

                <div className="mt-4 flex items-center justify-between">
                  <button
                    onClick={() => handleStatusToggle(property)}
                    className={`text-xs font-medium px-3 py-1 rounded-full ${
                      property.status === PropertyStatus.ACTIVE
                        ? 'bg-red-100 text-red-800 hover:bg-red-200'
                        : 'bg-green-100 text-green-800 hover:bg-green-200'
                    }`}
                  >
                    {property.status === PropertyStatus.ACTIVE ? '비활성화' : '활성화'}
                  </button>
                  
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleEdit(property)}
                      className="p-1 text-gray-400 hover:text-blue-600"
                      title="수정"
                    >
                      <PencilIcon className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(property)}
                      className="p-1 text-gray-400 hover:text-red-600"
                      title="삭제"
                    >
                      <TrashIcon className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Modals */}
        {editingProperty && (
          <PropertyEditModal
            property={editingProperty}
            isOpen={!!editingProperty}
            onClose={() => setEditingProperty(null)}
            onSave={(updatedProperty) => {
              // Handle save logic here
              setEditingProperty(null);
            }}
          />
        )}

        {deletingProperty && (
          <PropertyDeleteDialog
            property={deletingProperty}
            isOpen={!!deletingProperty}
            onClose={() => setDeletingProperty(null)}
            onConfirm={handleConfirmDelete}
          />
        )}
      </>
    );
  }

  // Table view
  return (
    <>
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                매물 정보
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                상태
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                임대료
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                통계
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                업데이트
              </th>
              <th className="relative px-6 py-3">
                <span className="sr-only">작업</span>
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {properties.map((property) => (
              <tr key={property.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0">
                      {property.images && property.images.length > 0 ? (
                        <img
                          className="h-10 w-10 rounded-lg object-cover"
                          src={property.images[0]}
                          alt={property.title}
                        />
                      ) : (
                        <div className="h-10 w-10 rounded-lg bg-gray-200 flex items-center justify-center">
                          <MapPinIcon className="h-5 w-5 text-gray-400" />
                        </div>
                      )}
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">{property.title}</div>
                      <div className="text-sm text-gray-500">{property.address}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(property.status)}`}>
                    {getStatusIcon(property.status)}
                    <span className="ml-1">{getStatusText(property.status)}</span>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {formatCurrency(property.rent)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  <div className="flex items-center space-x-4">
                    <div className="flex items-center">
                      <EyeIcon className="h-4 w-4 mr-1" />
                      {property.views}
                    </div>
                    <div>문의 {property.inquiries}</div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {formatDate(property.updatedAt)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex items-center space-x-2">
                    <button
                      onClick={() => handleStatusToggle(property)}
                      className={`text-xs font-medium px-3 py-1 rounded-full ${
                        property.status === PropertyStatus.ACTIVE
                          ? 'bg-red-100 text-red-800 hover:bg-red-200'
                          : 'bg-green-100 text-green-800 hover:bg-green-200'
                      }`}
                    >
                      {property.status === PropertyStatus.ACTIVE ? '비활성화' : '활성화'}
                    </button>
                    
                    <button
                      onClick={() => handleEdit(property)}
                      className="text-blue-600 hover:text-blue-900"
                      title="수정"
                    >
                      <PencilIcon className="h-4 w-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(property)}
                      className="text-red-600 hover:text-red-900"
                      title="삭제"
                    >
                      <TrashIcon className="h-4 w-4" />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Modals */}
      {editingProperty && (
        <PropertyEditModal
          property={editingProperty}
          isOpen={!!editingProperty}
          onClose={() => setEditingProperty(null)}
          onSave={(updatedProperty) => {
            // Handle save logic here
            setEditingProperty(null);
          }}
        />
      )}

      {deletingProperty && (
        <PropertyDeleteDialog
          property={deletingProperty}
          isOpen={!!deletingProperty}
          onClose={() => setDeletingProperty(null)}
          onConfirm={handleConfirmDelete}
        />
      )}
    </>
  );
}