'use client';

import React, { useState, useEffect } from 'react';
import { useSession } from 'next-auth/react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { format, isFuture, isPast } from 'date-fns';
import { 
  Calendar, 
  Clock, 
  User, 
  MapPin, 
  Phone, 
  Mail, 
  MessageSquare,
  Check,
  X,
  Edit,
  Eye
} from 'lucide-react';
import ViewingCalendar from './viewing-calendar';

interface ViewingData {
  id: number;
  propertyId: number;
  propertyTitle: string;
  propertyAddress: string;
  tenantUserId: number;
  tenantName: string;
  tenantEmail: string;
  scheduledAt: string;
  durationMinutes: number;
  status: 'REQUESTED' | 'CONFIRMED' | 'CANCELLED' | 'COMPLETED';
  tenantNotes?: string;
  landlordNotes?: string;
  contactPhone?: string;
  contactEmail?: string;
  canBeCancelled: boolean;
  canBeRescheduled: boolean;
  requiresFeedback: boolean;
  createdAt: string;
}

interface ViewingManagementProps {
  landlordUserId?: number;
  className?: string;
}

export default function ViewingManagement({ landlordUserId, className }: ViewingManagementProps) {
  const { data: session } = useSession();
  const [viewings, setViewings] = useState<ViewingData[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedViewing, setSelectedViewing] = useState<ViewingData | null>(null);
  const [activeTab, setActiveTab] = useState('calendar');

  useEffect(() => {
    fetchViewings();
  }, [session, landlordUserId]);

  const fetchViewings = async () => {
    if (!session?.accessToken) return;

    try {
      const response = await fetch('/api/v1/viewings/my-viewings?role=landlord&size=100', {
        headers: {
          'Authorization': `Bearer ${session.accessToken}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.ok) {
        const data = await response.json();
        setViewings(data.data.content || []);
      }
    } catch (error) {
      console.error('Failed to fetch viewings:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleViewingAction = async (viewingId: number, action: 'confirm' | 'cancel', reason?: string) => {
    if (!session?.accessToken) return;

    try {
      let url = `/api/v1/viewings/${viewingId}/${action}`;
      let method = 'PUT';
      let body: any = null;

      if (action === 'cancel' && reason) {
        body = JSON.stringify({ reason });
      }

      const response = await fetch(url, {
        method,
        headers: {
          'Authorization': `Bearer ${session.accessToken}`,
          'Content-Type': 'application/json',
        },
        body,
      });

      if (response.ok) {
        fetchViewings(); // Refresh the list
        setSelectedViewing(null);
      }
    } catch (error) {
      console.error(`Failed to ${action} viewing:`, error);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'REQUESTED': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'CONFIRMED': return 'bg-green-100 text-green-800 border-green-200';
      case 'CANCELLED': return 'bg-red-100 text-red-800 border-red-200';
      case 'COMPLETED': return 'bg-blue-100 text-blue-800 border-blue-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  const filterViewings = (status?: string) => {
    if (!status) return viewings;
    return viewings.filter(v => v.status === status);
  };

  const upcomingViewings = viewings.filter(v => 
    (v.status === 'CONFIRMED' || v.status === 'REQUESTED') && 
    isFuture(new Date(v.scheduledAt))
  );

  const ViewingCard = ({ viewing, compact = false }: { viewing: ViewingData; compact?: boolean }) => (
    <Card 
      className={`p-4 cursor-pointer hover:shadow-md transition-shadow ${
        selectedViewing?.id === viewing.id ? 'ring-2 ring-blue-500' : ''
      }`}
      onClick={() => setSelectedViewing(viewing)}
    >
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <div className="flex items-center gap-2 mb-2">
            <Clock className="h-4 w-4 text-gray-500" />
            <span className="font-medium">
              {format(new Date(viewing.scheduledAt), 'MMM d, yyyy • h:mm a')}
            </span>
            <Badge className={getStatusColor(viewing.status)}>
              {viewing.status}
            </Badge>
          </div>

          <div className="space-y-1 text-sm text-gray-600">
            <div className="flex items-center gap-2">
              <MapPin className="h-3 w-3" />
              <span className="font-medium text-gray-900">{viewing.propertyTitle}</span>
            </div>
            <div className="flex items-center gap-2">
              <User className="h-3 w-3" />
              <span>Tenant: {viewing.tenantName}</span>
            </div>
            {!compact && viewing.contactPhone && (
              <div className="flex items-center gap-2">
                <Phone className="h-3 w-3" />
                <span>{viewing.contactPhone}</span>
              </div>
            )}
          </div>

          {!compact && viewing.tenantNotes && (
            <div className="mt-2 p-2 bg-gray-50 rounded text-sm">
              <div className="flex items-start gap-2">
                <MessageSquare className="h-3 w-3 mt-0.5 text-gray-400" />
                <span className="text-gray-600">{viewing.tenantNotes}</span>
              </div>
            </div>
          )}
        </div>

        <div className="flex gap-2 ml-4">
          {viewing.status === 'REQUESTED' && (
            <>
              <Button
                size="sm"
                onClick={(e) => {
                  e.stopPropagation();
                  handleViewingAction(viewing.id, 'confirm');
                }}
                className="bg-green-600 hover:bg-green-700"
              >
                <Check className="h-3 w-3 mr-1" />
                Confirm
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={(e) => {
                  e.stopPropagation();
                  const reason = prompt('Reason for cancellation (optional):');
                  handleViewingAction(viewing.id, 'cancel', reason || undefined);
                }}
                className="text-red-600 hover:text-red-700"
              >
                <X className="h-3 w-3 mr-1" />
                Decline
              </Button>
            </>
          )}
          
          {viewing.status === 'CONFIRMED' && viewing.canBeCancelled && (
            <Button
              size="sm"
              variant="outline"
              onClick={(e) => {
                e.stopPropagation();
                const reason = prompt('Reason for cancellation:');
                if (reason) {
                  handleViewingAction(viewing.id, 'cancel', reason);
                }
              }}
              className="text-red-600 hover:text-red-700"
            >
              <X className="h-3 w-3 mr-1" />
              Cancel
            </Button>
          )}
        </div>
      </div>
    </Card>
  );

  if (loading) {
    return (
      <div className={`space-y-4 ${className}`}>
        <div className="animate-pulse space-y-3">
          {[...Array(5)].map((_, i) => (
            <div key={i} className="h-24 bg-gray-200 rounded-lg"></div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className={`space-y-6 ${className}`}>
      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4 text-center">
          <div className="text-2xl font-bold text-yellow-600">
            {filterViewings('REQUESTED').length}
          </div>
          <div className="text-sm text-gray-600">Pending Requests</div>
        </Card>
        <Card className="p-4 text-center">
          <div className="text-2xl font-bold text-green-600">
            {filterViewings('CONFIRMED').length}
          </div>
          <div className="text-sm text-gray-600">Confirmed</div>
        </Card>
        <Card className="p-4 text-center">
          <div className="text-2xl font-bold text-blue-600">
            {upcomingViewings.length}
          </div>
          <div className="text-sm text-gray-600">Upcoming</div>
        </Card>
        <Card className="p-4 text-center">
          <div className="text-2xl font-bold text-gray-600">
            {viewings.length}
          </div>
          <div className="text-sm text-gray-600">Total Viewings</div>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-4">
          <TabsTrigger value="calendar">Calendar</TabsTrigger>
          <TabsTrigger value="requests">
            Requests {filterViewings('REQUESTED').length > 0 && 
            <Badge className="ml-2 bg-yellow-500 text-white">{filterViewings('REQUESTED').length}</Badge>}
          </TabsTrigger>
          <TabsTrigger value="confirmed">Confirmed</TabsTrigger>
          <TabsTrigger value="history">History</TabsTrigger>
        </TabsList>

        <TabsContent value="calendar" className="space-y-4">
          <ViewingCalendar
            landlordUserId={landlordUserId}
            mode="view"
            onViewingSelect={setSelectedViewing}
          />
        </TabsContent>

        <TabsContent value="requests" className="space-y-3">
          <h2 className="text-lg font-semibold">Pending Viewing Requests</h2>
          {filterViewings('REQUESTED').length === 0 ? (
            <Card className="p-8 text-center text-gray-500">
              <Calendar className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No pending viewing requests</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {filterViewings('REQUESTED')
                .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())
                .map(viewing => (
                  <ViewingCard key={viewing.id} viewing={viewing} />
                ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="confirmed" className="space-y-3">
          <h2 className="text-lg font-semibold">Confirmed Viewings</h2>
          {filterViewings('CONFIRMED').length === 0 ? (
            <Card className="p-8 text-center text-gray-500">
              <Check className="h-12 w-12 mx-auto mb-4 text-gray-300" />
              <p>No confirmed viewings</p>
            </Card>
          ) : (
            <div className="space-y-3">
              {filterViewings('CONFIRMED')
                .sort((a, b) => new Date(a.scheduledAt).getTime() - new Date(b.scheduledAt).getTime())
                .map(viewing => (
                  <ViewingCard key={viewing.id} viewing={viewing} />
                ))}
            </div>
          )}
        </TabsContent>

        <TabsContent value="history" className="space-y-3">
          <h2 className="text-lg font-semibold">Viewing History</h2>
          <div className="space-y-3">
            {viewings
              .filter(v => v.status === 'COMPLETED' || v.status === 'CANCELLED')
              .sort((a, b) => new Date(b.scheduledAt).getTime() - new Date(a.scheduledAt).getTime())
              .map(viewing => (
                <ViewingCard key={viewing.id} viewing={viewing} compact />
              ))}
          </div>
        </TabsContent>
      </Tabs>

      {/* Viewing Details Modal/Panel */}
      {selectedViewing && (
        <Card className="fixed inset-y-0 right-0 w-96 shadow-2xl z-50 overflow-y-auto">
          <div className="p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-semibold">Viewing Details</h3>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => setSelectedViewing(null)}
              >
                <X className="h-4 w-4" />
              </Button>
            </div>

            <div className="space-y-4">
              <div>
                <div className="flex items-center gap-2 mb-2">
                  <Badge className={getStatusColor(selectedViewing.status)}>
                    {selectedViewing.status}
                  </Badge>
                </div>
                <h4 className="font-medium">{selectedViewing.propertyTitle}</h4>
                <p className="text-sm text-gray-600">{selectedViewing.propertyAddress}</p>
              </div>

              <div className="space-y-2">
                <div className="flex items-center gap-2 text-sm">
                  <Calendar className="h-4 w-4 text-gray-500" />
                  <span>{format(new Date(selectedViewing.scheduledAt), 'EEEE, MMMM d, yyyy')}</span>
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Clock className="h-4 w-4 text-gray-500" />
                  <span>
                    {format(new Date(selectedViewing.scheduledAt), 'h:mm a')} 
                    ({selectedViewing.durationMinutes} minutes)
                  </span>
                </div>
              </div>

              <div className="border-t pt-4">
                <h5 className="font-medium mb-2">Tenant Information</h5>
                <div className="space-y-2 text-sm">
                  <div className="flex items-center gap-2">
                    <User className="h-4 w-4 text-gray-500" />
                    <span>{selectedViewing.tenantName}</span>
                  </div>
                  {selectedViewing.tenantEmail && (
                    <div className="flex items-center gap-2">
                      <Mail className="h-4 w-4 text-gray-500" />
                      <span>{selectedViewing.tenantEmail}</span>
                    </div>
                  )}
                  {selectedViewing.contactPhone && (
                    <div className="flex items-center gap-2">
                      <Phone className="h-4 w-4 text-gray-500" />
                      <span>{selectedViewing.contactPhone}</span>
                    </div>
                  )}
                </div>
              </div>

              {selectedViewing.tenantNotes && (
                <div className="border-t pt-4">
                  <h5 className="font-medium mb-2">Tenant Notes</h5>
                  <p className="text-sm text-gray-600 bg-gray-50 p-3 rounded">
                    {selectedViewing.tenantNotes}
                  </p>
                </div>
              )}

              {selectedViewing.status === 'REQUESTED' && (
                <div className="border-t pt-4 space-y-2">
                  <Button
                    className="w-full bg-green-600 hover:bg-green-700"
                    onClick={() => handleViewingAction(selectedViewing.id, 'confirm')}
                  >
                    <Check className="h-4 w-4 mr-2" />
                    Confirm Viewing
                  </Button>
                  <Button
                    variant="outline"
                    className="w-full text-red-600 hover:text-red-700"
                    onClick={() => {
                      const reason = prompt('Reason for declining (optional):');
                      handleViewingAction(selectedViewing.id, 'cancel', reason || undefined);
                    }}
                  >
                    <X className="h-4 w-4 mr-2" />
                    Decline Request
                  </Button>
                </div>
              )}
            </div>
          </div>
        </Card>
      )}
    </div>
  );
}