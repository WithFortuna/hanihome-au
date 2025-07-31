'use client';

import React, { useEffect, useRef } from 'react';
import { ClusterMarker as ClusterMarkerType } from '@/lib/maps/clustering';
import { getClusterIconSize, getClusterColor } from '@/lib/maps/clustering';

interface ClusterMarkerProps {
  cluster: ClusterMarkerType;
  map: google.maps.Map;
  onClick?: (cluster: ClusterMarkerType) => void;
  onMouseOver?: (cluster: ClusterMarkerType) => void;
  onMouseOut?: (cluster: ClusterMarkerType) => void;
  zIndex?: number;
}

export function ClusterMarker({
  cluster,
  map,
  onClick,
  onMouseOver,
  onMouseOut,
  zIndex = 2,
}: ClusterMarkerProps) {
  const markerRef = useRef<google.maps.Marker | null>(null);

  useEffect(() => {
    if (!map) return;

    // Don't create cluster marker for single items
    if (cluster.count === 1) return;

    // Create cluster marker
    const googleMarker = new google.maps.Marker({
      position: { lat: cluster.position.lat, lng: cluster.position.lng },
      map,
      zIndex,
      icon: {
        url: createClusterIcon(cluster.count),
        scaledSize: new google.maps.Size(
          getClusterIconSize(cluster.count),
          getClusterIconSize(cluster.count)
        ),
        anchor: new google.maps.Point(
          getClusterIconSize(cluster.count) / 2,
          getClusterIconSize(cluster.count) / 2
        ),
      },
      title: `${cluster.count} properties`,
    });

    markerRef.current = googleMarker;

    // Add event listeners
    const clickListener = onClick ? 
      googleMarker.addListener('click', () => onClick(cluster)) : null;
    const mouseOverListener = onMouseOver ? 
      googleMarker.addListener('mouseover', () => onMouseOver(cluster)) : null;
    const mouseOutListener = onMouseOut ? 
      googleMarker.addListener('mouseout', () => onMouseOut(cluster)) : null;

    // Cleanup function
    return () => {
      if (clickListener) google.maps.event.removeListener(clickListener);
      if (mouseOverListener) google.maps.event.removeListener(mouseOverListener);
      if (mouseOutListener) google.maps.event.removeListener(mouseOutListener);
      if (markerRef.current) {
        markerRef.current.setMap(null);
      }
    };
  }, [map, cluster, onClick, onMouseOver, onMouseOut, zIndex]);

  return null;
}

/**
 * Create cluster icon as data URL
 */
function createClusterIcon(count: number): string {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';

  const size = getClusterIconSize(count);
  canvas.width = size;
  canvas.height = size;

  const radius = size / 2;
  const center = radius;

  // Draw outer circle (background)
  ctx.fillStyle = getClusterColor(count);
  ctx.beginPath();
  ctx.arc(center, center, radius - 2, 0, 2 * Math.PI);
  ctx.fill();

  // Draw inner circle (lighter background)
  ctx.fillStyle = 'rgba(255, 255, 255, 0.3)';
  ctx.beginPath();
  ctx.arc(center, center, radius - 6, 0, 2 * Math.PI);
  ctx.fill();

  // Draw border
  ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(center, center, radius - 2, 0, 2 * Math.PI);
  ctx.stroke();

  // Draw count text
  ctx.fillStyle = '#ffffff';
  ctx.font = `bold ${Math.max(10, size / 4)}px Arial`;
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(formatClusterCount(count), center, center);

  return canvas.toDataURL();
}

/**
 * Format cluster count for display
 */
function formatClusterCount(count: number): string {
  if (count >= 1000) {
    return `${Math.round(count / 1000)}k`;
  }
  if (count >= 100) {
    return `${Math.round(count / 100)}00`;
  }
  return count.toString();
}

/**
 * Spiderfier Effect for Cluster Explosion
 */
interface SpiderMarkerProps {
  cluster: ClusterMarkerType;
  map: google.maps.Map;
  isExpanded: boolean;
  onMarkerClick?: (markerId: string) => void;
}

export function SpiderMarkers({
  cluster,
  map,
  isExpanded,
  onMarkerClick,
}: SpiderMarkerProps) {
  const spiderMarkersRef = useRef<google.maps.Marker[]>([]);
  const spiderLinesRef = useRef<google.maps.Polyline[]>([]);

  useEffect(() => {
    // Clear existing spider markers and lines
    spiderMarkersRef.current.forEach(marker => marker.setMap(null));
    spiderLinesRef.current.forEach(line => line.setMap(null));
    spiderMarkersRef.current = [];
    spiderLinesRef.current = [];

    if (!isExpanded || cluster.count === 1) return;

    // Calculate spider positions
    const spiderPositions = calculateSpiderPositions(
      cluster.position,
      cluster.markers.length,
      map
    );

    // Create spider markers
    cluster.markers.forEach((marker, index) => {
      const spiderPosition = spiderPositions[index];

      // Create spider line
      const spiderLine = new google.maps.Polyline({
        path: [
          { lat: cluster.position.lat, lng: cluster.position.lng },
          spiderPosition,
        ],
        strokeColor: '#666666',
        strokeOpacity: 0.6,
        strokeWeight: 2,
        map,
        zIndex: 0,
      });
      spiderLinesRef.current.push(spiderLine);

      // Create spider marker
      const spiderMarker = new google.maps.Marker({
        position: spiderPosition,
        map,
        icon: {
          url: createSpiderMarkerIcon(marker.propertyType),
          scaledSize: new google.maps.Size(30, 30),
          anchor: new google.maps.Point(15, 15),
        },
        title: marker.title,
        zIndex: 3,
      });

      // Add click listener
      if (onMarkerClick) {
        spiderMarker.addListener('click', () => onMarkerClick(marker.id));
      }

      spiderMarkersRef.current.push(spiderMarker);
    });

    return () => {
      spiderMarkersRef.current.forEach(marker => marker.setMap(null));
      spiderLinesRef.current.forEach(line => line.setMap(null));
    };
  }, [cluster, map, isExpanded, onMarkerClick]);

  return null;
}

/**
 * Calculate spider positions around cluster center
 */
function calculateSpiderPositions(
  center: { lat: number; lng: number },
  count: number,
  map: google.maps.Map
): google.maps.LatLng[] {
  const positions: google.maps.LatLng[] = [];
  const angleStep = (2 * Math.PI) / count;
  const radiusMeters = 50; // 50 meter radius

  for (let i = 0; i < count; i++) {
    const angle = i * angleStep;
    
    // Calculate offset in meters
    const latOffset = (radiusMeters * Math.cos(angle)) / 111320; // rough meters to degrees lat
    const lngOffset = (radiusMeters * Math.sin(angle)) / (111320 * Math.cos(center.lat * Math.PI / 180));

    positions.push(
      new google.maps.LatLng(
        center.lat + latOffset,
        center.lng + lngOffset
      )
    );
  }

  return positions;
}

/**
 * Create spider marker icon
 */
function createSpiderMarkerIcon(propertyType?: string): string {
  const canvas = document.createElement('canvas');
  const ctx = canvas.getContext('2d');
  if (!ctx) return '';

  canvas.width = 30;
  canvas.height = 30;

  // Draw circle background
  ctx.fillStyle = getPropertyTypeColor(propertyType);
  ctx.beginPath();
  ctx.arc(15, 15, 12, 0, 2 * Math.PI);
  ctx.fill();

  // Draw border
  ctx.strokeStyle = '#ffffff';
  ctx.lineWidth = 2;
  ctx.beginPath();
  ctx.arc(15, 15, 10, 0, 2 * Math.PI);
  ctx.stroke();

  // Draw property type icon
  ctx.fillStyle = '#ffffff';
  ctx.font = '12px Arial';
  ctx.textAlign = 'center';
  ctx.textBaseline = 'middle';
  ctx.fillText(getPropertyTypeIcon(propertyType), 15, 15);

  return canvas.toDataURL();
}

/**
 * Get property type color
 */
function getPropertyTypeColor(propertyType?: string): string {
  switch (propertyType?.toLowerCase()) {
    case 'house':
      return '#4F46E5';
    case 'apartment':
    case 'unit':
      return '#059669';
    case 'townhouse':
      return '#DC2626';
    case 'studio':
      return '#7C3AED';
    case 'room':
      return '#D97706';
    default:
      return '#6B7280';
  }
}

/**
 * Get property type icon character
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
      return '📐';
    case 'room':
      return '🚪';
    default:
      return '📍';
  }
}