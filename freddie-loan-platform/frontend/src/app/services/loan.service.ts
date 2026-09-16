import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

export interface LoanApplication {
  loanId: string;
  id?: number;
  customerId: string;
  customerName: string;
  email?: string;
  loanType: string;
  loanAmount: number;
  propertyValue: number;
  annualIncome: number;
  monthlyDebt: number;
  ltvRatio: number;
  dtiRatio: number;
  creditScore: number;
  status: 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'CONDITIONAL_APPROVAL' | 'REJECTED' | 'DISBURSED';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  decisionReason?: string;
  submittedAt: Date;
  documents: { documentId: string; fileName: string; type: string; status: string }[];
}

export interface UnderwritingAssessResult {
  loanId: number;
  decision: 'APPROVED' | 'REFERRED' | 'DECLINED';
  riskLevel: 'LOW' | 'MEDIUM' | 'HIGH';
  dtiRatio: number;
  ltvRatio: number;
  remarks: string;
}

export interface RateQuoteResult {
  pricingTier: string;
  baseRate: number;
  adjustedRate: number;
  monthlyEmi: number;
  ltvRatio: number;
}

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private useRealBackend = true;
  private backendBase = '/api/v1';

  private mockApplications: LoanApplication[] = [
    {
      loanId: 'LN-1082',
      id: 1082,
      customerId: 'CUST-802',
      customerName: 'Marcus Vance',
      email: 'marcus@freddiemac.com',
      loanType: 'Purchase (Fixed 30Y)',
      loanAmount: 380000,
      propertyValue: 450000,
      annualIncome: 120000,
      monthlyDebt: 2200,
      ltvRatio: 84.44,
      dtiRatio: 22.00,
      creditScore: 780,
      status: 'APPROVED',
      riskLevel: 'LOW',
      decisionReason: 'Eligible for primary residence standard purchase program.',
      submittedAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000),
      documents: [
        { documentId: 'DOC-1', fileName: 'W2_2025.pdf', type: 'INCOME_PROOF', status: 'VERIFIED' },
        { documentId: 'DOC-2', fileName: 'DriverLicense.jpg', type: 'IDENTIFICATION', status: 'VERIFIED' }
      ]
    },
    {
      loanId: 'LN-2940',
      id: 2940,
      customerId: 'CUST-390',
      customerName: 'Sarah Jenkins',
      email: 'sarah@freddiemac.com',
      loanType: 'Refinance (Floating 15Y)',
      loanAmount: 290000,
      propertyValue: 310000,
      annualIncome: 85000,
      monthlyDebt: 3100,
      ltvRatio: 93.55,
      dtiRatio: 43.76,
      creditScore: 650,
      status: 'UNDER_REVIEW',
      riskLevel: 'HIGH',
      decisionReason: 'Elevated DTI ratio and low equity margin. Referred to manual underwriting.',
      submittedAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000),
      documents: [
        { documentId: 'DOC-3', fileName: 'PayStub_May.pdf', type: 'INCOME_PROOF', status: 'VERIFIED' }
      ]
    },
    {
      loanId: 'LN-4830',
      id: 4830,
      customerId: 'CUST-411',
      customerName: 'Elena Rostova',
      email: 'elena@freddiemac.com',
      loanType: 'HELOC (Variable)',
      loanAmount: 85000,
      propertyValue: 600000,
      annualIncome: 150000,
      monthlyDebt: 1100,
      ltvRatio: 14.17,
      dtiRatio: 8.80,
      creditScore: 820,
      status: 'APPROVED',
      riskLevel: 'LOW',
      decisionReason: 'Strong asset leverage and high creditworthiness.',
      submittedAt: new Date(Date.now() - 12 * 60 * 60 * 1000),
      documents: []
    }
  ];

  private applicationsSubject = new BehaviorSubject<LoanApplication[]>(this.mockApplications);

  constructor(private http: HttpClient) {}

  getApplications(): Observable<LoanApplication[]> {
    if (this.useRealBackend) {
      this.http.get<any[]>(`${this.backendBase}/loans`).pipe(
        map(backendApps => backendApps.map(app => this.mapToFrontendModel(app))),
        catchError(err => {
          console.warn('Real backend fetch failed. Falling back to local in-memory Mock Sandbox Mode.', err);
          return of(this.mockApplications);
        })
      ).subscribe(apps => {
        if (apps && apps.length > 0) {
          this.applicationsSubject.next(apps);
        }
      });
    }
    return this.applicationsSubject.asObservable();
  }

  submitApplication(appData: Omit<LoanApplication, 'loanId' | 'ltvRatio' | 'dtiRatio' | 'status' | 'riskLevel' | 'submittedAt' | 'documents'>): Observable<LoanApplication> {
    const ltv = Number(((appData.loanAmount / appData.propertyValue) * 100).toFixed(2));
    const monthlyIncome = appData.annualIncome / 12;
    const dti = Number(((appData.monthlyDebt / monthlyIncome) * 100).toFixed(2));
    
    let riskLevel: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL' = 'LOW';
    if (appData.creditScore < 600 || dti > 50 || ltv > 95) riskLevel = 'CRITICAL';
    else if (appData.creditScore < 660 || dti > 45 || ltv > 90) riskLevel = 'HIGH';
    else if (appData.creditScore < 740 || dti > 36 || ltv > 80) riskLevel = 'MEDIUM';

    let status: LoanApplication['status'] = 'SUBMITTED';
    let decisionReason = 'Application intake complete.';

    const payload = {
      customerId: appData.customerId,
      applicantName: appData.customerName,
      email: `${appData.customerId.toLowerCase()}@freddiemac.com`,
      loanAmount: appData.loanAmount,
      propertyValue: appData.propertyValue,
      monthlyIncome: monthlyIncome,
      monthlyDebt: appData.monthlyDebt,
      creditScore: appData.creditScore,
      termMonths: 360
    };

    if (this.useRealBackend) {
      return this.http.post<any>(`${this.backendBase}/loans`, payload).pipe(
        map(res => this.mapToFrontendModel(res)),
        tap(newApp => {
          const currentList = this.applicationsSubject.value;
          this.applicationsSubject.next([newApp, ...currentList]);
        }),
        catchError(err => {
          console.warn('Real backend submission failed. Falling back to local in-memory Mock Sandbox Mode.', err);
          return this.fallbackLocalSubmit(appData, ltv, dti, riskLevel, status, decisionReason);
        })
      );
    }

    return this.fallbackLocalSubmit(appData, ltv, dti, riskLevel, status, decisionReason);
  }

  submitForUnderwritingNative(numericLoanId: number): Observable<LoanApplication> {
    return this.http.post<any>(`${this.backendBase}/loans/${numericLoanId}/submit-underwriting`, {}).pipe(
      map(res => this.mapToFrontendModel(res)),
      tap(updated => this.updateLocalAppStatus(updated.loanId, updated.status, 'PostgreSQL Native Query status update', 'System'))
    );
  }

  assessUnderwriting(request: { loanId: number; monthlyIncome: number; monthlyDebt: number; loanAmount: number; propertyValue: number; creditScore: number }): Observable<UnderwritingAssessResult> {
    return this.http.post<UnderwritingAssessResult>(`${this.backendBase}/underwriting/assess`, request);
  }

  calculateRateQuote(request: { loanAmount: number; propertyValue: number; creditScore: number; termMonths?: number }): Observable<RateQuoteResult> {
    return this.http.post<RateQuoteResult>(`${this.backendBase}/rates/calculate`, request);
  }

  publishJmsEvent(eventType: string, payload: any): Observable<any> {
    return this.http.post<any>(`${this.backendBase}/notifications/publish?eventType=${eventType}`, JSON.stringify(payload));
  }

  private fallbackLocalSubmit(appData: any, ltv: number, dti: number, riskLevel: any, status: any, decisionReason: string): Observable<LoanApplication> {
    const newApp: LoanApplication = {
      ...appData,
      loanId: 'LN-' + Math.floor(1000 + Math.random() * 9000),
      ltvRatio: ltv,
      dtiRatio: dti,
      status,
      riskLevel,
      decisionReason,
      submittedAt: new Date(),
      documents: []
    };
    const currentList = this.applicationsSubject.value;
    this.applicationsSubject.next([newApp, ...currentList]);
    return of(newApp);
  }

  updateStatus(loanId: string, status: LoanApplication['status'], reason: string, reviewer: string): Observable<boolean> {
    const numericId = parseInt(loanId.replace('LN-', ''), 10);
    if (!isNaN(numericId) && this.useRealBackend) {
      return this.submitForUnderwritingNative(numericId).pipe(
        map(() => true),
        catchError(() => {
          this.updateLocalAppStatus(loanId, status, reason, reviewer);
          return of(true);
        })
      );
    }
    this.updateLocalAppStatus(loanId, status, reason, reviewer);
    return of(true);
  }

  private updateLocalAppStatus(loanId: string, status: LoanApplication['status'], reason: string, reviewer: string) {
    const currentList = this.applicationsSubject.value.map(app => {
      if (app.loanId === loanId) {
        return {
          ...app,
          status,
          decisionReason: `Modified by ${reviewer}: ${reason}`
        };
      }
      return app;
    });
    this.applicationsSubject.next(currentList);
  }

  addDocument(loanId: string, fileName: string, type: string): Observable<boolean> {
    this.addLocalDocument(loanId, fileName, type);
    return of(true);
  }

  private addLocalDocument(loanId: string, fileName: string, type: string) {
    const currentList = this.applicationsSubject.value.map(app => {
      if (app.loanId === loanId) {
        const docs = [...app.documents, {
          documentId: 'DOC-' + Math.floor(100 + Math.random() * 900),
          fileName,
          type,
          status: 'UPLOADED'
        }];
        return { ...app, documents: docs };
      }
      return app;
    });
    this.applicationsSubject.next(currentList);
  }

  private mapToFrontendModel(res: any): LoanApplication {
    const rawId = res.loanId || res.id;
    return {
      loanId: rawId ? `LN-${rawId}` : 'LN-' + Math.floor(1000 + Math.random() * 9000),
      id: rawId,
      customerId: res.customerId || 'CUST-TBD',
      customerName: res.applicantName || res.customerName || 'Mortgage Client',
      email: res.email,
      loanType: 'Purchase (Fixed 30Y)',
      loanAmount: res.loanAmount || 0,
      propertyValue: res.propertyValue || 0,
      annualIncome: res.monthlyIncome ? res.monthlyIncome * 12 : (res.annualIncome || 95000),
      monthlyDebt: res.monthlyDebt || 1200,
      ltvRatio: res.ltvRatio || Number((((res.loanAmount || 0) / (res.propertyValue || 1)) * 100).toFixed(2)),
      dtiRatio: res.dtiRatio || Number((((res.monthlyDebt || 0) / ((res.monthlyIncome || 10000))) * 100).toFixed(2)),
      creditScore: res.creditScore || 700,
      status: res.status || 'SUBMITTED',
      riskLevel: res.riskLevel || 'LOW',
      decisionReason: res.decisionReason || 'Intake process complete.',
      submittedAt: res.createdAt ? new Date(res.createdAt) : new Date(),
      documents: res.documents || []
    };
  }
}
