import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Auth } from './auth';
import { Router } from '@angular/router';
import { ProductList } from './product-list/product-list';
import { OrderForm } from './order-form/order-form';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ProductList, OrderForm],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  constructor(private auth: Auth, private router: Router) {}

  logout() {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  isAuthenticated() {
    return this.auth.isAuthenticated();
  }
  protected readonly title = signal('frontend');
}
