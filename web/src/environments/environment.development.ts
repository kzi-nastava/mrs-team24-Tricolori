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
    ]
};
