import React from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import DashboardCards from "../components/DashboardCards";
import FeedbackTable from "../components/FeedbackTable";
import "../css/AdminDashboard.css";

function AdminDashboard() {
  return (
    <div className="dashboard-container">
      <Sidebar />

      <div className="main-content">
        <Navbar />

        <div className="dashboard-header">
          <h1>Welcome to Admin Dashboard 👋</h1>
          <p>
            Manage feedback, users, reports, and monitor your system
            efficiently.
          </p>
        </div>

        <DashboardCards />

        <FeedbackTable />
      </div>
    </div>
  );
}

export default AdminDashboard;