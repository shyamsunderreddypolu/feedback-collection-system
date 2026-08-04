import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import "../css/FeedbackDetails.css";

function FeedbackDetails() {

  const location = useLocation();
  const navigate = useNavigate();

  const feedback = location.state;

  return (
    <div className="feedback-page">

      <div className="feedback-card">

        <h1>Feedback Details</h1>

        <div className="detail">
          <strong>Name :</strong> {feedback.name}
        </div>

        <div className="detail">
          <strong>Email :</strong> {feedback.email}
        </div>

        <div className="detail">
          <strong>Category :</strong> {feedback.category}
        </div>

        <div className="detail">
          <strong>Rating :</strong> {feedback.rating}
        </div>

        <div className="detail">
          <strong>Status :</strong> {feedback.status}
        </div>

        <div className="detail">
          <strong>Date :</strong> {feedback.date}
        </div>

        <div className="message-box">
          <h3>Feedback Message</h3>

          <p>{feedback.message}</p>
        </div>

        <button
          className="back-btn"
          onClick={() => navigate("/dashboard")}
        >
          Back to Dashboard
        </button>

      </div>

    </div>
  );
}

export default FeedbackDetails;