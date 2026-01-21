import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('access_token');

  console.log('🔍 Interceptor - Token exists:', !!token);
  console.log('🔍 Interceptor - Request URL:', req.url);

  if (token) {
    const authReq = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(authReq);
  }

  console.log('❌ No token - sending request without auth');
  return next(req);
};
