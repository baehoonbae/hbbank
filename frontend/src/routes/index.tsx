import { RouteObject } from 'react-router-dom';

// 페이지 컴포넌트들
import AccountList from '../pages/AccountList';
import Home from '../pages/Home';
import Login from '../pages/Login';
import SignUp from '../pages/SignUp';
import Transaction from '../pages/Transaction';
import Transfer from '../pages/Transfer';
import CreateAccount from '../pages/CreateAccount';

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
    {
        path: '/transfer',
        element: <Transfer />
    },
    {
        path: '/account-list',
        element: <AccountList />
    },
    {
        path: '/transaction',
        element: <Transaction />
    },
    {
        path: 'create-account',
        element: <CreateAccount />
    }
]; 