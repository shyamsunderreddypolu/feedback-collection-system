import React, { useState, useEffect, useCallback } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import API from "../api/axiosInstance";
import "../css/Users.css";
import { FaTrash, FaFilter } from "react-icons/fa";

function Users() {
  const [users, setUsers] = useState([]);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("ALL");
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState({ type: "", text: "" });

  const fetchUsers = useCallback(async () => {
    try {
      setLoading(true);
      const url = roleFilter === "ALL" ? "/users/active" : `/users/role/${roleFilter}`;
      const response = await API.get(url);
      setUsers(response.data || []);
    } catch (err) {
      console.error("Error fetching users", err);
      setMessage({ type: "error", text: "Failed to fetch user directory." });
    } finally {
      setLoading(false);
    }
  }, [roleFilter]);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  const deleteUser = async (id, name) => {
    if (!window.confirm(`Are you sure you want to deactivate ${name}?`)) return;

    try {
      await API.delete(`/users/${id}`);
      setMessage({ type: "success", text: `User "${name}" deactivated successfully.` });
      fetchUsers();
    } catch (err) {
      console.error("Error deactivating user", err);
      setMessage({ type: "error", text: "Failed to deactivate user." });
    }
  };

  const filteredUsers = users.filter((u) =>
    (u.name || "").toLowerCase().includes(search.toLowerCase()) ||
    (u.email || "").toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="dashboard-container" style={{ display: "flex", minHeight: "100vh", backgroundColor: "#f8fafc" }}>
      <Sidebar />
      <div className="main-content" style={{ flex: 1, padding: "2rem" }}>
        <Navbar />

        <div className="users-page">
          <div className="users-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "1.5rem" }}>
            <div>
              <h1 style={{ color: "#0f172a", fontSize: "1.8rem", fontWeight: "700" }}>👥 User Management Directory</h1>
              <p style={{ color: "#64748b" }}>Manage student, faculty, and administrator accounts across all departments.</p>
            </div>

            <div style={{ display: "flex", gap: "0.75rem", alignItems: "center" }}>
              <div style={{ display: "flex", alignItems: "center", gap: "0.5rem", backgroundColor: "#ffffff", padding: "0.5rem 1rem", borderRadius: "8px", border: "1px solid #e2e8f0" }}>
                <FaFilter style={{ color: "#64748b" }} />
                <select
                  value={roleFilter}
                  onChange={(e) => setRoleFilter(e.target.value)}
                  style={{ border: "none", background: "none", color: "#334155", fontWeight: "600", cursor: "pointer" }}
                >
                  <option value="ALL">All Roles</option>
                  <option value="ROLE_STUDENT">Students Only</option>
                  <option value="ROLE_FACULTY">Faculty Only</option>
                  <option value="ROLE_ADMIN">Admins Only</option>
                </select>
              </div>
            </div>
          </div>

          {message.text && (
            <div style={{
              padding: "1rem",
              borderRadius: "8px",
              marginBottom: "1.5rem",
              backgroundColor: message.type === "success" ? "#f0fdf4" : "#fef2f2",
              color: message.type === "success" ? "#166534" : "#991b1b",
              border: `1px solid ${message.type === "success" ? "#bbf7d0" : "#fecaca"}`
            }}>
              {message.text}
            </div>
          )}

          <input
            type="text"
            placeholder="Search user by name or email..."
            className="search-user"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            style={{ width: "100%", padding: "0.75rem 1rem", borderRadius: "8px", border: "1px solid #cbd5e1", marginBottom: "1.5rem", fontSize: "0.95rem" }}
          />

          {loading ? (
            <div style={{ textAlign: "center", padding: "3rem" }}>Loading active users...</div>
          ) : (
            <table className="user-table" style={{ width: "100%", backgroundColor: "#ffffff", borderRadius: "12px", boxShadow: "0 1px 3px rgba(0,0,0,0.1)", borderCollapse: "collapse" }}>
              <thead>
                <tr style={{ backgroundColor: "#f1f5f9", textAlign: "left" }}>
                  <th style={{ padding: "1rem" }}>ID</th>
                  <th style={{ padding: "1rem" }}>Name</th>
                  <th style={{ padding: "1rem" }}>Email</th>
                  <th style={{ padding: "1rem" }}>Department</th>
                  <th style={{ padding: "1rem" }}>Role</th>
                  <th style={{ padding: "1rem" }}>Action</th>
                </tr>
              </thead>

              <tbody>
                {filteredUsers.map((user) => (
                  <tr key={user.id} style={{ borderBottom: "1px solid #f1f5f9" }}>
                    <td style={{ padding: "1rem" }}>#{user.id}</td>
                    <td style={{ padding: "1rem", fontWeight: "600", color: "#0f172a" }}>{user.name}</td>
                    <td style={{ padding: "1rem", color: "#475569" }}>{user.email}</td>
                    <td style={{ padding: "1rem", color: "#475569" }}>{user.departmentCode || user.departmentName || "N/A"}</td>
                    <td style={{ padding: "1rem" }}>
                      <span style={{
                        padding: "0.25rem 0.6rem",
                        borderRadius: "20px",
                        fontSize: "0.75rem",
                        fontWeight: "700",
                        backgroundColor: user.roleName === "ROLE_ADMIN" ? "#fef3c7" : user.roleName === "ROLE_FACULTY" ? "#dbeafe" : "#dcfce7",
                        color: user.roleName === "ROLE_ADMIN" ? "#92400e" : user.roleName === "ROLE_FACULTY" ? "#1e40af" : "#166534"
                      }}>
                        {user.roleName ? user.roleName.replace("ROLE_", "") : "USER"}
                      </span>
                    </td>
                    <td style={{ padding: "1rem" }}>
                      <button
                        className="delete-btn"
                        onClick={() => deleteUser(user.id, user.name)}
                        title="Deactivate Account"
                        style={{ backgroundColor: "#ef4444", color: "#fff", border: "none", padding: "0.5rem 0.75rem", borderRadius: "6px", cursor: "pointer" }}
                      >
                        <FaTrash /> Deactivate
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </div>
  );
}

export default Users;