'use client';

import React, { useState, useCallback } from 'react';
import { 
  ArrowLeft, 
  MapPin, 
  Calendar, 
  Eye, 
  MessageSquare, 
  Heart, 
  Share2, 
  Edit3, 
  Trash2,
  ToggleLeft,
  ToggleRight,
  Download,
  BarChart3,
  Clock,
  DollarSign,
  Home,
  Ruler,
  Car,
  Wifi,
  Shield,
  ChevronLeft,
  ChevronRight,
  Maximize2
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Progress } from '@/components/ui/progress';
import { PropertyWithStats } from '../property-dashboard';
import { PropertyStatus, PropertyType } from '@/lib/types/property';

interface PropertyDetailPageProps {
  property: PropertyWithStats;
  onBack: () => void;
  onEdit: (property: PropertyWithStats) => void;
  onDelete: (propertyId: string) => void;
  onStatusChange: (propertyId: string, status: PropertyStatus) => void;
  isLoading?: boolean;
}

interface ViewStats {
  totalViews: number;
  uniqueViews: number;
  viewsThisWeek: number;
  averageTimeOnPage: string;
  bounceRate: number;
  conversionRate: number;
}

interface InquiryStats {
  totalInquiries: number;
  responseRate: number;
  averageResponseTime: string;
  inquiriesThisWeek: number;
}

export default function PropertyDetailPage({
  property,
  onBack,
  onEdit,
  onDelete,
  onStatusChange,
  isLoading = false,
}: PropertyDetailPageProps) {
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [showImageModal, setShowImageModal] = useState(false);
  const [activeTab, setActiveTab] = useState<'overview' | 'analytics' | 'inquiries' | 'history'>('overview');

  // Mock analytics data
  const viewStats: ViewStats = {
    totalViews: property.views,
    uniqueViews: Math.round(property.views * 0.7),
    viewsThisWeek: Math.round(property.views * 0.15),
    averageTimeOnPage: '2분 34초',
    bounceRate: 0.32,
    conversionRate: 0.08,
  };

  const inquiryStats: InquiryStats = {
    totalInquiries: property.inquiries,
    responseRate: 0.95,
    averageResponseTime: '4시간 12분',
    inquiriesThisWeek: Math.round(property.inquiries * 0.2),
  };

  const handleImageNavigation = useCallback((direction: 'prev' | 'next') => {
    if (!property.images || property.images.length === 0) return;
    
    if (direction === 'prev') {
      setCurrentImageIndex(prev => 
        prev === 0 ? property.images!.length - 1 : prev - 1
      );
    } else {
      setCurrentImageIndex(prev => 
        prev === property.images!.length - 1 ? 0 : prev + 1
      );
    }
  }, [property.images]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (date: Date) => {
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date);
  };

  const getStatusColor = (status: PropertyStatus) => {
    switch (status) {
      case PropertyStatus.ACTIVE:
        return 'bg-green-100 text-green-800';
      case PropertyStatus.INACTIVE:
        return 'bg-gray-100 text-gray-800';
      case PropertyStatus.PENDING_APPROVAL:
        return 'bg-yellow-100 text-yellow-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const getStatusText = (status: PropertyStatus) => {
    switch (status) {
      case PropertyStatus.ACTIVE:
        return '활성';
      case PropertyStatus.INACTIVE:
        return '비활성';
      case PropertyStatus.PENDING_APPROVAL:
        return '승인 대기';
      default:
        return '알 수 없음';
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div className="flex items-center gap-4">
          <Button
            variant="outline"
            size="sm"
            onClick={onBack}
            className="flex items-center gap-2"
          >
            <ArrowLeft className="w-4 h-4" />
            목록으로
          </Button>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">{property.title}</h1>
            <p className="text-gray-600 flex items-center gap-1 mt-1">
              <MapPin className="w-4 h-4" />
              {property.address}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Badge className={getStatusColor(property.status)}>
            {getStatusText(property.status)}
          </Badge>
          
          <Button
            variant="outline"
            onClick={() => onStatusChange(
              property.id, 
              property.status === PropertyStatus.ACTIVE 
                ? PropertyStatus.INACTIVE 
                : PropertyStatus.ACTIVE
            )}
            disabled={isLoading}
            className="flex items-center gap-2"
          >
            {property.status === PropertyStatus.ACTIVE ? (
              <>
                <ToggleLeft className="w-4 h-4" />
                비활성화
              </>
            ) : (
              <>
                <ToggleRight className="w-4 h-4" />
                활성화
              </>
            )}
          </Button>

          <Button
            variant="outline"
            onClick={() => onEdit(property)}
            disabled={isLoading}
            className="flex items-center gap-2"
          >
            <Edit3 className="w-4 h-4" />
            수정
          </Button>

          <Button
            variant="outline"
            onClick={() => onDelete(property.id)}
            disabled={isLoading}
            className="flex items-center gap-2 text-red-600 hover:text-red-700 hover:bg-red-50"
          >
            <Trash2 className="w-4 h-4" />
            삭제
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Main Content */}
        <div className="lg:col-span-2 space-y-6">
          {/* Image Gallery */}
          {property.images && property.images.length > 0 && (
            <Card>
              <CardContent className="p-0">
                <div className="relative">
                  <img
                    src={property.images[currentImageIndex]}
                    alt={`${property.title} 이미지 ${currentImageIndex + 1}`}
                    className="w-full h-80 object-cover rounded-t-lg"
                  />
                  
                  {property.images.length > 1 && (
                    <>
                      <button
                        onClick={() => handleImageNavigation('prev')}
                        className="absolute left-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full p-2 hover:bg-opacity-70"
                      >
                        <ChevronLeft className="w-5 h-5" />
                      </button>
                      <button
                        onClick={() => handleImageNavigation('next')}
                        className="absolute right-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full p-2 hover:bg-opacity-70"
                      >
                        <ChevronRight className="w-5 h-5" />
                      </button>
                    </>
                  )}

                  <button
                    onClick={() => setShowImageModal(true)}
                    className="absolute top-4 right-4 bg-black bg-opacity-50 text-white rounded-lg p-2 hover:bg-opacity-70"
                  >
                    <Maximize2 className="w-4 h-4" />
                  </button>

                  <div className="absolute bottom-4 left-4 bg-black bg-opacity-50 text-white px-3 py-1 rounded-lg text-sm">
                    {currentImageIndex + 1} / {property.images.length}
                  </div>
                </div>

                {property.images.length > 1 && (
                  <div className="p-4">
                    <div className="flex gap-2 overflow-x-auto">
                      {property.images.map((image, index) => (
                        <button
                          key={index}
                          onClick={() => setCurrentImageIndex(index)}
                          className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 ${
                            index === currentImageIndex ? 'border-blue-500' : 'border-gray-200'
                          }`}
                        >
                          <img
                            src={image}
                            alt={`썸네일 ${index + 1}`}
                            className="w-full h-full object-cover"
                          />
                        </button>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          )}

          {/* Tabs */}
          <div className="border-b border-gray-200">
            <nav className="-mb-px flex space-x-8">
              {[
                { id: 'overview', label: '개요', icon: Home },
                { id: 'analytics', label: '분석', icon: BarChart3 },
                { id: 'inquiries', label: '문의', icon: MessageSquare },
                { id: 'history', label: '히스토리', icon: Clock },
              ].map(({ id, label, icon: Icon }) => (
                <button
                  key={id}
                  onClick={() => setActiveTab(id as any)}
                  className={`flex items-center gap-2 py-2 px-1 border-b-2 font-medium text-sm ${
                    activeTab === id
                      ? 'border-blue-500 text-blue-600'
                      : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {label}
                </button>
              ))}
            </nav>
          </div>

          {/* Tab Content */}
          <div className="space-y-6">
            {activeTab === 'overview' && (
              <div className="space-y-6">
                {/* Description */}
                <Card>
                  <CardHeader>
                    <CardTitle>매물 설명</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <p className="text-gray-700 leading-relaxed">
                      {property.description || '매물 설명이 없습니다.'}
                    </p>
                  </CardContent>
                </Card>

                {/* Property Details */}
                <Card>
                  <CardHeader>
                    <CardTitle>매물 정보</CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                      <div className="text-center p-4 bg-gray-50 rounded-lg">
                        <Ruler className="w-6 h-6 mx-auto mb-2 text-gray-600" />
                        <div className="text-2xl font-bold text-gray-900">{property.area}</div>
                        <div className="text-sm text-gray-500">㎡</div>
                      </div>
                      <div className="text-center p-4 bg-gray-50 rounded-lg">
                        <Home className="w-6 h-6 mx-auto mb-2 text-gray-600" />
                        <div className="text-2xl font-bold text-gray-900">{property.rooms}</div>
                        <div className="text-sm text-gray-500">방</div>
                      </div>
                      <div className="text-center p-4 bg-gray-50 rounded-lg">
                        <div className="w-6 h-6 mx-auto mb-2 text-gray-600 text-lg">🚿</div>
                        <div className="text-2xl font-bold text-gray-900">{property.bathrooms}</div>
                        <div className="text-sm text-gray-500">욕실</div>
                      </div>
                      <div className="text-center p-4 bg-gray-50 rounded-lg">
                        <div className="w-6 h-6 mx-auto mb-2 text-gray-600 text-lg">🏢</div>
                        <div className="text-2xl font-bold text-gray-900">{property.floor}</div>
                        <div className="text-sm text-gray-500">층</div>
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Options */}
                {property.options && property.options.length > 0 && (
                  <Card>
                    <CardHeader>
                      <CardTitle>편의시설 및 옵션</CardTitle>
                    </CardHeader>
                    <CardContent>
                      <div className="flex flex-wrap gap-2">
                        {property.options.map((option, index) => (
                          <Badge key={index} variant="secondary">
                            {option}
                          </Badge>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            )}

            {activeTab === 'analytics' && (
              <div className="space-y-6">
                {/* View Analytics */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <Eye className="w-5 h-5" />
                      조회수 분석
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                      <div className="text-center">
                        <div className="text-2xl font-bold text-blue-600">{viewStats.totalViews}</div>
                        <div className="text-sm text-gray-500">총 조회수</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-green-600">{viewStats.uniqueViews}</div>
                        <div className="text-sm text-gray-500">순 조회수</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-purple-600">{viewStats.viewsThisWeek}</div>
                        <div className="text-sm text-gray-500">이번 주</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-orange-600">{viewStats.averageTimeOnPage}</div>
                        <div className="text-sm text-gray-500">평균 체류시간</div>
                      </div>
                    </div>
                    
                    <div className="space-y-4">
                      <div>
                        <div className="flex justify-between mb-2">
                          <span className="text-sm font-medium">이탈율</span>
                          <span className="text-sm text-gray-600">{(viewStats.bounceRate * 100).toFixed(1)}%</span>
                        </div>
                        <Progress value={viewStats.bounceRate * 100} className="h-2" />
                      </div>
                      
                      <div>
                        <div className="flex justify-between mb-2">
                          <span className="text-sm font-medium">전환율 (문의로 이어진 비율)</span>
                          <span className="text-sm text-gray-600">{(viewStats.conversionRate * 100).toFixed(1)}%</span>
                        </div>
                        <Progress value={viewStats.conversionRate * 100} className="h-2" />
                      </div>
                    </div>
                  </CardContent>
                </Card>

                {/* Inquiry Analytics */}
                <Card>
                  <CardHeader>
                    <CardTitle className="flex items-center gap-2">
                      <MessageSquare className="w-5 h-5" />
                      문의 분석
                    </CardTitle>
                  </CardHeader>
                  <CardContent>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
                      <div className="text-center">
                        <div className="text-2xl font-bold text-blue-600">{inquiryStats.totalInquiries}</div>
                        <div className="text-sm text-gray-500">총 문의수</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-green-600">{(inquiryStats.responseRate * 100).toFixed(0)}%</div>
                        <div className="text-sm text-gray-500">응답률</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-purple-600">{inquiryStats.inquiriesThisWeek}</div>
                        <div className="text-sm text-gray-500">이번 주</div>
                      </div>
                      <div className="text-center">
                        <div className="text-2xl font-bold text-orange-600">{inquiryStats.averageResponseTime}</div>
                        <div className="text-sm text-gray-500">평균 응답시간</div>
                      </div>
                    </div>

                    <div>
                      <div className="flex justify-between mb-2">
                        <span className="text-sm font-medium">응답률</span>
                        <span className="text-sm text-gray-600">{(inquiryStats.responseRate * 100).toFixed(1)}%</span>
                      </div>
                      <Progress value={inquiryStats.responseRate * 100} className="h-2" />
                    </div>
                  </CardContent>
                </Card>
              </div>
            )}

            {activeTab === 'inquiries' && (
              <Card>
                <CardHeader>
                  <CardTitle>최근 문의 내역</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="text-center py-8 text-gray-500">
                    <MessageSquare className="w-12 h-12 mx-auto mb-4 text-gray-400" />
                    <p>문의 내역이 없습니다.</p>
                  </div>
                </CardContent>
              </Card>
            )}

            {activeTab === 'history' && (
              <Card>
                <CardHeader>
                  <CardTitle>변경 히스토리</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                      <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                      <div className="flex-1">
                        <p className="font-medium">매물 등록</p>
                        <p className="text-sm text-gray-500">{formatDate(property.createdAt)}</p>
                      </div>
                    </div>
                    {property.updatedAt > property.createdAt && (
                      <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-lg">
                        <div className="w-2 h-2 bg-green-500 rounded-full"></div>
                        <div className="flex-1">
                          <p className="font-medium">매물 정보 수정</p>
                          <p className="text-sm text-gray-500">{formatDate(property.updatedAt)}</p>
                        </div>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          {/* Quick Stats */}
          <Card>
            <CardHeader>
              <CardTitle>통계 요약</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Eye className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">조회수</span>
                </div>
                <span className="font-semibold">{property.views}</span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <MessageSquare className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">문의수</span>
                </div>
                <span className="font-semibold">{property.inquiries}</span>
              </div>
              
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <Heart className="w-4 h-4 text-gray-500" />
                  <span className="text-sm">관심 등록</span>
                </div>
                <span className="font-semibold">{property.favorites}</span>
              </div>
            </CardContent>
          </Card>

          {/* Pricing */}
          <Card>
            <CardHeader>
              <CardTitle>가격 정보</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="text-sm">월세</span>
                <span className="font-bold text-lg">{formatCurrency(property.rent)}</span>
              </div>
              
              {property.deposit && property.deposit > 0 && (
                <div className="flex items-center justify-between">
                  <span className="text-sm">보증금</span>
                  <span className="font-semibold">{formatCurrency(property.deposit)}</span>
                </div>
              )}
              
              {property.managementFee && property.managementFee > 0 && (
                <div className="flex items-center justify-between">
                  <span className="text-sm">관리비</span>
                  <span className="font-semibold">{formatCurrency(property.managementFee)}</span>
                </div>
              )}
            </CardContent>
          </Card>

          {/* Actions */}
          <Card>
            <CardHeader>
              <CardTitle>빠른 작업</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button variant="outline" className="w-full justify-start">
                <Share2 className="w-4 h-4 mr-2" />
                공유하기
              </Button>
              
              <Button variant="outline" className="w-full justify-start">
                <Download className="w-4 h-4 mr-2" />
                보고서 다운로드
              </Button>
              
              <Button variant="outline" className="w-full justify-start">
                <BarChart3 className="w-4 h-4 mr-2" />
                상세 분석 보기
              </Button>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* Image Modal */}
      {showImageModal && property.images && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-90 flex items-center justify-center z-50 p-4"
          onClick={() => setShowImageModal(false)}
        >
          <div className="relative max-w-6xl max-h-full">
            <img
              src={property.images[currentImageIndex]}
              alt={`${property.title} 이미지 ${currentImageIndex + 1}`}
              className="max-w-full max-h-full object-contain rounded-lg"
            />
            
            {property.images.length > 1 && (
              <>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleImageNavigation('prev');
                  }}
                  className="absolute left-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full p-3 hover:bg-opacity-70"
                >
                  <ChevronLeft className="w-6 h-6" />
                </button>
                <button
                  onClick={(e) => {
                    e.stopPropagation();
                    handleImageNavigation('next');
                  }}
                  className="absolute right-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full p-3 hover:bg-opacity-70"
                >
                  <ChevronRight className="w-6 h-6" />
                </button>
              </>
            )}

            <div className="absolute bottom-4 left-4 bg-black bg-opacity-50 text-white px-3 py-1 rounded-lg">
              {currentImageIndex + 1} / {property.images.length}
            </div>

            <Button
              variant="outline"
              className="absolute top-4 right-4 bg-white"
              onClick={() => setShowImageModal(false)}
            >
              닫기
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}