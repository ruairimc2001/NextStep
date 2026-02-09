import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/Navbar';
import './Dashboard.css';

function Dashboard() {
  const [dashboard, setDashboard] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchDashboard = async () => {
      const userId = localStorage.getItem('userId');
      const token = localStorage.getItem('token');

      if (!userId) {
        navigate('/login');
        return;
      }

      try {
        const response = await fetch(`http://localhost:8080/api/dashboard/${userId}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });

        if (response.ok) {
          const data = await response.json();
          setDashboard(data);
        } else if (response.status === 401) {
          localStorage.clear();
          navigate('/login');
        } else {
          setError('Failed to load dashboard');
        }
      } catch (err) {
        setError('Failed to connect to the server');
        console.error('Dashboard fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchDashboard();
  }, [navigate]);

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  };

  const handleDeleteRoadmap = async (roadmapId) => {
    if (!window.confirm('Are you sure you want to delete this roadmap? This action cannot be undone.')) {
      return;
    }

    const token = localStorage.getItem('token');

    try {
      const response = await fetch(`http://localhost:8080/api/roadmaps/${roadmapId}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
      });

      if (response.status === 204 || response.ok) {
        setDashboard({
          ...dashboard,
          roadmaps: dashboard.roadmaps.filter(r => r.id !== roadmapId),
          stats: {
            ...dashboard.stats,
            totalRoadmaps: dashboard.stats.totalRoadmaps - 1,
          },
        });
      } else {
        const errorText = await response.text();
        alert(`Failed to delete roadmap (Status: ${response.status})`);
      }
    } catch (err) {
      alert('Failed to delete roadmap: ' + err.message);
    }
  };

  if (loading) {
    return (
      <div className="dashboard-page">
        <Navbar />
        <div className="dashboard-content">
          <p className="loading-message">Loading dashboard...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="dashboard-page">
        <Navbar />
        <div className="dashboard-content">
          <p className="error-message">{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <Navbar />
      <div className="dashboard-content">
        <h1 className="dashboard-title">Dashboard</h1>
        <div className="dashboard-grid">
          {/* Profile Card */}
          <section className="dashboard-card profile-card">
            <h2 className="card-title">Your Profile</h2>
            <div className="card-body">
              {dashboard?.profile ? (
                <>
                  <div className="detail-row">
                    <span className="detail-label">Name</span>
                    <span className="detail-value">
                      {dashboard.profile.firstName} {dashboard.profile.surname}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Email</span>
                    <span className="detail-value">{dashboard.profile.email}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Career Goal</span>
                    <span className="detail-value">{dashboard.profile.goalTitle || 'Not set'}</span>
                  </div>
                  {dashboard.profile.skills && dashboard.profile.skills.length > 0 && (
                    <div className="detail-row">
                      <span className="detail-label">Skills</span>
                      <div className="skills-list">
                        {dashboard.profile.skills.map((skill, idx) => (
                          <span key={idx} className="skill-badge">{skill}</span>
                        ))}
                      </div>
                    </div>
                  )}
                  {dashboard.profile.interests && dashboard.profile.interests.length > 0 && (
                    <div className="detail-row">
                      <span className="detail-label">Interests</span>
                      <div className="interests-list">
                        {dashboard.profile.interests.map((interest, idx) => (
                          <span key={idx} className="interest-badge">{interest}</span>
                        ))}
                      </div>
                    </div>
                  )}
                  <button className="edit-btn" onClick={() => navigate('/profile')}>
                    Edit Profile
                  </button>
                </>
              ) : (
                <p className="card-hint">No profile data available</p>
              )}
            </div>
          </section>

          {/* Roadmaps Card */}
          <section className="dashboard-card roadmaps-card">
            <div className="card-header">
              <h2 className="card-title">Your Roadmaps</h2>
              <button className="new-roadmap-btn" onClick={() => navigate('/roadmap')}>
                + New
              </button>
            </div>
            <div className="card-body">
              {dashboard?.stats && (
                <div className="stats-summary">
                  <div className="stat">
                    <span className="stat-value">{dashboard.stats.totalRoadmaps}</span>
                    <span className="stat-label">Roadmaps</span>
                  </div>
                  <div className="stat">
                    <span className="stat-value">{dashboard.stats.totalStages}</span>
                    <span className="stat-label">Stages</span>
                  </div>
                  <div className="stat">
                    <span className="stat-value">{dashboard.stats.totalStagesCompleted}</span>
                    <span className="stat-label">Completed</span>
                  </div>
                </div>
              )}

              {dashboard?.roadmaps && dashboard.roadmaps.length > 0 ? (
                <div className="roadmaps-list">
                  {dashboard.roadmaps.map((roadmap) => (
                    <div key={roadmap.id} className="roadmap-item">
                      <div className="roadmap-header">
                        <h3 className="roadmap-title">{roadmap.title}</h3>
                        <span className="roadmap-date">{formatDate(roadmap.createdAt)}</span>
                      </div>
                      <p className="roadmap-summary">{roadmap.summary}</p>
                      {roadmap.stats && (
                        <div className="roadmap-info">
                          <span>{roadmap.stats.completedCourses} / {roadmap.stats.totalCourses} courses</span>
                          <span>•</span>
                          <span>{roadmap.totalStages} stages</span>
                        </div>
                      )}
                      <button
                        className="view-btn"
                        onClick={() => navigate(`/roadmap/${roadmap.id}`)}
                      >
                        View Details
                      </button>
                      <button
                        className="delete-btn"
                        onClick={() => handleDeleteRoadmap(roadmap.id)}
                      >
                        Delete
                      </button>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="no-roadmaps">
                  <p>You haven't created any roadmaps yet</p>
                  <button className="create-btn" onClick={() => navigate('/roadmap')}>
                    Create Your First Roadmap
                  </button>
                </div>
              )}
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}

export default Dashboard;
