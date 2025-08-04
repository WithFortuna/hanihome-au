'use client';

import React, { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { 
  Clock, 
  Star, 
  Bookmark, 
  Search, 
  Trash2, 
  MoreVertical,
  MapPin,
  DollarSign,
  Home,
  Filter
} from 'lucide-react';

interface SearchHistoryItem {
  id: number;
  userId: number;
  searchName?: string;
  keyword?: string;
  displayText: string;
  isSaved: boolean;
  searchCount: number;
  lastUsedAt: string;
  createdAt: string;
  searchSummary: {
    location?: string;
    priceRange?: string;
    propertyTypes?: string;
    amenities?: string;
    totalFilters: number;
  };
}

interface SearchHistoryStats {
  totalSearches: number;
  savedSearches: number;
  recentSearches: number;
}

interface SearchHistoryManagerProps {
  onSearchSelect?: (searchItem: SearchHistoryItem) => void;
  className?: string;
}

export function SearchHistoryManager({ onSearchSelect, className }: SearchHistoryManagerProps) {
  const { data: session } = useSession();
  const [searchHistory, setSearchHistory] = useState<SearchHistoryItem[]>([]);
  const [savedSearches, setSavedSearches] = useState<SearchHistoryItem[]>([]);
  const [frequentSearches, setFrequentSearches] = useState<SearchHistoryItem[]>([]);
  const [stats, setStats] = useState<SearchHistoryStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [selectedTab, setSelectedTab] = useState('recent');
  const [saveSearchId, setSaveSearchId] = useState<number | null>(null);
  const [saveSearchName, setSaveSearchName] = useState('');

  useEffect(() => {
    if (session?.accessToken) {
      fetchSearchHistory();
      fetchSavedSearches();
      fetchFrequentSearches();
      fetchStats();
    }
  }, [session]);

  const fetchSearchHistory = async () => {
    try {
      const response = await fetch('/api/v1/search/history?savedOnly=false&size=20', {
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
      });
      if (response.ok) {
        const data = await response.json();
        setSearchHistory(data.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch search history:', error);
    }
  };

  const fetchSavedSearches = async () => {
    try {
      const response = await fetch('/api/v1/search/history?savedOnly=true&size=50', {
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
      });
      if (response.ok) {
        const data = await response.json();
        setSavedSearches(data.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch saved searches:', error);
    }
  };

  const fetchFrequentSearches = async () => {
    try {
      const response = await fetch('/api/v1/search/history/frequent?limit=10', {
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
      });
      if (response.ok) {
        const data = await response.json();
        setFrequentSearches(data.data || []);
      }
    } catch (error) {
      console.error('Failed to fetch frequent searches:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchStats = async () => {
    try {
      const response = await fetch('/api/v1/search/history/stats', {
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
      });
      if (response.ok) {
        const data = await response.json();
        setStats(data.data);
      }
    } catch (error) {
      console.error('Failed to fetch search stats:', error);
    }
  };

  const handleSaveSearch = async (searchId: number, name: string) => {
    try {
      const response = await fetch(`/api/v1/search/history/${searchId}/save`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ searchName: name }),
      });

      if (response.ok) {
        setSaveSearchId(null);
        setSaveSearchName('');
        fetchSearchHistory();
        fetchSavedSearches();
        fetchStats();
      } else {
        const error = await response.json();
        alert(error.message || 'Failed to save search');
      }
    } catch (error) {
      console.error('Failed to save search:', error);
      alert('Failed to save search');
    }
  };

  const handleDeleteSearch = async (searchId: number) => {
    if (!confirm('Are you sure you want to delete this search?')) return;

    try {
      const response = await fetch(`/api/v1/search/history/${searchId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
        },
      });

      if (response.ok) {
        fetchSearchHistory();
        fetchSavedSearches();
        fetchFrequentSearches();
        fetchStats();
      }
    } catch (error) {
      console.error('Failed to delete search:', error);
    }
  };

  const handleClearHistory = async () => {
    if (!confirm('Are you sure you want to clear all search history? Saved searches will not be affected.')) return;

    try {
      const response = await fetch('/api/v1/search/history/clear', {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
        },
      });

      if (response.ok) {
        fetchSearchHistory();
        fetchFrequentSearches();
        fetchStats();
      }
    } catch (error) {
      console.error('Failed to clear history:', error);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffTime = Math.abs(now.getTime() - date.getTime());
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 1) return 'Today';
    if (diffDays === 2) return 'Yesterday';
    if (diffDays <= 7) return `${diffDays - 1} days ago`;
    return date.toLocaleDateString();
  };

  const SearchItem = ({ item, showSaveOption = false }: { item: SearchHistoryItem; showSaveOption?: boolean }) => (
    <Card className="p-4 hover:shadow-md transition-shadow cursor-pointer border-l-4 border-l-blue-500">
      <div className="flex justify-between items-start">
        <div className="flex-1" onClick={() => onSearchSelect?.(item)}>
          <div className="flex items-center gap-2 mb-2">
            {item.isSaved ? (
              <Bookmark className="h-4 w-4 text-yellow-500 fill-current" />
            ) : (
              <Clock className="h-4 w-4 text-gray-500" />
            )}
            <h3 className="font-semibold text-sm truncate">{item.displayText}</h3>
            {item.searchCount > 1 && (
              <Badge variant="secondary" className="text-xs">
                {item.searchCount}x
              </Badge>
            )}
          </div>

          <div className="space-y-1 text-xs text-gray-600">
            {item.searchSummary.location && (
              <div className="flex items-center gap-1">
                <MapPin className="h-3 w-3" />
                <span>{item.searchSummary.location}</span>
              </div>
            )}
            {item.searchSummary.priceRange && (
              <div className="flex items-center gap-1">
                <DollarSign className="h-3 w-3" />
                <span>{item.searchSummary.priceRange}</span>
              </div>
            )}
            {item.searchSummary.propertyTypes && (
              <div className="flex items-center gap-1">
                <Home className="h-3 w-3" />
                <span>{item.searchSummary.propertyTypes}</span>
              </div>
            )}
            {item.searchSummary.totalFilters > 0 && (
              <div className="flex items-center gap-1">
                <Filter className="h-3 w-3" />
                <span>{item.searchSummary.totalFilters} filters</span>
              </div>
            )}
          </div>

          <div className="text-xs text-gray-400 mt-2">
            {formatDate(item.lastUsedAt)}
          </div>
        </div>

        <div className="flex gap-1 ml-2">
          {showSaveOption && !item.isSaved && (
            <Button
              size="sm"
              variant="ghost"
              onClick={(e) => {
                e.stopPropagation();
                setSaveSearchId(item.id);
              }}
              className="h-8 w-8 p-0"
            >
              <Bookmark className="h-4 w-4" />
            </Button>
          )}
          <Button
            size="sm"
            variant="ghost"
            onClick={(e) => {
              e.stopPropagation();
              handleDeleteSearch(item.id);
            }}
            className="h-8 w-8 p-0 text-red-500 hover:text-red-700"
          >
            <Trash2 className="h-4 w-4" />
          </Button>
        </div>
      </div>

      {saveSearchId === item.id && (
        <div className="mt-3 pt-3 border-t">
          <div className="flex gap-2">
            <Input
              placeholder="Enter search name"
              value={saveSearchName}
              onChange={(e) => setSaveSearchName(e.target.value)}
              className="flex-1"
            />
            <Button
              size="sm"
              onClick={() => handleSaveSearch(item.id, saveSearchName)}
              disabled={!saveSearchName.trim()}
            >
              Save
            </Button>
            <Button
              size="sm"
              variant="outline"
              onClick={() => {
                setSaveSearchId(null);
                setSaveSearchName('');
              }}
            >
              Cancel
            </Button>
          </div>
        </div>
      )}
    </Card>
  );

  if (loading) {
    return (
      <div className={`space-y-4 ${className}`}>
        <div className="animate-pulse space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-20 bg-gray-200 rounded-lg"></div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className={`space-y-4 ${className}`}>
      {stats && (
        <div className="grid grid-cols-3 gap-4 mb-6">
          <Card className="p-4 text-center">
            <div className="text-2xl font-bold text-blue-600">{stats.totalSearches}</div>
            <div className="text-sm text-gray-600">Total Searches</div>
          </Card>
          <Card className="p-4 text-center">
            <div className="text-2xl font-bold text-yellow-600">{stats.savedSearches}</div>
            <div className="text-sm text-gray-600">Saved Searches</div>
          </Card>
          <Card className="p-4 text-center">
            <div className="text-2xl font-bold text-green-600">{stats.recentSearches}</div>
            <div className="text-sm text-gray-600">Recent Searches</div>
          </Card>
        </div>
      )}

      <Tabs value={selectedTab} onValueChange={setSelectedTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="recent">Recent</TabsTrigger>
          <TabsTrigger value="saved">Saved</TabsTrigger>
          <TabsTrigger value="frequent">Frequent</TabsTrigger>
        </TabsList>

        <TabsContent value="recent" className="space-y-3">
          <div className="flex justify-between items-center">
            <h2 className="text-lg font-semibold">Recent Searches</h2>
            {searchHistory.length > 0 && (
              <Button variant="outline" size="sm" onClick={handleClearHistory}>
                Clear All
              </Button>
            )}
          </div>
          {searchHistory.length === 0 ? (
            <Card className="p-8 text-center text-gray-500">
              <Search className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No recent searches</p>
              <p className="text-sm">Your recent property searches will appear here</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {searchHistory.map((item) => (
                <SearchItem key={item.id} item={item} showSaveOption />
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="saved" className="space-y-3">
          <h2 className="text-lg font-semibold">Saved Searches</h2>
          {savedSearches.length === 0 ? (
            <Card className="p-8 text-center text-gray-500">
              <Bookmark className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No saved searches</p>
              <p className="text-sm">Save your favorite search criteria for quick access</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {savedSearches.map((item) => (
                <SearchItem key={item.id} item={item} />
              ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="frequent" className="space-y-3">
          <h2 className="text-lg font-semibold">Frequent Searches</h2>
          {frequentSearches.length === 0 ? (
            <Card className="p-8 text-center text-gray-500">
              <Star className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No frequent searches</p>
              <p className="text-sm">Searches you perform often will appear here</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {frequentSearches.map((item) => (
                <SearchItem key={item.id} item={item} showSaveOption />
              ))}
            </div>
          )}
        </TabsContent>
      </Tabs>
    </div>
  );
}