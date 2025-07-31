# 프론트엔드 문서 5 - 매물 관리 시스템 프론트엔드 구현

## 문서 히스토리 및 개요

**문서 생성일**: 2025-07-31  
**작업 범위**: Task 4 - 매물 데이터 모델 및 기본 CRUD API 구현 (프론트엔드 관련)  
**관련 TaskMaster 작업**: Task 4.1-4.7 완료 작업 기반  
**이전 문서**: [frontend-documentation-4.md](./frontend-documentation-4.md)

### 구현 완료 항목

1. **매물 관리 인터페이스 타입 정의**
2. **매물 CRUD 프론트엔드 컴포넌트 구현**
3. **이미지 업로드 및 관리 시스템**
4. **매물 검색 및 필터링 UI**
5. **매물 상태 관리 인터페이스**
6. **API 연동 및 상태 관리**
7. **반응형 매물 관리 대시보드**

## 매물 관리 타입 시스템

### 핵심 인터페이스 정의

```typescript
// types/property.ts
export interface Property {
  id: number;
  title: string;
  address: {
    fullAddress: string;
    city: string;
    district: string;
    neighborhood: string;
    coordinates?: {
      latitude: number;
      longitude: number;
    };
  };
  price: {
    rent: number;
    deposit: number;
    maintenanceFee?: number;
    currency: 'KRW' | 'USD';
  };
  details: {
    propertyType: PropertyType;
    roomCount: number;
    bathroomCount: number;
    area: number; // 평방미터
    floor: number;
    totalFloors: number;
    availableDate: string;
    furnished: boolean;
  };
  options: PropertyOption[];
  images: PropertyImage[];
  status: PropertyStatus;
  owner: {
    id: number;
    name: string;
    phone: string;
    email: string;
  };
  createdAt: string;
  updatedAt: string;
}

export enum PropertyType {
  APARTMENT = 'APARTMENT',
  VILLA = 'VILLA',
  HOUSE = 'HOUSE',
  STUDIO = 'STUDIO',
  OFFICETEL = 'OFFICETEL'
}

export enum PropertyStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  RENTED = 'RENTED',
  PENDING = 'PENDING'
}

export interface PropertyImage {
  id: number;
  url: string;
  thumbnailUrl: string;
  order: number;
  caption?: string;
}

export interface PropertyOption {
  id: number;
  name: string;
  category: 'APPLIANCE' | 'FURNITURE' | 'SECURITY' | 'CONVENIENCE';
}

export interface PropertySearchFilters {
  priceRange?: {
    minRent: number;
    maxRent: number;
    minDeposit: number;
    maxDeposit: number;
  };
  location?: {
    city?: string;
    district?: string;
    radius?: number; // km
    coordinates?: {
      latitude: number;
      longitude: number;
    };
  };
  propertyType?: PropertyType[];
  roomCount?: number[];
  options?: number[];
  availableFrom?: string;
  furnished?: boolean;
}
```

## 매물 관리 컴포넌트 구현

### 매물 목록 컴포넌트

```tsx
// components/property/PropertyList.tsx
'use client';

import React, { useState, useEffect } from 'react';
import { Property, PropertySearchFilters } from '@/types/property';
import { usePropertyStore } from '@/store/propertyStore';
import PropertyCard from './PropertyCard';
import PropertyFilters from './PropertyFilters';
import { Pagination } from '@/components/ui/Pagination';
import { LoadingSpinner } from '@/components/ui/LoadingSpinner';

interface PropertyListProps {
  initialProperties?: Property[];
  showFilters?: boolean;
  pageSize?: number;
}

const PropertyList: React.FC<PropertyListProps> = ({
  initialProperties = [],
  showFilters = true,
  pageSize = 12
}) => {
  const {
    properties,
    loading,
    error,
    totalCount,
    currentPage,
    filters,
    setFilters,
    fetchProperties,
    setPage
  } = usePropertyStore();

  const [isInitialized, setIsInitialized] = useState(false);

  useEffect(() => {
    if (!isInitialized) {
      if (initialProperties.length > 0) {
        // 서버에서 받은 초기 데이터 사용
        usePropertyStore.getState().setProperties(initialProperties);
      } else {
        fetchProperties();
      }
      setIsInitialized(true);
    }
  }, [isInitialized, initialProperties, fetchProperties]);

  const handleFilterChange = (newFilters: PropertySearchFilters) => {
    setFilters(newFilters);
    setPage(1);
    fetchProperties();
  };

  const handlePageChange = (page: number) => {
    setPage(page);
    fetchProperties();
  };

  if (loading && !isInitialized) {
    return (
      <div className="flex justify-center items-center min-h-64">
        <LoadingSpinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center py-8">
        <p className="text-red-600 mb-4">매물을 불러오는데 실패했습니다.</p>
        <button
          onClick={() => fetchProperties()}
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {showFilters && (
        <PropertyFilters
          filters={filters}
          onFiltersChange={handleFilterChange}
        />
      )}

      <div className="flex justify-between items-center">
        <h2 className="text-xl font-semibold">
          매물 목록 ({totalCount}개)
        </h2>
        <div className="flex space-x-2">
          <select className="border rounded px-3 py-1">
            <option value="recent">최신순</option>
            <option value="price-low">가격 낮은순</option>
            <option value="price-high">가격 높은순</option>
          </select>
        </div>
      </div>

      {properties.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-gray-500 mb-4">등록된 매물이 없습니다.</p>
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {properties.map((property) => (
              <PropertyCard key={property.id} property={property} />
            ))}
          </div>

          <Pagination
            current={currentPage}
            total={Math.ceil(totalCount / pageSize)}
            onPageChange={handlePageChange}
          />
        </>
      )}
    </div>
  );
};

export default PropertyList;
```

### 매물 카드 컴포넌트

```tsx
// components/property/PropertyCard.tsx
'use client';

import React from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Property, PropertyStatus } from '@/types/property';
import { formatCurrency } from '@/lib/utils';

interface PropertyCardProps {
  property: Property;
  showActions?: boolean;
}

const PropertyCard: React.FC<PropertyCardProps> = ({
  property,
  showActions = false
}) => {
  const getStatusBadge = (status: PropertyStatus) => {
    const statusConfig = {
      [PropertyStatus.ACTIVE]: { label: '임대가능', className: 'bg-green-100 text-green-800' },
      [PropertyStatus.INACTIVE]: { label: '비활성', className: 'bg-gray-100 text-gray-800' },
      [PropertyStatus.RENTED]: { label: '임대완료', className: 'bg-red-100 text-red-800' },
      [PropertyStatus.PENDING]: { label: '검토중', className: 'bg-yellow-100 text-yellow-800' }
    };

    const config = statusConfig[status];
    return (
      <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.className}`}>
        {config.label}
      </span>
    );
  };

  const primaryImage = property.images[0];

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
      {/* 이미지 섹션 */}
      <div className="relative h-48">
        {primaryImage ? (
          <Image
            src={primaryImage.url}
            alt={property.title}
            fill
            className="object-cover"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 25vw"
          />
        ) : (
          <div className="w-full h-full bg-gray-200 flex items-center justify-center">
            <span className="text-gray-400">이미지 없음</span>
          </div>
        )}
        
        {/* 상태 배지 */}
        <div className="absolute top-2 right-2">
          {getStatusBadge(property.status)}
        </div>

        {/* 이미지 개수 */}
        {property.images.length > 1 && (
          <div className="absolute bottom-2 right-2 bg-black bg-opacity-50 text-white px-2 py-1 rounded text-xs">
            +{property.images.length - 1}
          </div>
        )}
      </div>

      {/* 정보 섹션 */}
      <div className="p-4">
        <div className="mb-2">
          <h3 className="font-semibold text-lg mb-1 line-clamp-1">
            {property.title}
          </h3>
          <p className="text-gray-600 text-sm line-clamp-1">
            {property.address.fullAddress}
          </p>
        </div>

        <div className="mb-3">
          <div className="text-lg font-bold text-blue-600">
            월세 {formatCurrency(property.price.rent)}
          </div>
          <div className="text-sm text-gray-600">
            보증금 {formatCurrency(property.price.deposit)}
          </div>
          {property.price.maintenanceFee && (
            <div className="text-sm text-gray-600">
              관리비 {formatCurrency(property.price.maintenanceFee)}
            </div>
          )}
        </div>

        <div className="flex justify-between items-center text-sm text-gray-600 mb-3">
          <span>{property.details.propertyType}</span>
          <span>{property.details.roomCount}룸</span>
          <span>{property.details.area}㎡</span>
        </div>

        {/* 주요 옵션 */}
        <div className="flex flex-wrap gap-1 mb-3">
          {property.options.slice(0, 3).map((option) => (
            <span
              key={option.id}
              className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded"
            >
              {option.name}
            </span>
          ))}
          {property.options.length > 3 && (
            <span className="px-2 py-1 bg-gray-100 text-gray-700 text-xs rounded">
              +{property.options.length - 3}
            </span>
          )}
        </div>

        {/* 액션 버튼 */}
        <div className="flex space-x-2">
          <Link
            href={`/properties/${property.id}`}
            className="flex-1 text-center px-3 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
          >
            자세히 보기
          </Link>
          {showActions && (
            <button className="px-3 py-2 border border-gray-300 rounded hover:bg-gray-50 transition-colors">
              ♡
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default PropertyCard;
```

## 이미지 업로드 시스템

### 다중 이미지 업로드 컴포넌트

```tsx
// components/property/ImageUpload.tsx
'use client';

import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import Image from 'next/image';
import { PropertyImage } from '@/types/property';
import { uploadPropertyImages } from '@/lib/api/property';
import { DragDropContext, Droppable, Draggable } from 'react-beautiful-dnd';

interface ImageUploadProps {
  images: PropertyImage[];
  onImagesChange: (images: PropertyImage[]) => void;
  maxImages?: number;
  disabled?: boolean;
}

const ImageUpload: React.FC<ImageUploadProps> = ({
  images,
  onImagesChange,
  maxImages = 10,
  disabled = false
}) => {
  const [uploading, setUploading] = useState(false);
  const [uploadProgress, setUploadProgress] = useState<{[key: string]: number}>({});

  const onDrop = useCallback(async (acceptedFiles: File[]) => {
    if (disabled || images.length >= maxImages) {
      return;
    }

    const remainingSlots = maxImages - images.length;
    const filesToUpload = acceptedFiles.slice(0, remainingSlots);

    setUploading(true);

    try {
      const uploadPromises = filesToUpload.map(async (file, index) => {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('order', String(images.length + index));

        // 업로드 진행률 추적
        const fileId = `${file.name}-${Date.now()}`;
        setUploadProgress(prev => ({ ...prev, [fileId]: 0 }));

        const response = await uploadPropertyImages(formData, (progress) => {
          setUploadProgress(prev => ({ ...prev, [fileId]: progress }));
        });

        return response.data;
      });

      const uploadedImages = await Promise.all(uploadPromises);
      onImagesChange([...images, ...uploadedImages]);
    } catch (error) {
      console.error('이미지 업로드 실패:', error);
      alert('이미지 업로드에 실패했습니다.');
    } finally {
      setUploading(false);
      setUploadProgress({});
    }
  }, [images, maxImages, disabled, onImagesChange]);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'image/*': ['.jpeg', '.jpg', '.png', '.webp']
    },
    multiple: true,
    disabled: disabled || uploading || images.length >= maxImages
  });

  const handleRemoveImage = (imageId: number) => {
    onImagesChange(images.filter(img => img.id !== imageId));
  };

  const handleDragEnd = (result: any) => {
    if (!result.destination) return;

    const items = Array.from(images);
    const [reorderedItem] = items.splice(result.source.index, 1);
    items.splice(result.destination.index, 0, reorderedItem);

    // 순서 업데이트
    const reorderedImages = items.map((item, index) => ({
      ...item,
      order: index
    }));

    onImagesChange(reorderedImages);
  };

  return (
    <div className="space-y-4">
      {/* 드래그 앤 드롭 업로드 영역 */}
      <div
        {...getRootProps()}
        className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer transition-colors ${
          isDragActive
            ? 'border-blue-400 bg-blue-50'
            : 'border-gray-300 hover:border-gray-400'
        } ${
          disabled || images.length >= maxImages
            ? 'opacity-50 cursor-not-allowed'
            : ''
        }`}
      >
        <input {...getInputProps()} />
        {uploading ? (
          <div className="space-y-2">
            <div className="animate-spin h-8 w-8 border-2 border-blue-600 border-t-transparent rounded-full mx-auto"></div>
            <p>이미지 업로드 중...</p>
          </div>
        ) : (
          <div className="space-y-2">
            <div className="mx-auto h-12 w-12 text-gray-400">
              📷
            </div>
            <div>
              <p className="text-sm font-medium">
                {images.length >= maxImages
                  ? `최대 ${maxImages}개까지 업로드 가능합니다`
                  : '이미지를 드래그하거나 클릭하여 업로드하세요'}
              </p>
              <p className="text-xs text-gray-500 mt-1">
                JPG, PNG, WebP 파일만 가능 (최대 5MB)
              </p>
              <p className="text-xs text-gray-500">
                {images.length}/{maxImages}개 업로드됨
              </p>
            </div>
          </div>
        )}
      </div>

      {/* 업로드된 이미지 목록 */}
      {images.length > 0 && (
        <div className="space-y-2">
          <h4 className="font-medium">업로드된 이미지 ({images.length}개)</h4>
          <p className="text-sm text-gray-600">드래그하여 순서를 변경할 수 있습니다.</p>
          
          <DragDropContext onDragEnd={handleDragEnd}>
            <Droppable droppableId="images" direction="horizontal">
              {(provided) => (
                <div
                  {...provided.droppableProps}
                  ref={provided.innerRef}
                  className="flex flex-wrap gap-2"
                >
                  {images.map((image, index) => (
                    <Draggable
                      key={image.id}
                      draggableId={String(image.id)}
                      index={index}
                    >
                      {(provided, snapshot) => (
                        <div
                          ref={provided.innerRef}
                          {...provided.draggableProps}
                          {...provided.dragHandleProps}
                          className={`relative group ${
                            snapshot.isDragging ? 'z-10' : ''
                          }`}
                        >
                          <div className="w-24 h-24 rounded-lg overflow-hidden border-2 border-gray-200">
                            <Image
                              src={image.thumbnailUrl || image.url}
                              alt={`매물 이미지 ${index + 1}`}
                              width={96}
                              height={96}
                              className="w-full h-full object-cover"
                            />
                          </div>
                          
                          {/* 삭제 버튼 */}
                          <button
                            onClick={() => handleRemoveImage(image.id)}
                            className="absolute -top-2 -right-2 w-6 h-6 bg-red-500 text-white rounded-full text-xs opacity-0 group-hover:opacity-100 transition-opacity"
                          >
                            ×
                          </button>
                          
                          {/* 순서 표시 */}
                          <div className="absolute top-1 left-1 w-5 h-5 bg-black bg-opacity-50 text-white text-xs rounded-full flex items-center justify-center">
                            {index + 1}
                          </div>
                        </div>
                      )}
                    </Draggable>
                  ))}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>
      )}
    </div>
  );
};

export default ImageUpload;
```

## 매물 등록/수정 폼

### 매물 폼 컴포넌트

```tsx
// components/property/PropertyForm.tsx
'use client';

import React, { useState } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { Property, PropertyType, PropertyStatus } from '@/types/property';
import { propertyFormSchema, PropertyFormData } from '@/lib/validations/property';
import ImageUpload from './ImageUpload';
import AddressSearch from './AddressSearch';
import OptionSelector from './OptionSelector';

interface PropertyFormProps {
  property?: Property;
  onSubmit: (data: PropertyFormData) => void;
  loading?: boolean;
}

const PropertyForm: React.FC<PropertyFormProps> = ({
  property,
  onSubmit,
  loading = false
}) => {
  const {
    control,
    handleSubmit,
    formState: { errors },
    watch,
    setValue
  } = useForm<PropertyFormData>({
    resolver: zodResolver(propertyFormSchema),
    defaultValues: property ? {
      title: property.title,
      address: property.address,
      price: property.price,
      details: property.details,
      options: property.options.map(opt => opt.id),
      images: property.images,
      status: property.status
    } : {
      status: PropertyStatus.ACTIVE,
      details: {
        furnished: false
      }
    }
  });

  const [currentStep, setCurrentStep] = useState(1);
  const totalSteps = 5;

  const nextStep = () => setCurrentStep(prev => Math.min(prev + 1, totalSteps));
  const prevStep = () => setCurrentStep(prev => Math.max(prev - 1, 1));

  const renderStepContent = () => {
    switch (currentStep) {
      case 1:
        return (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold">기본 정보</h3>
            
            <Controller
              name="title"
              control={control}
              render={({ field }) => (
                <div>
                  <label className="block text-sm font-medium mb-2">
                    매물 제목 *
                  </label>
                  <input
                    {...field}
                    type="text"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="매물 제목을 입력하세요"
                  />
                  {errors.title && (
                    <p className="mt-1 text-sm text-red-600">{errors.title.message}</p>
                  )}
                </div>
              )}
            />

            <Controller
              name="details.propertyType"
              control={control}
              render={({ field }) => (
                <div>
                  <label className="block text-sm font-medium mb-2">
                    매물 유형 *
                  </label>
                  <select
                    {...field}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">선택하세요</option>
                    <option value={PropertyType.APARTMENT}>아파트</option>
                    <option value={PropertyType.VILLA}>빌라/연립</option>
                    <option value={PropertyType.HOUSE}>단독주택</option>
                    <option value={PropertyType.STUDIO}>원룸/스튜디오</option>
                    <option value={PropertyType.OFFICETEL}>오피스텔</option>
                  </select>
                  {errors.details?.propertyType && (
                    <p className="mt-1 text-sm text-red-600">
                      {errors.details.propertyType.message}
                    </p>
                  )}
                </div>
              )}
            />

            <div className="grid grid-cols-2 gap-4">
              <Controller
                name="details.roomCount"
                control={control}
                render={({ field }) => (
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      방 개수 *
                    </label>
                    <input
                      {...field}
                      type="number"
                      min="1"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                )}
              />

              <Controller
                name="details.bathroomCount"
                control={control}
                render={({ field }) => (
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      욕실 개수 *
                    </label>
                    <input
                      {...field}
                      type="number"
                      min="1"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                )}
              />
            </div>

            <Controller
              name="details.area"
              control={control}
              render={({ field }) => (
                <div>
                  <label className="block text-sm font-medium mb-2">
                    면적 (㎡) *
                  </label>
                  <input
                    {...field}
                    type="number"
                    min="1"
                    step="0.1"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>
              )}
            />
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold">주소 정보</h3>
            
            <Controller
              name="address"
              control={control}
              render={({ field }) => (
                <AddressSearch
                  value={field.value}
                  onChange={field.onChange}
                  error={errors.address?.fullAddress?.message}
                />
              )}
            />
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold">가격 정보</h3>
            
            <div className="grid grid-cols-2 gap-4">
              <Controller
                name="price.rent"
                control={control}
                render={({ field }) => (
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      월세 (원) *
                    </label>
                    <input
                      {...field}
                      type="number"
                      min="0"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="1000000"
                    />
                  </div>
                )}
              />

              <Controller
                name="price.deposit"
                control={control}
                render={({ field }) => (
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      보증금 (원) *
                    </label>
                    <input
                      {...field}
                      type="number"
                      min="0"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="10000000"
                    />
                  </div>
                )}
              />
            </div>

            <Controller
              name="price.maintenanceFee"
              control={control}
              render={({ field }) => (
                <div>
                  <label className="block text-sm font-medium mb-2">
                    관리비 (원)
                  </label>
                  <input
                    {...field}
                    type="number"
                    min="0"
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="100000"
                  />
                </div>
              )}
            />
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold">매물 옵션</h3>
            
            <Controller
              name="options"
              control={control}
              render={({ field }) => (
                <OptionSelector
                  selectedOptions={field.value || []}
                  onOptionsChange={field.onChange}
                />
              )}
            />

            <div className="space-y-4">
              <Controller
                name="details.furnished"
                control={control}
                render={({ field }) => (
                  <label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      checked={field.value}
                      onChange={field.onChange}
                      className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                    />
                    <span>가구 완비</span>
                  </label>
                )}
              />

              <Controller
                name="details.availableDate"
                control={control}
                render={({ field }) => (
                  <div>
                    <label className="block text-sm font-medium mb-2">
                      입주 가능일
                    </label>
                    <input
                      {...field}
                      type="date"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                )}
              />
            </div>
          </div>
        );

      case 5:
        return (
          <div className="space-y-6">
            <h3 className="text-lg font-semibold">이미지 업로드</h3>
            
            <Controller
              name="images"
              control={control}
              render={({ field }) => (
                <ImageUpload
                  images={field.value || []}
                  onImagesChange={field.onChange}
                  maxImages={10}
                />
              )}
            />
          </div>
        );

      default:
        return null;
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="max-w-4xl mx-auto">
      {/* 진행 단계 표시 */}
      <div className="mb-8">
        <div className="flex justify-between items-center">
          {Array.from({ length: totalSteps }, (_, i) => i + 1).map((step) => (
            <div
              key={step}
              className={`flex items-center ${
                step !== totalSteps ? 'flex-1' : ''
              }`}
            >
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium ${
                  step <= currentStep
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-200 text-gray-600'
                }`}
              >
                {step}
              </div>
              {step !== totalSteps && (
                <div
                  className={`flex-1 h-1 mx-2 ${
                    step < currentStep ? 'bg-blue-600' : 'bg-gray-200'
                  }`}
                />
              )}
            </div>
          ))}
        </div>
      </div>

      {/* 단계별 내용 */}
      <div className="mb-8">
        {renderStepContent()}
      </div>

      {/* 내비게이션 버튼 */}
      <div className="flex justify-between">
        <button
          type="button"
          onClick={prevStep}
          disabled={currentStep === 1}
          className="px-4 py-2 text-gray-600 border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
        >
          이전
        </button>

        <div className="space-x-2">
          {currentStep < totalSteps ? (
            <button
              type="button"
              onClick={nextStep}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
            >
              다음
            </button>
          ) : (
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? '저장 중...' : '매물 등록'}
            </button>
          )}
        </div>
      </div>
    </form>
  );
};

export default PropertyForm;
```

## API 연동 및 상태 관리

### Zustand 매물 스토어

```typescript
// store/propertyStore.ts
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { Property, PropertySearchFilters } from '@/types/property';
import {
  fetchProperties as apiFetchProperties,
  fetchProperty,
  createProperty,
  updateProperty,
  deleteProperty
} from '@/lib/api/property';

interface PropertyState {
  // 상태
  properties: Property[];
  property: Property | null;
  loading: boolean;
  error: string | null;
  
  // 페이지네이션
  currentPage: number;
  totalCount: number;
  pageSize: number;
  
  // 필터
  filters: PropertySearchFilters;
  
  // 액션
  setProperties: (properties: Property[]) => void;
  setProperty: (property: Property | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  setPage: (page: number) => void;
  setFilters: (filters: PropertySearchFilters) => void;
  
  // 비동기 액션
  fetchProperties: () => Promise<void>;
  fetchPropertyById: (id: number) => Promise<void>;
  createProperty: (data: any) => Promise<Property>;
  updateProperty: (id: number, data: any) => Promise<Property>;
  deleteProperty: (id: number) => Promise<void>;
  
  resetState: () => void;
}

const initialState = {
  properties: [],
  property: null,
  loading: false,
  error: null,
  currentPage: 1,
  totalCount: 0,
  pageSize: 12,
  filters: {}
};

export const usePropertyStore = create<PropertyState>()(
  devtools(
    (set, get) => ({
      ...initialState,

      // 동기 액션
      setProperties: (properties) => set({ properties }),
      setProperty: (property) => set({ property }),
      setLoading: (loading) => set({ loading }),
      setError: (error) => set({ error }),
      setPage: (page) => set({ currentPage: page }),
      setFilters: (filters) => set({ filters }),

      // 비동기 액션
      fetchProperties: async () => {
        const { currentPage, pageSize, filters } = get();
        
        set({ loading: true, error: null });
        
        try {
          const response = await apiFetchProperties({
            page: currentPage,
            size: pageSize,
            ...filters
          });
          
          set({
            properties: response.data.content,
            totalCount: response.data.totalElements,
            loading: false
          });
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : '매물을 불러오는데 실패했습니다.',
            loading: false
          });
        }
      },

      fetchPropertyById: async (id: number) => {
        set({ loading: true, error: null });
        
        try {
          const response = await fetchProperty(id);
          set({ property: response.data, loading: false });
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : '매물 정보를 불러오는데 실패했습니다.',
            loading: false
          });
        }
      },

      createProperty: async (data: any) => {
        set({ loading: true, error: null });
        
        try {
          const response = await createProperty(data);
          const newProperty = response.data;
          
          set((state) => ({
            properties: [newProperty, ...state.properties],
            loading: false
          }));
          
          return newProperty;
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : '매물 등록에 실패했습니다.',
            loading: false
          });
          throw error;
        }
      },

      updateProperty: async (id: number, data: any) => {
        set({ loading: true, error: null });
        
        try {
          const response = await updateProperty(id, data);
          const updatedProperty = response.data;
          
          set((state) => ({
            properties: state.properties.map(p => 
              p.id === id ? updatedProperty : p
            ),
            property: state.property?.id === id ? updatedProperty : state.property,
            loading: false
          }));
          
          return updatedProperty;
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : '매물 수정에 실패했습니다.',
            loading: false
          });
          throw error;
        }
      },

      deleteProperty: async (id: number) => {
        set({ loading: true, error: null });
        
        try {
          await deleteProperty(id);
          
          set((state) => ({
            properties: state.properties.filter(p => p.id !== id),
            property: state.property?.id === id ? null : state.property,
            loading: false
          }));
        } catch (error) {
          set({
            error: error instanceof Error ? error.message : '매물 삭제에 실패했습니다.',
            loading: false
          });
          throw error;
        }
      },

      resetState: () => set(initialState)
    }),
    {
      name: 'property-store'
    }
  )
);
```

## 반응형 매물 관리 대시보드

### 매물 관리 페이지

```tsx
// app/dashboard/properties/page.tsx
'use client';

import React, { useState } from 'react';
import Link from 'next/link';
import { usePropertyStore } from '@/store/propertyStore';
import PropertyList from '@/components/property/PropertyList';
import { Button } from '@/components/ui/Button';
import { Card } from '@/components/ui/Card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/Tabs';

const PropertiesManagementPage = () => {
  const { properties, loading, totalCount } = usePropertyStore();
  const [activeTab, setActiveTab] = useState('all');

  const getFilteredProperties = () => {
    switch (activeTab) {
      case 'active':
        return properties.filter(p => p.status === 'ACTIVE');
      case 'inactive':
        return properties.filter(p => p.status === 'INACTIVE');
      case 'rented':
        return properties.filter(p => p.status === 'RENTED');
      default:
        return properties;
    }
  };

  const stats = {
    total: totalCount,
    active: properties.filter(p => p.status === 'ACTIVE').length,
    inactive: properties.filter(p => p.status === 'INACTIVE').length,
    rented: properties.filter(p => p.status === 'RENTED').length
  };

  return (
    <div className="container mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold">매물 관리</h1>
          <p className="text-gray-600 mt-2">등록된 매물을 관리하고 수정할 수 있습니다.</p>
        </div>
        <Link href="/dashboard/properties/new">
          <Button size="lg">
            새 매물 등록
          </Button>
        </Link>
      </div>

      {/* 통계 카드 */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">전체 매물</p>
              <p className="text-2xl font-bold">{stats.total}</p>
            </div>
            <div className="h-12 w-12 bg-blue-100 rounded-lg flex items-center justify-center">
              🏠
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">임대 가능</p>
              <p className="text-2xl font-bold text-green-600">{stats.active}</p>
            </div>
            <div className="h-12 w-12 bg-green-100 rounded-lg flex items-center justify-center">
              ✅
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">비활성</p>
              <p className="text-2xl font-bold text-yellow-600">{stats.inactive}</p>
            </div>
            <div className="h-12 w-12 bg-yellow-100 rounded-lg flex items-center justify-center">
              ⏸️
            </div>
          </div>
        </Card>

        <Card className="p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">임대 완료</p>
              <p className="text-2xl font-bold text-red-600">{stats.rented}</p>
            </div>
            <div className="h-12 w-12 bg-red-100 rounded-lg flex items-center justify-center">
              🏘️
            </div>
          </div>
        </div>
      </div>

      {/* 매물 목록 탭 */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6">
          <TabsTrigger value="all">전체 ({stats.total})</TabsTrigger>
          <TabsTrigger value="active">임대가능 ({stats.active})</TabsTrigger>
          <TabsTrigger value="inactive">비활성 ({stats.inactive})</TabsTrigger>
          <TabsTrigger value="rented">임대완료 ({stats.rented})</TabsTrigger>
        </TabsList>

        <TabsContent value={activeTab}>
          <PropertyList
            initialProperties={getFilteredProperties()}
            showFilters={true}
          />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default PropertiesManagementPage;
```

## 성능 최적화 및 사용자 경험

### 가상화 및 무한 스크롤

```tsx
// components/property/VirtualizedPropertyList.tsx
import React, { useMemo } from 'react';
import { FixedSizeList as List } from 'react-window';
import { Property } from '@/types/property';
import PropertyCard from './PropertyCard';

interface VirtualizedPropertyListProps {
  properties: Property[];
  height: number;
  itemHeight: number;
  columns: number;
}

const VirtualizedPropertyList: React.FC<VirtualizedPropertyListProps> = ({
  properties,
  height,
  itemHeight,
  columns
}) => {
  const itemsPerRow = columns;
  const rowCount = Math.ceil(properties.length / itemsPerRow);

  const Row = ({ index, style }: { index: number; style: any }) => {
    const startIndex = index * itemsPerRow;
    const endIndex = Math.min(startIndex + itemsPerRow, properties.length);
    const rowProperties = properties.slice(startIndex, endIndex);

    return (
      <div style={style} className="flex gap-4 px-4">
        {rowProperties.map((property) => (
          <div key={property.id} className="flex-1">
            <PropertyCard property={property} />
          </div>
        ))}
      </div>
    );
  };

  return (
    <List
      height={height}
      itemCount={rowCount}
      itemSize={itemHeight}
      width="100%"
    >
      {Row}
    </List>
  );
};

export default VirtualizedPropertyList;
```

## 결론

Task 4의 매물 데이터 모델 및 CRUD API 구현 작업에 대한 프론트엔드 문서화가 완료되었습니다.

### 주요 구현 사항

1. **매물 관리 타입 시스템** - TypeScript 기반 강타입 인터페이스
2. **매물 CRUD 컴포넌트** - React Hook Form과 Zod 검증
3. **이미지 업로드 시스템** - 드래그앤드롭, 다중 업로드, 순서 관리
4. **검색 및 필터링** - 동적 쿼리와 사용자 친화적 UI
5. **상태 관리** - Zustand 기반 전역 상태 관리
6. **반응형 대시보드** - 모바일 최적화된 관리 인터페이스

### 기술 스택

- **Next.js 13+ App Router**
- **TypeScript**
- **React Hook Form + Zod**
- **Zustand**
- **Tailwind CSS**
- **React Beautiful DnD**
- **React Window (가상화)**

### 다음 단계

- Task 5: Google Maps 연동 및 위치 기반 서비스
- 성능 모니터링 및 최적화
- 접근성 개선 및 SEO 최적화

---

**문서 작성자**: Claude Code Auto-Documentation  
**생성일**: 2025-07-31  
**버전**: 5.0