import React, { useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import API from "../api/axiosInstance";
import "../css/Reports.css";
import {
  FaCommentDots,
  FaSmile,
  FaFrown,
  FaStar,
  FaFilePdf,
  FaFileCsv,
} from "react-icons/fa";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar } from "react-chartjs-2";

ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

function Reports() {
  const [downloading, setDownloading] = useState(false);
  const [message, setMessage] = useState({ type: "", text: "" });

  const data = {
    labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
    datasets: [
      {
        label: "Monthly Submissions",
        data: [25, 40, 30, 55, 60, 45],
        backgroundColor: "#2563eb",
        borderRadius: 8,
      },
    ],
  };

  const options = {
    responsive: true,
    plugins: {
      legend: {
        position: "top",
      },
    },
  };

  const handleExportPDF = async () => {
    try {
      setDownloading(true);
      setMessage({ type: "info", text: "Generating PDF report..." });
      
      const response = await API.get("/reports/export/pdf", {
        responseType: "blob",
      });

      const blob = new Blob([response.data], { type: "application/pdf" });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = `FBCS_Feedback_Report_${Date.now()}.pdf`;
      link.click();
      setMessage({ type: "success", text: "PDF Report downloaded successfully!" });
    } catch (err) {
      console.error("PDF Export error", err);
      setMessage({ type: "error", text: "Failed to download PDF report." });
    } finally {
      setDownloading(false);
    }
  };

  const handleExportCSV = async () => {
    try {
      setDownloading(true);
      setMessage({ type: "info", text: "Exporting CSV spreadsheet..." });

      const response = await API.get("/reports/export/csv", {
        responseType: "blob",
      });

      const blob = new Blob([response.data], { type: "text/csv" });
      const link = document.createElement("a");
      link.href = window.URL.createObjectURL(blob);
      link.download = `FBCS_Feedback_Data_${Date.now()}.csv`;
      link.click();
      setMessage({ type: "success", text: "CSV Spreadsheet downloaded successfully!" });
    } catch (err) {
      console.error("CSV Export error", err);
      setMessage({ type: "error", text: "Failed to download CSV spreadsheet." });
    } finally {
      setDownloading(false);
    }
  };

  return (
    <div className="dashboard-container" style={{ display: "flex", minHeight: "100vh", backgroundColor: "#f8fafc" }}>
      <Sidebar />

      <div className="main-content" style={{ flex: 1, padding: "2rem" }}>
        <Navbar />

        <div className="reports-page">
          <h1 style={{ color: "#0f172a", fontSize: "1.8rem", fontWeight: "700" }}>📊 Reports & Analytics Studio</h1>
          <p style={{ color: "#64748b", marginBottom: "1.5rem" }}>View feedback statistics and export PDF / CSV reports.</p>

          {message.text && (
            <div style={{
              padding: "1rem",
              borderRadius: "8px",
              marginBottom: "1.5rem",
              backgroundColor: message.type === "success" ? "#f0fdf4" : message.type === "info" ? "#f0f9ff" : "#fef2f2",
              color: message.type === "success" ? "#166534" : message.type === "info" ? "#0369a1" : "#991b1b",
              border: `1px solid ${message.type === "success" ? "#bbf7d0" : message.type === "info" ? "#bae6fd" : "#fecaca"}`
            }}>
              {message.text}
            </div>
          )}

          <div className="report-cards" style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))", gap: "1.5rem", marginBottom: "2rem" }}>
            <div className="report-card" style={{ backgroundColor: "#fff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
              <FaCommentDots className="card-icon" style={{ color: "#2563eb", fontSize: "1.8rem", marginBottom: "0.5rem" }} />
              <h2 style={{ fontSize: "1.8rem", color: "#0f172a" }}>120</h2>
              <p style={{ color: "#64748b" }}>Total Submissions</p>
            </div>

            <div className="report-card" style={{ backgroundColor: "#fff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
              <FaSmile className="card-icon" style={{ color: "#16a34a", fontSize: "1.8rem", marginBottom: "0.5rem" }} />
              <h2 style={{ fontSize: "1.8rem", color: "#0f172a" }}>90</h2>
              <p style={{ color: "#64748b" }}>Positive Ratings</p>
            </div>

            <div className="report-card" style={{ backgroundColor: "#fff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
              <FaFrown className="card-icon" style={{ color: "#ef4444", fontSize: "1.8rem", marginBottom: "0.5rem" }} />
              <h2 style={{ fontSize: "1.8rem", color: "#0f172a" }}>30</h2>
              <p style={{ color: "#64748b" }}>Constructive Remarks</p>
            </div>

            <div className="report-card" style={{ backgroundColor: "#fff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", border: "1px solid #e2e8f0" }}>
              <FaStar className="card-icon" style={{ color: "#f59e0b", fontSize: "1.8rem", marginBottom: "0.5rem" }} />
              <h2 style={{ fontSize: "1.8rem", color: "#0f172a" }}>4.65 / 5.0</h2>
              <p style={{ color: "#64748b" }}>Average Rating</p>
            </div>
          </div>

          <div className="chart-box" style={{ backgroundColor: "#fff", padding: "1.5rem", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", marginBottom: "2rem", border: "1px solid #e2e8f0" }}>
            <h2 style={{ fontSize: "1.2rem", color: "#0f172a", marginBottom: "1rem" }}>Monthly Feedback Submissions</h2>
            <Bar data={data} options={options} />
          </div>

          <div className="export-buttons" style={{ display: "flex", gap: "1rem" }}>
            <button
              className="pdf-btn"
              onClick={handleExportPDF}
              disabled={downloading}
              style={{ display: "flex", alignItems: "center", gap: "0.5rem", backgroundColor: "#dc2626", color: "#fff", border: "none", padding: "0.75rem 1.5rem", borderRadius: "8px", fontWeight: "600", cursor: "pointer" }}
            >
              <FaFilePdf /> {downloading ? "Downloading..." : "Download PDF Report"}
            </button>

            <button
              className="excel-btn"
              onClick={handleExportCSV}
              disabled={downloading}
              style={{ display: "flex", alignItems: "center", gap: "0.5rem", backgroundColor: "#16a34a", color: "#fff", border: "none", padding: "0.75rem 1.5rem", borderRadius: "8px", fontWeight: "600", cursor: "pointer" }}
            >
              <FaFileCsv /> {downloading ? "Exporting..." : "Export CSV Spreadsheet"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default Reports;