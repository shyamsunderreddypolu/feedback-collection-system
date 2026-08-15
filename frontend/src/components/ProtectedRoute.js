import React, { useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';

const ProtectedRoute = ({ children, allowedRole }) => {
  const { token, user } = useContext(AuthContext);

  if (!token) {
    return <Navigate to="/" replace />;
  }

  if (allowedRole && user && user.role !== allowedRole) {
    return <Navigate to="/dashboard" replace />;
  }

  return children;
};

export default ProtectedRoute;
