export const environment = {
    production: false,
    apiUrl: "http://localhost:8080/api/v1",
    defaultPfp: "assets/icons/logo.svg",
    dailyWorkGoal: 8,
    // TODO: load vehicle types from backend...
    vehicleTypes: [
        { id: 'STANDARD', label: 'Standard', icon: '🚗' },
        { id: 'LUXURY', label: 'Luxury', icon: '✨' },
        { id: 'VAN', label: 'Van', icon: '🚐' }
    ],
    excludeTokenEndpoints: ['api/v1/auth'],
    sendTokenAuthEndpoints: ['api/v1/auth/register-driver'],
    nsLat: 45.267136,
    nsLon: 19.833549
};
