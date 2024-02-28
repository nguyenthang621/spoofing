import { Component, OnInit, OnDestroy } from '@angular/core';
import { webSocket } from 'rxjs/webSocket';
import { filter, map } from 'rxjs/operators';
import { SERVER_API_URL, BUILD_TIMESTAMP } from 'app/app.constants';
// formly config
import * as jsyaml from 'js-yaml';
import { FormlyFieldConfig } from '@ngx-formly/core';
import { FormGroup } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
// lodash
import * as _ from 'lodash';
import * as moment from 'moment';

@Component({
  selector: 'jhi-tracing',
  templateUrl: './tracing.component.html'
})
export class TracingComponent implements OnInit, OnDestroy {
  isReady = false;
  socket: any;
  connected = false;
  subscription: any;
  results: any = {};
  wsEndpoint = 'ws://' + window.location.host + '/ws';
  wsTopic = 'tracing';
  wsSocket: any;
  flag: any;
  fields: FormlyFieldConfig[] = [];
  _ = _;
  tracing = false;
  session = '';
  smsLog: any = {};
  model: any = {};

  options: any = {
    formState: {
      mainModel: this.model
    }
  };
  searchForm = new FormGroup({});
  apiEndpoint = 'api/tracing';

  constructor(private httpClient: HttpClient) {}

  ngOnInit(): void {
    this.httpClient
      .get(`assets/spoofing-management/tracing.yaml?ts=` + BUILD_TIMESTAMP, { responseType: 'text', observe: 'response' })
      .pipe(
        filter(res => res.ok),
        map(res => jsyaml.load(res.body || ''))
      )
      .subscribe(res => {
        this.fields = res.fields || [];
        this.isReady = true;
      });
  }

  ngOnDestroy(): void {
    this.clearSession();

    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  connect(endpoint: string): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }

    this.wsSocket = webSocket(endpoint);
    this.subscription = this.wsSocket.subscribe(
      (msg: any) => this.filterMsg(msg)
      // err => console.log(err), // Called if at any point WebSocket API signals some kind of error.
      // () => console.log('complete') // Called when connection is closed (for whatever reason).
    );
    // this.socket = new WebSocket(this.wsEndpoint);
    // this.socket.onopen = (event) => this.connected = true;
    // this.socket.onmessage = (event) => this.msg.push(event.data);
    // this.socket.onerror = (event) => console.log(event);
    // this.socket.onclose = (event) => this.connected = false;
  }

  filterMsg(log: any): void {
    if (log.message) {
      const msg = _.get(JSON.parse(_.get(log, 'message')), 'message', '');
      if (!this.model.session || !msg.startsWith(this.model.session)) {
        return;
      }
      // eslint-disable-next-line no-console
      console.log('msg', msg);
      const traceID = msg.split('#')[0];
      const messageID = msg.split('#')[1];
      const timestamp = moment(msg.split('#')[2], 'x').format('YYYY-MM-DD HH:mm:ss.SSS');
      const stage = msg.split('#')[3];
      this.results[messageID] = { traceID, messageID, timestamp, stage };
    }
    //
    //   // filter same flag to array
    //   if (!this.results[messageID]) this.results[messageID] = [];
    //   if (stage.startsWith('Submit_SM:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'spSubmitSm' });
    //   if (stage.startsWith('Submit_SM_Response:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'gwSubmitSmResp' });
    //   if (stage.startsWith('Submit_SM to SMSC:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'gwSubmitSm' });
    //   if (stage.startsWith('Submit_SM_Response from SMSC:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'fdaSubmitSmResp' });
    //   if (stage.startsWith('Submit_SM_Response to CP:')) {
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'fdaSubmitSmResp' });
    //   }
    //   if (stage.startsWith('Deliver_SM:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'fdaDeliverSm' });
    //   if (stage.startsWith('Deliver_SM to CP:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'gwDeliverSm' });
    //   if (stage.startsWith('Deliver_SM_Response from CP:'))
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'spDeliverSmResp' });
    //   if (stage.startsWith('Deliver_SM_Response to SMSC:')) {
    //     this.results[messageID] = this.results[messageID].concat({ timestamp: timestamp, stage: stage, detail: 'spDeliverSmResp' });
    //   }
    // } else if (log.meta != null) {
    //   console.log(log);
    //   if (!this.smsLog[log.id]) this.smsLog[log.id] = {};
    //   this.smsLog[log.id] = _.assign(this.smsLog[log.id], log);
    // }
    //
    // if (_.keys(this.results).length >= 5) {
    //   this.clearSession();
    // }
  }

  // + FIXME: Need a detail endpoint on this
  startTracing(): void {
    this.tracing = true;
    this.httpClient
      .post(SERVER_API_URL + '/api/trace', this.model, { observe: 'response' })
      .pipe(
        filter(res => res.ok),
        map(res => res.body)
      )
      .subscribe(res => {
        this.session = _.get(res, 'session');
        this.model = res;
        this.connect(this.wsEndpoint + '-' + this.model.site);
      });
  }

  // + FIXME: Need a detail endpoint on this
  clearSession(): void {
    this.httpClient.delete(SERVER_API_URL + `/api/trace/${this.session}`).subscribe(() => (this.model = {}));
  }
}
