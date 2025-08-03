'use client';

import React, { useState, useCallback, useMemo } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Checkbox } from '@/components/ui/checkbox';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog';
import { 
  Search, 
  Plus, 
  X, 
  Sparkles, 
  DollarSign, 
  Tag,
  ChevronDown,
  ChevronUp,
  Filter
} from 'lucide-react';
import { 
  PROPERTY_OPTIONS, 
  PROPERTY_OPTION_CATEGORIES,
  PropertyOption,
  PropertyOptionWithPrice,
  getOptionsByCategory,
  getPopularOptions,
  getPricingOptions,
  searchOptions
} from '@/lib/types/property-options';

export interface SelectedOption {
  id: string;
  monthlyFee?: number;
  depositFee?: number;
  customNote?: string;
}

interface EnhancedPropertyOptionsProps {
  selectedOptions: SelectedOption[];
  onOptionsChange: (options: SelectedOption[]) => void;
  showPricing?: boolean;
  allowCustomOptions?: boolean;
  maxSelections?: number;
  className?: string;
}

export function EnhancedPropertyOptions({
  selectedOptions,
  onOptionsChange,
  showPricing = true,
  allowCustomOptions = true,
  maxSelections,
  className = '',
}: EnhancedPropertyOptionsProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeCategory, setActiveCategory] = useState<string>('all');
  const [showPopularOnly, setShowPopularOnly] = useState(false);
  const [showPricingOnly, setShowPricingOnly] = useState(false);
  const [customOptions, setCustomOptions] = useState<PropertyOption[]>([]);
  const [newCustomOption, setNewCustomOption] = useState({ label: '', description: '' });
  const [expandedCategories, setExpandedCategories] = useState<string[]>(['security', 'appliances']);
  const [optionPricing, setOptionPricing] = useState<Record<string, { monthly: number; deposit: number }>>({});

  // Combine default and custom options
  const allOptions = useMemo(() => [...PROPERTY_OPTIONS, ...customOptions], [customOptions]);

  // Filter options based on search and category
  const filteredOptions = useMemo(() => {
    let options = allOptions;

    // Search filter
    if (searchQuery) {
      options = searchOptions(searchQuery);
    }

    // Category filter
    if (activeCategory !== 'all') {
      options = options.filter(option => option.category === activeCategory);
    }

    // Popular filter
    if (showPopularOnly) {
      options = options.filter(option => option.isPopular);
    }

    // Pricing filter
    if (showPricingOnly) {
      options = options.filter(option => option.hasPricing);
    }

    return options;
  }, [allOptions, searchQuery, activeCategory, showPopularOnly, showPricingOnly]);

  // Group options by category
  const optionsByCategory = useMemo(() => {
    const categoryGroups = getOptionsByCategory();
    
    return categoryGroups.map(category => ({
      ...category,
      options: filteredOptions.filter(option => option.category === category.id)
    })).filter(category => category.options.length > 0);
  }, [filteredOptions]);

  // Check if option is selected
  const isOptionSelected = useCallback((optionId: string) => {
    return selectedOptions.some(selected => selected.id === optionId);
  }, [selectedOptions]);

  // Get selected option data
  const getSelectedOption = useCallback((optionId: string) => {
    return selectedOptions.find(selected => selected.id === optionId);
  }, [selectedOptions]);

  // Toggle option selection
  const toggleOption = useCallback((option: PropertyOption) => {
    const isSelected = isOptionSelected(option.id);
    
    if (isSelected) {
      // Remove option
      onOptionsChange(selectedOptions.filter(selected => selected.id !== option.id));
    } else {
      // Add option
      if (maxSelections && selectedOptions.length >= maxSelections) {
        alert(`최대 ${maxSelections}개까지 선택 가능합니다.`);
        return;
      }
      
      const newOption: SelectedOption = {
        id: option.id,
        ...(showPricing && option.hasPricing && optionPricing[option.id] && {
          monthlyFee: optionPricing[option.id].monthly,
          depositFee: optionPricing[option.id].deposit,
        })
      };
      
      onOptionsChange([...selectedOptions, newOption]);
    }
  }, [selectedOptions, onOptionsChange, isOptionSelected, maxSelections, showPricing, optionPricing]);

  // Update option pricing
  const updateOptionPricing = useCallback((optionId: string, type: 'monthly' | 'deposit', value: number) => {
    setOptionPricing(prev => ({
      ...prev,
      [optionId]: {
        ...prev[optionId],
        [type]: value,
      }
    }));

    // Update selected option if it exists
    const selectedOption = getSelectedOption(optionId);
    if (selectedOption) {
      const updatedOptions = selectedOptions.map(option => 
        option.id === optionId 
          ? { ...option, [type === 'monthly' ? 'monthlyFee' : 'depositFee']: value }
          : option
      );
      onOptionsChange(updatedOptions);
    }
  }, [selectedOptions, onOptionsChange, getSelectedOption]);

  // Add custom option
  const addCustomOption = useCallback(() => {
    if (!newCustomOption.label.trim()) return;

    const customOption: PropertyOption = {
      id: `custom_${Date.now()}`,
      label: newCustomOption.label,
      category: 'policy',
      icon: Tag,
      description: newCustomOption.description,
      tags: [newCustomOption.label.toLowerCase()],
    };

    setCustomOptions(prev => [...prev, customOption]);
    setNewCustomOption({ label: '', description: '' });
  }, [newCustomOption]);

  // Remove custom option
  const removeCustomOption = useCallback((optionId: string) => {
    setCustomOptions(prev => prev.filter(option => option.id !== optionId));
    onOptionsChange(selectedOptions.filter(selected => selected.id !== optionId));
  }, [selectedOptions, onOptionsChange]);

  // Toggle category expansion
  const toggleCategory = useCallback((categoryId: string) => {
    setExpandedCategories(prev => 
      prev.includes(categoryId)
        ? prev.filter(id => id !== categoryId)
        : [...prev, categoryId]
    );
  }, []);

  // Calculate total costs
  const totalCosts = useMemo(() => {
    const monthly = selectedOptions.reduce((sum, option) => sum + (option.monthlyFee || 0), 0);
    const deposit = selectedOptions.reduce((sum, option) => sum + (option.depositFee || 0), 0);
    return { monthly, deposit };
  }, [selectedOptions]);

  const popularOptions = getPopularOptions();

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Header with search and filters */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-semibold">매물 옵션 선택</h3>
          <div className="flex items-center gap-2">
            <Badge variant="outline">
              {selectedOptions.length}개 선택됨
            </Badge>
            {maxSelections && (
              <Badge variant="secondary">
                최대 {maxSelections}개
              </Badge>
            )}
          </div>
        </div>

        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
          <Input
            placeholder="옵션 검색..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>

        {/* Filters */}
        <div className="flex flex-wrap gap-2">
          <Button
            variant={showPopularOnly ? "default" : "outline"}
            size="sm"
            onClick={() => setShowPopularOnly(!showPopularOnly)}
            className="flex items-center gap-1"
          >
            <Sparkles className="w-3 h-3" />
            인기 옵션
          </Button>
          
          {showPricing && (
            <Button
              variant={showPricingOnly ? "default" : "outline"}
              size="sm"
              onClick={() => setShowPricingOnly(!showPricingOnly)}
              className="flex items-center gap-1"
            >
              <DollarSign className="w-3 h-3" />
              가격 설정 가능
            </Button>
          )}
        </div>
      </div>

      <Tabs value={activeCategory} onValueChange={setActiveCategory}>
        <TabsList className="grid grid-cols-4 lg:grid-cols-7">
          <TabsTrigger value="all">전체</TabsTrigger>
          {PROPERTY_OPTION_CATEGORIES.map(category => (
            <TabsTrigger key={category.id} value={category.id} className="text-xs">
              {category.label}
            </TabsTrigger>
          ))}
        </TabsList>

        <TabsContent value="all" className="space-y-6">
          {/* Popular Options */}
          {!searchQuery && !showPricingOnly && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Sparkles className="w-5 h-5 text-yellow-500" />
                  인기 옵션
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                  {popularOptions.slice(0, 8).map(option => {
                    const isSelected = isOptionSelected(option.id);
                    const Icon = option.icon;
                    
                    return (
                      <div
                        key={option.id}
                        className={`
                          flex items-center p-3 border rounded-lg cursor-pointer transition-all
                          ${isSelected 
                            ? 'bg-blue-50 border-blue-300 text-blue-700' 
                            : 'hover:bg-gray-50 border-gray-200'
                          }
                        `}
                        onClick={() => toggleOption(option)}
                      >
                        <Checkbox
                          checked={isSelected}
                          onChange={() => {}}
                          className="mr-3"
                        />
                        <Icon className="w-4 h-4 mr-2" />
                        <span className="text-sm font-medium">{option.label}</span>
                      </div>
                    );
                  })}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Options by Category */}
          {optionsByCategory.map(category => {
            const isExpanded = expandedCategories.includes(category.id);
            const CategoryIcon = category.icon;
            
            return (
              <Card key={category.id}>
                <CardHeader 
                  className="cursor-pointer" 
                  onClick={() => toggleCategory(category.id)}
                >
                  <CardTitle className="flex items-center justify-between text-base">
                    <div className="flex items-center gap-2">
                      <CategoryIcon className="w-5 h-5" />
                      {category.label}
                      <Badge variant="outline">
                        {category.options.length}개
                      </Badge>
                    </div>
                    {isExpanded ? (
                      <ChevronUp className="w-4 h-4" />
                    ) : (
                      <ChevronDown className="w-4 h-4" />
                    )}
                  </CardTitle>
                </CardHeader>
                
                {isExpanded && (
                  <CardContent>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      {category.options.map(option => {
                        const isSelected = isOptionSelected(option.id);
                        const selectedOption = getSelectedOption(option.id);
                        const Icon = option.icon;
                        
                        return (
                          <div
                            key={option.id}
                            className={`
                              border rounded-lg p-4 transition-all
                              ${isSelected 
                                ? 'bg-blue-50 border-blue-300' 
                                : 'border-gray-200 hover:border-gray-300'
                              }
                            `}
                          >
                            <div 
                              className="flex items-start gap-3 cursor-pointer"
                              onClick={() => toggleOption(option)}
                            >
                              <Checkbox
                                checked={isSelected}
                                onChange={() => {}}
                                className="mt-1"
                              />
                              <div className="flex-1">
                                <div className="flex items-center gap-2 mb-1">
                                  <Icon className="w-4 h-4" />
                                  <span className="font-medium">{option.label}</span>
                                  {option.isPopular && (
                                    <Badge variant="secondary" className="text-xs bg-yellow-100 text-yellow-800">
                                      인기
                                    </Badge>
                                  )}
                                </div>
                                {option.description && (
                                  <p className="text-sm text-gray-600">{option.description}</p>
                                )}
                              </div>
                            </div>

                            {/* Pricing inputs */}
                            {showPricing && option.hasPricing && isSelected && (
                              <div className="mt-3 p-3 bg-gray-50 rounded space-y-2">
                                <div className="grid grid-cols-2 gap-2">
                                  <div>
                                    <Label className="text-xs">월 추가비용</Label>
                                    <Input
                                      type="number"
                                      placeholder="0"
                                      value={optionPricing[option.id]?.monthly || ''}
                                      onChange={(e) => updateOptionPricing(option.id, 'monthly', parseInt(e.target.value) || 0)}
                                      className="h-8 text-sm"
                                    />
                                  </div>
                                  <div>
                                    <Label className="text-xs">보증금 추가</Label>
                                    <Input
                                      type="number"
                                      placeholder="0"
                                      value={optionPricing[option.id]?.deposit || ''}
                                      onChange={(e) => updateOptionPricing(option.id, 'deposit', parseInt(e.target.value) || 0)}
                                      className="h-8 text-sm"
                                    />
                                  </div>
                                </div>
                              </div>
                            )}

                            {/* Custom option remove button */}
                            {option.id.startsWith('custom_') && (
                              <Button
                                variant="ghost"
                                size="sm"
                                onClick={(e) => {
                                  e.stopPropagation();
                                  removeCustomOption(option.id);
                                }}
                                className="mt-2 h-6 px-2 text-red-600 hover:text-red-700"
                              >
                                <X className="w-3 h-3 mr-1" />
                                삭제
                              </Button>
                            )}
                          </div>
                        );
                      })}
                    </div>
                  </CardContent>
                )}
              </Card>
            );
          })}

          {/* Add Custom Option */}
          {allowCustomOptions && (
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2 text-base">
                  <Plus className="w-5 h-5" />
                  커스텀 옵션 추가
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-3">
                  <div>
                    <Label>옵션 이름</Label>
                    <Input
                      placeholder="예: 루프탑 이용 가능"
                      value={newCustomOption.label}
                      onChange={(e) => setNewCustomOption(prev => ({ ...prev, label: e.target.value }))}
                    />
                  </div>
                  <div>
                    <Label>설명 (선택사항)</Label>
                    <Input
                      placeholder="옵션에 대한 상세 설명"
                      value={newCustomOption.description}
                      onChange={(e) => setNewCustomOption(prev => ({ ...prev, description: e.target.value }))}
                    />
                  </div>
                  <Button
                    onClick={addCustomOption}
                    disabled={!newCustomOption.label.trim()}
                    className="w-full"
                  >
                    <Plus className="w-4 h-4 mr-2" />
                    옵션 추가
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}
        </TabsContent>

        {/* Individual category tabs */}
        {PROPERTY_OPTION_CATEGORIES.map(category => (
          <TabsContent key={category.id} value={category.id}>
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <category.icon className="w-5 h-5" />
                  {category.label}
                </CardTitle>
                <p className="text-sm text-gray-600">{category.description}</p>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  {filteredOptions
                    .filter(option => option.category === category.id)
                    .map(option => {
                      const isSelected = isOptionSelected(option.id);
                      const Icon = option.icon;
                      
                      return (
                        <div
                          key={option.id}
                          className={`
                            flex items-center p-4 border rounded-lg cursor-pointer transition-all
                            ${isSelected 
                              ? 'bg-blue-50 border-blue-300 text-blue-700' 
                              : 'hover:bg-gray-50 border-gray-200'
                            }
                          `}
                          onClick={() => toggleOption(option)}
                        >
                          <Checkbox
                            checked={isSelected}
                            onChange={() => {}}
                            className="mr-3"
                          />
                          <Icon className="w-5 h-5 mr-3" />
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <span className="font-medium">{option.label}</span>
                              {option.isPopular && (
                                <Badge variant="secondary" className="text-xs">
                                  인기
                                </Badge>
                              )}
                            </div>
                            {option.description && (
                              <p className="text-sm text-gray-600">{option.description}</p>
                            )}
                          </div>
                        </div>
                      );
                    })}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        ))}
      </Tabs>

      {/* Selected Options Summary */}
      {selectedOptions.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center justify-between">
              <span>선택된 옵션 요약</span>
              {showPricing && (totalCosts.monthly > 0 || totalCosts.deposit > 0) && (
                <div className="text-right text-sm">
                  {totalCosts.monthly > 0 && (
                    <div className="text-blue-600 font-medium">
                      월 +{totalCosts.monthly.toLocaleString()}원
                    </div>
                  )}
                  {totalCosts.deposit > 0 && (
                    <div className="text-green-600 font-medium">
                      보증금 +{totalCosts.deposit.toLocaleString()}원
                    </div>
                  )}
                </div>
              )}
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-2">
              {selectedOptions.map(selectedOption => {
                const option = allOptions.find(opt => opt.id === selectedOption.id);
                if (!option) return null;
                
                const Icon = option.icon;
                
                return (
                  <Badge
                    key={selectedOption.id}
                    variant="secondary"
                    className="flex items-center gap-1 px-3 py-1"
                  >
                    <Icon className="w-3 h-3" />
                    {option.label}
                    {showPricing && (selectedOption.monthlyFee || selectedOption.depositFee) && (
                      <span className="text-xs">
                        {selectedOption.monthlyFee && `+${selectedOption.monthlyFee.toLocaleString()}원/월`}
                        {selectedOption.depositFee && ` +${selectedOption.depositFee.toLocaleString()}원`}
                      </span>
                    )}
                    <button
                      onClick={() => toggleOption(option)}
                      className="ml-1 hover:bg-gray-200 rounded-full p-0.5"
                    >
                      <X className="w-3 h-3" />
                    </button>
                  </Badge>
                );
              })}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  );
}