'use client';

import React, { useState, useEffect, useCallback, useRef } from 'react';
import GoogleMap from './google-map';
import { PropertyMarker } from './property-marker';
import { ClusterMarker, SpiderMarkers } from './cluster-marker';
import { PropertyInfoWindow } from './property-marker';
import MobileMapControls from './mobile-map-controls';
import { useGoogleMaps } from '@/hooks/use-google-maps';
import { useMobileDetection } from '@/hooks/use-mobile-detection';
import { 
  PropertyMarker as PropertyMarkerType, 
  MapPosition, 
  MapBounds,
  MapEventHandlers 
} from '@/lib/maps/types';
import { 
  MarkerClusterer, 
  ClusterMarker as ClusterMarkerType,
  ClusteringOptions 
} from '@/lib/maps/clustering';
import {
  filterPropertiesInViewport,
  prioritizeProperties,
  debounce,
  MarkerPool,
  MapPerformanceMonitor,
  PERFORMANCE_THRESHOLDS
} from '@/lib/maps/viewport-optimization';

interface PropertyMapProps {
  properties: PropertyMarkerType[];
  center?: MapPosition;
  zoom?: number;
  className?: string;
  style?: React.CSSProperties;
  onPropertyClick?: (property: PropertyMarkerType) => void;
  onBoundsChanged?: (bounds: MapBounds) => void;
  onZoomChanged?: (zoom: number) => void;
  clustering?: boolean;
  clusteringOptions?: ClusteringOptions;
  showInfoWindow?: boolean;
}

export default function PropertyMap({
  properties = [],
  center,
  zoom = 12,
  className = '',
  style,
  onPropertyClick,
  onBoundsChanged,
  onZoomChanged,
  clustering = true,
  clusteringOptions = {},
  showInfoWindow = true,
}: PropertyMapProps) {
  const { isMobile, isTablet, screenSize } = useMobileDetection();
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const [currentZoom, setCurrentZoom] = useState(zoom);
  const [currentBounds, setCurrentBounds] = useState<MapBounds | null>(null);
  const [clusters, setClusters] = useState<ClusterMarkerType[]>([]);
  const [selectedProperty, setSelectedProperty] = useState<PropertyMarkerType | null>(null);
  const [infoWindowOpen, setInfoWindowOpen] = useState(false);
  const [expandedCluster, setExpandedCluster] = useState<string | null>(null);
  const [mapType, setMapType] = useState<google.maps.MapTypeId>(google.maps.MapTypeId.ROADMAP);

  const clustererRef = useRef<MarkerClusterer | null>(null);
  const markerPoolRef = useRef<MarkerPool>(new MarkerPool());
  const performanceMonitorRef = useRef<MapPerformanceMonitor>(new MapPerformanceMonitor());
  const [visibleProperties, setVisibleProperties] = useState<PropertyMarkerType[]>([]);
  const [isLoadingMarkers, setIsLoadingMarkers] = useState(false);

  // Initialize clusterer
  useEffect(() => {
    if (clustering) {
      clustererRef.current = new MarkerClusterer(clusteringOptions);
    }
  }, [clustering, clusteringOptions]);

  // Handle map load
  const handleMapLoad = useCallback((loadedMap: google.maps.Map) => {
    setMap(loadedMap);
    
    // Set initial bounds
    const bounds = loadedMap.getBounds();
    if (bounds) {
      const newBounds: MapBounds = {
        north: bounds.getNorthEast().lat(),
        south: bounds.getSouthWest().lat(),
        east: bounds.getNorthEast().lng(),
        west: bounds.getSouthWest().lng(),
      };
      setCurrentBounds(newBounds);
      onBoundsChanged?.(newBounds);
    }
    
    // Set initial zoom
    const initialZoom = loadedMap.getZoom() || zoom;
    setCurrentZoom(initialZoom);
    onZoomChanged?.(initialZoom);
  }, [zoom, onBoundsChanged, onZoomChanged]);

  // Optimized viewport-based property filtering with debouncing
  const updateVisibleProperties = useCallback(
    debounce((allProperties: PropertyMarkerType[], bounds: MapBounds, zoom: number, center: MapPosition) => {
      setIsLoadingMarkers(true);
      const endTimer = performanceMonitorRef.current.startTimer('markerRender');
      
      try {
        // Filter properties within viewport with buffer
        const viewportProperties = filterPropertiesInViewport(allProperties, bounds);
        
        // Prioritize and limit properties for performance
        const prioritizedProperties = prioritizeProperties(
          viewportProperties, 
          center, 
          zoom,
          PERFORMANCE_THRESHOLDS.MAX_VISIBLE_MARKERS
        );
        
        setVisibleProperties(prioritizedProperties);
        performanceMonitorRef.current.recordViewportUpdate();
      } finally {
        endTimer();
        setIsLoadingMarkers(false);
      }
    }, PERFORMANCE_THRESHOLDS.DEBOUNCE_DELAY),
    []
  );

  // Update visible properties when viewport changes
  useEffect(() => {
    if (!map || !currentBounds) return;
    
    const mapCenter = map.getCenter();
    if (mapCenter) {
      const center: MapPosition = {
        lat: mapCenter.lat(),
        lng: mapCenter.lng(),
      };
      
      updateVisibleProperties(properties, currentBounds, currentZoom, center);
    }
  }, [properties, currentBounds, currentZoom, map, updateVisibleProperties]);

  // Update clusters when visible properties, zoom, or bounds change
  useEffect(() => {
    if (!map || !clustererRef.current || !currentBounds) return;

    const endTimer = performanceMonitorRef.current.startTimer('clustering');
    
    try {
      if (clustering && visibleProperties.length > 0) {
        const newClusters = clustererRef.current.cluster(
          visibleProperties,
          currentZoom,
          currentBounds
        );
        setClusters(newClusters);
      } else {
        // No clustering, create individual clusters
        const individualClusters = visibleProperties.map(property => ({
          id: `single_${property.id}`,
          position: property.position,
          count: 1,
          markers: [property],
          bounds: {
            north: property.position.lat + 0.001,
            south: property.position.lat - 0.001,
            east: property.position.lng + 0.001,
            west: property.position.lng - 0.001,
          } as MapBounds,
        }));
        setClusters(individualClusters);
      }
    } finally {
      endTimer();
    }
  }, [visibleProperties, currentZoom, currentBounds, clustering, map]);

  // Map event handlers
  const mapEventHandlers: MapEventHandlers = {
    onBoundsChanged: useCallback((bounds: MapBounds) => {
      setCurrentBounds(bounds);
      onBoundsChanged?.(bounds);
    }, [onBoundsChanged]),

    onZoomChanged: useCallback((zoom: number) => {
      setCurrentZoom(zoom);
      onZoomChanged?.(zoom);
      // Close expanded cluster on zoom change
      setExpandedCluster(null);
    }, [onZoomChanged]),
  };

  // Handle property marker click
  const handlePropertyClick = useCallback((property: PropertyMarkerType) => {
    setSelectedProperty(property);
    setInfoWindowOpen(true);
    onPropertyClick?.(property);
  }, [onPropertyClick]);

  // Handle cluster click
  const handleClusterClick = useCallback((cluster: ClusterMarkerType) => {
    if (!map) return;

    if (cluster.count === 1) {
      // Single marker in cluster, treat as property click
      handlePropertyClick(cluster.markers[0]);
      return;
    }

    if (currentZoom >= 18) {
      // Max zoom reached, expand into spider
      setExpandedCluster(expandedCluster === cluster.id ? null : cluster.id);
    } else {
      // Zoom into cluster bounds
      const bounds = new google.maps.LatLngBounds();
      cluster.markers.forEach(marker => {
        bounds.extend(new google.maps.LatLng(marker.position.lat, marker.position.lng));
      });
      map.fitBounds(bounds);
      
      // Add some padding
      setTimeout(() => {
        const currentZoom = map.getZoom();
        if (currentZoom && currentZoom > 18) {
          map.setZoom(18);
        }
      }, 100);
    }
  }, [map, currentZoom, expandedCluster, handlePropertyClick]);

  // Handle spider marker click
  const handleSpiderMarkerClick = useCallback((markerId: string) => {
    const property = properties.find(p => p.id === markerId);
    if (property) {
      handlePropertyClick(property);
    }
    setExpandedCluster(null);
  }, [properties, handlePropertyClick]);

  // Close info window
  const handleInfoWindowClose = useCallback(() => {
    setInfoWindowOpen(false);
    setSelectedProperty(null);
  }, []);

  // Handle map type toggle for mobile
  const handleToggleMapType = useCallback(() => {
    if (!map) return;
    
    const newMapType = mapType === google.maps.MapTypeId.ROADMAP 
      ? google.maps.MapTypeId.SATELLITE 
      : google.maps.MapTypeId.ROADMAP;
    
    setMapType(newMapType);
    map.setMapTypeId(newMapType);
  }, [map, mapType]);

  // Handle location request from mobile controls
  const handleLocationRequest = useCallback(() => {
    // This will be handled by the MobileMapControls component
    // Just for potential callback purposes
  }, []);

  // Cleanup on unmount - prevent memory leaks
  useEffect(() => {
    return () => {
      // Clean up marker pool
      markerPoolRef.current.releaseAllMarkers();
      
      // Clean up clusterer
      if (clustererRef.current) {
        clustererRef.current = null;
      }
      
      // Clean up event listeners (done by useMapEvents hook)
      if (map) {
        google.maps.event.clearInstanceListeners(map);
      }
    };
  }, [map]);

  return (
    <div className={`relative ${className}`}>
      <GoogleMap
        center={center}
        zoom={zoom}
        style={style}
        onMapLoad={handleMapLoad}
        eventHandlers={mapEventHandlers}
        className="w-full h-full"
      />

      {/* Render markers and clusters */}
      {map && clusters.map(cluster => (
        <React.Fragment key={cluster.id}>
          {cluster.count === 1 ? (
            // Single property marker
            <PropertyMarker
              marker={cluster.markers[0]}
              map={map}
              onClick={handlePropertyClick}
            />
          ) : (
            // Cluster marker
            <ClusterMarker
              cluster={cluster}
              map={map}
              onClick={handleClusterClick}
            />
          )}

          {/* Spider markers for expanded clusters */}
          {expandedCluster === cluster.id && (
            <SpiderMarkers
              cluster={cluster}
              map={map}
              isExpanded={true}
              onMarkerClick={handleSpiderMarkerClick}
            />
          )}
        </React.Fragment>
      ))}

      {/* Info window */}
      {showInfoWindow && selectedProperty && map && (
        <PropertyInfoWindow
          marker={selectedProperty}
          map={map}
          isOpen={infoWindowOpen}
          onClose={handleInfoWindowClose}
        />
      )}

      {/* Desktop Map controls overlay */}
      {!isMobile && !isTablet && (
        <div className="absolute top-4 right-4 flex flex-col gap-2 z-10">
          {/* Zoom level indicator */}
          <div className="bg-white px-3 py-1 rounded-lg shadow-lg text-sm font-medium text-gray-600">
            Zoom: {currentZoom}
          </div>

          {/* Property count */}
          <div className="bg-white px-3 py-1 rounded-lg shadow-lg text-sm font-medium text-gray-600">
            Properties: {visibleProperties.length}/{properties.length}
            {isLoadingMarkers && <span className="ml-1 text-blue-500">⟳</span>}
          </div>

          {/* Cluster count */}
          {clustering && (
            <div className="bg-white px-3 py-1 rounded-lg shadow-lg text-sm font-medium text-gray-600">
              Clusters: {clusters.filter(c => c.count > 1).length}
            </div>
          )}
        </div>
      )}

      {/* Desktop Map legend */}
      {!isMobile && !isTablet && (
        <div className="absolute bottom-4 left-4 bg-white p-3 rounded-lg shadow-lg max-w-xs z-10">
          <h4 className="font-medium text-gray-900 mb-2">Property Types</h4>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-indigo-500"></div>
              <span>House</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-emerald-500"></div>
              <span>Apartment</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-red-500"></div>
              <span>Townhouse</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-3 h-3 rounded-full bg-violet-500"></div>
              <span>Studio</span>
            </div>
          </div>
          
          {clustering && (
            <div className="mt-3 pt-2 border-t border-gray-200">
              <p className="text-xs text-gray-600">
                Click clusters to zoom in or expand at max zoom
              </p>
            </div>
          )}
        </div>
      )}

      {/* Mobile Map Controls */}
      <MobileMapControls
        map={map}
        propertyCount={visibleProperties.length}
        clusterCount={clusters.filter(c => c.count > 1).length}
        currentZoom={currentZoom}
        onLocationRequest={handleLocationRequest}
        onToggleMapType={handleToggleMapType}
      />
      
      {/* Loading indicator for mobile */}
      {isLoadingMarkers && (isMobile || isTablet) && (
        <div className="absolute top-4 left-4 bg-white px-3 py-2 rounded-lg shadow-lg z-20">
          <div className="flex items-center gap-2 text-sm text-gray-600">
            <div className="animate-spin rounded-full h-4 w-4 border-2 border-blue-600 border-t-transparent"></div>
            <span>Loading markers...</span>
          </div>
        </div>
      )}
    </div>
  );
}