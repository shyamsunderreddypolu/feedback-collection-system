import React, { useContext } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  FaHome,
  FaCommentDots,
  FaUsers,
  FaChartBar,
  FaCog,
  FaSignOutAlt,
  FaWpforms,
  FaGraduationCap,
  FaChalkboardTeacher
} from "react-icons/fa";
import { AuthContext } from "../context/AuthContext";
import "../css/Sidebar.css";

function Sidebar() {
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useContext(AuthContext);

  const handleLogout = () => {
    if (window.confirm("Are you sure you want to logout?")) {
      logout();
      navigate("/");
    }
  };

  const role = user?.role || "ROLE_ADMIN";

  return (
    <div className="sidebar">
      <div className="logo">
        <h2>Feedback System</h2>
        <p>{role.replace("ROLE_", "")} Portal</p>
      </div>

      <ul className="menu">
        {/* Admin Navigation */}
        {role === "ROLE_ADMIN" && (
          <>
            <li className={location.pathname === "/dashboard" ? "active" : ""}>
              <Link to="/dashboard">
                <FaHome />
                <span>Dashboard</span>
              </Link>
            </li>
            <li className={location.pathname === "/form-builder" ? "active" : ""}>
              <Link to="/form-builder">
                <FaWpforms />
                <span>Form Builder</span>
              </Link>
            </li>
            <li className={location.pathname === "/users" ? "active" : ""}>
              <Link to="/users">
                <FaUsers />
                <span>User Mgmt</span>
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
          </>
        )}

        {/* Student Navigation */}
        {role === "ROLE_STUDENT" && (
          <>
            <li className={location.pathname === "/student-surveys" || location.pathname === "/dashboard" ? "active" : ""}>
              <Link to="/student-surveys">
                <FaGraduationCap />
                <span>My Surveys</span>
              </Link>
            </li>
          </>
        )}

        {/* Faculty Navigation */}
        {role === "ROLE_FACULTY" && (
          <>
            <li className={location.pathname === "/faculty-analytics" || location.pathname === "/dashboard" ? "active" : ""}>
              <Link to="/faculty-analytics">
                <FaChalkboardTeacher />
                <span>Course Analytics</span>
              </Link>
            </li>
          </>
        )}

        <li className="logout-item" onClick={handleLogout} style={{ cursor: "pointer", marginTop: "auto" }}>
          <FaSignOutAlt />
          <span>Logout</span>
        </li>
      </ul>
    </div>
  );
}

export default Sidebar;