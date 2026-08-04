import React, { useState } from "react";
import Sidebar from "../components/Sidebar";
import Navbar from "../components/Navbar";
import "../css/Users.css";
import { FaPlus, FaEdit, FaTrash } from "react-icons/fa";

function Users() {

  const [users, setUsers] = useState([
    {
      id: 1,
      name: "Mohan",
      email: "mohan@gmail.com",
      role: "Admin",
    },
    {
      id: 2,
      name: "Rahul",
      email: "rahul@gmail.com",
      role: "User",
    },
    {
      id: 3,
      name: "Priya",
      email: "priya@gmail.com",
      role: "User",
    },
  ]);

  const [search, setSearch] = useState("");

  const [showModal, setShowModal] = useState(false);

  const [editingUser, setEditingUser] = useState(null);

  const [newUser, setNewUser] = useState({
    name: "",
    email: "",
    role: "User",
  });

  const deleteUser = (id) => {
    setUsers(users.filter((user) => user.id !== id));
  };

  const editUser = (user) => {
    setEditingUser(user);

    setNewUser({
      name: user.name,
      email: user.email,
      role: user.role,
    });

    setShowModal(true);
  };

  const saveUser = () => {

    if (newUser.name === "" || newUser.email === "") {
      alert("Please Fill All Fields");
      return;
    }

    if (editingUser) {

      const updatedUsers = users.map((user) =>
        user.id === editingUser.id
          ? {
              ...user,
              name: newUser.name,
              email: newUser.email,
              role: newUser.role,
            }
          : user
      );

      setUsers(updatedUsers);

      setEditingUser(null);

    } else {

      const user = {
        id: users.length + 1,
        name: newUser.name,
        email: newUser.email,
        role: newUser.role,
      };

      setUsers([...users, user]);
    }

    setNewUser({
      name: "",
      email: "",
      role: "User",
    });

    setShowModal(false);
  };

  const filteredUsers = users.filter((user) =>
    user.name.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div className="dashboard-container">

      <Sidebar />

      <div className="main-content">

        <Navbar />

        <div className="users-page">

          <div className="users-header">

            <h1>User Management</h1>

            <button
              className="add-user-btn"
              onClick={() => {
                setEditingUser(null);

                setNewUser({
                  name: "",
                  email: "",
                  role: "User",
                });

                setShowModal(true);
              }}
            >
              <FaPlus /> Add User
            </button>

          </div>

          <input
            type="text"
            placeholder="Search User..."
            className="search-user"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <table className="user-table">

            <thead>

              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Action</th>
              </tr>

            </thead>

            <tbody>

              {filteredUsers.map((user) => (

                <tr key={user.id}>

                  <td>{user.id}</td>

                  <td>{user.name}</td>

                  <td>{user.email}</td>

                  <td>{user.role}</td>

                  <td>

                    <button
                      className="edit-btn"
                      onClick={() => editUser(user)}
                    >
                      <FaEdit />
                    </button>

                    <button
                      className="delete-btn"
                      onClick={() => deleteUser(user.id)}
                    >
                      <FaTrash />
                    </button>

                  </td>

                </tr>

              ))}

            </tbody>

          </table>

        </div>

      </div>

      {showModal && (

        <div className="modal">

          <div className="modal-content">

            <h2>
              {editingUser ? "Edit User" : "Add User"}
            </h2>

            <input
              type="text"
              placeholder="Name"
              value={newUser.name}
              onChange={(e) =>
                setNewUser({
                  ...newUser,
                  name: e.target.value,
                })
              }
            />

            <input
              type="email"
              placeholder="Email"
              value={newUser.email}
              onChange={(e) =>
                setNewUser({
                  ...newUser,
                  email: e.target.value,
                })
              }
            />

            <select
              value={newUser.role}
              onChange={(e) =>
                setNewUser({
                  ...newUser,
                  role: e.target.value,
                })
              }
            >
              <option>Admin</option>
              <option>User</option>
            </select>

            <div className="modal-buttons">

              <button
                className="save-btn"
                onClick={saveUser}
              >
                {editingUser ? "Update User" : "Save User"}
              </button>

              <button
                className="cancel-btn"
                onClick={() => {
                  setShowModal(false);
                  setEditingUser(null);
                }}
              >
                Cancel
              </button>

            </div>

          </div>

        </div>

      )}

    </div>
  );
}

export default Users;