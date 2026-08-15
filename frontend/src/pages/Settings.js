import React, { useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import "../css/Settings.css";

import {
  FaUser,
  FaLock,
  FaBell,
  FaPalette,
  FaSave,
} from "react-icons/fa";

function Settings() {
  const [profile, setProfile] = useState({
    name: "Mohana",
    email: "mohana@gmail.com",
    phone: "9876543210",
  });

  const [password, setPassword] = useState({
    current: "",
    newPass: "",
    confirm: "",
  });

  const [notifications, setNotifications] = useState({
    email: true,
    sms: false,
  });

  const [theme, setTheme] = useState("light");

  const handleSave = () => {
    alert("Settings saved successfully!");
  };

  return (
    <div className="dashboard-container">
      <Sidebar />

      <div className="main-content">
        <Navbar />

        <div className="settings-page">

          <h1>Settings</h1>
          <p>Manage your account settings.</p>

          {/* Profile */}

          <div className="settings-card">

            <h2>
              <FaUser /> Profile
            </h2>

            <input
              type="text"
              placeholder="Name"
              value={profile.name}
              onChange={(e) =>
                setProfile({ ...profile, name: e.target.value })
              }
            />

            <input
              type="email"
              placeholder="Email"
              value={profile.email}
              onChange={(e) =>
                setProfile({ ...profile, email: e.target.value })
              }
            />

            <input
              type="text"
              placeholder="Phone"
              value={profile.phone}
              onChange={(e) =>
                setProfile({ ...profile, phone: e.target.value })
              }
            />

          </div>

          {/* Password */}

          <div className="settings-card">

            <h2>
              <FaLock /> Change Password
            </h2>

            <input
              type="password"
              placeholder="Current Password"
              value={password.current}
              onChange={(e) =>
                setPassword({ ...password, current: e.target.value })
              }
            />

            <input
              type="password"
              placeholder="New Password"
              value={password.newPass}
              onChange={(e) =>
                setPassword({ ...password, newPass: e.target.value })
              }
            />

            <input
              type="password"
              placeholder="Confirm Password"
              value={password.confirm}
              onChange={(e) =>
                setPassword({ ...password, confirm: e.target.value })
              }
            />

          </div>

          {/* Notifications */}

          <div className="settings-card">

            <h2>
              <FaBell /> Notifications
            </h2>

            <label>
              <input
                type="checkbox"
                checked={notifications.email}
                onChange={() =>
                  setNotifications({
                    ...notifications,
                    email: !notifications.email,
                  })
                }
              />
              Email Notifications
            </label>

            <label>
              <input
                type="checkbox"
                checked={notifications.sms}
                onChange={() =>
                  setNotifications({
                    ...notifications,
                    sms: !notifications.sms,
                  })
                }
              />
              SMS Notifications
            </label>

          </div>

          {/* Theme */}

          <div className="settings-card">

            <h2>
              <FaPalette /> Theme
            </h2>

            <label>
              <input
                type="radio"
                name="theme"
                value="light"
                checked={theme === "light"}
                onChange={(e) => setTheme(e.target.value)}
              />
              Light Mode
            </label>

            <label>
              <input
                type="radio"
                name="theme"
                value="dark"
                checked={theme === "dark"}
                onChange={(e) => setTheme(e.target.value)}
              />
              Dark Mode
            </label>

          </div>

          <button
            className="save-settings-btn"
            onClick={handleSave}
          >
            <FaSave />
            Save Changes
          </button>

        </div>
      </div>
    </div>
  );
}

export default Settings;