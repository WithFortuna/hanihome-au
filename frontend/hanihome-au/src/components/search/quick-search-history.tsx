'use client';

import React, { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Clock, Bookmark, Search } from 'lucide-react';

interface SearchHistoryItem {
  id: number;
  displayText: string;
  isSaved: boolean;
  searchCount: number;
  lastUsedAt: string;
  searchSummary: {
    location?: string;
    priceRange?: string;
    totalFilters: number;
  };
}

interface QuickSearchHistoryProps {
  onSearchSelect?: (searchItem: SearchHistoryItem) => void;
  maxItems?: number;
  className?: string;
}

export function QuickSearchHistory({ 
  onSearchSelect, 
  maxItems = 5, 
  className 
}: QuickSearchHistoryProps) {
  const { data: session } = useSession();
  const [recentSearches, setRecentSearches] = useState<SearchHistoryItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (session?.accessToken) {
      fetchRecentSearches();
    }
  }, [session]);

  const fetchRecentSearches = async () => {
    try {
      const response = await fetch(`/api/v1/search/history?savedOnly=false&size=${maxItems}`, {
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
      });
      if (response.ok) {
        const data = await response.json();
        setRecentSearches(data.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch recent searches:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffHours = Math.ceil(diffTime / (1000 * 60 * 60));

    if (diffHours < 1) return 'Just now';
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffHours < 48) return 'Yesterday';
    return date.toLocaleDateString();
  };

  if (loading) {
    return (
      <div className={className}>
        <div className="animate-pulse space-y-2">
          {[...Array(3)].map((_, i) => (
            <div key={i} className="h-12 bg-gray-200 rounded-lg"></div>
          ))}
        </div>
      </div>
    );
  }

  if (recentSearches.length === 0) {
    return null;
  }

  return (
    <div className={`space-y-2 ${className}`}>
      <div className="flex items-center gap-2 text-sm text-gray-600 mb-3">
        <Clock className="h-4 w-4" />
        <span>Recent Searches</span>
      </div>
      
      {recentSearches.map((item) => (
        <Card 
          key={item.id}
          className="p-3 hover:shadow-md transition-shadow cursor-pointer border-l-2 border-l-blue-400"
          onClick={() => onSearchSelect?.(item)}
        >
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                {item.isSaved ? (
                  <Bookmark className="h-3 w-3 text-yellow-500 fill-current flex-shrink-0" />
                ) : (
                  <Search className="h-3 w-3 text-gray-400 flex-shrink-0" />
                )}
                <span className="text-sm font-medium truncate">{item.displayText}</span>
                {item.searchCount > 1 && (
                  <span className="text-xs bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded">
                    {item.searchCount}x
                  </span>
                )}
              </div>
              
              <div className="text-xs text-gray-500 space-y-0.5">
                {item.searchSummary.location && (
                  <div>📍 {item.searchSummary.location}</div>
                )}
                {item.searchSummary.priceRange && (
                  <div>💰 {item.searchSummary.priceRange}</div>
                )}
                {item.searchSummary.totalFilters > 0 && (
                  <div>🔍 {item.searchSummary.totalFilters} filters</div>
                )}
              </div>
            </div>
            
            <div className="text-xs text-gray-400 ml-2 flex-shrink-0">
              {formatDate(item.lastUsedAt)}
            </div>
          </div>
        </Card>
      ))}
      
      <Button 
        variant="ghost" 
        size="sm" 
        className="w-full text-blue-600 hover:text-blue-700 hover:bg-blue-50"
        onClick={() => {
          // This could open a full search history modal/page
          console.log('View all search history');
        }}
      >
        View all search history
      </Button>
    </div>
  );
}