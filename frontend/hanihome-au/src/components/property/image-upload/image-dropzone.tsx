'use client';

import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, X, Image as ImageIcon, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';

export interface ImageFile {
  id: string;
  file: File;
  preview: string;
  uploadProgress: number;
  isUploading: boolean;
  isUploaded: boolean;
  error?: string;
  url?: string; // S3 URL after upload
}

interface ImageDropzoneProps {
  onImagesChange: (images: ImageFile[]) => void;
  maxFiles?: number;
  maxFileSize?: number; // in bytes
  acceptedFileTypes?: string[];
  existingImages?: ImageFile[];
}

export function ImageDropzone({
  onImagesChange,
  maxFiles = 10,
  maxFileSize = 5 * 1024 * 1024, // 5MB
  acceptedFileTypes = ['image/jpeg', 'image/png', 'image/webp'],
  existingImages = [],
}: ImageDropzoneProps) {
  const [images, setImages] = useState<ImageFile[]>(existingImages);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const simulateUpload = async (imageFile: ImageFile): Promise<string> => {
    // Simulate S3 upload process
    const updateProgress = (progress: number) => {
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...img, uploadProgress: progress }
          : img
      ));
    };

    // Simulate progressive upload
    for (let progress = 0; progress <= 100; progress += 10) {
      await new Promise(resolve => setTimeout(resolve, 100));
      updateProgress(progress);
    }

    // Simulate getting S3 URL
    const mockS3Url = `https://hanihome-storage.s3.amazonaws.com/properties/${imageFile.id}.jpg`;
    return mockS3Url;
  };

  const uploadImage = async (imageFile: ImageFile) => {
    try {
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...img, isUploading: true, error: undefined }
          : img
      ));

      const s3Url = await simulateUpload(imageFile);

      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { 
              ...img, 
              isUploading: false, 
              isUploaded: true, 
              uploadProgress: 100,
              url: s3Url 
            }
          : img
      ));
    } catch (error) {
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { 
              ...img, 
              isUploading: false, 
              error: 'Upload failed. Please try again.',
              uploadProgress: 0 
            }
          : img
      ));
    }
  };

  const onDrop = useCallback(async (acceptedFiles: File[], rejectedFiles: any[]) => {
    setUploadError(null);

    // Handle rejected files
    if (rejectedFiles.length > 0) {
      const errors = rejectedFiles.map(rejection => 
        rejection.errors.map((error: any) => error.message).join(', ')
      );
      setUploadError(`Some files were rejected: ${errors.join('; ')}`);
    }

    // Check total file count
    if (images.length + acceptedFiles.length > maxFiles) {
      setUploadError(`Maximum ${maxFiles} files allowed. Please remove some existing files.`);
      return;
    }

    // Process accepted files
    const newImages: ImageFile[] = acceptedFiles.map(file => ({
      id: Math.random().toString(36).substring(2),
      file,
      preview: URL.createObjectURL(file),
      uploadProgress: 0,
      isUploading: false,
      isUploaded: false,
    }));

    const updatedImages = [...images, ...newImages];
    setImages(updatedImages);
    onImagesChange(updatedImages);

    // Start uploading each image
    newImages.forEach(imageFile => {
      uploadImage(imageFile);
    });
  }, [images, maxFiles, onImagesChange]);

  const removeImage = (imageId: string) => {
    const imageToRemove = images.find(img => img.id === imageId);
    if (imageToRemove) {
      URL.revokeObjectURL(imageToRemove.preview);
    }
    
    const updatedImages = images.filter(img => img.id !== imageId);
    setImages(updatedImages);
    onImagesChange(updatedImages);
  };

  const retryUpload = (imageId: string) => {
    const imageFile = images.find(img => img.id === imageId);
    if (imageFile) {
      uploadImage(imageFile);
    }
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: acceptedFileTypes.reduce((acc, type) => ({ ...acc, [type]: [] }), {}),
    maxSize: maxFileSize,
    multiple: true,
    disabled: images.length >= maxFiles,
  });

  const formatFileSize = (bytes: number): string => {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const getTotalUploadProgress = (): number => {
    if (images.length === 0) return 0;
    const totalProgress = images.reduce((sum, img) => sum + img.uploadProgress, 0);
    return Math.round(totalProgress / images.length);
  };

  const isAnyUploading = images.some(img => img.isUploading);
  const uploadedCount = images.filter(img => img.isUploaded).length;

  return (
    <div className="space-y-6">
      {/* Dropzone Area */}
      <div
        {...getRootProps()}
        className={`
          border-2 border-dashed rounded-lg p-8 text-center cursor-pointer transition-colors
          ${isDragActive 
            ? 'border-blue-400 bg-blue-50' 
            : images.length >= maxFiles 
            ? 'border-gray-200 bg-gray-50 cursor-not-allowed' 
            : 'border-gray-300 hover:border-gray-400 hover:bg-gray-50'
          }
        `}
      >
        <input {...getInputProps()} />
        
        {images.length >= maxFiles ? (
          <div className="text-gray-500">
            <ImageIcon className="w-12 h-12 mx-auto mb-4 text-gray-400" />
            <p className="text-lg font-medium mb-2">최대 파일 수에 도달했습니다</p>
            <p className="text-sm">더 많은 이미지를 추가하려면 기존 이미지를 삭제하세요</p>
          </div>
        ) : isDragActive ? (
          <div className="text-blue-600">
            <Upload className="w-12 h-12 mx-auto mb-4" />
            <p className="text-lg font-medium mb-2">이미지를 여기에 놓으세요</p>
          </div>
        ) : (
          <div className="text-gray-600">
            <Upload className="w-12 h-12 mx-auto mb-4 text-gray-400" />
            <p className="text-lg font-medium mb-2">이미지를 드래그하거나 클릭하여 업로드</p>
            <p className="text-sm mb-4">
              최대 {maxFiles}개 파일, 파일당 최대 {formatFileSize(maxFileSize)}
            </p>
            <p className="text-xs text-gray-500">
              지원 형식: {acceptedFileTypes.map(type => type.split('/')[1].toUpperCase()).join(', ')}
            </p>
          </div>
        )}
      </div>

      {/* Upload Error */}
      {uploadError && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <div className="flex items-center">
            <AlertCircle className="w-5 h-5 text-red-500 mr-2" />
            <p className="text-sm text-red-700">{uploadError}</p>
          </div>
        </div>
      )}

      {/* Upload Progress */}
      {isAnyUploading && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between mb-2">
            <span className="text-sm font-medium text-blue-700">업로드 진행 중...</span>
            <span className="text-sm text-blue-600">{getTotalUploadProgress()}%</span>
          </div>
          <Progress value={getTotalUploadProgress()} className="h-2" />
        </div>
      )}

      {/* Images Grid */}
      {images.length > 0 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h4 className="font-medium text-gray-700">
              업로드된 이미지 ({uploadedCount}/{images.length})
            </h4>
            <Badge variant="secondary">
              {images.length}/{maxFiles}
            </Badge>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {images.map((imageFile) => (
              <div key={imageFile.id} className="relative group">
                <div className="aspect-square rounded-lg overflow-hidden border bg-gray-100">
                  <img
                    src={imageFile.preview}
                    alt="Preview"
                    className="w-full h-full object-cover"
                  />
                  
                  {/* Upload Status Overlay */}
                  {imageFile.isUploading && (
                    <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                      <div className="text-center text-white">
                        <div className="mb-2">업로드 중...</div>
                        <Progress 
                          value={imageFile.uploadProgress} 
                          className="h-1 w-16 bg-gray-600" 
                        />
                      </div>
                    </div>
                  )}

                  {/* Error State */}
                  {imageFile.error && (
                    <div className="absolute inset-0 bg-red-500 bg-opacity-75 flex items-center justify-center">
                      <div className="text-center text-white text-xs p-2">
                        <AlertCircle className="w-6 h-6 mx-auto mb-1" />
                        <p>업로드 실패</p>
                        <Button
                          size="sm"
                          variant="outline"
                          className="mt-2 text-xs h-6"
                          onClick={() => retryUpload(imageFile.id)}
                        >
                          재시도
                        </Button>
                      </div>
                    </div>
                  )}

                  {/* Success State */}
                  {imageFile.isUploaded && (
                    <div className="absolute top-2 right-2 bg-green-500 text-white rounded-full p-1">
                      <svg className="w-3 h-3" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                      </svg>
                    </div>
                  )}
                </div>

                {/* Remove Button */}
                <button
                  onClick={() => removeImage(imageFile.id)}
                  className="absolute -top-2 -right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:bg-red-600"
                >
                  <X className="w-3 h-3" />
                </button>

                {/* File Info */}
                <div className="mt-2 text-xs text-gray-500 text-center">
                  <p className="truncate">{imageFile.file.name}</p>
                  <p>{formatFileSize(imageFile.file.size)}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Summary */}
      {images.length > 0 && (
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="text-sm text-gray-600">
            <p>총 {images.length}개 이미지</p>
            <p>업로드 완료: {uploadedCount}개</p>
            <p>업로드 중: {images.filter(img => img.isUploading).length}개</p>
            {images.some(img => img.error) && (
              <p className="text-red-600">오류: {images.filter(img => img.error).length}개</p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}