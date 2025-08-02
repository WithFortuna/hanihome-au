'use client';

import React, { useState, useMemo } from 'react';
import { Search, Check } from 'lucide-react';
import { Input } from './input';
import { Label } from './label';
import { Badge } from './badge';
import { cn } from '@/lib/utils';

export interface CheckboxOption {
  value: string;
  label: string;
  description?: string;
  category?: string;
  disabled?: boolean;
}

interface CheckboxGroupProps {
  options: CheckboxOption[];
  selectedValues: string[];
  onChange: (selectedValues: string[]) => void;
  title?: string;
  description?: string;
  maxSelections?: number;
  enableSearch?: boolean;
  enableCategories?: boolean;
  columns?: 1 | 2 | 3 | 4;
  className?: string;
  variant?: 'default' | 'card' | 'compact';
  showSelectedCount?: boolean;
}

export function CheckboxGroup({
  options,
  selectedValues,
  onChange,
  title,
  description,
  maxSelections,
  enableSearch = false,
  enableCategories = false,
  columns = 2,
  className,
  variant = 'default',
  showSelectedCount = true,
}: CheckboxGroupProps) {
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedCategories, setExpandedCategories] = useState<Set<string>>(new Set());

  // Filter options based on search term
  const filteredOptions = useMemo(() => {
    if (!searchTerm) return options;
    return options.filter(option =>
      option.label.toLowerCase().includes(searchTerm.toLowerCase()) ||
      option.description?.toLowerCase().includes(searchTerm.toLowerCase())
    );
  }, [options, searchTerm]);

  // Group options by category
  const categorizedOptions = useMemo(() => {
    if (!enableCategories) return { '': filteredOptions };
    
    return filteredOptions.reduce((acc, option) => {
      const category = option.category || '기타';
      if (!acc[category]) acc[category] = [];
      acc[category].push(option);
      return acc;
    }, {} as Record<string, CheckboxOption[]>);
  }, [filteredOptions, enableCategories]);

  const handleOptionToggle = (value: string) => {
    const isSelected = selectedValues.includes(value);
    
    if (isSelected) {
      onChange(selectedValues.filter(v => v !== value));
    } else {
      if (maxSelections && selectedValues.length >= maxSelections) {
        return; // Don't allow more selections
      }
      onChange([...selectedValues, value]);
    }
  };

  const handleCategoryToggle = (category: string) => {
    const newExpanded = new Set(expandedCategories);
    if (newExpanded.has(category)) {
      newExpanded.delete(category);
    } else {
      newExpanded.add(category);
    }
    setExpandedCategories(newExpanded);
  };

  const getGridColumns = () => {
    switch (columns) {
      case 1: return 'grid-cols-1';
      case 2: return 'grid-cols-1 md:grid-cols-2';
      case 3: return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-3';
      case 4: return 'grid-cols-1 md:grid-cols-2 lg:grid-cols-4';
      default: return 'grid-cols-1 md:grid-cols-2';
    }
  };

  const CheckboxItem = ({ option }: { option: CheckboxOption }) => {
    const isSelected = selectedValues.includes(option.value);
    const isDisabled = option.disabled || (maxSelections && selectedValues.length >= maxSelections && !isSelected);
    
    if (variant === 'card') {
      return (
        <label
          className={cn(
            'flex flex-col p-4 border rounded-lg cursor-pointer transition-all',
            isSelected
              ? 'border-blue-500 bg-blue-50 ring-2 ring-blue-200'
              : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50',
            isDisabled && 'opacity-50 cursor-not-allowed'
          )}
        >
          <div className="flex items-start space-x-3">
            <div className="relative">
              <input
                type="checkbox"
                checked={isSelected}
                onChange={() => !isDisabled && handleOptionToggle(option.value)}
                disabled={isDisabled}
                className="sr-only"
              />
              <div
                className={cn(
                  'w-5 h-5 border-2 rounded flex items-center justify-center transition-colors',
                  isSelected
                    ? 'border-blue-500 bg-blue-500'
                    : 'border-gray-300',
                  isDisabled && 'opacity-50'
                )}
              >
                {isSelected && <Check className="w-3 h-3 text-white" />}
              </div>
            </div>
            <div className="flex-1">
              <p className="font-medium text-gray-900">{option.label}</p>
              {option.description && (
                <p className="text-sm text-gray-600 mt-1">{option.description}</p>
              )}
            </div>
          </div>
        </label>
      );
    }

    if (variant === 'compact') {
      return (
        <label
          className={cn(
            'flex items-center space-x-2 p-2 rounded cursor-pointer transition-colors',
            isSelected ? 'bg-blue-50 text-blue-700' : 'hover:bg-gray-50',
            isDisabled && 'opacity-50 cursor-not-allowed'
          )}
        >
          <input
            type="checkbox"
            checked={isSelected}
            onChange={() => !isDisabled && handleOptionToggle(option.value)}
            disabled={isDisabled}
            className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
          />
          <span className="text-sm font-medium">{option.label}</span>
        </label>
      );
    }

    // Default variant
    return (
      <label
        className={cn(
          'flex items-center space-x-3 p-3 border rounded-lg cursor-pointer transition-all',
          isSelected
            ? 'border-blue-300 bg-blue-50'
            : 'border-gray-200 hover:border-gray-300 hover:bg-gray-50',
          isDisabled && 'opacity-50 cursor-not-allowed'
        )}
      >
        <input
          type="checkbox"
          checked={isSelected}
          onChange={() => !isDisabled && handleOptionToggle(option.value)}
          disabled={isDisabled}
          className="w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
        />
        <div className="flex-1">
          <p className="font-medium text-gray-900">{option.label}</p>
          {option.description && (
            <p className="text-sm text-gray-600">{option.description}</p>
          )}
        </div>
      </label>
    );
  };

  return (
    <div className={cn('space-y-4', className)}>
      {/* Header */}
      {(title || description) && (
        <div>
          {title && (
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold text-gray-900">{title}</h3>
              {showSelectedCount && (
                <Badge variant="secondary">
                  {selectedValues.length}
                  {maxSelections && ` / ${maxSelections}`}
                </Badge>
              )}
            </div>
          )}
          {description && (
            <p className="text-sm text-gray-600 mt-1">{description}</p>
          )}
        </div>
      )}

      {/* Search */}
      {enableSearch && (
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
          <Input
            type="text"
            placeholder="옵션 검색..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
      )}

      {/* Max selections warning */}
      {maxSelections && selectedValues.length >= maxSelections && (
        <div className="bg-amber-50 border border-amber-200 rounded-lg p-3">
          <p className="text-sm text-amber-800">
            최대 {maxSelections}개까지 선택할 수 있습니다.
          </p>
        </div>
      )}

      {/* Options */}
      <div className="space-y-6">
        {Object.entries(categorizedOptions).map(([category, categoryOptions]) => (
          <div key={category}>
            {enableCategories && category && (
              <button
                onClick={() => handleCategoryToggle(category)}
                className="flex items-center justify-between w-full p-2 text-left font-medium text-gray-700 bg-gray-50 rounded-lg hover:bg-gray-100 mb-3"
              >
                <span>{category}</span>
                <div className="flex items-center space-x-2">
                  <Badge variant="secondary" className="text-xs">
                    {categoryOptions.filter(opt => selectedValues.includes(opt.value)).length} / {categoryOptions.length}
                  </Badge>
                  <span className="text-gray-400">
                    {expandedCategories.has(category) ? '−' : '+'}
                  </span>
                </div>
              </button>
            )}
            
            {(!enableCategories || !category || expandedCategories.has(category)) && (
              <div className={cn('grid gap-3', getGridColumns())}>
                {categoryOptions.map((option) => (
                  <CheckboxItem key={option.value} option={option} />
                ))}
              </div>
            )}
          </div>
        ))}
      </div>

      {/* No results */}
      {filteredOptions.length === 0 && (
        <div className="text-center py-8 text-gray-500">
          <p>검색 결과가 없습니다.</p>
        </div>
      )}
    </div>
  );
}