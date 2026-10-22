import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { PortalEvent, EventRegistration } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  CalendarDays,
  Plus,
  MapPin,
  Clock,
  Video,
  CheckCircle,
  AlertTriangle,
  X,
  Send,
  Eye,
} from 'lucide-react';

export const EventsPage: React.FC = () => {
  const { user } = useAuth();
  const isStudent = user?.roles?.includes('STUDENT');
  const canManage = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD') || user?.roles?.includes('FACULTY');

  const [events, setEvents] = useState<PortalEvent[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // New Event Modal
  const [showCreateModal, setShowCreateModal] = useState<boolean>(false);
  const [eventForm, setEventForm] = useState({
    eventTitle: '',
    eventType: 'WORKSHOP',
    description: '',
    eventDate: new Date().toISOString().split('T')[0],
    startTime: '11:00:00',
    endTime: '13:00:00',
    venue: 'Auditorium Block A',
    onlineMeetingUrl: '',
    targetAudience: 'ALL',
    maxParticipants: 150,
    organizerName: 'JECRC Training & Placement Cell',
  });
  const [submitting, setSubmitting] = useState<boolean>(false);

  // Registrations Modal
  const [showRegModal, setShowRegModal] = useState<boolean>(false);
  const [selectedEvent, setSelectedEvent] = useState<PortalEvent | null>(null);
  const [registrations, setRegistrations] = useState<EventRegistration[]>([]);
  const [regLoading, setRegLoading] = useState<boolean>(false);

  useEffect(() => {
    loadEvents();
  }, []);

  const loadEvents = async () => {
    setLoading(true);
    try {
      const res = await academicOpsApi.getEvents({ size: 30 });
      setEvents(res.content || []);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load campus events');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateEvent = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await academicOpsApi.createEvent(eventForm);
      setShowCreateModal(false);
      setSuccessMsg('Event drafted successfully!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadEvents();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to create event');
    } finally {
      setSubmitting(false);
    }
  };

  const handlePublish = async (eventId: string) => {
    try {
      await academicOpsApi.publishEvent(eventId);
      setSuccessMsg('Event published and announced to campus cohorts!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadEvents();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to publish event');
    }
  };

  const handleRegister = async (eventId: string) => {
    try {
      await academicOpsApi.registerForEvent(eventId);
      setSuccessMsg('Successfully registered for the event!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadEvents();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to register for event');
    }
  };

  const handleViewRegistrations = async (ev: PortalEvent) => {
    setSelectedEvent(ev);
    setShowRegModal(true);
    setRegLoading(true);
    try {
      const list = await academicOpsApi.getEventRegistrations(ev.id);
      setRegistrations(list);
    } catch (e) {
      console.error(e);
    } finally {
      setRegLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CalendarDays className="w-7 h-7 text-[#8B0000]" />
            Events, Workshops & Announcements
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Industry guest seminars, hackathons, pre-placement talks, and campus skill symposiums
          </p>
        </div>

        {canManage && (
          <button
            onClick={() => setShowCreateModal(true)}
            className="flex items-center gap-2 bg-[#8B0000] hover:bg-[#6b0000] text-white px-4 py-2.5 rounded-lg text-xs font-semibold shadow-sm transition"
          >
            <Plus className="w-4 h-4" />
            Draft New Event
          </button>
        )}
      </div>

      {successMsg && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <CheckCircle className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          {successMsg}
        </div>
      )}
      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <AlertTriangle className="w-5 h-5 text-rose-600 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* Events Grid */}
      {loading ? (
        <div className="text-center py-20 text-gray-400">Loading campus events...</div>
      ) : events.length === 0 ? (
        <div className="text-center py-16 bg-white rounded-xl border border-gray-100">
          <CalendarDays className="w-12 h-12 text-gray-300 mx-auto mb-3" />
          <h3 className="text-base font-semibold text-gray-700">No events currently announced</h3>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((ev) => (
            <div
              key={ev.id}
              className="bg-white rounded-xl border border-gray-200 hover:border-[#8B0000] shadow-sm hover:shadow-md transition p-6 flex flex-col justify-between"
            >
              <div>
                <div className="flex items-center justify-between mb-2">
                  <span className="text-xs font-bold text-[#8B0000] bg-rose-50 px-2.5 py-1 rounded-full border border-rose-100">
                    {ev.eventType}
                  </span>
                  <span
                    className={`text-xs px-2.5 py-0.5 rounded-full font-bold ${
                      ev.status === 'PUBLISHED'
                        ? 'bg-emerald-100 text-emerald-800'
                        : 'bg-amber-100 text-amber-800'
                    }`}
                  >
                    {ev.status}
                  </span>
                </div>

                <h3 className="font-bold text-gray-900 text-lg mb-1">{ev.eventTitle}</h3>
                <p className="text-xs text-gray-500 line-clamp-2 mb-4">{ev.description || 'No description provided'}</p>

                <div className="space-y-2 text-xs text-gray-600 border-t border-gray-100 pt-3">
                  <div className="flex items-center gap-2">
                    <CalendarDays className="w-3.5 h-3.5 text-gray-400" />
                    <span className="font-medium text-gray-800">{ev.eventDate}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <Clock className="w-3.5 h-3.5 text-gray-400" />
                    <span>
                      {ev.startTime.substring(0, 5)} - {ev.endTime.substring(0, 5)}
                    </span>
                  </div>
                  {ev.venue && (
                    <div className="flex items-center gap-2">
                      <MapPin className="w-3.5 h-3.5 text-gray-400" />
                      <span>{ev.venue}</span>
                    </div>
                  )}
                  {ev.onlineMeetingUrl && (
                    <div className="flex items-center gap-2">
                      <Video className="w-3.5 h-3.5 text-emerald-600" />
                      <a
                        href={ev.onlineMeetingUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="text-emerald-700 hover:underline truncate"
                      >
                        {ev.onlineMeetingUrl}
                      </a>
                    </div>
                  )}
                </div>
              </div>

              <div className="mt-5 pt-4 border-t border-gray-100 flex items-center justify-between">
                <span className="text-xs text-gray-400">
                  Registered: {ev.registeredCount} / {ev.maxParticipants || '∞'}
                </span>

                <div className="flex gap-2">
                  {canManage && ev.status === 'DRAFT' && (
                    <button
                      onClick={() => handlePublish(ev.id)}
                      className="px-3 py-1.5 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-xs font-semibold shadow-sm transition flex items-center gap-1"
                    >
                      <Send className="w-3 h-3" />
                      Publish
                    </button>
                  )}

                  {ev.status === 'PUBLISHED' && isStudent && (
                    <button
                      onClick={() => handleRegister(ev.id)}
                      className="px-3 py-1.5 bg-emerald-700 hover:bg-emerald-800 text-white rounded-lg text-xs font-semibold shadow-sm transition"
                    >
                      Register
                    </button>
                  )}

                  {canManage && (
                    <button
                      onClick={() => handleViewRegistrations(ev)}
                      className="p-1.5 border border-gray-200 hover:bg-gray-50 rounded-lg text-gray-500 hover:text-gray-700 transition"
                      title="View Registrations"
                    >
                      <Eye className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base">Draft Campus Event / Workshop</h3>
              <button onClick={() => setShowCreateModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleCreateEvent} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Event Title</label>
                <input
                  type="text"
                  placeholder="e.g. AWS Cloud Architecture & GenAI Hackathon"
                  value={eventForm.eventTitle}
                  onChange={(e) => setEventForm({ ...eventForm, eventTitle: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Type</label>
                  <select
                    value={eventForm.eventType}
                    onChange={(e) => setEventForm({ ...eventForm, eventType: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    <option value="WORKSHOP">WORKSHOP</option>
                    <option value="SEMINAR">SEMINAR</option>
                    <option value="HACKATHON">HACKATHON</option>
                    <option value="PRE_PLACEMENT_TALK">PRE_PLACEMENT_TALK</option>
                    <option value="CAREER_SESSION">CAREER_SESSION</option>
                    <option value="ANNOUNCEMENT">ANNOUNCEMENT</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Target Audience</label>
                  <select
                    value={eventForm.targetAudience}
                    onChange={(e) => setEventForm({ ...eventForm, targetAudience: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    <option value="ALL">ALL (Campus-Wide)</option>
                    <option value="BTECH_CSE">B.Tech CSE</option>
                    <option value="FINAL_YEAR">Final Year Cohorts</option>
                    <option value="PLACEMENT_ELIGIBLE">Placement Eligible Only</option>
                    <option value="FACULTY_ONLY">Faculty Only</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Date</label>
                  <input
                    type="date"
                    value={eventForm.eventDate}
                    onChange={(e) => setEventForm({ ...eventForm, eventDate: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Start</label>
                  <input
                    type="time"
                    value={eventForm.startTime.substring(0, 5)}
                    onChange={(e) => setEventForm({ ...eventForm, startTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">End</label>
                  <input
                    type="time"
                    value={eventForm.endTime.substring(0, 5)}
                    onChange={(e) => setEventForm({ ...eventForm, endTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Physical Venue</label>
                  <input
                    type="text"
                    value={eventForm.venue}
                    onChange={(e) => setEventForm({ ...eventForm, venue: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Virtual URL</label>
                  <input
                    type="text"
                    placeholder="https://meet.google.com/..."
                    value={eventForm.onlineMeetingUrl}
                    onChange={(e) => setEventForm({ ...eventForm, onlineMeetingUrl: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Description</label>
                <textarea
                  rows={2}
                  value={eventForm.description}
                  onChange={(e) => setEventForm({ ...eventForm, description: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm"
                >
                  {submitting ? 'Creating...' : 'Draft Event'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Registrations List Modal */}
      {showRegModal && selectedEvent && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <div>
                <h3 className="font-bold text-gray-900 text-base">Registered Participants</h3>
                <p className="text-xs text-gray-500">{selectedEvent.eventTitle}</p>
              </div>
              <button onClick={() => setShowRegModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <div className="p-6 max-h-[400px] overflow-y-auto">
              {regLoading ? (
                <div className="text-center py-8 text-gray-400">Loading registrations...</div>
              ) : registrations.length === 0 ? (
                <div className="text-center py-8 text-gray-400">No students registered yet.</div>
              ) : (
                <div className="divide-y divide-gray-100">
                  {registrations.map((r) => (
                    <div key={r.id} className="py-2.5 flex items-center justify-between text-xs">
                      <div>
                        <span className="font-semibold text-gray-900">{r.studentName}</span>
                        <span className="text-gray-400 ml-2">({r.rollNumber})</span>
                      </div>
                      <span className="text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded font-semibold text-[10px]">
                        {r.attendanceStatus}
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default EventsPage;
