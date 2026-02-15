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

    it('should send POST request to correct endpoint with number rideId', () => {
      const rideId = 123;

      service.submitRating(rideId, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/123/rate`);
      expect(req.request.method).toBe('POST');
      expect(req.request.url).toBe(`${baseUrl}/123/rate`);
      req.flush(null);
    });

    it('should send POST request to correct endpoint with string rideId', () => {
      const rideId = '456';

      service.submitRating(rideId, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/456/rate`);
      expect(req.request.method).toBe('POST');
      expect(req.request.url).toBe(`${baseUrl}/456/rate`);
      req.flush(null);
    });

    it('should send correct rating data in request body', () => {
      service.submitRating(1, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body).toEqual(mockRatingData);
      req.flush(null);
    });

    it('should send rating data without comment', () => {
      const dataWithoutComment: RideRatingRequest = {
        driverRating: 3,
        vehicleRating: 4
      };

      service.submitRating(1, dataWithoutComment).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body).toEqual(dataWithoutComment);
      expect(req.request.body.comment).toBeUndefined();
      req.flush(null);
    });

    it('should send rating data with empty comment', () => {
      const dataWithEmptyComment: RideRatingRequest = {
        driverRating: 5,
        vehicleRating: 5,
        comment: ''
      };

      service.submitRating(1, dataWithEmptyComment).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.comment).toBe('');
      req.flush(null);
    });

    it('should handle successful response', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        next: (response) => {
          expect(response).toBeUndefined();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush(null);
    });

    it('should handle 400 Bad Request error', (done) => {
      const errorMessage = 'Invalid rating data';

      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error.status).toBe(400);
          expect(error.error.message).toBe(errorMessage);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.flush({ message: errorMessage }, { status: 400, statusText: 'Bad Request' });
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

    it('should send minimum rating values', () => {
      const minRating: RideRatingRequest = {
        driverRating: 1,
        vehicleRating: 1
      };

      service.submitRating(1, minRating).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.driverRating).toBe(1);
      expect(req.request.body.vehicleRating).toBe(1);
      req.flush(null);
    });

    it('should send maximum rating values', () => {
      const maxRating: RideRatingRequest = {
        driverRating: 5,
        vehicleRating: 5,
        comment: 'A'.repeat(500)
      };

      service.submitRating(1, maxRating).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.driverRating).toBe(5);
      expect(req.request.body.vehicleRating).toBe(5);
      expect(req.request.body.comment.length).toBe(500);
      req.flush(null);
    });

    it('should handle large ride IDs', () => {
      const largeId = 999999999;

      service.submitRating(largeId, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${largeId}/rate`);
      expect(req.request.url).toContain(largeId.toString());
      req.flush(null);
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

    it('should set correct Content-Type header', () => {
      service.submitRating(1, mockRatingData).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.headers.has('Content-Type')).toBe(false);
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

    it('should handle timeout error', (done) => {
      service.submitRating(1, mockRatingData).subscribe({
        error: (error) => {
          expect(error).toBeTruthy();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      req.error(new ProgressEvent('timeout'));
    });
  });

  describe('getRatingStatus', () => {
    const mockRatingResponse: RideRatingResponse = {
      canRate: true,
      alreadyRated: false,
      deadlinePassed: false,
      deadline: '2025-02-13T10:00:00Z'
    };

    it('should send GET request to correct endpoint with number rideId', () => {
      const rideId = 123;

      service.getRatingStatus(rideId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/123/rating-status`);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toBe(`${baseUrl}/123/rating-status`);
      req.flush(mockRatingResponse);
    });

    it('should send GET request to correct endpoint with string rideId', () => {
      const rideId = '456';

      service.getRatingStatus(rideId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/456/rating-status`);
      expect(req.request.method).toBe('GET');
      expect(req.request.url).toBe(`${baseUrl}/456/rating-status`);
      req.flush(mockRatingResponse);
    });

    it('should return correct response data', (done) => {
      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response).toEqual(mockRatingResponse);
          expect(response.canRate).toBe(true);
          expect(response.alreadyRated).toBe(false);
          expect(response.deadlinePassed).toBe(false);
          expect(response.deadline).toBe('2025-02-13T10:00:00Z');
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
          expect(response.canRate).toBe(false);
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
          expect(response.canRate).toBe(false);
          expect(response.deadlinePassed).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(expiredResponse);
    });

    it('should handle response when both alreadyRated and deadlinePassed are true', (done) => {
      const bothTrueResponse: RideRatingResponse = {
        canRate: false,
        alreadyRated: true,
        deadlinePassed: true,
        deadline: '2025-02-10T10:00:00Z'
      };

      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response.alreadyRated).toBe(true);
          expect(response.deadlinePassed).toBe(true);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(bothTrueResponse);
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

    it('should handle 403 Forbidden error', (done) => {
      service.getRatingStatus(1).subscribe({
        error: (error) => {
          expect(error.status).toBe(403);
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(null, { status: 403, statusText: 'Forbidden' });
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

    it('should not send request body', () => {
      service.getRatingStatus(1).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      expect(req.request.body).toBeNull();
      req.flush(mockRatingResponse);
    });

    it('should handle large ride IDs', () => {
      const largeId = 999999999;

      service.getRatingStatus(largeId).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/${largeId}/rating-status`);
      expect(req.request.url).toContain(largeId.toString());
      req.flush(mockRatingResponse);
    });

    it('should handle different deadline formats', (done) => {
      const responseWithDifferentDate: RideRatingResponse = {
        canRate: true,
        alreadyRated: false,
        deadlinePassed: false,
        deadline: '2025-12-31T23:59:59.999Z'
      };

      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response.deadline).toBe('2025-12-31T23:59:59.999Z');
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(responseWithDifferentDate);
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

    it('should handle malformed response gracefully', (done) => {
      const malformedResponse = {
        canRate: 'true',
        alreadyRated: 'false'
      };

      service.getRatingStatus(1).subscribe({
        next: (response) => {
          expect(response).toBeTruthy();
          done();
        }
      });

      const req = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      req.flush(malformedResponse);
    });
  });

  describe('API URL Construction', () => {
    it('should use correct base URL from environment', () => {
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/rides/1/rate`);
      expect(req.request.url).toContain(environment.apiUrl);
      req.flush(null);
    });

    it('should construct URL correctly for rating status', () => {
      service.getRatingStatus(1).subscribe();

      const req = httpMock.expectOne(`${environment.apiUrl}/rides/1/rating-status`);
      expect(req.request.url).toContain(environment.apiUrl);
      req.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
    });

    it('should handle trailing slashes in base URL', () => {
      const expectedUrl = `${baseUrl}/1/rate`.replace('//', '/');
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();

      httpMock.expectOne((req) => req.url.includes('/rides/1/rate'));
      httpMock.verify();
    });
  });

  describe('Multiple Sequential Requests', () => {
    it('should handle multiple submitRating calls', () => {
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();
      service.submitRating(2, { driverRating: 4, vehicleRating: 4 }).subscribe();
      service.submitRating(3, { driverRating: 3, vehicleRating: 3 }).subscribe();

      const req1 = httpMock.expectOne(`${baseUrl}/1/rate`);
      const req2 = httpMock.expectOne(`${baseUrl}/2/rate`);
      const req3 = httpMock.expectOne(`${baseUrl}/3/rate`);

      req1.flush(null);
      req2.flush(null);
      req3.flush(null);
    });

    it('should handle multiple getRatingStatus calls', () => {
      service.getRatingStatus(1).subscribe();
      service.getRatingStatus(2).subscribe();
      service.getRatingStatus(3).subscribe();

      const req1 = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      const req2 = httpMock.expectOne(`${baseUrl}/2/rating-status`);
      const req3 = httpMock.expectOne(`${baseUrl}/3/rating-status`);

      req1.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
      req2.flush({ canRate: false, alreadyRated: true, deadlinePassed: false, deadline: '' });
      req3.flush({ canRate: false, alreadyRated: false, deadlinePassed: true, deadline: '' });
    });

    it('should handle alternating submitRating and getRatingStatus calls', () => {
      service.getRatingStatus(1).subscribe();
      service.submitRating(1, { driverRating: 5, vehicleRating: 5 }).subscribe();
      service.getRatingStatus(1).subscribe();

      const getReq1 = httpMock.expectOne(`${baseUrl}/1/rating-status`);
      const postReq = httpMock.expectOne(`${baseUrl}/1/rate`);
      const getReq2 = httpMock.expectOne(`${baseUrl}/1/rating-status`);

      getReq1.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
      postReq.flush(null);
      getReq2.flush({ canRate: false, alreadyRated: true, deadlinePassed: false, deadline: '' });
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

    it('should handle decimal rating values', () => {
      const decimalRating: RideRatingRequest = {
        driverRating: 4.5,
        vehicleRating: 3.7
      };

      service.submitRating(1, decimalRating).subscribe();

      const req = httpMock.expectOne(`${baseUrl}/1/rate`);
      expect(req.request.body.driverRating).toBe(4.5);
      expect(req.request.body.vehicleRating).toBe(3.7);
      req.flush(null);
    });

    it('should handle empty string rideId', () => {
      service.getRatingStatus('').subscribe();

      const req = httpMock.expectOne(`${baseUrl}//rating-status`);
      req.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
    });

    it('should handle whitespace in string rideId', () => {
      service.getRatingStatus('  123  ').subscribe();

      const req = httpMock.expectOne(`${baseUrl}/  123  /rating-status`);
      req.flush({ canRate: true, alreadyRated: false, deadlinePassed: false, deadline: '' });
    });
  });
});