import React, { useState, useEffect } from 'react';
import { interviewApi } from '../api/interview';
import { placementApi } from '../api/placement';
import {
  InterviewQuestion,
  InterviewQuestionCreateRequest,
  QuestionCategory,
  QuestionDifficulty,
  QuestionType,
} from '../types/interview';
import { Company } from '../types/placement';
import { useAuth } from '../context/AuthContext';

export const QuestionBankPage: React.FC = () => {
  const { user } = useAuth();
  const isStaff = user?.roles.some((r) => ['SUPER_ADMIN', 'TPO_ADMIN', 'TPO_COORDINATOR', 'FACULTY'].includes(r));

  const [questions, setQuestions] = useState<InterviewQuestion[]>([]);
  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [totalElements, setTotalElements] = useState(0);

  // Filters
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState<string>('');
  const [difficulty, setDifficulty] = useState<string>('');
  const [questionType, setQuestionType] = useState<string>('');
  const [companyId, setCompanyId] = useState<string>('');

  // Modals & Active Question
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedQuestion, setSelectedQuestion] = useState<InterviewQuestion | null>(null);
  const [msg, setMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);

  const [createForm, setCreateForm] = useState<InterviewQuestionCreateRequest>({
    category: 'DATA_STRUCTURES',
    difficulty: 'MEDIUM',
    questionType: 'CODING',
    questionTitle: '',
    problemStatement: '',
    expectedAnswerOrRubric: '',
    sampleInputOutput: '',
    confidentialNotes: '',
    suggestedDurationMinutes: 30,
    tags: [],
  });
  const [tagsInput, setTagsInput] = useState('');

  useEffect(() => {
    fetchCompanies();
  }, []);

  useEffect(() => {
    fetchQuestions();
  }, [category, difficulty, questionType, companyId]);

  const fetchCompanies = async () => {
    try {
      const res = await placementApi.getCompanies({ size: 100 });
      setCompanies(res.content);
    } catch (err) {
      console.error('Failed to load companies for filter', err);
    }
  };

  const fetchQuestions = async () => {
    try {
      setLoading(true);
      const res = await interviewApi.searchQuestions({
        query: query.trim() || undefined,
        category: category || undefined,
        difficulty: difficulty || undefined,
        questionType: questionType || undefined,
        companyId: companyId || undefined,
        size: 50,
      });
      setQuestions(res.content);
      setTotalElements(res.totalElements);
    } catch (err) {
      console.error('Failed to search question bank', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    fetchQuestions();
  };

  const handleCreateQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setMsg(null);
      const tagsArray = tagsInput
        .split(',')
        .map((t) => t.trim())
        .filter((t) => t.length > 0);

      await interviewApi.createQuestion({
        ...createForm,
        tags: tagsArray,
      });
      setShowCreateModal(false);
      setMsg({ text: 'Question added to Question Bank successfully!', type: 'success' });
      fetchQuestions();
    } catch (err: any) {
      setMsg({ text: err.response?.data?.message || 'Failed to create question', type: 'error' });
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* Header */}
      <div className="mb-6 flex flex-col md:flex-row md:items-center md:justify-between border-b pb-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2">
            <span className="inline-block w-3 h-6 bg-red-700 rounded-sm"></span>
            Technical & HR Interview Question Bank
          </h1>
          <p className="text-sm text-gray-600 mt-1">
            Curated repository of coding challenges, system design prompts, core CS concepts, and HR behavioral questions.
          </p>
        </div>
        {isStaff && (
          <button
            onClick={() => {
              setCreateForm({
                category: 'DATA_STRUCTURES',
                difficulty: 'MEDIUM',
                questionType: 'CODING',
                questionTitle: '',
                problemStatement: '',
                expectedAnswerOrRubric: '',
                sampleInputOutput: '',
                confidentialNotes: '',
                suggestedDurationMinutes: 30,
                tags: [],
              });
              setTagsInput('');
              setShowCreateModal(true);
            }}
            className="mt-3 md:mt-0 px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold shadow transition"
          >
            + Add New Question
          </button>
        )}
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

      {/* Filters Bar */}
      <div className="bg-white p-4 rounded-lg border border-gray-200 shadow-sm mb-6 space-y-3">
        <form onSubmit={handleSearchSubmit} className="flex gap-2">
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search questions by title, problem statement, or tags..."
            className="flex-1 border rounded px-3 py-2 text-sm focus:ring-1 focus:ring-red-600"
          />
          <button
            type="submit"
            className="px-5 py-2 bg-gray-800 hover:bg-black text-white rounded text-sm font-semibold transition"
          >
            Search
          </button>
        </form>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-3 pt-2 border-t text-xs">
          <div>
            <label className="block text-gray-600 font-medium mb-1">Category</label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value)}
              className="w-full border rounded px-2.5 py-1.5 focus:ring-1 focus:ring-red-600"
            >
              <option value="">All Categories</option>
              <option value="DATA_STRUCTURES">Data Structures</option>
              <option value="ALGORITHMS">Algorithms</option>
              <option value="SYSTEM_DESIGN">System Design</option>
              <option value="CS_CORE">CS Core (OS/DBMS/CN)</option>
              <option value="HR_BEHAVIORAL">HR & Behavioral</option>
              <option value="DOMAIN_SPECIFIC">Domain Specific</option>
            </select>
          </div>

          <div>
            <label className="block text-gray-600 font-medium mb-1">Difficulty</label>
            <select
              value={difficulty}
              onChange={(e) => setDifficulty(e.target.value)}
              className="w-full border rounded px-2.5 py-1.5 focus:ring-1 focus:ring-red-600"
            >
              <option value="">All Difficulties</option>
              <option value="EASY">Easy</option>
              <option value="MEDIUM">Medium</option>
              <option value="HARD">Hard</option>
            </select>
          </div>

          <div>
            <label className="block text-gray-600 font-medium mb-1">Question Type</label>
            <select
              value={questionType}
              onChange={(e) => setQuestionType(e.target.value)}
              className="w-full border rounded px-2.5 py-1.5 focus:ring-1 focus:ring-red-600"
            >
              <option value="">All Types</option>
              <option value="CODING">Coding Problem</option>
              <option value="THEORETICAL">Theoretical</option>
              <option value="SYSTEM_DESIGN">System Design</option>
              <option value="BEHAVIORAL">Behavioral / HR</option>
            </select>
          </div>

          <div>
            <label className="block text-gray-600 font-medium mb-1">Associated Company</label>
            <select
              value={companyId}
              onChange={(e) => setCompanyId(e.target.value)}
              className="w-full border rounded px-2.5 py-1.5 focus:ring-1 focus:ring-red-600"
            >
              <option value="">All Companies / Generic</option>
              {companies.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.companyName}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Questions List & Details Modal Grid */}
      <div className="bg-white rounded-lg border border-gray-200 shadow-sm p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-sm font-semibold uppercase tracking-wider text-gray-600">
            Available Questions ({totalElements})
          </h2>
        </div>

        {loading ? (
          <div className="p-8 text-center text-gray-400">Loading questions...</div>
        ) : questions.length === 0 ? (
          <div className="p-8 text-center text-gray-500 bg-gray-50 rounded">
            No questions matching your criteria found.
          </div>
        ) : (
          <div className="space-y-3">
            {questions.map((q) => {
              const diffBg =
                q.difficulty === 'EASY'
                  ? 'bg-green-100 text-green-800'
                  : q.difficulty === 'MEDIUM'
                  ? 'bg-amber-100 text-amber-800'
                  : 'bg-red-100 text-red-800';

              return (
                <div
                  key={q.id}
                  onClick={() => setSelectedQuestion(q)}
                  className="p-4 rounded-lg border border-gray-200 hover:border-red-300 hover:bg-red-50/20 cursor-pointer transition flex flex-col md:flex-row md:items-center md:justify-between gap-3"
                >
                  <div className="space-y-1">
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${diffBg}`}>
                        {q.difficulty}
                      </span>
                      <span className="text-[10px] font-medium px-2 py-0.5 rounded bg-gray-100 text-gray-700">
                        {q.category.replace('_', ' ')}
                      </span>
                      {q.companyName && (
                        <span className="text-[10px] font-semibold px-2 py-0.5 rounded bg-purple-100 text-purple-800">
                          {q.companyName}
                        </span>
                      )}
                      <span className="text-xs text-gray-400">
                        • {q.suggestedDurationMinutes || 30} mins
                      </span>
                    </div>

                    <h3 className="font-bold text-gray-900 text-base">{q.questionTitle}</h3>
                    <p className="text-xs text-gray-600 line-clamp-2 max-w-3xl">
                      {q.problemStatement}
                    </p>

                    {q.tags && q.tags.length > 0 && (
                      <div className="flex gap-1.5 pt-1 flex-wrap">
                        {q.tags.map((t, idx) => (
                          <span key={idx} className="text-[10px] bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded">
                            #{t}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div className="text-right whitespace-nowrap text-xs text-red-700 font-semibold self-start md:self-center">
                    View Problem ➔
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* QUESTION DETAIL MODAL */}
      {selectedQuestion && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <div className="border-b pb-3 flex justify-between items-start">
              <div>
                <div className="flex items-center gap-2">
                  <span
                    className={`text-[10px] font-bold px-2 py-0.5 rounded ${
                      selectedQuestion.difficulty === 'EASY'
                        ? 'bg-green-100 text-green-800'
                        : selectedQuestion.difficulty === 'MEDIUM'
                        ? 'bg-amber-100 text-amber-800'
                        : 'bg-red-100 text-red-800'
                    }`}
                  >
                    {selectedQuestion.difficulty}
                  </span>
                  <span className="text-xs text-gray-500">{selectedQuestion.category}</span>
                </div>
                <h3 className="text-lg font-bold text-gray-900 mt-1">{selectedQuestion.questionTitle}</h3>
              </div>
              <button
                onClick={() => setSelectedQuestion(null)}
                className="text-gray-400 hover:text-gray-600 text-lg font-bold"
              >
                ✕
              </button>
            </div>

            <div className="space-y-4 text-xs">
              <div>
                <h4 className="font-bold uppercase tracking-wider text-gray-700 mb-1">Problem Statement</h4>
                <div className="p-3 bg-gray-50 rounded border text-gray-800 whitespace-pre-wrap leading-relaxed">
                  {selectedQuestion.problemStatement}
                </div>
              </div>

              {selectedQuestion.sampleInputOutput && (
                <div>
                  <h4 className="font-bold uppercase tracking-wider text-gray-700 mb-1">Sample Input / Output</h4>
                  <pre className="p-3 bg-gray-900 text-green-400 font-mono rounded overflow-x-auto text-xs">
                    {selectedQuestion.sampleInputOutput}
                  </pre>
                </div>
              )}

              {selectedQuestion.expectedAnswerOrRubric && (
                <div>
                  <h4 className="font-bold uppercase tracking-wider text-gray-700 mb-1">Expected Solution / Rubric</h4>
                  <div className="p-3 bg-amber-50 border border-amber-200 rounded text-amber-900 whitespace-pre-wrap">
                    {selectedQuestion.expectedAnswerOrRubric}
                  </div>
                </div>
              )}

              {/* Confidential notes visible only if returned by backend (evaluators/TPO) */}
              {selectedQuestion.confidentialNotes && (
                <div className="bg-red-50 border border-red-200 rounded p-3">
                  <div className="font-bold text-red-900 uppercase tracking-wider mb-1 flex items-center justify-between">
                    <span>Confidential Evaluator Notes & Hints</span>
                    <span className="text-[10px] bg-red-200 text-red-900 px-1 py-0.5 rounded font-bold">STAFF ONLY</span>
                  </div>
                  <div className="text-red-800 whitespace-pre-wrap">{selectedQuestion.confidentialNotes}</div>
                </div>
              )}
            </div>

            <div className="flex justify-end pt-3 border-t">
              <button
                onClick={() => setSelectedQuestion(null)}
                className="px-5 py-2 bg-gray-800 hover:bg-black text-white rounded text-xs font-semibold"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* CREATE QUESTION MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg max-w-xl w-full p-6 space-y-4 max-h-[90vh] overflow-y-auto">
            <h3 className="text-lg font-bold text-gray-900 border-b pb-2">Add Interview Question to Repository</h3>
            <form onSubmit={handleCreateQuestion} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Question Title *</label>
                <input
                  type="text"
                  required
                  value={createForm.questionTitle}
                  onChange={(e) => setCreateForm({ ...createForm, questionTitle: e.target.value })}
                  placeholder="e.g. Reverse Nodes in k-Group"
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="grid grid-cols-3 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Category *</label>
                  <select
                    value={createForm.category}
                    onChange={(e) => setCreateForm({ ...createForm, category: e.target.value as QuestionCategory })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="DATA_STRUCTURES">Data Structures</option>
                    <option value="ALGORITHMS">Algorithms</option>
                    <option value="SYSTEM_DESIGN">System Design</option>
                    <option value="CS_CORE">CS Core</option>
                    <option value="HR_BEHAVIORAL">HR & Behavioral</option>
                    <option value="DOMAIN_SPECIFIC">Domain Specific</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Difficulty *</label>
                  <select
                    value={createForm.difficulty}
                    onChange={(e) => setCreateForm({ ...createForm, difficulty: e.target.value as QuestionDifficulty })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="EASY">Easy</option>
                    <option value="MEDIUM">Medium</option>
                    <option value="HARD">Hard</option>
                  </select>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Question Type *</label>
                  <select
                    value={createForm.questionType}
                    onChange={(e) => setCreateForm({ ...createForm, questionType: e.target.value as QuestionType })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="CODING">Coding</option>
                    <option value="THEORETICAL">Theoretical</option>
                    <option value="SYSTEM_DESIGN">System Design</option>
                    <option value="BEHAVIORAL">Behavioral</option>
                  </select>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Associated Company</label>
                  <select
                    value={createForm.companyId || ''}
                    onChange={(e) => setCreateForm({ ...createForm, companyId: e.target.value || undefined })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  >
                    <option value="">Generic / All Companies</option>
                    {companies.map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.companyName}
                      </option>
                    ))}
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-700 mb-1">Suggested Duration (mins)</label>
                  <input
                    type="number"
                    min="5"
                    max="180"
                    value={createForm.suggestedDurationMinutes}
                    onChange={(e) => setCreateForm({ ...createForm, suggestedDurationMinutes: parseInt(e.target.value) || 30 })}
                    className="w-full border rounded px-3 py-2 text-sm"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Problem Statement *</label>
                <textarea
                  rows={4}
                  required
                  value={createForm.problemStatement}
                  onChange={(e) => setCreateForm({ ...createForm, problemStatement: e.target.value })}
                  placeholder="Detailed description of the problem, constraints, edge cases..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Sample Input / Output</label>
                <textarea
                  rows={2}
                  value={createForm.sampleInputOutput}
                  onChange={(e) => setCreateForm({ ...createForm, sampleInputOutput: e.target.value })}
                  placeholder="Input: head = [1,2,3,4,5], k = 2&#10;Output: [2,1,4,3,5]"
                  className="w-full border rounded font-mono text-xs px-3 py-2"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Expected Answer / Scoring Rubric</label>
                <textarea
                  rows={3}
                  value={createForm.expectedAnswerOrRubric}
                  onChange={(e) => setCreateForm({ ...createForm, expectedAnswerOrRubric: e.target.value })}
                  placeholder="Optimal solution in O(N) time and O(1) space. Common pitfalls to watch for..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">
                  Confidential Evaluator Notes (Hidden from Students)
                </label>
                <textarea
                  rows={2}
                  value={createForm.confidentialNotes}
                  onChange={(e) => setCreateForm({ ...createForm, confidentialNotes: e.target.value })}
                  placeholder="Panel follow-up questions, benchmark hints..."
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 mb-1">Tags (comma separated)</label>
                <input
                  type="text"
                  value={tagsInput}
                  onChange={(e) => setTagsInput(e.target.value)}
                  placeholder="linked-list, recursion, faang, hard"
                  className="w-full border rounded px-3 py-2 text-sm"
                />
              </div>

              <div className="flex justify-end gap-2 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border rounded text-sm text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-red-700 hover:bg-red-800 text-white rounded text-sm font-semibold"
                >
                  Save Question
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
