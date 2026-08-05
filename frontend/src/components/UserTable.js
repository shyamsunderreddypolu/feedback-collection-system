import React from "react";
import "../css/UserTable.css";

function UserTable() {
  return (
    <div className="table-container">

      <div className="table-header">
        <h2>Users</h2>

        <button className="add-btn">
          + Add User
        </button>
      </div>

      <table className="user-table">

        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>

          <tr>
            <td>Mohan</td>
            <td>mohan@gmail.com</td>
            <td>Admin</td>
            <td>Active</td>

            <td>
              <button>Edit</button>
              <button>Delete</button>
            </td>

          </tr>

        </tbody>

      </table>

    </div>
  );
}

export default UserTable;