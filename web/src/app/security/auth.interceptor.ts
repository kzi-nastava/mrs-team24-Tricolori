import { HttpInterceptorFn } from '@angular/common/http';
import { environment } from '../../environments/environment';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('access_token');

  console.log('🚀 Interceptor for:', req.url);
  console.log('🔑 Token found:', token ? 'YES' : 'NO');

  const isExcluded = environment.excludeTokenEndpoints.some(url => req.url.includes(url));
  const isSpecialCase = environment.sendTokenAuthEndpoints.some(url => req.url.includes(url));

  const addToken = !isExcluded || isSpecialCase;

  if (!addToken) {
    console.log('🔓 Skipping auth for:', req.url);
    return next(req);
  }

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
