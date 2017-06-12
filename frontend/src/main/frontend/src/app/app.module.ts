import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpModule, XSRFStrategy, CookieXSRFStrategy } from '@angular/http';

import { AppComponent } from './app.component';
import { TestComponent } from "app/test/test.component";
import { HomeComponent } from "app/home.component";
import { TestResolve } from "app/test/test.resolve";
import { SockComponent } from "app/test/sock.component";

import { StompService } from 'ng2-stomp-service';
import { AuthenticationComponent } from "app/authentication/authentication.component";
import { AuthenticationService } from "app/authentication/authentication.service";
import { RegistrationComponent } from "app/authentication/registration/registration.component";
import { TestService } from "app/test/test.service";
import { CanActivateAuthGuard } from "app/authentication/can-activate.authguard";

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent
  },
  {
    path: 'login',
    component: AuthenticationComponent
  },
  {
    path: 'register',
    component: RegistrationComponent
  },
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },

  {
    path: 'test',
    component: TestComponent,
    canActivate: [ CanActivateAuthGuard ],
    resolve: { text: TestResolve }
  },
  {
    path: 'websocket',
    component: SockComponent,
    canActivate: [ CanActivateAuthGuard ]
  }
  //TODO add default error page.
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    AuthenticationComponent,
    RegistrationComponent,

    TestComponent,
    SockComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    RouterModule.forRoot(routes)
  ],
  providers: [
    AuthenticationService,
    CanActivateAuthGuard,

    TestService,
    TestResolve,
    StompService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }