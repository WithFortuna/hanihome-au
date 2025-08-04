'use client';

import React, { useState, useCallback, useMemo } from 'react';
import { 
  Trash2, 
  RotateCcw, 
  AlertTriangle, 
  Calendar, 
  User, 
  Clock, 
  Search,
  Filter,
  CheckSquare,
  Square,
  Download,
  AlertCircle,
  Info,
  Eye,
  FileX,
  Archive,
  Database,
  FileImage,
  MessageSquare,
  Heart,
  Timer,
  Zap,
  Shield
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { PropertyWithStats } from '../property-dashboard';
import { EnhancedPropertyDeleteModal } from '../enhanced-property-delete-modal';

interface DeletedProperty extends PropertyWithStats {
  deletedAt: Date;
  deletedBy: string;
  deletedByName: string;
  deleteReason?: string;
  scheduledPermanentDeletion: Date;
  canRestore: boolean;
  relatedDataCount: {
    inquiries: number;
    favorites: number;
    views: number;
    images: number;
  };
}

interface PropertyTrashManagerProps {
  onRestoreProperty: (propertyId: string) => void;
  onPermanentDelete: (propertyId: string) => void;
  onBulkRestore: (propertyIds: string[]) => void;
  onBulkPermanentDelete: (propertyIds: string[]) => void;
  onCleanupExpired: () => void;
  isLoading?: boolean;
}

export default function PropertyTrashManager({
  onRestoreProperty,
  onPermanentDelete,
  onBulkRestore,
  onBulkPermanentDelete,
  onCleanupExpired,
  isLoading = false,
}: PropertyTrashManagerProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterPeriod, setFilterPeriod] = useState<string>('all');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{
    type: 'restore' | 'permanent_delete' | 'cleanup';
    propertyIds?: string[];
  } | null>(null);

  // Mock deleted properties data
  const [deletedProperties] = useState<DeletedProperty[]>([
    {
      id: '1',
      title: '삭제된 매물 1',
      description: '테스트용 삭제된 매물입니다.',
      address: '서울시 강남구 역삼동',
      rent: 1200000,
      deposit: 10000000,
      area: 84,
      rooms: 3,
      bathrooms: 2,
      floor: 5,
      totalFloors: 15,
      propertyType: 'APARTMENT' as any,
      rentalType: 'MONTHLY' as any,
      status: 'INACTIVE' as any,
      createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000),
      updatedAt: new Date(Date.now() - 10 * 24 * 60 * 60 * 1000),
      views: 150,
      inquiries: 8,
      favorites: 12,
      userId: 'user1',
      isActive: false,
      deletedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
      deletedBy: 'user1',
      deletedByName: '관리자',
      deleteReason: '중복 매물로 인한 삭제',
      scheduledPermanentDeletion: new Date(Date.now() + 25 * 24 * 60 * 60 * 1000),
      canRestore: true,
      relatedDataCount: {
        inquiries: 8,
        favorites: 12,
        views: 150,
        images: 5,
      },
      contactName: '홍길동',
      contactPhone: '010-1234-5678',
      images: ['image1.jpg', 'image2.jpg'],
      options: ['주차가능', '엘리베이터'],
      managementFee: 50000,
      availableFrom: new Date(),
      latitude: 37.5665,
      longitude: 126.9780,
    },
    {
      id: '2',
      title: '삭제된 매물 2',
      description: '만료된 임대 계약으로 삭제',
      address: '서울시 서초구 서초동',
      rent: 800000,
      deposit: 5000000,
      area: 60,
      rooms: 2,
      bathrooms: 1,
      floor: 3,
      totalFloors: 12,
      propertyType: 'VILLA' as any,
      rentalType: 'MONTHLY' as any,
      status: 'INACTIVE' as any,
      createdAt: new Date(Date.now() - 45 * 24 * 60 * 60 * 1000),
      updatedAt: new Date(Date.now() - 32 * 24 * 60 * 60 * 1000),
      views: 89,
      inquiries: 5,
      favorites: 3,
      userId: 'user2',
      isActive: false,
      deletedAt: new Date(Date.now() - 32 * 24 * 60 * 60 * 1000),
      deletedBy: 'user2',
      deletedByName: '중개인',
      deleteReason: '임대 계약 만료',
      scheduledPermanentDeletion: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // Expired
      canRestore: false,
      relatedDataCount: {
        inquiries: 5,
        favorites: 3,
        views: 89,
        images: 3,
      },
      contactName: '김영희',
      contactPhone: '010-5678-9012',
      images: ['image3.jpg'],
      options: ['반려동물 가능'],
      managementFee: 30000,
      availableFrom: new Date(),
      latitude: 37.4833,
      longitude: 127.0522,
    }
  ]);

  const filteredProperties = deletedProperties.filter(property => {
    // Search filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      if (
        !property.title.toLowerCase().includes(searchLower) &&
        !property.address.toLowerCase().includes(searchLower) &&
        !property.deletedByName.toLowerCase().includes(searchLower)
      ) {
        return false;
      }
    }

    // Period filter
    if (filterPeriod !== 'all') {
      const now = new Date();
      const deletedDate = property.deletedAt;
      const daysDiff = (now.getTime() - deletedDate.getTime()) / (1000 * 60 * 60 * 24);

      switch (filterPeriod) {
        case 'today':
          if (daysDiff > 1) return false;
          break;
        case 'week':
          if (daysDiff > 7) return false;
          break;
        case 'month':
          if (daysDiff > 30) return false;
          break;
        case 'expired':
          if (property.scheduledPermanentDeletion > now) return false;
          break;
      }
    }

    return true;
  });

  const expiredProperties = deletedProperties.filter(
    p => p.scheduledPermanentDeletion < new Date()
  );

  const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  };

  const formatRelativeTime = (date: Date) => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return '오늘';
    if (diffDays === 1) return '어제';
    if (diffDays < 7) return `${diffDays}일 전`;
    if (diffDays < 30) return `${Math.floor(diffDays / 7)}주 전`;
    return formatDate(date);
  };

  const getDaysUntilPermanentDeletion = (scheduledDate: Date) => {
    const now = new Date();
    const diffMs = scheduledDate.getTime() - now.getTime();
    const diffDays = Math.ceil(diffMs / (1000 * 60 * 60 * 24));
    return diffDays;
  };

  const handleSelectAll = () => {
    if (selectedIds.length === filteredProperties.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredProperties.map(p => p.id));
    }
  };

  const handleIndividualSelect = (propertyId: string) => {
    if (selectedIds.includes(propertyId)) {
      setSelectedIds(selectedIds.filter(id => id !== propertyId));
    } else {
      setSelectedIds([...selectedIds, propertyId]);
    }
  };

  const handleAction = (
    type: 'restore' | 'permanent_delete' | 'cleanup', 
    propertyIds?: string[]
  ) => {
    setPendingAction({ type, propertyIds });
    setShowConfirmDialog(true);
  };

  const confirmAction = () => {
    if (!pendingAction) return;

    switch (pendingAction.type) {
      case 'restore':
        if (pendingAction.propertyIds) {
          if (pendingAction.propertyIds.length === 1) {
            onRestoreProperty(pendingAction.propertyIds[0]);
          } else {
            onBulkRestore(pendingAction.propertyIds);
          }
        }
        break;
      case 'permanent_delete':
        if (pendingAction.propertyIds) {
          if (pendingAction.propertyIds.length === 1) {
            onPermanentDelete(pendingAction.propertyIds[0]);
          } else {
            onBulkPermanentDelete(pendingAction.propertyIds);
          }
        }
        break;
      case 'cleanup':
        onCleanupExpired();
        break;
    }

    setShowConfirmDialog(false);
    setPendingAction(null);
    setSelectedIds([]);
  };

  const cancelAction = () => {
    setShowConfirmDialog(false);
    setPendingAction(null);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">휴지통</h3>
          <p className="text-sm text-gray-600">
            삭제된 매물을 복원하거나 영구 삭제할 수 있습니다.
          </p>
        </div>

        {expiredProperties.length > 0 && (
          <Button
            variant="outline"
            onClick={() => handleAction('cleanup')}
            className="flex items-center gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
          >
            <FileX className="w-4 h-4" />
            만료된 매물 정리 ({expiredProperties.length})
          </Button>
        )}
      </div>

      {/* Warnings */}
      {expiredProperties.length > 0 && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="p-4">
            <div className="flex items-center gap-3">
              <AlertTriangle className="w-5 h-5 text-red-600" />
              <div>
                <p className="font-medium text-red-800">
                  {expiredProperties.length}개의 매물이 영구 삭제 예정입니다
                </p>
                <p className="text-sm text-red-700">
                  이들 매물은 복원 기간이 만료되어 곧 영구적으로 삭제됩니다.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="매물명, 주소, 삭제자 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            
            <Select value={filterPeriod} onValueChange={setFilterPeriod}>
              <SelectTrigger className="w-full sm:w-40">
                <SelectValue placeholder="삭제 기간" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">전체 기간</SelectItem>
                <SelectItem value="today">오늘</SelectItem>
                <SelectItem value="week">이번 주</SelectItem>
                <SelectItem value="month">이번 달</SelectItem>
                <SelectItem value="expired">만료된 매물</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* Bulk Actions */}
      {selectedIds.length > 0 && (
        <Card className="border-blue-200 bg-blue-50">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Badge variant="secondary">{selectedIds.length}개 선택됨</Badge>
                <span className="text-sm text-gray-700">
                  선택된 매물에 대해 일괄 작업을 수행할 수 있습니다.
                </span>
              </div>
              
              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleAction('restore', selectedIds)}
                  disabled={isLoading}
                  className="flex items-center gap-2"
                >
                  <RotateCcw className="w-3 h-3" />
                  일괄 복원
                </Button>
                
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleAction('permanent_delete', selectedIds)}
                  disabled={isLoading}
                  className="flex items-center gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
                >
                  <Trash2 className="w-3 h-3" />
                  영구 삭제
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Select All */}
      <div className="flex items-center gap-3">
        <button
          onClick={handleSelectAll}
          className="flex items-center gap-2 text-sm font-medium text-gray-700 hover:text-gray-900"
        >
          {selectedIds.length === filteredProperties.length ? (
            <CheckSquare className="w-4 h-4 text-blue-600" />
          ) : selectedIds.length > 0 ? (
            <div className="w-4 h-4 border-2 border-blue-600 bg-blue-600 flex items-center justify-center">
              <div className="w-1.5 h-0.5 bg-white"></div>
            </div>
          ) : (
            <Square className="w-4 h-4 text-gray-400" />
          )}
          전체 선택
        </button>
      </div>

      {/* Properties List */}
      <div className="space-y-4">
        {filteredProperties.length === 0 ? (
          <Card>
            <CardContent className="p-8 text-center">
              <Trash2 className="w-12 h-12 mx-auto mb-4 text-gray-400" />
              <p className="text-gray-500">삭제된 매물이 없습니다.</p>
            </CardContent>
          </Card>
        ) : (
          filteredProperties.map((property) => {
            const isSelected = selectedIds.includes(property.id);
            const daysLeft = getDaysUntilPermanentDeletion(property.scheduledPermanentDeletion);
            const isExpired = daysLeft <= 0;
            
            return (
              <Card 
                key={property.id} 
                className={`transition-all ${
                  isSelected ? 'ring-2 ring-blue-500 bg-blue-50' : 
                  isExpired ? 'border-red-200 bg-red-50' : 'hover:shadow-md'
                }`}
              >
                <CardContent className="p-4">
                  <div className="flex items-start gap-4">
                    {/* Checkbox */}
                    <button
                      onClick={() => handleIndividualSelect(property.id)}
                      className="flex-shrink-0 mt-1"
                    >
                      {isSelected ? (
                        <CheckSquare className="w-4 h-4 text-blue-600" />
                      ) : (
                        <Square className="w-4 h-4 text-gray-400 hover:text-gray-600" />
                      )}
                    </button>

                    {/* Property Info */}
                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h4 className="font-medium text-gray-900 flex items-center gap-2">
                            {property.title}
                            {isExpired && (
                              <Badge className="bg-red-100 text-red-800 text-xs">
                                만료됨
                              </Badge>
                            )}
                          </h4>
                          <p className="text-sm text-gray-600">{property.address}</p>
                        </div>
                        
                        <div className="flex items-center gap-2">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleAction('restore', [property.id])}
                            disabled={isLoading || !property.canRestore}
                            className="h-8"
                          >
                            <RotateCcw className="w-3 h-3 mr-1" />
                            복원
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => handleAction('permanent_delete', [property.id])}
                            disabled={isLoading}
                            className="h-8 text-red-600 hover:text-red-700 hover:bg-red-50"
                          >
                            <Trash2 className="w-3 h-3 mr-1" />
                            영구삭제
                          </Button>
                        </div>
                      </div>

                      {/* Property Details */}
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-3 text-sm text-gray-600">
                        <div>월세: {property.rent.toLocaleString()}원</div>
                        <div>면적: {property.area}㎡</div>
                        <div>조회: {property.views}회</div>
                        <div>문의: {property.inquiries}건</div>
                      </div>

                      {/* Deletion Info */}
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
                          <div>
                            <span className="font-medium text-gray-700">삭제일:</span>
                            <div className="text-gray-600">{formatRelativeTime(property.deletedAt)}</div>
                          </div>
                          <div>
                            <span className="font-medium text-gray-700">삭제자:</span>
                            <div className="text-gray-600">{property.deletedByName}</div>
                          </div>
                          <div>
                            <span className="font-medium text-gray-700">영구 삭제 예정:</span>
                            <div className={`${isExpired ? 'text-red-600 font-medium' : 'text-gray-600'}`}>
                              {isExpired ? '만료됨' : `${daysLeft}일 후`}
                            </div>
                          </div>
                        </div>
                        
                        {property.deleteReason && (
                          <div className="mt-2 pt-2 border-t border-gray-200">
                            <span className="font-medium text-gray-700 text-xs">삭제 사유:</span>
                            <div className="text-gray-600 text-xs">{property.deleteReason}</div>
                          </div>
                        )}

                        {/* Related Data Warning */}
                        <div className="mt-2 pt-2 border-t border-gray-200">
                          <div className="flex items-center gap-2 text-xs text-orange-600">
                            <AlertCircle className="w-3 h-3" />
                            <span>
                              관련 데이터: 문의 {property.relatedDataCount.inquiries}건, 
                              관심 {property.relatedDataCount.favorites}건, 
                              이미지 {property.relatedDataCount.images}개
                            </span>
                          </div>
                        </div>

                        {/* Progress bar for time left */}
                        {!isExpired && (
                          <div className="mt-3">
                            <div className="flex justify-between items-center mb-1">
                              <span className="text-xs font-medium text-gray-700">복원 가능 기간</span>
                              <span className="text-xs text-gray-600">{daysLeft}일 남음</span>
                            </div>
                            <Progress 
                              value={Math.max(0, (30 - (30 - daysLeft)) / 30 * 100)} 
                              className="h-1"
                            />
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            );
          })
        )}
      </div>

      {/* Summary Stats */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">휴지통 요약</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-gray-600">
                {deletedProperties.length}
              </div>
              <div className="text-sm text-gray-500">삭제된 매물</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">
                {deletedProperties.filter(p => p.canRestore).length}
              </div>
              <div className="text-sm text-gray-500">복원 가능</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-red-600">
                {expiredProperties.length}
              </div>
              <div className="text-sm text-gray-500">만료됨</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-blue-600">
                {deletedProperties.reduce((sum, p) => sum + p.relatedDataCount.inquiries, 0)}
              </div>
              <div className="text-sm text-gray-500">관련 문의</div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Confirmation Dialog */}
      {showConfirmDialog && pendingAction && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <CardContent className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <AlertTriangle className="w-6 h-6 text-orange-500" />
                <h3 className="text-lg font-semibold text-gray-900">작업 확인</h3>
              </div>

              <div className="space-y-4">
                <p className="text-gray-600">
                  {pendingAction.type === 'restore' 
                    ? `${pendingAction.propertyIds?.length || 0}개의 매물을 복원하시겠습니까?`
                    : pendingAction.type === 'permanent_delete'
                    ? `${pendingAction.propertyIds?.length || 0}개의 매물을 영구적으로 삭제하시겠습니까?`
                    : `만료된 ${expiredProperties.length}개의 매물을 정리하시겠습니까?`
                  }
                </p>

                {(pendingAction.type === 'permanent_delete' || pendingAction.type === 'cleanup') && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                    <p className="text-sm text-red-800">
                      ⚠️ 이 작업은 되돌릴 수 없습니다. 매물과 모든 관련 데이터가 영구적으로 삭제됩니다.
                    </p>
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <Button
                  variant="outline"
                  onClick={cancelAction}
                  disabled={isLoading}
                >
                  취소
                </Button>
                <Button
                  onClick={confirmAction}
                  disabled={isLoading}
                  className={
                    pendingAction.type === 'restore'
                      ? 'bg-green-600 hover:bg-green-700'
                      : 'bg-red-600 hover:bg-red-700'
                  }
                >
                  {isLoading ? '처리 중...' : 
                   pendingAction.type === 'restore' ? '복원' : '삭제'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  );
}