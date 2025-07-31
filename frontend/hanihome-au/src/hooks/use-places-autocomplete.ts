'use client';

import { useState, useCallback, useRef, useEffect } from 'react';
import { PlacesService } from '@/lib/maps/places';
import { PlaceResult, MapPosition } from '@/lib/maps/types';
import { useGoogleMaps } from './use-google-maps';

interface UsePlacesAutocompleteOptions {
  defaultValue?: string;
  debounceMs?: number;
  minLength?: number;
  types?: string[];
  componentRestrictions?: { country: string };
  location?: MapPosition;
  radius?: number;
}

interface UsePlacesAutocompleteReturn {
  inputValue: string;
  setInputValue: (value: string) => void;
  predictions: google.maps.places.AutocompletePrediction[];
  isLoading: boolean;
  error: string | null;
  selectedPlace: PlaceResult | null;
  clearSelection: () => void;
  selectPrediction: (prediction: google.maps.places.AutocompletePrediction) => Promise<void>;
  getPlaceDetails: (placeId: string) => Promise<PlaceResult | null>;
}

export function usePlacesAutocomplete(
  options: UsePlacesAutocompleteOptions = {}
): UsePlacesAutocompleteReturn {
  const {
    defaultValue = '',
    debounceMs = 300,
    minLength = 2,
    types = ['address'],
    componentRestrictions = { country: 'AU' },
    location,
    radius,
  } = options;

  const { isLoaded } = useGoogleMaps();
  const [inputValue, setInputValue] = useState(defaultValue);
  const [predictions, setPredictions] = useState<google.maps.places.AutocompletePrediction[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedPlace, setSelectedPlace] = useState<PlaceResult | null>(null);
  
  const debounceTimeoutRef = useRef<NodeJS.Timeout>();
  const placesServiceRef = useRef<PlacesService | null>(null);

  // Initialize PlacesService when Google Maps is loaded
  useEffect(() => {
    if (isLoaded && !placesServiceRef.current) {
      placesServiceRef.current = new PlacesService();
    }
  }, [isLoaded]);

  // Debounced prediction fetching
  const fetchPredictions = useCallback(async (input: string) => {
    if (!placesServiceRef.current || input.length < minLength) {
      setPredictions([]);
      return;
    }

    setIsLoading(true);
    setError(null);

    try {
      const results = await placesServiceRef.current.getPlacePredictions(input, {
        types,
        componentRestrictions,
        location,
        radius,
      });
      setPredictions(results);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to fetch predictions');
      setPredictions([]);
    } finally {
      setIsLoading(false);
    }
  }, [minLength, types, componentRestrictions, location, radius]);

  // Handle input value changes with debouncing
  const handleInputValueChange = useCallback((value: string) => {
    setInputValue(value);
    
    // Clear previous timeout
    if (debounceTimeoutRef.current) {
      clearTimeout(debounceTimeoutRef.current);
    }

    // Clear predictions and selected place if input is cleared
    if (!value.trim()) {
      setPredictions([]);
      setSelectedPlace(null);
      return;
    }

    // Set new timeout for debounced fetch
    debounceTimeoutRef.current = setTimeout(() => {
      fetchPredictions(value);
    }, debounceMs);
  }, [fetchPredictions, debounceMs]);

  // Get detailed place information
  const getPlaceDetails = useCallback(async (placeId: string): Promise<PlaceResult | null> => {
    if (!placesServiceRef.current) {
      return null;
    }

    try {
      // For place details, we need a map instance, so we'll create a temporary one
      const mapDiv = document.createElement('div');
      const map = new google.maps.Map(mapDiv);
      const serviceWithMap = new PlacesService(map);
      return await serviceWithMap.getPlaceDetails(placeId);
    } catch (err) {
      console.error('Failed to get place details:', err);
      return null;
    }
  }, []);

  // Select a prediction and get its details
  const selectPrediction = useCallback(async (prediction: google.maps.places.AutocompletePrediction) => {
    setIsLoading(true);
    setError(null);

    try {
      const placeDetails = await getPlaceDetails(prediction.place_id);
      if (placeDetails) {
        setSelectedPlace(placeDetails);
        setInputValue(placeDetails.formatted_address);
        setPredictions([]); // Clear predictions after selection
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to get place details');
    } finally {
      setIsLoading(false);
    }
  }, [getPlaceDetails]);

  // Clear selection
  const clearSelection = useCallback(() => {
    setSelectedPlace(null);
    setInputValue('');
    setPredictions([]);
    setError(null);
  }, []);

  // Cleanup timeout on unmount
  useEffect(() => {
    return () => {
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, []);

  return {
    inputValue,
    setInputValue: handleInputValueChange,
    predictions,
    isLoading,
    error,
    selectedPlace,
    clearSelection,
    selectPrediction,
    getPlaceDetails,
  };
}