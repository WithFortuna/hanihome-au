/**
 * Map Marker Clustering Utilities
 */

import { PropertyMarker, MapPosition, MapBounds } from './types';

export interface ClusterMarker {
  id: string;
  position: MapPosition;
  count: number;
  markers: PropertyMarker[];
  bounds: MapBounds;
}

export interface ClusteringOptions {
  gridSize?: number;
  maxZoom?: number;
  minimumClusterSize?: number;
  enableRetinaIcons?: boolean;
}

/**
 * Simple grid-based clustering algorithm
 */
export class MarkerClusterer {
  private gridSize: number;
  private maxZoom: number;
  private minimumClusterSize: number;
  private clusters: ClusterMarker[] = [];

  constructor(options: ClusteringOptions = {}) {
    this.gridSize = options.gridSize || 60;
    this.maxZoom = options.maxZoom || 15;
    this.minimumClusterSize = options.minimumClusterSize || 2;
  }

  /**
   * Create clusters from markers based on current map zoom and bounds
   */
  cluster(markers: PropertyMarker[], zoom: number, bounds: MapBounds): ClusterMarker[] {
    // Clear existing clusters
    this.clusters = [];

    // Don't cluster if zoom is too high
    if (zoom > this.maxZoom) {
      return markers.map(marker => ({
        id: `single_${marker.id}`,
        position: marker.position,
        count: 1,
        markers: [marker],
        bounds: {
          north: marker.position.lat + 0.001,
          south: marker.position.lat - 0.001,
          east: marker.position.lng + 0.001,
          west: marker.position.lng - 0.001,
        },
      }));
    }

    // Filter markers within bounds
    const visibleMarkers = markers.filter(marker => 
      this.isMarkerInBounds(marker, bounds)
    );

    // Group markers into grid cells
    const gridCells = new Map<string, PropertyMarker[]>();
    
    visibleMarkers.forEach(marker => {
      const gridKey = this.getGridKey(marker.position, zoom);
      if (!gridCells.has(gridKey)) {
        gridCells.set(gridKey, []);
      }
      gridCells.get(gridKey)!.push(marker);
    });

    // Create clusters from grid cells
    gridCells.forEach((cellMarkers, gridKey) => {
      if (cellMarkers.length >= this.minimumClusterSize) {
        // Create cluster
        const cluster = this.createCluster(cellMarkers, gridKey);
        this.clusters.push(cluster);
      } else {
        // Individual markers
        cellMarkers.forEach(marker => {
          this.clusters.push({
            id: `single_${marker.id}`,
            position: marker.position,
            count: 1,
            markers: [marker],
            bounds: {
              north: marker.position.lat + 0.001,
              south: marker.position.lat - 0.001,
              east: marker.position.lng + 0.001,
              west: marker.position.lng - 0.001,
            },
          });
        });
      }
    });

    return this.clusters;
  }

  /**
   * Check if marker is within bounds
   */
  private isMarkerInBounds(marker: PropertyMarker, bounds: MapBounds): boolean {
    return (
      marker.position.lat >= bounds.south &&
      marker.position.lat <= bounds.north &&
      marker.position.lng >= bounds.west &&
      marker.position.lng <= bounds.east
    );
  }

  /**
   * Get grid key for marker position based on zoom level
   */
  private getGridKey(position: MapPosition, zoom: number): string {
    const gridSize = this.gridSize / Math.pow(2, zoom - 1);
    const x = Math.floor(position.lng / gridSize);
    const y = Math.floor(position.lat / gridSize);
    return `${x}_${y}`;
  }

  /**
   * Create cluster from group of markers
   */
  private createCluster(markers: PropertyMarker[], gridKey: string): ClusterMarker {
    // Calculate cluster center (average position)
    const totalLat = markers.reduce((sum, marker) => sum + marker.position.lat, 0);
    const totalLng = markers.reduce((sum, marker) => sum + marker.position.lng, 0);
    
    const centerPosition: MapPosition = {
      lat: totalLat / markers.length,
      lng: totalLng / markers.length,
    };

    // Calculate cluster bounds
    const lats = markers.map(m => m.position.lat);
    const lngs = markers.map(m => m.position.lng);
    
    const bounds: MapBounds = {
      north: Math.max(...lats),
      south: Math.min(...lats),
      east: Math.max(...lngs),
      west: Math.min(...lngs),
    };

    return {
      id: `cluster_${gridKey}`,
      position: centerPosition,
      count: markers.length,
      markers,
      bounds,
    };
  }

  /**
   * Get existing clusters
   */
  getClusters(): ClusterMarker[] {
    return this.clusters;
  }

  /**
   * Clear all clusters
   */
  clearClusters(): void {
    this.clusters = [];
  }
}

/**
 * Distance-based clustering algorithm (alternative to grid-based)
 */
export class DistanceBasedClusterer {
  private minDistance: number;
  private maxZoom: number;
  private minimumClusterSize: number;

  constructor(options: ClusteringOptions & { minDistance?: number } = {}) {
    this.minDistance = options.minDistance || 100; // pixels
    this.maxZoom = options.maxZoom || 15;
    this.minimumClusterSize = options.minimumClusterSize || 2;
  }

  /**
   * Cluster markers based on distance
   */
  cluster(markers: PropertyMarker[], zoom: number, map: google.maps.Map): ClusterMarker[] {
    if (zoom > this.maxZoom || markers.length === 0) {
      return markers.map(marker => ({
        id: `single_${marker.id}`,
        position: marker.position,
        count: 1,
        markers: [marker],
        bounds: {
          north: marker.position.lat + 0.001,
          south: marker.position.lat - 0.001,
          east: marker.position.lng + 0.001,
          west: marker.position.lng - 0.001,
        },
      }));
    }

    const clusters: ClusterMarker[] = [];
    const processed = new Set<string>();

    markers.forEach(marker => {
      if (processed.has(marker.id)) return;

      const nearbyMarkers = [marker];
      processed.add(marker.id);

      // Find nearby markers
      markers.forEach(otherMarker => {
        if (processed.has(otherMarker.id) || marker.id === otherMarker.id) return;

        const pixelDistance = this.getPixelDistance(
          marker.position,
          otherMarker.position,
          map
        );

        if (pixelDistance <= this.minDistance) {
          nearbyMarkers.push(otherMarker);
          processed.add(otherMarker.id);
        }
      });

      // Create cluster or individual marker
      if (nearbyMarkers.length >= this.minimumClusterSize) {
        clusters.push(this.createDistanceCluster(nearbyMarkers));
      } else {
        nearbyMarkers.forEach(m => {
          clusters.push({
            id: `single_${m.id}`,
            position: m.position,
            count: 1,
            markers: [m],
            bounds: {
              north: m.position.lat + 0.001,
              south: m.position.lat - 0.001,
              east: m.position.lng + 0.001,
              west: m.position.lng - 0.001,
            },
          });
        });
      }
    });

    return clusters;
  }

  /**
   * Calculate pixel distance between two positions
   */
  private getPixelDistance(pos1: MapPosition, pos2: MapPosition, map: google.maps.Map): number {
    const projection = map.getProjection();
    if (!projection) return Infinity;

    const point1 = projection.fromLatLngToPoint(new google.maps.LatLng(pos1.lat, pos1.lng));
    const point2 = projection.fromLatLngToPoint(new google.maps.LatLng(pos2.lat, pos2.lng));
    
    if (!point1 || !point2) return Infinity;

    const scale = Math.pow(2, map.getZoom() || 0);
    
    const dx = (point1.x - point2.x) * scale;
    const dy = (point1.y - point2.y) * scale;
    
    return Math.sqrt(dx * dx + dy * dy);
  }

  /**
   * Create cluster from nearby markers
   */
  private createDistanceCluster(markers: PropertyMarker[]): ClusterMarker {
    const totalLat = markers.reduce((sum, marker) => sum + marker.position.lat, 0);
    const totalLng = markers.reduce((sum, marker) => sum + marker.position.lng, 0);
    
    const centerPosition: MapPosition = {
      lat: totalLat / markers.length,
      lng: totalLng / markers.length,
    };

    const lats = markers.map(m => m.position.lat);
    const lngs = markers.map(m => m.position.lng);
    
    const bounds: MapBounds = {
      north: Math.max(...lats),
      south: Math.min(...lats),
      east: Math.max(...lngs),
      west: Math.min(...lngs),
    };

    return {
      id: `distance_cluster_${Date.now()}_${Math.random()}`,
      position: centerPosition,
      count: markers.length,
      markers,
      bounds,
    };
  }
}

/**
 * Utility functions for clustering
 */

/**
 * Calculate geographic distance between two positions (in meters)
 */
export function calculateDistance(pos1: MapPosition, pos2: MapPosition): number {
  const R = 6371000; // Earth's radius in meters
  const dLat = (pos2.lat - pos1.lat) * Math.PI / 180;
  const dLng = (pos2.lng - pos1.lng) * Math.PI / 180;
  const a = 
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(pos1.lat * Math.PI / 180) * Math.cos(pos2.lat * Math.PI / 180) * 
    Math.sin(dLng / 2) * Math.sin(dLng / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

/**
 * Get optimal cluster icon size based on marker count
 */
export function getClusterIconSize(count: number): number {
  if (count < 10) return 30;
  if (count < 100) return 40;
  if (count < 1000) return 50;
  return 60;
}

/**
 * Get cluster color based on marker count
 */
export function getClusterColor(count: number): string {
  if (count < 10) return '#4F46E5'; // Indigo
  if (count < 50) return '#059669'; // Emerald
  if (count < 100) return '#D97706'; // Amber
  return '#DC2626'; // Red
}