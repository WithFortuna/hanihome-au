'use client';

import React, { useState, useRef } from 'react';
import { ImageFile } from './image-dropzone';
import { GripVertical, Star, X } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';

interface ImageReorderProps {
  images: ImageFile[];
  onReorder: (reorderedImages: ImageFile[]) => void;
  onRemove: (imageId: string) => void;
  onSetThumbnail: (imageId: string) => void;
  thumbnailId?: string;
}

export function ImageReorder({
  images,
  onReorder,
  onRemove,
  onSetThumbnail,
  thumbnailId,
}: ImageReorderProps) {
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const draggedItem = useRef<ImageFile | null>(null);

  const handleDragStart = (e: React.DragEvent, index: number) => {
    setDraggedIndex(index);
    draggedItem.current = images[index];
    e.dataTransfer.effectAllowed = 'move';
  };

  const handleDragOver = (e: React.DragEvent, index: number) => {
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    setDragOverIndex(index);
  };

  const handleDragLeave = () => {
    setDragOverIndex(null);
  };

  const handleDrop = (e: React.DragEvent, dropIndex: number) => {
    e.preventDefault();
    
    if (draggedIndex === null || draggedItem.current === null) return;
    
    const newImages = [...images];
    const draggedItemData = draggedItem.current;
    
    // Remove dragged item from its original position
    newImages.splice(draggedIndex, 1);
    
    // Insert at new position
    newImages.splice(dropIndex, 0, draggedItemData);
    
    onReorder(newImages);
    
    // Reset drag state
    setDraggedIndex(null);
    setDragOverIndex(null);
    draggedItem.current = null;
  };

  const handleDragEnd = () => {
    setDraggedIndex(null);
    setDragOverIndex(null);
    draggedItem.current = null;
  };

  if (images.length === 0) {
    return (
      <div className="text-center text-gray-500 py-8">
        <p>업로드된 이미지가 없습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h4 className="font-medium text-gray-700">이미지 순서 관리</h4>
        <p className="text-sm text-gray-500">드래그하여 순서를 변경하세요</p>
      </div>

      <div className="grid gap-3">
        {images.map((image, index) => {
          const isThumbnail = thumbnailId === image.id;
          const isDragging = draggedIndex === index;
          const isDragOver = dragOverIndex === index;
          
          return (
            <div
              key={image.id}
              draggable
              onDragStart={(e) => handleDragStart(e, index)}
              onDragOver={(e) => handleDragOver(e, index)}
              onDragLeave={handleDragLeave}
              onDrop={(e) => handleDrop(e, index)}
              onDragEnd={handleDragEnd}
              className={`
                flex items-center gap-4 p-3 bg-white border rounded-lg transition-all cursor-move
                ${isDragging ? 'opacity-50 scale-95' : ''}
                ${isDragOver ? 'border-blue-400 bg-blue-50' : 'border-gray-200 hover:border-gray-300'}
                ${isThumbnail ? 'ring-2 ring-yellow-400 bg-yellow-50' : ''}
              `}
            >
              {/* Drag Handle */}
              <div className="flex-shrink-0 text-gray-400 hover:text-gray-600">
                <GripVertical className="w-5 h-5" />
              </div>

              {/* Image Preview */}
              <div className="flex-shrink-0 relative">
                <img
                  src={image.preview}
                  alt={`Image ${index + 1}`}
                  className="w-16 h-16 object-cover rounded-md border"
                />
                {isThumbnail && (
                  <div className="absolute -top-1 -right-1 bg-yellow-400 text-white rounded-full p-1">
                    <Star className="w-3 h-3 fill-current" />
                  </div>
                )}
              </div>

              {/* Image Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <p className="text-sm font-medium text-gray-700 truncate">
                    {image.file.name}
                  </p>
                  {isThumbnail && (
                    <Badge variant="secondary" className="bg-yellow-100 text-yellow-800 text-xs">
                      대표 이미지
                    </Badge>
                  )}
                </div>
                <div className="flex items-center gap-4 text-xs text-gray-500">
                  <span>순서: {index + 1}</span>
                  <span>{Math.round(image.file.size / 1024)} KB</span>
                  <span>
                    {image.isUploaded ? (
                      <span className="text-green-600">업로드 완료</span>
                    ) : image.isUploading ? (
                      <span className="text-blue-600">업로드 중...</span>
                    ) : image.error ? (
                      <span className="text-red-600">업로드 실패</span>
                    ) : (
                      <span className="text-gray-400">대기 중</span>
                    )}
                  </span>
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-2">
                {!isThumbnail && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => onSetThumbnail(image.id)}
                    className="text-xs h-7"
                  >
                    <Star className="w-3 h-3 mr-1" />
                    대표 설정
                  </Button>
                )}
                
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => onRemove(image.id)}
                  className="text-red-600 hover:text-red-700 hover:bg-red-50 h-7 w-7 p-0"
                >
                  <X className="w-3 h-3" />
                </Button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Instructions */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h5 className="font-medium text-blue-800 mb-2">이미지 관리 안내</h5>
        <ul className="text-sm text-blue-700 space-y-1">
          <li>• 드래그하여 이미지 순서를 변경할 수 있습니다</li>
          <li>• "대표 설정" 버튼을 클릭하여 썸네일 이미지를 지정하세요</li>
          <li>• 첫 번째 이미지가 기본 썸네일로 사용됩니다</li>
          <li>• X 버튼을 클릭하여 이미지를 삭제할 수 있습니다</li>
        </ul>
      </div>
    </div>
  );
}