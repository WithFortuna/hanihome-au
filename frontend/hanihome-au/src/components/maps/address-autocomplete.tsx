'use client';

import React, { useRef, useEffect, useState, forwardRef } from 'react';
import { useGoogleMaps } from '@/hooks/use-google-maps';
import { PlaceResult } from '@/lib/maps/types';
import { AUSTRALIA_BOUNDS } from '@/lib/maps/config';

interface AddressAutocompleteProps {
  onPlaceSelect: (place: PlaceResult) => void;
  placeholder?: string;
  className?: string;
  disabled?: boolean;
  value?: string;
  onChange?: (value: string) => void;
  restrictToAustralia?: boolean;
  types?: string[];
}

export const AddressAutocomplete = forwardRef<HTMLInputElement, AddressAutocompleteProps>(
  (
    {
      onPlaceSelect,
      placeholder = 'Enter address...',
      className = '',
      disabled = false,
      value,
      onChange,
      restrictToAustralia = true,
      types = ['address'],
    },
    ref
  ) => {
    const inputRef = useRef<HTMLInputElement>(null);
    const autocompleteRef = useRef<google.maps.places.Autocomplete | null>(null);
    const [inputValue, setInputValue] = useState(value || '');
    const { isLoaded, loadError } = useGoogleMaps();

    // Handle controlled/uncontrolled component
    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
      const newValue = e.target.value;
      setInputValue(newValue);
      onChange?.(newValue);
    };

    useEffect(() => {
      if (value !== undefined) {
        setInputValue(value);
      }
    }, [value]);

    useEffect(() => {
      if (!isLoaded || !inputRef.current || loadError) return;

      // Create autocomplete options
      const autocompleteOptions: google.maps.places.AutocompleteOptions = {
        types: types,
        fields: [
          'place_id',
          'formatted_address',
          'geometry.location',
          'geometry.viewport',
          'address_components',
          'name',
          'types'
        ],
      };

      // Restrict to Australia if requested
      if (restrictToAustralia) {
        autocompleteOptions.componentRestrictions = { country: 'AU' };
        autocompleteOptions.bounds = new google.maps.LatLngBounds(
          new google.maps.LatLng(AUSTRALIA_BOUNDS.south, AUSTRALIA_BOUNDS.west),
          new google.maps.LatLng(AUSTRALIA_BOUNDS.north, AUSTRALIA_BOUNDS.east)
        );
      }

      // Initialize autocomplete
      autocompleteRef.current = new google.maps.places.Autocomplete(
        inputRef.current,
        autocompleteOptions
      );

      // Handle place selection
      const handlePlaceChanged = () => {
        const place = autocompleteRef.current?.getPlace();
        if (!place) return;

        // Convert to our PlaceResult interface
        const placeResult: PlaceResult = {
          place_id: place.place_id || '',
          formatted_address: place.formatted_address || '',
          geometry: place.geometry
            ? {
                location: {
                  lat: place.geometry.location?.lat() || 0,
                  lng: place.geometry.location?.lng() || 0,
                },
                viewport: place.geometry.viewport
                  ? {
                      north: place.geometry.viewport.getNorthEast().lat(),
                      south: place.geometry.viewport.getSouthWest().lat(),
                      east: place.geometry.viewport.getNorthEast().lng(),
                      west: place.geometry.viewport.getSouthWest().lng(),
                    }
                  : undefined,
              }
            : undefined,
          address_components: place.address_components?.map((component) => ({
            long_name: component.long_name,
            short_name: component.short_name,
            types: component.types,
          })),
          name: place.name,
          types: place.types,
        };

        setInputValue(place.formatted_address || '');
        onPlaceSelect(placeResult);
      };

      // Add event listener
      autocompleteRef.current.addListener('place_changed', handlePlaceChanged);

      // Cleanup function
      return () => {
        if (autocompleteRef.current) {
          google.maps.event.clearInstanceListeners(autocompleteRef.current);
        }
      };
    }, [isLoaded, loadError, restrictToAustralia, types, onPlaceSelect]);

    // Clear autocomplete when input is cleared
    useEffect(() => {
      if (inputValue === '' && autocompleteRef.current) {
        // Clear the autocomplete selection
        autocompleteRef.current.set('place', null);
      }
    }, [inputValue]);

    if (loadError) {
      return (
        <input
          ref={ref}
          type="text"
          placeholder="Google Maps failed to load"
          className={`${className} bg-gray-100 cursor-not-allowed`}
          disabled
        />
      );
    }

    if (!isLoaded) {
      return (
        <input
          ref={ref}
          type="text"
          placeholder="Loading maps..."
          className={`${className} bg-gray-50`}
          disabled
        />
      );
    }

    return (
      <input
        ref={(node) => {
          inputRef.current = node;
          if (typeof ref === 'function') {
            ref(node);
          } else if (ref) {
            ref.current = node;
          }
        }}
        type="text"
        value={inputValue}
        onChange={handleInputChange}
        placeholder={placeholder}
        className={className}
        disabled={disabled}
        autoComplete="off"
      />
    );
  }
);

AddressAutocomplete.displayName = 'AddressAutocomplete';