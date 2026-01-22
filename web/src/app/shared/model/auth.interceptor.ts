import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/api/v1/auth/')) {
    console.log('🔓 Skipping auth for:', req.url);
    return next(req);
  }

  const token = localStorage.getItem('access_token');
  
  console.log('🚀 Interceptor for:', req.url);
  console.log('🔑 Token found:', token ? 'YES' : 'NO');

  if (token) {
    console.log('✅ Adding Authorization header');
    const authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(authReq);
  }

  console.warn('⚠️ No token found');
  return next(req);
};