import { Navigate, RouteObject } from 'react-router-dom';

// 페이지 컴포넌트들
import AccountDetailPage from '../pages/AccountDetailPage';
import AccountListPage from '../pages/AccountListPage';
import AutoTransferPage from '../pages/AutoTransferPage';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import OAuth2RedirectPage from '../pages/OAuth2RedirectPage';
import ReserveTransferPage from '../pages/ReserveTransferPage';
import TransactionPage from '../pages/TransactionPage';
import TransferPage from '../pages/TransferPage';
import UserDashBoard from '../pages/UserDashBoard';
import AutoTransferManagePage from '../pages/AutoTransferManagePage';
import SignUpPage from '../pages/SignUpPage';
import CreateAccountPage from '../pages/CreateAccountPage';
import AdditionalInfoPage from '../pages/AdditionalInfoPage';
import AutoTransferEditPage from '../pages/AutoTransferEditPage';
import ReserveTransferManagePage from '../pages/ReserveTransferManagePage';

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
        path: '/auto-transfer/manage',
        element: <PrivateRoute><AutoTransferManagePage /></PrivateRoute>
    },
    {
        path: '/auto-transfer/edit/:id',
        element: <PrivateRoute><AutoTransferEditPage /></PrivateRoute>
    },
    {
        path: '/reserve-transfer',
        element: <PrivateRoute><ReserveTransferPage /></PrivateRoute>
    },
    {
        path: '/reserve-transfer/manage',
        element: <PrivateRoute><ReserveTransferManagePage /></PrivateRoute>
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