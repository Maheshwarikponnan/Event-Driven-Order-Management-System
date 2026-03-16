import { Component, OnInit } from '@angular/core';
import { Api } from '../api';

@Component({
  selector: 'app-product-list',
  imports: [],
  templateUrl: './product-list.html',
  styleUrl: './product-list.scss',
})
export class ProductList implements OnInit {
  products: any[] = [];

  constructor(private api: Api) {}

  ngOnInit() {
    this.api.getProducts().subscribe(data => this.products = data);
  }
}
