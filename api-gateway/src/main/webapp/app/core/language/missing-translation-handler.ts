import { MissingTranslationHandler, MissingTranslationHandlerParams } from '@ngx-translate/core';
import * as _ from 'lodash';

export class TranslationHandler implements MissingTranslationHandler {
  i18n: any = {};
  constructor() {}
  handle(params: MissingTranslationHandlerParams): string {
    const trans = _.startCase(_.last(params.key.split('.')));
    _.set(this.i18n, params.key, trans);
    window.localStorage.setItem('missingTranslation', JSON.stringify(this.i18n));
    return trans;
  }
}
