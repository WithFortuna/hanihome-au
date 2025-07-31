'use client';

import React, { useEffect, useRef } from 'react';
import { PropertyMarker as PropertyMarkerType } from '@/lib/maps/types';

interface PropertyMarkerProps {
  marker: PropertyMarkerType;
  map: google.maps.Map;
  onClick?: (marker: PropertyMarkerType) => void;
  onMouseOver?: (marker: PropertyMarkerType) => void;
  onMouseOut?: (marker: PropertyMarkerType) => void;
  icon?: string | google.maps.Icon | google.maps.Symbol;
  zIndex?: number;
  animation?: google.maps.Animation;
}

export function PropertyMarker({
  marker,
  map,
  onClick,
  onMouseOver,
  onMouseOut,
  icon,
  zIndex = 1,
  animation,
}: PropertyMarkerProps) {
  const markerRef = useRef<google.maps.Marker | null>(null);

  useEffect(() => {
    if (!map) return;

    // Create marker
    const googleMarker = new google.maps.Marker({
      position: { lat: marker.position.lat, lng: marker.position.lng },
      map,
      title: marker.title,
      zIndex,
      animation,
      icon: icon || {
        url: createPropertyMarkerIcon(marker),
        scaledSize: new google.maps.Size(40, 50),
        anchor: new google.maps.Point(20, 50),
      },
    });

    markerRef.current = googleMarker;

    // Add event listeners
    const clickListener = onClick ? googleMarker.addListener('click', () => onClick(marker)) : null;
    const mouseOverListener = onMouseOver ? googleMarker.addListener('mouseover', () => onMouseOver(marker)) : null;
    const mouseOutListener = onMouseOut ? googleMarker.addListener('mouseout', () => onMouseOut(marker)) : null;

    // Cleanup function
    return () => {
      if (clickListener) google.maps.event.removeListener(clickListener);
      if (mouseOverListener) google.maps.event.removeListener(mouseOverListener);
      if (mouseOutListener) google.maps.event.removeListener(mouseOutListener);
      if (markerRef.current) {
        markerRef.current.setMap(null);
      }
    };
  }, [map, marker, onClick, onMouseOver, onMouseOut, icon, zIndex, animation]);

  // Update marker position if changed
  useEffect(() => {
    if (markerRef.current) {
      markerRef.current.setPosition({
        lat: marker.position.lat,
        lng: marker.position.lng,
      });
    }
  }, [marker.position]);

  // Update marker title if changed
  useEffect(() => {
    if (markerRef.current) {
      markerRef.current.setTitle(marker.title);
    }
  }, [marker.title]);

  return null; // This component doesn't render anything directly
}

/**
 * Create custom property marker icon as data URL
 */
function createPropertyMarkerIcon(marker: PropertyMarkerType): string {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';

  canvas.width = 40;
  canvas.height = 50;

  // Draw marker background
  ctx.fillStyle = getMarkerColor(marker.propertyType);
  ctx.beginPath();
  ctx.arc(20, 20, 18, 0, 2 * Math.PI);
  ctx.fill();

  // Draw marker point
  ctx.beginPath();
  ctx.moveTo(20, 38);
  ctx.lineTo(10, 30);
  ctx.lineTo(30, 30);
  ctx.closePath();
  ctx.fill();

  // Draw border
  ctx.strokeStyle = '#ffffff';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(20, 20, 16, 0, 2 * Math.PI);
  ctx.stroke();

  // Draw price or property type icon
  ctx.fillStyle = '#ffffff';
  ctx.font = 'bold 10px Arial';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';

  if (marker.price) {
    const priceText = formatPrice(marker.price);
    ctx.fillText(priceText, 20, 20);
  } else {
    // Draw property type icon
    const icon = getPropertyTypeIcon(marker.propertyType);
    ctx.font = '14px Arial';
    ctx.fillText(icon, 20, 20);
  }

  return canvas.toDataURL();
}

/**
 * Get marker color based on property type
 */
function getMarkerColor(propertyType?: string): string {
  switch (propertyType?.toLowerCase()) {
    case 'house':
      return '#4F46E5'; // Indigo
    case 'apartment':
    case 'unit':
      return '#059669'; // Emerald
    case 'townhouse':
      return '#DC2626'; // Red
    case 'studio':
      return '#7C3AED'; // Violet
    case 'room':
      return '#D97706'; // Amber
    default:
      return '#6B7280'; // Gray
  }
}

/**
 * Get property type icon
 */
function getPropertyTypeIcon(propertyType?: string): string {
  switch (propertyType?.toLowerCase()) {
    case 'house':
      return '🏠';
    case 'apartment':
    case 'unit':
      return '🏢';
    case 'townhouse':
      return '🏘️';
    case 'studio':
      return '🏠';
    case 'room':
      return '🚪';
    default:
      return '📍';
  }
}

/**
 * Format price for display on marker
 */
function formatPrice(price: number): string {
  if (price >= 1000) {
    return `${Math.round(price / 1000)}k`;
  }
  return price.toString();
}

/**
 * Custom Info Window Component for Property Details
 */
interface PropertyInfoWindowProps {
  marker: PropertyMarkerType;
  map: google.maps.Map;
  isOpen: boolean;
  onClose: () => void;
}

export function PropertyInfoWindow({
  marker,
  map,
  isOpen,
  onClose,
}: PropertyInfoWindowProps) {
  const infoWindowRef = useRef<google.maps.InfoWindow | null>(null);

  useEffect(() => {
    if (!map) return;

    // Create info window
    const infoWindow = new google.maps.InfoWindow({
      content: createInfoWindowContent(marker),
      disableAutoPan: false,
      maxWidth: 300,
    });

    infoWindowRef.current = infoWindow;

    // Add close listener
    const closeListener = infoWindow.addListener('closeclick', onClose);

    return () => {
      google.maps.event.removeListener(closeListener);
      if (infoWindowRef.current) {
        infoWindowRef.current.close();
      }
    };
  }, [map, marker, onClose]);

  useEffect(() => {
    if (!infoWindowRef.current) return;

    if (isOpen) {
      infoWindowRef.current.open(map, undefined);
      infoWindowRef.current.setPosition({
        lat: marker.position.lat,
        lng: marker.position.lng,
      });
    } else {
      infoWindowRef.current.close();
    }
  }, [isOpen, map, marker.position]);

  return null;
}

/**
 * Create HTML content for info window
 */
function createInfoWindowContent(marker: PropertyMarkerType): string {
  return `
    <div class="property-info-window" style="padding: 12px; max-width: 280px;">
      ${marker.imageUrl ? `
        <img src="${marker.imageUrl}" 
             alt="${marker.title}" 
             style="width: 100%; height: 120px; object-fit: cover; border-radius: 6px; margin-bottom: 8px;" />
      ` : ''}
      
      <h3 style="margin: 0 0 8px 0; font-size: 16px; font-weight: 600; color: #1f2937;">
        ${marker.title}
      </h3>
      
      ${marker.price ? `
        <div style="margin-bottom: 8px;">
          <span style="font-size: 18px; font-weight: 700; color: #059669;">
            $${marker.price.toLocaleString()}/week
          </span>
        </div>
      ` : ''}
      
      <div style="display: flex; gap: 16px; margin-bottom: 8px; font-size: 14px; color: #6b7280;">
        ${marker.bedrooms ? `
          <div style="display: flex; align-items: center; gap: 4px;">
            <span>🛏️</span>
            <span>${marker.bedrooms} bed${marker.bedrooms > 1 ? 's' : ''}</span>
          </div>
        ` : ''}
        
        ${marker.bathrooms ? `
          <div style="display: flex; align-items: center; gap: 4px;">
            <span>🚿</span>
            <span>${marker.bathrooms} bath${marker.bathrooms > 1 ? 's' : ''}</span>
          </div>
        ` : ''}
      </div>
      
      ${marker.propertyType ? `
        <div style="margin-bottom: 8px;">
          <span style="background: #f3f4f6; padding: 2px 8px; border-radius: 12px; font-size: 12px; color: #374151;">
            ${marker.propertyType}
          </span>
        </div>
      ` : ''}
      
      <button 
        onclick="window.viewPropertyDetails('${marker.id}')"
        style="
          width: 100%; 
          padding: 8px 16px; 
          background: #4f46e5; 
          color: white; 
          border: none; 
          border-radius: 6px; 
          font-size: 14px; 
          font-weight: 500; 
          cursor: pointer;
          margin-top: 8px;
        "
        onmouseover="this.style.background='#4338ca'"
        onmouseout="this.style.background='#4f46e5'"
      >
        View Details
      </button>
    </div>
  `;
}