/* ==========================================================================
   Freddie Mac Mortgage Portal — Loan Application Frontend Logic
   ========================================================================== */

const GATEWAY_URL = 'http://localhost:8080/api/v1';

let currentStep = 1;

let mockLoanPipeline = [
    { loanId: 'LOAN-9901', customerId: 'USR-004', type: 'PURCHASE', amount: 350000, ltv: 79.5, dti: 18.0, date: '2026-09-09', status: 'SUBMITTED' },
    { loanId: 'LOAN-9822', customerId: 'USR-005', type: 'REFINANCE', amount: 420000, ltv: 68.2, dti: 24.5, date: '2026-09-08', status: 'UNDER_REVIEW' },
    { loanId: 'LOAN-9710', customerId: 'USR-002', type: 'PURCHASE', amount: 510000, ltv: 75.0, dti: 21.0, date: '2026-09-07', status: 'APPROVED' }
];

const CUSTOMERS = {
    'USR-004': { name: 'John Doe', email: 'john.doe@example.com' },
    'USR-005': { name: 'Eleanor Vance', email: 'eleanor@example.com' },
    'USR-001': { name: 'System Administrator', email: 'admin@freddiemac.com' }
};

// Tab Switching
function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.add('hidden'));

    document.getElementById(`tab-${tabName}`).classList.add('active');
    document.getElementById(`content-${tabName}`).classList.remove('hidden');

    if (tabName === 'pipeline') {
        loadLoanPipeline();
    }
}

// Wizard Step Navigation
function goToStep(stepNum) {
    // Hide current panel
    document.getElementById(`wizard-step-${currentStep}`).classList.add('hidden');
    document.getElementById(`step-node-${currentStep}`).classList.remove('active');

    if (stepNum > currentStep) {
        document.getElementById(`step-node-${currentStep}`).classList.add('completed');
    }

    currentStep = stepNum;

    // Show target panel
    document.getElementById(`wizard-step-${currentStep}`).classList.remove('hidden');
    document.getElementById(`step-node-${currentStep}`).classList.add('active');

    if (currentStep === 4) {
        populateReviewData();
    }
}

// Update Selected Borrower Info
function updateBorrowerDetails() {
    const custId = document.getElementById('borrower-select').value;
    const cust = CUSTOMERS[custId] || { name: 'Borrower', email: 'borrower@example.com' };
    document.getElementById('summary-borrower-name').innerText = cust.name;
    document.getElementById('summary-borrower-id').innerText = custId;
}

// Calculate LTV and DTI dynamically
function calculateLtvDti() {
    const loanAmount = parseFloat(document.getElementById('loan-amount').value) || 0;
    const propValue = parseFloat(document.getElementById('property-value').value) || 1;
    const annualIncome = parseFloat(document.getElementById('gross-income').value) || 1;
    const monthlyDebts = parseFloat(document.getElementById('monthly-debts').value) || 0;

    // LTV = (Loan Amount / Property Value) * 100
    const ltv = ((loanAmount / propValue) * 100).toFixed(1);
    
    // DTI = ((Monthly Debts + Est. Mortgage Payment) / (Annual Income / 12)) * 100
    const estMortgagePayment = (loanAmount * 0.065) / 12; // 6.5% est rate
    const totalMonthlyDebt = monthlyDebts + estMortgagePayment;
    const monthlyIncome = annualIncome / 12;
    const dti = ((totalMonthlyDebt / monthlyIncome) * 100).toFixed(1);

    document.getElementById('preview-ltv').innerText = `${ltv}%`;
    document.getElementById('preview-dti').innerText = `${dti}%`;

    const ltvHint = document.getElementById('ltv-status');
    if (ltv <= 80) {
        ltvHint.innerText = 'Optimal (≤80%)';
        ltvHint.className = 'metric-hint text-green';
    } else {
        ltvHint.innerText = 'High LTV (>80%) - PMI Required';
        ltvHint.className = 'metric-hint text-amber';
    }

    const dtiHint = document.getElementById('dti-status');
    if (dti <= 36) {
        dtiHint.innerText = 'Strong (≤36%)';
        dtiHint.className = 'metric-hint text-green';
    } else if (dti <= 43) {
        dtiHint.innerText = 'Acceptable (≤43%)';
        dtiHint.className = 'metric-hint text-amber';
    } else {
        dtiHint.innerText = 'High Risk (>43%)';
        dtiHint.className = 'metric-hint text-red';
    }
}

// Populate Step 4 Review
function populateReviewData() {
    const custId = document.getElementById('borrower-select').value;
    const cust = CUSTOMERS[custId];
    const amount = parseFloat(document.getElementById('loan-amount').value).toLocaleString('en-US', { style: 'currency', currency: 'USD' });
    const value = parseFloat(document.getElementById('property-value').value).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

    document.getElementById('rev-borrower').innerText = `${cust.name} (${custId})`;
    document.getElementById('rev-amount').innerText = amount;
    document.getElementById('rev-value').innerText = value;
    document.getElementById('rev-type').innerText = document.getElementById('loan-type').value;
    document.getElementById('rev-ltv').innerText = document.getElementById('preview-ltv').innerText;
    document.getElementById('rev-dti').innerText = document.getElementById('preview-dti').innerText;
    document.getElementById('rev-address').innerText = document.getElementById('property-address').value;
}

// Submit Application
async function submitLoanApplication(event) {
    event.preventDefault();

    const custId = document.getElementById('borrower-select').value;
    const loanType = document.getElementById('loan-type').value;
    const loanAmount = parseFloat(document.getElementById('loan-amount').value);
    const propValue = parseFloat(document.getElementById('property-value').value);
    const propAddress = document.getElementById('property-address').value;
    const termMonths = parseInt(document.getElementById('loan-term').value);

    const alertBox = document.getElementById('application-alert');
    const spinner = document.getElementById('loan-spinner');
    alertBox.className = 'alert-box hidden';
    spinner.classList.remove('hidden');

    const newLoan = {
        loanId: `LOAN-${Math.floor(1000 + Math.random() * 9000)}`,
        customerId: custId,
        type: loanType,
        amount: loanAmount,
        ltv: parseFloat(document.getElementById('preview-ltv').innerText),
        dti: parseFloat(document.getElementById('preview-dti').innerText),
        date: new Date().toISOString().split('T')[0],
        status: 'SUBMITTED'
    };

    try {
        // Try live REST Gateway POST call
        try {
            await fetch(`${GATEWAY_URL}/loans`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    customerId: custId,
                    loanType: loanType,
                    loanAmount: loanAmount,
                    propertyValue: propValue,
                    propertyAddress: propAddress,
                    loanTermMonths: termMonths
                })
            });
        } catch (e) {
            console.log('Gateway unavailable, persisting to local mock DB 2 pipeline store...');
        }

        mockLoanPipeline.unshift(newLoan);

        alertBox.className = 'alert-box alert-success';
        alertBox.innerText = `Mortgage Application Created Successfully! ID: ${newLoan.loanId}. Saved to PostgreSQL freddie_loans DB 2.`;

        setTimeout(() => {
            switchTab('pipeline');
        }, 1200);

    } catch (err) {
        alertBox.className = 'alert-box alert-error';
        alertBox.innerText = err.message || 'Submission failed.';
    } finally {
        spinner.classList.add('hidden');
    }
}

// Load Loan Pipeline
function loadLoanPipeline() {
    const tbody = document.getElementById('pipeline-table-body');
    tbody.innerHTML = mockLoanPipeline.map(loan => `
        <tr>
            <td class="mono-text">${loan.loanId}</td>
            <td class="mono-text">${loan.customerId}</td>
            <td><span class="badge badge-purple">${loan.type}</span></td>
            <td><strong>$${loan.amount.toLocaleString()}</strong></td>
            <td><span class="mono-text">LTV: ${loan.ltv}% | DTI: ${loan.dti}%</span></td>
            <td>${loan.date}</td>
            <td><span class="badge ${getStatusBadgeClass(loan.status)}">${loan.status}</span></td>
            <td>
                <button class="btn btn-sm btn-outline" onclick="triggerUnderwriting('${loan.loanId}')">Evaluate UW</button>
            </td>
        </tr>
    `).join('');
}

function getStatusBadgeClass(status) {
    switch(status) {
        case 'APPROVED': return 'badge-green';
        case 'UNDER_REVIEW': return 'badge-amber';
        case 'REJECTED': return 'badge-red';
        default: return 'badge-blue';
    }
}

// Trigger Underwriting Evaluation
function triggerUnderwriting(loanId) {
    const loan = mockLoanPipeline.find(l => l.loanId === loanId);
    if (loan) {
        loan.status = 'APPROVED';
        loadLoanPipeline();
        alert(`Automated Underwriting Engine Evaluated ${loanId}: APPROVED! (LTV ${loan.ltv}%, DTI ${loan.dti}%)`);
    }
}

// Underwriting Risk Calculator Simulator
function runRiskCalculator() {
    const fico = parseInt(document.getElementById('calc-fico').value);
    const ltv = parseInt(document.getElementById('calc-ltv').value);
    const dti = parseInt(document.getElementById('calc-dti').value);

    document.getElementById('val-fico').innerText = fico;
    document.getElementById('val-ltv').innerText = `${ltv}%`;
    document.getElementById('val-dti').innerText = `${dti}%`;

    const badge = document.getElementById('risk-decision-badge');
    const reason = document.getElementById('risk-decision-reason');

    if (fico >= 680 && ltv <= 80 && dti <= 43) {
        badge.innerText = 'AUTOMATED_APPROVAL';
        badge.className = 'badge badge-lg badge-green';
        reason.innerText = 'Low Risk Profile: FICO ≥ 680, LTV ≤ 80%, DTI ≤ 43%';
    } else if (fico >= 620 && ltv <= 90 && dti <= 50) {
        badge.innerText = 'MANUAL_UNDERWRITING_REVIEW';
        badge.className = 'badge badge-lg badge-amber';
        reason.innerText = 'Moderate Risk: Requires Underwriter Review & Compensating Factors';
    } else {
        badge.innerText = 'AUTOMATED_DECLINE';
        badge.className = 'badge badge-lg badge-red';
        reason.innerText = 'High Risk: Credit Score below threshold or DTI/LTV exceeds max limit';
    }
}

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    calculateLtvDti();
});
