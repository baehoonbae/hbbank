import { Route, Routes } from 'react-router-dom';
import { RecoilRoot } from 'recoil';
import SideMenu from './components/common/SideMenu';
import { routes } from './routes';
import './styles/App.css';

function App() {

  return (
    <RecoilRoot>
      <SideMenu />
      <div className="ml-64">
        <Routes>
          {routes.map((route) => (
            <Route
              key={route.path}
              path={route.path}
              element={route.element}
            />
          ))}
        </Routes>
      </div>
    </RecoilRoot>
  )
}

export default App
