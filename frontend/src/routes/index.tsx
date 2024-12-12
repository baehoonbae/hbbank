import { RouteObject, useParams } from 'react-router-dom';

// 페이지 컴포넌트들
import AccountListPage from '../pages/AccountListPage';
import HomePage from '../pages/HomePage';
import LoginPage from '../pages/LoginPage';
import SignUpPage from '../pages/SignUpPage';
import TransactionPage from '../pages/TransactionPage';
import TransferPage from '../pages/TransferPage';
import CreateAccountPage from '../pages/CreateAccountPage';
import AccountDetailPage from '../pages/AccountDetailPage';

export const routes: RouteObject[] = [
    {
        path: '/',
        element: <HomePage />
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
        element: <TransferPage />
    },
    {
        path: '/account-list',
        element: <AccountListPage />
    },
    {
        path: '/transaction',
        element: <TransactionPage />
    },
    {
        path: 'create-account',
        element: <CreateAccountPage />
    },
    {
        path: '/account/:id',
        element: <AccountDetailPage />
    }
]; 