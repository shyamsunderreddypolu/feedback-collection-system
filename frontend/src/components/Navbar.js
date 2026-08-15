import React, { useContext } from "react";
import "../css/Navbar.css";
import { AuthContext } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";
import { FaSearch, FaBell, FaUserCircle, FaSignOutAlt } from "react-icons/fa";

function Navbar() {
  const { user, logout } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  const getRoleDisplayName = (role) => {
    if (!role) return "User";
    if (role === "ROLE_ADMIN") return "Administrator";
    if (role === "ROLE_FACULTY") return "Faculty Member";
    if (role === "ROLE_STUDENT") return "Student";
    return role.replace("ROLE_", "");
  };

  return (
    <div className="navbar">
      <div className="search-box">
        <FaSearch className="search-icon" />
        <input type="text" placeholder="Search forms, courses..." />
      </div>

      <div className="navbar-right">
        <div className="notification" title="Notifications">
          <FaBell />
          <span className="badge">1</span>
        </div>

        <div className="profile">
          <FaUserCircle className="profile-icon" />
          <div className="profile-info">
            <h4>{user?.name || user?.email || "User"}</h4>
            <p>{getRoleDisplayName(user?.role)}</p>
          </div>
        </div>

        <button className="logout-btn" onClick={handleLogout} title="Sign Out" style={{
          background: "transparent",
          border: "none",
          color: "#94a3b8",
          cursor: "pointer",
          fontSize: "1.2rem",
          marginLeft: "1rem",
          display: "flex",
          alignItems: "center"
        }}>
          <FaSignOutAlt />
        </button>
      </div>
    </div>
  );
}

export default Navbar;