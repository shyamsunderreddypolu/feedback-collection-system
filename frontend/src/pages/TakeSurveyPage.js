import React, { useState, useEffect, useCallback } from "react";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import API from "../api/axiosInstance";
import { useParams, useNavigate } from "react-router-dom";
import { FaStar, FaPaperPlane, FaArrowLeft } from "react-icons/fa";

function TakeSurveyPage() {
  const { formId } = useParams();
  const navigate = useNavigate();

  const [form, setForm] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  const fetchSurveyDetails = useCallback(async () => {
    try {
      setLoading(true);
      const formRes = await API.get(`/forms/${formId}`);
      setForm(formRes.data);

      const qRes = await API.get(`/questions/form/${formId}`);
      setQuestions(qRes.data || []);
    } catch (err) {
      console.error("Error fetching survey details", err);
      setMessage({ type: "error", text: "Failed to load survey questions." });
    } finally {
      setLoading(false);
    }
  }, [formId]);

  useEffect(() => {
    fetchSurveyDetails();
  }, [fetchSurveyDetails]);

  const handleRatingChange = (questionId, rating) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: { questionId, ratingValue: rating }
    }));
  };

  const handleTextChange = (questionId, text) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: { questionId, textValue: text }
    }));
  };

  const handleChoiceChange = (questionId, optionId) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: { questionId, selectedOptionId: optionId }
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setMessage({ type: "", text: "" });

      // Transform answers state to array for API
      const answersList = Object.values(answers);

      await API.post("/submissions", {
        feedbackFormId: Number(formId),
        answers: answersList
      });

      setMessage({ type: "success", text: "Feedback submitted successfully! Thank you." });
      setTimeout(() => navigate("/student-surveys"), 2000);
    } catch (err) {
      console.error("Submission failed", err);
      setMessage({
        type: "error",
        text: err.response?.data?.message || "Failed to submit survey feedback. Please try again."
      });
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="dashboard-container" style={{ display: "flex", minHeight: "100vh", backgroundColor: "#f8fafc" }}>
      <Sidebar />
      <div className="main-content" style={{ flex: 1, padding: "2rem" }}>
        <Navbar />

        <button
          onClick={() => navigate("/student-surveys")}
          style={{
            display: "flex",
            alignItems: "center",
            gap: "0.5rem",
            background: "none",
            border: "none",
            color: "#64748b",
            cursor: "pointer",
            fontWeight: "600",
            marginBottom: "1.5rem"
          }}
        >
          <FaArrowLeft /> Back to Surveys
        </button>

        {loading ? (
          <div style={{ textAlign: "center", padding: "3rem" }}>Loading survey questions...</div>
        ) : (
          <div style={{ maxWidth: "800px", margin: "0 auto", backgroundColor: "#ffffff", padding: "2.5rem", borderRadius: "12px", boxShadow: "0 4px 6px -1px rgba(0,0,0,0.1)" }}>
            <h2 style={{ color: "#0f172a", fontSize: "1.8rem", marginBottom: "0.5rem" }}>{form?.title || "Feedback Evaluation"}</h2>
            <p style={{ color: "#64748b", marginBottom: "2rem", borderBottom: "1px solid #e2e8f0", paddingBottom: "1rem" }}>
              {form?.description || "Your answers are anonymized to ensure constructive and honest evaluation."}
            </p>

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

            <form onSubmit={handleSubmit}>
              {questions.map((q, idx) => (
                <div key={q.id} style={{ marginBottom: "2rem", padding: "1.5rem", border: "1px solid #f1f5f9", borderRadius: "8px", backgroundColor: "#fafafa" }}>
                  <h4 style={{ color: "#1e293b", fontSize: "1.1rem", marginBottom: "0.5rem" }}>
                    {idx + 1}. {q.questionText} {q.mandatory && <span style={{ color: "#ef4444" }}>*</span>}
                  </h4>

                  {/* Rating Question */}
                  {q.questionType === "RATING" && (
                    <div style={{ display: "flex", gap: "0.75rem", marginTop: "1rem" }}>
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          key={star}
                          type="button"
                          onClick={() => handleRatingChange(q.id, star)}
                          style={{
                            background: "none",
                            border: "none",
                            cursor: "pointer",
                            fontSize: "2rem",
                            color: star <= (answers[q.id]?.ratingValue || 0) ? "#f59e0b" : "#cbd5e1"
                          }}
                        >
                          <FaStar />
                        </button>
                      ))}
                    </div>
                  )}

                  {/* Single Choice / Radio */}
                  {(q.questionType === "RADIO" || q.questionType === "SINGLE_CHOICE") && (
                    <div style={{ marginTop: "1rem", display: "flex", flexDirection: "column", gap: "0.5rem" }}>
                      {q.options?.map((opt) => (
                        <label key={opt.id} style={{ display: "flex", alignItems: "center", gap: "0.5rem", cursor: "pointer" }}>
                          <input
                            type="radio"
                            name={`q_${q.id}`}
                            value={opt.id}
                            onChange={() => handleChoiceChange(q.id, opt.id)}
                            required={q.mandatory}
                          />
                          <span>{opt.optionText}</span>
                        </label>
                      ))}
                    </div>
                  )}

                  {/* Text Question */}
                  {(q.questionType === "TEXT" || q.questionType === "TEXTAREA") && (
                    <textarea
                      rows="4"
                      placeholder="Write your feedback here..."
                      onChange={(e) => handleTextChange(q.id, e.target.value)}
                      required={q.mandatory}
                      style={{
                        width: "100%",
                        marginTop: "1rem",
                        padding: "0.75rem",
                        borderRadius: "8px",
                        border: "1px solid #cbd5e1",
                        fontSize: "0.95rem"
                      }}
                    />
                  )}
                </div>
              ))}

              <button
                type="submit"
                disabled={submitting}
                style={{
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "0.5rem",
                  width: "100%",
                  backgroundColor: "#2563eb",
                  color: "#ffffff",
                  padding: "0.85rem",
                  borderRadius: "8px",
                  fontWeight: "600",
                  fontSize: "1rem",
                  border: "none",
                  cursor: "pointer"
                }}
              >
                <FaPaperPlane /> {submitting ? "Submitting Feedback..." : "Submit Anonymous Feedback"}
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  );
}

export default TakeSurveyPage;
