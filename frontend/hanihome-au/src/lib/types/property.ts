// Property related types
export enum PropertyType {
  APARTMENT = 'APARTMENT',
  VILLA = 'VILLA',
  STUDIO = 'STUDIO',
  TWO_ROOM = 'TWO_ROOM',
  THREE_ROOM = 'THREE_ROOM',
  OFFICETEL = 'OFFICETEL',
  HOUSE = 'HOUSE',
}

export enum RentalType {
  MONTHLY = 'MONTHLY',
  JEONSE = 'JEONSE',
  SALE = 'SALE',
}

export enum PropertyStatus {
  PENDING_APPROVAL = 'PENDING_APPROVAL',
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED',
}

export const PropertyTypeDisplayNames = {
  [PropertyType.APARTMENT]: '아파트',
  [PropertyType.VILLA]: '빌라',
  [PropertyType.STUDIO]: '원룸',
  [PropertyType.TWO_ROOM]: '투룸',
  [PropertyType.THREE_ROOM]: '쓰리룸',
  [PropertyType.OFFICETEL]: '오피스텔',
  [PropertyType.HOUSE]: '단독주택',
};

export const RentalTypeDisplayNames = {
  [RentalType.MONTHLY]: '월세',
  [RentalType.JEONSE]: '전세',
  [RentalType.SALE]: '매매',
};

export interface PropertyFormData {
  // Basic Information
  title: string;
  description: string;
  propertyType: PropertyType;
  rentalType: RentalType;

  // Location Information
  address: string;
  detailAddress?: string;
  zipCode?: string;
  city?: string;
  district?: string;
  latitude?: number;
  longitude?: number;

  // Price Information
  deposit?: number;
  monthlyRent?: number;
  maintenanceFee?: number;

  // Property Details
  area?: number;
  rooms?: number;
  bathrooms?: number;
  floor?: number;
  totalFloors?: number;
  availableDate?: string;

  // Features
  parkingAvailable?: boolean;
  petAllowed?: boolean;
  furnished?: boolean;
  shortTermAvailable?: boolean;

  // Options and Images
  options: string[];
  imageUrls: string[];
}

export interface PropertyFormStep {
  id: string;
  title: string;
  description: string;
  fields: (keyof PropertyFormData)[];
}

export const PROPERTY_FORM_STEPS: PropertyFormStep[] = [
  {
    id: 'basic',
    title: '기본 정보',
    description: '매물의 기본 정보를 입력해주세요.',
    fields: ['title', 'description', 'propertyType', 'rentalType'],
  },
  {
    id: 'details',
    title: '상세 정보',
    description: '매물의 상세 정보를 입력해주세요.',
    fields: [
      'area',
      'rooms',
      'bathrooms',
      'floor',
      'totalFloors',
      'availableDate',
      'deposit',
      'monthlyRent',
      'maintenanceFee',
    ],
  },
  {
    id: 'options',
    title: '옵션 선택',
    description: '매물의 옵션과 특징을 선택해주세요.',
    fields: ['parkingAvailable', 'petAllowed', 'furnished', 'shortTermAvailable', 'options'],
  },
  {
    id: 'images',
    title: '이미지 업로드',
    description: '매물 사진을 업로드해주세요.',
    fields: ['imageUrls'],
  },
];

// Common property options
export const PROPERTY_OPTIONS = [
  // 보안시설
  'CCTV',
  '출입통제시스템',
  '인터폰',
  '도어락',
  '경비실',
  
  // 편의시설
  '에어컨',
  '세탁기',
  '냉장고',
  '가스레인지',
  '전자레인지',
  '인덕션',
  '식기세척기',
  '건조기',
  '엘리베이터',
  '주차장',
  
  // 주변환경
  '지하철역 근처',
  '버스정류장 근처',
  '학교 근처',
  '병원 근처',
  '마트 근처',
  '공원 근처',
  '편의점 근처',
  
  // 기타
  '베란다',
  '발코니',
  '옥상',
  '반려동물 가능',
  '흡연 가능',
  '단기임대 가능',
];