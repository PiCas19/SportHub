import {Navigate} from 'react-router-dom';
import Cookies from 'js-cookie';

const ProtectedRoute = ({children}: any) => {
  const authToken = Cookies.get('auth_token');

  if (!authToken) {
    return <Navigate to="" replace/>;
  }

  return children;
};

export default ProtectedRoute;