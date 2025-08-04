'use client';

import React, { useState, useCallback } from 'react';
import { 
  Clock, 
  Edit3, 
  ToggleLeft, 
  ToggleRight, 
  Upload, 
  Trash2, 
  User,
  Calendar,
  FileText,
  Image as ImageIcon,
  DollarSign,
  Home,
  MapPin,
  Settings,
  Eye,
  Download,
  Filter,
  Search
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { PropertyWithStats } from '../property-dashboard';
import { PropertyStatus } from '@/lib/types/property';

interface PropertyHistoryEntry {
  id: string;
  timestamp: Date;
  action: 'created' | 'updated' | 'status_changed' | 'images_updated' | 'deleted' | 'restored';
  userId: string;
  userName: string;
  details: {
    field?: string;
    oldValue?: any;
    newValue?: any;
    description?: string;
    changes?: Record<string, { old: any; new: any }>;
  };
  metadata?: {
    ipAddress?: string;
    userAgent?: string;
    source?: 'web' | 'mobile' | 'api';
  };
}

interface PropertyHistoryManagerProps {
  property: PropertyWithStats;
  onExportHistory?: (propertyId: string) => void;
  onRestoreVersion?: (propertyId: string, historyId: string) => void;
}

export default function PropertyHistoryManager({
  property,
  onExportHistory,
  onRestoreVersion,
}: PropertyHistoryManagerProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [filterAction, setFilterAction] = useState<string>('all');
  const [filterDateRange, setFilterDateRange] = useState<string>('all');
  const [expandedEntry, setExpandedEntry] = useState<string | null>(null);

  // Mock history data
  const [historyEntries] = useState<PropertyHistoryEntry[]>([
    {
      id: '1',
      timestamp: new Date(Date.now() - 24 * 60 * 60 * 1000), // 1 day ago
      action: 'updated',
      userId: 'user1',
      userName: '관리자',
      details: {
        changes: {
          title: { old: '원래 매물명', new: property.title },
          rent: { old: 800000, new: property.rent },
          description: { old: '이전 설명', new: property.description },
        },
        description: '매물 기본 정보 수정'
      },
      metadata: {
        source: 'web',
        ipAddress: '192.168.1.1'
      }
    },
    {
      id: '2',
      timestamp: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
      action: 'status_changed',
      userId: 'user1',
      userName: '관리자',
      details: {
        field: 'status',
        oldValue: PropertyStatus.INACTIVE,
        newValue: PropertyStatus.ACTIVE,
        description: '매물 상태를 활성으로 변경'
      },
      metadata: {
        source: 'web'
      }
    },
    {
      id: '3',
      timestamp: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // 1 week ago
      action: 'images_updated',
      userId: 'user2',
      userName: '중개인',
      details: {
        description: '이미지 3개 추가, 1개 삭제',
        changes: {
          images: { 
            old: ['image1.jpg', 'image2.jpg'], 
            new: ['image1.jpg', 'image3.jpg', 'image4.jpg', 'image5.jpg'] 
          }
        }
      },
      metadata: {
        source: 'mobile'
      }
    },
    {
      id: '4',
      timestamp: property.createdAt,
      action: 'created',
      userId: 'user2',
      userName: '중개인',
      details: {
        description: '매물 최초 등록'
      },
      metadata: {
        source: 'web'
      }
    }
  ]);

  const getActionIcon = (action: PropertyHistoryEntry['action']) => {
    switch (action) {
      case 'created':
        return <Upload className="w-4 h-4 text-green-600" />;
      case 'updated':
        return <Edit3 className="w-4 h-4 text-blue-600" />;
      case 'status_changed':
        return <ToggleRight className="w-4 h-4 text-purple-600" />;
      case 'images_updated':
        return <ImageIcon className="w-4 h-4 text-orange-600" />;
      case 'deleted':
        return <Trash2 className="w-4 h-4 text-red-600" />;
      case 'restored':
        return <Settings className="w-4 h-4 text-indigo-600" />;
      default:
        return <Clock className="w-4 h-4 text-gray-600" />;
    }
  };

  const getActionText = (action: PropertyHistoryEntry['action']) => {
    switch (action) {
      case 'created':
        return '생성됨';
      case 'updated':
        return '수정됨';
      case 'status_changed':
        return '상태 변경';
      case 'images_updated':
        return '이미지 업데이트';
      case 'deleted':
        return '삭제됨';
      case 'restored':
        return '복원됨';
      default:
        return '알 수 없음';
    }
  };

  const getActionColor = (action: PropertyHistoryEntry['action']) => {
    switch (action) {
      case 'created':
        return 'bg-green-100 text-green-800';
      case 'updated':
        return 'bg-blue-100 text-blue-800';
      case 'status_changed':
        return 'bg-purple-100 text-purple-800';
      case 'images_updated':
        return 'bg-orange-100 text-orange-800';
      case 'deleted':
        return 'bg-red-100 text-red-800';
      case 'restored':
        return 'bg-indigo-100 text-indigo-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const formatTimestamp = (date: Date) => {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
    }).format(date);
  };

  const formatRelativeTime = (date: Date) => {
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMinutes = Math.floor(diffMs / (1000 * 60));
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

    if (diffMinutes < 60) {
      return `${diffMinutes}분 전`;
    } else if (diffHours < 24) {
      return `${diffHours}시간 전`;
    } else if (diffDays < 7) {
      return `${diffDays}일 전`;
    } else {
      return formatTimestamp(date);
    }
  };

  const filteredEntries = historyEntries.filter(entry => {
    // Search filter
    if (searchTerm) {
      const searchLower = searchTerm.toLowerCase();
      if (
        !entry.userName.toLowerCase().includes(searchLower) &&
        !entry.details.description?.toLowerCase().includes(searchLower) &&
        !getActionText(entry.action).toLowerCase().includes(searchLower)
      ) {
        return false;
      }
    }

    // Action filter
    if (filterAction !== 'all' && entry.action !== filterAction) {
      return false;
    }

    // Date range filter
    if (filterDateRange !== 'all') {
      const now = new Date();
      const entryDate = entry.timestamp;
      const daysDiff = (now.getTime() - entryDate.getTime()) / (1000 * 60 * 60 * 24);

      switch (filterDateRange) {
        case 'today':
          if (daysDiff > 1) return false;
          break;
        case 'week':
          if (daysDiff > 7) return false;
          break;
        case 'month':
          if (daysDiff > 30) return false;
          break;
      }
    }

    return true;
  });

  const renderChangeDetails = (entry: PropertyHistoryEntry) => {
    if (!entry.details.changes) return null;

    return (
      <div className="mt-3 space-y-2">
        {Object.entries(entry.details.changes).map(([field, change]) => (
          <div key={field} className="bg-gray-50 p-3 rounded-lg">
            <div className="font-medium text-sm text-gray-700 mb-2 capitalize">
              {field === 'title' ? '매물명' :
               field === 'rent' ? '임대료' :
               field === 'description' ? '설명' :
               field === 'status' ? '상태' :
               field === 'images' ? '이미지' :
               field}
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs">
              <div>
                <div className="text-gray-500 mb-1">이전:</div>
                <div className="bg-red-50 p-2 rounded border-l-2 border-red-200">
                  {typeof change.old === 'object' ? JSON.stringify(change.old) : String(change.old)}
                </div>
              </div>
              <div>
                <div className="text-gray-500 mb-1">변경:</div>
                <div className="bg-green-50 p-2 rounded border-l-2 border-green-200">
                  {typeof change.new === 'object' ? JSON.stringify(change.new) : String(change.new)}
                </div>
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold text-gray-900">변경 히스토리</h3>
          <p className="text-sm text-gray-600">
            매물의 모든 변경 사항을 추적하고 관리합니다.
          </p>
        </div>
        
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={() => onExportHistory?.(property.id)}
            className="flex items-center gap-2"
          >
            <Download className="w-4 h-4" />
            히스토리 내보내기
          </Button>
        </div>
      </div>

      {/* Filters */}
      <Card>
        <CardContent className="p-4">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                <Input
                  placeholder="사용자명, 설명, 작업 검색..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="pl-10"
                />
              </div>
            </div>
            
            <Select value={filterAction} onValueChange={setFilterAction}>
              <SelectTrigger className="w-full sm:w-40">
                <SelectValue placeholder="작업 유형" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">모든 작업</SelectItem>
                <SelectItem value="created">생성</SelectItem>
                <SelectItem value="updated">수정</SelectItem>
                <SelectItem value="status_changed">상태 변경</SelectItem>
                <SelectItem value="images_updated">이미지 업데이트</SelectItem>
                <SelectItem value="deleted">삭제</SelectItem>
                <SelectItem value="restored">복원</SelectItem>
              </SelectContent>
            </Select>

            <Select value={filterDateRange} onValueChange={setFilterDateRange}>
              <SelectTrigger className="w-full sm:w-32">
                <SelectValue placeholder="기간" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">전체 기간</SelectItem>
                <SelectItem value="today">오늘</SelectItem>
                <SelectItem value="week">이번 주</SelectItem>
                <SelectItem value="month">이번 달</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* History Timeline */}
      <div className="space-y-4">
        {filteredEntries.length === 0 ? (
          <Card>
            <CardContent className="p-8 text-center">
              <Clock className="w-12 h-12 mx-auto mb-4 text-gray-400" />
              <p className="text-gray-500">필터 조건에 맞는 히스토리가 없습니다.</p>
            </CardContent>
          </Card>
        ) : (
          filteredEntries.map((entry, index) => (
            <Card key={entry.id} className="transition-shadow hover:shadow-md">
              <CardContent className="p-4">
                <div className="flex items-start gap-4">
                  {/* Timeline dot */}
                  <div className="flex-shrink-0 mt-1">
                    <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center">
                      {getActionIcon(entry.action)}
                    </div>
                    {index < filteredEntries.length - 1 && (
                      <div className="w-0.5 h-8 bg-gray-200 mx-auto mt-2"></div>
                    )}
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center justify-between mb-2">
                      <div className="flex items-center gap-2">
                        <Badge className={getActionColor(entry.action)}>
                          {getActionText(entry.action)}
                        </Badge>
                        <span className="text-sm font-medium text-gray-900">
                          {entry.details.description}
                        </span>
                      </div>
                      
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => setExpandedEntry(
                          expandedEntry === entry.id ? null : entry.id
                        )}
                        className="text-xs h-6 px-2"
                      >
                        {expandedEntry === entry.id ? '접기' : '자세히'}
                      </Button>
                    </div>

                    <div className="flex items-center gap-4 text-xs text-gray-500 mb-2">
                      <div className="flex items-center gap-1">
                        <User className="w-3 h-3" />
                        {entry.userName}
                      </div>
                      <div className="flex items-center gap-1">
                        <Calendar className="w-3 h-3" />
                        {formatRelativeTime(entry.timestamp)}
                      </div>
                      {entry.metadata?.source && (
                        <Badge variant="outline" className="text-xs">
                          {entry.metadata.source === 'web' ? '웹' : 
                           entry.metadata.source === 'mobile' ? '모바일' : 'API'}
                        </Badge>
                      )}
                    </div>

                    {/* Expanded details */}
                    {expandedEntry === entry.id && (
                      <div className="border-t pt-3 mt-3">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4 text-xs">
                          <div>
                            <span className="font-medium text-gray-700">정확한 시각:</span>
                            <div className="text-gray-600">{formatTimestamp(entry.timestamp)}</div>
                          </div>
                          <div>
                            <span className="font-medium text-gray-700">사용자 ID:</span>
                            <div className="text-gray-600">{entry.userId}</div>
                          </div>
                          {entry.metadata?.ipAddress && (
                            <div>
                              <span className="font-medium text-gray-700">IP 주소:</span>
                              <div className="text-gray-600">{entry.metadata.ipAddress}</div>
                            </div>
                          )}
                          {entry.metadata?.userAgent && (
                            <div>
                              <span className="font-medium text-gray-700">사용자 에이전트:</span>
                              <div className="text-gray-600 truncate">{entry.metadata.userAgent}</div>
                            </div>
                          )}
                        </div>

                        {renderChangeDetails(entry)}

                        {entry.action === 'updated' && onRestoreVersion && (
                          <div className="mt-4 pt-3 border-t">
                            <Button
                              variant="outline"
                              size="sm"
                              onClick={() => onRestoreVersion(property.id, entry.id)}
                              className="text-xs"
                            >
                              이 버전으로 복원
                            </Button>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* Summary Stats */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">히스토리 요약</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-center">
            <div>
              <div className="text-2xl font-bold text-blue-600">
                {historyEntries.filter(e => e.action === 'updated').length}
              </div>
              <div className="text-sm text-gray-500">수정 횟수</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-purple-600">
                {historyEntries.filter(e => e.action === 'status_changed').length}
              </div>
              <div className="text-sm text-gray-500">상태 변경</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-orange-600">
                {historyEntries.filter(e => e.action === 'images_updated').length}
              </div>
              <div className="text-sm text-gray-500">이미지 업데이트</div>
            </div>
            <div>
              <div className="text-2xl font-bold text-green-600">
                {new Set(historyEntries.map(e => e.userId)).size}
              </div>
              <div className="text-sm text-gray-500">참여 사용자</div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}