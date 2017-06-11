import { Injectable } from '@angular/core';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { Http, RequestOptions } from "@angular/http";

import 'rxjs/add/operator/toPromise';

@Injectable()
export class TestResolve implements Resolve<Promise<string> | boolean> {
    constructor(private router: Router, private http: Http) { }
    
    text:string;

    resolve(route: ActivatedRouteSnapshot): Promise<string> | boolean {
        return this.getTest().then(
            res => {
                if (res) {
                    this.text = res;
                    return this.text;
                }
                else {
                    this.router.navigate(['home']);
                    return null;
                }
                
            }
        )
    }

    getTest(): Promise<string> {
        return this.http
            .get('http://localhost:8080/api/test').toPromise()
            .then(response => response.text())
            .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('Error occurred', error);
        return Promise.reject(error.message || error);
    }

}