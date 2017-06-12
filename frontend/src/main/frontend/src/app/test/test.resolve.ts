import { Injectable } from '@angular/core';
import { Router, Resolve, ActivatedRouteSnapshot } from '@angular/router';
import { Http, RequestOptions } from "@angular/http";

import 'rxjs/add/operator/toPromise';
import { AuthenticationService } from "app/authentication/authentication.service";
import { TestService } from "app/test/test.service";

@Injectable()
export class TestResolve implements Resolve<Promise<string> | boolean> {
    constructor(private router: Router, private http: Http, private testService: TestService) { }
    
    text:string;

    resolve(route: ActivatedRouteSnapshot): Promise<string> | boolean {
        return this.getTest().then(
            res => {
                if (res) {
                    console.log(res);
                    this.text = res;
                    return this.text;
                }
                else {
                    this.router.navigateByUrl('');
                    return null;
                }
                
            }
        )
        .catch(err => {
            this.router.navigateByUrl('');
            return null;
        });
    }

    getTest(): Promise<string> {
        return this.testService.getTest();
    }

    private handleError(error: any): Promise<any> {
        console.error('Error occurred', error);
        return Promise.reject(error.message || error);
    }

}