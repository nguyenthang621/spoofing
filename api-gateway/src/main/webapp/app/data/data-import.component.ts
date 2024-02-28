import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse, HttpClient } from '@angular/common/http';
import { SERVER_API_URL } from 'app/app.constants';
import { JhiDataUtils, JhiAlertService } from 'ng-jhipster';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-data-import',
  templateUrl: './data-import.component.html'
})
export class DataImportComponent implements OnInit {
  dataFile: any = {};
  jsonString = '';
  jsonData: any[] = [];
  isSaving = false;
  apiEndpoint = '';
  responseType = 'json';

  constructor(
    private activatedRoute: ActivatedRoute,
    private alertService: JhiAlertService,
    private dataUtils: JhiDataUtils,
    private httpClient: HttpClient
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ templateFile }) => {
      this.apiEndpoint = _.get(templateFile, 'config.importApiEndpoint', 'api/import/nodes');
      this.responseType = _.get(templateFile, 'config.importApiResponseType', 'json');
    });
  }

  setFileData(event: any): void {
    const file: File = event.target.files[0];
    const fileReader: FileReader = new FileReader();
    fileReader.onload = () => (this.jsonString = fileReader.result as string);
    fileReader.readAsText(file);
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    if (this.responseType === 'json') {
      this.jsonData = JSON.parse(this.jsonString);
      this.httpClient.post(SERVER_API_URL + this.apiEndpoint, this.jsonData, { observe: 'response', responseType: 'json' }).subscribe(
        () => this.onSaveSuccess(),
        (res: HttpErrorResponse) => this.onSaveError(res)
      );
    } else if (this.responseType === 'blob') {
      // blob
      this.httpClient.post(SERVER_API_URL + this.apiEndpoint, this.jsonString, { observe: 'response', responseType: 'blob' }).subscribe(
        () => this.onSaveSuccess(),
        (res: HttpErrorResponse) => this.onSaveError(res)
      );
    }
  }

  protected onSaveSuccess(): void {
    this.isSaving = false;
    this.alertService.success('xlsx-import.accepted');
  }

  protected onSaveError(error: any): void {
    this.isSaving = false;
    this.alertService.error(error.error, error.message);
  }
}
