import './App.css';
import { BrowserRouter as Router, Route, Routes  } from 'react-router-dom';
import React from 'react';


import HomePage from './Home/HomePage';
function App() {
  return (
    <div className="bx--grid">
      <div className="bx--row">
        <div className="bx--col">
        <Router>
          <Routes>
            <Route path="/" element={<HomePage />} />
          </Routes>
        </Router>
        </div>
      </div>
    </div>
  );
}

export default App;