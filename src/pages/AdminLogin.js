import React from "react";
import { FaLock } from "react-icons/fa";
import { MdEmail } from "react-icons/md";
import { IoEyeOutline } from "react-icons/io5";
import { useNavigate } from "react-router-dom";

import "../css/AdminLogin.css";
import loginImage from "../assets/images/login.png";

function AdminLogin() {
  const navigate = useNavigate();

  const handleLogin = () => {
    // Later we will validate email & password here
    navigate("/dashboard");
  };

  return (
    <div className="login-container">
      {/* Left Section */}
      <div className="left-sec">
        <div className="left-content">
          <img
            src={loginImage}
            alt="Login Illustration"
            className="login-image"
          />

          <h1>Feedback Collection System</h1>

          <p>
            Collect Feedback.
            <br />
            Analyze Insights.
            <br />
            Improve Experiences.
          </p>
        </div>
      </div>

      {/* Right Section */}
      <div className="right-sec">
        <div className="login-card">

          <div className="login-head">
            <div className="logo-circle">
              <FaLock />
            </div>

            <h2>Admin Login</h2>

            <p>Welcome back! Please Login to continue</p>
          </div>

          <div className="login-form">

            {/* Email */}
            <div className="input-group">
              <label>Email</label>

              <div className="input-box">
                <MdEmail className="input-icon" />

                <input
                  type="email"
                  placeholder="Enter admin email"
                />
              </div>
            </div>

            {/* Password */}
            <div className="input-group">
              <label>Password</label>

              <div className="input-box">
                <FaLock className="input-icon" />

                <input
                  type="password"
                  placeholder="Enter Password"
                />

                <IoEyeOutline className="eye-icon" />
              </div>
            </div>

            {/* Remember */}
            <div className="remember-forgot">
              <label className="remember">
                <input type="checkbox" />
                <span>Remember Me</span>
              </label>

              <a href="/">Forgot Password?</a>
            </div>

            {/* Login Button */}
            <button
              className="login-btn"
              onClick={handleLogin}
            >
              Sign In
            </button>

          </div>
        </div>
      </div>
    </div>
  );
}

export default AdminLogin;