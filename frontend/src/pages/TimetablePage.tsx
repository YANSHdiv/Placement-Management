import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { TimetableSlot, Classroom, AcademicSubject, FacultyProfile } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  Calendar,
  Clock,
  MapPin,
  User as UserIcon,
  Plus,
  AlertTriangle,
  Filter,
  CheckCircle,
  X,
} from 'lucide-react';

const DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'] as const;

export const TimetablePage: React.FC = () => {
  const { user } = useAuth();
  const canManage = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD');

  const [selectedDay, setSelectedDay] = useState<string>('MONDAY');
  const batchId = 1;
  const [semester, setSemester] = useState<number>(5);
  const [section, setSection] = useState<string>('A');

  const [slots, setSlots] = useState<TimetableSlot[]>([]);
  const [classrooms, setClassrooms] = useState<Classroom[]>([]);
  const [subjects, setSubjects] = useState<AcademicSubject[]>([]);
  const [facultyList, setFacultyList] = useState<FacultyProfile[]>([]);

  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // New Slot Modal
  const [showModal, setShowModal] = useState<boolean>(false);
  const [formData, setFormData] = useState({
    subjectId: '',
    facultyId: '',
    classroomId: '',
    dayOfWeek: 'MONDAY',
    startTime: '09:00:00',
    endTime: '10:00:00',
    academicYear: '2025-2026',
    status: 'PUBLISHED',
  });
  const [modalError, setModalError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState<boolean>(false);

  useEffect(() => {
    loadPrerequisites();
  }, []);

  useEffect(() => {
    loadTimetable();
  }, [batchId, semester, section]);

  const loadPrerequisites = async () => {
    try {
      const [rooms, subs, facs] = await Promise.all([
        academicOpsApi.getClassrooms(),
        academicOpsApi.getSubjects(),
        academicOpsApi.getFacultyProfiles(),
      ]);
      setClassrooms(rooms);
      setSubjects(subs);
      setFacultyList(facs);
      if (subs.length > 0) setFormData((prev) => ({ ...prev, subjectId: subs[0].id.toString() }));
      if (facs.length > 0) setFormData((prev) => ({ ...prev, facultyId: facs[0].id }));
      if (rooms.length > 0) setFormData((prev) => ({ ...prev, classroomId: rooms[0].id.toString() }));
    } catch (err: any) {
      console.error('Failed to load prerequisites', err);
    }
  };

  const loadTimetable = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await academicOpsApi.getCohortTimetable({
        batchId,
        semester,
        section,
      });
      setSlots(data);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load timetable');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateSlot = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);
    setSubmitting(true);
    try {
      await academicOpsApi.createTimetableSlot({
        batchId,
        semester,
        section,
        subjectId: Number(formData.subjectId),
        facultyId: formData.facultyId,
        classroomId: Number(formData.classroomId),
        dayOfWeek: formData.dayOfWeek,
        startTime: formData.startTime,
        endTime: formData.endTime,
        academicYear: formData.academicYear,
        status: formData.status,
      });
      setShowModal(false);
      setSuccessMsg('Timetable slot scheduled successfully with conflict-free GiST verification!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadTimetable();
    } catch (err: any) {
      setModalError(err?.response?.data?.message || 'Conflict detected during slot scheduling');
    } finally {
      setSubmitting(false);
    }
  };

  const daySlots = slots.filter((s) => s.dayOfWeek === selectedDay);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Calendar className="w-7 h-7 text-[#8B0000]" />
            Academic Timetable & Class Schedules
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Race-free GiST exclusion protected scheduling for classrooms, faculty, and student cohorts
          </p>
        </div>
        {canManage && (
          <button
            onClick={() => setShowModal(true)}
            className="flex items-center gap-2 bg-[#8B0000] hover:bg-[#6b0000] text-white px-4 py-2.5 rounded-lg font-medium shadow-sm transition"
          >
            <Plus className="w-4 h-4" />
            Schedule Class Slot
          </button>
        )}
      </div>

      {/* Success / Error Alerts */}
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

      {/* Cohort Selector Filter */}
      <div className="bg-white p-4 rounded-xl border border-gray-100 shadow-sm flex flex-wrap items-center gap-4 text-sm">
        <div className="flex items-center gap-2 text-gray-700 font-medium">
          <Filter className="w-4 h-4 text-gray-400" />
          Filter Cohort:
        </div>
        <div className="flex items-center gap-2">
          <label className="text-gray-500 text-xs uppercase font-semibold">Semester:</label>
          <select
            value={semester}
            onChange={(e) => setSemester(Number(e.target.value))}
            className="border border-gray-300 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-[#8B0000]"
          >
            {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
              <option key={s} value={s}>
                Semester {s}
              </option>
            ))}
          </select>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-gray-500 text-xs uppercase font-semibold">Section:</label>
          <select
            value={section}
            onChange={(e) => setSection(e.target.value)}
            className="border border-gray-300 rounded px-3 py-1.5 focus:outline-none focus:ring-1 focus:ring-[#8B0000]"
          >
            {['A', 'B', 'C', 'D'].map((sec) => (
              <option key={sec} value={sec}>
                Section {sec}
              </option>
            ))}
          </select>
        </div>
        <div className="ml-auto text-xs text-gray-500 font-medium bg-gray-50 px-3 py-1.5 rounded-full border border-gray-200">
          Showing: B.Tech CSE • Semester {semester} • Section {section}
        </div>
      </div>

      {/* Days Tabs */}
      <div className="flex border-b border-gray-200 bg-white rounded-t-xl px-4 pt-2 gap-2 overflow-x-auto">
        {DAYS.map((day) => {
          const count = slots.filter((s) => s.dayOfWeek === day).length;
          return (
            <button
              key={day}
              onClick={() => setSelectedDay(day)}
              className={`py-3 px-4 font-medium text-sm border-b-2 whitespace-nowrap transition flex items-center gap-2 ${
                selectedDay === day
                  ? 'border-[#8B0000] text-[#8B0000] bg-rose-50/30'
                  : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
            >
              {day}
              <span
                className={`text-xs px-2 py-0.5 rounded-full font-semibold ${
                  selectedDay === day ? 'bg-[#8B0000] text-white' : 'bg-gray-100 text-gray-600'
                }`}
              >
                {count}
              </span>
            </button>
          );
        })}
      </div>

      {/* Slots Matrix */}
      <div className="bg-white p-6 rounded-b-xl border border-t-0 border-gray-100 shadow-sm min-h-[350px]">
        {loading ? (
          <div className="flex items-center justify-center py-20 text-gray-400">Loading schedule...</div>
        ) : daySlots.length === 0 ? (
          <div className="text-center py-16">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <h3 className="text-base font-semibold text-gray-700">No classes scheduled for {selectedDay}</h3>
            <p className="text-sm text-gray-400 mt-1">This cohort has no timetable entries on this day.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {daySlots.map((slot) => (
              <div
                key={slot.id}
                className="border border-gray-200 hover:border-[#8B0000] hover:shadow-md transition rounded-xl p-5 bg-gradient-to-br from-white to-gray-50/50 flex flex-col justify-between"
              >
                <div>
                  <div className="flex items-center justify-between mb-3">
                    <span className="text-xs font-bold text-[#8B0000] bg-rose-50 px-2.5 py-1 rounded-full border border-rose-100">
                      {slot.subjectCode}
                    </span>
                    <span className="text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded border border-emerald-100">
                      {slot.status}
                    </span>
                  </div>

                  <h3 className="font-semibold text-gray-900 text-base mb-1">{slot.subjectName}</h3>

                  <div className="space-y-2 mt-4 text-xs text-gray-600">
                    <div className="flex items-center gap-2">
                      <Clock className="w-4 h-4 text-gray-400 flex-shrink-0" />
                      <span className="font-medium text-gray-800">
                        {slot.startTime.substring(0, 5)} - {slot.endTime.substring(0, 5)}
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <MapPin className="w-4 h-4 text-gray-400 flex-shrink-0" />
                      <span>
                        Room <strong className="text-gray-900">{slot.roomNumber}</strong> ({slot.buildingName})
                      </span>
                    </div>
                    <div className="flex items-center gap-2">
                      <UserIcon className="w-4 h-4 text-gray-400 flex-shrink-0" />
                      <span>
                        Faculty: <strong className="text-gray-900">{slot.facultyName || slot.facultyCode}</strong>
                      </span>
                    </div>
                  </div>
                </div>

                <div className="mt-5 pt-3 border-t border-gray-100 flex items-center justify-between text-xs text-gray-400">
                  <span>Acad Year: {slot.academicYear}</span>
                  <span>Sec {slot.section}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Schedule Slot Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-lg flex items-center gap-2">
                <Calendar className="w-5 h-5 text-[#8B0000]" />
                Schedule Class Timetable Slot
              </h3>
              <button
                onClick={() => setShowModal(false)}
                className="text-gray-400 hover:text-gray-600 transition"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleCreateSlot} className="p-6 space-y-4">
              {modalError && (
                <div className="bg-rose-50 border border-rose-200 text-rose-800 p-3.5 rounded-lg text-xs flex items-center gap-2">
                  <AlertTriangle className="w-4 h-4 text-rose-600 flex-shrink-0" />
                  {modalError}
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                  Subject / Course
                </label>
                <select
                  value={formData.subjectId}
                  onChange={(e) => setFormData({ ...formData, subjectId: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                >
                  {subjects.map((sub) => (
                    <option key={sub.id} value={sub.id}>
                      {sub.subjectCode} - {sub.subjectName} ({sub.credits} Credits)
                    </option>
                  ))}
                </select>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Faculty Member
                  </label>
                  <select
                    value={formData.facultyId}
                    onChange={(e) => setFormData({ ...formData, facultyId: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  >
                    {facultyList.map((f) => (
                      <option key={f.id} value={f.id}>
                        {f.firstName} {f.lastName} ({f.facultyCode})
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Classroom / Venue
                  </label>
                  <select
                    value={formData.classroomId}
                    onChange={(e) => setFormData({ ...formData, classroomId: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  >
                    {classrooms.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.roomNumber} ({c.buildingName} - Cap {c.capacity})
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Day of Week
                  </label>
                  <select
                    value={formData.dayOfWeek}
                    onChange={(e) => setFormData({ ...formData, dayOfWeek: e.target.value })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  >
                    {DAYS.map((d) => (
                      <option key={d} value={d}>
                        {d}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    Start Time
                  </label>
                  <input
                    type="time"
                    value={formData.startTime.substring(0, 5)}
                    onChange={(e) => setFormData({ ...formData, startTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                    End Time
                  </label>
                  <input
                    type="time"
                    value={formData.endTime.substring(0, 5)}
                    onChange={(e) => setFormData({ ...formData, endTime: `${e.target.value}:00` })}
                    className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                    required
                  />
                </div>
              </div>

              <div className="bg-amber-50 border border-amber-200 text-amber-800 p-3 rounded-lg text-xs leading-relaxed">
                <strong>GiST Conflict Guard:</strong> The PostgreSQL engine will reject overlapping bookings for the selected classroom, faculty, or student cohort in real time.
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm transition disabled:opacity-50"
                >
                  {submitting ? 'Verifying & Saving...' : 'Save & Publish Slot'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default TimetablePage;
