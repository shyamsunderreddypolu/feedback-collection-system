import React from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import "../css/Reports.css";

import {
  FaCommentDots,
  FaSmile,
  FaFrown,
  FaStar,
  FaFilePdf,
  FaFileExcel,
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

  const data = {
    labels: ["Jan", "Feb", "Mar", "Apr", "May", "Jun"],
    datasets: [
      {
        label: "Monthly Feedback",
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

  return (
    <div className="dashboard-container">

      <Sidebar />

      <div className="main-content">

        <Navbar />

        <div className="reports-page">

          <h1>Reports Dashboard</h1>

          <p>View feedback statistics and export reports.</p>

          <div className="report-cards">

            <div className="report-card">
              <FaCommentDots className="card-icon" />
              <h2>120</h2>
              <p>Total Feedback</p>
            </div>

            <div className="report-card">
              <FaSmile className="card-icon" />
              <h2>90</h2>
              <p>Positive</p>
            </div>

            <div className="report-card">
              <FaFrown className="card-icon" />
              <h2>30</h2>
              <p>Negative</p>
            </div>

            <div className="report-card">
              <FaStar className="card-icon" />
              <h2>4.5</h2>
              <p>Average Rating</p>
            </div>

          </div>

          <div className="chart-box">

            <h2>Monthly Feedback</h2>

            <Bar data={data} options={options} />

          </div>

          <div className="export-buttons">

            <button className="pdf-btn">
              <FaFilePdf />
              Export PDF
            </button>

            <button className="excel-btn">
              <FaFileExcel />
              Export Excel
            </button>

          </div>

        </div>

      </div>

    </div>
  );
}

export default Reports;