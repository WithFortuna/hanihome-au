'use client';

import React, { useState, useCallback } from 'react';
import { Dialog, DialogContent, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Badge } from '@/components/ui/badge';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Switch } from '@/components/ui/switch';
import { Separator } from '@/components/ui/separator';
import { 
  Trash2, 
  AlertTriangle, 
  Archive, 
  HardDrive,
  Clock,
  Database,
  FileImage,
  MessageSquare,
  Heart,
  Eye,
  Users,
  X,
  RotateCcw,
  Shield
} from 'lucide-react';
import { PropertyWithStats } from './property-dashboard';

interface PropertyDeleteModalProps {
  property: PropertyWithStats;
  isOpen: boolean;
  onClose: () => void;
  onSoftDelete: (property: PropertyWithStats, reason?: string) => void;
  onHardDelete: (property: PropertyWithStats, reason?: string) => void;
  onArchive: (property: PropertyWithStats, reason?: string) => void;
  permissions?: {
    canSoftDelete: boolean;
    canHardDelete: boolean;
    canArchive: boolean;
    requireReason: boolean;
  };
}

interface DeleteOption {
  id: 'soft' | 'hard' | 'archive';
  title: string;
  description: string;
  icon: React.ComponentType<{ className?: string }>;
  color: string;
  bgColor: string;
  risks: string[];
  recoverable: boolean;
  dataImpact: DataImpact[];
}

interface DataImpact {
  type: string;
  action: 'preserved' | 'deleted' | 'anonymized';
  description: string;
  icon: React.ComponentType<{ className?: string }>;
}

export function EnhancedPropertyDeleteModal({
  property,
  isOpen,
  onClose,
  onSoftDelete,
  onHardDelete,
  onArchive,
  permissions = {
    canSoftDelete: true,
    canHardDelete: true,
    canArchive: true,
    requireReason: true,
  },
}: PropertyDeleteModalProps) {
  const [selectedOption, setSelectedOption] = useState<'soft' | 'hard' | 'archive' | null>(null);
  const [confirmText, setConfirmText] = useState('');
  const [deleteReason, setDeleteReason] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [cleanupImages, setCleanupImages] = useState(true);
  const [notifyUsers, setNotifyUsers] = useState(true);
  const [scheduleDate, setScheduleDate] = useState('');

  const deleteOptions: DeleteOption[] = [
    {
      id: 'soft',
      title: '임시 삭제 (소프트 삭제)',
      description: '매물을 비활성화하고 30일간 복구 가능한 상태로 보관합니다.',
      icon: Archive,
      color: 'text-orange-600',
      bgColor: 'bg-orange-100',
      recoverable: true,
      risks: [
        '검색 결과에서 즉시 숨겨집니다',
        '사용자가 접근할 수 없게 됩니다',
        '30일 후 자동으로 완전 삭제됩니다',
      ],
      dataImpact: [
        { type: '매물 정보', action: 'preserved', description: '모든 데이터 보존', icon: Database },
        { type: '이미지', action: 'preserved', description: '원본 파일 보존', icon: FileImage },
        { type: '문의 내역', action: 'preserved', description: '문의 기록 보존', icon: MessageSquare },
        { type: '즐겨찾기', action: 'preserved', description: '사용자 즐겨찾기 보존', icon: Heart },
      ],
    },
    {
      id: 'archive',
      title: '아카이브',
      description: '매물을 아카이브하여 장기 보관합니다. 언제든 복구 가능합니다.',
      icon: Archive,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
      recoverable: true,
      risks: [
        '검색 결과에서 숨겨집니다',
        '읽기 전용 상태로 변경됩니다',
        '관리자만 접근 가능합니다',
      ],
      dataImpact: [
        { type: '매물 정보', action: 'preserved', description: '모든 데이터 보존', icon: Database },
        { type: '이미지', action: 'preserved', description: '압축하여 보관', icon: FileImage },
        { type: '문의 내역', action: 'preserved', description: '읽기 전용으로 보존', icon: MessageSquare },
        { type: '즐겨찾기', action: 'preserved', description: '사용자 목록에서 제거', icon: Heart },
      ],
    },
    {
      id: 'hard',
      title: '완전 삭제 (하드 삭제)',
      description: '매물과 관련된 모든 데이터를 영구적으로 삭제합니다. 복구 불가능합니다.',
      icon: Trash2,
      color: 'text-red-600',
      bgColor: 'bg-red-100',
      recoverable: false,
      risks: [
        '모든 데이터가 영구적으로 삭제됩니다',
        '복구가 절대 불가능합니다',
        '관련된 모든 기록이 사라집니다',
        '법적 요구사항 확인이 필요할 수 있습니다',
      ],
      dataImpact: [
        { type: '매물 정보', action: 'deleted', description: '모든 데이터 영구 삭제', icon: Database },
        { type: '이미지', action: 'deleted', description: '원본 파일 완전 삭제', icon: FileImage },
        { type: '문의 내역', action: 'anonymized', description: '개인정보 익명화 후 통계용 보존', icon: MessageSquare },
        { type: '즐겨찾기', action: 'deleted', description: '모든 즐겨찾기 기록 삭제', icon: Heart },
      ],
    },
  ];

  const selectedOptionData = selectedOption ? deleteOptions.find(opt => opt.id === selectedOption) : null;
  const expectedConfirmText = property.title;
  const isConfirmValid = confirmText === expectedConfirmText;
  const canProceed = selectedOption && isConfirmValid && (!permissions.requireReason || deleteReason.trim());

  const handleConfirm = useCallback(async () => {
    if (!canProceed || !selectedOptionData) return;

    setIsLoading(true);
    try {
      // Simulate API call delay
      await new Promise(resolve => setTimeout(resolve, 2000));

      switch (selectedOption) {
        case 'soft':
          await onSoftDelete(property, deleteReason);
          break;
        case 'hard':
          await onHardDelete(property, deleteReason);
          break;
        case 'archive':
          await onArchive(property, deleteReason);
          break;
      }

      onClose();
    } catch (error) {
      console.error('Delete operation failed:', error);
    } finally {
      setIsLoading(false);
    }
  }, [canProceed, selectedOptionData, selectedOption, property, deleteReason, onSoftDelete, onHardDelete, onArchive, onClose]);

  const handleClose = useCallback(() => {
    if (!isLoading) {
      setSelectedOption(null);
      setConfirmText('');
      setDeleteReason('');
      setCleanupImages(true);
      setNotifyUsers(true);
      setScheduleDate('');
      onClose();
    }
  }, [isLoading, onClose]);

  const getActionIcon = (action: DataImpact['action']) => {
    switch (action) {
      case 'preserved': return <Shield className="w-4 h-4 text-green-600" />;
      case 'deleted': return <X className="w-4 h-4 text-red-600" />;
      case 'anonymized': return <Eye className="w-4 h-4 text-yellow-600" />;
    }
  };

  const getActionColor = (action: DataImpact['action']) => {
    switch (action) {
      case 'preserved': return 'text-green-600';
      case 'deleted': return 'text-red-600';
      case 'anonymized': return 'text-yellow-600';
    }
  };

  const getActionLabel = (action: DataImpact['action']) => {
    switch (action) {
      case 'preserved': return '보존';
      case 'deleted': return '삭제';
      case 'anonymized': return '익명화';
    }
  };

  return (
    <Dialog open={isOpen} onOpenChange={handleClose}>
      <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Trash2 className="w-5 h-5 text-red-600" />
            매물 삭제 관리
          </DialogTitle>
        </DialogHeader>

        <div className="flex-1 overflow-y-auto space-y-6">
          {/* Property Information */}
          <Card>
            <CardHeader>
              <CardTitle className="text-base">삭제 대상 매물</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex items-start gap-4">
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
                <div className="flex-1">
                  <h3 className="font-semibold text-lg">{property.title}</h3>
                  <p className="text-gray-600 text-sm mb-2">{property.address}</p>
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <div className="flex items-center gap-1">
                      <Eye className="w-4 h-4" />
                      <span>조회 {property.views}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <MessageSquare className="w-4 h-4" />
                      <span>문의 {property.inquiries}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Heart className="w-4 h-4" />
                      <span>관심 {property.favorites}</span>
                    </div>
                  </div>
                </div>
                <Badge variant={property.status === 'ACTIVE' ? 'default' : 'secondary'}>
                  {property.status}
                </Badge>
              </div>
            </CardContent>
          </Card>

          {/* Delete Options */}
          <div className="space-y-4">
            <h3 className="text-lg font-semibold">삭제 방식 선택</h3>
            
            <div className="grid gap-4">
              {deleteOptions.map((option) => {
                const Icon = option.icon;
                const isPermitted = 
                  (option.id === 'soft' && permissions.canSoftDelete) ||
                  (option.id === 'hard' && permissions.canHardDelete) ||
                  (option.id === 'archive' && permissions.canArchive);

                if (!isPermitted) return null;

                return (
                  <Card 
                    key={option.id}
                    className={`cursor-pointer transition-all ${
                      selectedOption === option.id 
                        ? 'ring-2 ring-blue-500 bg-blue-50' 
                        : 'hover:bg-gray-50'
                    } ${!isPermitted ? 'opacity-50 cursor-not-allowed' : ''}`}
                    onClick={() => isPermitted && setSelectedOption(option.id)}
                  >
                    <CardContent className="pt-4">
                      <div className="flex items-start gap-4">
                        <div className={`p-3 rounded-full ${option.bgColor}`}>
                          <Icon className={`w-6 h-6 ${option.color}`} />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <h4 className="font-semibold">{option.title}</h4>
                            {option.recoverable && (
                              <Badge variant="outline" className="text-xs">
                                <RotateCcw className="w-3 h-3 mr-1" />
                                복구 가능
                              </Badge>
                            )}
                          </div>
                          <p className="text-sm text-gray-600 mb-3">{option.description}</p>
                          
                          <div className="space-y-2">
                            <h5 className="text-sm font-medium">주요 영향:</h5>
                            <ul className="text-sm text-gray-600 space-y-1">
                              {option.risks.map((risk, index) => (
                                <li key={index} className="flex items-start gap-2">
                                  <AlertTriangle className="w-3 h-3 mt-0.5 text-yellow-500 flex-shrink-0" />
                                  <span>{risk}</span>
                                </li>
                              ))}
                            </ul>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          </div>

          {/* Data Impact Details */}
          {selectedOptionData && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Database className="w-5 h-5" />
                  데이터 영향 분석
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4">
                  {selectedOptionData.dataImpact.map((impact, index) => {
                    const ImpactIcon = impact.icon;
                    return (
                      <div key={index} className="flex items-center justify-between p-3 border rounded-lg">
                        <div className="flex items-center gap-3">
                          <ImpactIcon className="w-5 h-5 text-gray-600" />
                          <div>
                            <span className="font-medium">{impact.type}</span>
                            <p className="text-sm text-gray-600">{impact.description}</p>
                          </div>
                        </div>
                        <div className="flex items-center gap-2">
                          {getActionIcon(impact.action)}
                          <span className={`text-sm font-medium ${getActionColor(impact.action)}`}>
                            {getActionLabel(impact.action)}
                          </span>
                        </div>
                      </div>
                    );
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Additional Options */}
          {selectedOption && (
            <Card>
              <CardHeader>
                <CardTitle>추가 옵션</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4">
                {selectedOption === 'hard' && (
                  <div className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <FileImage className="w-4 h-4" />
                      <span className="text-sm">관련 이미지 파일 정리</span>
                    </div>
                    <Switch
                      checked={cleanupImages}
                      onCheckedChange={setCleanupImages}
                    />
                  </div>
                )}

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Users className="w-4 h-4" />
                    <span className="text-sm">관련 사용자에게 알림</span>
                  </div>
                  <Switch
                    checked={notifyUsers}
                    onCheckedChange={setNotifyUsers}
                  />
                </div>

                {selectedOption === 'soft' && (
                  <div>
                    <Label htmlFor="scheduleDate">예약 삭제 (선택사항)</Label>
                    <Input
                      id="scheduleDate"
                      type="datetime-local"
                      value={scheduleDate}
                      onChange={(e) => setScheduleDate(e.target.value)}
                      className="mt-1"
                    />
                    <p className="text-xs text-gray-500 mt-1">
                      지정하지 않으면 즉시 실행됩니다.
                    </p>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Reason Input */}
          {selectedOption && (
            <div className="space-y-4">
              <div>
                <Label htmlFor="reason">
                  삭제 사유 {permissions.requireReason && <span className="text-red-500">*</span>}
                </Label>
                <Textarea
                  id="reason"
                  value={deleteReason}
                  onChange={(e) => setDeleteReason(e.target.value)}
                  placeholder="삭제 사유를 입력하세요..."
                  className="mt-1"
                  required={permissions.requireReason}
                />
              </div>

              <div>
                <Label htmlFor="confirmText">
                  확인을 위해 매물명을 정확히 입력하세요: <code className="bg-gray-100 px-1 py-0.5 rounded">{expectedConfirmText}</code>
                </Label>
                <Input
                  id="confirmText"
                  value={confirmText}
                  onChange={(e) => setConfirmText(e.target.value)}
                  placeholder="매물명을 입력하세요"
                  className={`mt-1 ${
                    confirmText && !isConfirmValid
                      ? 'border-red-300 focus:border-red-500 focus:ring-red-500'
                      : confirmText && isConfirmValid
                      ? 'border-green-300 focus:border-green-500 focus:ring-green-500'
                      : ''
                  }`}
                />
                {confirmText && !isConfirmValid && (
                  <p className="text-sm text-red-600 mt-1">매물명이 일치하지 않습니다.</p>
                )}
                {isConfirmValid && (
                  <p className="text-sm text-green-600 mt-1">✓ 매물명이 확인되었습니다.</p>
                )}
              </div>
            </div>
          )}

          {/* Warning Alert */}
          {selectedOption === 'hard' && (
            <Alert className="border-red-200 bg-red-50">
              <AlertTriangle className="w-4 h-4 text-red-600" />
              <AlertDescription className="text-red-800">
                <strong>경고:</strong> 완전 삭제는 되돌릴 수 없습니다. 
                모든 데이터가 영구적으로 삭제되며, 법적 요구사항에 따라 일부 데이터는 
                익명화되어 통계 목적으로만 보관될 수 있습니다.
              </AlertDescription>
            </Alert>
          )}
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between pt-4 border-t">
          <Button variant="outline" onClick={handleClose} disabled={isLoading}>
            취소
          </Button>
          
          <Button
            variant={selectedOption === 'hard' ? 'destructive' : 'default'}
            onClick={handleConfirm}
            disabled={!canProceed || isLoading}
            className="min-w-[120px]"
          >
            {isLoading ? (
              <div className="flex items-center gap-2">
                <Clock className="w-4 h-4 animate-spin" />
                처리 중...
              </div>
            ) : (
              <div className="flex items-center gap-2">
                {selectedOptionData?.icon && <selectedOptionData.icon className="w-4 h-4" />}
                {selectedOption === 'soft' && '임시 삭제'}
                {selectedOption === 'hard' && '완전 삭제'}
                {selectedOption === 'archive' && '아카이브'}
                {!selectedOption && '삭제 방식 선택'}
              </div>
            )}
          </Button>
        </div>
      </DialogContent>
    </Dialog>
  );
}