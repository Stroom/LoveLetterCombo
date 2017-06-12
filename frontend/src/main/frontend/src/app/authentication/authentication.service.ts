
import { Injectable } from "@angular/core";
import { Http, Headers, RequestOptions } from "@angular/http";
import { Observable } from "rxjs/Observable";
import { environment } from "environments/environment";

import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';


@Injectable()
export class AuthenticationService {

    private headers = new Headers({'Content-Type': 'application/json'});

    roles: string[] = [];

    constructor(private http: Http) {}

    authenticate(username: string, password: string): Observable<boolean> {
        return this.http.post(environment.BASE_URL + '/auth/login', JSON.stringify({username: username, password: password}), {headers: this.headers})
            .map(res => {
                let token = res.json().token;
                if(token) {
                    localStorage.setItem('LoveLetterUser', JSON.stringify({username: username, token: token}));
                }
                else {
                    return false;
                }
                this.roles = res.json().authorities;
                return true;
            },
            err => null)
            .catch((error:any) => this.handleError(error));
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

    hasToken(): boolean {
        return localStorage.getItem('LoveLetterUser') != null;
    }

    logout(): void {
        console.log("OUT");
        localStorage.removeItem('LoveLetterUser');
        this.http.post(environment.BASE_URL + '/auth/logout', JSON.stringify({}), {headers: this.headers}).toPromise()
            .then(res => res)
            .catch((error:any) => this.handleError(error));
    }

    register(username: string, password: string): Observable<boolean> {
        localStorage.setItem('code', btoa(username+':'+password));
        
        return this.http.post(environment.BASE_URL + '/auth/register', JSON.stringify({ username: username, password: password }), {headers: this.headers})
            .map(res => {
                return true;
            })
            .catch(this.handleError);
    }

    private handleError(error: Response | any) {
        return Observable.throw(error);
    }
    
}