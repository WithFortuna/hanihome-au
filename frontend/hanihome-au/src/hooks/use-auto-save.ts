'use client';

import { useEffect, useCallback, useRef } from 'react';
import { UseFormWatch, UseFormSetValue } from 'react-hook-form';
import { PropertyFormData } from '@/lib/types/property';

interface UseAutoSaveProps {
  watch: UseFormWatch<PropertyFormData>;
  setValue: UseFormSetValue<PropertyFormData>;
  storageKey: string;
  debounceMs?: number;
}

export function useAutoSave({ 
  watch, 
  setValue, 
  storageKey, 
  debounceMs = 1000 
}: UseAutoSaveProps) {
  const debounceTimeoutRef = useRef<NodeJS.Timeout>();
  const isInitialLoadRef = useRef(true);

  // Load saved data on component mount
  const loadSavedData = useCallback(() => {
    try {
      const savedData = localStorage.getItem(storageKey);
      if (savedData) {
        const parsedData = JSON.parse(savedData);
        
        // Set each field value individually to trigger proper form updates
        Object.entries(parsedData).forEach(([key, value]) => {
          if (value !== undefined && value !== null && value !== '') {
            setValue(key as keyof PropertyFormData, value, { 
              shouldValidate: false,
              shouldDirty: true 
            });
          }
        });
        
        return true; // Data was loaded
      }
    } catch (error) {
      console.error('Failed to load saved form data:', error);
    }
    return false; // No data was loaded
  }, [setValue, storageKey]);

  // Save data to localStorage
  const saveData = useCallback((data: PropertyFormData) => {
    try {
      // Filter out empty values to reduce storage size
      const filteredData = Object.entries(data).reduce((acc, [key, value]) => {
        if (value !== undefined && value !== null && value !== '' && 
            !(Array.isArray(value) && value.length === 0)) {
          acc[key] = value;
        }
        return acc;
      }, {} as Record<string, any>);

      localStorage.setItem(storageKey, JSON.stringify(filteredData));
      
      // Store last save timestamp
      localStorage.setItem(`${storageKey}_timestamp`, new Date().toISOString());
    } catch (error) {
      console.error('Failed to save form data:', error);
    }
  }, [storageKey]);

  // Clear saved data
  const clearSavedData = useCallback(() => {
    try {
      localStorage.removeItem(storageKey);
      localStorage.removeItem(`${storageKey}_timestamp`);
    } catch (error) {
      console.error('Failed to clear saved form data:', error);
    }
  }, [storageKey]);

  // Get last save timestamp
  const getLastSaveTime = useCallback(() => {
    try {
      const timestamp = localStorage.getItem(`${storageKey}_timestamp`);
      return timestamp ? new Date(timestamp) : null;
    } catch (error) {
      console.error('Failed to get last save time:', error);
      return null;
    }
  }, [storageKey]);

  // Watch all form values and auto-save with debouncing
  useEffect(() => {
    const subscription = watch((value) => {
      // Skip saving during initial load to prevent overwriting loaded data
      if (isInitialLoadRef.current) {
        isInitialLoadRef.current = false;
        return;
      }

      // Clear existing timeout
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }

      // Set new timeout for debounced save
      debounceTimeoutRef.current = setTimeout(() => {
        saveData(value as PropertyFormData);
      }, debounceMs);
    });

    return () => {
      subscription.unsubscribe();
      if (debounceTimeoutRef.current) {
        clearTimeout(debounceTimeoutRef.current);
      }
    };
  }, [watch, saveData, debounceMs]);

  // Load saved data on mount
  useEffect(() => {
    const hasData = loadSavedData();
    if (hasData) {
      // Allow auto-saving after a brief delay to let the form settle
      setTimeout(() => {
        isInitialLoadRef.current = false;
      }, 100);
    } else {
      isInitialLoadRef.current = false;
    }
  }, [loadSavedData]);

  return {
    loadSavedData,
    saveData,
    clearSavedData,
    getLastSaveTime,
  };
}