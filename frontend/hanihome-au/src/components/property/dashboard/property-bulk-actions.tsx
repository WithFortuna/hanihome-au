'use client';

import React, { useState, useCallback } from 'react';
import { 
  CheckSquare, 
  Square, 
  Trash2, 
  ToggleLeft, 
  ToggleRight, 
  Download, 
  Upload,
  Archive,
  AlertTriangle,
  X
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent } from '@/components/ui/card';
import { PropertyWithStats } from '../property-dashboard';
import { PropertyStatus } from '@/lib/types/property';

interface PropertyBulkActionsProps {
  properties: PropertyWithStats[];
  selectedIds: string[];
  onSelectionChange: (selectedIds: string[]) => void;
  onBulkDelete: (propertyIds: string[]) => void;
  onBulkStatusChange: (propertyIds: string[], status: PropertyStatus) => void;
  onBulkExport: (propertyIds: string[]) => void;
  isLoading?: boolean;
}

export default function PropertyBulkActions({
  properties,
  selectedIds,
  onSelectionChange,
  onBulkDelete,
  onBulkStatusChange,
  onBulkExport,
  isLoading = false,
}: PropertyBulkActionsProps) {
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [pendingAction, setPendingAction] = useState<{
    type: 'delete' | 'statusChange';
    status?: PropertyStatus;
  } | null>(null);

  const isAllSelected = properties.length > 0 && selectedIds.length === properties.length;
  const isPartiallySelected = selectedIds.length > 0 && selectedIds.length < properties.length;

  const handleSelectAll = useCallback(() => {
    if (isAllSelected) {
      onSelectionChange([]);
    } else {
      onSelectionChange(properties.map(p => p.id));
    }
  }, [isAllSelected, properties, onSelectionChange]);

  const handleIndividualSelect = useCallback((propertyId: string) => {
    if (selectedIds.includes(propertyId)) {
      onSelectionChange(selectedIds.filter(id => id !== propertyId));
    } else {
      onSelectionChange([...selectedIds, propertyId]);
    }
  }, [selectedIds, onSelectionChange]);

  const handleBulkAction = useCallback((action: 'delete' | 'statusChange', status?: PropertyStatus) => {
    setPendingAction({ type: action, status });
    setShowConfirmDialog(true);
  }, []);

  const confirmAction = useCallback(() => {
    if (!pendingAction || selectedIds.length === 0) return;

    if (pendingAction.type === 'delete') {
      onBulkDelete(selectedIds);
    } else if (pendingAction.type === 'statusChange' && pendingAction.status) {
      onBulkStatusChange(selectedIds, pendingAction.status);
    }

    setShowConfirmDialog(false);
    setPendingAction(null);
    onSelectionChange([]);
  }, [pendingAction, selectedIds, onBulkDelete, onBulkStatusChange, onSelectionChange]);

  const cancelAction = useCallback(() => {
    setShowConfirmDialog(false);
    setPendingAction(null);
  }, []);

  const getSelectedProperties = useCallback(() => {
    return properties.filter(p => selectedIds.includes(p.id));
  }, [properties, selectedIds]);

  const getActionText = () => {
    if (!pendingAction) return '';
    
    const selectedCount = selectedIds.length;
    if (pendingAction.type === 'delete') {
      return `${selectedCount}개의 매물을 삭제`;
    } else if (pendingAction.type === 'statusChange') {
      const statusText = pendingAction.status === PropertyStatus.ACTIVE ? '활성화' : '비활성화';
      return `${selectedCount}개의 매물을 ${statusText}`;
    }
    return '';
  };

  const getStatusCounts = () => {
    const selectedProperties = getSelectedProperties();
    return {
      active: selectedProperties.filter(p => p.status === PropertyStatus.ACTIVE).length,
      inactive: selectedProperties.filter(p => p.status === PropertyStatus.INACTIVE).length,
      pending: selectedProperties.filter(p => p.status === PropertyStatus.PENDING_APPROVAL).length,
    };
  };

  const statusCounts = getStatusCounts();

  if (properties.length === 0) {
    return null;
  }

  return (
    <>
      {/* Selection Bar */}
      <Card className="mb-4">
        <CardContent className="p-4">
          <div className="flex items-center justify-between">
            {/* Select All Checkbox */}
            <div className="flex items-center gap-3">
              <button
                onClick={handleSelectAll}
                className="flex items-center gap-2 text-sm font-medium text-gray-700 hover:text-gray-900"
              >
                {isAllSelected ? (
                  <CheckSquare className="w-5 h-5 text-blue-600" />
                ) : isPartiallySelected ? (
                  <div className="w-5 h-5 border-2 border-blue-600 bg-blue-600 flex items-center justify-center">
                    <div className="w-2 h-0.5 bg-white"></div>
                  </div>
                ) : (
                  <Square className="w-5 h-5 text-gray-400" />
                )}
                <span>
                  {isAllSelected
                    ? '전체 선택 해제'
                    : isPartiallySelected
                    ? `${selectedIds.length}개 선택됨`
                    : `전체 선택 (${properties.length}개)`
                  }
                </span>
              </button>

              {selectedIds.length > 0 && (
                <div className="flex items-center gap-2">
                  <Badge variant="secondary">{selectedIds.length}개 선택</Badge>
                  {statusCounts.active > 0 && (
                    <Badge className="bg-green-100 text-green-800">활성 {statusCounts.active}</Badge>
                  )}
                  {statusCounts.inactive > 0 && (
                    <Badge className="bg-gray-100 text-gray-800">비활성 {statusCounts.inactive}</Badge>
                  )}
                  {statusCounts.pending > 0 && (
                    <Badge className="bg-yellow-100 text-yellow-800">대기 {statusCounts.pending}</Badge>
                  )}
                </div>
              )}
            </div>

            {/* Bulk Actions */}
            {selectedIds.length > 0 && (
              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => onBulkExport(selectedIds)}
                  disabled={isLoading}
                  className="h-8"
                >
                  <Download className="w-3 h-3 mr-1" />
                  내보내기
                </Button>

                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleBulkAction('statusChange', PropertyStatus.ACTIVE)}
                  disabled={isLoading || statusCounts.active === selectedIds.length}
                  className="h-8"
                >
                  <ToggleRight className="w-3 h-3 mr-1" />
                  활성화
                </Button>

                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleBulkAction('statusChange', PropertyStatus.INACTIVE)}
                  disabled={isLoading || statusCounts.inactive === selectedIds.length}
                  className="h-8"
                >
                  <ToggleLeft className="w-3 h-3 mr-1" />
                  비활성화
                </Button>

                <Button
                  size="sm"
                  variant="outline"
                  onClick={() => handleBulkAction('delete')}
                  disabled={isLoading}
                  className="h-8 text-red-600 hover:text-red-700 hover:bg-red-50"
                >
                  <Trash2 className="w-3 h-3 mr-1" />
                  삭제
                </Button>
              </div>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Individual Property Selection */}
      <div className="space-y-2">
        {properties.map((property) => {
          const isSelected = selectedIds.includes(property.id);
          
          return (
            <div
              key={property.id}
              className={`flex items-center gap-3 p-2 rounded-lg border transition-colors ${
                isSelected ? 'bg-blue-50 border-blue-200' : 'bg-white border-gray-200 hover:bg-gray-50'
              }`}
            >
              <button
                onClick={() => handleIndividualSelect(property.id)}
                className="flex-shrink-0"
              >
                {isSelected ? (
                  <CheckSquare className="w-4 h-4 text-blue-600" />
                ) : (
                  <Square className="w-4 h-4 text-gray-400 hover:text-gray-600" />
                )}
              </button>

              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <h4 className="font-medium text-sm text-gray-900 truncate">{property.title}</h4>
                  <Badge 
                    className={
                      property.status === PropertyStatus.ACTIVE
                        ? 'bg-green-100 text-green-800'
                        : property.status === PropertyStatus.INACTIVE
                        ? 'bg-gray-100 text-gray-800'
                        : 'bg-yellow-100 text-yellow-800'
                    }
                  >
                    {property.status === PropertyStatus.ACTIVE
                      ? '활성'
                      : property.status === PropertyStatus.INACTIVE
                      ? '비활성'
                      : '대기'
                    }
                  </Badge>
                </div>
                <p className="text-xs text-gray-500 truncate">{property.address}</p>
              </div>

              <div className="flex items-center gap-4 text-xs text-gray-500">
                <span>조회 {property.views}</span>
                <span>문의 {property.inquiries}</span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Confirmation Dialog */}
      {showConfirmDialog && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <Card className="w-full max-w-md">
            <CardContent className="p-6">
              <div className="flex items-center gap-3 mb-4">
                <AlertTriangle className="w-6 h-6 text-orange-500" />
                <h3 className="text-lg font-semibold text-gray-900">작업 확인</h3>
              </div>

              <div className="space-y-4">
                <p className="text-gray-600">
                  정말로 {getActionText()}하시겠습니까?
                </p>

                {pendingAction?.type === 'delete' && (
                  <div className="bg-red-50 border border-red-200 rounded-lg p-3">
                    <p className="text-sm text-red-800">
                      ⚠️ 이 작업은 되돌릴 수 없습니다. 매물과 관련된 모든 데이터가 영구적으로 삭제됩니다.
                    </p>
                  </div>
                )}

                <div className="bg-gray-50 rounded-lg p-3">
                  <h4 className="font-medium text-gray-900 mb-2">선택된 매물:</h4>
                  <div className="space-y-1 max-h-32 overflow-y-auto">
                    {getSelectedProperties().map((property) => (
                      <div key={property.id} className="text-sm text-gray-600">
                        • {property.title}
                      </div>
                    ))}
                  </div>
                </div>
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
                    pendingAction?.type === 'delete'
                      ? 'bg-red-600 hover:bg-red-700'
                      : 'bg-blue-600 hover:bg-blue-700'
                  }
                >
                  {isLoading ? '처리 중...' : '확인'}
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      )}
    </>
  );
}