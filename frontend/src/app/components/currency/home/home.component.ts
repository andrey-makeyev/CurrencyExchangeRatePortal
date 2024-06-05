import {Component, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {HttpClient} from '@angular/common/http';
import {LoadingService} from "../../../services/loader/loading.service";
import {trigger, transition, style, animate} from '@angular/animations';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({opacity: 0}),
        animate('500ms ease-in', style({opacity: 1}))
      ])
    ])
  ]
})
export class HomeComponent implements OnInit {
  popularCurrencies: { name: string, rate: number }[] = [];
  lastUpdate: string | undefined;
  isLoading = false;

  calculatorForm: FormGroup;
  result: number | null = null;
  currencies: any[] = [];
  errorMessage: string | null = null;

  constructor(
    private http: HttpClient,
    private loadingService: LoadingService,
    private fb: FormBuilder
  ) {
    this.calculatorForm = this.fb.group({
      fromCurrency: ['EUR', Validators.required],
      toCurrency: ['USD', Validators.required],
      amount: [100, Validators.required],
      result: [{value: '', disabled: true}]
    });
  }

  ngOnInit(): void {
    this.fetchData();
    this.loadAvailableCurrencies();
  }

  fetchData(): void {
    this.isLoading = true;
    this.loadingService.show();
    this.http.get<any>('/api/last-update').subscribe(data => {
      data.lastUpdate = new Date().toISOString().slice(0, 10);
      this.lastUpdate = data.lastUpdate;
      this.loadingService.hide();
    }, () => {
      this.loadingService.hide();
    });

    this.http.get<any>('/api/fx-rate/current-exchange-rates/LT').subscribe(data => {
      const fxRates = data._embedded.fxRateDTOList;
      this.popularCurrencies = [
        {name: 'USD', rate: this.getRateByCurrency(fxRates, 'USD')},
        {name: 'JPY', rate: this.getRateByCurrency(fxRates, 'JPY')},
        {name: 'GBP', rate: this.getRateByCurrency(fxRates, 'GBP')},
        {name: 'AUD', rate: this.getRateByCurrency(fxRates, 'AUD')},
        {name: 'CAD', rate: this.getRateByCurrency(fxRates, 'CAD')},
        {name: 'CHF', rate: this.getRateByCurrency(fxRates, 'CHF')},
        {name: 'CNY', rate: this.getRateByCurrency(fxRates, 'CNY')}
      ];
      this.isLoading = false;
      this.loadingService.hide();
    }, () => {
      this.isLoading = false;
      this.loadingService.hide();
    });
  }

  getRateByCurrency(fxRates: any[], targetCurrency: string): number {
    const currency = fxRates.find(rate => rate.currencyAmounts.some((c: {
      targetCurrency: string;
    }) => c.targetCurrency === targetCurrency));
    return currency ? currency.rate : undefined;
  }

  loadAvailableCurrencies(): void {
    this.loadingService.show();
    this.http.get('/api/fx-rate/available-currency-list')
      .subscribe((data: any) => {
        console.log('Received available currencies:', data);
        this.currencies = data._embedded?.ccyDTOList || [];
        this.loadingService.hide();
      }, error => {
        console.error('Error loading available currencies:', error);
        this.loadingService.hide();
      });
  }

  onSubmit(): void {
    if (this.calculatorForm.invalid) {
      alert('Please fill in all fields');
      return;
    }

    const {fromCurrency, toCurrency, amount} = this.calculatorForm.value;
    this.isLoading = true;
    this.errorMessage = null;

    this.loadingService.show();
    this.http.get(`/api/fx-rate/cross-rate/${fromCurrency}/${toCurrency}`)
      .subscribe((data: any) => {
        this.result = data;
        this.isLoading = false;
        this.loadingService.hide();
        if (this.result != null) {
          this.calculatorForm.get('result')?.setValue((this.result * amount).toFixed(5));
        }
      }, error => {
        console.error('Error converting currency:', error);
        this.result = null;
        this.isLoading = false;
        this.loadingService.hide();
        if (error.status === 404) {
          this.errorMessage = 'Currency data not found.';
        } else {
          this.errorMessage = 'Unknown error.';
        }
      });
  }

  resetForm(): void {
    this.calculatorForm.reset({
      amount: 100,
      result: ''
    });
    this.result = null;
    this.errorMessage = null;
  }
}
