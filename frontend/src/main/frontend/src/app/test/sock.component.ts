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
  publicLobby: any;
  privateLobby: any;

  isConnected: boolean = false;
  isSubscribed: boolean = false;
  showSubButton: boolean = false;
  showUnsubButton: boolean = false;

  text: any;
  messages: Array<TestMessage> = new Array<TestMessage>();

  constructor(
    private authentication: AuthenticationService,
    private route: ActivatedRoute,
    private router: Router
  ) {}
  
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
      that.subscribe();
    }, function (err) {
      console.log('err', err);
    });
  }

  subscribe() {
    var that = this;
    this.publicLobby = this.stompClient.subscribe('/topic/greetings', function(message) {
      console.log("In");
      that.messages.push(JSON.parse(message.body));
    });
    this.privateLobby = this.stompClient.subscribe("/user/queue/reply", function(message) {
      that.messages.push(JSON.parse(message.body));
    });
    this.isSubscribed = true;
    this.showSubButtons();
  }

  unsubscribe() {
    this.publicLobby.unsubscribe();
    this.privateLobby.unsubscribe();
    this.isSubscribed = false;
    this.showSubButtons();
  }

  send() {
    let message:TestMessage = {text : this.text};
    this.stompClient.send('/app/hello', {}, JSON.stringify(message));
  }

  disconnect() {
    //TODO event listener for refresh, close, page change so that the socket could be closed correctly.
    this.stompClient.disconnect();
    this.isConnected = false;
    this.isSubscribed = false;
    this.showSubButtons();
  }

  showSubButtons() {
    this.showSubButton = this.isConnected && !this.isSubscribed;
    this.showUnsubButton = this.isConnected && this.isSubscribed;
  }

}