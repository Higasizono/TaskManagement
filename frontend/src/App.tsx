import { Route, Routes } from 'react-router-dom';
import { TopPage } from './pages/TopPage';
import { BoardPage } from './pages/BoardPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<TopPage />} />
      <Route path="/board/:boardId" element={<BoardPage />} />
    </Routes>
  );
}

export default App;
