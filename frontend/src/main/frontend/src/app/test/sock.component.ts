import { Component } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";
import { TestMessage } from "app/definitions";
import { AuthenticationService } from "app/authentication/authentication.service";
import { environment } from "environments/environment";


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
  //TODO place socket(s) in a separate service?
  //TODO Keep track of subscriptions so you could resubscribe after refresh or relogin?
  //TODO Maybe ask that info from the server instead...
  connect() {
    var that = this;
    this.messages = new Array<TestMessage>();
    var socket = new SockJS(environment.BASE_URL + '/ws?jwt=' + this.authentication.getToken());
    this.stompClient = Stomp.over(socket);
    this.stompClient.debug = null;
    
    this.stompClient.connect({}, function (frame) {
      that.isConnected = true;
      that.stompClient.subscribe('/topic/greetings', function(message) {
        that.messages.push(JSON.parse(message.body));
      });
      that.stompClient.subscribe("/user/queue/reply", function(message) {
        that.messages.push(JSON.parse(message.body));
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