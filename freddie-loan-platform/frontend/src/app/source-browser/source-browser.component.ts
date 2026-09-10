import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export interface FileItem {
  name: string;
  isFolder: boolean;
  description: string;
  fileType?: string;
  size?: string;
  content?: string;
}

@Component({
  selector: 'app-source-browser',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './source-browser.component.html',
  styleUrls: ['./source-browser.component.css']
})
export class SourceBrowserComponent {
  systemName = 'Freddie Mac Mortgage Platform';
  
  availableRepos = [
    { repoName: 'freddie-loan-platform', systemName: 'Freddie Mac Mortgage Platform' },
    { repoName: 'nslrregui_ba0352_br3127', systemName: 'Unified Counterparty Management System' }
  ];
  
  selectedRepoIndex = 0;

  get repoName(): string {
    return this.availableRepos[this.selectedRepoIndex].repoName;
  }

  currentBranch = 'master';
  availableBranches = [
    'master',
    'main',
    'feature/loan-origination-v2',
    'release/freddie-1.0',
    'bugfix/underwriting-rules'
  ];

  isRepoDropdownOpen = false;
  isBranchDropdownOpen = false;
  isEllipsisMenuOpen = false;

  pathSegments: string[] = ['freddie-loan-platform', 'frontend', 'src', 'app'];

  // Directory Tree mapping for the active project
  directoryContents: { [pathKey: string]: FileItem[] } = {
    // Current Project: freddie-loan-platform
    'freddie-loan-platform/frontend/src/app': [
      { name: 'interceptors', isFolder: true, description: '' },
      { name: 'pipes', isFolder: true, description: '' },
      { name: 'services', isFolder: true, description: '' },
      { name: 'source-browser', isFolder: true, description: '' },
      { 
        name: 'app-routing.module.ts', 
        isFolder: false, 
        description: 'Update',
        fileType: 'ts',
        size: '1.2 KB',
        content: `import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }`
      },
      { 
        name: 'app.component.html', 
        isFolder: false, 
        description: 'Changing wording',
        fileType: 'html',
        size: '35.2 KB',
        content: `<!-- LOGGED IN WORKSPACE -->
<div class="dashboard-container">
  <aside class="sidebar">
    <div class="brand-section">
      <i class="fa-solid fa-house-chimney-crack brand-icon"></i>
      <h3>FreddieMac</h3>
    </div>
  </aside>
</div>`
      },
      { 
        name: 'app.component.scss', 
        isFolder: false, 
        description: 'First update commit for Angular UI setup for UCP',
        fileType: 'scss',
        size: '850 B',
        content: `.dashboard-container { display: flex; min-height: 100vh; }`
      },
      { 
        name: 'app.component.spec.ts', 
        isFolder: false, 
        description: 'Lego UX upgrade 15.6.0 and JEST test upgrade from Lego UX',
        fileType: 'ts',
        size: '922 B',
        content: `import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';

describe('AppComponent', () => {
  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });
});`
      },
      { 
        name: 'app.component.ts', 
        isFolder: false, 
        description: 'Initial Completion',
        fileType: 'ts',
        size: '10.5 KB',
        content: `import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'Freddie Mac Mortgage Portal';
  ngOnInit() {}
}`
      },
      { 
        name: 'app.module.ts', 
        isFolder: false, 
        description: '[UCOUNTGOLD-18977] - import multi select component',
        fileType: 'ts',
        size: '2.1 KB',
        content: `import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppComponent } from './app.component';

@NgModule({
  declarations: [AppComponent],
  imports: [BrowserModule],
  bootstrap: [AppComponent]
})
export class AppModule { }`
      }
    ],

    'freddie-loan-platform/frontend/src/app/services': [
      { 
        name: 'auth.service.ts', 
        isFolder: false, 
        description: 'OAuth2 JWT token authentication service',
        fileType: 'ts',
        size: '2.4 KB',
        content: `import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class AuthService {
  login(u: string, p: string) { return true; }
}`
      },
      { 
        name: 'loan.service.ts', 
        isFolder: false, 
        description: 'Loan application CRUD & GridFS upload dispatch',
        fileType: 'ts',
        size: '4.8 KB',
        content: `import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class LoanService {
  getApplications() { return []; }
}`
      }
    ],

    'freddie-loan-platform/frontend/src/app/pipes': [
      { 
        name: 'filter-status.pipe.ts', 
        isFolder: false, 
        description: 'Pure Angular pipe for loan status filtering',
        fileType: 'ts',
        size: '650 B',
        content: `import { Pipe, PipeTransform } from '@angular/core';

@Pipe({ name: 'filterStatus', standalone: true })
export class FilterStatusPipe implements PipeTransform {
  transform(items: any[], status: string) { return items ? items.filter(i => i.status === status) : []; }
}`
      }
    ],

    'freddie-loan-platform/frontend/src/app/interceptors': [
      { 
        name: 'auth.interceptor.ts', 
        isFolder: false, 
        description: 'HTTP Interceptor for Bearer token injection',
        fileType: 'ts',
        size: '1.1 KB',
        content: `import { HttpInterceptor } from '@angular/common/http';
export class AuthInterceptor {}`
      }
    ],

    'freddie-loan-platform/frontend/src/app/source-browser': [
      { name: 'source-browser.component.css', isFolder: false, description: 'Bitbucket / Lego UX source code browser styles', fileType: 'css', size: '4.8 KB' },
      { name: 'source-browser.component.html', isFolder: false, description: 'Repository file browser template matching UI screenshot', fileType: 'html', size: '3.6 KB' },
      { name: 'source-browser.component.ts', isFolder: false, description: 'Repository navigation state & branch selection controller', fileType: 'ts', size: '6.2 KB' }
    ],

    'freddie-loan-platform/frontend/src': [
      { name: 'app', isFolder: true, description: '[UCOUNTGOLD-18977] - import multi select component' },
      { name: 'assets', isFolder: true, description: 'Add Freddie Mac enterprise brand logos and icons' },
      { name: 'favicon.ico', isFolder: false, description: 'Initial favicon setup', fileType: 'ico', size: '4.2 KB' },
      { name: 'index.html', isFolder: false, description: 'Add Font Awesome icons and HTML5 document structure', fileType: 'html', size: '693 B' },
      { name: 'main.ts', isFolder: false, description: 'Angular application bootstrap entrypoint', fileType: 'ts', size: '250 B' },
      { name: 'styles.css', isFolder: false, description: 'Global UI theme tokens and component helper classes', fileType: 'css', size: '17.6 KB' }
    ],

    'freddie-loan-platform/frontend': [
      { name: 'src', isFolder: true, description: '[UCOUNTGOLD-18977] - import multi select component' },
      { name: '.gitignore', isFolder: false, description: 'Git exclusion rules for node_modules and dist', fileType: 'git', size: '7.0 KB' },
      { name: 'angular.json', isFolder: false, description: 'Angular CLI build and workspace config', fileType: 'json', size: '2.8 KB' },
      { name: 'package.json', isFolder: false, description: 'Dependencies update for Lego UX 15.6.0', fileType: 'json', size: '1.0 KB' },
      { name: 'tsconfig.json', isFolder: false, description: 'TypeScript compiler configuration target ES2022', fileType: 'json', size: '920 B' }
    ],

    'freddie-loan-platform': [
      { name: 'api-gateway', isFolder: true, description: 'Spring Cloud API Gateway service' },
      { name: 'auth-service', isFolder: true, description: 'OAuth2 / JWT Authentication microservice' },
      { name: 'customer-service', isFolder: true, description: 'Borrower and Customer CRM service' },
      { name: 'database', isFolder: true, description: 'MongoDB and PostgreSQL schema init scripts' },
      { name: 'deployment', isFolder: true, description: 'Docker compose and Kubernetes deployment manifests' },
      { name: 'document-service', isFolder: true, description: 'Reactive GridFS document management service' },
      { name: 'eureka-server', isFolder: true, description: 'Netflix Eureka Service Discovery server' },
      { name: 'frontend', isFolder: true, description: 'Angular 15 Single Page Application UI' },
      { name: 'loan-frontend-service', isFolder: true, description: 'BFF service for loan processing' },
      { name: 'loan-origination-service', isFolder: true, description: 'Loan application intake microservice' },
      { name: 'login-frontend-service', isFolder: true, description: 'Login authentication proxy service' },
      { name: 'rate-calculator-service', isFolder: true, description: 'Interest rate and EMI calculation engine' },
      { name: 'underwriting-service', isFolder: true, description: 'Automated underwriting & risk assessment engine' },
      { name: 'Freddie_Mac_Loan_Platform.postman_collection.json', isFolder: false, description: 'Postman API integration test collection', fileType: 'json', size: '9.2 KB' },
      { name: 'pom.xml', isFolder: false, description: 'Parent Maven POM for microservices', fileType: 'xml', size: '6.3 KB' },
      { name: 'README.md', isFolder: false, description: 'Enterprise Freddie Mac Loan Platform Documentation', fileType: 'md', size: '23.1 KB' }
    ],

    // Legacy / Mock UCP Module (nslrregui_ba0352_br3127)
    'nslrregui_ba0352_br3127/src/app': [
      { name: 'error', isFolder: true, description: '' },
      { name: 'Registration', isFolder: true, description: '' },
      { name: 'app-routing.module.ts', isFolder: false, description: 'Update', fileType: 'ts', size: '1.2 KB' },
      { name: 'app.component.html', isFolder: false, description: 'Changing wording', fileType: 'html', size: '2.4 KB' },
      { name: 'app.component.scss', isFolder: false, description: 'First update commit for Angular UI setup for UCP', fileType: 'scss', size: '850 B' },
      { name: 'app.component.spec.ts', isFolder: false, description: 'Lego UX upgrade 15.6.0 and JEST test upgrade from Lego UX', fileType: 'ts', size: '1.8 KB' },
      { name: 'app.component.ts', isFolder: false, description: 'Initial Completion', fileType: 'ts', size: '1.5 KB' },
      { name: 'app.module.ts', isFolder: false, description: '[UCOUNTGOLD-18977] - import multi select component', fileType: 'ts', size: '2.1 KB' }
    ]
  };

  selectedFile: FileItem | null = null;

  get currentPathKey(): string {
    return this.pathSegments.join('/');
  }

  get currentItems(): FileItem[] {
    return this.directoryContents[this.currentPathKey] || [];
  }

  get canNavigateUp(): boolean {
    return this.pathSegments.length > 1;
  }

  toggleRepoDropdown(event?: MouseEvent) {
    if (event) event.stopPropagation();
    this.isRepoDropdownOpen = !this.isRepoDropdownOpen;
    this.isBranchDropdownOpen = false;
    this.isEllipsisMenuOpen = false;
  }

  selectRepo(index: number) {
    this.selectedRepoIndex = index;
    const selected = this.availableRepos[index];
    this.systemName = selected.systemName;
    if (selected.repoName === 'freddie-loan-platform') {
      this.pathSegments = ['freddie-loan-platform', 'frontend', 'src', 'app'];
    } else {
      this.pathSegments = ['nslrregui_ba0352_br3127', 'src', 'app'];
    }
    this.isRepoDropdownOpen = false;
    this.selectedFile = null;
  }

  toggleBranchDropdown(event?: MouseEvent) {
    if (event) event.stopPropagation();
    this.isBranchDropdownOpen = !this.isBranchDropdownOpen;
    this.isRepoDropdownOpen = false;
    this.isEllipsisMenuOpen = false;
  }

  selectBranch(branch: string) {
    this.currentBranch = branch;
    this.isBranchDropdownOpen = false;
  }

  toggleEllipsisMenu(event?: MouseEvent) {
    if (event) event.stopPropagation();
    this.isEllipsisMenuOpen = !this.isEllipsisMenuOpen;
    this.isBranchDropdownOpen = false;
    this.isRepoDropdownOpen = false;
  }

  navigateToSegment(index: number) {
    this.pathSegments = this.pathSegments.slice(0, index + 1);
    this.selectedFile = null;
  }

  navigateUp() {
    if (this.canNavigateUp) {
      this.pathSegments.pop();
      this.selectedFile = null;
    }
  }

  onItemClick(item: FileItem) {
    if (item.isFolder) {
      this.pathSegments.push(item.name);
      this.selectedFile = null;
    } else {
      this.selectedFile = item;
    }
  }

  closeFileViewer() {
    this.selectedFile = null;
  }

  copyFilePath() {
    const fullPath = this.currentPathKey + (this.selectedFile ? '/' + this.selectedFile.name : '');
    navigator.clipboard.writeText(fullPath);
    alert('Path copied to clipboard: ' + fullPath);
    this.isEllipsisMenuOpen = false;
  }
}
