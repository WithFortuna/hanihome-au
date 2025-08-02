'use client';

import React, { useState } from 'react';
import { useFormContext } from 'react-hook-form';
import { PropertyFormData, PROPERTY_OPTIONS } from '@/lib/types/property';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { X, Plus } from 'lucide-react';

export function PropertyOptionsStep() {
  const { register, setValue, watch, formState: { errors } } = useFormContext<PropertyFormData>();
  const [customOption, setCustomOption] = useState('');
  
  const watchedOptions = watch('options') || [];
  const watchedParkingAvailable = watch('parkingAvailable');
  const watchedPetAllowed = watch('petAllowed');
  const watchedFurnished = watch('furnished');
  const watchedShortTermAvailable = watch('shortTermAvailable');

  const handleOptionToggle = (option: string) => {
    const currentOptions = watchedOptions;
    const isSelected = currentOptions.includes(option);
    
    if (isSelected) {
      setValue('options', currentOptions.filter(o => o !== option));
    } else {
      setValue('options', [...currentOptions, option]);
    }
  };

  const handleAddCustomOption = () => {
    if (customOption.trim() && !watchedOptions.includes(customOption.trim())) {
      setValue('options', [...watchedOptions, customOption.trim()]);
      setCustomOption('');
    }
  };

  const handleRemoveOption = (optionToRemove: string) => {
    setValue('options', watchedOptions.filter(option => option !== optionToRemove));
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddCustomOption();
    }
  };

  // Group options by category
  const securityOptions = PROPERTY_OPTIONS.slice(0, 5);
  const amenityOptions = PROPERTY_OPTIONS.slice(5, 15);
  const locationOptions = PROPERTY_OPTIONS.slice(15, 22);
  const otherOptions = PROPERTY_OPTIONS.slice(22);

  const OptionCategory = ({ title, options }: { title: string; options: string[] }) => (
    <div className="space-y-3">
      <h4 className="font-medium text-gray-700">{title}</h4>
      <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
        {options.map((option) => {
          const isSelected = watchedOptions.includes(option);
          return (
            <button
              key={option}
              type="button"
              onClick={() => handleOptionToggle(option)}
              className={`
                p-3 text-sm rounded-lg border transition-colors text-left
                ${isSelected 
                  ? 'bg-blue-50 border-blue-300 text-blue-700' 
                  : 'bg-white border-gray-200 text-gray-700 hover:bg-gray-50'
                }
              `}
            >
              {option}
            </button>
          );
        })}
      </div>
    </div>
  );

  return (
    <div className="space-y-8">
      {/* Basic Features */}
      <div className="space-y-4">
        <h4 className="text-lg font-semibold text-gray-800">기본 시설</h4>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
            <input
              type="checkbox"
              {...register('parkingAvailable')}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded"
            />
            <span className="text-sm font-medium">주차 가능</span>
          </label>

          <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
            <input
              type="checkbox"
              {...register('petAllowed')}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded"
            />
            <span className="text-sm font-medium">반려동물 허용</span>
          </label>

          <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
            <input
              type="checkbox"
              {...register('furnished')}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded"
            />
            <span className="text-sm font-medium">가구 포함</span>
          </label>

          <label className="flex items-center space-x-3 p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
            <input
              type="checkbox"
              {...register('shortTermAvailable')}
              className="w-4 h-4 text-blue-600 border-gray-300 rounded"
            />
            <span className="text-sm font-medium">단기 임대 가능</span>
          </label>
        </div>
      </div>

      {/* Additional Options */}
      <div className="space-y-6">
        <h4 className="text-lg font-semibold text-gray-800">추가 옵션</h4>
        
        <OptionCategory title="보안 시설" options={securityOptions} />
        <OptionCategory title="편의 시설" options={amenityOptions} />
        <OptionCategory title="주변 환경" options={locationOptions} />
        <OptionCategory title="기타" options={otherOptions} />
      </div>

      {/* Custom Option Input */}
      <div className="space-y-3">
        <h4 className="font-medium text-gray-700">직접 추가</h4>
        <div className="flex gap-2">
          <Input
            value={customOption}
            onChange={(e) => setCustomOption(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="옵션을 직접 입력하세요"
            className="flex-1"
          />
          <Button
            type="button"
            onClick={handleAddCustomOption}
            disabled={!customOption.trim()}
            variant="outline"
          >
            <Plus className="w-4 h-4" />
          </Button>
        </div>
      </div>

      {/* Selected Options Preview */}
      {watchedOptions.length > 0 && (
        <div className="space-y-3">
          <h4 className="font-medium text-gray-700">선택된 옵션 ({watchedOptions.length}개)</h4>
          <div className="flex flex-wrap gap-2">
            {watchedOptions.map((option) => (
              <Badge
                key={option}
                variant="secondary"
                className="flex items-center gap-1 px-3 py-1"
              >
                {option}
                <button
                  type="button"
                  onClick={() => handleRemoveOption(option)}
                  className="ml-1 hover:text-red-500"
                >
                  <X className="w-3 h-3" />
                </button>
              </Badge>
            ))}
          </div>
        </div>
      )}

      {/* Feature Summary */}
      {(watchedParkingAvailable || watchedPetAllowed || watchedFurnished || watchedShortTermAvailable) && (
        <div className="mt-6 p-4 bg-green-50 rounded-lg border">
          <h4 className="font-medium text-green-800 mb-2">선택된 기본 시설</h4>
          <div className="flex flex-wrap gap-2">
            {watchedParkingAvailable && (
              <Badge variant="secondary" className="bg-green-100 text-green-800">주차 가능</Badge>
            )}
            {watchedPetAllowed && (
              <Badge variant="secondary" className="bg-green-100 text-green-800">반려동물 허용</Badge>
            )}
            {watchedFurnished && (
              <Badge variant="secondary" className="bg-green-100 text-green-800">가구 포함</Badge>
            )}
            {watchedShortTermAvailable && (
              <Badge variant="secondary" className="bg-green-100 text-green-800">단기 임대 가능</Badge>
            )}
          </div>
        </div>
      )}
    </div>
  );
}