import imageCompression from 'browser-image-compression';

export interface CompressionOptions {
  maxSizeMB?: number;
  maxWidthOrHeight?: number;
  useWebWorker?: boolean;
  fileType?: string;
  initialQuality?: number;
}

export interface CompressionResult {
  compressedFile: File;
  originalSize: number;
  compressedSize: number;
  compressionRatio: number;
}

const DEFAULT_OPTIONS: CompressionOptions = {
  maxSizeMB: 1, // Maximum file size in MB
  maxWidthOrHeight: 1920, // Maximum width or height in pixels
  useWebWorker: true,
  initialQuality: 0.8,
};

/**
 * Compress an image file
 */
export async function compressImage(
  file: File,
  options: CompressionOptions = {}
): Promise<CompressionResult> {
  const compressionOptions = { ...DEFAULT_OPTIONS, ...options };

  try {
    const compressedFile = await imageCompression(file, compressionOptions);
    
    const originalSize = file.size;
    const compressedSize = compressedFile.size;
    const compressionRatio = Math.round(((originalSize - compressedSize) / originalSize) * 100);

    return {
      compressedFile,
      originalSize,
      compressedSize,
      compressionRatio,
    };
  } catch (error) {
    console.error('Image compression failed:', error);
    throw new Error('이미지 압축에 실패했습니다.');
  }
}

/**
 * Generate different sizes of the same image for responsive display
 */
export async function generateImageSizes(
  file: File,
  sizes: { name: string; maxWidth: number; quality?: number }[]
): Promise<{ [key: string]: File }> {
  const results: { [key: string]: File } = {};

  for (const size of sizes) {
    try {
      const options: CompressionOptions = {
        maxWidthOrHeight: size.maxWidth,
        maxSizeMB: 5, // Higher limit for different sizes
        useWebWorker: true,
        initialQuality: size.quality || 0.8,
      };

      const compressed = await compressImage(file, options);
      
      // Rename file with size suffix
      const nameParts = file.name.split('.');
      const extension = nameParts.pop();
      const baseName = nameParts.join('.');
      
      const newFile = new File(
        [compressed.compressedFile],
        `${baseName}_${size.name}.${extension}`,
        { type: compressed.compressedFile.type }
      );
      
      results[size.name] = newFile;
    } catch (error) {
      console.warn(`Failed to generate ${size.name} size for ${file.name}:`, error);
    }
  }

  return results;
}

/**
 * Validate image file before compression
 */
export function validateImageFile(file: File): { isValid: boolean; error?: string } {
  // Check file type
  if (!file.type.startsWith('image/')) {
    return { isValid: false, error: '이미지 파일만 업로드할 수 있습니다.' };
  }

  // Check supported formats
  const supportedTypes = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];
  if (!supportedTypes.includes(file.type)) {
    return { 
      isValid: false, 
      error: 'JPEG, PNG, WebP, GIF 형식의 이미지만 지원됩니다.' 
    };
  }

  // Check file size (50MB limit before compression)
  const maxSizeBytes = 50 * 1024 * 1024; // 50MB
  if (file.size > maxSizeBytes) {
    return { 
      isValid: false, 
      error: '이미지 파일은 50MB 이하여야 합니다.' 
    };
  }

  return { isValid: true };
}

/**
 * Get image dimensions without loading the full image
 */
export function getImageDimensions(file: File): Promise<{ width: number; height: number }> {
  return new Promise((resolve, reject) => {
    const img = new Image();
    const url = URL.createObjectURL(file);

    img.onload = () => {
      URL.revokeObjectURL(url);
      resolve({ width: img.naturalWidth, height: img.naturalHeight });
    };

    img.onerror = () => {
      URL.revokeObjectURL(url);
      reject(new Error('이미지 크기를 읽을 수 없습니다.'));
    };

    img.src = url;
  });
}

/**
 * Format file size in human readable format
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 Bytes';
  
  const k = 1024;
  const sizes = ['Bytes', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

/**
 * Property image size configurations
 */
export const PROPERTY_IMAGE_SIZES = [
  { name: 'thumbnail', maxWidth: 300, quality: 0.7 },
  { name: 'medium', maxWidth: 800, quality: 0.8 },
  { name: 'large', maxWidth: 1920, quality: 0.9 },
] as const;

/**
 * Default compression options for property images
 */
export const PROPERTY_IMAGE_COMPRESSION_OPTIONS: CompressionOptions = {
  maxSizeMB: 2,
  maxWidthOrHeight: 1920,
  useWebWorker: true,
  initialQuality: 0.85,
};