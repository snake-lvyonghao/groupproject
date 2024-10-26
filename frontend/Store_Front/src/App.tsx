import { Route, BrowserRouter as Router, Routes } from "react-router-dom";
import "./App.css";
import MainPage from "./components/MainPage";
import SignIn from "./components/SignIn";
import SignUp from "./components/SignUp";

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<SignIn />} />
        <Route path="/signup" element={<SignUp />} />
        <Route path="/mainpage/:email" element={<MainPage />} />
      </Routes>
    </Router>
  );
}

export default App;
