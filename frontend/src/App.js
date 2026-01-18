import React from 'react';
import { Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Profile from './pages/Profile';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Login />} />
      <Route path="/profile" element={<Profile />} />
      <Route path="/register" element={<div style={{ maxWidth: 400, margin: 'auto', padding: 20 }}><h2>Register</h2><p>Registration page coming soon.</p></div>} />
    </Routes>
  );
}

export default App;
