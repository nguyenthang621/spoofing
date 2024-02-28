import { HttpParams } from '@angular/common/http';
import * as _ from 'lodash';

export interface Pagination {
  page: number;
  size: number;
  sort: string[];
}

export interface Search {
  query: string;
}

export interface SearchWithPagination extends Search, Pagination {}

export const createRequestOption = (req?: any): HttpParams => {
  let options: HttpParams = new HttpParams();

  if (req) {
    _.each(req, (val, key) => {
      if (key !== 'sort') {
        if (_.isArray(val)) {
          _.each(val, v => (options = options.append(key, v)));
        } else {
          options = options.set(key, req[key]);
        }
      }
    });

    if (req.sort) {
      req.sort.forEach((val: string) => {
        options = options.append('sort', val);
      });
    }
  }

  return options;
};
