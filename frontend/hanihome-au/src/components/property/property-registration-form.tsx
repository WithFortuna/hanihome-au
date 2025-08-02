'use client';

import React, { useState, useCallback } from 'react';
import { useForm, FormProvider } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { 
  PropertyFormData, 
  PROPERTY_FORM_STEPS, 
  PropertyType, 
  RentalType,
  PropertyTypeDisplayNames,
  RentalTypeDisplayNames 
} from '@/lib/types/property';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Progress } from '@/components/ui/progress';
import { ChevronLeft, ChevronRight, Check } from 'lucide-react';
import { PropertyBasicInfoStep } from './steps/property-basic-info-step';
import { PropertyDetailsStep } from './steps/property-details-step';
import { PropertyOptionsStep } from './steps/property-options-step';
import { PropertyImagesStep } from './steps/property-images-step';

// Validation schema using Zod
const propertyFormSchema = z.object({
  title: z.string().min(1, '제목은 필수입니다').max(200, '제목은 200자 이하로 입력해주세요'),
  description: z.string().optional(),
  propertyType: z.nativeEnum(PropertyType, { required_error: '매물 유형을 선택해주세요' }),
  rentalType: z.nativeEnum(RentalType, { required_error: '임대 유형을 선택해주세요' }),
  address: z.string().min(1, '주소는 필수입니다').max(500, '주소는 500자 이하로 입력해주세요'),
  detailAddress: z.string().max(100, '상세주소는 100자 이하로 입력해주세요').optional(),
  zipCode: z.string().max(10, '우편번호는 10자 이하로 입력해주세요').optional(),
  city: z.string().max(50, '시/도는 50자 이하로 입력해주세요').optional(),
  district: z.string().max(50, '구/군은 50자 이하로 입력해주세요').optional(),
  latitude: z.number().optional(),
  longitude: z.number().optional(),
  deposit: z.number().min(0, '보증금은 0 이상이어야 합니다').optional(),
  monthlyRent: z.number().min(0, '월세는 0 이상이어야 합니다').optional(),
  maintenanceFee: z.number().min(0, '관리비는 0 이상이어야 합니다').optional(),
  area: z.number().min(0, '면적은 0 이상이어야 합니다').optional(),
  rooms: z.number().min(0, '방 개수는 0 이상이어야 합니다').optional(),
  bathrooms: z.number().min(0, '욕실 개수는 0 이상이어야 합니다').optional(),
  floor: z.number().optional(),
  totalFloors: z.number().min(1, '총 층수는 1 이상이어야 합니다').optional(),
  availableDate: z.string().optional(),
  parkingAvailable: z.boolean().optional(),
  petAllowed: z.boolean().optional(),
  furnished: z.boolean().optional(),
  shortTermAvailable: z.boolean().optional(),
  options: z.array(z.string()).default([]),
  imageUrls: z.array(z.string()).default([]),
});

interface PropertyRegistrationFormProps {
  onSubmit: (data: PropertyFormData) => Promise<void>;
  onCancel?: () => void;
  initialData?: Partial<PropertyFormData>;
  isLoading?: boolean;
}

export function PropertyRegistrationForm({
  onSubmit,
  onCancel,
  initialData,
  isLoading = false,
}: PropertyRegistrationFormProps) {
  const [currentStep, setCurrentStep] = useState(0);
  const [completedSteps, setCompletedSteps] = useState<number[]>([]);

  const methods = useForm<PropertyFormData>({
    resolver: zodResolver(propertyFormSchema),
    defaultValues: {
      title: '',
      description: '',
      address: '',
      detailAddress: '',
      options: [],
      imageUrls: [],
      parkingAvailable: false,
      petAllowed: false,
      furnished: false,
      shortTermAvailable: false,
      ...initialData,
    },
    mode: 'onChange',
  });

  const { handleSubmit, trigger, formState: { errors, isValid } } = methods;

  const currentStepData = PROPERTY_FORM_STEPS[currentStep];
  const isLastStep = currentStep === PROPERTY_FORM_STEPS.length - 1;
  const isFirstStep = currentStep === 0;

  const validateCurrentStep = useCallback(async () => {
    const fieldsToValidate = currentStepData.fields;
    const result = await trigger(fieldsToValidate);
    return result;
  }, [currentStep, trigger, currentStepData.fields]);

  const handleNext = useCallback(async () => {
    const isStepValid = await validateCurrentStep();
    if (isStepValid) {
      if (!completedSteps.includes(currentStep)) {
        setCompletedSteps(prev => [...prev, currentStep]);
      }
      if (!isLastStep) {
        setCurrentStep(prev => prev + 1);
      }
    }
  }, [currentStep, isLastStep, validateCurrentStep, completedSteps]);

  const handlePrevious = useCallback(() => {
    if (!isFirstStep) {
      setCurrentStep(prev => prev - 1);
    }
  }, [isFirstStep]);

  const handleStepClick = useCallback(async (stepIndex: number) => {
    if (stepIndex < currentStep || completedSteps.includes(stepIndex)) {
      setCurrentStep(stepIndex);
    } else if (stepIndex === currentStep + 1) {
      await handleNext();
    }
  }, [currentStep, completedSteps, handleNext]);

  const onFormSubmit = async (data: PropertyFormData) => {
    try {
      await onSubmit(data);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  };

  const renderStepContent = () => {
    switch (currentStep) {
      case 0:
        return <PropertyBasicInfoStep />;
      case 1:
        return <PropertyDetailsStep />;
      case 2:
        return <PropertyOptionsStep />;
      case 3:
        return <PropertyImagesStep />;
      default:
        return null;
    }
  };

  const progressPercentage = ((currentStep + 1) / PROPERTY_FORM_STEPS.length) * 100;

  return (
    <FormProvider {...methods}>
      <div className="max-w-4xl mx-auto p-6">
        <Card>
          <CardHeader>
            <CardTitle className="text-2xl font-bold text-center">매물 등록</CardTitle>
            
            {/* Progress Bar */}
            <div className="w-full mt-4">
              <Progress value={progressPercentage} className="w-full" />
              <div className="flex justify-between mt-2 text-sm text-gray-500">
                <span>단계 {currentStep + 1} / {PROPERTY_FORM_STEPS.length}</span>
                <span>{Math.round(progressPercentage)}% 완료</span>
              </div>
            </div>

            {/* Step Indicators */}
            <div className="flex justify-between items-center mt-6">
              {PROPERTY_FORM_STEPS.map((step, index) => (
                <div 
                  key={step.id} 
                  className="flex flex-col items-center cursor-pointer"
                  onClick={() => handleStepClick(index)}
                >
                  <div 
                    className={`
                      w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
                      ${index === currentStep 
                        ? 'bg-blue-600 text-white' 
                        : completedSteps.includes(index)
                        ? 'bg-green-600 text-white'
                        : 'bg-gray-200 text-gray-600'
                      }
                      ${(index <= currentStep || completedSteps.includes(index)) 
                        ? 'cursor-pointer hover:opacity-80' 
                        : 'cursor-not-allowed'
                      }
                    `}
                  >
                    {completedSteps.includes(index) ? (
                      <Check className="w-4 h-4" />
                    ) : (
                      index + 1
                    )}
                  </div>
                  <span className="text-xs mt-1 text-center">{step.title}</span>
                </div>
              ))}
            </div>
          </CardHeader>

          <CardContent>
            <form onSubmit={handleSubmit(onFormSubmit)}>
              {/* Current Step Content */}
              <div className="mb-8">
                <h3 className="text-lg font-semibold mb-2">{currentStepData.title}</h3>
                <p className="text-gray-600 mb-6">{currentStepData.description}</p>
                {renderStepContent()}
              </div>

              {/* Navigation Buttons */}
              <div className="flex justify-between items-center pt-6 border-t">
                <div>
                  {!isFirstStep && (
                    <Button
                      type="button"
                      variant="outline"
                      onClick={handlePrevious}
                      disabled={isLoading}
                    >
                      <ChevronLeft className="w-4 h-4 mr-2" />
                      이전
                    </Button>
                  )}
                </div>

                <div className="flex gap-2">
                  {onCancel && (
                    <Button
                      type="button"
                      variant="ghost"
                      onClick={onCancel}
                      disabled={isLoading}
                    >
                      취소
                    </Button>
                  )}
                  
                  {!isLastStep ? (
                    <Button
                      type="button"
                      onClick={handleNext}
                      disabled={isLoading}
                    >
                      다음
                      <ChevronRight className="w-4 h-4 ml-2" />
                    </Button>
                  ) : (
                    <Button
                      type="submit"
                      disabled={isLoading || !isValid}
                      className="bg-blue-600 hover:bg-blue-700"
                    >
                      {isLoading ? '등록 중...' : '매물 등록'}
                    </Button>
                  )}
                </div>
              </div>
            </form>
          </CardContent>
        </Card>

        {/* Debug: Show form errors in development */}
        {process.env.NODE_ENV === 'development' && Object.keys(errors).length > 0 && (
          <Card className="mt-4 border-red-200">
            <CardHeader>
              <CardTitle className="text-red-600">Form Errors (Development)</CardTitle>
            </CardHeader>
            <CardContent>
              <pre className="text-sm text-red-600">
                {JSON.stringify(errors, null, 2)}
              </pre>
            </CardContent>
          </Card>
        )}
      </div>
    </FormProvider>
  );
}