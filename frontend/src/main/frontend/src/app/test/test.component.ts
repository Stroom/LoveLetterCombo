import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: 'test-component',
  templateUrl: './test.component.html'
})
export class TestComponent implements OnInit {
  title: string = "app";
  text: string = "";

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit() {
    this.route.data
    .subscribe((data: {text: string}) => {
      this.text = data.text;
    },
    err => {
      console.log("err");
      this.router.navigateByUrl('home');
    });
  }
  
}