import React from 'react';
import { Navigate } from 'react-router-dom';

function ProtectedRoute({ children }) {
  const userId = localStorage.getItem('userId');
  const token = localStorage.getItem('token');

  // Check if user is authenticated
  if (!userId || !token) {
    // Redirect to login if not authenticated
    return <Navigate to="/login" replace />;
  }

  return children;
}

export default ProtectedRoute;

