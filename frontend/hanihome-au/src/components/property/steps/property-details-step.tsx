'use client';

import React from 'react';
import { useFormContext } from 'react-hook-form';
import { PropertyFormData, RentalType } from '@/lib/types/property';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AddressSearchWithMap } from '../address-search-with-map';

export function PropertyDetailsStep() {
  const { register, formState: { errors }, watch, setValue } = useFormContext<PropertyFormData>();
  
  const watchedRentalType = watch('rentalType');
  const watchedDeposit = watch('deposit');
  const watchedMonthlyRent = watch('monthlyRent');

  const formatNumber = (value: string) => {
    const number = value.replace(/[^\d]/g, '');
    return number.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
  };

  return (
    <div className="space-y-6">
      {/* Property Specifications */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="area">면적 (㎡)</Label>
          <Input
            id="area"
            type="number"
            step="0.01"
            {...register('area', { valueAsNumber: true })}
            placeholder="예: 33.12"
            className={errors.area ? 'border-red-500' : ''}
          />
          {errors.area && (
            <p className="text-sm text-red-500">{errors.area.message}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="rooms">방 개수</Label>
          <Input
            id="rooms"
            type="number"
            {...register('rooms', { valueAsNumber: true })}
            placeholder="예: 2"
            className={errors.rooms ? 'border-red-500' : ''}
          />
          {errors.rooms && (
            <p className="text-sm text-red-500">{errors.rooms.message}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="bathrooms">욕실 개수</Label>
          <Input
            id="bathrooms"
            type="number"
            {...register('bathrooms', { valueAsNumber: true })}
            placeholder="예: 1"
            className={errors.bathrooms ? 'border-red-500' : ''}
          />
          {errors.bathrooms && (
            <p className="text-sm text-red-500">{errors.bathrooms.message}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="availableDate">입주 가능일</Label>
          <Input
            id="availableDate"
            type="date"
            {...register('availableDate')}
            className={errors.availableDate ? 'border-red-500' : ''}
          />
          {errors.availableDate && (
            <p className="text-sm text-red-500">{errors.availableDate.message}</p>
          )}
        </div>
      </div>

      {/* Floor Information */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="space-y-2">
          <Label htmlFor="floor">해당 층</Label>
          <Input
            id="floor"
            type="number"
            {...register('floor', { valueAsNumber: true })}
            placeholder="예: 3"
            className={errors.floor ? 'border-red-500' : ''}
          />
          {errors.floor && (
            <p className="text-sm text-red-500">{errors.floor.message}</p>
          )}
        </div>

        <div className="space-y-2">
          <Label htmlFor="totalFloors">총 층수</Label>
          <Input
            id="totalFloors"
            type="number"
            {...register('totalFloors', { valueAsNumber: true })}
            placeholder="예: 5"
            className={errors.totalFloors ? 'border-red-500' : ''}
          />
          {errors.totalFloors && (
            <p className="text-sm text-red-500">{errors.totalFloors.message}</p>
          )}
        </div>
      </div>

      {/* Address Information */}
      <div className="space-y-4">
        <h4 className="text-lg font-semibold text-gray-800">주소 정보</h4>
        <AddressSearchWithMap
          onAddressChange={(addressInfo) => {
            setValue('address', addressInfo.fullAddress);
            setValue('detailAddress', addressInfo.detailAddress || '');
            setValue('zipCode', addressInfo.zipCode || '');
            setValue('city', addressInfo.city || '');
            setValue('district', addressInfo.district || '');
            setValue('latitude', addressInfo.coordinates.lat);
            setValue('longitude', addressInfo.coordinates.lng);
          }}
          initialAddress={watch('address')}
          initialCoordinates={
            watch('latitude') && watch('longitude')
              ? { lat: watch('latitude'), lng: watch('longitude') }
              : undefined
          }
        />
      </div>

      {/* Price Information */}
      <div className="space-y-4">
        <h4 className="text-lg font-semibold text-gray-800">가격 정보</h4>
        
        {watchedRentalType !== RentalType.SALE && (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="deposit">보증금 (만원)</Label>
              <Input
                id="deposit"
                type="number"
                {...register('deposit', { valueAsNumber: true })}
                placeholder="예: 1000"
                className={errors.deposit ? 'border-red-500' : ''}
              />
              {errors.deposit && (
                <p className="text-sm text-red-500">{errors.deposit.message}</p>
              )}
            </div>

            {watchedRentalType === RentalType.MONTHLY && (
              <div className="space-y-2">
                <Label htmlFor="monthlyRent">월세 (만원)</Label>
                <Input
                  id="monthlyRent"
                  type="number"
                  {...register('monthlyRent', { valueAsNumber: true })}
                  placeholder="예: 50"
                  className={errors.monthlyRent ? 'border-red-500' : ''}
                />
                {errors.monthlyRent && (
                  <p className="text-sm text-red-500">{errors.monthlyRent.message}</p>
                )}
              </div>
            )}
          </div>
        )}

        {watchedRentalType === RentalType.SALE && (
          <div className="space-y-2">
            <Label htmlFor="deposit">매매가 (만원)</Label>
            <Input
              id="deposit"
              type="number"
              {...register('deposit', { valueAsNumber: true })}
              placeholder="예: 50000"
              className={errors.deposit ? 'border-red-500' : ''}
            />
            {errors.deposit && (
              <p className="text-sm text-red-500">{errors.deposit.message}</p>
            )}
          </div>
        )}

        <div className="space-y-2">
          <Label htmlFor="maintenanceFee">관리비 (만원)</Label>
          <Input
            id="maintenanceFee"
            type="number"
            {...register('maintenanceFee', { valueAsNumber: true })}
            placeholder="예: 5"
            className={errors.maintenanceFee ? 'border-red-500' : ''}
          />
          {errors.maintenanceFee && (
            <p className="text-sm text-red-500">{errors.maintenanceFee.message}</p>
          )}
        </div>
      </div>

      {/* Price Preview */}
      {(watchedDeposit || watchedMonthlyRent) && (
        <div className="mt-6 p-4 bg-blue-50 rounded-lg border">
          <h4 className="font-medium text-blue-800 mb-2">가격 미리보기</h4>
          <div className="text-sm text-blue-700">
            {watchedRentalType === RentalType.MONTHLY && (
              <p>월세: 보증금 {watchedDeposit?.toLocaleString() || 0}만원 / 월 {watchedMonthlyRent?.toLocaleString() || 0}만원</p>
            )}
            {watchedRentalType === RentalType.JEONSE && (
              <p>전세: {watchedDeposit?.toLocaleString() || 0}만원</p>
            )}
            {watchedRentalType === RentalType.SALE && (
              <p>매매: {watchedDeposit?.toLocaleString() || 0}만원</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}