import React from "react";
import { Link } from "react-router-dom";
import { FaEye } from "react-icons/fa";
import "../css/FeedbackTable.css";
import feedbackData from "../data/feedbackData";

function FeedbackTable() {
  return (
    <div className="table-container">
      <h2>Recent Feedback</h2>

      <table className="feedback-table">
        <thead>
          <tr>
            <th>User</th>
            <th>Email</th>
            <th>Rating</th>
            <th>Category</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {feedbackData.map((feedback) => (
            <tr key={feedback.id}>
              <td>{feedback.name}</td>
              <td>{feedback.email}</td>
              <td>{feedback.rating}</td>
              <td>{feedback.category}</td>

              <td>
                <span
                  className={
                    feedback.status === "Positive"
                      ? "status positive"
                      : "status negative"
                  }
                >
                  {feedback.status}
                </span>
              </td>

              <td>
                <Link
                  to="/feedback-details"
                  state={feedback}
                  className="view-btn"
                >
                  <FaEye /> View
                </Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export default FeedbackTable;