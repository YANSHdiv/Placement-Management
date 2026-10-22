import React, { useState, useEffect } from 'react';
import { academicOpsApi } from '../api/academicOps';
import { NotificationItem } from '../types/academicOps';
import { useAuth } from '../context/AuthContext';
import {
  Bell,
  CheckCircle2,
  Send,
  AlertCircle,
  Briefcase,
  Calendar,
  BookOpen,
  Award,
  Clock,
  X,
  CheckCheck,
} from 'lucide-react';

export const NotificationCenterPage: React.FC = () => {
  const { user } = useAuth();
  const canBroadcast = user?.roles?.includes('SUPER_ADMIN') || user?.roles?.includes('TPO_ADMIN') || user?.roles?.includes('HOD');

  const [notifications, setNotifications] = useState<NotificationItem[]>([]);
  const [unreadCount, setUnreadCount] = useState<number>(0);
  const [filterUnread, setFilterUnread] = useState<boolean>(false);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const [successMsg, setSuccessMsg] = useState<string | null>(null);

  // Broadcast Modal
  const [showBroadcastModal, setShowBroadcastModal] = useState<boolean>(false);
  const [broadcastForm, setBroadcastForm] = useState({
    title: '',
    message: '',
    targetAudienceType: 'ALL_STUDENTS',
    category: 'ACADEMIC',
    actionUrl: '',
  });
  const [broadcasting, setBroadcasting] = useState<boolean>(false);

  useEffect(() => {
    loadNotifications();
  }, [filterUnread]);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const [res, unread] = await Promise.all([
        academicOpsApi.getNotifications({ isRead: filterUnread ? false : undefined, size: 50 }),
        academicOpsApi.getUnreadNotificationCount(),
      ]);
      setNotifications(res.content || []);
      setUnreadCount(unread);
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to load notifications');
    } finally {
      setLoading(false);
    }
  };

  const handleMarkRead = async (id: string) => {
    try {
      await academicOpsApi.markNotificationAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true, readAt: new Date().toISOString() } : n))
      );
      setUnreadCount((c) => Math.max(0, c - 1));
    } catch (err) {
      console.error(err);
    }
  };

  const handleMarkAllRead = async () => {
    try {
      await academicOpsApi.markAllNotificationsAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      setUnreadCount(0);
      setSuccessMsg('All notifications marked as read.');
      setTimeout(() => setSuccessMsg(null), 3000);
    } catch (err) {
      console.error(err);
    }
  };

  const handleBroadcast = async (e: React.FormEvent) => {
    e.preventDefault();
    setBroadcasting(true);
    try {
      await academicOpsApi.broadcastNotification(broadcastForm);
      setShowBroadcastModal(false);
      setSuccessMsg('Announcement broadcast dispatched through Transactional Outbox!');
      setTimeout(() => setSuccessMsg(null), 5000);
      loadNotifications();
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to broadcast notification');
    } finally {
      setBroadcasting(false);
    }
  };

  const getCategoryBadge = (category: string) => {
    switch (category) {
      case 'ACADEMIC':
      case 'TIMETABLE':
        return <span className="bg-blue-50 text-blue-700 px-2 py-0.5 rounded text-[10px] font-bold border border-blue-100 flex items-center gap-1"><BookOpen className="w-3 h-3" /> ACADEMIC</span>;
      case 'RECRUITMENT_DRIVE':
      case 'INTERVIEW':
      case 'OFFER':
        return <span className="bg-emerald-50 text-emerald-700 px-2 py-0.5 rounded text-[10px] font-bold border border-emerald-100 flex items-center gap-1"><Briefcase className="w-3 h-3" /> PLACEMENT</span>;
      case 'CRT':
        return <span className="bg-purple-50 text-purple-700 px-2 py-0.5 rounded text-[10px] font-bold border border-purple-100 flex items-center gap-1"><Award className="w-3 h-3" /> TRAINING</span>;
      case 'EVENT':
        return <span className="bg-amber-50 text-amber-700 px-2 py-0.5 rounded text-[10px] font-bold border border-amber-100 flex items-center gap-1"><Calendar className="w-3 h-3" /> EVENT</span>;
      default:
        return <span className="bg-gray-100 text-gray-700 px-2 py-0.5 rounded text-[10px] font-bold">ANNOUNCEMENT</span>;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-6 rounded-xl border border-gray-100 shadow-sm">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <Bell className="w-7 h-7 text-[#8B0000]" />
            Notification Center & Institutional Outbox
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Personal alerts feed, cohort broadcasts, and transactional outbox message dispatch
          </p>
        </div>

        <div className="flex items-center gap-3">
          {unreadCount > 0 && (
            <button
              onClick={handleMarkAllRead}
              className="flex items-center gap-1.5 px-3 py-2 border border-gray-300 hover:bg-gray-50 rounded-lg text-xs font-semibold text-gray-700 transition"
            >
              <CheckCheck className="w-4 h-4 text-emerald-600" />
              Mark All as Read
            </button>
          )}

          {canBroadcast && (
            <button
              onClick={() => setShowBroadcastModal(true)}
              className="flex items-center gap-2 bg-[#8B0000] hover:bg-[#6b0000] text-white px-4 py-2 rounded-lg text-xs font-semibold shadow-sm transition"
            >
              <Send className="w-3.5 h-3.5" />
              Broadcast Alert
            </button>
          )}
        </div>
      </div>

      {successMsg && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <CheckCircle2 className="w-5 h-5 text-emerald-600 flex-shrink-0" />
          {successMsg}
        </div>
      )}
      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-800 p-4 rounded-lg flex items-center gap-2 text-sm">
          <AlertCircle className="w-5 h-5 text-rose-600 flex-shrink-0" />
          {error}
        </div>
      )}

      {/* Filter Tabs */}
      <div className="flex items-center gap-3 bg-white p-4 rounded-xl border border-gray-100 shadow-sm text-xs">
        <span className="text-gray-500 font-medium">Filter Feed:</span>
        <button
          onClick={() => setFilterUnread(false)}
          className={`px-3 py-1.5 rounded-lg font-semibold transition ${
            !filterUnread ? 'bg-[#8B0000] text-white shadow-sm' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          All Notifications
        </button>
        <button
          onClick={() => setFilterUnread(true)}
          className={`px-3 py-1.5 rounded-lg font-semibold transition flex items-center gap-1.5 ${
            filterUnread ? 'bg-[#8B0000] text-white shadow-sm' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
          }`}
        >
          Unread Only
          {unreadCount > 0 && (
            <span
              className={`px-1.5 py-0.2 rounded-full text-[10px] font-bold ${
                filterUnread ? 'bg-white text-[#8B0000]' : 'bg-[#8B0000] text-white'
              }`}
            >
              {unreadCount}
            </span>
          )}
        </button>
      </div>

      {/* Notifications List */}
      <div className="bg-white rounded-xl border border-gray-100 shadow-sm divide-y divide-gray-100 overflow-hidden">
        {loading ? (
          <div className="text-center py-20 text-gray-400 text-sm">Loading notification feed...</div>
        ) : notifications.length === 0 ? (
          <div className="text-center py-20 text-gray-400">
            <Bell className="w-12 h-12 text-gray-300 mx-auto mb-3" />
            <h3 className="text-base font-semibold text-gray-700">No notifications in your inbox</h3>
            <p className="text-xs text-gray-400 mt-1">You are all caught up on campus and placement activities.</p>
          </div>
        ) : (
          notifications.map((n) => (
            <div
              key={n.id}
              className={`p-5 transition flex items-start justify-between gap-4 ${
                !n.isRead ? 'bg-rose-50/20 hover:bg-rose-50/40' : 'hover:bg-gray-50/50'
              }`}
            >
              <div className="flex items-start gap-3.5">
                <div
                  className={`w-2.5 h-2.5 rounded-full mt-1.5 flex-shrink-0 ${
                    !n.isRead ? 'bg-[#8B0000] ring-4 ring-rose-100' : 'bg-transparent'
                  }`}
                />
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    {getCategoryBadge(n.category)}
                    <span className="text-[11px] text-gray-400 flex items-center gap-1">
                      <Clock className="w-3 h-3" />
                      {new Date(n.createdAt).toLocaleString()}
                    </span>
                  </div>

                  <h4 className="font-bold text-gray-900 text-sm">{n.title}</h4>
                  <p className="text-xs text-gray-600 mt-1 leading-relaxed">{n.message}</p>

                  {n.actionUrl && (
                    <a
                      href={n.actionUrl}
                      className="inline-block mt-2 text-xs font-semibold text-[#8B0000] hover:underline"
                    >
                      View Details →
                    </a>
                  )}
                </div>
              </div>

              {!n.isRead && (
                <button
                  onClick={() => handleMarkRead(n.id)}
                  className="text-xs text-gray-400 hover:text-gray-700 font-medium px-2 py-1 rounded hover:bg-gray-100 flex-shrink-0 transition"
                  title="Mark as read"
                >
                  Mark read
                </button>
              )}
            </div>
          ))
        )}
      </div>

      {/* Broadcast Modal */}
      {showBroadcastModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-lg border border-gray-100 overflow-hidden">
            <div className="flex items-center justify-between p-5 border-b border-gray-100 bg-gray-50">
              <h3 className="font-bold text-gray-900 text-base flex items-center gap-2">
                <Send className="w-4 h-4 text-[#8B0000]" />
                Broadcast Institutional Alert
              </h3>
              <button onClick={() => setShowBroadcastModal(false)} className="text-gray-400 hover:text-gray-600">
                <X className="w-5 h-5" />
              </button>
            </div>
            <form onSubmit={handleBroadcast} className="p-6 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Target Audience</label>
                <select
                  value={broadcastForm.targetAudienceType}
                  onChange={(e) => setBroadcastForm({ ...broadcastForm, targetAudienceType: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                >
                  <option value="ALL_STUDENTS">All Registered Students</option>
                  <option value="ALL_FACULTY">All Faculty Members</option>
                  <option value="PROGRAM">B.Tech Computer Science Program</option>
                  <option value="DEPARTMENT">Engineering Department</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Notification Category</label>
                <select
                  value={broadcastForm.category}
                  onChange={(e) => setBroadcastForm({ ...broadcastForm, category: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                >
                  <option value="ACADEMIC">ACADEMIC / EXAMINATIONS</option>
                  <option value="TIMETABLE">TIMETABLE UPDATE</option>
                  <option value="RECRUITMENT_DRIVE">PLACEMENT DRIVE</option>
                  <option value="CRT">CRT TRAINING</option>
                  <option value="EVENT">CAMPUS EVENT</option>
                  <option value="GENERAL">GENERAL ANNOUNCEMENT</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Subject / Title</label>
                <input
                  type="text"
                  placeholder="e.g. Mandatory Pre-Placement Session for 2026 Batch"
                  value={broadcastForm.title}
                  onChange={(e) => setBroadcastForm({ ...broadcastForm, title: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Broadcast Message Body</label>
                <textarea
                  rows={4}
                  placeholder="Detailed announcement content..."
                  value={broadcastForm.message}
                  onChange={(e) => setBroadcastForm({ ...broadcastForm, message: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">Action URL (Optional)</label>
                <input
                  type="text"
                  placeholder="e.g. /timetables or /crt"
                  value={broadcastForm.actionUrl}
                  onChange={(e) => setBroadcastForm({ ...broadcastForm, actionUrl: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-1 focus:ring-[#8B0000] focus:outline-none"
                />
              </div>

              <div className="bg-gray-50 p-3 rounded-lg border border-gray-100 text-[11px] text-gray-500">
                <strong>Transactional Outbox:</strong> This notification is persisted to the institutional outbox ledger for resilient, asynchronous multi-channel dissemination.
              </div>

              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setShowBroadcastModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={broadcasting}
                  className="px-4 py-2 bg-[#8B0000] hover:bg-[#6b0000] text-white rounded-lg text-sm font-medium shadow-sm transition disabled:opacity-50"
                >
                  {broadcasting ? 'Dispatching...' : 'Dispatch Broadcast'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
export default NotificationCenterPage;
