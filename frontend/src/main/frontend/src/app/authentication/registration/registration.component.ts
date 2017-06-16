import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { AuthenticationService } from "app/authentication/authentication.service";

@Component({
  selector: 'register',
  templateUrl: './registration.component.html'
})
export class RegistrationComponent {

  username: string;
  password: string;

  constructor(private authentication: AuthenticationService, private router: Router) {}

  register() {
    this.authentication.register(this.username, this.password).subscribe(res => this.successfulRegister());
  }

  successfulRegister() {
    this.router.navigateByUrl('login');
  }

}