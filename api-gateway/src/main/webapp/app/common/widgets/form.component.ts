import { Component, Input, OnInit, OnDestroy } from '@angular/core';
// + Formly Support
import { FormGroup } from '@angular/forms';
import { FormlyFieldConfig } from '@ngx-formly/core';

// + HTTP support
import { HttpClient, HttpResponse } from '@angular/common/http';
// + look for anything
import * as _ from 'lodash';
import * as jsyaml from 'js-yaml';

@Component({
  selector: 'jhi-yaml-form',
  template: `
    <formly-form [model]="model" [options]="options" [fields]="formFields" [form]="form"></formly-form>
  `
})
export class JhiYamlFormComponent implements OnInit, OnDestroy {
  // Abs path for the YAML
  @Input() src = '';
  @Input() model: any = {};
  @Input() form: FormGroup = new FormGroup({});
  @Input() options: any = {};
  formFields: FormlyFieldConfig[] = [];
  // + apiEndpoint and params
  apiEndpoint = '';
  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    // FIXME: Load existings value from formControl to populate into of
    this.httpClient
      .get(this.src + '?ts=' + new Date().getTime(), { responseType: 'text', observe: 'response' })
      .subscribe((res: HttpResponse<any>) => {
        this.formFields = _.get(jsyaml.load(res.body), 'fields', []);
        // + apiEndpoint and params
        this.apiEndpoint = _.get(res.body, 'apiEndpoint', res.body.apiEndpoint);
      });
  }

  ngOnDestroy(): void {}
}
