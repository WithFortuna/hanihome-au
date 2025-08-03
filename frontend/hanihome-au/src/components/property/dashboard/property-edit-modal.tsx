'use client';

import React, { useState, useEffect } from 'react';
import { Fragment } from 'react';
import { Dialog, Transition } from '@headlessui/react';
import { XMarkIcon, CloudArrowUpIcon } from '@heroicons/react/24/outline';
import { PropertyWithStats } from '../property-dashboard';
import { PropertyFormData, PropertyType, RentalType } from '@/lib/types/property';
import { usePropertyRegistrationForm } from '@/hooks/use-property-registration-form';

interface PropertyEditModalProps {
  property: PropertyWithStats;
  isOpen: boolean;
  onClose: () => void;
  onSave: (property: PropertyWithStats) => void;
}

export default function PropertyEditModal({
  property,
  isOpen,
  onClose,
  onSave,
}: PropertyEditModalProps) {
  const [formData, setFormData] = useState<PropertyFormData>({
    title: property.title,
    description: property.description,
    propertyType: property.propertyType,
    rentalType: property.rentalType,
    address: property.address,
    latitude: property.latitude,
    longitude: property.longitude,
    rent: property.rent,
    deposit: property.deposit,
    managementFee: property.managementFee,
    area: property.area,
    rooms: property.rooms,
    bathrooms: property.bathrooms,
    floor: property.floor,
    totalFloors: property.totalFloors,
    availableFrom: property.availableFrom,
    options: property.options || [],
    images: property.images || [],
    contactName: property.contactName,
    contactPhone: property.contactPhone,
  });

  const [isLoading, setIsLoading] = useState(false);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleInputChange = (
    field: keyof PropertyFormData,
    value: string | number | string[] | Date
  ) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    // Clear error when user starts typing
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    if (!formData.title?.trim()) {
      newErrors.title = '매물명을 입력해주세요.';
    }

    if (!formData.description?.trim()) {
      newErrors.description = '매물 설명을 입력해주세요.';
    }

    if (!formData.address?.trim()) {
      newErrors.address = '주소를 입력해주세요.';
    }

    if (formData.rent <= 0) {
      newErrors.rent = '임대료를 입력해주세요.';
    }

    if (formData.deposit < 0) {
      newErrors.deposit = '보증금은 0원 이상이어야 합니다.';
    }

    if (formData.area <= 0) {
      newErrors.area = '면적을 입력해주세요.';
    }

    if (formData.rooms < 0) {
      newErrors.rooms = '방 개수는 0개 이상이어야 합니다.';
    }

    if (!formData.contactName?.trim()) {
      newErrors.contactName = '연락처 이름을 입력해주세요.';
    }

    if (!formData.contactPhone?.trim()) {
      newErrors.contactPhone = '연락처 번호를 입력해주세요.';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSave = async () => {
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      const updatedProperty: PropertyWithStats = {
        ...property,
        ...formData,
        updatedAt: new Date(),
      };
      
      onSave(updatedProperty);
    } catch (error) {
      console.error('Failed to update property:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleImageRemove = (index: number) => {
    const newImages = formData.images.filter((_, i) => i !== index);
    handleInputChange('images', newImages);
  };

  const handleImageReorder = (dragIndex: number, hoverIndex: number) => {
    const newImages = [...formData.images];
    const draggedImage = newImages[dragIndex];
    newImages.splice(dragIndex, 1);
    newImages.splice(hoverIndex, 0, draggedImage);
    handleInputChange('images', newImages);
  };

  return (
    <Transition.Root show={isOpen} as={Fragment}>
      <Dialog as="div" className="relative z-50" onClose={onClose}>
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-300"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-200"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" />
        </Transition.Child>

        <div className="fixed inset-0 z-10 overflow-y-auto">
          <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-300"
              enterFrom="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
              enterTo="opacity-100 translate-y-0 sm:scale-100"
              leave="ease-in duration-200"
              leaveFrom="opacity-100 translate-y-0 sm:scale-100"
              leaveTo="opacity-0 translate-y-4 sm:translate-y-0 sm:scale-95"
            >
              <Dialog.Panel className="relative transform overflow-hidden rounded-lg bg-white px-4 pb-4 pt-5 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-4xl sm:p-6">
                <div className="absolute right-0 top-0 pr-4 pt-4">
                  <button
                    type="button"
                    className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    onClick={onClose}
                  >
                    <span className="sr-only">닫기</span>
                    <XMarkIcon className="h-6 w-6" aria-hidden="true" />
                  </button>
                </div>

                <div className="sm:flex sm:items-start">
                  <div className="w-full mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                    <Dialog.Title as="h3" className="text-lg font-semibold leading-6 text-gray-900">
                      매물 정보 수정
                    </Dialog.Title>
                    <div className="mt-2">
                      <p className="text-sm text-gray-500">
                        매물 정보를 수정하세요. 변경된 내용은 즉시 반영됩니다.
                      </p>
                    </div>

                    <div className="mt-6">
                      <form className="space-y-6">
                        {/* Basic Information */}
                        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                          <div>
                            <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                              매물명 *
                            </label>
                            <input
                              type="text"
                              id="title"
                              value={formData.title}
                              onChange={(e) => handleInputChange('title', e.target.value)}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.title ? 'border-red-300' : ''
                              }`}
                              placeholder="매물명을 입력하세요"
                            />
                            {errors.title && (
                              <p className="mt-1 text-sm text-red-600">{errors.title}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="propertyType" className="block text-sm font-medium text-gray-700">
                              매물 유형 *
                            </label>
                            <select
                              id="propertyType"
                              value={formData.propertyType}
                              onChange={(e) => handleInputChange('propertyType', e.target.value as PropertyType)}
                              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                            >
                              <option value={PropertyType.APARTMENT}>아파트</option>
                              <option value={PropertyType.HOUSE}>단독주택</option>
                              <option value={PropertyType.VILLA}>빌라</option>
                              <option value={PropertyType.OFFICETEL}>오피스텔</option>
                              <option value={PropertyType.STUDIO}>원룸</option>
                            </select>
                          </div>
                        </div>

                        <div>
                          <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                            매물 설명 *
                          </label>
                          <textarea
                            id="description"
                            rows={3}
                            value={formData.description}
                            onChange={(e) => handleInputChange('description', e.target.value)}
                            className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                              errors.description ? 'border-red-300' : ''
                            }`}
                            placeholder="매물에 대한 상세한 설명을 입력하세요"
                          />
                          {errors.description && (
                            <p className="mt-1 text-sm text-red-600">{errors.description}</p>
                          )}
                        </div>

                        {/* Address */}
                        <div>
                          <label htmlFor="address" className="block text-sm font-medium text-gray-700">
                            주소 *
                          </label>
                          <input
                            type="text"
                            id="address"
                            value={formData.address}
                            onChange={(e) => handleInputChange('address', e.target.value)}
                            className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                              errors.address ? 'border-red-300' : ''
                            }`}
                            placeholder="주소를 입력하세요"
                          />
                          {errors.address && (
                            <p className="mt-1 text-sm text-red-600">{errors.address}</p>
                          )}
                        </div>

                        {/* Price Information */}
                        <div className="grid grid-cols-1 gap-6 sm:grid-cols-3">
                          <div>
                            <label htmlFor="rent" className="block text-sm font-medium text-gray-700">
                              월 임대료 (원) *
                            </label>
                            <input
                              type="number"
                              id="rent"
                              value={formData.rent}
                              onChange={(e) => handleInputChange('rent', Number(e.target.value))}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.rent ? 'border-red-300' : ''
                              }`}
                              placeholder="0"
                            />
                            {errors.rent && (
                              <p className="mt-1 text-sm text-red-600">{errors.rent}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="deposit" className="block text-sm font-medium text-gray-700">
                              보증금 (원)
                            </label>
                            <input
                              type="number"
                              id="deposit"
                              value={formData.deposit}
                              onChange={(e) => handleInputChange('deposit', Number(e.target.value))}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.deposit ? 'border-red-300' : ''
                              }`}
                              placeholder="0"
                            />
                            {errors.deposit && (
                              <p className="mt-1 text-sm text-red-600">{errors.deposit}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="managementFee" className="block text-sm font-medium text-gray-700">
                              관리비 (원)
                            </label>
                            <input
                              type="number"
                              id="managementFee"
                              value={formData.managementFee || 0}
                              onChange={(e) => handleInputChange('managementFee', Number(e.target.value))}
                              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                              placeholder="0"
                            />
                          </div>
                        </div>

                        {/* Property Details */}
                        <div className="grid grid-cols-1 gap-6 sm:grid-cols-4">
                          <div>
                            <label htmlFor="area" className="block text-sm font-medium text-gray-700">
                              면적 (㎡) *
                            </label>
                            <input
                              type="number"
                              id="area"
                              value={formData.area}
                              onChange={(e) => handleInputChange('area', Number(e.target.value))}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.area ? 'border-red-300' : ''
                              }`}
                              placeholder="0"
                            />
                            {errors.area && (
                              <p className="mt-1 text-sm text-red-600">{errors.area}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="rooms" className="block text-sm font-medium text-gray-700">
                              방 개수
                            </label>
                            <input
                              type="number"
                              id="rooms"
                              value={formData.rooms}
                              onChange={(e) => handleInputChange('rooms', Number(e.target.value))}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.rooms ? 'border-red-300' : ''
                              }`}
                              placeholder="0"
                            />
                            {errors.rooms && (
                              <p className="mt-1 text-sm text-red-600">{errors.rooms}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="bathrooms" className="block text-sm font-medium text-gray-700">
                              욕실 개수
                            </label>
                            <input
                              type="number"
                              id="bathrooms"
                              value={formData.bathrooms}
                              onChange={(e) => handleInputChange('bathrooms', Number(e.target.value))}
                              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                              placeholder="0"
                            />
                          </div>

                          <div>
                            <label htmlFor="floor" className="block text-sm font-medium text-gray-700">
                              층수
                            </label>
                            <input
                              type="number"
                              id="floor"
                              value={formData.floor}
                              onChange={(e) => handleInputChange('floor', Number(e.target.value))}
                              className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm"
                              placeholder="0"
                            />
                          </div>
                        </div>

                        {/* Contact Information */}
                        <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
                          <div>
                            <label htmlFor="contactName" className="block text-sm font-medium text-gray-700">
                              연락처 이름 *
                            </label>
                            <input
                              type="text"
                              id="contactName"
                              value={formData.contactName}
                              onChange={(e) => handleInputChange('contactName', e.target.value)}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.contactName ? 'border-red-300' : ''
                              }`}
                              placeholder="이름을 입력하세요"
                            />
                            {errors.contactName && (
                              <p className="mt-1 text-sm text-red-600">{errors.contactName}</p>
                            )}
                          </div>

                          <div>
                            <label htmlFor="contactPhone" className="block text-sm font-medium text-gray-700">
                              연락처 번호 *
                            </label>
                            <input
                              type="tel"
                              id="contactPhone"
                              value={formData.contactPhone}
                              onChange={(e) => handleInputChange('contactPhone', e.target.value)}
                              className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 sm:text-sm ${
                                errors.contactPhone ? 'border-red-300' : ''
                              }`}
                              placeholder="010-0000-0000"
                            />
                            {errors.contactPhone && (
                              <p className="mt-1 text-sm text-red-600">{errors.contactPhone}</p>
                            )}
                          </div>
                        </div>

                        {/* Images */}
                        {formData.images && formData.images.length > 0 && (
                          <div>
                            <label className="block text-sm font-medium text-gray-700">
                              이미지
                            </label>
                            <div className="mt-2 grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
                              {formData.images.map((image, index) => (
                                <div key={index} className="relative group">
                                  <img
                                    src={image}
                                    alt={`매물 이미지 ${index + 1}`}
                                    className="h-24 w-full object-cover rounded-lg"
                                  />
                                  <button
                                    type="button"
                                    onClick={() => handleImageRemove(index)}
                                    className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity"
                                  >
                                    <XMarkIcon className="h-4 w-4" />
                                  </button>
                                </div>
                              ))}
                            </div>
                          </div>
                        )}
                      </form>
                    </div>
                  </div>
                </div>

                <div className="mt-5 sm:mt-4 sm:flex sm:flex-row-reverse">
                  <button
                    type="button"
                    className="inline-flex w-full justify-center rounded-md bg-blue-600 px-3 py-2 text-sm font-semibold text-white shadow-sm hover:bg-blue-500 focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-blue-600 sm:ml-3 sm:w-auto disabled:opacity-50 disabled:cursor-not-allowed"
                    onClick={handleSave}
                    disabled={isLoading}
                  >
                    {isLoading ? '저장 중...' : '저장'}
                  </button>
                  <button
                    type="button"
                    className="mt-3 inline-flex w-full justify-center rounded-md bg-white px-3 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
                    onClick={onClose}
                    disabled={isLoading}
                  >
                    취소
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition.Root>
  );
}