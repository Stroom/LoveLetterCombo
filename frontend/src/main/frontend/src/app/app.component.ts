import { Component } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { AuthenticationService } from "app/authentication/authentication.service";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent {
  title = 'Love Letter Application';

  constructor(private authentication: AuthenticationService, private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    if(this.authentication.isLoggedIn() && !this.authentication.hasRoles()) {
      console.log("asking for roles.");
      this.authentication.authenticateForRoles();
    }
  }

  isLoggedIn(): boolean {
    return this.authentication.isLoggedIn();
  }

  roleIs(roles: string[]): boolean {
    return this.authentication.hasRole(roles);
  }

  logout(): void {
    this.authentication.logout();
    this.router.navigateByUrl('');
  }
}