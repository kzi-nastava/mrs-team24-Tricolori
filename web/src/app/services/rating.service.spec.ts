import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RatingService, RideRatingRequest, RideRatingResponse } from './rating.service';
import { environment } from '../../environments/environment';

describe('RatingService', () => {
  let service: RatingService;
  let httpMock: HttpTestingController;
  const baseUrl = `${environment.apiUrl}/rides`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RatingService]
    });
    service = TestBed.inject(RatingService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  describe('submitRating', () => {
    const mockRatingData: RideRatingRequest = {
      driverRating: 4,
      vehicleRating: 5,
      comment: 'Great ride!'
    };

    it('should send POST request with number rideId', () => {
      service.submitRating(123, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/123/rate`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(mockRatingData);
      req.flush(null);
    });

    it('should send POST request with string rideId', () => {
      service.submitRating('456', mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/456/rate`);
      expect(req.request.method).toBe('POST');
      req.flush(null);
    });

    it('should send rating data without comment', () => {
      const dataWithoutComment: RideRatingRequest = {
        driverRating: 3,
        vehicleRating: 4
      };

      service.submitRating(1, dataWithoutComment).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.comment).toBeUndefined();
      req.flush(null);
    });

    it('should handle successful response', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        next: (response) => {
          expect(response).toBeNull();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush(null);
    });

    it('should handle 400 Bad Request error', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush({ message: 'Invalid rating data' }, { status: 400, statusText: 'Bad Request' });
    });

    it('should handle 404 Not Found error', (done) => {
      service.submitRating(999, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/999/rate`);
      req.flush(null, { status: 404, statusText: 'Not Found' });
    });

    it('should handle 403 Forbidden error when deadline passed', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush({ message: 'Rating deadline has passed' }, { status: 403, statusText: 'Forbidden' });
    });

    it('should handle 409 Conflict error when already rated', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(409);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush({ message: 'Ride already rated' }, { status: 409, statusText: 'Conflict' });
    });

    it('should handle 500 Internal Server Error', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should handle network error', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.error).toBeTruthy();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.error(new ProgressEvent('Network error'));
    });

    it('should send minimum and maximum rating values', () => {
      const minRating: RideRatingRequest = { driverRating: 1, vehicleRating: 1 };
      service.submitRating(1, minRating).subscribe();
      const req1 = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req1.request.body.driverRating).toBe(1);
      req1.flush(null);

      const maxRating: RideRatingRequest = { driverRating: 5, vehicleRating: 5 };
      service.submitRating(2, maxRating).subscribe();
      const req2 = httpMock.expectOne(`${baseUrl}/2/rate`);
      expect(req2.request.body.driverRating).toBe(5);
      req2.flush(null);
    });

    it('should handle special characters in comment', () => {
      const specialComment: RideRatingRequest = {
        driverRating: 4,
        vehicleRating: 4,
        comment: 'Great! @#$%^&*() 你好 😊'
      };

      service.submitRating(1, specialComment).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.comment).toBe('Great! @#$%^&*() 你好 😊');
      req.flush(null);
    });

    it('should return Observable that completes', (done) => {
      let completed = false;

      service.submitRating(1, mockRatingData).subscribe({
        complete: () => {
          completed = true;
          expect(completed).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush(null);
    });
  });

  describe('getRatingStatus', () => {
    const mockRatingResponse: RideRatingResponse = {
      canRate: true,
      alreadyRated: false,
      deadlinePassed: false,
      deadline: '2025-02-13T10:00:00Z'
    };

    it('should send GET request with number rideId', () => {
      service.getRatingStatus(123).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/123/rating-status`);
      expect(req.request.method).toBe('GET');
      expect(req.request.body).toBeNull();
      req.flush(mockRatingResponse);
    });

    it('should send GET request with string rideId', () => {
      service.getRatingStatus('456').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/456/rating-status`);
      expect(req.request.method).toBe('GET');
      req.flush(mockRatingResponse);
    });

    it('should return correct response data', (done) => {
      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response).toEqual(mockRatingResponse);
          expect(response.canRate).toBe(true);
          expect(response.alreadyRated).toBe(false);
          expect(response.deadlinePassed).toBe(false);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(mockRatingResponse);
    });

    it('should handle response when already rated', (done) => {
      const alreadyRatedResponse: RideRatingResponse = {
        canRate: false,
        alreadyRated: true,
        deadlinePassed: false,
        deadline: '2025-02-13T10:00:00Z'
      };

      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response.alreadyRated).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(alreadyRatedResponse);
    });

    it('should handle response when deadline passed', (done) => {
      const expiredResponse: RideRatingResponse = {
        canRate: false,
        alreadyRated: false,
        deadlinePassed: true,
        deadline: '2025-02-10T10:00:00Z'
      };

      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response.deadlinePassed).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(expiredResponse);
    });

    it('should handle 404 Not Found error', (done) => {
      service.getRatingStatus(999).subscribe({
        error: (error) => {
          expect(error.status).toBe(404);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/999/rating-status`);
      req.flush({ message: 'Ride not found' }, { status: 404, statusText: 'Not Found' });
    });

    it('should handle 401 Unauthorized error', (done) => {
      service.getRatingStatus(1).subscribe({
        error: (error) => {
          expect(error.status).toBe(401);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(null, { status: 401, statusText: 'Unauthorized' });
    });

    it('should handle 500 Internal Server Error', (done) => {
      service.getRatingStatus(1).subscribe({
        error: (error) => {
          expect(error.status).toBe(500);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(null, { status: 500, statusText: 'Internal Server Error' });
    });

    it('should handle network error', (done) => {
      service.getRatingStatus(1).subscribe({
        error: (error) => {
          expect(error.error).toBeTruthy();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.error(new ProgressEvent('Network error'));
    });

    it('should return Observable that completes', (done) => {
      let completed = false;

      service.getRatingStatus(1).subscribe({
        complete: () => {
          completed = true;
          expect(completed).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(mockRatingResponse);
    });
  });

  describe('API URL Construction', () => {
    it('should use correct base URL from environment', () => {
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();
      const req = httpMock.expectOne(`${environment.apiUrl}/rides/1/rate`);
      expect(req.request.url).toContain(environment.apiUrl);
      req.flush(null);
    });
  });

  describe('Multiple Sequential Requests', () => {
    it('should handle multiple submitRating calls', () => {
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();
      service.submitRating(2, { driverRating: 4, vehicleRating: 4 }).subscribe();

      const req1 = httpMock.expectOne(`${baseUrl}/1/rate`);
      const req2 = httpMock.expectOne(`${baseUrl}/2/rate`);

      req1.flush(null);
      req2.flush(null);
    });

    it('should handle multiple getRatingStatus calls', () => {
      service.getRatingStatus(1).subscribe();
      service.getRatingStatus(2).subscribe();

      const req1 = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      const req2 = httpMock.expectOne(`${baseUrl}/2/rating-status`);

      req1.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
      req2.flush({ canRate: false, alreadyRated: true, deadlinePassed: false, deadline: '' });
    });

    it('should handle alternating submitRating and getRatingStatus calls', () => {
      service.getRatingStatus(1).subscribe();
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();
      service.getRatingStatus(1).subscribe();

      const requests = httpMock.match(() => true);
      
      const getRequests = requests.filter(r => r.request.method === 'GET' && r.request.url.includes('rating-status'));
      const postRequest = requests.find(r => r.request.method === 'POST' && r.request.url.includes('/rate'));

      expect(getRequests.length).toBe(2);
      expect(postRequest).toBeTruthy();

      getRequests[0].flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
      postRequest?.flush(null);
      getRequests[1].flush({ canRate: false, alreadyRated: true, deadlinePassed: false, deadline: '' });
    });
  });

  describe('Edge Cases', () => {
    it('should handle rideId of 0', () => {
      service.getRatingStatus(0).subscribe();
      const req = httpMock.expectOne(`${baseUrl}/0/rating-status`);
      expect(req.request.url).toContain('/0/');
      req.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
    });

    it('should handle negative rideId', () => {
      service.getRatingStatus(-1).subscribe();
      const req = httpMock.expectOne(`${baseUrl}/-1/rating-status`);
      expect(req.request.url).toContain('/-1/');
      req.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
    });

    it('should handle large ride IDs', () => {
      service.submitRating(999999999, { driverRating: 5, vehicleRating: 5 }).subscribe();
      const req = httpMock.expectOne(`${baseUrl}/999999999/rate`);
      expect(req.request.url).toContain('999999999');
      req.flush(null);
    });
  });
});