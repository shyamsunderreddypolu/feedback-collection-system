import React, { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import API from "../api/axiosInstance";
import "../css/AdminDashboard.css";

function FormBuilderStudio() {
  const [activeForm, setActiveForm] = useState(null);

  // Form Creation State
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [endDate, setEndDate] = useState("");

  // Question Creation State
  const [questionText, setQuestionText] = useState("");
  const [questionType, setQuestionType] = useState("RATING");
  const [questions, setQuestions] = useState([]);

  // Assignment Modal State
  const [showTargetModal, setShowTargetModal] = useState(false);
  const [departmentCode, setDepartmentCode] = useState("CSE");
  const [academicYear] = useState("2025-2026");
  const [semester, setSemester] = useState(5);
  const [section, setSection] = useState("A");

  const [message, setMessage] = useState({ type: "", text: "" });

  useEffect(() => {
    fetchForms();
  }, []);

  const fetchForms = async () => {
    try {
      await API.get("/forms/active");
    } catch (err) {
      console.error("Error loading forms", err);
    }
  };

  const handleCreateForm = async (e) => {
    e.preventDefault();
    try {
      setMessage({ type: "", text: "" });
      const response = await API.post("/forms", {
        title,
        description,
        startDate: new Date().toISOString(),
        endDate: endDate ? new Date(endDate).toISOString() : new Date(Date.now() + 7 * 86400000).toISOString()
      });

      setActiveForm(response.data);
      setMessage({ type: "success", text: `Form "${response.data.title}" created in DRAFT status!` });
      fetchForms();
    } catch (err) {
      console.error("Error creating form", err);
      setMessage({ type: "error", text: err.response?.data?.message || "Failed to create feedback form." });
    }
  };

  const handleAddQuestion = async (e) => {
    e.preventDefault();
    if (!activeForm) return;

    try {
      const response = await API.post("/questions", {
        formId: activeForm.id,
        questionText,
        questionType,
        displayOrder: questions.length + 1,
        mandatory: true
      });

      setQuestions([...questions, response.data]);
      setQuestionText("");
      setMessage({ type: "success", text: "Question added successfully!" });
    } catch (err) {
      console.error("Error adding question", err);
      setMessage({ type: "error", text: "Failed to add question." });
    }
  };

  const handlePublishForm = async () => {
    if (!activeForm) return;
    try {
      await API.put(`/forms/${activeForm.id}/publish`);
      setMessage({ type: "success", text: "Form published successfully! Students can now submit responses." });
      fetchForms();
    } catch (err) {
      console.error("Error publishing form", err);
      setMessage({ type: "error", text: err.response?.data?.message || "Publish failed. Form requires at least 1 question." });
    }
  };

  const handleAssignForm = async (e) => {
    e.preventDefault();
    if (!activeForm) return;
    try {
      await API.post("/assignments", {
        feedbackFormId: activeForm.id,
        departmentCode,
        academicYear,
        semester: Number(semester),
        section,
        batch: academicYear
      });

      setMessage({ type: "success", text: `Form assigned to ${departmentCode} Sem ${semester} Section ${section}!` });
      setShowTargetModal(false);
    } catch (err) {
      console.error("Error assigning form", err);
      setMessage({ type: "error", text: err.response?.data?.message || "Failed to assign form." });
    }
  };

  return (
    <div className="dashboard-container" style={{ display: "flex", minHeight: "100vh", backgroundColor: "#f8fafc" }}>
      <Sidebar />
      <div className="main-content" style={{ flex: 1, padding: "2rem" }}>
        <Navbar />

        <div className="page-header" style={{ marginBottom: "2rem" }}>
          <h2 style={{ color: "#0f172a", fontSize: "1.8rem", fontWeight: "700" }}>🛠️ Survey Form Builder Studio</h2>
          <p style={{ color: "#64748b" }}>Create, build question items, publish, and target surveys to academic cohorts.</p>
        </div>

        {message.text && (
          <div style={{
            padding: "1rem",
            borderRadius: "8px",
            marginBottom: "1.5rem",
            backgroundColor: message.type === "success" ? "#f0fdf4" : "#fef2f2",
            color: message.type === "success" ? "#166534" : "#991b1b",
            border: `1px solid ${message.type === "success" ? "#bbf7d0" : "#fecaca"}`
          }}>
            {message.text}
          </div>
        )}

        <div style={{ display: "grid", gridTemplateColumns: "1fr 2fr", gap: "2rem" }}>
          {/* Create Form Section */}
          <div style={{ backgroundColor: "#ffffff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
            <h3 style={{ fontSize: "1.2rem", color: "#0f172a", marginBottom: "1rem" }}>1. Create New Form</h3>
            <form onSubmit={handleCreateForm} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
              <div>
                <label style={{ fontSize: "0.85rem", fontWeight: "600", color: "#475569" }}>Form Title *</label>
                <input
                  type="text"
                  placeholder="e.g. CS101 Instructor Evaluation"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                  style={{ width: "100%", padding: "0.6rem", borderRadius: "6px", border: "1px solid #cbd5e1", marginTop: "0.25rem" }}
                />
              </div>

              <div>
                <label style={{ fontSize: "0.85rem", fontWeight: "600", color: "#475569" }}>Description</label>
                <textarea
                  rows="3"
                  placeholder="Enter survey description..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  style={{ width: "100%", padding: "0.6rem", borderRadius: "6px", border: "1px solid #cbd5e1", marginTop: "0.25rem" }}
                />
              </div>

              <div>
                <label style={{ fontSize: "0.85rem", fontWeight: "600", color: "#475569" }}>End Date</label>
                <input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  style={{ width: "100%", padding: "0.6rem", borderRadius: "6px", border: "1px solid #cbd5e1", marginTop: "0.25rem" }}
                />
              </div>

              <button type="submit" style={{ backgroundColor: "#2563eb", color: "#ffffff", border: "none", padding: "0.75rem", borderRadius: "6px", fontWeight: "600", cursor: "pointer" }}>
                Save Draft Form
              </button>
            </form>
          </div>

          {/* Question Builder Section */}
          <div style={{ backgroundColor: "#ffffff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem", borderBottom: "1px solid #f1f5f9", paddingBottom: "1rem" }}>
              <div>
                <h3 style={{ fontSize: "1.2rem", color: "#0f172a" }}>2. Question Builder</h3>
                <p style={{ fontSize: "0.85rem", color: "#64748b" }}>
                  Active Form: <strong>{activeForm?.title || "No Form Selected"}</strong>
                </p>
              </div>

              {activeForm && (
                <div style={{ display: "flex", gap: "0.5rem" }}>
                  <button onClick={() => setShowTargetModal(true)} style={{ backgroundColor: "#0284c7", color: "#fff", border: "none", padding: "0.5rem 1rem", borderRadius: "6px", fontWeight: "600", cursor: "pointer" }}>
                    Target Cohort
                  </button>
                  <button onClick={handlePublishForm} style={{ backgroundColor: "#16a34a", color: "#fff", border: "none", padding: "0.5rem 1rem", borderRadius: "6px", fontWeight: "600", cursor: "pointer" }}>
                    Publish Form
                  </button>
                </div>
              )}
            </div>

            {activeForm ? (
              <div>
                <form onSubmit={handleAddQuestion} style={{ display: "flex", gap: "0.5rem", marginBottom: "1.5rem" }}>
                  <input
                    type="text"
                    placeholder="Enter question prompt..."
                    value={questionText}
                    onChange={(e) => setQuestionText(e.target.value)}
                    required
                    style={{ flex: 2, padding: "0.6rem", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                  />
                  <select
                    value={questionType}
                    onChange={(e) => setQuestionType(e.target.value)}
                    style={{ flex: 1, padding: "0.6rem", borderRadius: "6px", border: "1px solid #cbd5e1" }}
                  >
                    <option value="RATING">⭐ Rating (1-5)</option>
                    <option value="RADIO">🔘 Single Choice</option>
                    <option value="CHECKBOX">☑️ Multi Select</option>
                    <option value="TEXT">📝 Open Text</option>
                  </select>
                  <button type="submit" style={{ backgroundColor: "#2563eb", color: "#fff", border: "none", padding: "0.6rem 1rem", borderRadius: "6px", fontWeight: "600", cursor: "pointer" }}>
                    Add
                  </button>
                </form>

                {/* Added Questions List */}
                <div style={{ display: "flex", flexDirection: "column", gap: "0.75rem" }}>
                  {questions.map((q, index) => (
                    <div key={q.id || index} style={{ padding: "0.85rem", backgroundColor: "#f8fafc", borderRadius: "6px", border: "1px solid #e2e8f0", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                      <span><strong>{index + 1}.</strong> {q.questionText}</span>
                      <span style={{ fontSize: "0.75rem", backgroundColor: "#e2e8f0", padding: "0.25rem 0.5rem", borderRadius: "4px", fontWeight: "600" }}>{q.questionType}</span>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div style={{ textAlign: "center", padding: "3rem", color: "#94a3b8" }}>Create a draft form on the left to start adding questions.</div>
            )}
          </div>
        </div>

        {/* Target Audience Modal */}
        {showTargetModal && (
          <div style={{ position: "fixed", top: 0, left: 0, right: 0, bottom: 0, backgroundColor: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center" }}>
            <div style={{ backgroundColor: "#fff", padding: "2rem", borderRadius: "12px", width: "400px" }}>
              <h3 style={{ marginBottom: "1rem" }}>Target Form Audience</h3>
              <form onSubmit={handleAssignForm} style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                <div>
                  <label>Department Code</label>
                  <input type="text" value={departmentCode} onChange={(e) => setDepartmentCode(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} required />
                </div>
                <div>
                  <label>Semester</label>
                  <input type="number" value={semester} onChange={(e) => setSemester(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} required />
                </div>
                <div>
                  <label>Section</label>
                  <input type="text" value={section} onChange={(e) => setSection(e.target.value)} style={{ width: "100%", padding: "0.5rem" }} required />
                </div>
                <div style={{ display: "flex", gap: "0.5rem", marginTop: "1rem" }}>
                  <button type="submit" style={{ flex: 1, backgroundColor: "#16a34a", color: "#fff", padding: "0.6rem", border: "none", borderRadius: "6px" }}>Confirm Assignment</button>
                  <button type="button" onClick={() => setShowTargetModal(false)} style={{ backgroundColor: "#94a3b8", color: "#fff", padding: "0.6rem", border: "none", borderRadius: "6px" }}>Cancel</button>
                </div>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default FormBuilderStudio;
