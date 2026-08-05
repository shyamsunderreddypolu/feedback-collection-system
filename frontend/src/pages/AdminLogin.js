import React, { useState, useContext } from "react";
import { FaLock } from "react-icons/fa";
import { MdEmail } from "react-icons/md";
import { IoEyeOutline, IoEyeOffOutline } from "react-icons/io5";
import { useNavigate } from "react-router-dom";
import { AuthContext } from "../context/AuthContext";
import API from "../api/axiosInstance";

import "../css/AdminLogin.css";
import loginImage from "../assets/images/login.png";

function AdminLogin() {
  const navigate = useNavigate();
  const { login } = useContext(AuthContext);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!email || !password) {
      setError("Please fill in all fields.");
      return;
    }

    try {
      setLoading(true);
      setError("");
      const response = await API.post("/auth/login", { email, password });
      
      const { token, id, name, userEmail, role } = response.data;
      login({ id, name, email: userEmail, role }, token);
      
      navigate("/dashboard");
    } catch (err) {
      setError(err.response?.data?.message || "Invalid credentials. Please try again.");
    } finally {
      setLoading(false);
    }
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

          {error && <div className="error-message" style={{ color: "#ef4444", marginBottom: "1rem", fontSize: "0.9rem", textAlign: "center" }}>{error}</div>}

          <form className="login-form" onSubmit={handleLogin}>

            {/* Email */}
            <div className="input-group">
              <label>Email</label>

              <div className="input-box">
                <MdEmail className="input-icon" />

                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="Enter admin email"
                  required
                />
              </div>
            </div>

            {/* Password */}
            <div className="input-group">
              <label>Password</label>

              <div className="input-box">
                <FaLock className="input-icon" />

                <input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter Password"
                  required
                />

                <span onClick={() => setShowPassword(!showPassword)} style={{ cursor: "pointer" }}>
                  {showPassword ? <IoEyeOffOutline className="eye-icon" /> : <IoEyeOutline className="eye-icon" />}
                </span>
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
              type="submit"
              className="login-btn"
              disabled={loading}
            >
              {loading ? "Signing In..." : "Sign In"}
            </button>

          </form>
        </div>
      </div>
    </div>
  );
}

export default AdminLogin;