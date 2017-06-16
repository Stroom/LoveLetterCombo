import { Injectable } from "@angular/core";
import { Http, Headers, RequestOptions } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { environment } from "environments/environment";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import { LoginResponse } from "app/definitions";

@Injectable()
export class AuthenticationService {

  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http) {}

  authenticate(username: string, password: string): Observable<boolean> {
    return this.http.post(environment.BASE_URL + '/auth/login', JSON.stringify({username: username, password: password}), {headers: this.headers})
      .map(res => {
        let response: LoginResponse = res.json();
        let token = response.token;
          if(token) {
            localStorage.setItem('LoveLetterUser', JSON.stringify({username: username, token: token}));
          }
          else {
            localStorage.removeItem('LoveLetterRoles');
            return false;
          }
          localStorage.setItem('LoveLetterRoles', JSON.stringify({roles: response.authorities}));
          return true;
        },
        err => null)
      .catch((error:any) => this.handleError(error));
  }

  authenticateForRoles(): Promise<boolean> {
    let headers = new Headers({
      'Content-Type': 'application/json',
      'Authorization': this.getToken()
    });
    return this.http.get(environment.BASE_URL + '/auth/roles', {headers: headers}).toPromise().then(res => {
      let roles: string[] = res.json();
      localStorage.setItem('LoveLetterRoles', JSON.stringify({roles: roles}));
      return true;
    });
  }

  isLoggedIn(): boolean {
    var token: String = this.getToken();
    return token && token.length > 0;
  }

  getToken(): string {
    var currentUser = JSON.parse(localStorage.getItem('LoveLetterUser'));
    var token = currentUser && currentUser.token;
    return token ? token : "";
  }
  
  getRoles(): string[] {
    var container = JSON.parse(localStorage.getItem('LoveLetterRoles'));
    var roles = container && container.roles;
    return roles ? roles : null;
  }

  hasToken(): boolean {
    return localStorage.getItem('LoveLetterUser') != null;
  }

  hasRoles(): boolean {
    return localStorage.getItem('LoveLetterRoles') != null;
  }

  hasRole(roles: string[]): boolean {
    if(roles != null && roles.length > 0 && this.getRoles() != null && this.getRoles().length > 0) {
      return this.getRoles().filter(role => roles.indexOf(role) >= 0).length > 0;
    }
    return false;
  }

  logout(): void {
    localStorage.removeItem('LoveLetterUser');
    localStorage.removeItem('LoveLetterRoles');
    this.http.post(environment.BASE_URL + '/auth/logout', JSON.stringify({}), {headers: this.headers}).toPromise()
      .then(res => res)
      .catch((error:any) => this.handleError(error));
  }

  register(username: string, password: string): Observable<boolean> {
    localStorage.setItem('code', btoa(username+':'+password));
    
    return this.http.post(environment.BASE_URL + '/auth/register', 
        JSON.stringify({ username: username, password: password }), 
        {headers: this.headers})
      .map(res => {
        return true;
      })
      .catch(this.handleError);
  }

  private handleError(error: Response | any) {
    return Observable.throw(error);
  }
    
}