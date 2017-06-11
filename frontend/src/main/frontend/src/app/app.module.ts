import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { TestComponent } from "app/test/test.component";
import { HomeComponent } from "app/home.component";
import { TestResolve } from "app/test/test.resolve";
import { SockComponent } from "app/test/sock.component";

import { StompService } from 'ng2-stomp-service';

const routes: Routes = [
  {
    path: 'home',
    component: HomeComponent
  },
  /*{
    path: 'login',
    component: AuthenticationComponent
  },*/
  {
    path: '',
    redirectTo: 'home',
    pathMatch: 'full'
  },
  {
    path: 'test',
    component: TestComponent,
    resolve: { text: TestResolve }
  },
  {
    path: 'websocket',
    component: SockComponent
  }
  //TODO add default error page.
];

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
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
    TestResolve,
    StompService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
