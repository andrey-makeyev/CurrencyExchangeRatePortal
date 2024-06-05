import {Routes} from '@angular/router';
import {ExchangeRatesComponent} from "./components/currency/exchange-rates/exchange-rates.component";
import {HomeComponent} from './components/currency/home/home.component';

export const routes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'exchange-rates', component: ExchangeRatesComponent},
  {path: '**', redirectTo: ''}
];
