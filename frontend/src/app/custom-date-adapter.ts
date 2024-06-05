import {Injectable} from '@angular/core';
import {NativeDateAdapter} from '@angular/material/core';

@Injectable({
  providedIn: 'root'
})
export class CustomDateAdapter extends NativeDateAdapter {
  override format(date: Date, displayFormat: Object): string {
    if (displayFormat === 'input') {
      const day = date.getDate();
      const month = date.getMonth() + 1;
      const year = date.getFullYear();
      return `${year}-${this._to2digit(month)}-${this._to2digit(day)}`;
    } else {
      return date.toDateString();
    }
  }

  private _to2digit(n: number): string {
    return ('00' + n).slice(-2);
  }
}
