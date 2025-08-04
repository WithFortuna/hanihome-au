import { 
  Shield, 
  Zap, 
  Wifi, 
  Car, 
  Building2, 
  Waves, 
  Snowflake, 
  Flame, 
  Utensils, 
  WashingMachine, 
  Refrigerator, 
  ChefHat,
  Train,
  Bus,
  GraduationCap,
  Hospital,
  ShoppingCart,
  Trees,
  Store,
  Home,
  Flower,
  PawPrint,
  Cigarette,
  Calendar,
  Camera,
  Lock,
  Phone,
  Key,
  UserCheck,
  ArrowUp,
  type LucideIcon
} from 'lucide-react';

export interface PropertyOption {
  id: string;
  label: string;
  category: string;
  icon: LucideIcon;
  description?: string;
  hasPricing?: boolean; // 가격 정보를 받을 수 있는 옵션인지
  isPopular?: boolean; // 인기 옵션 여부
  tags?: string[]; // 검색용 태그
}

export interface PropertyOptionCategory {
  id: string;
  label: string;
  icon: LucideIcon;
  color: string;
  description?: string;
}

export interface PropertyOptionWithPrice extends PropertyOption {
  monthlyFee?: number; // 월 추가 비용
  depositFee?: number; // 보증금 추가
  description?: string;
}

export const PROPERTY_OPTION_CATEGORIES: PropertyOptionCategory[] = [
  {
    id: 'security',
    label: '보안시설',
    icon: Shield,
    color: 'blue',
    description: '건물 보안 및 안전 시설'
  },
  {
    id: 'appliances',
    label: '생활가전',
    icon: Zap,
    color: 'green',
    description: '주방 및 생활 가전제품'
  },
  {
    id: 'building',
    label: '건물시설',
    icon: Building2,
    color: 'purple',
    description: '건물 공용 시설 및 편의시설'
  },
  {
    id: 'location',
    label: '주변환경',
    icon: Trees,
    color: 'emerald',
    description: '근처 교통 및 편의시설'
  },
  {
    id: 'structure',
    label: '공간구성',
    icon: Home,
    color: 'orange',
    description: '매물 공간 구조 및 특징'
  },
  {
    id: 'policy',
    label: '특별조건',
    icon: Calendar,
    color: 'red',
    description: '임대 조건 및 특별 정책'
  },
];

export const PROPERTY_OPTIONS: PropertyOption[] = [
  // 보안시설
  {
    id: 'cctv',
    label: 'CCTV',
    category: 'security',
    icon: Camera,
    description: '건물 내/외부 CCTV 감시시스템',
    isPopular: true,
    tags: ['보안', '안전', 'CCTV']
  },
  {
    id: 'access_control',
    label: '출입통제시스템',
    category: 'security',
    icon: Lock,
    description: '카드키, 비밀번호 등 출입 통제 시스템',
    tags: ['보안', '카드키', '출입']
  },
  {
    id: 'intercom',
    label: '인터폰',
    category: 'security',
    icon: Phone,
    description: '방문자 확인 및 통화 가능한 인터폰',
    tags: ['인터폰', '방문자']
  },
  {
    id: 'digital_lock',
    label: '디지털 도어락',
    category: 'security',
    icon: Key,
    description: '비밀번호, 지문, 카드 등 디지털 도어락',
    isPopular: true,
    tags: ['도어락', '비밀번호', '지문']
  },
  {
    id: 'security_office',
    label: '경비실',
    category: 'security',
    icon: UserCheck,
    description: '24시간 경비실 운영',
    tags: ['경비', '24시간', '관리']
  },

  // 생활가전
  {
    id: 'air_conditioner',
    label: '에어컨',
    category: 'appliances',
    icon: Snowflake,
    description: '냉난방 에어컨 설치',
    hasPricing: true,
    isPopular: true,
    tags: ['에어컨', '냉방', '난방']
  },
  {
    id: 'washing_machine',
    label: '세탁기',
    category: 'appliances',
    icon: WashingMachine,
    description: '세탁기 포함 제공',
    hasPricing: true,
    isPopular: true,
    tags: ['세탁기', '빨래']
  },
  {
    id: 'refrigerator',
    label: '냉장고',
    category: 'appliances',
    icon: Refrigerator,
    description: '냉장고 포함 제공',
    hasPricing: true,
    isPopular: true,
    tags: ['냉장고', '주방']
  },
  {
    id: 'gas_range',
    label: '가스레인지',
    category: 'appliances',
    icon: Flame,
    description: '가스레인지 설치',
    tags: ['가스레인지', '요리', '주방']
  },
  {
    id: 'microwave',
    label: '전자레인지',
    category: 'appliances',
    icon: Zap,
    description: '전자레인지 포함 제공',
    hasPricing: true,
    tags: ['전자레인지', '주방']
  },
  {
    id: 'induction',
    label: '인덕션',
    category: 'appliances',
    icon: Utensils,
    description: '인덕션 쿡탑 설치',
    tags: ['인덕션', '요리', '주방']
  },
  {
    id: 'dishwasher',
    label: '식기세척기',
    category: 'appliances',
    icon: ChefHat,
    description: '식기세척기 포함 제공',
    hasPricing: true,
    tags: ['식기세척기', '주방']
  },

  // 건물시설
  {
    id: 'elevator',
    label: '엘리베이터',
    category: 'building',
    icon: ArrowUp,
    description: '엘리베이터 이용 가능',
    isPopular: true,
    tags: ['엘리베이터', '이동']
  },
  {
    id: 'parking',
    label: '주차장',
    category: 'building',
    icon: Car,
    description: '전용 또는 공용 주차 공간',
    hasPricing: true,
    isPopular: true,
    tags: ['주차장', '주차', '차량']
  },
  {
    id: 'wifi',
    label: '무선인터넷',
    category: 'building',
    icon: Wifi,
    description: '고속 무선 인터넷 제공',
    hasPricing: true,
    tags: ['와이파이', '인터넷', '무선']
  },

  // 주변환경
  {
    id: 'subway_nearby',
    label: '지하철역 근처',
    category: 'location',
    icon: Train,
    description: '도보 10분 이내 지하철역',
    isPopular: true,
    tags: ['지하철', '교통', '역']
  },
  {
    id: 'bus_stop_nearby',
    label: '버스정류장 근처',
    category: 'location',
    icon: Bus,
    description: '도보 5분 이내 버스정류장',
    tags: ['버스', '교통', '정류장']
  },
  {
    id: 'school_nearby',
    label: '학교 근처',
    category: 'location',
    icon: GraduationCap,
    description: '도보 15분 이내 초/중/고등학교',
    tags: ['학교', '교육', '아이']
  },
  {
    id: 'hospital_nearby',
    label: '병원 근처',
    category: 'location',
    icon: Hospital,
    description: '도보 10분 이내 병원/의원',
    tags: ['병원', '의료', '건강']
  },
  {
    id: 'mart_nearby',
    label: '마트 근처',
    category: 'location',
    icon: ShoppingCart,
    description: '도보 10분 이내 대형마트',
    isPopular: true,
    tags: ['마트', '쇼핑', '생필품']
  },
  {
    id: 'park_nearby',
    label: '공원 근처',
    category: 'location',
    icon: Trees,
    description: '도보 10분 이내 공원',
    tags: ['공원', '자연', '산책']
  },
  {
    id: 'convenience_store_nearby',
    label: '편의점 근처',
    category: 'location',
    icon: Store,
    description: '도보 5분 이내 편의점',
    tags: ['편의점', '24시간', '생필품']
  },

  // 공간구성
  {
    id: 'balcony',
    label: '베란다/발코니',
    category: 'structure',
    icon: Flower,
    description: '베란다 또는 발코니 공간',
    tags: ['베란다', '발코니', '공간']
  },
  {
    id: 'rooftop',
    label: '옥상 이용',
    category: 'structure',
    icon: ArrowUp,
    description: '옥상 공간 이용 가능',
    tags: ['옥상', '루프탑', '공간']
  },

  // 특별조건
  {
    id: 'pet_allowed',
    label: '반려동물 가능',
    category: 'policy',
    icon: PawPrint,
    description: '반려동물 키우기 가능',
    tags: ['반려동물', '펫', '강아지', '고양이']
  },
  {
    id: 'smoking_allowed',
    label: '흡연 가능',
    category: 'policy',
    icon: Cigarette,
    description: '실내 흡연 가능',
    tags: ['흡연', '담배']
  },
  {
    id: 'short_term_rental',
    label: '단기임대 가능',
    category: 'policy',
    icon: Calendar,
    description: '1개월 미만 단기 임대 가능',
    tags: ['단기', '임대', '월세']
  },
];

// 카테고리별 옵션 그룹핑
export const getOptionsByCategory = () => {
  return PROPERTY_OPTION_CATEGORIES.map(category => ({
    ...category,
    options: PROPERTY_OPTIONS.filter(option => option.category === category.id)
  }));
};

// 인기 옵션 가져오기
export const getPopularOptions = () => {
  return PROPERTY_OPTIONS.filter(option => option.isPopular);
};

// 가격 설정 가능한 옵션 가져오기
export const getPricingOptions = () => {
  return PROPERTY_OPTIONS.filter(option => option.hasPricing);
};

// 옵션 검색
export const searchOptions = (query: string) => {
  const lowercaseQuery = query.toLowerCase();
  return PROPERTY_OPTIONS.filter(option => 
    option.label.toLowerCase().includes(lowercaseQuery) ||
    option.description?.toLowerCase().includes(lowercaseQuery) ||
    option.tags?.some(tag => tag.toLowerCase().includes(lowercaseQuery))
  );
};