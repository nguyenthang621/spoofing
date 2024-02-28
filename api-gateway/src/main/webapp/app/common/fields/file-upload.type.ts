import { Component } from '@angular/core';
import { FieldType } from '@ngx-formly/core';
// + HttpClient
import { HttpClient } from '@angular/common/http';
import { SERVER_API_URL } from 'app/app.constants';
import * as _ from 'lodash';
import { SafePipe } from 'app/common/util/safe.pipe';

@Component({
  selector: 'jhi-formly-file-upload',
  template: `
    <input type="file" (change)="addFile($event)" class="custom-input" />
    <div class="file-viewer" *ngIf="to.template" [innerHtml]="getTemplate()"></div>
    <div class="card-deck" *ngIf="!to.template && formControl.value" style="margin-bottom: 5px">
      <div class="card">
        <button type="button" (click)="removeFile()" class="btn btn-danger btn-block">
          <fa-icon icon="times"></fa-icon>
          Remove
        </button>
        <a [href]="getFileSrc()" [class]="to.className" target="_blank" *ngIf="!to.isImage">{{ formControl.value }}</a>
        <img [src]="getFileSrc()" [class]="to.className" *ngIf="to.isImage" />
      </div>
    </div>
  `,
  providers: [SafePipe]
})
export class FormlyFileUploadComponent extends FieldType {
  fileToUpload: any;

  constructor(private pipe: SafePipe, private httpClient: HttpClient) {
    super();
  }

  // API Endpoint for Retrieve File
  getFileSrc(): string {
    return this.to.getFileSrc
      ? this.to.getFileSrc()
      : this.to.fileSrc
      ? this.to.fileSrc.replace('${fileId}', this.formControl.value)
      : SERVER_API_URL + _.get(this.to, 'apiEndpoint', 'api/public/upload') + `/${this.formControl.value}`;
  }

  removeFile(): void {
    this.httpClient.delete(SERVER_API_URL + _.get(this.to, 'apiEndpoint', 'api/upload') + `/${this.formControl.value}`).subscribe(
      () => this.formControl.setValue(null),
      () => this.formControl.setValue(null)
    );
  }

  // Upload a file to server, return the URL
  addFile(event: any): void {
    this.fileToUpload = event.target.files.item(0);
    this.uploadFile();
  }

  // API Endpoint for Upload File
  protected uploadFile(): void {
    const formData = new FormData();
    formData.append('file', this.fileToUpload, this.fileToUpload.name);
    this.httpClient
      .post(SERVER_API_URL + _.get(this.to, 'apiEndpoint', 'api/upload'), formData, { responseType: 'text' })
      .subscribe(res => this.formControl.setValue(res));
  }
  getTemplate(): any {
    return this.pipe.transform(this.to.template.replace('${fileId}', this.formControl.value));
  }
}
