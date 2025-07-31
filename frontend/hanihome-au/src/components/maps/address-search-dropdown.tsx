'use client';

import React, { useRef, useEffect } from 'react';
import { formatAddress } from '@/lib/maps/places';

interface AddressSearchDropdownProps {
  predictions: google.maps.places.AutocompletePrediction[];
  isLoading: boolean;
  error: string | null;
  onSelect: (prediction: google.maps.places.AutocompletePrediction) => void;
  isOpen: boolean;
  onClose: () => void;
  className?: string;
}

export function AddressSearchDropdown({
  predictions,
  isLoading,
  error,
  onSelect,
  isOpen,
  onClose,
  className = '',
}: AddressSearchDropdownProps) {
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdown when clicking outside
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        onClose();
      }
    }

    if (isOpen) {
      document.addEventListener('mousedown', handleClickOutside);
      return () => document.removeEventListener('mousedown', handleClickOutside);
    }
  }, [isOpen, onClose]);

  // Handle keyboard navigation
  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if (!isOpen) return;

      if (event.key === 'Escape') {
        onClose();
      }
    }

    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  const formatPredictionText = (prediction: google.maps.places.AutocompletePrediction) => {
    // Extract main text and secondary text for better display
    const mainText = prediction.structured_formatting?.main_text || '';
    const secondaryText = prediction.structured_formatting?.secondary_text || '';
    
    return {
      main: mainText,
      secondary: secondaryText,
      full: prediction.description,
    };
  };

  return (
    <div
      ref={dropdownRef}
      className={`absolute z-50 w-full bg-white border border-gray-300 rounded-md shadow-lg max-h-60 overflow-y-auto ${className}`}
      style={{ top: '100%' }}
    >
      {isLoading && (
        <div className="px-4 py-3 text-sm text-gray-500 flex items-center">
          <svg className="animate-spin h-4 w-4 mr-2" viewBox="0 0 24 24">
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
              fill="none"
            />
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 0 1 16 0"
            />
          </svg>
          Searching addresses...
        </div>
      )}

      {error && (
        <div className="px-4 py-3 text-sm text-red-600 bg-red-50 border-b border-red-200">
          <div className="flex items-center">
            <svg className="h-4 w-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
              <path
                fillRule="evenodd"
                d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z"
                clipRule="evenodd"
              />
            </svg>
            {error}
          </div>
        </div>
      )}

      {!isLoading && !error && predictions.length === 0 && (
        <div className="px-4 py-3 text-sm text-gray-500">
          No addresses found. Try a different search term.
        </div>
      )}

      {predictions.map((prediction) => {
        const formatted = formatPredictionText(prediction);
        
        return (
          <button
            key={prediction.place_id}
            className="w-full px-4 py-3 text-left hover:bg-gray-50 focus:bg-gray-50 focus:outline-none border-b border-gray-100 last:border-b-0 transition-colors"
            onClick={() => onSelect(prediction)}
          >
            <div className="flex items-start">
              <svg
                className="h-4 w-4 text-gray-400 mt-0.5 mr-3 flex-shrink-0"
                fill="currentColor"
                viewBox="0 0 20 20"
              >
                <path
                  fillRule="evenodd"
                  d="M5.05 4.05a7 7 0 119.9 9.9L10 18.9l-4.95-4.95a7 7 0 010-9.9zM10 11a2 2 0 100-4 2 2 0 000 4z"
                  clipRule="evenodd"
                />
              </svg>
              <div className="flex-1 min-w-0">
                <div className="text-sm font-medium text-gray-900 truncate">
                  {formatted.main}
                </div>
                {formatted.secondary && (
                  <div className="text-xs text-gray-500 truncate mt-0.5">
                    {formatted.secondary}
                  </div>
                )}
              </div>
            </div>
          </button>
        );
      })}

      {predictions.length > 0 && (
        <div className="px-4 py-2 text-xs text-gray-400 bg-gray-50 border-t border-gray-200">
          <div className="flex items-center justify-center">
            <span>Powered by</span>
            <svg className="h-3 ml-1" viewBox="0 0 66 16" fill="none">
              <path
                d="M10.48 7.52h4.8v1.28h-4.8v4.16h5.36v1.28H9.2V2.56h6.64v1.28h-5.36v3.68zm8.8-5.12c1.04 0 1.84.24 2.4.72.56.48.84 1.16.84 2.04v6.08h-1.28V5.28c0-.64-.16-1.12-.48-1.44-.32-.32-.8-.48-1.44-.48-.64 0-1.12.16-1.44.48-.32.32-.48.8-.48 1.44v6.96h-1.28V5.16c0-.88.28-1.56.84-2.04.56-.48 1.36-.72 2.4-.72h-.08zm6.32 0c1.04 0 1.84.24 2.4.72.56.48.84 1.16.84 2.04v6.08h-1.28V5.28c0-.64-.16-1.12-.48-1.44-.32-.32-.8-.48-1.44-.48-.64 0-1.12.16-1.44.48-.32.32-.48.8-.48 1.44v6.96h-1.28V5.16c0-.88.28-1.56.84-2.04.56-.48 1.36-.72 2.4-.72h-.08zm6.32 8.64c-.88 0-1.6-.28-2.16-.84-.56-.56-.84-1.28-.84-2.16V5.16c0-.88.28-1.6.84-2.16.56-.56 1.28-.84 2.16-.84.88 0 1.6.28 2.16.84.56.56.84 1.28.84 2.16v2.88c0 .88-.28 1.6-.84 2.16-.56.56-1.28.84-2.16.84zm0-1.28c.48 0 .88-.16 1.2-.48.32-.32.48-.72.48-1.2V5.16c0-.48-.16-.88-.48-1.2-.32-.32-.72-.48-1.2-.48-.48 0-.88.16-1.2.48-.32.32-.48.72-.48 1.2v2.88c0 .48.16.88.48 1.2.32.32.72.48 1.2.48zm8.96-7.52h1.28v8h-1.28l-4.48-6.24v6.24h-1.28v-8h1.28l4.48 6.24V2.4z"
                fill="#4285F4"
              />
            </svg>
          </div>
        </div>
      )}
    </div>
  );
}