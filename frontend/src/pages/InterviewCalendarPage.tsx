import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/interview';
import { placementApi } from '../api/placement';
import {
  InterviewAppointment,
  InterviewAppointmentCreateRequest,
  ConflictCheckResponse,
  InterviewRound,
  RoundCandidate,
} from '../types/interview';
import { PlacementDrive } from '../types/placement';
import { UserRecord } from '../types/user';

export const InterviewCalendarPage: React.FC = () => {
  const [appointments, setAppointments] = useState<InterviewAppointment[]>([]);
  const [drives, setDrives] = useState<PlacementDrive[]>([]);
  const [staffUsers, setStaffUsers] = useState<UserRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Scheduling Modal
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedDriveId, setSelectedDriveId] = useState('');
  const [driveRounds, setDriveRounds] = useState<InterviewRound[]>([]);
  const [selectedRoundId, setSelectedRoundId] = useState('');
  const [roundCandidates, setRoundCandidates] = useState<RoundCandidate[]>([]);

  const [appointmentForm, setAppointmentForm] = useState<InterviewAppointmentCreateRequest>({
    roundCandidateId: '',
    evaluatorId: '',
    scheduledStartTime: '',
    scheduledEndTime: '',
    venueOrMeetingUrl: '',
    meetingInstructions: '',
  });

  const [conflictResult, setConflictResult] = useState<ConflictCheckResponse | null>(null);
  const [checkingConflict, setCheckingConflict] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    fetchInitialData();
  }, []);

  const fetchInitialData = async () => {
    try {
      setLoading(true);
      const [drivesRes, staffList] = await Promise.all([
        placementApi.getDrives({ size: 100 }),
        interviewApi.getStaffUsers(),
      ]);
      setDrives(drivesRes.content);
      setStaffUsers(staffList);
      // Fetch some appointments via candidate queries or my appointments
      const myApps = await interviewApi.getMyAppointments();
      setAppointments(myApps);
    } catch (err) {
      console.error('Failed to load initial scheduling data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleDriveChange = async (driveId: string) => {
    setSelectedDriveId(driveId);
    setSelectedRoundId('');
    setRoundCandidates([]);
    if (!driveId) return;

    try {
      const profilesRes = await placementApi.getJobProfiles({ driveId, size: 50 });
      if (profilesRes.content.length > 0) {
        const roundsList = await interviewApi.getRoundsForJobProfile(profilesRes.content[0].id);
        setDriveRounds(roundsList);
        if (roundsList.length > 0) {
          handleRoundChange(roundsList[0].id);
        }
      } else {
        setDriveRounds([]);
      }
    } catch (err) {
      console.error('Failed to load rounds for drive', err);
    }
  };

  const handleRoundChange = async (roundId: string) => {
    setSelectedRoundId(roundId);
    if (!roundId) return;
    try {
      const cands = await interviewApi.getCandidatesForRound(roundId);
      setRoundCandidates(cands);
      if (cands.length > 0) {
        setAppointmentForm((prev) => ({ ...prev, roundCandidateId: cands[0].id }));
      }
    } catch (err) {
      console.error('Failed to load candidates for round', err);
    }
  };

  const handleCheckConflict = async () => {
    if (!appointmentForm.roundCandidateId || !appointmentForm.scheduledStartTime || !appointmentForm.scheduledEndTime) {
      return;
    }
    const cand = roundCandidates.find((c) => c.id === appointmentForm.roundCandidateId);
    if (!cand) return;

    try {
      setCheckingConflict(true);
      const conf = await interviewApi.checkConflicts(
        cand.studentId,
        new Date(appointmentForm.scheduledStartTime).toISOString(),
        new Date(appointmentForm.scheduledEndTime).toISOString(),
        appointmentForm.evaluatorId || undefined
      );
      setConflictResult(conf);
    } catch (err) {
      console.error('Conflict check failed', err);
    } finally {
      setCheckingConflict(false);
    }
  };

  const handleScheduleAppointment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!appointmentForm.roundCandidateId || !appointmentForm.scheduledStartTime || !appointmentForm.scheduledEndTime) {
      setMsg({ text: 'Candidate, start time and end time are required.', type: 'error' });
      return;
    }

    try {
      setSubmitting(true);
      setMsg(null);
      const created = await interviewApi.scheduleAppointment({
        ...appointmentForm,
        scheduledStartTime: new Date(appointmentForm.scheduledStartTime).toISOString(),
        scheduledEndTime: new Date(appointmentForm.scheduledEndTime).toISOString(),
      });
      setAppointments((prev) => [created, ...prev]);
      setShowScheduleModal(false);
      setMsg({ text: 'Interview appointment scheduled successfully!', type: 'success' });
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to schedule appointment', type: 'error' });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            Interview Calendar & Scheduling Panel
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Book candidate interview slots with cross-company conflict detection and calendar coordination.
          </p>
        </div>
        <button
          onClick={() => setShowScheduleModal(true)}
          className="mt-3 md:mt-0 px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold shadow transition"
        >
          + Schedule Interview Slot
        </button>
      </div>

      {msg && (
        <div
          className={`mb-6 p-4 rounded text-sm ${
            msg.type === 'success' ? 'bg-green-50 text-green-800 border border-green-200' : 'bg-red-50 text-red-800 border border-red-200'
          }`}
        >
          {msg.text}
        </div>
      )}

      {/* Appointment Slots Grid */}
      <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
        <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-600 mb-4">
          Upcoming Interview Appointments ({appointments.length})
        </h2>

        {loading ? (
          <div className="p-8 text-center text-gray-400">Loading interview appointments...</div>
        ) : appointments.length === 0 ? (
          <div className="p-8 text-center text-gray-500 bg-gray-50 rounded">
            No scheduled interview appointments found. Use "+ Schedule Interview Slot" above to book candidate slots.
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {appointments.map((app) => (
              <div
                key={app.id}
                className="border rounded-lg p-4 bg-white shadow-sm hover:shadow transition-shadow flex flex-col justify-between"
              >
                <div>
                  <div className="flex justify-between items-start mb-2">
                    <span className="text-xs font-semibold px-2 py-0.5 rounded bg-red-50 text-red-800 border border-red-200">
                      {app.appointmentStatus}
                    </span>
                    <span className="text-xs text-gray-500">
                      {new Date(app.scheduledStartTime).toLocaleDateString()}
                    </span>
                  </div>

                  <h3 className="font-bold text-gray-900 text-sm">{app.studentName || 'Candidate'}</h3>
                  <div className="text-xs text-gray-600 mt-0.5">{app.studentEmail}</div>

                  <div className="mt-3 text-xs space-y-1 text-gray-700">
                    <div>
                      <span className="font-medium text-gray-500">Drive / Round: </span>
                      {app.companyName || 'Recruitment'} • {app.roundName || 'Interview'}
                    </div>
                    <div>
                      <span className="font-medium text-gray-500">Time: </span>
                      {new Date(app.scheduledStartTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })} -{' '}
                      {new Date(app.scheduledEndTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    </div>
                    <div>
                      <span className="font-medium text-gray-500">Evaluator: </span>
                      {app.evaluatorName || 'Unassigned'}
                    </div>
                    {app.venueOrMeetingUrl && (
                      <div className="truncate">
                        <span className="font-medium text-gray-500">Venue / Link: </span>
                        {app.venueOrMeetingUrl.startsWith('http') ? (
                          <a
                            href={app.venueOrMeetingUrl}
                            target="_blank"
                            rel="noreferrer"
                            className="text-blue-600 hover:underline"
                          >
                            Open Meeting
                          </a>
                        ) : (
                          <span>{app.venueOrMeetingUrl}</span>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* SCHEDULE APPOINTMENT MODAL */}
      {showScheduleModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-lg w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Schedule Candidate Interview Slot</h3>
            <form onSubmit={handleScheduleAppointment} className="space-y-3">
              {/* Drive selector */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Drive *</label>
                <select
                  value={selectedDriveId}
                  onChange={(e) => handleDriveChange(e.target.value)}
                  className="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="">-- Choose Drive --</option>
                  {drives.map((d) => (
                    <option key={d.id} value={d.id}>
                      {d.driveTitle} ({d.companyName})
                    </option>
                  ))}
                </select>
              </div>

              {/* Round selector */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Round *</label>
                <select
                  disabled={driveRounds.length === 0}
                  value={selectedRoundId}
                  onChange={(e) => handleRoundChange(e.target.value)}
                  className="w-full border rounded px-3 py-2 text-sm disabled:bg-gray-100"
                >
                  <option value="">-- Choose Round --</option>
                  {driveRounds.map((r) => (
                    <option key={r.id} value={r.id}>
                      Round {r.sequenceOrder}: {r.roundName}
                    </option>
                  ))}
                </select>
              </div>

              {/* Candidate selector */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Candidate *</label>
                <select
                  disabled={roundCandidates.length === 0}
                  value={appointmentForm.roundCandidateId}
                  onChange={(e) => setAppointmentForm({ ...appointmentForm, roundCandidateId: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm disabled:bg-gray-100"
                >
                  <option value="">-- Choose Candidate --</option>
                  {roundCandidates.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.studentName} ({c.universityStudentId}) - {c.candidateRoundStatus}
                    </option>
                  ))}
                </select>
              </div>

              {/* Evaluator selector */}
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Assign Evaluator (Optional)</label>
                <select
                  value={appointmentForm.evaluatorId}
                  onChange={(e) => setAppointmentForm({ ...appointmentForm, evaluatorId: e.target.value })}
                  className="w-full border rounded px-3 py-2 text-sm"
                >
                  <option value="">-- No Evaluator Assigned --</option>
                  {staffUsers.map((u) => (
                    <option key={u.id} value={u.id}>
                      {u.fullName} ({u.email})
                    </option>
                  ))}
                </select>
              </div>

              {/* Start & End Times */}
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Start Time *</label>
                  <input
                    type="datetime-local"
                    required
                    value={appointmentForm.scheduledStartTime}
                    onChange={(e) => setAppointmentForm({ ...appointmentForm, scheduledStartTime: e.target.value })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">End Time *</label>
                  <input
                    type="datetime-local"
                    required
                    value={appointmentForm.scheduledEndTime}
                    onChange={(e) => setAppointmentForm({ ...appointmentForm, scheduledEndTime: e.target.value })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
              </div>

              {/* Conflict check trigger */}
              <div className="pt-1">
                <button
                  type="button"
                  onClick={handleCheckConflict}
                  disabled={checkingConflict || !appointmentForm.roundCandidateId || !appointmentForm.scheduledStartTime}
                  className="text-xs bg-gray-100 hover:bg-gray-200 text-gray-800 px-3 py-1.5 rounded border border-gray-300 font-medium"
                >
                  {checkingConflict ? 'Checking...' : '🔍 Check Cross-Company Conflicts'}
                </button>
              </div>

              {conflictResult && (
                <div
                  className={`p-3 rounded text-xs border ${
                    conflictResult.hasConflict
                      ? 'bg-red-50 text-red-800 border-red-200'
                      : 'bg-green-50 text-green-800 border-green-200'
                  }`}
                >
                  {conflictResult.hasConflict ? (
                    <div>
                      <span className="font-bold">Conflict Detected: </span>
                      {conflictResult.conflictDetails.join(', ')}
                    </div>
                  ) : (
                    <div className="font-medium">No schedule conflicts detected. Slot is free!</div>
                  )}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Venue or Meeting URL</label>
                <input
                  type="text"
                  value={appointmentForm.venueOrMeetingUrl}
                  onChange={(e) => setAppointmentForm({ ...appointmentForm, venueOrMeetingUrl: e.target.value })}
                  placeholder="e.g. Placement Cell Room 102 or https://meet.google.com/..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Instructions</label>
                <textarea
                  rows={2}
                  value={appointmentForm.meetingInstructions}
                  onChange={(e) => setAppointmentForm({ ...appointmentForm, meetingInstructions: e.target.value })}
                  placeholder="Carry 2 copies of your resume, laptop with code editor..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowScheduleModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold disabled:opacity-50"
                >
                  {submitting ? 'Scheduling...' : 'Confirm Appointment'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
