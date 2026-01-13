import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

function Profile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProfile = async () => {
      const userId = localStorage.getItem('userId');

      if (!userId) {
        // If no userId in localStorage, redirect to login
        navigate('/');
        return;
      }

      try {
        const response = await fetch(`http://localhost:8080/api/profile/${userId}`);

        if (response.ok) {
          const data = await response.json();
          setProfile(data);
        } else if (response.status === 404) {
          setError('Profile not found');
        } else {
          setError('Failed to load profile');
        }
      } catch (err) {
        setError('Failed to connect to the server');
        console.error('Profile fetch error:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('userId');
    localStorage.removeItem('userEmail');
    navigate('/');
  };

  if (loading) {
    return (
      <div className="profile-container">
        <div className="profile-card">
          <p>Loading profile...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profile-container">
        <div className="profile-card">
          <h2>Error</h2>
          <p style={{ color: 'red' }}>{error}</p>
          <button onClick={handleLogout}>Back to Login</button>
        </div>
      </div>
    );
  }

  return (
    <div className="profile-container">
      <div className="profile-card">
        <div className="profile-header">
          <h1 className="brand-title">NextStep</h1>
          <div className="user-info">
            <h2 className="user-name">
              {[profile?.firstName, profile?.surname].filter(Boolean).join(' ') || 'User'}
            </h2>
            <p className="user-email">{profile?.email}</p>
          </div>
        </div>

        {/* Career Goal - Prominent Display */}
        {profile?.goalTitle ? (
          <div className="goal-section">
            <div className="goal-icon">🎯</div>
            <div className="goal-content">
              <h3 className="goal-label">Career Goal</h3>
              <p className="goal-title">{profile.goalTitle}</p>
            </div>
          </div>
        ) : (
          <div className="goal-section empty">
            <div className="goal-icon">🎯</div>
            <div className="goal-content">
              <h3 className="goal-label">Career Goal</h3>
              <p className="goal-title-empty">No career goal set yet</p>
            </div>
          </div>
        )}

        {/* Skills Section */}
        <div className="skills-section">
          <h3 className="section-title">
            <span className="section-icon">💡</span>
            Skills
          </h3>
          {profile?.skills && profile.skills.length > 0 ? (
            <div className="tags-container">
              {profile.skills.map((skill, index) => (
                <span key={index} className="tag skill-tag">{skill.trim()}</span>
              ))}
            </div>
          ) : (
            <p className="empty-message">No skills added yet</p>
          )}
        </div>

        {/* Interests Section */}
        <div className="interests-section">
          <h3 className="section-title">
            <span className="section-icon">⭐</span>
            Interests
          </h3>
          {profile?.interests && profile.interests.length > 0 ? (
            <div className="tags-container">
              {profile.interests.map((interest, index) => (
                <span key={index} className="tag interest-tag">{interest.trim()}</span>
              ))}
            </div>
          ) : (
            <p className="empty-message">No interests added yet</p>
          )}
        </div>

        {/* Footer */}
        {profile?.updatedAt && (
          <div className="profile-footer">
            <p className="last-updated">
              Last updated: {new Date(profile.updatedAt).toLocaleDateString()}
            </p>
          </div>
        )}

        <div className="profile-actions">
          <button onClick={handleLogout} className="logout-button">Logout</button>
        </div>
      </div>
    </div>
  );
}

export default Profile;

