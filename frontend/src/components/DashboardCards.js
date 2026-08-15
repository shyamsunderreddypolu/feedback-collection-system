import React from "react";
import "../css/DashboardCards.css";

import {
  FaClipboardList,
  FaUsers,
  FaSmile,
  FaFrown,
} from "react-icons/fa";

function DashboardCards() {
  return (
    <div className="cards-container">

      <div className="card">
        <div className="card-icon">
          <FaClipboardList />
        </div>

        <div className="card-info">
          <h3>Total Feedback</h3>
          <h2>120</h2>
        </div>
      </div>

      <div className="card">
        <div className="card-icon">
          <FaUsers />
        </div>

        <div className="card-info">
          <h3>Total Users</h3>
          <h2>75</h2>
        </div>
      </div>

      <div className="card">
        <div className="card-icon">
          <FaSmile />
        </div>

        <div className="card-info">
          <h3>Positive</h3>
          <h2>90</h2>
        </div>
      </div>

      <div className="card">
        <div className="card-icon">
          <FaFrown />
        </div>

        <div className="card-info">
          <h3>Negative</h3>
          <h2>30</h2>
        </div>
      </div>

    </div>
  );
}

export default DashboardCards;