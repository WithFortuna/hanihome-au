'use client';

import React, { useCallback, useState } from 'react';
import { useDropzone } from 'react-dropzone';
import { Upload, X, Image as ImageIcon, AlertCircle, Zap } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { Badge } from '@/components/ui/badge';
import { 
  compressImage, 
  validateImageFile, 
  getImageDimensions,
  formatFileSize,
  PROPERTY_IMAGE_COMPRESSION_OPTIONS 
} from '@/lib/utils/image-compression';

export interface ImageFile {
  id: string;
  file: File;
  originalFile?: File; // Keep reference to original file
  preview: string;
  uploadProgress: number;
  isUploading: boolean;
  isUploaded: boolean;
  isCompressing?: boolean;
  compressionProgress?: number;
  error?: string;
  url?: string; // S3 URL after upload
  dimensions?: { width: number; height: number };
  compressionRatio?: number;
}

interface ImageDropzoneProps {
  onImagesChange: (images: ImageFile[]) => void;
  maxFiles?: number;
  maxFileSize?: number; // in bytes
  acceptedFileTypes?: string[];
  existingImages?: ImageFile[];
  enableCompression?: boolean;
  compressionQuality?: number;
}

export function ImageDropzone({
  onImagesChange,
  maxFiles = 10,
  maxFileSize = 5 * 1024 * 1024, // 5MB
  acceptedFileTypes = ['image/jpeg', 'image/png', 'image/webp'],
  existingImages = [],
  enableCompression = true,
  compressionQuality = 0.85,
}: ImageDropzoneProps) {
  const [images, setImages] = useState<ImageFile[]>(existingImages);
  const [uploadError, setUploadError] = useState<string | null>(null);

  const compressImageFile = async (imageFile: ImageFile): Promise<ImageFile> => {
    if (!enableCompression) return imageFile;

    try {
      // Update compression status
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...img, isCompressing: true, compressionProgress: 0 }
          : img
      ));

      // Get image dimensions
      const dimensions = await getImageDimensions(imageFile.file);
      
      // Simulate compression progress
      for (let progress = 0; progress <= 80; progress += 20) {
        setImages(prev => prev.map(img => 
          img.id === imageFile.id 
            ? { ...img, compressionProgress: progress }
            : img
        ));
        await new Promise(resolve => setTimeout(resolve, 200));
      }

      // Compress the image
      const compressionResult = await compressImage(imageFile.file, {
        ...PROPERTY_IMAGE_COMPRESSION_OPTIONS,
        initialQuality: compressionQuality,
      });

      // Final progress update
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...img, compressionProgress: 100 }
          : img
      ));

      await new Promise(resolve => setTimeout(resolve, 300));

      // Update with compressed file
      return {
        ...imageFile,
        originalFile: imageFile.file,
        file: compressionResult.compressedFile,
        dimensions,
        compressionRatio: compressionResult.compressionRatio,
        isCompressing: false,
        compressionProgress: 100,
      };
    } catch (error) {
      console.error('Compression failed:', error);
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...img, isCompressing: false, error: 'compression failed' }
          : img
      ));
      return imageFile;
    }
  };

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
      // First compress the image if enabled
      const compressedImageFile = await compressImageFile(imageFile);
      
      // Update state with compressed file
      setImages(prev => prev.map(img => 
        img.id === imageFile.id 
          ? { ...compressedImageFile, isUploading: true, error: undefined }
          : img
      ));

      const s3Url = await simulateUpload(compressedImageFile);

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
              isCompressing: false,
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

    // Validate each file
    const validFiles: File[] = [];
    const invalidFiles: string[] = [];

    for (const file of acceptedFiles) {
      const validation = validateImageFile(file);
      if (validation.isValid) {
        validFiles.push(file);
      } else {
        invalidFiles.push(`${file.name}: ${validation.error}`);
      }
    }

    if (invalidFiles.length > 0) {
      setUploadError(`Invalid files: ${invalidFiles.join('; ')}`);
      if (validFiles.length === 0) return;
    }

    // Process valid files
    const newImages: ImageFile[] = validFiles.map(file => ({
      id: Math.random().toString(36).substring(2),
      file,
      preview: URL.createObjectURL(file),
      uploadProgress: 0,
      isUploading: false,
      isUploaded: false,
      isCompressing: false,
      compressionProgress: 0,
    }));

    const updatedImages = [...images, ...newImages];
    setImages(updatedImages);
    onImagesChange(updatedImages);

    // Start processing each image (compression + upload)
    newImages.forEach(imageFile => {
      uploadImage(imageFile);
    });
  }, [images, maxFiles, onImagesChange, enableCompression, compressionQuality]);

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

  const getProcessingStatus = (imageFile: ImageFile): string => {
    if (imageFile.isCompressing) {
      return `압축 중... ${imageFile.compressionProgress || 0}%`;
    } else if (imageFile.isUploading) {
      return `업로드 중... ${imageFile.uploadProgress}%`;
    } else if (imageFile.isUploaded) {
      return '완료';
    } else if (imageFile.error) {
      return '오류';
    }
    return '대기 중';
  };

  const getOverallProgress = (): number => {
    if (images.length === 0) return 0;
    
    const totalProgress = images.reduce((sum, img) => {
      if (img.isUploaded) return sum + 100;
      if (img.isUploading) return sum + (50 + img.uploadProgress * 0.5);
      if (img.isCompressing) return sum + (img.compressionProgress || 0) * 0.5;
      return sum;
    }, 0);
    
    return Math.round(totalProgress / images.length);
  };

  const isAnyProcessing = images.some(img => img.isUploading || img.isCompressing);
  const uploadedCount = images.filter(img => img.isUploaded).length;
  const compressingCount = images.filter(img => img.isCompressing).length;

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

      {/* Processing Progress */}
      {isAnyProcessing && (
        <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
          <div className="flex items-center justify-between mb-2">
            <div className="flex items-center gap-2">
              <Zap className="w-4 h-4 text-blue-600" />
              <span className="text-sm font-medium text-blue-700">
                {compressingCount > 0 ? '이미지 압축 및 업로드 중...' : '업로드 진행 중...'}
              </span>
            </div>
            <span className="text-sm text-blue-600">{getOverallProgress()}%</span>
          </div>
          <Progress value={getOverallProgress()} className="h-2" />
          {enableCompression && compressingCount > 0 && (
            <p className="text-xs text-blue-600 mt-2">
              {compressingCount}개 이미지 압축 중 • 파일 크기를 최적화하고 있습니다
            </p>
          )}
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
                  
                  {/* Compression Status Overlay */}
                  {imageFile.isCompressing && (
                    <div className="absolute inset-0 bg-purple-500 bg-opacity-75 flex items-center justify-center">
                      <div className="text-center text-white">
                        <Zap className="w-6 h-6 mx-auto mb-1" />
                        <div className="text-xs mb-2">압축 중...</div>
                        <Progress 
                          value={imageFile.compressionProgress || 0} 
                          className="h-1 w-16 bg-purple-600" 
                        />
                      </div>
                    </div>
                  )}

                  {/* Upload Status Overlay */}
                  {imageFile.isUploading && !imageFile.isCompressing && (
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
                  <div className="flex items-center justify-center gap-2">
                    <span>{formatFileSize(imageFile.file.size)}</span>
                    {imageFile.originalFile && imageFile.compressionRatio && (
                      <Badge variant="secondary" className="text-xs bg-green-100 text-green-700">
                        -{imageFile.compressionRatio}%
                      </Badge>
                    )}
                  </div>
                  {imageFile.dimensions && (
                    <p className="text-gray-400">
                      {imageFile.dimensions.width} × {imageFile.dimensions.height}
                    </p>
                  )}
                  <p className="text-xs">{getProcessingStatus(imageFile)}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Upload Summary */}
      {images.length > 0 && (
        <div className="bg-gray-50 rounded-lg p-4">
          <div className="text-sm text-gray-600 space-y-1">
            <div className="flex justify-between">
              <span>총 이미지:</span>
              <span className="font-medium">{images.length}개</span>
            </div>
            <div className="flex justify-between">
              <span>업로드 완료:</span>
              <span className="font-medium text-green-600">{uploadedCount}개</span>
            </div>
            {enableCompression && (
              <div className="flex justify-between">
                <span>압축 진행 중:</span>
                <span className="font-medium text-purple-600">{compressingCount}개</span>
              </div>
            )}
            <div className="flex justify-between">
              <span>업로드 중:</span>
              <span className="font-medium text-blue-600">
                {images.filter(img => img.isUploading && !img.isCompressing).length}개
              </span>
            </div>
            {images.some(img => img.error) && (
              <div className="flex justify-between">
                <span>오류:</span>
                <span className="font-medium text-red-600">
                  {images.filter(img => img.error).length}개
                </span>
              </div>
            )}
            {enableCompression && images.some(img => img.compressionRatio) && (
              <div className="pt-2 border-t border-gray-200">
                <div className="flex justify-between">
                  <span>평균 압축률:</span>
                  <span className="font-medium text-green-600">
                    -{Math.round(
                      images
                        .filter(img => img.compressionRatio)
                        .reduce((sum, img) => sum + (img.compressionRatio || 0), 0) /
                      images.filter(img => img.compressionRatio).length
                    )}%
                  </span>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}