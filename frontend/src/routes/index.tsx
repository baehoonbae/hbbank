import { RouteObject } from 'react-router-dom';

// 페이지 컴포넌트들
import Home from '../pages/Home';
import Login from '../pages/Login';
import SignUp from '../pages/SignUp';

export const routes: RouteObject[] = [
    {
        path: '/',
        element: <Home />
    },
    {
        path: '/login',
        element: <Login />
    },
    {
        path: '/signup',
        element: <SignUp />
    },
]; 