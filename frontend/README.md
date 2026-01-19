# NextSteps Frontend

This folder contains the React-based frontend for the NextSteps application.

## Setup
- Built with [React](https://react.dev/) and [React Router](https://reactrouter.com/) for navigation.
- Main entry point: `src/index.js`.
- App routes are defined in `src/App.js`.
- Pages are located in `src/pages/` (e.g., `Login.js`).
- Components for reuse can be placed in `src/components/`.

## Getting Started
1. Install dependencies:
   ```bash
   npm install
   ```
2. Start the development server:
   ```bash
   npm start
   ```
3. Open [http://localhost:3000](http://localhost:3000) in your browser to view the app.


4. Docker set up 
# Start everything
docker compose up -d

# Fresh database (fixes Liquibase checksum issues)
docker compose down -v && docker compose up -d

# Stop everything
docker compose down


