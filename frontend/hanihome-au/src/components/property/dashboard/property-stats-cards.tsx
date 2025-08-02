/**
 * Property Statistics Cards Component
 */

'use client';

import React from 'react';
import {
  HomeIcon,
  CheckCircleIcon,
  ClockIcon,
  EyeIcon,
  ChatBubbleLeftIcon,
  XCircleIcon,
} from '@heroicons/react/24/outline';

interface PropertyStats {
  total: number;
  active: number;
  pending: number;
  inactive: number;
  totalViews: number;
  totalInquiries: number;
}

interface PropertyStatsCardsProps {
  stats: PropertyStats;
  isLoading?: boolean;
}

interface StatCard {
  name: string;
  value: number;
  icon: React.ComponentType<React.SVGProps<SVGSVGElement>>;
  color: string;
  bgColor: string;
  description: string;
}

export default function PropertyStatsCards({ stats, isLoading = false }: PropertyStatsCardsProps) {
  const statCards: StatCard[] = [
    {
      name: '총 매물',
      value: stats.total,
      icon: HomeIcon,
      color: 'text-blue-600',
      bgColor: 'bg-blue-50',
      description: '등록된 전체 매물 수',
    },
    {
      name: '활성 매물',
      value: stats.active,
      icon: CheckCircleIcon,
      color: 'text-green-600',
      bgColor: 'bg-green-50',
      description: '현재 활성화된 매물',
    },
    {
      name: '승인 대기',
      value: stats.pending,
      icon: ClockIcon,
      color: 'text-yellow-600',
      bgColor: 'bg-yellow-50',
      description: '승인 대기 중인 매물',
    },
    {
      name: '비활성 매물',
      value: stats.inactive,
      icon: XCircleIcon,
      color: 'text-gray-600',
      bgColor: 'bg-gray-50',
      description: '비활성화된 매물',
    },
    {
      name: '총 조회수',
      value: stats.totalViews,
      icon: EyeIcon,
      color: 'text-purple-600',
      bgColor: 'bg-purple-50',
      description: '전체 매물 조회수',
    },
    {
      name: '총 문의수',
      value: stats.totalInquiries,
      icon: ChatBubbleLeftIcon,
      color: 'text-indigo-600',
      bgColor: 'bg-indigo-50',
      description: '받은 문의 총 개수',
    },
  ];

  if (isLoading) {
    return (
      <div className="mb-8">
        <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
          {Array.from({ length: 6 }).map((_, index) => (
            <div key={index} className="overflow-hidden rounded-lg bg-white px-4 py-5 shadow animate-pulse">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <div className="h-8 w-8 bg-gray-200 rounded-md"></div>
                </div>
                <div className="ml-5 w-0 flex-1">
                  <div className="h-4 bg-gray-200 rounded mb-2"></div>
                  <div className="h-6 bg-gray-200 rounded"></div>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="mb-8">
      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6">
        {statCards.map((stat) => {
          const IconComponent = stat.icon;
          return (
            <div
              key={stat.name}
              className="relative overflow-hidden rounded-lg bg-white px-4 py-5 shadow hover:shadow-md transition-shadow duration-200"
            >
              <dt className="flex items-center">
                <div className={`flex-shrink-0 ${stat.bgColor} p-2 rounded-md`}>
                  <IconComponent className={`h-5 w-5 ${stat.color}`} aria-hidden="true" />
                </div>
                <div className="ml-3">
                  <p className="text-sm font-medium text-gray-500 truncate">{stat.name}</p>
                </div>
              </dt>
              <dd className="mt-2">
                <div className="flex items-baseline">
                  <p className="text-2xl font-semibold text-gray-900">
                    {stat.value.toLocaleString()}
                  </p>
                </div>
                <p className="text-xs text-gray-600 mt-1">{stat.description}</p>
              </dd>
              
              {/* Subtle hover effect overlay */}
              <div className="absolute inset-0 bg-gradient-to-r from-transparent to-gray-50 opacity-0 hover:opacity-20 transition-opacity duration-200 pointer-events-none"></div>
            </div>
          );
        })}
      </div>
      
      {/* Quick insights */}
      <div className="mt-4 text-center">
        <div className="inline-flex items-center space-x-6 text-sm text-gray-600">
          {stats.total > 0 && (
            <>
              <span>
                활성화율: <span className="font-medium text-green-600">
                  {((stats.active / stats.total) * 100).toFixed(1)}%
                </span>
              </span>
              {stats.totalViews > 0 && (
                <span>
                  평균 조회수: <span className="font-medium text-purple-600">
                    {Math.round(stats.totalViews / stats.total)}
                  </span>
                </span>
              )}
              {stats.totalInquiries > 0 && (
                <span>
                  문의 전환율: <span className="font-medium text-indigo-600">
                    {((stats.totalInquiries / stats.totalViews) * 100).toFixed(1)}%
                  </span>
                </span>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  );
}