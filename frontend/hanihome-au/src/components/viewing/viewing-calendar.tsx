'use client';

import React, { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import Calendar from 'react-calendar';
import DatePicker from 'react-datepicker';
import { format, addMinutes, isSameDay, startOfDay, endOfDay, addDays } from 'date-fns';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Clock, Calendar as CalendarIcon, MapPin, User, AlertCircle } from 'lucide-react';
import 'react-calendar/dist/Calendar.css';
import 'react-datepicker/dist/react-datepicker.css';

interface ViewingData {
  id: number;
  propertyId: number;
  propertyTitle: string;
  propertyAddress: string;
  tenantName: string;
  landlordName: string;
  scheduledAt: string;
  durationMinutes: number;
  status: 'REQUESTED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  canBeCancelled: boolean;
  canBeRescheduled: boolean;
}

interface TimeSlot {
  time: Date;
  available: boolean;
  conflictingViewing?: ViewingData;
}

interface ViewingCalendarProps {
  propertyId?: number;
  landlordUserId?: number;
  onViewingSelect?: (viewing: ViewingData) => void;
  onTimeSlotSelect?: (timeSlot: Date) => void;
  mode?: 'view' | 'schedule'; // view for displaying existing, schedule for booking new
  className?: string;
}

export default function ViewingCalendar({
  propertyId,
  landlordUserId,
  onViewingSelect,
  onTimeSlotSelect,
  mode = 'view',
  className
}: ViewingCalendarProps) {
  const { data: session } = useSession();
  const [selectedDate, setSelectedDate] = useState<Date>(new Date());
  const [viewings, setViewings] = useState<ViewingData[]>([]);
  const [timeSlots, setTimeSlots] = useState<TimeSlot[]>([]);
  const [selectedTime, setSelectedTime] = useState<Date | null>(null);
  const [loading, setLoading] = useState(false);

  // Business hours configuration
  const businessHours = {
    start: 9, // 9 AM
    end: 18,  // 6 PM
    slotDuration: 60, // 60 minutes per slot
    daysInAdvance: 30 // Allow booking up to 30 days in advance
  };

  useEffect(() => {
    if (selectedDate) {
      fetchViewingsForDate(selectedDate);
      if (mode === 'schedule') {
        generateTimeSlots(selectedDate);
      }
    }
  }, [selectedDate, propertyId, landlordUserId, mode]);

  const fetchViewingsForDate = async (date: Date) => {
    if (!session?.accessToken) return;

    setLoading(true);
    try {
      const startDate = startOfDay(date).toISOString();
      const endDate = endOfDay(date).toISOString();
      
      let url = '/api/v1/viewings/';
      if (propertyId) {
        url += `property/${propertyId}?`;
      } else if (landlordUserId) {
        url += `my-viewings?role=landlord&`;
      } else {
        url += `my-viewings?`;
      }
      
      url += `startDate=${startDate}&endDate=${endDate}`;

      const response = await fetch(url, {
        headers: {
          'Authorization': `Bearer ${session.accessToken}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        const dayViewings = data.data.content || data.data || [];
        setViewings(dayViewings.filter((v: ViewingData) => 
          isSameDay(new Date(v.scheduledAt), date)
        ));
      }
    } catch (error) {
      console.error('Failed to fetch viewings:', error);
    } finally {
      setLoading(false);
    }
  };

  const generateTimeSlots = (date: Date) => {
    const slots: TimeSlot[] = [];
    const today = new Date();
    
    // Don't show slots for past dates
    if (date < startOfDay(today)) {
      setTimeSlots([]);
      return;
    }

    // Don't show slots beyond the allowed advance booking period
    if (date > addDays(today, businessHours.daysInAdvance)) {
      setTimeSlots([]);
      return;
    }

    // Generate time slots for business hours
    for (let hour = businessHours.start; hour < businessHours.end; hour++) {
      const slotTime = new Date(date);
      slotTime.setHours(hour, 0, 0, 0);

      // Skip past time slots for today
      if (isSameDay(date, today) && slotTime <= today) {
        continue;
      }

      // Check if this slot conflicts with existing viewings
      const conflicting = viewings.find(viewing => {
        const viewingStart = new Date(viewing.scheduledAt);
        const viewingEnd = addMinutes(viewingStart, viewing.durationMinutes);
        const slotEnd = addMinutes(slotTime, businessHours.slotDuration);
        
        return (slotTime < viewingEnd && slotEnd > viewingStart);
      });

      slots.push({
        time: slotTime,
        available: !conflicting && (viewing => viewing.status === 'REQUESTED' || viewing.status === 'CONFIRMED'),
        conflictingViewing: conflicting
      });
    }

    setTimeSlots(slots);
  };

  const handleDateChange = (date: Date | Date[]) => {
    if (Array.isArray(date)) {
      setSelectedDate(date[0]);
    } else {
      setSelectedDate(date);
    }
    setSelectedTime(null);
  };

  const handleTimeSlotClick = (slot: TimeSlot) => {
    if (mode === 'schedule' && slot.available) {
      setSelectedTime(slot.time);
      onTimeSlotSelect?.(slot.time);
    } else if (mode === 'view' && slot.conflictingViewing) {
      onViewingSelect?.(slot.conflictingViewing);
    }
  };

  const getViewingStatusColor = (status: string) => {
    switch (status) {
      case 'REQUESTED': return 'bg-yellow-100 text-yellow-800';
      case 'CONFIRMED': return 'bg-green-100 text-green-800';
      case 'CANCELLED': return 'bg-red-100 text-red-800';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const tileContent = ({ date, view }: { date: Date; view: string }) => {
    if (view !== 'month') return null;

    const dayViewings = viewings.filter(v => isSameDay(new Date(v.scheduledAt), date));
    if (dayViewings.length === 0) return null;

    return (
      <div className="flex flex-wrap gap-1 mt-1">
        {dayViewings.slice(0, 2).map((viewing, index) => (
          <div
            key={viewing.id}
            className={`w-2 h-2 rounded-full ${
              viewing.status === 'CONFIRMED' ? 'bg-green-500' :
              viewing.status === 'REQUESTED' ? 'bg-yellow-500' :
              viewing.status === 'CANCELLED' ? 'bg-red-500' : 'bg-blue-500'
            }`}
          />
        ))}
        {dayViewings.length > 2 && (
          <div className="text-xs text-gray-600">+{dayViewings.length - 2}</div>
        )}
      </div>
    );
  };

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Calendar */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
          <CalendarIcon className="h-5 w-5" />
          {mode === 'schedule' ? 'Select Viewing Date' : 'Viewing Calendar'}
        </h3>
        
        <Calendar
          onChange={handleDateChange}
          value={selectedDate}
          tileContent={tileContent}
          minDate={new Date()}
          maxDate={addDays(new Date(), businessHours.daysInAdvance)}
          className="react-calendar"
        />

        <div className="mt-4 flex flex-wrap gap-2">
          <div className="flex items-center gap-2 text-sm">
            <div className="w-3 h-3 rounded-full bg-green-500"></div>
            <span>Confirmed</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <div className="w-3 h-3 rounded-full bg-yellow-500"></div>
            <span>Requested</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <div className="w-3 h-3 rounded-full bg-red-500"></div>
            <span>Cancelled</span>
          </div>
          <div className="flex items-center gap-2 text-sm">
            <div className="w-3 h-3 rounded-full bg-blue-500"></div>
            <span>Completed</span>
          </div>
        </div>
      </Card>

      {/* Time Slots / Daily Schedule */}
      <Card className="p-6">
        <h3 className="text-lg font-semibold mb-4 flex items-center gap-2">
          <Clock className="h-5 w-5" />
          {format(selectedDate, 'EEEE, MMMM d, yyyy')}
        </h3>

        {loading ? (
          <div className="space-y-3">
            {[...Array(6)].map((_, i) => (
              <div key={i} className="h-16 bg-gray-200 rounded-lg animate-pulse"></div>
            ))}
          </div>
        ) : (
          <>
            {mode === 'schedule' && timeSlots.length > 0 && (
              <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
                {timeSlots.map((slot, index) => (
                  <Button
                    key={index}
                    variant={selectedTime && slot.time.getTime() === selectedTime.getTime() ? "default" : "outline"}
                    onClick={() => handleTimeSlotClick(slot)}
                    disabled={!slot.available}
                    className={`p-4 h-auto justify-start ${
                      !slot.available ? 'opacity-50 cursor-not-allowed' : ''
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <Clock className="h-4 w-4" />
                      <div className="text-left">
                        <div className="font-medium">
                          {format(slot.time, 'h:mm a')} - {format(addMinutes(slot.time, businessHours.slotDuration), 'h:mm a')}
                        </div>
                        {!slot.available && slot.conflictingViewing && (
                          <div className="text-sm text-gray-500">
                            Booked by {slot.conflictingViewing.tenantName}
                          </div>
                        )}
                      </div>
                    </div>
                  </Button>
                ))}
              </div>
            )}

            {mode === 'view' && (
              <div className="space-y-3">
                {viewings.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">
                    <CalendarIcon className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                    <p>No viewings scheduled for this date</p>
                  </div>
                ) : (
                  viewings.map(viewing => (
                    <Card
                      key={viewing.id}
                      className="p-4 cursor-pointer hover:shadow-md transition-shadow"
                      onClick={() => onViewingSelect?.(viewing)}
                    >
                      <div className="flex items-start justify-between">
                        <div className="flex-1">
                          <div className="flex items-center gap-2 mb-2">
                            <Clock className="h-4 w-4 text-gray-500" />
                            <span className="font-medium">
                              {format(new Date(viewing.scheduledAt), 'h:mm a')} - 
                              {format(addMinutes(new Date(viewing.scheduledAt), viewing.durationMinutes), 'h:mm a')}
                            </span>
                            <Badge className={getViewingStatusColor(viewing.status)}>
                              {viewing.status}
                            </Badge>
                          </div>
                          
                          <div className="space-y-1 text-sm text-gray-600">
                            <div className="flex items-center gap-2">
                              <MapPin className="h-3 w-3" />
                              <span>{viewing.propertyTitle}</span>
                            </div>
                            <div className="flex items-center gap-2">
                              <User className="h-3 w-3" />
                              <span>Tenant: {viewing.tenantName}</span>
                            </div>
                          </div>
                        </div>

                        <div className="flex gap-2">
                          {viewing.canBeRescheduled && (
                            <Button size="sm" variant="outline">
                              Reschedule
                            </Button>
                          )}
                          {viewing.canBeCancelled && (
                            <Button size="sm" variant="outline" className="text-red-600 hover:text-red-700">
                              Cancel
                            </Button>
                          )}
                        </div>
                      </div>
                    </Card>
                  ))
                )}
              </div>
            )}

            {mode === 'schedule' && timeSlots.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                <AlertCircle className="h-12 w-12 mx-auto mb-4 text-gray-300" />
                <p>No available time slots for this date</p>
                <p className="text-sm">Business hours: {businessHours.start}:00 AM - {businessHours.end}:00 PM</p>
              </div>
            )}
          </>
        )}
      </Card>

      {/* Selected Time Confirmation */}
      {mode === 'schedule' && selectedTime && (
        <Card className="p-4 bg-blue-50 border-blue-200">
          <div className="flex items-center gap-3">
            <div className="w-3 h-3 rounded-full bg-blue-500"></div>
            <div>
              <div className="font-medium">Selected Time</div>
              <div className="text-sm text-gray-600">
                {format(selectedTime, 'EEEE, MMMM d, yyyy')} at {format(selectedTime, 'h:mm a')}
              </div>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}