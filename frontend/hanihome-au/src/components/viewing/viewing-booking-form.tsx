'use client';

import React, { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select } from '@/components/ui/select';
import { format } from 'date-fns';
import { Calendar, Clock, User, Phone, Mail, MessageSquare } from 'lucide-react';
import ViewingCalendar from './viewing-calendar';

interface PropertyInfo {
  id: number;
  title: string;
  address: string;
  landlordUserId: number;
  landlordName: string;
  agentUserId?: number;
  agentName?: string;
}

interface ViewingBookingFormProps {
  property: PropertyInfo;
  onSuccess?: (viewingId: number) => void;
  onCancel?: () => void;
  className?: string;
}

interface BookingFormData {
  selectedDateTime: Date | null;
  durationMinutes: number;
  tenantNotes: string;
  contactPhone: string;
  contactEmail: string;
}

export default function ViewingBookingForm({
  property,
  onSuccess,
  onCancel,
  className
}: ViewingBookingFormProps) {
  const { data: session } = useSession();
  const [currentStep, setCurrentStep] = useState<'calendar' | 'details' | 'confirmation'>('calendar');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  const [formData, setFormData] = useState<BookingFormData>({
    selectedDateTime: null,
    durationMinutes: 60,
    tenantNotes: '',
    contactPhone: '',
    contactEmail: session?.user?.email || ''
  });

  const handleTimeSlotSelect = (timeSlot: Date) => {
    setFormData(prev => ({
      ...prev,
      selectedDateTime: timeSlot
    }));
  };

  const handleFormChange = (field: keyof BookingFormData, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const validateForm = (): string | null => {
    if (!formData.selectedDateTime) {
      return 'Please select a date and time for the viewing';
    }
    if (!formData.contactPhone && !formData.contactEmail) {
      return 'Please provide at least one contact method';
    }
    if (formData.contactEmail && !isValidEmail(formData.contactEmail)) {
      return 'Please enter a valid email address';
    }
    return null;
  };

  const isValidEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleSubmit = async () => {
    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await fetch('/api/v1/viewings', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${session?.accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          propertyId: property.id,
          landlordUserId: property.landlordUserId,
          agentUserId: property.agentUserId,
          scheduledAt: formData.selectedDateTime?.toISOString(),
          durationMinutes: formData.durationMinutes,
          tenantNotes: formData.tenantNotes,
          contactPhone: formData.contactPhone,
          contactEmail: formData.contactEmail
        }),
      });

      if (response.ok) {
        const result = await response.json();
        setCurrentStep('confirmation');
        onSuccess?.(result.data.id);
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to book viewing');
      }
    } catch (err) {
      setError('Network error. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const nextStep = () => {
    if (currentStep === 'calendar' && formData.selectedDateTime) {
      setCurrentStep('details');
      setError(null);
    } else if (currentStep === 'details') {
      const validationError = validateForm();
      if (validationError) {
        setError(validationError);
      } else {
        handleSubmit();
      }
    }
  };

  const prevStep = () => {
    if (currentStep === 'details') {
      setCurrentStep('calendar');
      setError(null);
    }
  };

  const renderStepIndicator = () => (
    <div className="flex items-center justify-center mb-6">
      <div className="flex items-center space-x-4">
        <div className={`flex items-center ${currentStep === 'calendar' ? 'text-blue-600' : 'text-gray-400'}`}>
          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
            ${currentStep === 'calendar' ? 'bg-blue-600 text-white' : 
              (formData.selectedDateTime ? 'bg-green-500 text-white' : 'bg-gray-200')}`}>
            1
          </div>
          <span className="ml-2 font-medium">Select Time</span>
        </div>
        
        <div className={`w-8 h-px ${formData.selectedDateTime ? 'bg-green-500' : 'bg-gray-300'}`}></div>
        
        <div className={`flex items-center ${currentStep === 'details' ? 'text-blue-600' : 'text-gray-400'}`}>
          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
            ${currentStep === 'details' ? 'bg-blue-600 text-white' : 'bg-gray-200'}`}>
            2
          </div>
          <span className="ml-2 font-medium">Details</span>
        </div>
        
        <div className="w-8 h-px bg-gray-300"></div>
        
        <div className={`flex items-center ${currentStep === 'confirmation' ? 'text-green-600' : 'text-gray-400'}`}>
          <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-medium
            ${currentStep === 'confirmation' ? 'bg-green-600 text-white' : 'bg-gray-200'}`}>
            3
          </div>
          <span className="ml-2 font-medium">Confirmation</span>
        </div>
      </div>
    </div>
  );

  return (
    <div className={`max-w-4xl mx-auto ${className}`}>
      {/* Property Header */}
      <Card className="p-6 mb-6">
        <div className="flex items-start justify-between">
          <div>
            <h2 className="text-xl font-semibold mb-2">{property.title}</h2>
            <p className="text-gray-600 mb-2">{property.address}</p>
            <div className="flex items-center gap-4 text-sm text-gray-500">
              <div className="flex items-center gap-1">
                <User className="h-4 w-4" />
                <span>Landlord: {property.landlordName}</span>
              </div>
              {property.agentName && (
                <div className="flex items-center gap-1">
                  <User className="h-4 w-4" />
                  <span>Agent: {property.agentName}</span>
                </div>
              )}
            </div>
          </div>
        </div>
      </Card>

      {/* Step Indicator */}
      {renderStepIndicator()}

      {/* Error Display */}
      {error && (
        <Card className="p-4 mb-6 bg-red-50 border-red-200">
          <p className="text-red-600 text-sm">{error}</p>
        </Card>
      )}

      {/* Step Content */}
      {currentStep === 'calendar' && (
        <ViewingCalendar
          propertyId={property.id}
          mode="schedule"
          onTimeSlotSelect={handleTimeSlotSelect}
          className="mb-6"
        />
      )}

      {currentStep === 'details' && (
        <Card className="p-6 mb-6">
          <h3 className="text-lg font-semibold mb-4">Viewing Details</h3>
          
          {/* Selected Time Display */}
          {formData.selectedDateTime && (
            <Card className="p-4 mb-6 bg-blue-50 border-blue-200">
              <div className="flex items-center gap-3">
                <Calendar className="h-5 w-5 text-blue-600" />
                <div>
                  <div className="font-medium text-blue-900">
                    {format(formData.selectedDateTime, 'EEEE, MMMM d, yyyy')}
                  </div>
                  <div className="text-blue-700">
                    {format(formData.selectedDateTime, 'h:mm a')} ({formData.durationMinutes} minutes)
                  </div>
                </div>
              </div>
            </Card>
          )}

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Duration */}
            <div>
              <Label htmlFor="duration" className="flex items-center gap-2 mb-2">
                <Clock className="h-4 w-4" />
                Duration
              </Label>
              <Select
                value={formData.durationMinutes.toString()}
                onValueChange={(value) => handleFormChange('durationMinutes', parseInt(value))}
              >
                <option value="30">30 minutes</option>
                <option value="60">1 hour</option>
                <option value="90">1.5 hours</option>
                <option value="120">2 hours</option>
              </Select>
            </div>

            {/* Contact Phone */}
            <div>
              <Label htmlFor="phone" className="flex items-center gap-2 mb-2">
                <Phone className="h-4 w-4" />
                Contact Phone
              </Label>
              <Input
                id="phone"
                type="tel"
                placeholder="Enter your phone number"
                value={formData.contactPhone}
                onChange={(e) => handleFormChange('contactPhone', e.target.value)}
              />
            </div>

            {/* Contact Email */}
            <div className="md:col-span-2">
              <Label htmlFor="email" className="flex items-center gap-2 mb-2">
                <Mail className="h-4 w-4" />
                Contact Email
              </Label>
              <Input
                id="email"
                type="email"
                placeholder="Enter your email address"
                value={formData.contactEmail}
                onChange={(e) => handleFormChange('contactEmail', e.target.value)}
              />
            </div>

            {/* Notes */}
            <div className="md:col-span-2">
              <Label htmlFor="notes" className="flex items-center gap-2 mb-2">
                <MessageSquare className="h-4 w-4" />
                Additional Notes (Optional)
              </Label>
              <Textarea
                id="notes"
                placeholder="Any specific requirements or questions about the property..."
                rows={4}
                value={formData.tenantNotes}
                onChange={(e) => handleFormChange('tenantNotes', e.target.value)}
              />
            </div>
          </div>
        </Card>
      )}

      {currentStep === 'confirmation' && (
        <Card className="p-6 text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <Calendar className="h-8 w-8 text-green-600" />
          </div>
          
          <h3 className="text-xl font-semibold text-green-900 mb-2">
            Viewing Request Submitted!
          </h3>
          
          <p className="text-gray-600 mb-6">
            Your viewing request has been sent to the landlord. You'll receive a confirmation 
            email once they approve your request.
          </p>

          {formData.selectedDateTime && (
            <Card className="p-4 bg-gray-50 mb-6">
              <div className="text-left">
                <div className="font-medium mb-2">Viewing Details:</div>
                <div className="space-y-1 text-sm text-gray-600">
                  <div>📅 {format(formData.selectedDateTime, 'EEEE, MMMM d, yyyy')}</div>
                  <div>⏰ {format(formData.selectedDateTime, 'h:mm a')} ({formData.durationMinutes} minutes)</div>
                  <div>🏠 {property.title}</div>
                  <div>📍 {property.address}</div>
                  {formData.contactPhone && <div>📞 {formData.contactPhone}</div>}
                  {formData.contactEmail && <div>📧 {formData.contactEmail}</div>}
                </div>
              </div>
            </Card>
          )}

          <div className="flex gap-3 justify-center">
            <Button onClick={() => window.location.href = '/dashboard'}>
              Go to Dashboard
            </Button>
            <Button variant="outline" onClick={() => window.location.href = '/search'}>
              Continue Searching
            </Button>
          </div>
        </Card>
      )}

      {/* Navigation Buttons */}
      {currentStep !== 'confirmation' && (
        <div className="flex justify-between">
          <div>
            {currentStep === 'details' && (
              <Button variant="outline" onClick={prevStep}>
                Back
              </Button>
            )}
            {onCancel && (
              <Button variant="outline" onClick={onCancel} className="ml-2">
                Cancel
              </Button>
            )}
          </div>
          
          <div>
            <Button 
              onClick={nextStep}
              disabled={loading || (currentStep === 'calendar' && !formData.selectedDateTime)}
            >
              {loading ? 'Processing...' : 
               currentStep === 'calendar' ? 'Continue' : 'Book Viewing'}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}