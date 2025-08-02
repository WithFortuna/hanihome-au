'use client';

import React, { useMemo } from 'react';
import { CheckboxGroup, CheckboxOption } from '@/components/ui/checkbox-group';
import { PROPERTY_OPTIONS } from '@/lib/types/property';

interface PropertyOptionsInterfaceProps {
  selectedOptions: string[];
  onOptionsChange: (options: string[]) => void;
  maxSelections?: number;
  variant?: 'default' | 'card' | 'compact';
  columns?: 1 | 2 | 3 | 4;
}

export function PropertyOptionsInterface({
  selectedOptions,
  onOptionsChange,
  maxSelections,
  variant = 'default',
  columns = 3,
}: PropertyOptionsInterfaceProps) {
  // Convert PROPERTY_OPTIONS to CheckboxOption format with categories
  const checkboxOptions: CheckboxOption[] = useMemo(() => {
    const optionCategories = {
      // Security options (first 5)
      'CCTV': '보안시설',
      '출입통제시스템': '보안시설',
      '인터폰': '보안시설',
      '도어락': '보안시설',
      '경비실': '보안시설',
      
      // Appliances (next 10)
      '에어컨': '생활가전',
      '세탁기': '생활가전',
      '냉장고': '생활가전',
      '가스레인지': '생활가전',
      '전자레인지': '생활가전',
      '인덕션': '생활가전',
      '식기세척기': '생활가전',
      '건조기': '생활가전',
      '엘리베이터': '건물시설',
      '주차장': '건물시설',
      
      // Location (next 7)
      '지하철역 근처': '주변환경',
      '버스정류장 근처': '주변환경',
      '학교 근처': '주변환경',
      '병원 근처': '주변환경',
      '마트 근처': '주변환경',
      '공원 근처': '주변환경',
      '편의점 근처': '주변환경',
      
      // Others
      '베란다': '공간구성',
      '발코니': '공간구성',
      '옥상': '공간구성',
      '반려동물 가능': '특별조건',
      '흡연 가능': '특별조건',
      '단기임대 가능': '특별조건',
    };

    return PROPERTY_OPTIONS.map(option => ({
      value: option,
      label: option,
      category: optionCategories[option as keyof typeof optionCategories] || '기타',
      description: getOptionDescription(option),
    }));
  }, []);

  return (
    <CheckboxGroup
      options={checkboxOptions}
      selectedValues={selectedOptions}
      onChange={onOptionsChange}
      title="매물 옵션 선택"
      description="매물에 포함된 옵션들을 선택해주세요. 여러 개 선택 가능합니다."
      maxSelections={maxSelections}
      enableSearch={true}
      enableCategories={true}
      columns={columns}
      variant={variant}
      showSelectedCount={true}
    />
  );
}

// Helper function to provide descriptions for options
function getOptionDescription(option: string): string | undefined {
  const descriptions: Record<string, string> = {
    'CCTV': '건물 내/외부 CCTV 설치',
    '출입통제시스템': '카드키, 비밀번호 등 출입 통제',
    '인터폰': '방문자 확인 인터폰 시설',
    '도어락': '디지털 도어락 설치',
    '경비실': '24시간 경비실 운영',
    '에어컨': '에어컨 설치',
    '세탁기': '세탁기 포함',
    '냉장고': '냉장고 포함',
    '가스레인지': '가스레인지 설치',
    '전자레인지': '전자레인지 포함',
    '인덕션': '인덕션 설치',
    '식기세척기': '식기세척기 포함',
    '건조기': '건조기 포함',
    '엘리베이터': '엘리베이터 이용 가능',
    '주차장': '주차 공간 이용 가능',
    '지하철역 근처': '도보 10분 이내 지하철역',
    '버스정류장 근처': '도보 5분 이내 버스정류장',
    '학교 근처': '도보 15분 이내 학교',
    '병원 근처': '도보 10분 이내 병원',
    '마트 근처': '도보 10분 이내 대형마트',
    '공원 근처': '도보 10분 이내 공원',
    '편의점 근처': '도보 5분 이내 편의점',
    '베란다': '베란다 공간 있음',
    '발코니': '발코니 공간 있음',
    '옥상': '옥상 이용 가능',
    '반려동물 가능': '반려동물 키우기 가능',
    '흡연 가능': '실내 흡연 가능',
    '단기임대 가능': '1개월 미만 단기 임대 가능',
  };

  return descriptions[option];
}