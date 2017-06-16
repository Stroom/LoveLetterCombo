
import { Injectable } from "@angular/core";
import { Http, Headers, RequestOptions } from "@angular/http";
import { environment } from "environments/environment";
import { AuthenticationService } from "app/authentication/authentication.service";
import { Observable } from "rxjs/Observable";

@Injectable()
export class TestService {

  private headers = new Headers({
    'Content-Type': 'application/json',
    'Authorization': this.authenticationService.getToken()
  });

  constructor(private http: Http, private authenticationService: AuthenticationService) {}

  getTest(): Promise<string> {
    this.headers = new Headers({
      'Content-Type': 'application/json',
      'Authorization': this.authenticationService.getToken()
    });
    return this.http
        .get(environment.BASE_URL + '/api/test', {headers: this.headers}).toPromise()
        .then(
          response => response.text(),
          err => null
        )
        .catch(this.handleError);
  }

  private handleError(error: Response | any) {
    return Observable.throw(error);
  }
  
}