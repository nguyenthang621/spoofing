import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { JhiLanguageService } from 'ng-jhipster';
import { SessionStorageService } from 'ngx-webstorage';

import { VERSION } from 'app/app.constants';
import { LANGUAGES } from 'app/core/language/language.constants';
import { AccountService } from 'app/core/auth/account.service';
import { LoginService } from 'app/core/login/login.service';
import { ProfileService } from 'app/layouts/profiles/profile.service';

// + HttpClient
import { JhiEventManager } from 'ng-jhipster';
import { HttpClient } from '@angular/common/http';
import { SERVER_API_URL, BUILD_TIMESTAMP } from 'app/app.constants';
import { filter, map, tap } from 'rxjs/operators';
import * as jsyaml from 'js-yaml';
import * as _ from 'lodash';

@Component({
  selector: 'jhi-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['navbar.scss']
})
export class NavbarComponent implements OnInit {
  inProduction?: boolean;
  isNavbarCollapsed = true;
  languages = LANGUAGES;
  swaggerEnabled?: boolean;
  version: string;
  // + Extra menu items
  _ = _;
  public navItems: any;
  public menuItems: any;

  constructor(
    private httpClient: HttpClient,
    private eventManager: JhiEventManager,
    // + jhipster
    private loginService: LoginService,
    private languageService: JhiLanguageService,
    private sessionStorage: SessionStorageService,
    private accountService: AccountService,
    private profileService: ProfileService,
    private router: Router
  ) {
    this.version = VERSION ? (VERSION.toLowerCase().startsWith('v') ? VERSION : 'v' + VERSION) : '';
  }

  ngOnInit(): void {
    this.profileService.getProfileInfo().subscribe(profileInfo => {
      this.inProduction = profileInfo.inProduction;
      this.swaggerEnabled = profileInfo.swaggerEnabled;
    });
    this.accountService
      .identity()
      .pipe(filter(account => account !== null))
      .subscribe(() => this.loadExtraMenu());
    this.eventManager.subscribe('authenticationSuccess', () => this.loadExtraMenu());
  }

  changeLanguage(languageKey: string): void {
    this.sessionStorage.store('locale', languageKey);
    this.languageService.changeLanguage(languageKey);
  }

  collapseNavbar(): void {
    this.isNavbarCollapsed = true;
  }

  isAuthenticated(): boolean {
    return this.accountService.isAuthenticated();
  }

  logout(): void {
    this.collapseNavbar();
    this.loginService.logout();
    this.menuItems = [];
    this.router.navigate(['']);
  }

  toggleNavbar(): void {
    this.isNavbarCollapsed = !this.isNavbarCollapsed;
  }

  getImageUrl(): string {
    return this.isAuthenticated() ? this.accountService.getImageUrl() : '';
  }
  getDisplayName(): string {
    return this.isAuthenticated() ? this.accountService.getDisplayName() : '';
  }

  // + Load Extra Menu
  loadExtraMenu(): void {
    // Retrieve the navbar
    this.httpClient
      .get(SERVER_API_URL + 'assets/config/navbar.yml' + `?ts=${BUILD_TIMESTAMP}`, { responseType: 'text' })
      .subscribe(res => (this.menuItems = jsyaml.load(res)));
    // retrieve the default sidebar
    this.loadSidebarItems('assets/config/sidebar.yml');
  }

  // + Download the sidebar if need
  downloadSidebarFile(file: any): void {
    this.router.navigate([file.url]).then(() => {
      if (file.sidebarUrl) {
        this.loadSidebarItems(file.sidebarUrl);
      }
    });
  }
  loadSidebarItems(url: string): void {
    this.httpClient
      .get(SERVER_API_URL + url + `?ts=${BUILD_TIMESTAMP}`, { responseType: 'text' })
      .pipe(
        map(res => jsyaml.load(res)),
        tap(menuItems => this.sessionStorage.store('sidebarMenuItems', menuItems))
      )
      .subscribe(() => this.eventManager.broadcast('reloadSidebar'));
  }
}
