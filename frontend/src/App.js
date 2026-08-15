import { Routes, Route } from "react-router-dom";

import AdminLogin from "./pages/AdminLogin";
import AdminDashboard from "./pages/AdminDashboard";
import StudentDashboard from "./pages/StudentDashboard";
import TakeSurveyPage from "./pages/TakeSurveyPage";
import FacultyDashboard from "./pages/FacultyDashboard";
import FormBuilderStudio from "./pages/FormBuilderStudio";
import Users from "./pages/Users";
import Reports from "./pages/Reports";
import Settings from "./pages/Settings";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <Routes>
      {/* Public Login Route */}
      <Route path="/" element={<AdminLogin />} />
      <Route path="/login" element={<AdminLogin />} />

      {/* Admin Protected Routes */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <AdminDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/form-builder"
        element={
          <ProtectedRoute allowedRole="ROLE_ADMIN">
            <FormBuilderStudio />
          </ProtectedRoute>
        }
      />
      <Route
        path="/users"
        element={
          <ProtectedRoute allowedRole="ROLE_ADMIN">
            <Users />
          </ProtectedRoute>
        }
      />
      <Route
        path="/reports"
        element={
          <ProtectedRoute>
            <Reports />
          </ProtectedRoute>
        }
      />
      <Route
        path="/settings"
        element={
          <ProtectedRoute>
            <Settings />
          </ProtectedRoute>
        }
      />

      {/* Student Protected Routes */}
      <Route
        path="/student-surveys"
        element={
          <ProtectedRoute>
            <StudentDashboard />
          </ProtectedRoute>
        }
      />
      <Route
        path="/take-survey/:formId"
        element={
          <ProtectedRoute>
            <TakeSurveyPage />
          </ProtectedRoute>
        }
      />

      {/* Faculty Protected Routes */}
      <Route
        path="/faculty-analytics"
        element={
          <ProtectedRoute>
            <FacultyDashboard />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default App;