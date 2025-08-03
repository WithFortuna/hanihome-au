'use client';

import React, { useState, useRef } from 'react';
import { ImageFile } from './image-dropzone';
import { GripVertical, Star, X, RotateCw, Edit3, Tag } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Input } from '@/components/ui/input';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';

interface ImageReorderProps {
  images: ImageFile[];
  onReorder: (reorderedImages: ImageFile[]) => void;
  onRemove: (imageId: string) => void;
  onSetThumbnail: (imageId: string) => void;
  onUpdateImage?: (imageId: string, updates: Partial<ImageFile>) => void;
  thumbnailId?: string;
}

export function ImageReorder({
  images,
  onReorder,
  onRemove,
  onSetThumbnail,
  onUpdateImage,
  thumbnailId,
}: ImageReorderProps) {
  const [draggedIndex, setDraggedIndex] = useState<number | null>(null);
  const [dragOverIndex, setDragOverIndex] = useState<number | null>(null);
  const [editingImage, setEditingImage] = useState<ImageFile | null>(null);
  const [editForm, setEditForm] = useState({
    alt: '',
    caption: '',
    tags: '',
  });
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

  const handleRotateImage = (imageId: string) => {
    if (!onUpdateImage) return;
    
    const image = images.find(img => img.id === imageId);
    if (!image) return;
    
    const currentRotation = image.metadata?.rotation || 0;
    const newRotation = (currentRotation + 90) % 360;
    
    onUpdateImage(imageId, {
      metadata: {
        ...image.metadata,
        rotation: newRotation,
      }
    });
  };

  const openEditDialog = (image: ImageFile) => {
    setEditingImage(image);
    setEditForm({
      alt: image.metadata?.alt || '',
      caption: image.metadata?.caption || '',
      tags: image.metadata?.tags?.join(', ') || '',
    });
  };

  const handleSaveMetadata = () => {
    if (!editingImage || !onUpdateImage) return;
    
    const tags = editForm.tags
      .split(',')
      .map(tag => tag.trim())
      .filter(tag => tag.length > 0);
    
    onUpdateImage(editingImage.id, {
      metadata: {
        ...editingImage.metadata,
        alt: editForm.alt,
        caption: editForm.caption,
        tags,
      }
    });
    
    setEditingImage(null);
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
                  alt={image.metadata?.alt || `Image ${index + 1}`}
                  className={`w-16 h-16 object-cover rounded-md border transition-transform ${
                    image.metadata?.rotation ? `rotate-[${image.metadata.rotation}deg]` : ''
                  }`}
                  style={{
                    transform: image.metadata?.rotation ? `rotate(${image.metadata.rotation}deg)` : 'none'
                  }}
                />
                {isThumbnail && (
                  <div className="absolute -top-1 -right-1 bg-yellow-400 text-white rounded-full p-1">
                    <Star className="w-3 h-3 fill-current" />
                  </div>
                )}
                {image.metadata?.tags && image.metadata.tags.length > 0 && (
                  <div className="absolute -bottom-1 -left-1 bg-blue-500 text-white rounded-full p-1">
                    <Tag className="w-2 h-2" />
                  </div>
                )}
              </div>

              {/* Image Info */}
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <p className="text-sm font-medium text-gray-700 truncate">
                    {image.metadata?.caption || image.file.name}
                  </p>
                  {isThumbnail && (
                    <Badge variant="secondary" className="bg-yellow-100 text-yellow-800 text-xs">
                      대표 이미지
                    </Badge>
                  )}
                </div>
                <div className="flex items-center gap-4 text-xs text-gray-500 mb-1">
                  <span>순서: {index + 1}</span>
                  <span>{Math.round(image.file.size / 1024)} KB</span>
                  {image.metadata?.rotation && image.metadata.rotation > 0 && (
                    <span className="text-purple-600">회전: {image.metadata.rotation}°</span>
                  )}
                </div>
                {image.metadata?.tags && image.metadata.tags.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-1">
                    {image.metadata.tags.slice(0, 3).map((tag, tagIndex) => (
                      <Badge key={tagIndex} variant="outline" className="text-xs px-1 py-0">
                        {tag}
                      </Badge>
                    ))}
                    {image.metadata.tags.length > 3 && (
                      <Badge variant="outline" className="text-xs px-1 py-0">
                        +{image.metadata.tags.length - 3}
                      </Badge>
                    )}
                  </div>
                )}
                <div className="text-xs text-gray-500">
                  {image.isUploaded ? (
                    <span className="text-green-600">업로드 완료</span>
                  ) : image.isUploading ? (
                    <span className="text-blue-600">업로드 중...</span>
                  ) : image.error ? (
                    <span className="text-red-600">업로드 실패</span>
                  ) : (
                    <span className="text-gray-400">대기 중</span>
                  )}
                </div>
              </div>

              {/* Action Buttons */}
              <div className="flex items-center gap-1">
                {!isThumbnail && (
                  <Button
                    size="sm"
                    variant="outline"
                    onClick={() => onSetThumbnail(image.id)}
                    className="text-xs h-7 px-2"
                  >
                    <Star className="w-3 h-3 mr-1" />
                    대표
                  </Button>
                )}
                
                {onUpdateImage && (
                  <>
                    <Button
                      size="sm"
                      variant="outline"
                      onClick={() => handleRotateImage(image.id)}
                      className="h-7 w-7 p-0"
                      title="90도 회전"
                    >
                      <RotateCw className="w-3 h-3" />
                    </Button>
                    
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => openEditDialog(image)}
                          className="h-7 w-7 p-0"
                          title="메타데이터 편집"
                        >
                          <Edit3 className="w-3 h-3" />
                        </Button>
                      </DialogTrigger>
                      <DialogContent>
                        <DialogHeader>
                          <DialogTitle>이미지 정보 편집</DialogTitle>
                        </DialogHeader>
                        <div className="space-y-4">
                          <div>
                            <Label htmlFor="alt">대체 텍스트 (Alt Text)</Label>
                            <Input
                              id="alt"
                              value={editForm.alt}
                              onChange={(e) => setEditForm(prev => ({ ...prev, alt: e.target.value }))}
                              placeholder="이미지 설명 입력"
                            />
                          </div>
                          <div>
                            <Label htmlFor="caption">캡션</Label>
                            <Input
                              id="caption"
                              value={editForm.caption}
                              onChange={(e) => setEditForm(prev => ({ ...prev, caption: e.target.value }))}
                              placeholder="이미지 제목이나 설명"
                            />
                          </div>
                          <div>
                            <Label htmlFor="tags">태그</Label>
                            <Input
                              id="tags"
                              value={editForm.tags}
                              onChange={(e) => setEditForm(prev => ({ ...prev, tags: e.target.value }))}
                              placeholder="태그들을 쉼표로 구분 (예: 거실, 넓음, 채광)"
                            />
                          </div>
                          <div className="flex gap-2 justify-end">
                            <Button variant="outline" onClick={() => setEditingImage(null)}>
                              취소
                            </Button>
                            <Button onClick={handleSaveMetadata}>
                              저장
                            </Button>
                          </div>
                        </div>
                      </DialogContent>
                    </Dialog>
                  </>
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