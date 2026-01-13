import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './Login.css';

function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username,
          password: password,
        }),
      });

      const data = await response.json();

      if (response.ok && data.success) {
        //Look at this as an example of storing data in the future
        localStorage.setItem('userId', data.userId);
        localStorage.setItem('userEmail', data.email);

        // Navigate to dashboard or home page
        alert(`Login successful! Welcome ${data.email}`);
        //TODO: navigate('/dashboard'); // Uncomment when dashboard is ready
      } else {
        setError(data.message || 'Login failed. Please try again.');
      }
    } catch (err) {
      setError('Failed to connect to the server. Please try again.');
      console.error('Login error:', err);
    } finally {
      setLoading(false);
    }
  };

  const goToRegister = () => {
    navigate('/register');
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h1 style={{ textAlign: 'center', marginBottom: '1rem', fontFamily: 'Segoe UI, sans-serif', color: '#1976d2', fontSize: '2rem', letterSpacing: '1px' }}>NextStep</h1>
        <h2>Login</h2>
        {error && <div style={{ color: 'red', marginBottom: '1rem', textAlign: 'center' }}>{error}</div>}
        <form onSubmit={handleLogin}>
          <div>
            <label>Username:</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <div>
            <label>Password:</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              disabled={loading}
            />
          </div>
          <button type="submit" disabled={loading}>
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>
        <button type="button" className="register-link" onClick={goToRegister}>
          Register
        </button>
      </div>
    </div>
  );
}

export default Login;

