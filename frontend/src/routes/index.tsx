import { RouteObject } from 'react-router-dom';
import { Navigate } from 'react-router-dom';

// 페이지 컴포넌트들
import AccountListPage from '../pages/AccountListPage';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import SignUpPage from '../pages/SignUpPage';
import TransactionPage from '../pages/TransactionPage';
import TransferPage from '../pages/TransferPage';
import CreateAccountPage from '../pages/CreateAccountPage';
import AccountDetailPage from '../pages/AccountDetailPage';
import UserDashBoard from '../pages/UserDashBoard';
import AutoTransferPage from '../pages/AutoTransferPage';
import ReserveTransferPage from '../pages/ReserveTransferPage';
import OAuth2RedirectPage from '../pages/OAuth2RedirectPage';
import AdditionalInfoPage from '../pages/AdditionalInfoPage';

// 인증이 필요한 라우트를 위한 컴포넌트
const PrivateRoute = ({ children }: { children: React.ReactElement }) => {
    const isAuthenticated = sessionStorage.getItem('accessToken');
    return isAuthenticated ? children : <Navigate to="/login" />;
};

// 홈 라우트를 위한 컴포넌트
const HomeRoute = () => {
    const isAuthenticated = sessionStorage.getItem('accessToken');
    return isAuthenticated ? <Navigate to="/user-dashboard" /> : <HomePage />;
};

export const routes: RouteObject[] = [
    {
        path: '/',
        element: <HomeRoute />
    },
    {
        path: '/user-dashboard',
        element: <PrivateRoute><UserDashBoard /></PrivateRoute>
    },
    {
        path: '/login',
        element: <LoginPage />
    },
    {
        path: '/signup',
        element: <SignUpPage />
    },
    {
        path: '/transfer',
        element: <PrivateRoute><TransferPage /></PrivateRoute>
    },
    {
        path: '/auto-transfer',
        element: <PrivateRoute><AutoTransferPage /></PrivateRoute>
    },
    {
        path: '/reserve-transfer',
        element: <PrivateRoute><ReserveTransferPage /></PrivateRoute>
    },
    {
        path: '/account-list',
        element: <PrivateRoute><AccountListPage /></PrivateRoute>
    },
    {
        path: '/transaction',
        element: <PrivateRoute><TransactionPage /></PrivateRoute>
    },
    {
        path: '/create-account',
        element: <PrivateRoute><CreateAccountPage /></PrivateRoute>
    },
    {
        path: '/account/:id',
        element: <PrivateRoute><AccountDetailPage /></PrivateRoute>
    },
    {
        path: '/oauth2/redirect',
        element: <OAuth2RedirectPage />
    },  
    {
        path: '/oauth2/additional-info',
        element: <AdditionalInfoPage />
    }
];