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
  Shield,
  Settings,
  Activity,
  Plus,
  Minus,
  RefreshCcw
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Progress } from '@/components/ui/progress';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Checkbox } from '@/components/ui/checkbox';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { PropertyWithStats } from './property-dashboard';

interface DeletedProperty extends PropertyWithStats {
  deletedAt: Date;
  deletedBy: string;
  deletedByName: string;
  deleteReason?: string;
  deleteType: 'soft' | 'archive';
  scheduledPermanentDeletion: Date;
  canRestore: boolean;
  relatedDataCount: {
    inquiries: number;
    favorites: number;
    views: number;
    images: number;
    documents: number;
  };
  deletionHistory: Array<{
    id: string;
    action: 'deleted' | 'restored' | 'archived';
    timestamp: Date;
    userId: string;
    userName: string;
    reason?: string;
  }>;
}

interface BulkActionProgress {
  total: number;
  completed: number;
  failed: number;
  currentAction: string;
}

interface EnhancedPropertyTrashManagerProps {
  onRestoreProperty: (propertyId: string, reason?: string) => Promise<void>;
  onPermanentDelete: (propertyId: string, reason?: string) => Promise<void>;
  onBulkRestore: (propertyIds: string[], reason?: string) => Promise<void>;
  onBulkPermanentDelete: (propertyIds: string[], reason?: string) => Promise<void>;
  onCleanupExpired: () => Promise<void>;
  onExportData: (propertyIds: string[]) => Promise<void>;
  isLoading?: boolean;
  permissions?: {
    canRestore: boolean;
    canPermanentDelete: boolean;
    canBulkOperations: boolean;
    canExportData: boolean;
    canViewHistory: boolean;
  };
}

export function EnhancedPropertyTrashManager({
  onRestoreProperty,
  onPermanentDelete,
  onBulkRestore,
  onBulkPermanentDelete,
  onCleanupExpired,
  onExportData,
  isLoading = false,
  permissions = {
    canRestore: true,
    canPermanentDelete: true,
    canBulkOperations: true,
    canExportData: true,
    canViewHistory: true,
  },
}: EnhancedPropertyTrashManagerProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterPeriod, setFilterPeriod] = useState<string>('all');
  const [filterType, setFilterType] = useState<string>('all');
  const [selectedIds, setSelectedIds] = useState<string[]>([]);
  const [showBulkActionDialog, setShowBulkActionDialog] = useState(false);
  const [showHistoryDialog, setShowHistoryDialog] = useState(false);
  const [selectedPropertyHistory, setSelectedPropertyHistory] = useState<DeletedProperty | null>(null);
  const [bulkActionType, setBulkActionType] = useState<'restore' | 'permanent_delete' | null>(null);
  const [bulkActionReason, setBulkActionReason] = useState('');
  const [bulkActionProgress, setBulkActionProgress] = useState<BulkActionProgress | null>(null);
  const [activeTab, setActiveTab] = useState('deleted');

  // Mock data
  const [deletedProperties] = useState<DeletedProperty[]>([
    {
      id: '1',
      title: '강남구 신축 아파트',
      description: '깨끗하고 현대적인 아파트입니다.',
      address: '서울시 강남구 역삼동 123-45',
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
      updatedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
      views: 150,
      inquiries: 8,
      favorites: 12,
      userId: 'user1',
      isActive: false,
      deletedAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
      deletedBy: 'user1',
      deletedByName: '홍길동',
      deleteReason: '중복 매물로 인한 삭제',
      deleteType: 'soft',
      scheduledPermanentDeletion: new Date(Date.now() + 25 * 24 * 60 * 60 * 1000),
      canRestore: true,
      relatedDataCount: {
        inquiries: 8,
        favorites: 12,
        views: 150,
        images: 5,
        documents: 2,
      },
      deletionHistory: [
        {
          id: '1',
          action: 'deleted',
          timestamp: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000),
          userId: 'user1',
          userName: '홍길동',
          reason: '중복 매물로 인한 삭제',
        },
      ],
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
      title: '서초구 빌라',
      description: '조용하고 깔끔한 빌라입니다.',
      address: '서울시 서초구 서초동 456-78',
      rent: 800000,
      deposit: 5000000,
      area: 60,
      rooms: 2,
      bathrooms: 1,
      floor: 3,
      totalFloors: 4,
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
      deletedByName: '이영희',
      deleteReason: '임대 완료',
      deleteType: 'archive',
      scheduledPermanentDeletion: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // Already expired
      canRestore: false,
      relatedDataCount: {
        inquiries: 5,
        favorites: 3,
        views: 89,
        images: 3,
        documents: 1,
      },
      deletionHistory: [
        {
          id: '2',
          action: 'archived',
          timestamp: new Date(Date.now() - 32 * 24 * 60 * 60 * 1000),
          userId: 'user2',
          userName: '이영희',
          reason: '임대 완료',
        },
      ],
      contactName: '이영희',
      contactPhone: '010-5678-9012',
      images: ['image3.jpg'],
      options: ['주차가능'],
      managementFee: 30000,
      availableFrom: new Date(),
      latitude: 37.4946,
      longitude: 127.0277,
    },
  ]);

  const filteredProperties = useMemo(() => {
    let filtered = deletedProperties;

    // Search filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      filtered = filtered.filter(property =>
        property.title.toLowerCase().includes(searchLower) ||
        property.address.toLowerCase().includes(searchLower) ||
        property.deletedByName.toLowerCase().includes(searchLower)
      );
    }

    // Period filter
    if (filterPeriod !== 'all') {
      const now = Date.now();
      const periods = {
        '7d': 7 * 24 * 60 * 60 * 1000,
        '30d': 30 * 24 * 60 * 60 * 1000,
        '90d': 90 * 24 * 60 * 60 * 1000,
      };
      const periodMs = periods[filterPeriod as keyof typeof periods];
      if (periodMs) {
        filtered = filtered.filter(property => 
          now - property.deletedAt.getTime() <= periodMs
        );
      }
    }

    // Type filter
    if (filterType !== 'all') {
      if (filterType === 'recoverable') {
        filtered = filtered.filter(property => property.canRestore);
      } else if (filterType === 'expired') {
        filtered = filtered.filter(property => 
          property.scheduledPermanentDeletion.getTime() < Date.now()
        );
      } else {
        filtered = filtered.filter(property => property.deleteType === filterType);
      }
    }

    return filtered;
  }, [deletedProperties, searchTerm, filterPeriod, filterType]);

  const stats = useMemo(() => {
    const total = deletedProperties.length;
    const recoverable = deletedProperties.filter(p => p.canRestore).length;
    const expired = deletedProperties.filter(p => 
      p.scheduledPermanentDeletion.getTime() < Date.now()
    ).length;
    const totalDataSize = deletedProperties.reduce((sum, p) => 
      sum + p.relatedDataCount.images + p.relatedDataCount.documents, 0
    );

    return { total, recoverable, expired, totalDataSize };
  }, [deletedProperties]);

  const handleSelectAll = useCallback(() => {
    if (selectedIds.length === filteredProperties.length) {
      setSelectedIds([]);
    } else {
      setSelectedIds(filteredProperties.map(p => p.id));
    }
  }, [selectedIds, filteredProperties]);

  const handleSelectProperty = useCallback((propertyId: string) => {
    setSelectedIds(prev => 
      prev.includes(propertyId)
        ? prev.filter(id => id !== propertyId)
        : [...prev, propertyId]
    );
  }, []);

  const handleBulkAction = useCallback(async (type: 'restore' | 'permanent_delete') => {
    if (selectedIds.length === 0) return;

    setBulkActionType(type);
    setShowBulkActionDialog(true);
  }, [selectedIds]);

  const executeBulkAction = useCallback(async () => {
    if (!bulkActionType || selectedIds.length === 0) return;

    setBulkActionProgress({
      total: selectedIds.length,
      completed: 0,
      failed: 0,
      currentAction: `${bulkActionType === 'restore' ? '복구' : '영구 삭제'} 중...`,
    });

    try {
      if (bulkActionType === 'restore') {
        await onBulkRestore(selectedIds, bulkActionReason);
      } else {
        await onBulkPermanentDelete(selectedIds, bulkActionReason);
      }

      setBulkActionProgress(prev => prev ? {
        ...prev,
        completed: selectedIds.length,
        currentAction: '완료',
      } : null);

      setTimeout(() => {
        setBulkActionProgress(null);
        setShowBulkActionDialog(false);
        setBulkActionType(null);
        setBulkActionReason('');
        setSelectedIds([]);
      }, 2000);

    } catch (error) {
      setBulkActionProgress(prev => prev ? {
        ...prev,
        failed: selectedIds.length,
        currentAction: '실패',
      } : null);
    }
  }, [bulkActionType, selectedIds, bulkActionReason, onBulkRestore, onBulkPermanentDelete]);

  const handleShowHistory = useCallback((property: DeletedProperty) => {
    setSelectedPropertyHistory(property);
    setShowHistoryDialog(true);
  }, []);

  const getDaysUntilPermanentDeletion = (property: DeletedProperty) => {
    const diffMs = property.scheduledPermanentDeletion.getTime() - Date.now();
    return Math.ceil(diffMs / (24 * 60 * 60 * 1000));
  };

  const getDeleteTypeColor = (type: 'soft' | 'archive') => {
    return type === 'soft' ? 'text-orange-600' : 'text-blue-600';
  };

  const getDeleteTypeBg = (type: 'soft' | 'archive') => {
    return type === 'soft' ? 'bg-orange-100' : 'bg-blue-100';
  };

  const renderStatsCards = () => (
    <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">총 삭제된 매물</p>
              <p className="text-2xl font-bold">{stats.total}</p>
            </div>
            <Trash2 className="w-8 h-8 text-gray-400" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">복구 가능</p>
              <p className="text-2xl font-bold text-green-600">{stats.recoverable}</p>
            </div>
            <RotateCcw className="w-8 h-8 text-green-400" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">만료 예정/완료</p>
              <p className="text-2xl font-bold text-red-600">{stats.expired}</p>
            </div>
            <AlertTriangle className="w-8 h-8 text-red-400" />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm font-medium text-gray-600">관련 파일</p>
              <p className="text-2xl font-bold">{stats.totalDataSize}</p>
            </div>
            <Database className="w-8 h-8 text-blue-400" />
          </div>
        </CardContent>
      </Card>
    </div>
  );

  const renderFilters = () => (
    <Card className="mb-6">
      <CardContent className="pt-6">
        <div className="flex flex-col md:flex-row gap-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
              <Input
                placeholder="매물명, 주소, 삭제자로 검색..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="pl-10"
              />
            </div>
          </div>

          <Select value={filterPeriod} onValueChange={setFilterPeriod}>
            <SelectTrigger className="w-48">
              <SelectValue placeholder="기간 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">전체 기간</SelectItem>
              <SelectItem value="7d">최근 7일</SelectItem>
              <SelectItem value="30d">최근 30일</SelectItem>
              <SelectItem value="90d">최근 90일</SelectItem>
            </SelectContent>
          </Select>

          <Select value={filterType} onValueChange={setFilterType}>
            <SelectTrigger className="w-48">
              <SelectValue placeholder="타입 선택" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">모든 타입</SelectItem>
              <SelectItem value="soft">임시 삭제</SelectItem>
              <SelectItem value="archive">아카이브</SelectItem>
              <SelectItem value="recoverable">복구 가능</SelectItem>
              <SelectItem value="expired">만료됨</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </CardContent>
    </Card>
  );

  const renderBulkActions = () => (
    selectedIds.length > 0 && (
      <Card className="mb-6 border-blue-200 bg-blue-50">
        <CardContent className="pt-6">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Badge variant="secondary">{selectedIds.length}개 선택됨</Badge>
              <span className="text-sm text-gray-600">
                선택된 매물에 대해 일괄 작업을 수행할 수 있습니다.
              </span>
            </div>
            <div className="flex items-center gap-2">
              {permissions.canBulkOperations && permissions.canRestore && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleBulkAction('restore')}
                  className="flex items-center gap-2"
                >
                  <RotateCcw className="w-4 h-4" />
                  일괄 복구
                </Button>
              )}
              {permissions.canBulkOperations && permissions.canPermanentDelete && (
                <Button
                  size="sm"
                  variant="destructive"
                  onClick={() => handleBulkAction('permanent_delete')}
                  className="flex items-center gap-2"
                >
                  <Trash2 className="w-4 h-4" />
                  일괄 영구삭제
                </Button>
              )}
              {permissions.canExportData && (
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => onExportData(selectedIds)}
                  className="flex items-center gap-2"
                >
                  <Download className="w-4 h-4" />
                  데이터 내보내기
                </Button>
              )}
              <Button
                size="sm"
                variant="ghost"
                onClick={() => setSelectedIds([])}
              >
                선택 해제
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    )
  );

  const renderPropertyList = () => (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <CardTitle className="flex items-center gap-2">
            <Trash2 className="w-5 h-5" />
            삭제된 매물 목록
          </CardTitle>
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              onClick={handleSelectAll}
              className="flex items-center gap-2"
            >
              {selectedIds.length === filteredProperties.length ? (
                <CheckSquare className="w-4 h-4" />
              ) : (
                <Square className="w-4 h-4" />
              )}
              전체 선택
            </Button>
            {permissions.canPermanentDelete && (
              <Button
                variant="outline"
                size="sm"
                onClick={onCleanupExpired}
                className="flex items-center gap-2"
              >
                <Zap className="w-4 h-4" />
                만료된 항목 정리
              </Button>
            )}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {filteredProperties.length === 0 ? (
          <div className="text-center py-12">
            <Archive className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              삭제된 매물이 없습니다
            </h3>
            <p className="text-gray-500">
              현재 조건에 맞는 삭제된 매물이 없습니다.
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {filteredProperties.map((property) => {
              const daysLeft = getDaysUntilPermanentDeletion(property);
              const isExpired = daysLeft < 0;
              const isSelected = selectedIds.includes(property.id);

              return (
                <div
                  key={property.id}
                  className={`border rounded-lg p-4 transition-all ${
                    isSelected ? 'bg-blue-50 border-blue-300' : 'hover:bg-gray-50'
                  } ${isExpired ? 'border-red-200 bg-red-50' : ''}`}
                >
                  <div className="flex items-start gap-4">
                    <Checkbox
                      checked={isSelected}
                      onCheckedChange={() => handleSelectProperty(property.id)}
                    />

                    {property.images && property.images.length > 0 ? (
                      <img
                        src={property.images[0]}
                        alt={property.title}
                        className="w-20 h-20 rounded-lg object-cover"
                      />
                    ) : (
                      <div className="w-20 h-20 rounded-lg bg-gray-200 flex items-center justify-center">
                        <FileImage className="w-8 h-8 text-gray-400" />
                      </div>
                    )}

                    <div className="flex-1 min-w-0">
                      <div className="flex items-start justify-between mb-2">
                        <div>
                          <h4 className="font-semibold text-lg truncate">{property.title}</h4>
                          <p className="text-gray-600 text-sm">{property.address}</p>
                        </div>
                        <div className="flex items-center gap-2">
                          <Badge variant="outline" className={getDeleteTypeBg(property.deleteType)}>
                            <Archive className={`w-3 h-3 mr-1 ${getDeleteTypeColor(property.deleteType)}`} />
                            {property.deleteType === 'soft' ? '임시삭제' : '아카이브'}
                          </Badge>
                          {isExpired ? (
                            <Badge variant="destructive">만료됨</Badge>
                          ) : (
                            <Badge variant="outline">
                              <Timer className="w-3 h-3 mr-1" />
                              {daysLeft}일 남음
                            </Badge>
                          )}
                        </div>
                      </div>

                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-3 text-sm">
                        <div className="flex items-center gap-1">
                          <Eye className="w-4 h-4 text-gray-400" />
                          <span>조회 {property.views}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <MessageSquare className="w-4 h-4 text-gray-400" />
                          <span>문의 {property.inquiries}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Heart className="w-4 h-4 text-gray-400" />
                          <span>관심 {property.favorites}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <FileImage className="w-4 h-4 text-gray-400" />
                          <span>파일 {property.relatedDataCount.images + property.relatedDataCount.documents}</span>
                        </div>
                      </div>

                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-4 text-sm text-gray-500">
                          <div className="flex items-center gap-1">
                            <User className="w-4 h-4" />
                            <span>{property.deletedByName}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Clock className="w-4 h-4" />
                            <span>{property.deletedAt.toLocaleDateString('ko-KR')}</span>
                          </div>
                          {property.deleteReason && (
                            <div className="flex items-center gap-1">
                              <Info className="w-4 h-4" />
                              <span className="truncate max-w-40">{property.deleteReason}</span>
                            </div>
                          )}
                        </div>

                        <div className="flex items-center gap-2">
                          {permissions.canViewHistory && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleShowHistory(property)}
                              className="flex items-center gap-1"
                            >
                              <Activity className="w-4 h-4" />
                              히스토리
                            </Button>
                          )}
                          {permissions.canRestore && property.canRestore && (
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => onRestoreProperty(property.id)}
                              className="flex items-center gap-1"
                            >
                              <RotateCcw className="w-4 h-4" />
                              복구
                            </Button>
                          )}
                          {permissions.canPermanentDelete && (
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => onPermanentDelete(property.id)}
                              className="flex items-center gap-1"
                            >
                              <Trash2 className="w-4 h-4" />
                              영구삭제
                            </Button>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </CardContent>
    </Card>
  );

  return (
    <div className="space-y-6">
      {/* Cleanup Alert */}
      {stats.expired > 0 && (
        <Alert className="border-red-200 bg-red-50">
          <AlertTriangle className="w-4 h-4 text-red-600" />
          <AlertDescription className="text-red-800">
            <strong>{stats.expired}개의 매물</strong>이 영구 삭제 예정일을 초과했습니다. 
            이들 매물은 시스템에서 완전히 제거될 수 있습니다.
            {permissions.canPermanentDelete && (
              <Button
                variant="outline"
                size="sm"
                onClick={onCleanupExpired}
                className="ml-4"
              >
                지금 정리하기
              </Button>
            )}
          </AlertDescription>
        </Alert>
      )}

      {renderStatsCards()}
      {renderFilters()}
      {renderBulkActions()}
      {renderPropertyList()}

      {/* Bulk Action Dialog */}
      <Dialog open={showBulkActionDialog} onOpenChange={setShowBulkActionDialog}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>
              일괄 {bulkActionType === 'restore' ? '복구' : '영구 삭제'}
            </DialogTitle>
          </DialogHeader>

          {bulkActionProgress ? (
            <div className="space-y-4">
              <div className="text-center">
                <p className="text-lg font-medium">{bulkActionProgress.currentAction}</p>
                <Progress 
                  value={(bulkActionProgress.completed / bulkActionProgress.total) * 100} 
                  className="mt-2" 
                />
                <p className="text-sm text-gray-600 mt-2">
                  {bulkActionProgress.completed} / {bulkActionProgress.total} 완료
                  {bulkActionProgress.failed > 0 && ` (${bulkActionProgress.failed}개 실패)`}
                </p>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <p className="text-sm text-gray-600">
                선택된 {selectedIds.length}개의 매물을 {bulkActionType === 'restore' ? '복구' : '영구 삭제'}하시겠습니까?
              </p>

              <div>
                <Label htmlFor="bulkReason">사유 (선택사항)</Label>
                <Textarea
                  id="bulkReason"
                  value={bulkActionReason}
                  onChange={(e) => setBulkActionReason(e.target.value)}
                  placeholder="일괄 작업 사유를 입력하세요..."
                  className="mt-1"
                />
              </div>

              <div className="flex justify-end gap-2">
                <Button variant="outline" onClick={() => setShowBulkActionDialog(false)}>
                  취소
                </Button>
                <Button
                  variant={bulkActionType === 'restore' ? 'default' : 'destructive'}
                  onClick={executeBulkAction}
                >
                  {bulkActionType === 'restore' ? '복구' : '영구 삭제'}
                </Button>
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>

      {/* History Dialog */}
      <Dialog open={showHistoryDialog} onOpenChange={setShowHistoryDialog}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>삭제 히스토리</DialogTitle>
          </DialogHeader>

          {selectedPropertyHistory && (
            <div className="space-y-4">
              <div className="p-4 bg-gray-50 rounded-lg">
                <h4 className="font-medium">{selectedPropertyHistory.title}</h4>
                <p className="text-sm text-gray-600">{selectedPropertyHistory.address}</p>
              </div>

              <div className="space-y-3">
                {selectedPropertyHistory.deletionHistory.map((entry) => (
                  <div key={entry.id} className="flex items-start gap-3 p-3 border rounded-lg">
                    <div className="p-2 rounded-full bg-blue-100">
                      <Activity className="w-4 h-4 text-blue-600" />
                    </div>
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <span className="font-medium">
                          {entry.action === 'deleted' && '삭제됨'}
                          {entry.action === 'restored' && '복구됨'}
                          {entry.action === 'archived' && '아카이브됨'}
                        </span>
                        <span className="text-sm text-gray-500">
                          {entry.timestamp.toLocaleString('ko-KR')}
                        </span>
                      </div>
                      <div className="text-sm text-gray-600">
                        <p>작업자: {entry.userName}</p>
                        {entry.reason && <p>사유: {entry.reason}</p>}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
}