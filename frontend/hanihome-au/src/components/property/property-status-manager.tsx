'use client';

import React, { useState, useCallback } from 'react';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Switch } from '@/components/ui/switch';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Textarea } from '@/components/ui/textarea';
import { Label } from '@/components/ui/label';
import { 
  Power, 
  Bell, 
  CheckCircle, 
  AlertTriangle, 
  Clock, 
  FileX,
  Eye,
  EyeOff,
  Users,
  Mail,
  MessageSquare,
  Calendar,
  Activity
} from 'lucide-react';
import { PropertyStatus } from '@/lib/types/property';
import { PropertyWithStats } from './property-dashboard';

interface PropertyStatusManagerProps {
  property: PropertyWithStats;
  onStatusChange: (propertyId: string, status: PropertyStatus, notify?: boolean, reason?: string) => void;
  permissions?: {
    canChangeStatus: boolean;
    canSetScheduledStatus: boolean;
    canViewStatusHistory: boolean;
  };
}

interface StatusChangeHistory {
  id: string;
  timestamp: Date;
  oldStatus: PropertyStatus;
  newStatus: PropertyStatus;
  reason?: string;
  userId: string;
  userName: string;
  notificationSent: boolean;
}

interface ScheduledStatusChange {
  id: string;
  targetStatus: PropertyStatus;
  scheduledDate: Date;
  reason?: string;
  isActive: boolean;
}

export function PropertyStatusManager({
  property,
  onStatusChange,
  permissions = { 
    canChangeStatus: true, 
    canSetScheduledStatus: true, 
    canViewStatusHistory: true 
  },
}: PropertyStatusManagerProps) {
  const [isChangeDialogOpen, setIsChangeDialogOpen] = useState(false);
  const [selectedStatus, setSelectedStatus] = useState<PropertyStatus | null>(null);
  const [changeReason, setChangeReason] = useState('');
  const [notifyUsers, setNotifyUsers] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  
  // Mock data - in real app, fetch from API
  const [statusHistory] = useState<StatusChangeHistory[]>([
    {
      id: '1',
      timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
      oldStatus: PropertyStatus.DRAFT,
      newStatus: PropertyStatus.ACTIVE,
      reason: '모든 정보 입력 완료',
      userId: 'user1',
      userName: 'John Kim',
      notificationSent: true,
    },
    {
      id: '2',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000),
      oldStatus: PropertyStatus.ACTIVE,
      newStatus: PropertyStatus.INACTIVE,
      reason: '임시 비활성화 - 정보 수정 필요',
      userId: 'user2',
      userName: 'Sarah Lee',
      notificationSent: false,
    },
  ]);

  const [scheduledChanges] = useState<ScheduledStatusChange[]>([
    {
      id: '1',
      targetStatus: PropertyStatus.INACTIVE,
      scheduledDate: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000), // 7 days from now
      reason: '계약 만료 예정',
      isActive: true,
    },
  ]);

  const statusConfig = {
    [PropertyStatus.ACTIVE]: {
      label: '활성',
      icon: CheckCircle,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
      description: '매물이 공개되어 검색 결과에 표시됩니다.',
    },
    [PropertyStatus.INACTIVE]: {
      label: '비활성',
      icon: EyeOff,
      color: 'text-gray-600',
      bgColor: 'bg-gray-100',
      description: '매물이 숨겨져 검색 결과에 표시되지 않습니다.',
    },
    [PropertyStatus.DRAFT]: {
      label: '임시저장',
      icon: Clock,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-100',
      description: '매물 정보가 완전하지 않아 공개되지 않습니다.',
    },
    [PropertyStatus.RENTED]: {
      label: '임대완료',
      icon: FileX,
      color: 'text-red-600',
      bgColor: 'bg-red-100',
      description: '임대가 완료되어 더 이상 공개되지 않습니다.',
    },
  };

  const getCurrentStatusConfig = () => statusConfig[property.status];

  const handleStatusChange = useCallback(async (newStatus: PropertyStatus) => {
    if (!permissions.canChangeStatus) return;

    setIsLoading(true);
    try {
      await onStatusChange(property.id, newStatus, notifyUsers, changeReason);
      setIsChangeDialogOpen(false);
      setChangeReason('');
      setSelectedStatus(null);
    } catch (error) {
      console.error('Failed to change status:', error);
    } finally {
      setIsLoading(false);
    }
  }, [property.id, onStatusChange, notifyUsers, changeReason, permissions.canChangeStatus]);

  const getStatusBadgeVariant = (status: PropertyStatus) => {
    switch (status) {
      case PropertyStatus.ACTIVE: return 'default';
      case PropertyStatus.INACTIVE: return 'secondary';
      case PropertyStatus.DRAFT: return 'outline';
      case PropertyStatus.RENTED: return 'destructive';
      default: return 'outline';
    }
  };

  const renderStatusChangeDialog = () => (
    <Dialog open={isChangeDialogOpen} onOpenChange={setIsChangeDialogOpen}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>상태 변경 확인</DialogTitle>
        </DialogHeader>
        
        <div className="space-y-4">
          {selectedStatus && (
            <div className="space-y-3">
              <div className="p-4 border rounded-lg bg-gray-50">
                <div className="flex items-center gap-3 mb-2">
                  <div className="flex items-center gap-2">
                    <div className={`p-2 rounded-full ${getCurrentStatusConfig().bgColor}`}>
                      <getCurrentStatusConfig().icon className={`w-4 h-4 ${getCurrentStatusConfig().color}`} />
                    </div>
                    <span className="font-medium">{getCurrentStatusConfig().label}</span>
                  </div>
                  <span>→</span>
                  <div className="flex items-center gap-2">
                    <div className={`p-2 rounded-full ${statusConfig[selectedStatus].bgColor}`}>
                      <statusConfig[selectedStatus].icon className={`w-4 h-4 ${statusConfig[selectedStatus].color}`} />
                    </div>
                    <span className="font-medium">{statusConfig[selectedStatus].label}</span>
                  </div>
                </div>
                <p className="text-sm text-gray-600">
                  {statusConfig[selectedStatus].description}
                </p>
              </div>

              <div>
                <Label htmlFor="reason">변경 사유 (선택사항)</Label>
                <Textarea
                  id="reason"
                  value={changeReason}
                  onChange={(e) => setChangeReason(e.target.value)}
                  placeholder="상태 변경 사유를 입력하세요..."
                  className="mt-1"
                />
              </div>

              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Bell className="w-4 h-4" />
                  <span className="text-sm">관련 사용자에게 알림 발송</span>
                </div>
                <Switch
                  checked={notifyUsers}
                  onCheckedChange={setNotifyUsers}
                />
              </div>
            </div>
          )}

          <div className="flex justify-end gap-2">
            <Button 
              variant="outline" 
              onClick={() => setIsChangeDialogOpen(false)}
              disabled={isLoading}
            >
              취소
            </Button>
            <Button 
              onClick={() => selectedStatus && handleStatusChange(selectedStatus)}
              disabled={isLoading}
            >
              {isLoading ? '변경 중...' : '상태 변경'}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );

  return (
    <div className="space-y-6">
      {/* Current Status */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="w-5 h-5" />
            현재 상태
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <div className={`p-3 rounded-full ${getCurrentStatusConfig().bgColor}`}>
                <getCurrentStatusConfig().icon className={`w-6 h-6 ${getCurrentStatusConfig().color}`} />
              </div>
              <div>
                <h3 className="font-semibold">{getCurrentStatusConfig().label}</h3>
                <p className="text-sm text-gray-600">{getCurrentStatusConfig().description}</p>
              </div>
            </div>
            <Badge variant={getStatusBadgeVariant(property.status)}>
              {getCurrentStatusConfig().label}
            </Badge>
          </div>

          {permissions.canChangeStatus && (
            <div className="grid grid-cols-2 gap-3">
              {Object.entries(statusConfig).map(([status, config]) => {
                const statusEnum = status as PropertyStatus;
                const isCurrentStatus = property.status === statusEnum;
                
                return (
                  <Button
                    key={status}
                    variant={isCurrentStatus ? "default" : "outline"}
                    disabled={isCurrentStatus}
                    onClick={() => {
                      setSelectedStatus(statusEnum);
                      setIsChangeDialogOpen(true);
                    }}
                    className="flex items-center gap-2 justify-start"
                  >
                    <config.icon className="w-4 h-4" />
                    {config.label}
                  </Button>
                );
              })}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Scheduled Status Changes */}
      {permissions.canSetScheduledStatus && scheduledChanges.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Calendar className="w-5 h-5" />
              예약된 상태 변경
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {scheduledChanges.map((scheduled) => (
                <div key={scheduled.id} className="flex items-center justify-between p-3 border rounded-lg">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-full ${statusConfig[scheduled.targetStatus].bgColor}`}>
                      <statusConfig[scheduled.targetStatus].icon className={`w-4 h-4 ${statusConfig[scheduled.targetStatus].color}`} />
                    </div>
                    <div>
                      <p className="font-medium">{statusConfig[scheduled.targetStatus].label}로 변경</p>
                      <p className="text-sm text-gray-600">{scheduled.scheduledDate.toLocaleDateString('ko-KR')}</p>
                      {scheduled.reason && (
                        <p className="text-xs text-gray-500">{scheduled.reason}</p>
                      )}
                    </div>
                  </div>
                  <Badge variant={scheduled.isActive ? "default" : "secondary"}>
                    {scheduled.isActive ? '활성' : '비활성'}
                  </Badge>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      {/* Status History */}
      {permissions.canViewStatusHistory && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="w-5 h-5" />
              상태 변경 히스토리
            </CardTitle>
          </CardHeader>
          <CardContent>
            {statusHistory.length === 0 ? (
              <div className="text-center py-8 text-gray-500">
                <Activity className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                <p>상태 변경 히스토리가 없습니다.</p>
              </div>
            ) : (
              <div className="space-y-4">
                {statusHistory.map((entry) => (
                  <div key={entry.id} className="border-l-2 border-blue-200 pl-4 pb-4">
                    <div className="flex items-start justify-between">
                      <div className="space-y-2">
                        <div className="flex items-center gap-2">
                          <div className={`p-1 rounded-full ${statusConfig[entry.oldStatus].bgColor}`}>
                            <statusConfig[entry.oldStatus].icon className={`w-3 h-3 ${statusConfig[entry.oldStatus].color}`} />
                          </div>
                          <span className="text-sm font-medium">{statusConfig[entry.oldStatus].label}</span>
                          <span className="text-sm text-gray-500">→</span>
                          <div className={`p-1 rounded-full ${statusConfig[entry.newStatus].bgColor}`}>
                            <statusConfig[entry.newStatus].icon className={`w-3 h-3 ${statusConfig[entry.newStatus].color}`} />
                          </div>
                          <span className="text-sm font-medium">{statusConfig[entry.newStatus].label}</span>
                        </div>
                        
                        <div className="text-xs text-gray-500">
                          <div className="flex items-center gap-2">
                            <Users className="w-3 h-3" />
                            <span>{entry.userName}</span>
                            <Clock className="w-3 h-3 ml-2" />
                            <span>{entry.timestamp.toLocaleString('ko-KR')}</span>
                          </div>
                        </div>

                        {entry.reason && (
                          <div className="text-sm text-gray-600">
                            <MessageSquare className="w-3 h-3 inline mr-1" />
                            {entry.reason}
                          </div>
                        )}
                      </div>

                      <div className="flex items-center gap-1">
                        {entry.notificationSent ? (
                          <Badge variant="outline" className="text-xs">
                            <Mail className="w-3 h-3 mr-1" />
                            알림 발송됨
                          </Badge>
                        ) : (
                          <Badge variant="secondary" className="text-xs">
                            알림 미발송
                          </Badge>
                        )}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {renderStatusChangeDialog()}
    </div>
  );
}