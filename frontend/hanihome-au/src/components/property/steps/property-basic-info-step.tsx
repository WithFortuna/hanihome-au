'use client';

import React from 'react';
import { useFormContext } from 'react-hook-form';
import { PropertyFormData, PropertyType, RentalType, PropertyTypeDisplayNames, RentalTypeDisplayNames } from '@/lib/types/property';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select } from '@/components/ui/select';
import { Textarea } from '@/components/ui/textarea';

export function PropertyBasicInfoStep() {
  const { register, formState: { errors }, watch } = useFormContext<PropertyFormData>();
  
  const watchedPropertyType = watch('propertyType');
  const watchedRentalType = watch('rentalType');

  return (
    <div className="space-y-6">
      {/* Title */}
      <div className="space-y-2">
        <Label htmlFor="title">매물 제목 *</Label>
        <Input
          id="title"
          {...register('title')}
          placeholder="예: 강남역 도보 5분, 깨끗한 원룸"
          className={errors.title ? 'border-red-500' : ''}
        />
        {errors.title && (
          <p className="text-sm text-red-500">{errors.title.message}</p>
        )}
      </div>

      {/* Property Type */}
      <div className="space-y-2">
        <Label htmlFor="propertyType">매물 유형 *</Label>
        <Select
          id="propertyType"
          {...register('propertyType')}
          className={errors.propertyType ? 'border-red-500' : ''}
        >
          <option value="">매물 유형을 선택하세요</option>
          {Object.entries(PropertyTypeDisplayNames).map(([key, value]) => (
            <option key={key} value={key}>
              {value}
            </option>
          ))}
        </Select>
        {errors.propertyType && (
          <p className="text-sm text-red-500">{errors.propertyType.message}</p>
        )}
      </div>

      {/* Rental Type */}
      <div className="space-y-2">
        <Label htmlFor="rentalType">임대 유형 *</Label>
        <Select
          id="rentalType"
          {...register('rentalType')}
          className={errors.rentalType ? 'border-red-500' : ''}
        >
          <option value="">임대 유형을 선택하세요</option>
          {Object.entries(RentalTypeDisplayNames).map(([key, value]) => (
            <option key={key} value={key}>
              {value}
            </option>
          ))}
        </Select>
        {errors.rentalType && (
          <p className="text-sm text-red-500">{errors.rentalType.message}</p>
        )}
      </div>

      {/* Description */}
      <div className="space-y-2">
        <Label htmlFor="description">매물 설명</Label>
        <Textarea
          id="description"
          {...register('description')}
          placeholder="매물에 대한 상세한 설명을 입력해주세요..."
          rows={4}
          className={errors.description ? 'border-red-500' : ''}
        />
        {errors.description && (
          <p className="text-sm text-red-500">{errors.description.message}</p>
        )}
      </div>

      {/* Preview Card */}
      {(watchedPropertyType || watchedRentalType) && (
        <div className="mt-6 p-4 bg-gray-50 rounded-lg border">
          <h4 className="font-medium text-gray-700 mb-2">미리보기</h4>
          <div className="text-sm text-gray-600">
            {watchedPropertyType && (
              <span className="inline-block px-2 py-1 bg-blue-100 text-blue-800 rounded mr-2">
                {PropertyTypeDisplayNames[watchedPropertyType as PropertyType]}
              </span>
            )}
            {watchedRentalType && (
              <span className="inline-block px-2 py-1 bg-green-100 text-green-800 rounded">
                {RentalTypeDisplayNames[watchedRentalType as RentalType]}
              </span>
            )}
          </div>
        </div>
      )}
    </div>
  );
}