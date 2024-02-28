import { NgModule, LOCALE_ID } from '@angular/core';
import { DatePipe, registerLocaleData } from '@angular/common';
import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { Title } from '@angular/platform-browser';
import { FaIconLibrary } from '@fortawesome/angular-fontawesome';
import { CookieModule } from 'ngx-cookie';
import { TranslateModule, TranslateLoader, MissingTranslationHandler } from '@ngx-translate/core';
import { NgxWebstorageModule } from 'ngx-webstorage';
import { NgJhipsterModule, JhiConfigService, JhiLanguageService } from 'ng-jhipster';
import locale from '@angular/common/locales/en';
import localeVi from '@angular/common/locales/vi';

import * as moment from 'moment';
import { NgbDateAdapter, NgbDatepickerConfig } from '@ng-bootstrap/ng-bootstrap';
import { NgbDateMomentAdapter } from 'app/shared/util/datepicker-adapter';

import { AuthInterceptor } from 'app/blocks/interceptor/auth.interceptor';
import { AuthExpiredInterceptor } from 'app/blocks/interceptor/auth-expired.interceptor';
import { ErrorHandlerInterceptor } from 'app/blocks/interceptor/errorhandler.interceptor';
import { NotificationInterceptor } from 'app/blocks/interceptor/notification.interceptor';

// import { fontAwesomeIcons } from './icons/font-awesome-icons';
import { fas } from '@fortawesome/free-solid-svg-icons';
// + customize translation handler
import { MultiTranslateHttpLoader } from 'ngx-translate-multi-http-loader';
export function translatePartialLoader(http: HttpClient): TranslateLoader {
  return new MultiTranslateHttpLoader(http, [
    { prefix: 'i18n/', suffix: `.json?buildTimestamp=${process.env.BUILD_TIMESTAMP}` },
    // + static files overload
    { prefix: 'assets/i18n/', suffix: `.json?buildTimestamp=${process.env.BUILD_TIMESTAMP}` },
    // + server side overload
    { prefix: '/api/public/static/', suffix: `.json?buildTimestamp=${process.env.BUILD_TIMESTAMP}` }
  ]);
}

import { TranslationHandler } from './language/missing-translation-handler';
export function missingTranslationHandler(): TranslationHandler {
  return new TranslationHandler();
}

@NgModule({
  imports: [
    HttpClientModule,
    CookieModule.forRoot(),
    NgxWebstorageModule.forRoot({ prefix: 'jhi', separator: '-' }),
    NgJhipsterModule.forRoot({
      // set below to true to make alerts look like toast
      alertAsToast: false,
      alertTimeout: 5000,
      i18nEnabled: true,
      defaultI18nLang: 'en'
    }),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: translatePartialLoader,
        deps: [HttpClient]
      },
      missingTranslationHandler: {
        provide: MissingTranslationHandler,
        useFactory: missingTranslationHandler,
        deps: [JhiConfigService]
      }
    })
  ],
  providers: [
    Title,
    {
      provide: LOCALE_ID,
      useValue: 'en'
    },
    { provide: NgbDateAdapter, useClass: NgbDateMomentAdapter },
    DatePipe,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthExpiredInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: ErrorHandlerInterceptor,
      multi: true
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: NotificationInterceptor,
      multi: true
    }
  ]
})
export class CoreModule {
  constructor(iconLibrary: FaIconLibrary, dpConfig: NgbDatepickerConfig, languageService: JhiLanguageService) {
    registerLocaleData(locale);
    registerLocaleData(localeVi);
    // iconLibrary.addIcons(...fontAwesomeIcons);
    iconLibrary.addIconPacks(fas);
    dpConfig.minDate = { year: moment().year() - 100, month: 1, day: 1 };
    languageService.init();
  }
}
