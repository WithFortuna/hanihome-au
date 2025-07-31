'use client';

import React, { useState, useRef, forwardRef } from 'react';
import { usePlacesAutocomplete } from '@/hooks/use-places-autocomplete';
import { AddressSearchDropdown } from './address-search-dropdown';
import { PlaceResult, MapPosition } from '@/lib/maps/types';

interface AddressSearchProps {
  onPlaceSelect: (place: PlaceResult) => void;
  placeholder?: string;
  className?: string;
  disabled?: boolean;
  defaultValue?: string;
  types?: string[];
  location?: MapPosition;
  radius?: number;
  showClearButton?: boolean;
  autoFocus?: boolean;
}

export const AddressSearch = forwardRef<HTMLDivElement, AddressSearchProps>(
  (
    {
      onPlaceSelect,
      placeholder = 'Search for an address...',
      className = '',
      disabled = false,
      defaultValue = '',
      types = ['address'],
      location,
      radius,
      showClearButton = true,
      autoFocus = false,
    },
    ref
  ) => {
    const [isDropdownOpen, setIsDropdownOpen] = useState(false);
    const inputRef = useRef<HTMLInputElement>(null);

    const {
      inputValue,
      setInputValue,
      predictions,
      isLoading,
      error,
      selectedPlace,
      clearSelection,
      selectPrediction,
    } = usePlacesAutocomplete({
      defaultValue,
      types,
      location,
      radius,
    });

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const value = e.target.value;
      setInputValue(value);
      
      // Open dropdown when user types
      if (value.trim() && !isDropdownOpen) {
        setIsDropdownOpen(true);
      }
      
      // Close dropdown when input is cleared
      if (!value.trim() && isDropdownOpen) {
        setIsDropdownOpen(false);
      }
    };

    const handleInputFocus = () => {
      if (inputValue.trim() && predictions.length > 0) {
        setIsDropdownOpen(true);
      }
    };

    const handlePredictionSelect = async (prediction: google.maps.places.AutocompletePrediction) => {
      await selectPrediction(prediction);
      setIsDropdownOpen(false);
      
      // Call the parent's onPlaceSelect if we have a selected place
      // Note: This will be called after selectPrediction updates selectedPlace
      setTimeout(() => {
        if (selectedPlace) {
          onPlaceSelect(selectedPlace);
        }
      }, 0);
    };

    const handleClear = () => {
      clearSelection();
      setIsDropdownOpen(false);
      if (inputRef.current) {
        inputRef.current.focus();
      }
    };

    const handleDropdownClose = () => {
      setIsDropdownOpen(false);
    };

    // Call onPlaceSelect when selectedPlace changes
    React.useEffect(() => {
      if (selectedPlace) {
        onPlaceSelect(selectedPlace);
      }
    }, [selectedPlace, onPlaceSelect]);

    return (
      <div ref={ref} className={`relative ${className}`}>
        <div className="relative">
          <input
            ref={inputRef}
            type="text"
            value={inputValue}
            onChange={handleInputChange}
            onFocus={handleInputFocus}
            placeholder={placeholder}
            disabled={disabled}
            autoFocus={autoFocus}
            autoComplete="off"
            className={`
              w-full px-4 py-2 pr-10 border border-gray-300 rounded-lg
              focus:ring-2 focus:ring-blue-500 focus:border-blue-500
              disabled:bg-gray-100 disabled:cursor-not-allowed
              ${isLoading ? 'bg-gray-50' : ''}
              ${error ? 'border-red-300 focus:ring-red-500 focus:border-red-500' : ''}
            `}
          />
          
          {/* Loading spinner */}
          {isLoading && (
            <div className="absolute inset-y-0 right-0 flex items-center pr-3">
              <svg className="animate-spin h-4 w-4 text-gray-400" viewBox="0 0 24 24">
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
            </div>
          )}
          
          {/* Clear button */}
          {showClearButton && inputValue && !isLoading && (
            <button
              type="button"
              onClick={handleClear}
              className="absolute inset-y-0 right-0 flex items-center pr-3 text-gray-400 hover:text-gray-600"
              disabled={disabled}
            >
              <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M6 18L18 6M6 6l12 12"
                />
              </svg>
            </button>
          )}
          
          {/* Search icon when no input */}
          {!inputValue && !isLoading && (
            <div className="absolute inset-y-0 right-0 flex items-center pr-3">
              <svg className="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  strokeWidth="2"
                  d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                />
              </svg>
            </div>
          )}
        </div>

        {/* Dropdown */}
        <AddressSearchDropdown
          predictions={predictions}
          isLoading={isLoading}
          error={error}
          onSelect={handlePredictionSelect}
          isOpen={isDropdownOpen}
          onClose={handleDropdownClose}
        />
        
        {/* Error message below input */}
        {error && !isDropdownOpen && (
          <div className="mt-1 text-sm text-red-600">
            {error}
          </div>
        )}
        
        {/* Selected place info */}
        {selectedPlace && (
          <div className="mt-2 p-2 bg-green-50 border border-green-200 rounded text-sm text-green-800">
            <div className="flex items-center">
              <svg className="h-4 w-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
                <path
                  fillRule="evenodd"
                  d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
                  clipRule="evenodd"
                />
              </svg>
              Address selected: {selectedPlace.formatted_address}
            </div>
          </div>
        )}
      </div>
    );
  }
);

AddressSearch.displayName = 'AddressSearch';