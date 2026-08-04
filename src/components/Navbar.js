import React from "react";
import "../css/Navbar.css";

import {
  FaSearch,
  FaBell,
  FaUserCircle
} from "react-icons/fa";

function Navbar() {
  return (
    <div className="navbar">

      <div className="search-box">
        <FaSearch className="search-icon" />
        <input
          type="text"
          placeholder="Search..."
        />
      </div>

      <div className="navbar-right">

        <div className="notification">
          <FaBell />
          <span className="badge">3</span>
        </div>

        <div className="profile">
          <FaUserCircle className="profile-icon" />

          <div className="profile-info">
            <h4>Admin</h4>
            <p>Administrator</p>
          </div>

        </div>

      </div>

    </div>
  );
}

export default Navbar;