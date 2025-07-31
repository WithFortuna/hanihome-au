/**
 * Google Maps API Loader Utility
 */

import { Loader } from '@googlemaps/js-api-loader';
import { GOOGLE_MAPS_CONFIG } from './config';

class GoogleMapsLoader {
  private static instance: GoogleMapsLoader | null = null;
  private loader: Loader;
  private loadPromise: Promise<typeof google> | null = null;

  private constructor() {
    this.loader = new Loader({
      apiKey: GOOGLE_MAPS_CONFIG.apiKey,
      version: 'weekly',
      libraries: [...GOOGLE_MAPS_CONFIG.libraries],
      language: GOOGLE_MAPS_CONFIG.language,
      region: GOOGLE_MAPS_CONFIG.region,
    });
  }

  public static getInstance(): GoogleMapsLoader {
    if (!GoogleMapsLoader.instance) {
      GoogleMapsLoader.instance = new GoogleMapsLoader();
    }
    return GoogleMapsLoader.instance;
  }

  public async load(): Promise<typeof google> {
    if (!this.loadPromise) {
      this.loadPromise = this.loader.load();
    }
    return this.loadPromise;
  }

  public isLoaded(): boolean {
    return typeof google !== 'undefined' && typeof google.maps !== 'undefined';
  }

  public getLoader(): Loader {
    return this.loader;
  }
}

export const googleMapsLoader = GoogleMapsLoader.getInstance();

export async function loadGoogleMaps(): Promise<typeof google> {
  return googleMapsLoader.load();
}

export function isGoogleMapsLoaded(): boolean {
  return googleMapsLoader.isLoaded();
}