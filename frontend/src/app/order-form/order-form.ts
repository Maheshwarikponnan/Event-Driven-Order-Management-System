import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Api } from '../api';

@Component({
  selector: 'app-order-form',
  imports: [FormsModule],
  templateUrl: './order-form.html',
  styleUrl: './order-form.scss',
})
export class OrderForm {
  order = { userId: 1, orderItems: [{ productId: 1, quantity: 1 }] };

  constructor(private api: Api) {}

  onSubmit() {
    this.api.createOrder(this.order).subscribe(response => {
      console.log('Order created', response);
    });
  }
}
