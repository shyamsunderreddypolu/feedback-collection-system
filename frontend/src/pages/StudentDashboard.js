import React, { useState, useEffect } from "react";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import API from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";
import { FaClipboardList, FaClock, FaCheckCircle } from "react-icons/fa";
import "../css/AdminDashboard.css";

function StudentDashboard() {
  const [surveys, setSurveys] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchStudentSurveys();
  }, []);

  const fetchStudentSurveys = async () => {
    try {
      setLoading(true);
      // Fetch active forms from backend API
      const response = await API.get("/forms/active");
      setSurveys(response.data || []);
    } catch (err) {
      console.error("Failed to load active surveys", err);
      setError("Unable to load active surveys. Please try again later.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-container" style={{ display: "flex", minHeight: "100vh", backgroundColor: "#f8fafc" }}>
      <Sidebar />
      <div className="main-content" style={{ flex: 1, padding: "2rem" }}>
        <Navbar />

        <div className="page-header" style={{ marginBottom: "2rem" }}>
          <h2 style={{ color: "#0f172a", fontSize: "1.8rem", fontWeight: "700" }}>👨‍🎓 My Active Feedback Surveys</h2>
          <p style={{ color: "#64748b" }}>Select a feedback survey below to submit your anonymous ratings and comments.</p>
        </div>

        {loading ? (
          <div style={{ textAlign: "center", padding: "3rem", color: "#64748b" }}>Loading assigned surveys...</div>
        ) : error ? (
          <div style={{ padding: "1rem", backgroundColor: "#fef2f2", color: "#991b1b", borderRadius: "8px" }}>{error}</div>
        ) : surveys.length === 0 ? (
          <div style={{ textAlign: "center", padding: "4rem 2rem", backgroundColor: "#ffffff", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)" }}>
            <FaCheckCircle style={{ fontSize: "3rem", color: "#22c55e", marginBottom: "1rem" }} />
            <h3 style={{ color: "#1e293b" }}>You're all caught up!</h3>
            <p style={{ color: "#64748b" }}>There are currently no pending feedback surveys assigned to your cohort.</p>
          </div>
        ) : (
          <div className="survey-grid" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(320px, 1fr))", gap: "1.5rem" }}>
            {surveys.map((survey) => (
              <div key={survey.id} className="survey-card" style={{
                backgroundColor: "#ffffff",
                padding: "1.5rem",
                borderRadius: "12px",
                boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.1)",
                border: "1px solid #e2e8f0",
                display: "flex",
                flexDirection: "column",
                justifyContent: "space-between"
              }}>
                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "#2563eb", marginBottom: "0.5rem" }}>
                    <FaClipboardList />
                    <span style={{ fontSize: "0.85rem", fontWeight: "600", textTransform: "uppercase" }}>{survey.status || "ACTIVE"}</span>
                  </div>
                  <h3 style={{ color: "#0f172a", fontSize: "1.2rem", fontWeight: "600", marginBottom: "0.5rem" }}>{survey.title}</h3>
                  <p style={{ color: "#64748b", fontSize: "0.9rem", marginBottom: "1rem", lineHeight: "1.4" }}>
                    {survey.description || "End-semester feedback evaluation for course instructors."}
                  </p>
                </div>

                <div>
                  <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", color: "#64748b", fontSize: "0.85rem", marginBottom: "1rem" }}>
                    <FaClock />
                    <span>Ends: {survey.endDate ? new Date(survey.endDate).toLocaleDateString() : "Active Now"}</span>
                  </div>
                  <button
                    onClick={() => navigate(`/take-survey/${survey.id}`)}
                    style={{
                      width: "100%",
                      backgroundColor: "#2563eb",
                      color: "#ffffff",
                      border: "none",
                      padding: "0.75rem",
                      borderRadius: "8px",
                      fontWeight: "600",
                      cursor: "pointer",
                      transition: "background 0.2s"
                    }}
                  >
                    Start Feedback Survey →
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

export default StudentDashboard;
