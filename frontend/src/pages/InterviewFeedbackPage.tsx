import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/interview';
import { placementApi } from '../api/placement';
import {
  InterviewFeedback,
  InterviewFeedbackCreateRequest,
  FeedbackType,
} from '../types/interview';
import { PlacementDrive } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const InterviewFeedbackPage: React.FC = () => {
  const { user } = useAuth();
  const isStaff = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN', 'TPO_COORDINATOR'].includes(r));

  const [feedbackList, setFeedbackList] = useState<InterviewFeedback[]>([]);
  const [drives, setDrives] = useState<PlacementDrive[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  // Submit Feedback Modal
  const [showSubmitModal, setShowSubmitModal] = useState(false);
  const [feedbackForm, setFeedbackForm] = useState<InterviewFeedbackCreateRequest>({
    feedbackType: 'STUDENT_TO_DRIVE',
    overallRating: 5,
    difficultyRating: 3,
    transparencyRating: 5,
    recommendToPeers: true,
    qualitativeComments: '',
    processImprovementsSuggestion: '',
    confidentialTpoNotes: '',
  });

  const [filterType, setFilterType] = useState<string>('');

  useEffect(() => {
    fetchInitial();
  }, [filterType]);

  const fetchInitial = async () => {
    try {
      setLoading(true);
      const [drivesRes, fbRes] = await Promise.all([
        placementApi.getDrives({ size: 100 }),
        isStaff
          ? interviewApi.searchFeedback({
              feedbackType: filterType || undefined,
              size: 50,
            })
          : Promise.resolve({ content: [], totalElements: 0 }),
      ]);
      setDrives(drivesRes.content);
      if (drivesRes.content.length > 0 && !feedbackForm.driveId) {
        setFeedbackForm((prev) => ({ ...prev, driveId: drivesRes.content[0].id }));
      }
      setFeedbackList(fbRes.content);
    } catch (err) {
      console.error('Failed to load feedback data', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitFeedback = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setMsg(null);
      await interviewApi.submitFeedback(feedbackForm);
      setShowSubmitModal(false);
      setMsg({ text: 'Recruitment feedback submitted successfully! Thank you.', type: 'success' });
      if (isStaff) {
        fetchInitial();
      }
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to submit feedback', type: 'error' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            Recruitment Drive Experience & Interview Feedback
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Share qualitative insights, process transparency reviews, and continuous improvement recommendations.
          </p>
        </div>
        <button
          onClick={() => setShowSubmitModal(true)}
          className="mt-3 md:mt-0 px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold shadow transition"
        >
          + Submit Drive Feedback
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

      {/* Staff View: Feedback Explorer */}
      {isStaff ? (
        <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6 space-y-4">
          <div className="flex justify-between items-center border-b pb-3">
            <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-700">
              Submitted Feedback Entries ({feedbackList.length})
            </h2>
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="text-xs border rounded px-3 py-1.5 focus:ring-1 focus:ring-red-600"
            >
              <option value="">All Feedback Types</option>
              <option value="STUDENT_TO_DRIVE">Student to Drive</option>
              <option value="EVALUATOR_TO_CANDIDATE">Evaluator to Candidate</option>
              <option value="TPO_TO_COMPANY">TPO to Company</option>
            </select>
          </div>

          {loading ? (
            <div className="p-8 text-center text-gray-400">Loading feedback records...</div>
          ) : feedbackList.length === 0 ? (
            <div className="p-8 text-center text-gray-500 bg-gray-50 rounded text-sm">
              No feedback submissions recorded matching the selected filter.
            </div>
          ) : (
            <div className="space-y-4">
              {feedbackList.map((fb) => (
                <div key={fb.id} className="p-4 rounded-lg border border-gray-200 bg-white space-y-2">
                  <div className="flex justify-between items-start">
                    <div>
                      <span className="text-xs font-semibold px-2 py-0.5 rounded bg-red-50 text-red-800 border border-red-200">
                        {fb.feedbackType.replace(/_/g, ' ')}
                      </span>
                      <h3 className="font-bold text-gray-900 text-sm mt-1">
                        {fb.companyName || 'Recruitment Drive'} • {fb.driveTitle || 'General Drive'}
                      </h3>
                      <div className="text-xs text-gray-500">
                        Submitted by: {fb.authorName || fb.authorEmail || 'Anonymous'} on {new Date(fb.createdAt).toLocaleDateString()}
                      </div>
                    </div>
                    <div className="text-right">
                      <div className="text-sm font-bold text-gray-900">
                        ⭐ {fb.overallRating} / 5
                      </div>
                      <span className="text-[10px] text-gray-500 uppercase tracking-wide">
                        Overall Rating
                      </span>
                    </div>
                  </div>

                  <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-xs pt-2 border-t text-gray-700">
                    <div>
                      <span className="font-medium text-gray-500">Difficulty: </span>
                      {fb.difficultyRating ? `${fb.difficultyRating} / 5` : 'N/A'}
                    </div>
                    <div>
                      <span className="font-medium text-gray-500">Transparency: </span>
                      {fb.transparencyRating ? `${fb.transparencyRating} / 5` : 'N/A'}
                    </div>
                    <div>
                      <span className="font-medium text-gray-500">Recommend to Peers: </span>
                      {fb.recommendToPeers ? 'Yes' : 'No'}
                    </div>
                  </div>

                  {fb.qualitativeComments && (
                    <div className="text-xs text-gray-800 bg-gray-50 p-2.5 rounded border">
                      <span className="font-semibold text-gray-600">Candidate Comments: </span>
                      {fb.qualitativeComments}
                    </div>
                  )}

                  {fb.processImprovementsSuggestion && (
                    <div className="text-xs text-amber-900 bg-amber-50 p-2.5 rounded border border-amber-200">
                      <span className="font-semibold">Improvement Suggestions: </span>
                      {fb.processImprovementsSuggestion}
                    </div>
                  )}

                  {fb.confidentialTpoNotes && (
                    <div className="text-xs text-red-900 bg-red-50 p-2.5 rounded border border-red-200">
                      <span className="font-bold">Confidential TPO Notes: </span>
                      {fb.confidentialTpoNotes}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      ) : (
        /* Student View: Friendly prompt and historical submission card */
        <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-8 text-center space-y-4">
          <div className="w-16 h-16 bg-red-100 text-red-800 rounded-full flex items-center justify-center mx-auto text-2xl">
            💬
          </div>
          <h2 className="text-lg font-bold text-gray-900">Your Placement Drive Feedback Matters</h2>
          <p className="text-sm text-gray-600 max-w-lg mx-auto">
            JECRC Training & Placement Office continuously reviews student feedback on company assessment rigor, interview panel conduct, and organizational transparency to guide future campus recruitment.
          </p>
          <div>
            <button
              onClick={() => setShowSubmitModal(true)}
              className="px-6 py-2.5 bg-red-700 hover:bg-red-800 text-white rounded font-semibold text-sm shadow transition"
            >
              Submit Drive Feedback Now
            </button>
          </div>
        </div>
      )}

      {/* SUBMIT FEEDBACK MODAL */}
      {showSubmitModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-lg w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Submit Recruitment Drive Feedback</h3>
            <form onSubmit={handleSubmitFeedback} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Select Drive *</label>
                <select
                  required
                  value={feedbackForm.driveId || ''}
                  onChange={(e) => setFeedbackForm({ ...feedbackForm, driveId: e.target.value })}
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

              {isStaff && (
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Feedback Type *</label>
                  <select
                    value={feedbackForm.feedbackType}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, feedbackType: e.target.value as FeedbackType })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="STUDENT_TO_DRIVE">Student to Drive</option>
                    <option value="EVALUATOR_TO_CANDIDATE">Evaluator to Candidate</option>
                    <option value="TPO_TO_COMPANY">TPO to Company</option>
                  </select>
                </div>
              )}

              {/* Ratings Grid */}
              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Overall (1-5) *</label>
                  <input
                    type="number"
                    min="1"
                    max="5"
                    required
                    value={feedbackForm.overallRating}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, overallRating: parseInt(e.target.value) || 5 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Difficulty (1-5)</label>
                  <input
                    type="number"
                    min="1"
                    max="5"
                    value={feedbackForm.difficultyRating || 3}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, difficultyRating: parseInt(e.target.value) || 3 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Transparency (1-5)</label>
                  <input
                    type="number"
                    min="1"
                    max="5"
                    value={feedbackForm.transparencyRating || 5}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, transparencyRating: parseInt(e.target.value) || 5 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
              </div>

              <div className="flex items-center gap-2 pt-1">
                <input
                  type="checkbox"
                  id="recPeers"
                  checked={feedbackForm.recommendToPeers}
                  onChange={(e) => setFeedbackForm({ ...feedbackForm, recommendToPeers: e.target.checked })}
                  className="rounded text-red-700 focus:ring-red-600"
                />
                <label htmlFor="recPeers" className="text-xs text-gray-700 font-medium">
                  Would you recommend this company drive to your junior peers?
                </label>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Qualitative Feedback / Experience Notes
                </label>
                <textarea
                  rows={3}
                  value={feedbackForm.qualitativeComments}
                  onChange={(e) => setFeedbackForm({ ...feedbackForm, qualitativeComments: e.target.value })}
                  placeholder="Structure of interview, topics asked, panel demeanor..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Suggestions for Process Improvement
                </label>
                <textarea
                  rows={2}
                  value={feedbackForm.processImprovementsSuggestion}
                  onChange={(e) => setFeedbackForm({ ...feedbackForm, processImprovementsSuggestion: e.target.value })}
                  placeholder="Earlier communication of slots, clearer problem statement specs..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              {isStaff && (
                <div>
                  <label className="block text-xs font-semibold text-red-900 mb-1">
                    Confidential TPO Committee Notes
                  </label>
                  <textarea
                    rows={2}
                    value={feedbackForm.confidentialTpoNotes}
                    onChange={(e) => setFeedbackForm({ ...feedbackForm, confidentialTpoNotes: e.target.value })}
                    placeholder="Internal relationship notes, company coordinator response time..."
                    className="w-full border rounded px-3 py-2 text-sm border-red-200"
                  />
                </div>
              )}

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowSubmitModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Submit Feedback
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
