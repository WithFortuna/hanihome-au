/**
 * Mobile Detection Hook
 */

import { useState, useEffect } from 'react';

export interface MobileBreakpoints {
  isMobile: boolean;
  isTablet: boolean;
  isDesktop: boolean;
  isTouchDevice: boolean;
  screenSize: 'mobile' | 'tablet' | 'desktop';
  width: number;
  height: number;
}

const BREAKPOINTS = {
  mobile: 768,
  tablet: 1024,
} as const;

export function useMobileDetection(): MobileBreakpoints {
  const [breakpoints, setBreakpoints] = useState<MobileBreakpoints>({
    isMobile: false,
    isTablet: false,
    isDesktop: true,
    isTouchDevice: false,
    screenSize: 'desktop',
    width: 1920,
    height: 1080,
  });

  useEffect(() => {
    const updateBreakpoints = () => {
      const width = window.innerWidth;
      const height = window.innerHeight;
      const isMobile = width < BREAKPOINTS.mobile;
      const isTablet = width >= BREAKPOINTS.mobile && width < BREAKPOINTS.tablet;
      const isDesktop = width >= BREAKPOINTS.tablet;
      const isTouchDevice = 'ontouchstart' in window || navigator.maxTouchPoints > 0;

      setBreakpoints({
        isMobile,
        isTablet,
        isDesktop,
        isTouchDevice,
        screenSize: isMobile ? 'mobile' : isTablet ? 'tablet' : 'desktop',
        width,
        height,
      });
    };

    // Initial check
    updateBreakpoints();

    // Listen for resize events
    window.addEventListener('resize', updateBreakpoints);
    
    // Listen for orientation change on mobile
    window.addEventListener('orientationchange', () => {
      // Small delay to ensure accurate dimensions after orientation change
      setTimeout(updateBreakpoints, 100);
    });

    return () => {
      window.removeEventListener('resize', updateBreakpoints);
      window.removeEventListener('orientationchange', updateBreakpoints);
    };
  }, []);

  return breakpoints;
}