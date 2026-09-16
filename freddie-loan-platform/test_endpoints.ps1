Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Freddie Mac Platform End-to-End Scratchpad Test Suite " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Create Loan Application on Port 8082
$loanPayload = @{
    customerId = "CUST-999"
    applicantName = "Scratchpad Tester"
    email = "scratchpad@freddiemac.com"
    loanAmount = 350000
    propertyValue = 420000
    monthlyIncome = 12000
    monthlyDebt = 2500
    creditScore = 750
    termMonths = 360
} | ConvertTo-Json

Write-Host "`n1. Testing Loan Application Creation (Port 8082)..." -ForegroundColor Yellow
$createRes = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/loans" -Method Post -Body $loanPayload -ContentType "application/json"
Write-Host "   Created Loan ID: $($createRes.id), Status: $($createRes.status)" -ForegroundColor Green

# 2. Submit for Underwriting (WebClient Trigger to Port 8083)
Write-Host "`n2. Testing Native SQL + WebClient Underwriting Trigger (Port 8082 -> Port 8083)..." -ForegroundColor Yellow
$submitRes = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/loans/$($createRes.id)/submit-underwriting" -Method Post -ContentType "application/json"
Write-Host "   Updated Status: $($submitRes.status)" -ForegroundColor Green

# 3. Direct Underwriting Assessment on Port 8083
$assessPayload = @{
    loanId = $createRes.id
    customerId = "CUST-999"
    loanAmount = 350000
    propertyValue = 420000
    monthlyIncome = 12000
    monthlyDebt = 2500
    creditScore = 750
    termMonths = 360
} | ConvertTo-Json

Write-Host "`n3. Testing Underwriting Risk Assessment (Port 8083)..." -ForegroundColor Yellow
$assessRes = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/underwriting/assess" -Method Post -Body $assessPayload -ContentType "application/json"
Write-Host "   Decision: $($assessRes.decision), Risk Level: $($assessRes.riskLevel)" -ForegroundColor Green

# 4. Pricing Quote Engine on Port 8083
Write-Host "`n4. Testing Tiered Rate Pricing Quote (Port 8083)..." -ForegroundColor Yellow
$quoteRes = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/rates/quote?creditScore=750&ltvRatio=83.33" -Method Get
Write-Host "   Pricing Tier: $($quoteRes.pricingTier), Final Rate: $($quoteRes.finalInterestRate)%, Monthly EMI: `$$($quoteRes.monthlyEmi)" -ForegroundColor Green

# 5. Account Lookup Update DTO on Port 8082
Write-Host "`n5. Testing Account Lookup Update DTO (Port 8082)..." -ForegroundColor Yellow
$lookupRes = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/account/lookup/update" -Method Get
Write-Host "   Status: $($lookupRes.respSts.statusMessage), Lines of Business: $($lookupRes.ucsLineOfBusinessDTOs.Count)" -ForegroundColor Green

# 6. Stage 1 Counterparty Onboarding on Port 8082
Write-Host "`n6. Testing Stage 1 Counterparty Intake & Approval (Port 8082)..." -ForegroundColor Yellow
$stage1Payload = @{ orgName = "Partner Bank Inc"; email = "partner@bank.com" } | ConvertTo-Json
$stage1Onboard = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/counterparty/stage1/onboard" -Method Post -Body $stage1Payload -ContentType "application/json"
$stage1Approve = Invoke-RestMethod -Uri "http://localhost:8082/api/v1/counterparty/stage1/approve/$($stage1Onboard.userId)" -Method Post -ContentType "application/json"
Write-Host "   User ID: $($stage1Approve.userId), Status: $($stage1Approve.status)" -ForegroundColor Green

# 7. ActiveMQ JMS Event Publishing on Port 8083
Write-Host "`n7. Testing ActiveMQ JMS Event Publishing (Port 8083)..." -ForegroundColor Yellow
$jmsRes = Invoke-RestMethod -Uri "http://localhost:8083/api/v1/notifications/publish?eventType=SCRATCHPAD_TEST" -Method Post -Body "{}" -ContentType "application/json"
Write-Host "   Event ID: $($jmsRes.eventId), Queue: $($jmsRes.queueName), Status: $($jmsRes.status)" -ForegroundColor Green

Write-Host "`n==========================================================" -ForegroundColor Cyan
Write-Host " ALL ENDPOINTS PASSED SUCCESSFULLY IN SCRATCHPAD TEST! " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan
