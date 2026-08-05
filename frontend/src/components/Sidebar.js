import React from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  FaHome,
  FaCommentDots,
  FaUsers,
  FaChartBar,
  FaCog,
  FaSignOutAlt,
} from "react-icons/fa";

import "../css/Sidebar.css";

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    const confirmLogout = window.confirm(
      "Are you sure you want to logout?"
    );

    if (confirmLogout) {
      navigate("/");
    }
  };

  return (
    <div className="sidebar">

      <div className="logo">
        <h2>Feedback Collection</h2>
        <p>Admin Panel</p>
      </div>

      <ul className="menu">

        <li className={location.pathname === "/dashboard" ? "active" : ""}>
          <Link to="/dashboard">
            <FaHome />
            <span>Dashboard</span>
          </Link>
        </li>

        <li className={location.pathname === "/dashboard" ? "active" : ""}>
          <Link to="/dashboard">
            <FaCommentDots />
            <span>Feedback</span>
          </Link>
        </li>

        <li className={location.pathname === "/users" ? "active" : ""}>
          <Link to="/users">
            <FaUsers />
            <span>Users</span>
          </Link>
        </li>

        <li className={location.pathname === "/reports" ? "active" : ""}>
          <Link to="/reports">
            <FaChartBar />
            <span>Reports</span>
          </Link>
        </li>

        <li className={location.pathname === "/settings" ? "active" : ""}>
          <Link to="/settings">
            <FaCog />
            <span>Settings</span>
          </Link>
        </li>

        <li className="logout-item" onClick={handleLogout}>
          <FaSignOutAlt />
          <span>Logout</span>
        </li>

      </ul>

    </div>
  );
}

export default Sidebar;