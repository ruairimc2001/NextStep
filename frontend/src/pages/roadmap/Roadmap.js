import React, { useState, useEffect } from 'react';
import './Roadmap.css';

function Roadmap() {
  const [roadmap, setRoadmap] = useState(null);
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [currentStageIndex, setCurrentStageIndex] = useState(0);

  // Fetch user profile on page load
  useEffect(() => {
    const fetchProfile = async () => {
      const userId = localStorage.getItem('userId');
      const token = localStorage.getItem('token');
      if (!userId) return;

      try {
        const response = await fetch(`http://localhost:8080/api/profile/${userId}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });
        if (response.ok) {
          const data = await response.json();
          setProfile(data);
        }
      } catch (err) {
        console.error('Failed to fetch profile:', err);
      }
    };

    fetchProfile();
  }, []);

  const generateRoadmap = async () => {
    setLoading(true);
    setError('');
    setCurrentStageIndex(0);

    const userId = localStorage.getItem('userId');
    const token = localStorage.getItem('token');

    try {
      const response = await fetch('http://localhost:8080/api/roadmaps/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`,
        },
        body: JSON.stringify({
          userId: userId,
          firstName: profile?.firstName,
          surname: profile?.surname,
          goalTitle: profile?.goalTitle || 'Software Developer',
          skills: profile?.skills || [],
          interests: profile?.interests || [],
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setRoadmap(data);
      } else {
        setError('Failed to generate roadmap');
      }
    } catch (err) {
      setError('Failed to connect to the server');
      console.error('Roadmap generation error:', err);
    } finally {
      setLoading(false);
    }
  };

  const goToPreviousStage = () => {
    if (currentStageIndex > 0) {
      setCurrentStageIndex(currentStageIndex - 1);
    }
  };

  const goToNextStage = () => {
    if (roadmap?.stages && currentStageIndex < roadmap.stages.length - 1) {
      setCurrentStageIndex(currentStageIndex + 1);
    }
  };

  const currentStage = roadmap?.stages?.[currentStageIndex];
  const totalStages = roadmap?.stages?.length || 0;

  return (
    <div className="roadmap-container">
      <div className="roadmap-card">
        <h1 className="roadmap-title">NextSteps</h1>
        <h2 className="roadmap-subtitle">Your Career Roadmap</h2>

        {!roadmap && !loading && (
          <div className="generate-section">
            <p>Click below to generate your personalised career roadmap</p>
            <button className="generate-button" onClick={generateRoadmap}>
              Generate Roadmap
            </button>
          </div>
        )}

        {loading && (
          <div className="loading-section">
            <p>Generating your roadmap...</p>
            <div className="spinner"></div>
          </div>
        )}

        {error && <p className="error-message">{error}</p>}

        {roadmap && currentStage && (
          <div className="roadmap-content">
            {/* Stage navigation indicator */}
            <div className="stage-navigation-header">
              <span className="stage-indicator">
                Stage {currentStageIndex + 1} of {totalStages}
              </span>
              <span className="target-role">Target: {roadmap.targetRole}</span>
            </div>

            {/* Progress bar */}
            <div className="progress-bar-container">
              <div
                className="progress-bar-fill"
                style={{ width: `${((currentStageIndex + 1) / totalStages) * 100}%` }}
              ></div>
            </div>

            {/* Current stage display */}
            <div className="stage-view">
              <div className="stage-view-header">
                <span className="stage-number">{currentStage.order || currentStageIndex + 1}</span>
                <h3 className="stage-view-title">{currentStage.title}</h3>
              </div>

              {currentStage.description && (
                <p className="stage-view-description">{currentStage.description}</p>
              )}

              {/* Courses displayed horizontally */}
              {currentStage.items && currentStage.items.length > 0 && (
                <div className="course-list">
                  {currentStage.items.map((course, courseIndex) => (
                    <div key={course.itemId || courseIndex} className="course-item">
                      <div className="course-header">
                        <span className="course-title">{course.title}</span>
                        {course.estimatedHours && (
                          <span className="course-hours">{course.estimatedHours}h</span>
                        )}
                      </div>
                      {course.description && (
                        <p className="course-description">{course.description}</p>
                      )}
                      {course.url && (
                        <a
                          href={course.url}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="course-link"
                        >
                          View Course →
                        </a>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Navigation buttons */}
            <div className="stage-navigation">
              <button
                className="nav-button prev"
                onClick={goToPreviousStage}
                disabled={currentStageIndex === 0}
              >
                ← Previous
              </button>
              <button
                className="nav-button next"
                onClick={goToNextStage}
                disabled={currentStageIndex === totalStages - 1}
              >
                Next →
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default Roadmap;

