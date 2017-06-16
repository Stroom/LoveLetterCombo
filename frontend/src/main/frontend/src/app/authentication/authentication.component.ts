import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { AuthenticationService } from "app/authentication/authentication.service";

@Component({
  selector: 'login',
  templateUrl: './authentication.component.html'
})
export class AuthenticationComponent implements OnInit {

  username: string = "";
  password: string = "";
  error: string = "";

  constructor(private authentication: AuthenticationService, private router: Router) {}

  ngOnInit() {
    if(this.authentication.isLoggedIn()) {
      this.authentication.logout();
    }
  }

  login() {
    this.authentication.authenticate(this.username, this.password).subscribe(
      res => {
        if(res === true) {
          this.error = "";
          this.successfulLogin();
        }
        else {
          this.error = "Username or password is incorrect";
        }
      }, 
      err => {
        this.error = "Username or password is incorrect";
      }
    );
  }

  successfulLogin() {
    this.router.navigateByUrl('');
  }

}