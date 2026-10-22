import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import {
  AttendanceSession,
  AttendanceRecord,
  AcademicSubject,
  AttendanceCorrectionHistory,
} from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  CheckSquare,
  Lock,
  Plus,
  MapPin,
  Clock,
  Calendar,
  AlertTriangle,
  CheckCircle,
  History,
  X,
  ShieldAlert,
} from 'lucide-react';

export const AttendanceSessionPage: React.FC = () => {
  const { user } = useAuth();
  const canMark = user?.roles?.includes('FACULTY') || user?.roles?.includes('HOD') || user?.roles?.includes('SUPER_ADMIN');
  const canCorrect = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD') || user?.roles?.includes('FACULTY');

  const [sessions, setSessions] = useState<AttendanceSession[]>([]);
  const [selectedSession, setSelectedSession] = useState<AttendanceSession | null>(null);
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [subjects, setSubjects] = useState<AcademicSubject[]>([]);

  const [loading, setLoading] = useState<boolean>(true);
  const [recordsLoading, setRecordsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Open Session Modal
  const [showOpenModal, setShowOpenModal] = useState<boolean>(false);
  const [openForm, setOpenForm] = useState({
    subjectId: '',
    batchId: 1,
    semester: 5,
    section: 'A',
    sessionDate: new Date().toISOString().split('T')[0],
    startTime: '10:00:00',
    endTime: '11:00:00',
    topicCovered: '',
    isGeofenceEnabled: true,
    authorizedLatitude: 26.779782,
    authorizedLongitude: 75.877864,
    geofenceRadiusMeters: 60,
  });
  const [openSubmitting, setOpenSubmitting] = useState<boolean>(false);

  // Correction Modal
  const [showCorrectionModal, setShowCorrectionModal] = useState<boolean>(false);
  const [targetRecord, setTargetRecord] = useState<AttendanceRecord | null>(null);
  const [correctionStatus, setCorrectionStatus] = useState<string>('EXCUSED');
  const [correctionReason, setCorrectionReason] = useState<string>('');
  const [correctionSubmitting, setCorrectionSubmitting] = useState<boolean>(false);

  // History Modal
  const [showHistoryModal, setShowHistoryModal] = useState<boolean>(false);
  const [correctionHistory, setCorrectionHistory] = useState<AttendanceCorrectionHistory[]>([]);
  const [historyLoading, setHistoryLoading] = useState<boolean>(false);

  useEffect(() => {
    loadInitial();
  }, []);

  const loadInitial = async () => {
    setLoading(true);
    try {
      const [sessRes, subs] = await Promise.all([
        academicOpsApi.listAttendanceSessions({ size: 30 }),
        academicOpsApi.getSubjects(),
      ]);
      const sessionList: AttendanceSession[] = sessRes.content || [];
      setSessions(sessionList);
      setSubjects(subs);
      if (subs.length > 0) setOpenForm((prev) => ({ ...prev, subjectId: subs[0].id.toString() }));

      if (sessionList.length > 0) {
        selectSession(sessionList[0]);
      }
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load attendance sessions');
    } finally {
      setLoading(false);
    }
  };

  const selectSession = async (sess: AttendanceSession) => {
    setSelectedSession(sess);
    setRecordsLoading(true);
    try {
      const data = await academicOpsApi.getSessionRecords(sess.id);
      setRecords(data);
    } catch (err: any) {
      setError('Failed to fetch session roster records');
    } finally {
      setRecordsLoading(false);
    }
  };

  const handleOpenSession = async (e: React.FormEvent) => {
    e.preventDefault();
    setOpenSubmitting(true);
    try {
      const created = await academicOpsApi.openAttendanceSession({
        ...openForm,
        subjectId: Number(openForm.subjectId),
      });
      setShowOpenModal(false);
      setSuccessMsg('Attendance session launched and student roster auto-populated!');
      setTimeout(() => setSuccessMsg(null), 5000);
      setSessions([created, ...sessions]);
      selectSession(created);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to open attendance session');
    } finally {
      setOpenSubmitting(false);
    }
  };

  const handleMarkStatus = async (record: AttendanceRecord, newStatus: string) => {
    if (!selectedSession || selectedSession.sessionStatus === 'FINALIZED') return;

    // When marking via roster, submit within session's configured geofence bounds if enabled
    const lat = selectedSession.authorizedLatitude || 0;
    const lon = selectedSession.authorizedLongitude || 0;

    try {
      const updated = await academicOpsApi.submitAttendanceRecord(selectedSession.id, {
        studentId: record.studentId,
        attendanceStatus: newStatus,
        verificationMethod: 'MANUAL_ROSTER',
        latitude: lat,
        longitude: lon,
        accuracyMeters: 5.0,
      });

      setRecords((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to update record');
    }
  };

  const handleFinalize = async () => {
    if (!selectedSession) return;
    if (!window.confirm('Are you sure you want to finalize and lock this attendance session? Direct modifications will be prohibited.')) return;

    try {
      const finalized = await academicOpsApi.finalizeAttendanceSession(selectedSession.id);
      setSelectedSession(finalized);
      setSessions((prev) => prev.map((s) => (s.id === finalized.id ? finalized : s)));
      setSuccessMsg('Attendance session locked and archived.');
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to finalize session');
    }
  };

  const handleOpenCorrection = (r: AttendanceRecord) => {
    setTargetRecord(r);
    setCorrectionStatus(r.attendanceStatus === 'ABSENT' ? 'PRESENT' : 'EXCUSED');
    setCorrectionReason('');
    setShowCorrectionModal(true);
  };

  const handleSubmitCorrection = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!targetRecord) return;
    setCorrectionSubmitting(true);
    try {
      const updated = await academicOpsApi.correctRecord(targetRecord.id, {
        newStatus: correctionStatus,
        correctionReason,
      });
      setShowCorrectionModal(false);
      setRecords((prev) => prev.map((r) => (r.id === updated.id ? updated : r)));
      setSuccessMsg('Attendance correction recorded with immutable audit history.');
      setTimeout(() => setSuccessMsg(null), 5000);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to submit attendance correction');
    } finally {
      setCorrectionSubmitting(false);
    }
  };

  const handleViewHistory = async (r: AttendanceRecord) => {
    setTargetRecord(r);
    setHistoryLoading(true);
    setShowHistoryModal(true);
    try {
      const history = await academicOpsApi.getCorrectionHistory(r.id);
      setCorrectionHistory(history);
    } catch (err: any) {
      console.error(err);
    } finally {
      setHistoryLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <CheckSquare className="w-7 h-7 text-[#8B0000]" />
            Classroom Attendance & Geotagged Console
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Conduct lecture attendance, verify device geolocation boundaries, and manage audited corrections
          </p>
        </div>
        {canMark && (
          <button
            onClick={() => setShowOpenModal(true)}
            className="flex items-center gap-2 bg-[#8B0000] hover:bg-[#6b0000] text-white px-4 py-2.5 rounded-lg font-medium shadow-sm transition"
          >
            <Plus className="w-4 h-4" />
            Launch Attendance Session
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

      {/* Main Layout: Session Selector Sidebar + Active Roster */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Sessions List */}
        <div className="bg-white rounded-xl border border-gray-100 shadow-sm p-4 space-y-3">
          <h3 className="font-bold text-gray-900 text-sm flex items-center justify-between pb-2 border-b border-gray-100">
            <span>Recent Sessions</span>
            <span className="text-xs font-normal text-gray-400">Total: {sessions.length}</span>
          </h3>

          <div className="space-y-2 max-h-[600px] overflow-y-auto">
            {loading ? (
              <div className="text-center py-8 text-gray-400 text-xs">Loading sessions...</div>
            ) : sessions.length === 0 ? (
              <div className="text-center py-8 text-gray-400 text-xs">No sessions launched yet.</div>
            ) : (
              sessions.map((s) => {
              const isSelected = selectedSession?.id === s.id;
              return (
                <div
                  key={s.id}
                  onClick={() => selectSession(s)}
                  className={`p-3 rounded-lg border text-xs cursor-pointer transition ${
                    isSelected
                      ? 'border-[#8B0000] bg-rose-50/40 shadow-sm'
                      : 'border-gray-100 hover:border-gray-200 hover:bg-gray-50'
                  }`}
                >
                  <div className="flex items-center justify-between font-semibold text-gray-900">
                    <span>{s.subjectCode}</span>
                    <span
                      className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${
                        s.sessionStatus === 'FINALIZED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-amber-100 text-amber-800 animate-pulse'
                      }`}
                    >
                      {s.sessionStatus}
                    </span>
                  </div>
                  <div className="text-gray-500 mt-1 truncate">{s.subjectName}</div>
                  <div className="text-gray-400 text-[11px] mt-2 flex items-center justify-between">
                    <span>{s.sessionDate}</span>
                    <span>Sec {s.section}</span>
                  </div>
                </div>
              );
            }))}
          </div>
        </div>

        {/* Selected Session Console */}
        <div className="lg:col-span-3 space-y-4">
          {selectedSession ? (
            <div className="bg-white rounded-xl border border-gray-100 shadow-sm overflow-hidden">
              {/* Session Banner */}
              <div className="p-6 border-b border-gray-100 bg-gradient-to-r from-gray-50 to-white flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs font-bold text-[#8B0000] bg-rose-50 px-2.5 py-0.5 rounded-full border border-rose-100">
                      {selectedSession.subjectCode}
                    </span>
                    <span className="text-xs text-gray-400">•</span>
                    <span className="text-xs font-medium text-gray-600">
                      Sem {selectedSession.semester} • Section {selectedSession.section}
                    </span>
                    <span
                      className={`text-xs px-2 py-0.5 rounded-full font-bold ${
                        selectedSession.sessionStatus === 'FINALIZED'
                          ? 'bg-emerald-100 text-emerald-800'
                          : 'bg-amber-100 text-amber-800'
                      }`}
                    >
                      {selectedSession.sessionStatus}
                    </span>
                  </div>
                  <h2 className="text-xl font-bold text-gray-900">{selectedSession.subjectName}</h2>
                  <div className="flex flex-wrap gap-4 mt-2 text-xs text-gray-500">
                    <span className="flex items-center gap-1">
                      <Calendar className="w-3.5 h-3.5 text-gray-400" />
                      {selectedSession.sessionDate}
                    </span>
                    <span className="flex items-center gap-1">
                      <Clock className="w-3.5 h-3.5 text-gray-400" />
                      {selectedSession.startTime.substring(0, 5)} - {selectedSession.endTime.substring(0, 5)}
                    </span>
                    {selectedSession.isGeofenceEnabled && (
                      <span className="flex items-center gap-1 text-emerald-700 font-medium">
                        <MapPin className="w-3.5 h-3.5 text-emerald-600" />
                        Geofence Enforced ({selectedSession.geofenceRadiusMeters}m radius)
                      </span>
                    )}
                  </div>
                </div>

                {selectedSession.sessionStatus === 'OPEN' && canMark && (
                  <button
                    onClick={handleFinalize}
                    className="flex items-center gap-2 bg-emerald-700 hover:bg-emerald-800 text-white px-4 py-2 rounded-lg text-xs font-semibold shadow-sm transition"
                  >
                    <Lock className="w-4 h-4" />
                    Finalize & Lock Session
                  </button>
                )}
              </div>

              {/* Spoofing Note Disclaimer */}
              <div className="p-3 bg-amber-50/70 border-b border-amber-100 text-[11px] text-amber-800 flex items-center gap-2 px-6">
                <ShieldAlert className="w-4 h-4 text-amber-600 flex-shrink-0" />
                <span>
                  <strong>Haversine Geofence Verification:</strong> Distances are strictly calculated server-side against authorized campus coordinates. In high-stakes sessions, verify photo evidence or faculty proctor rosters.
                </span>
              </div>

              {/* Roster Table */}
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs">
                  <thead className="bg-gray-50 text-gray-600 uppercase font-semibold text-[11px] border-b border-gray-100">
                    <tr>
                      <th className="py-3 px-6">Roll Number</th>
                      <th className="py-3 px-6">Student Name</th>
                      <th className="py-3 px-6">Attendance Status</th>
                      <th className="py-3 px-6">Geofence Telemetry</th>
                      <th className="py-3 px-6 text-right">Actions</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    {recordsLoading ? (
                      <tr>
                        <td colSpan={5} className="py-12 text-center text-gray-400">
                          Loading cohort roster...
                        </td>
                      </tr>
                    ) : records.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="py-12 text-center text-gray-400">
                          No student records enrolled for this session.
                        </td>
                      </tr>
                    ) : (
                      records.map((r) => {
                        const isFinalized = selectedSession.sessionStatus === 'FINALIZED';
                        return (
                          <tr key={r.id} className="hover:bg-gray-50/80 transition">
                            <td className="py-3.5 px-6 font-semibold text-gray-900">{r.rollNumber}</td>
                            <td className="py-3.5 px-6 text-gray-800 font-medium">{r.studentName}</td>
                            <td className="py-3.5 px-6">
                              {isFinalized ? (
                                <span
                                  className={`px-2.5 py-1 rounded-full text-[11px] font-bold ${
                                    r.attendanceStatus === 'PRESENT'
                                      ? 'bg-emerald-100 text-emerald-800'
                                      : r.attendanceStatus === 'ABSENT'
                                      ? 'bg-rose-100 text-rose-800'
                                      : r.attendanceStatus === 'LATE'
                                      ? 'bg-amber-100 text-amber-800'
                                      : 'bg-blue-100 text-blue-800'
                                  }`}
                                >
                                  {r.attendanceStatus}
                                  {r.isCorrected && ' (Adjusted)'}
                                </span>
                              ) : (
                                <div className="flex gap-1">
                                  {(['PRESENT', 'ABSENT', 'LATE', 'EXCUSED'] as const).map((st) => (
                                    <button
                                      key={st}
                                      onClick={() => handleMarkStatus(r, st)}
                                      className={`px-2 py-1 rounded text-[10px] font-bold transition ${
                                        r.attendanceStatus === st
                                          ? st === 'PRESENT'
                                            ? 'bg-emerald-700 text-white'
                                            : st === 'ABSENT'
                                            ? 'bg-rose-700 text-white'
                                            : st === 'LATE'
                                            ? 'bg-amber-600 text-white'
                                            : 'bg-blue-600 text-white'
                                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                                      }`}
                                    >
                                      {st[0]}
                                    </button>
                                  ))}
                                </div>
                              )}
                            </td>
                            <td className="py-3.5 px-6">
                              {r.geofenceStatus === 'WITHIN_GEOFENCE' ? (
                                <span className="text-emerald-700 font-medium flex items-center gap-1">
                                  <CheckCircle className="w-3.5 h-3.5" />
                                  Within Geofence ({r.calculatedDistanceMeters}m)
                                </span>
                              ) : r.geofenceStatus === 'OUTSIDE_GEOFENCE' ? (
                                <span className="text-rose-600 font-medium flex items-center gap-1">
                                  <AlertTriangle className="w-3.5 h-3.5" />
                                  Outside Geofence ({r.calculatedDistanceMeters}m)
                                </span>
                              ) : (
                                <span className="text-gray-400 text-[11px]">{r.geofenceStatus}</span>
                              )}
                            </td>
                            <td className="py-3.5 px-6 text-right space-x-2">
                              {isFinalized && canCorrect && (
                                <button
                                  onClick={() => handleOpenCorrection(r)}
                                  className="text-[#8B0000] hover:underline font-medium text-[11px]"
                                >
                                  Formal Correction
                                </button>
                              )}
                              {r.isCorrected && (
                                <button
                                  onClick={() => handleViewHistory(r)}
                                  className="text-gray-500 hover:text-gray-700 text-[11px] inline-flex items-center gap-0.5"
                                >
                                  <History className="w-3 h-3" />
                                  History
                                </button>
                              )}
                            </td>
                          </tr>
                        );
                      })
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-gray-100 p-16 text-center text-gray-400">
              Select or launch an attendance session to review roster records
            </div>
          )}
        </div>
      </div>

      {/* Launch Attendance Session Modal */}
      {showOpenModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                <CheckSquare className="w-5 h-5 text-[#8B0000]" />
                Launch Lecture Attendance Session
              </h3>
              <button
                onClick={() => setShowOpenModal(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleOpenSession} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Subject / Curriculum Course
                </label>
                <select
                  value={openForm.subjectId}
                  onChange={(e) => setOpenForm({ ...openForm, subjectId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                >
                  {subjects.map((sub) => (
                    <option key={sub.id} value={sub.id}>
                      {sub.subjectCode} - {sub.subjectName}
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Semester
                  </label>
                  <select
                    value={openForm.semester}
                    onChange={(e) => setOpenForm({ ...openForm, semester: Number(e.target.value) })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                      <option key={s} value={s}>
                        Semester {s}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Section
                  </label>
                  <select
                    value={openForm.section}
                    onChange={(e) => setOpenForm({ ...openForm, section: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    {['A', 'B', 'C', 'D'].map((sec) => (
                      <option key={sec} value={sec}>
                        Section {sec}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Date
                  </label>
                  <input
                    type="date"
                    value={openForm.sessionDate}
                    onChange={(e) => setOpenForm({ ...openForm, sessionDate: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Start
                  </label>
                  <input
                    type="time"
                    value={openForm.startTime.substring(0, 5)}
                    onChange={(e) => setOpenForm({ ...openForm, startTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    End
                  </label>
                  <input
                    type="time"
                    value={openForm.endTime.substring(0, 5)}
                    onChange={(e) => setOpenForm({ ...openForm, endTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Topic Covered
                </label>
                <input
                  type="text"
                  placeholder="e.g. Red-Black Trees Insertion and Rotations"
                  value={openForm.topicCovered}
                  onChange={(e) => setOpenForm({ ...openForm, topicCovered: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                />
              </div>

              <div className="space-y-3 p-3 bg-gray-50 rounded-lg border border-gray-200">
                <div className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    id="geofenceCheck"
                    checked={openForm.isGeofenceEnabled}
                    onChange={(e) => setOpenForm({ ...openForm, isGeofenceEnabled: e.target.checked })}
                    className="w-4 h-4 text-[#8B0000] rounded focus:ring-[#8B0000]"
                  />
                  <label htmlFor="geofenceCheck" className="text-xs text-gray-700 font-medium">
                    Enforce Classroom GPS Geofence Validation
                  </label>
                </div>

                {openForm.isGeofenceEnabled && (
                  <div className="space-y-2 pt-2 border-t border-gray-200 text-xs">
                    <div className="grid grid-cols-2 gap-2">
                      <div>
                        <label className="block text-[11px] font-semibold text-gray-600 mb-0.5">
                          Authorized Latitude
                        </label>
                        <input
                          type="number"
                          step="0.000001"
                          value={openForm.authorizedLatitude}
                          onChange={(e) => setOpenForm({ ...openForm, authorizedLatitude: parseFloat(e.target.value) || 0 })}
                          className="w-full border border-gray-300 rounded p-1.5 text-xs"
                        />
                      </div>
                      <div>
                        <label className="block text-[11px] font-semibold text-gray-600 mb-0.5">
                          Authorized Longitude
                        </label>
                        <input
                          type="number"
                          step="0.000001"
                          value={openForm.authorizedLongitude}
                          onChange={(e) => setOpenForm({ ...openForm, authorizedLongitude: parseFloat(e.target.value) || 0 })}
                          className="w-full border border-gray-300 rounded p-1.5 text-xs"
                        />
                      </div>
                    </div>
                    <div>
                      <label className="block text-[11px] font-semibold text-gray-600 mb-0.5">
                        Geofence Radius (meters)
                      </label>
                      <input
                        type="number"
                        min="20"
                        max="500"
                        value={openForm.geofenceRadiusMeters}
                        onChange={(e) => setOpenForm({ ...openForm, geofenceRadiusMeters: parseInt(e.target.value) || 60 })}
                        className="w-full border border-gray-300 rounded p-1.5 text-xs"
                      />
                    </div>
                    <p className="text-[10px] text-gray-500 italic">
                      * Pre-populated coordinates represent development sandbox test baselines. Exact campus geofences must be verified administratively.
                    </p>
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowOpenModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={openSubmitting}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm transition disabled:opacity-50"
                >
                  {openSubmitting ? 'Opening Session...' : 'Launch Session'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Formal Attendance Correction Modal */}
      {showCorrectionModal && targetRecord && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base">Formal Attendance Correction</h3>
              <button
                onClick={() => setShowCorrectionModal(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitCorrection} className="p-6 space-y-4">
              <div className="p-3 bg-gray-50 rounded-lg text-xs space-y-1">
                <p>
                  <strong className="text-gray-800">Student:</strong> {targetRecord.studentName} ({targetRecord.rollNumber})
                </p>
                <p>
                  <strong className="text-gray-800">Current Status:</strong> {targetRecord.attendanceStatus}
                </p>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Corrected Status
                </label>
                <select
                  value={correctionStatus}
                  onChange={(e) => setCorrectionStatus(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                >
                  <option value="PRESENT">PRESENT</option>
                  <option value="ABSENT">ABSENT</option>
                  <option value="LATE">LATE</option>
                  <option value="EXCUSED">EXCUSED</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Mandatory Justification Reason (min 5 chars)
                </label>
                <textarea
                  rows={3}
                  value={correctionReason}
                  onChange={(e) => setCorrectionReason(e.target.value)}
                  placeholder="e.g. Medical leave approved by HOD or student was attending placement drive"
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowCorrectionModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={correctionSubmitting || correctionReason.trim().length < 5}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm transition disabled:opacity-50"
                >
                  {correctionSubmitting ? 'Auditing...' : 'Confirm Correction'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* History Modal */}
      {showHistoryModal && targetRecord && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
                <History className="w-4 h-4 text-[#8B0000]" />
                Attendance Correction Audit Trail
              </h3>
              <button
                onClick={() => setShowHistoryModal(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-3 max-h-[400px] overflow-y-auto">
              {historyLoading ? (
                <div className="text-center py-8 text-gray-400">Loading audit records...</div>
              ) : correctionHistory.length === 0 ? (
                <div className="text-center py-8 text-gray-400">No correction history recorded.</div>
              ) : (
                correctionHistory.map((h) => (
                  <div key={h.id} className="p-3 bg-gray-50 rounded-lg border border-gray-100 text-xs space-y-1">
                    <div className="flex items-center justify-between font-semibold">
                      <span className="text-gray-800">
                        {h.previousStatus} → <span className="text-[#8B0000]">{h.newStatus}</span>
                      </span>
                      <span className="text-gray-400 font-normal">{new Date(h.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="text-gray-600">
                      <strong>Reason:</strong> {h.correctionReason}
                    </div>
                    {h.actorName && (
                      <div className="text-gray-400 text-[11px]">
                        <strong>Adjusted By:</strong> {h.actorName}
                      </div>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
export default AttendanceSessionPage;
