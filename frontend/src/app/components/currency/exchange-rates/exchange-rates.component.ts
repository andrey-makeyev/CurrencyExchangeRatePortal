import {Component, OnInit, AfterViewInit, ViewChild} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {FormBuilder, FormGroup} from '@angular/forms';
import {DatePipe} from '@angular/common';
import {trigger, transition, style, animate} from '@angular/animations';
import {Chart, ChartData, ChartOptions, registerables} from 'chart.js';
import 'chartjs-adapter-date-fns';
import {enUS} from 'date-fns/locale';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table';
import {LoadingService} from "../../../services/loader/loading.service";

Chart.register(...registerables);

interface FxRate {
  date: string;
  rate: number;
}

@Component({
  selector: 'app-exchange-rates',
  templateUrl: './exchange-rates.component.html',
  styleUrls: ['./exchange-rates.component.css'],
  animations: [
    trigger('fadeIn', [
      transition(':enter', [
        style({opacity: 0}),
        animate('500ms', style({opacity: 1}))
      ])
    ])
  ]
})
export class ExchangeRatesComponent implements OnInit, AfterViewInit {
  displayedColumns: string[] = ['date', 'proportion', 'change'];
  dataSource = new MatTableDataSource<any>([]);
  rateHistoryForm: FormGroup;
  currencies: any[] = [];
  allData: any[] = [];
  chart: Chart | undefined;
  chartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        label: 'Currency Value',
        data: [],
        borderColor: 'rgba(0, 255, 0, 1)',
        backgroundColor: 'rgba(0, 0, 255, 1)',
        fill: false,
        tension: 0.1,
        pointBackgroundColor: 'rgba(0, 255, 0, 1)',
        showLine: true
      }
    ]
  };
  chartOptions: ChartOptions<'line'> = {
    responsive: true,
    scales: {
      x: {
        type: 'time',
        time: {
          unit: 'year',
          displayFormats: {
            day: 'yyyy'
          }
        },
        adapters: {
          date: {
            locale: enUS,
          }
        }
      },
      y: {
        ticks: {
          stepSize: 0.05
        }
      }
    }
  };
  chartType: 'line' = 'line';
  minDate: Date | null = null;
  maxDate: Date | null = null;
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private datePipe: DatePipe,
    private loadingService: LoadingService
  ) {
    this.rateHistoryForm = this.fb.group({
      fromDate: [null],
      toDate: [null],
      currency: ['']
    });
  }

  ngOnInit(): void {
    this.loadCurrencies();
    this.setMinMaxDate();
    this.setDefaultCurrency();
  }

  ngAfterViewInit(): void {
    if (document.querySelector('canvas')) {
      this.initChart();
    }

    if (this.paginator) {
      setTimeout(() => {
        this.dataSource.paginator = this.paginator;
        this.updatePaginatorLength();
      });

      this.paginator.page.subscribe((event) => {
        this.updateTable(event);
      });
    }
  }

  updatePaginatorLength(): void {
    if (this.paginator && this.dataSource.data) {
      this.paginator.length = this.allData.length;
    }
  }

  setDefaultCurrency(): void {
    this.rateHistoryForm.get('currency')?.setValue('USD');
  }

  loadCurrencies(): void {
    this.isLoading = true;
    this.loadingService.show();
    this.http.get('/api/fx-rate/available-currency-list')
      .subscribe((data: any) => {
        this.currencies = data._embedded?.ccyDTOList || [];
        this.isLoading = false;
        this.loadingService.hide();
      }, error => {
        console.error('Error loading currencies:', error);
        this.isLoading = false;
        this.loadingService.hide();
      });
  }

  setMinMaxDate(): void {
    const minDate = new Date('2015-01-01');
    const maxDate = new Date();
    this.rateHistoryForm.get('fromDate')?.setValue(minDate);
    this.rateHistoryForm.get('toDate')?.setValue(maxDate);
  }

  paginatorLength: number = 0;

  onSubmit(): void {
    if (!this.rateHistoryForm.valid) {
      alert('Please fill in all fields');
      return;
    }

    const {fromDate, toDate, currency} = this.rateHistoryForm.value;
    const formattedFromDate = this.datePipe.transform(fromDate, 'yyyy-MM-dd');
    const formattedToDate = this.datePipe.transform(toDate, 'yyyy-MM-dd');

    const exchangeRateType = 'EU';

    this.dataSource.data = [];
    this.allData = [];

    this.isLoading = true;
    this.loadingService.show();
    this.http.get(`/api/fx-rate/exchange-rates/${exchangeRateType}/${currency}/${formattedFromDate}/${formattedToDate}`)
      .subscribe((data: any) => {
        const fxRates: FxRate[] = data._embedded?.fxRateDTOList || [];
        this.allData = fxRates.map((rate: FxRate, index: number) => {
          const date = this.datePipe.transform(rate.date, 'yyyy-MM-dd');
          const proportion = rate.rate;
          let change = 0;
          let changePercent = 0;

          if (index > 0) {
            const previousRate = fxRates[index - 1].rate;
            change = proportion - previousRate;
            changePercent = (change / previousRate) * 100;
          }

          return {
            date,
            proportion,
            change: `${change.toFixed(4)} / ${changePercent.toFixed(4)} %`
          };
        });

        this.chartData.labels = this.allData.map(item => item.date);
        this.chartData.datasets[0].data = this.allData.map(item => item.proportion);
        this.updateChart();

        setTimeout(() => {
          if (this.paginator) {
            this.updateTable({pageIndex: 0, pageSize: this.paginator.pageSize});
          }
        });
        this.chartData.labels = this.allData.map(item => item.date);
        this.chartData.datasets[0].data = this.allData.map(item => item.proportion);
        this.updateChart();
        this.updatePaginatorLength();
        this.isLoading = false;
        this.loadingService.hide();
      }, error => {
        console.log('Error fetching data:', error);
        if (error.status === 400) {
          console.log('Fields are not filled correctly:', error.error.message);
        }
        this.dataSource.data = [];
        this.allData = [];
        this.isLoading = false;
        this.updatePaginatorLength();
        this.loadingService.hide();
      });
  }

  initChart(): void {
    const chartElement = document.querySelector('canvas') as HTMLCanvasElement;
    const ctx = chartElement.getContext('2d');
    if (ctx) {
      this.chart = new Chart(ctx, {
        data: this.chartData,
        options: this.chartOptions,
        type: this.chartType,
      });
    }
  }

  updateChart(): void {
    if (this.chart) {
      this.chart.update();
    }
  }

  updateTable(event: any): void {
    if (!this.paginator) return;
    const pageIndex = event.pageIndex;
    const pageSize = event.pageSize;
    const startIndex = pageIndex * pageSize;
    const endIndex = startIndex + pageSize;
    this.dataSource.data = this.allData.slice(startIndex, endIndex);
  }
}
