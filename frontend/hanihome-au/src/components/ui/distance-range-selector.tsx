'use client';

import React, { useState, useCallback } from 'react';
import { Slider } from '@/components/ui/slider';
import { Select } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import { MapPin, Crosshair } from 'lucide-react';
import { cn } from '@/lib/utils';
import { MapPosition } from '@/lib/maps/types';

export type DistanceUnit = 'km' | 'miles';
export type SelectionMode = 'slider' | 'dropdown';

export interface DistanceRangeValue {
  distance: number;
  unit: DistanceUnit;
}

export interface DistanceRangeSelectorProps {
  /** Current distance value */
  value?: DistanceRangeValue;
  /** Callback when distance changes */
  onChange?: (value: DistanceRangeValue) => void;
  /** User's current location for distance calculation */
  userLocation?: MapPosition | null;
  /** Callback to get user's current location */
  onGetCurrentLocation?: () => Promise<MapPosition>;
  /** Whether location detection is in progress */
  isGettingLocation?: boolean;
  /** Selection mode: slider or dropdown */
  mode?: SelectionMode;
  /** Allow user to toggle between modes */
  allowModeToggle?: boolean;
  /** Minimum distance in km */
  minDistance?: number;
  /** Maximum distance in km */
  maxDistance?: number;
  /** Distance step for slider */
  step?: number;
  /** Predefined distance options for dropdown */
  distanceOptions?: number[];
  /** Default unit */
  defaultUnit?: DistanceUnit;
  /** Show unit toggle */
  showUnitToggle?: boolean;
  /** Additional CSS classes */
  className?: string;
  /** Whether the component is disabled */
  disabled?: boolean;
  /** Size variant */
  size?: 'sm' | 'default' | 'lg';
}

const DEFAULT_DISTANCE_OPTIONS = [0.5, 1, 2, 5, 10, 15, 20, 30, 50];
const DEFAULT_VALUE: DistanceRangeValue = { distance: 5, unit: 'km' };

export function DistanceRangeSelector({
  value = DEFAULT_VALUE,
  onChange,
  userLocation,
  onGetCurrentLocation,
  isGettingLocation = false,
  mode = 'slider',
  allowModeToggle = true,
  minDistance = 0.5,
  maxDistance = 50,
  step = 0.5,
  distanceOptions = DEFAULT_DISTANCE_OPTIONS,
  defaultUnit = 'km',
  showUnitToggle = true,
  className,
  disabled = false,
  size = 'default',
}: DistanceRangeSelectorProps) {
  const [currentMode, setCurrentMode] = useState<SelectionMode>(mode);
  const [internalValue, setInternalValue] = useState<DistanceRangeValue>(value);

  // Convert km to miles and vice versa
  const convertDistance = useCallback((distance: number, fromUnit: DistanceUnit, toUnit: DistanceUnit): number => {
    if (fromUnit === toUnit) return distance;
    if (fromUnit === 'km' && toUnit === 'miles') return distance * 0.621371;
    if (fromUnit === 'miles' && toUnit === 'km') return distance / 0.621371;
    return distance;
  }, []);

  // Handle distance change
  const handleDistanceChange = useCallback((newDistance: number) => {
    const updatedValue = { ...internalValue, distance: newDistance };
    setInternalValue(updatedValue);
    onChange?.(updatedValue);
  }, [internalValue, onChange]);

  // Handle unit change
  const handleUnitChange = useCallback((newUnit: DistanceUnit) => {
    const convertedDistance = convertDistance(internalValue.distance, internalValue.unit, newUnit);
    const updatedValue = { distance: convertedDistance, unit: newUnit };
    setInternalValue(updatedValue);
    onChange?.(updatedValue);
  }, [internalValue, convertDistance, onChange]);

  // Handle location request
  const handleGetCurrentLocation = useCallback(async () => {
    if (onGetCurrentLocation && !isGettingLocation) {
      try {
        await onGetCurrentLocation();
      } catch (error) {
        console.error('Failed to get current location:', error);
      }
    }
  }, [onGetCurrentLocation, isGettingLocation]);

  // Format distance for display
  const formatDistance = useCallback((distance: number, unit: DistanceUnit): string => {
    if (distance < 1) {
      return `${(distance * 1000).toFixed(0)}m`;
    }
    return `${distance.toFixed(distance % 1 === 0 ? 0 : 1)}${unit === 'km' ? 'km' : 'mi'}`;
  }, []);

  const sizeClasses = {
    sm: 'text-sm',
    default: 'text-base',
    lg: 'text-lg',
  };

  return (
    <div className={cn('space-y-4', sizeClasses[size], className)}>
      {/* Header with location and mode toggle */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <MapPin className="h-4 w-4 text-muted-foreground" />
          <span className="text-sm font-medium text-foreground">Distance Range</span>
          {userLocation && (
            <span className="text-xs text-muted-foreground">
              ({userLocation.lat.toFixed(4)}, {userLocation.lng.toFixed(4)})
            </span>
          )}
        </div>
        
        <div className="flex items-center gap-2">
          {/* Get current location button */}
          {onGetCurrentLocation && (
            <Button
              variant="outline"
              size="sm"
              onClick={handleGetCurrentLocation}
              disabled={disabled || isGettingLocation}
              className="text-xs"
            >
              <Crosshair className={cn('h-3 w-3', isGettingLocation && 'animate-spin')} />
              {isGettingLocation ? 'Getting...' : 'My Location'}
            </Button>
          )}
          
          {/* Mode toggle */}
          {allowModeToggle && (
            <div className="flex border rounded-md">
              <Button
                variant={currentMode === 'slider' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setCurrentMode('slider')}
                disabled={disabled}
                className="rounded-r-none text-xs px-2"
              >
                Slider
              </Button>
              <Button
                variant={currentMode === 'dropdown' ? 'default' : 'ghost'}
                size="sm"
                onClick={() => setCurrentMode('dropdown')}
                disabled={disabled}
                className="rounded-l-none text-xs px-2"
              >
                Select
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Distance selection */}
      <div className="space-y-3">
        {currentMode === 'slider' ? (
          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <label className="text-sm text-muted-foreground">
                Within {formatDistance(internalValue.distance, internalValue.unit)}
              </label>
              <div className="text-xs text-muted-foreground">
                {formatDistance(minDistance, internalValue.unit)} - {formatDistance(maxDistance, internalValue.unit)}
              </div>
            </div>
            <Slider
              value={[internalValue.distance]}
              onValueChange={([newDistance]) => handleDistanceChange(newDistance)}
              min={minDistance}
              max={maxDistance}
              step={step}
              disabled={disabled}
              className="w-full"
            />
          </div>
        ) : (
          <div className="space-y-2">
            <label className="text-sm text-muted-foreground">Select distance range</label>
            <Select
              value={internalValue.distance.toString()}
              onChange={(e) => handleDistanceChange(parseFloat(e.target.value))}
              disabled={disabled}
              className="w-full"
            >
              {distanceOptions.map((distance) => (
                <option key={distance} value={distance.toString()}>
                  Within {formatDistance(distance, internalValue.unit)}
                </option>
              ))}
            </Select>
          </div>
        )}
      </div>

      {/* Unit toggle */}
      {showUnitToggle && (
        <div className="flex items-center justify-center">
          <div className="flex border rounded-md">
            <Button
              variant={internalValue.unit === 'km' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => handleUnitChange('km')}
              disabled={disabled}
              className="rounded-r-none text-xs px-3"
            >
              Kilometers
            </Button>
            <Button
              variant={internalValue.unit === 'miles' ? 'default' : 'ghost'}
              size="sm"
              onClick={() => handleUnitChange('miles')}
              disabled={disabled}
              className="rounded-l-none text-xs px-3"
            >
              Miles
            </Button>
          </div>
        </div>
      )}

      {/* Status message */}
      {!userLocation && !isGettingLocation && (
        <div className="text-xs text-muted-foreground text-center p-2 bg-muted/50 rounded">
          📍 Enable location access to search properties near you
        </div>
      )}
    </div>
  );
}

export default DistanceRangeSelector;