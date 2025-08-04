'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { 
  Save, 
  X, 
  History, 
  Eye, 
  Settings, 
  Bell,
  Clock,
  Shield,
  CheckCircle,
  AlertTriangle,
  RotateCcw,
  FileEdit,
  Activity,
  Power,
  User
} from 'lucide-react';
import { PropertyWithStats } from './property-dashboard';
import { PropertyFormData, PropertyStatus } from '@/lib/types/property';
import { PropertyRegistrationForm } from './property-registration-form';
import { useForm, FormProvider } from 'react-hook-form';

interface PropertyEditHistory {
  id: string;
  timestamp: Date;
  userId: string;
  userName: string;
  changes: Array<{
    field: string;
    oldValue: any;
    newValue: any;
    label: string;
  }>;
  action: 'updated' | 'status_changed' | 'created';
}

interface PropertyEditModalProps {
  property: PropertyWithStats;
  isOpen: boolean;
  onClose: () => void;
  onSave: (property: PropertyWithStats) => void;
  onStatusChange?: (propertyId: string, status: PropertyStatus, notify?: boolean) => void;
  currentUser?: { id: string; name: string; role: string };
  permissions?: {
    canEdit: boolean;
    canChangeStatus: boolean;
    canViewHistory: boolean;
  };
}

export function EnhancedPropertyEditModal({
  property,
  isOpen,
  onClose,
  onSave,
  onStatusChange,
  currentUser = { id: '1', name: 'Current User', role: 'admin' },
  permissions = { canEdit: true, canChangeStatus: true, canViewHistory: true },
}: PropertyEditModalProps) {
  const [activeTab, setActiveTab] = useState('edit');
  const [isLoading, setIsLoading] = useState(false);
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false);
  const [autoSaveEnabled, setAutoSaveEnabled] = useState(true);
  const [lastSaved, setLastSaved] = useState<Date | null>(null);
  const [statusNotification, setStatusNotification] = useState(true);
  const [originalData, setOriginalData] = useState<PropertyFormData | null>(null);
  const [editHistory, setEditHistory] = useState<PropertyEditHistory[]>([]);
  const autoSaveTimeoutRef = useRef<NodeJS.Timeout>();

  // Form setup
  const methods = useForm<PropertyFormData>({
    defaultValues: {
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
      parkingAvailable: property.parkingAvailable,
      petAllowed: property.petAllowed,
      furnished: property.furnished,
      shortTermAvailable: property.shortTermAvailable,
    },
  });

  const { watch, getValues, reset } = methods;
  const watchedValues = watch();

  // Initialize data
  useEffect(() => {
    if (isOpen && originalData === null) {
      setOriginalData(getValues());
      loadEditHistory();
    }
  }, [isOpen, originalData, getValues]);

  // Track changes for auto-save
  useEffect(() => {
    if (originalData && isOpen) {
      const currentValues = getValues();
      const hasChanges = JSON.stringify(currentValues) !== JSON.stringify(originalData);
      setHasUnsavedChanges(hasChanges);

      if (hasChanges && autoSaveEnabled) {
        // Clear existing timeout
        if (autoSaveTimeoutRef.current) {
          clearTimeout(autoSaveTimeoutRef.current);
        }

        // Set new timeout for auto-save
        autoSaveTimeoutRef.current = setTimeout(() => {
          handleAutoSave();
        }, 3000); // Auto-save after 3 seconds of inactivity
      }
    }

    return () => {
      if (autoSaveTimeoutRef.current) {
        clearTimeout(autoSaveTimeoutRef.current);
      }
    };
  }, [watchedValues, originalData, autoSaveEnabled, isOpen]);

  const loadEditHistory = useCallback(async () => {
    // Mock edit history data
    const mockHistory: PropertyEditHistory[] = [
      {
        id: '1',
        timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000), // 2 hours ago
        userId: 'user1',
        userName: 'John Kim',
        action: 'updated',
        changes: [
          { field: 'rent', oldValue: 800000, newValue: 850000, label: '월 임대료' },
          { field: 'description', oldValue: '깨끗한 원룸', newValue: '깨끗하고 넓은 원룸', label: '설명' },
        ],
      },
      {
        id: '2',
        timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
        userId: 'user2',
        userName: 'Sarah Lee',
        action: 'status_changed',
        changes: [
          { field: 'status', oldValue: 'DRAFT', newValue: 'ACTIVE', label: '상태' },
        ],
      },
      {
        id: '3',
        timestamp: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
        userId: currentUser.id,
        userName: currentUser.name,
        action: 'created',
        changes: [],
      },
    ];
    setEditHistory(mockHistory);
  }, [currentUser]);

  const handleAutoSave = useCallback(async () => {
    if (!hasUnsavedChanges || !autoSaveEnabled) return;

    try {
      const formData = getValues();
      // Simulate auto-save API call
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setOriginalData(formData);
      setLastSaved(new Date());
      setHasUnsavedChanges(false);
    } catch (error) {
      console.error('Auto-save failed:', error);
    }
  }, [hasUnsavedChanges, autoSaveEnabled, getValues]);

  const handleManualSave = useCallback(async () => {
    setIsLoading(true);
    try {
      const formData = getValues();
      
      // Calculate changes for history
      const changes: Array<{ field: string; oldValue: any; newValue: any; label: string }> = [];
      if (originalData) {
        Object.keys(formData).forEach(key => {
          const typedKey = key as keyof PropertyFormData;
          if (JSON.stringify(formData[typedKey]) !== JSON.stringify(originalData[typedKey])) {
            changes.push({
              field: key,
              oldValue: originalData[typedKey],
              newValue: formData[typedKey],
              label: getFieldLabel(key),
            });
          }
        });
      }

      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000));

      const updatedProperty: PropertyWithStats = {
        ...property,
        ...formData,
        updatedAt: new Date(),
      };

      // Add to history
      if (changes.length > 0) {
        const historyEntry: PropertyEditHistory = {
          id: Date.now().toString(),
          timestamp: new Date(),
          userId: currentUser.id,
          userName: currentUser.name,
          action: 'updated',
          changes,
        };
        setEditHistory(prev => [historyEntry, ...prev]);
      }

      setOriginalData(formData);
      setLastSaved(new Date());
      setHasUnsavedChanges(false);
      onSave(updatedProperty);
    } catch (error) {
      console.error('Failed to save property:', error);
    } finally {
      setIsLoading(false);
    }
  }, [getValues, originalData, property, onSave, currentUser]);

  const handleStatusChange = useCallback(async (newStatus: PropertyStatus) => {
    if (!onStatusChange || !permissions.canChangeStatus) return;

    try {
      await onStatusChange(property.id, newStatus, statusNotification);
      
      // Add to history
      const historyEntry: PropertyEditHistory = {
        id: Date.now().toString(),
        timestamp: new Date(),
        userId: currentUser.id,
        userName: currentUser.name,
        action: 'status_changed',
        changes: [
          { field: 'status', oldValue: property.status, newValue: newStatus, label: '상태' },
        ],
      };
      setEditHistory(prev => [historyEntry, ...prev]);
    } catch (error) {
      console.error('Failed to change status:', error);
    }
  }, [onStatusChange, property.id, property.status, statusNotification, permissions.canChangeStatus, currentUser]);

  const handleClose = useCallback(() => {
    if (hasUnsavedChanges) {
      const confirmClose = window.confirm('저장하지 않은 변경사항이 있습니다. 정말 닫으시겠습니까?');
      if (!confirmClose) return;
    }
    
    // Reset form and state
    reset();
    setOriginalData(null);
    setHasUnsavedChanges(false);
    setActiveTab('edit');
    onClose();
  }, [hasUnsavedChanges, reset, onClose]);

  const handleRevertChanges = useCallback(() => {
    if (originalData) {
      reset(originalData);
      setHasUnsavedChanges(false);
    }
  }, [originalData, reset]);

  const getFieldLabel = (field: string): string => {
    const labels: Record<string, string> = {
      title: '매물명',
      description: '설명',
      address: '주소',
      rent: '월 임대료',
      deposit: '보증금',
      managementFee: '관리비',
      area: '면적',
      rooms: '방 개수',
      bathrooms: '욕실 개수',
      floor: '층수',
      contactName: '연락처 이름',
      contactPhone: '연락처 번호',
      propertyType: '매물 유형',
      rentalType: '임대 유형',
      status: '상태',
    };
    return labels[field] || field;
  };

  const getStatusBadgeVariant = (status: PropertyStatus) => {
    switch (status) {
      case PropertyStatus.ACTIVE: return 'default';
      case PropertyStatus.INACTIVE: return 'secondary';
      case PropertyStatus.DRAFT: return 'outline';
      case PropertyStatus.RENTED: return 'destructive';
      default: return 'outline';
    }
  };

  const getStatusColor = (status: PropertyStatus) => {
    switch (status) {
      case PropertyStatus.ACTIVE: return 'text-green-600';
      case PropertyStatus.INACTIVE: return 'text-gray-600';
      case PropertyStatus.DRAFT: return 'text-yellow-600';
      case PropertyStatus.RENTED: return 'text-red-600';
      default: return 'text-gray-600';
    }
  };

  const renderStatusTab = () => (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5" />
            매물 상태 관리
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h4 className="font-medium">현재 상태</h4>
              <p className="text-sm text-gray-600">매물의 활성화 상태를 관리합니다</p>
            </div>
            <Badge variant={getStatusBadgeVariant(property.status)} className="text-sm">
              <Power className={`w-3 h-3 mr-1 ${getStatusColor(property.status)}`} />
              {property.status === PropertyStatus.ACTIVE && '활성'}
              {property.status === PropertyStatus.INACTIVE && '비활성'}
              {property.status === PropertyStatus.DRAFT && '임시저장'}
              {property.status === PropertyStatus.RENTED && '임대완료'}
            </Badge>
          </div>

          {permissions.canChangeStatus && (
            <div className="grid grid-cols-2 gap-3">
              <Button
                variant={property.status === PropertyStatus.ACTIVE ? "default" : "outline"}
                onClick={() => handleStatusChange(PropertyStatus.ACTIVE)}
                disabled={property.status === PropertyStatus.ACTIVE}
                className="flex items-center gap-2"
              >
                <CheckCircle className="w-4 h-4" />
                활성화
              </Button>
              <Button
                variant={property.status === PropertyStatus.INACTIVE ? "default" : "outline"}
                onClick={() => handleStatusChange(PropertyStatus.INACTIVE)}
                disabled={property.status === PropertyStatus.INACTIVE}
                className="flex items-center gap-2"
              >
                <AlertTriangle className="w-4 h-4" />
                비활성화
              </Button>
            </div>
          )}

          <div className="flex items-center justify-between pt-4 border-t">
            <div className="flex items-center gap-2">
              <Bell className="w-4 h-4" />
              <span className="text-sm">상태 변경 알림</span>
            </div>
            <Switch
              checked={statusNotification}
              onCheckedChange={setStatusNotification}
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Shield className="w-5 h-5" />
            권한 정보
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex items-center justify-between">
            <span className="text-sm">매물 수정</span>
            <Badge variant={permissions.canEdit ? "default" : "secondary"}>
              {permissions.canEdit ? '허용' : '제한'}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm">상태 변경</span>
            <Badge variant={permissions.canChangeStatus ? "default" : "secondary"}>
              {permissions.canChangeStatus ? '허용' : '제한'}
            </Badge>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm">히스토리 조회</span>
            <Badge variant={permissions.canViewHistory ? "default" : "secondary"}>
              {permissions.canViewHistory ? '허용' : '제한'}
            </Badge>
          </div>
        </CardContent>
      </Card>
    </div>
  );

  const renderHistoryTab = () => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold">수정 히스토리</h3>
        <Badge variant="outline">{editHistory.length}개 기록</Badge>
      </div>

      {editHistory.length === 0 ? (
        <div className="text-center py-8 text-gray-500">
          <History className="w-12 h-12 mx-auto mb-4 text-gray-300" />
          <p>아직 수정 히스토리가 없습니다.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {editHistory.map((entry) => (
            <Card key={entry.id}>
              <CardContent className="pt-4">
                <div className="flex items-start justify-between mb-3">
                  <div className="flex items-center gap-2">
                    <div className="p-2 rounded-full bg-blue-100">
                      {entry.action === 'created' && <FileEdit className="w-4 h-4 text-blue-600" />}
                      {entry.action === 'updated' && <FileEdit className="w-4 h-4 text-blue-600" />}
                      {entry.action === 'status_changed' && <Activity className="w-4 h-4 text-blue-600" />}
                    </div>
                    <div>
                      <p className="font-medium text-sm">
                        {entry.action === 'created' && '매물 생성'}
                        {entry.action === 'updated' && '매물 정보 수정'}
                        {entry.action === 'status_changed' && '상태 변경'}
                      </p>
                      <div className="flex items-center gap-2 text-xs text-gray-500">
                        <User className="w-3 h-3" />
                        <span>{entry.userName}</span>
                        <Clock className="w-3 h-3 ml-2" />
                        <span>{entry.timestamp.toLocaleString('ko-KR')}</span>
                      </div>
                    </div>
                  </div>
                </div>

                {entry.changes.length > 0 && (
                  <div className="space-y-2">
                    {entry.changes.map((change, index) => (
                      <div key={index} className="bg-gray-50 p-3 rounded-lg">
                        <div className="flex items-center justify-between mb-1">
                          <span className="text-sm font-medium">{change.label}</span>
                        </div>
                        <div className="flex items-center gap-2 text-sm">
                          <span className="text-red-600 line-through">
                            {typeof change.oldValue === 'number' ? 
                              change.oldValue.toLocaleString() : 
                              String(change.oldValue || '없음')
                            }
                          </span>
                          <span>→</span>
                          <span className="text-green-600 font-medium">
                            {typeof change.newValue === 'number' ? 
                              change.newValue.toLocaleString() : 
                              String(change.newValue || '없음')
                            }
                          </span>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  );

  const renderPreviewTab = () => (
    <div className="space-y-4">
      <Alert>
        <Eye className="w-4 h-4" />
        <AlertDescription>
          이 화면은 저장 후 실제로 표시될 매물 정보의 미리보기입니다.
        </AlertDescription>
      </Alert>

      <Card>
        <CardContent className="pt-6">
          <div className="space-y-4">
            <div>
              <h2 className="text-xl font-bold">{watchedValues.title}</h2>
              <p className="text-gray-600 mt-2">{watchedValues.description}</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <span className="text-sm text-gray-500">월 임대료</span>
                <p className="text-lg font-semibold text-blue-600">
                  {watchedValues.rent?.toLocaleString()}원
                </p>
              </div>
              <div>
                <span className="text-sm text-gray-500">보증금</span>
                <p className="text-lg font-semibold">
                  {watchedValues.deposit?.toLocaleString()}원
                </p>
              </div>
            </div>

            <div>
              <span className="text-sm text-gray-500">주소</span>
              <p className="font-medium">{watchedValues.address}</p>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div>
                <span className="text-sm text-gray-500">면적</span>
                <p className="font-medium">{watchedValues.area}㎡</p>
              </div>
              <div>
                <span className="text-sm text-gray-500">방</span>
                <p className="font-medium">{watchedValues.rooms}개</p>
              </div>
              <div>
                <span className="text-sm text-gray-500">욕실</span>
                <p className="font-medium">{watchedValues.bathrooms}개</p>
              </div>
            </div>

            {watchedValues.options && watchedValues.options.length > 0 && (
              <div>
                <span className="text-sm text-gray-500 block mb-2">옵션</span>
                <div className="flex flex-wrap gap-2">
                  {watchedValues.options.map((option, index) => (
                    <Badge key={index} variant="secondary">{option}</Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
    </div>
  );

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-5xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center justify-between">
            <span>매물 관리</span>
            <div className="flex items-center gap-2">
              {hasUnsavedChanges && (
                <Badge variant="outline" className="text-orange-600">
                  <Clock className="w-3 h-3 mr-1" />
                  저장되지 않음
                </Badge>
              )}
              {lastSaved && (
                <span className="text-xs text-gray-500">
                  마지막 저장: {lastSaved.toLocaleTimeString()}
                </span>
              )}
            </div>
          </DialogTitle>
        </DialogHeader>

        <Tabs value={activeTab} onValueChange={setActiveTab} className="flex-1 overflow-hidden">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="edit" className="flex items-center gap-2">
              <FileEdit className="w-4 h-4" />
              편집
            </TabsTrigger>
            <TabsTrigger value="status" className="flex items-center gap-2">
              <Settings className="w-4 h-4" />
              상태
            </TabsTrigger>
            <TabsTrigger value="preview" className="flex items-center gap-2">
              <Eye className="w-4 h-4" />
              미리보기
            </TabsTrigger>
            <TabsTrigger value="history" className="flex items-center gap-2" disabled={!permissions.canViewHistory}>
              <History className="w-4 h-4" />
              히스토리
            </TabsTrigger>
          </TabsList>

          <div className="flex-1 overflow-y-auto mt-4">
            <TabsContent value="edit" className="mt-0">
              {permissions.canEdit ? (
                <FormProvider {...methods}>
                  <PropertyRegistrationForm />
                </FormProvider>
              ) : (
                <Alert>
                  <Shield className="w-4 h-4" />
                  <AlertDescription>
                    이 매물을 편집할 권한이 없습니다.
                  </AlertDescription>
                </Alert>
              )}
            </TabsContent>

            <TabsContent value="status" className="mt-0">
              {renderStatusTab()}
            </TabsContent>

            <TabsContent value="preview" className="mt-0">
              {renderPreviewTab()}
            </TabsContent>

            <TabsContent value="history" className="mt-0">
              {permissions.canViewHistory ? renderHistoryTab() : (
                <Alert>
                  <Shield className="w-4 h-4" />
                  <AlertDescription>
                    히스토리를 조회할 권한이 없습니다.
                  </AlertDescription>
                </Alert>
              )}
            </TabsContent>
          </div>
        </Tabs>

        <div className="flex items-center justify-between pt-4 border-t">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <span className="text-sm text-gray-600">자동 저장</span>
              <Switch
                checked={autoSaveEnabled}
                onCheckedChange={setAutoSaveEnabled}
              />
            </div>
            {hasUnsavedChanges && (
              <Button
                variant="outline"
                size="sm"
                onClick={handleRevertChanges}
                className="flex items-center gap-2"
              >
                <RotateCcw className="w-4 h-4" />
                변경사항 되돌리기
              </Button>
            )}
          </div>

          <div className="flex items-center gap-2">
            <Button variant="outline" onClick={handleClose}>
              <X className="w-4 h-4 mr-2" />
              닫기
            </Button>
            {permissions.canEdit && (
              <Button 
                onClick={handleManualSave}
                disabled={isLoading || !hasUnsavedChanges}
                className="flex items-center gap-2"
              >
                <Save className="w-4 h-4" />
                {isLoading ? '저장 중...' : '저장'}
              </Button>
            )}
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}