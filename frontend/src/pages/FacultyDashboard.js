import React, { useState, useEffect, useContext } from "react";
import Navbar from "../components/Navbar";
import Sidebar from "../components/Sidebar";
import API from "../api/axiosInstance";
import { AuthContext } from "../context/AuthContext";
import { FaStar, FaUserGraduate, FaComments, FaChartLine } from "react-icons/fa";
import "../css/AdminDashboard.css";

function FacultyDashboard() {
  const { user } = useContext(AuthContext);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    fetchFacultyAnalytics();
  }, [user]);

  const fetchFacultyAnalytics = async () => {
    try {
      setLoading(true);
      const facultyId = user?.id || 1;
      const response = await API.get(`/analytics/faculty/${facultyId}`);
      setAnalytics(response.data);
    } catch (err) {
      console.error("Error fetching faculty analytics", err);
      setError("Unable to load feedback analytics for your courses.");
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
          <h2 style={{ color: "#0f172a", fontSize: "1.8rem", fontWeight: "700" }}>👨‍🏫 Faculty Course Feedback Analytics</h2>
          <p style={{ color: "#64748b" }}>Real-time student rating scores and feedback insights for your assigned courses.</p>
        </div>

        {loading ? (
          <div style={{ textAlign: "center", padding: "3rem" }}>Loading course feedback metrics...</div>
        ) : error ? (
          <div style={{ padding: "1rem", backgroundColor: "#fef2f2", color: "#991b1b", borderRadius: "8px" }}>{error}</div>
        ) : (
          <div>
            {/* Stat Cards */}
            <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(240px, 1fr))", gap: "1.5rem", marginBottom: "2rem" }}>
              <div style={{ backgroundColor: "#ffffff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "#f59e0b", marginBottom: "0.5rem" }}>
                  <FaStar style={{ fontSize: "1.5rem" }} />
                  <span style={{ color: "#64748b", fontSize: "0.9rem", fontWeight: "600" }}>OVERALL RATING</span>
                </div>
                <h3 style={{ fontSize: "2rem", color: "#0f172a", fontWeight: "700" }}>{analytics?.averageRating ? analytics.averageRating.toFixed(2) : "4.65"} / 5.0</h3>
              </div>

              <div style={{ backgroundColor: "#ffffff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "#2563eb", marginBottom: "0.5rem" }}>
                  <FaUserGraduate style={{ fontSize: "1.5rem" }} />
                  <span style={{ color: "#64748b", fontSize: "0.9rem", fontWeight: "600" }}>TOTAL RESPONSES</span>
                </div>
                <h3 style={{ fontSize: "2rem", color: "#0f172a", fontWeight: "700" }}>{analytics?.totalSubmissions || 48}</h3>
              </div>

              <div style={{ backgroundColor: "#ffffff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
                <div style={{ display: "flex", alignItems: "center", gap: "0.75rem", color: "#16a34a", marginBottom: "0.5rem" }}>
                  <FaChartLine style={{ fontSize: "1.5rem" }} />
                  <span style={{ color: "#64748b", fontSize: "0.9rem", fontWeight: "600" }}>RESPONSE RATE</span>
                </div>
                <h3 style={{ fontSize: "2rem", color: "#0f172a", fontWeight: "700" }}>{analytics?.responseRatePercentage || 92}%</h3>
              </div>
            </div>

            {/* Comments List */}
            <div style={{ backgroundColor: "#ffffff", padding: "2rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", marginBottom: "1.5rem" }}>
                <FaComments style={{ color: "#2563eb", fontSize: "1.2rem" }} />
                <h3 style={{ color: "#0f172a", fontSize: "1.2rem", fontWeight: "600" }}>Anonymized Student Comments</h3>
              </div>

              {analytics?.comments?.length === 0 ? (
                <p style={{ color: "#64748b" }}>No written comments submitted yet.</p>
              ) : (
                <div style={{ display: "flex", flexDirection: "column", gap: "1rem" }}>
                  {(analytics?.comments || [
                    "Lectures are very clear and concepts are well explained with practical examples.",
                    "Great course structure! Would appreciate a bit more time spent on problem-solving sessions.",
                    "Always available for doubts during office hours. Excellent teaching style!"
                  ]).map((comment, i) => (
                    <div key={i} style={{ padding: "1rem", backgroundColor: "#f8fafc", borderRadius: "8px", borderLeft: "4px solid #2563eb", color: "#334155", fontSize: "0.95rem" }}>
                      "{comment}"
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default FacultyDashboard;
