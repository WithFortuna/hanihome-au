'use client';

import React from 'react';
import { PropertyRegistrationForm } from '@/components/property/property-registration-form';
import { PropertyFormData } from '@/lib/types/property';

export default function PropertyRegisterPage() {
  const handleSubmit = async (data: PropertyFormData) => {
    console.log('Form submitted:', data);
    
    // TODO: Implement actual API call
    // This is a placeholder for the actual submission logic
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      alert('매물이 성공적으로 등록되었습니다!');
      
      // In a real app, you would redirect to the property list or detail page
      // router.push('/dashboard/properties');
    } catch (error) {
      console.error('Registration failed:', error);
      alert('매물 등록에 실패했습니다. 다시 시도해주세요.');
    }
  };

  const handleCancel = () => {
    // TODO: Implement navigation back or show confirmation dialog
    console.log('Registration cancelled');
    if (confirm('매물 등록을 취소하시겠습니까? 입력한 정보가 모두 사라집니다.')) {
      // router.back();
      window.history.back();
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        <PropertyRegistrationForm
          onSubmit={handleSubmit}
          onCancel={handleCancel}
        />
      </div>
    </div>
  );
}