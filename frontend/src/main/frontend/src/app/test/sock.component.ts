import { Component } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { TestMessage } from "app/definitions";
import { AuthenticationService } from "app/authentication/authentication.service";


var SockJS = require('sockjs-client');
var Stomp = require('stompjs');

@Component({
  selector: 'socket-component',
  templateUrl: './sock.component.html'
})
export class SockComponent {
  stompClient: any;

  isConnected: boolean = false;
  text: any;
  messages: Array<TestMessage> = new Array<TestMessage>();

  constructor(
    private authentication: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}
  
  send() {
    let message:TestMessage = {text : this.text};
    this.stompClient.send('/app/hello', {}, JSON.stringify(message));
  }

  connect() {
    var that = this;
    this.messages = new Array<TestMessage>();
    var socket = new SockJS('http://localhost:8080/ws?jwt=' + this.authentication.getToken());
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = null;
    
    this.stompClient.connect({}, function (frame) {
      that.isConnected = true;
      that.stompClient.subscribe('/topic/greetings', function(greeting) {
        that.messages.push(JSON.parse(greeting.body));
      });
    }, function (err) {
      console.log('err', err);
    });
  }

  disconnect() {
    this.stompClient.disconnect();
    this.isConnected = false;
  }

}