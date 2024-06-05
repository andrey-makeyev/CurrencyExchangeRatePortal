import {NgModule, LOCALE_ID} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {RouterModule} from '@angular/router';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatTableModule} from '@angular/material/table';
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatSortModule} from '@angular/material/sort';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatSelectModule} from '@angular/material/select';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatNativeDateModule} from '@angular/material/core';
import {MatOptionModule} from '@angular/material/core';
import {MatCardModule} from '@angular/material/card';
import {MatTooltipModule} from '@angular/material/tooltip';

import {MatListModule} from '@angular/material/list';
import {DatePipe, registerLocaleData} from '@angular/common';
import localeEn from '@angular/common/locales/en';
import {DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE} from '@angular/material/core';
import {CommonModule} from '@angular/common';

import {AppComponent} from './app.component';
import {ExchangeRatesComponent} from './components/currency/exchange-rates/exchange-rates.component';
import {HomeComponent} from './components/currency/home/home.component';
import {SpinnerComponent} from "./components/spinner/spinner.component";

import {CustomDateAdapter} from './custom-date-adapter';
import {BaseChartDirective} from 'ng2-charts';

import {LoadingService} from "./services/loader/loading.service";
import {routes} from './app.routes';
import {MatProgressSpinner} from "@angular/material/progress-spinner";


registerLocaleData(localeEn);

const MY_DATE_FORMATS = {
  parse: {
    dateInput: 'yyyy-MM-dd',
  },
  display: {
    dateInput: 'yyyy-MM-dd',
    monthYearLabel: 'MMM yyyy',
    dateA11yLabel: 'LL',
    monthYearA11yLabel: 'MMMM yyyy',
  }
};

@NgModule({
  declarations: [
    AppComponent,
    ExchangeRatesComponent,
    HomeComponent,
    SpinnerComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatInputModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatSelectModule,
    MatToolbarModule,
    MatButtonModule,
    MatListModule,
    MatNativeDateModule,
    MatOptionModule,
    MatCardModule,
    MatTooltipModule,
    FormsModule,
    RouterModule.forRoot(routes),
    CommonModule,
    ReactiveFormsModule,
    BaseChartDirective,
    MatProgressSpinner
  ],
  providers: [
    DatePipe,
    {provide: LOCALE_ID, useValue: 'en-US'},
    {provide: DateAdapter, useClass: CustomDateAdapter},
    {provide: MAT_DATE_FORMATS, useValue: MY_DATE_FORMATS},
    LoadingService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
