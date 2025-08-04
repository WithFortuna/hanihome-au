'use client';

import React, { useState, useCallback, useEffect } from 'react';
import { useFormContext } from 'react-hook-form';
import { PropertyFormData } from '@/lib/types/property';
import { ImageDropzone, ImageFile } from '../image-upload/image-dropzone';
import { ImageReorder } from '../image-upload/image-reorder';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';

export function PropertyImagesStep() {
  const { setValue, watch } = useFormContext<PropertyFormData>();
  const watchedImageUrls = watch('imageUrls') || [];
  
  const [images, setImages] = useState<ImageFile[]>([]);
  const [thumbnailId, setThumbnailId] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState('upload');

  // Convert existing URLs to ImageFile format on component mount
  useEffect(() => {
    if (watchedImageUrls.length > 0 && images.length === 0) {
      const existingImages: ImageFile[] = watchedImageUrls.map((url, index) => ({
        id: `existing-${index}`,
        file: new File([], `existing-image-${index + 1}.jpg`, { type: 'image/jpeg' }),
        preview: url,
        uploadProgress: 100,
        isUploading: false,
        isUploaded: true,
        url,
      }));
      setImages(existingImages);
      if (existingImages.length > 0 && !thumbnailId) {
        setThumbnailId(existingImages[0].id);
      }
    }
  }, [watchedImageUrls, images.length, thumbnailId]);

  // Update form data when images change
  const updateFormImages = useCallback((updatedImages: ImageFile[]) => {
    const urls = updatedImages
      .filter(img => img.isUploaded && img.url)
      .map(img => img.url!);
    setValue('imageUrls', urls);
  }, [setValue]);

  const handleImagesChange = useCallback((newImages: ImageFile[]) => {
    setImages(newImages);
    updateFormImages(newImages);
    
    // Auto-switch to reorder tab when images are added
    if (newImages.length > 0 && activeTab === 'upload') {
      setActiveTab('reorder');
    }
    
    // Set first image as thumbnail if none is set
    if (newImages.length > 0 && !thumbnailId) {
      setThumbnailId(newImages[0].id);
    }
  }, [updateFormImages, activeTab, thumbnailId]);

  const handleReorder = useCallback((reorderedImages: ImageFile[]) => {
    setImages(reorderedImages);
    updateFormImages(reorderedImages);
  }, [updateFormImages]);

  const handleRemoveImage = useCallback((imageId: string) => {
    const updatedImages = images.filter(img => img.id !== imageId);
    setImages(updatedImages);
    updateFormImages(updatedImages);
    
    // Reset thumbnail if the removed image was the thumbnail
    if (thumbnailId === imageId) {
      setThumbnailId(updatedImages.length > 0 ? updatedImages[0].id : null);
    }
    
    // Switch back to upload tab if no images left
    if (updatedImages.length === 0) {
      setActiveTab('upload');
    }
  }, [images, updateFormImages, thumbnailId]);

  const handleSetThumbnail = useCallback((imageId: string) => {
    setThumbnailId(imageId);
    
    // Update metadata for all images
    const updatedImages = images.map(img => ({
      ...img,
      metadata: {
        ...img.metadata,
        isThumbnail: img.id === imageId,
      }
    }));
    
    // Move thumbnail image to first position
    const imageIndex = updatedImages.findIndex(img => img.id === imageId);
    if (imageIndex > 0) {
      const reorderedImages = [...updatedImages];
      const thumbnailImage = reorderedImages.splice(imageIndex, 1)[0];
      reorderedImages.unshift(thumbnailImage);
      setImages(reorderedImages);
      updateFormImages(reorderedImages);
    } else {
      setImages(updatedImages);
      updateFormImages(updatedImages);
    }
  }, [images, updateFormImages]);

  const handleUpdateImage = useCallback((imageId: string, updates: Partial<ImageFile>) => {
    const updatedImages = images.map(img => 
      img.id === imageId ? { ...img, ...updates } : img
    );
    setImages(updatedImages);
    updateFormImages(updatedImages);
  }, [images, updateFormImages]);

  const uploadedCount = images.filter(img => img.isUploaded).length;
  const uploadingCount = images.filter(img => img.isUploading).length;
  const errorCount = images.filter(img => img.error).length;

  return (
    <div className="space-y-6">
      {/* Summary Info */}
      {images.length > 0 && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <h4 className="font-medium text-blue-800">이미지 업로드 현황</h4>
            <div className="flex gap-4 text-sm text-blue-700">
              <span>총 {images.length}개</span>
              <span>완료 {uploadedCount}개</span>
              {uploadingCount > 0 && <span>업로드 중 {uploadingCount}개</span>}
              {errorCount > 0 && <span className="text-red-600">오류 {errorCount}개</span>}
            </div>
          </div>
        </div>
      )}

      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-2">
          <TabsTrigger value="upload" className="flex items-center gap-2">
            이미지 업로드
            {images.length > 0 && (
              <span className="bg-blue-100 text-blue-800 text-xs px-2 py-0.5 rounded-full">
                {images.length}
              </span>
            )}
          </TabsTrigger>
          <TabsTrigger 
            value="reorder" 
            disabled={images.length === 0}
            className="flex items-center gap-2"
          >
            순서 관리
            {thumbnailId && (
              <span className="bg-yellow-100 text-yellow-800 text-xs px-2 py-0.5 rounded-full">
                대표
              </span>
            )}
          </TabsTrigger>
        </TabsList>

        <TabsContent value="upload" className="mt-6">
          <ImageDropzone
            onImagesChange={handleImagesChange}
            maxFiles={10}
            maxFileSize={5 * 1024 * 1024} // 5MB
            acceptedFileTypes={['image/jpeg', 'image/png', 'image/webp']}
            existingImages={images}
          />
        </TabsContent>

        <TabsContent value="reorder" className="mt-6">
          <ImageReorder
            images={images}
            onReorder={handleReorder}
            onRemove={handleRemoveImage}
            onSetThumbnail={handleSetThumbnail}
            onUpdateImage={handleUpdateImage}
            thumbnailId={thumbnailId || undefined}
          />
        </TabsContent>
      </Tabs>

      {/* Instructions */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-4">
        <h4 className="font-medium text-green-800 mb-2">이미지 관리 가이드</h4>
        <ul className="text-sm text-green-700 space-y-1">
          <li>• 최대 10개의 이미지까지 업로드 가능합니다</li>
          <li>• 각 이미지는 자동으로 압축되어 최적화됩니다</li>
          <li>• JPEG, PNG, WebP 형식을 지원합니다</li>
          <li>• 첫 번째 이미지가 대표 이미지로 설정됩니다</li>
          <li>• "순서 관리" 탭에서 드래그하여 순서를 변경할 수 있습니다</li>
          <li>• 이미지 회전, 캡션/태그 추가, 대체 텍스트 설정이 가능합니다</li>
          <li>• 태그를 추가하면 나중에 검색할 때 유용합니다</li>
        </ul>
      </div>
    </div>
  );
}