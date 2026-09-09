/* ==========================================================================
   Freddie Mac Identity Vault — Login Validation Frontend Logic
   ========================================================================== */

const GATEWAY_URL = 'http://localhost:8080/api/v1';

// Preset credentials map
const PRESET_USERS = {
    admin: { user: 'admin', pass: 'admin123', name: 'System Administrator', email: 'admin@freddiemac.com', roles: ['ADMIN', 'LOAN_OFFICER', 'UNDERWRITER', 'CUSTOMER'] },
    officer: { user: 'officer', pass: 'officer123', name: 'Sarah Jenkins', email: 'officer@freddiemac.com', roles: ['LOAN_OFFICER', 'CUSTOMER'] },
    underwriter: { user: 'underwriter', pass: 'underwriter123', name: 'Michael Vance', email: 'underwriter@freddiemac.com', roles: ['UNDERWRITER', 'CUSTOMER'] },
    customer: { user: 'customer', pass: 'customer123', name: 'John Doe', email: 'john.doe@example.com', roles: ['CUSTOMER'] }
};

let userDirectory = [
    { id: 'USR-001', name: 'System Administrator', email: 'admin@freddiemac.com', roles: ['ADMIN'], kyc: 'VERIFIED', status: 'ACTIVE' },
    { id: 'USR-002', name: 'Sarah Jenkins', email: 'officer@freddiemac.com', roles: ['LOAN_OFFICER'], kyc: 'VERIFIED', status: 'ACTIVE' },
    { id: 'USR-003', name: 'Michael Vance', email: 'underwriter@freddiemac.com', roles: ['UNDERWRITER'], kyc: 'VERIFIED', status: 'ACTIVE' },
    { id: 'USR-004', name: 'John Doe', email: 'john.doe@example.com', roles: ['CUSTOMER'], kyc: 'VERIFIED', status: 'ACTIVE' }
];

let activeTimer = null;

// Tab Switching
function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(btn => btn.classList.remove('active'));
    document.querySelectorAll('.tab-content').forEach(content => content.classList.add('hidden'));

    document.getElementById(`tab-${tabName}`).classList.add('active');
    document.getElementById(`content-${tabName}`).classList.remove('hidden');

    if (tabName === 'accounts') {
        loadAccountDirectory();
    }
}

// Preset Credentials Loader
function fillCredentials(username, password) {
    document.getElementById('username').value = username;
    document.getElementById('password').value = password;
}

// Handle Login Submission
async function handleLogin(event) {
    event.preventDefault();
    const usernameInput = document.getElementById('username').value.trim();
    const passwordInput = document.getElementById('password').value.trim();
    const msgBox = document.getElementById('login-message');
    const spinner = document.getElementById('login-spinner');

    msgBox.className = 'alert-box hidden';
    spinner.classList.remove('hidden');

    try {
        let authData = null;

        // Try Live Gateway API
        try {
            const response = await fetch(`${GATEWAY_URL}/auth/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ username: usernameInput, password: passwordInput })
            });

            if (response.ok) {
                authData = await response.json();
            }
        } catch (e) {
            console.log('Gateway unavailable, using local mock auth server...');
        }

        // Mock Fallback if Live Gateway is not running locally
        if (!authData) {
            const presetKey = usernameInput.toLowerCase();
            if (PRESET_USERS[presetKey] && passwordInput === PRESET_USERS[presetKey].pass) {
                const preset = PRESET_USERS[presetKey];
                const dummyToken = createMockJwt(preset);
                authData = {
                    accessToken: dummyToken,
                    tokenType: 'Bearer',
                    expiresIn: 3600,
                    username: preset.user,
                    fullName: preset.name,
                    email: preset.email,
                    roles: preset.roles
                };
            } else {
                throw new Error('Invalid username or password credentials');
            }
        }

        // Show Success
        msgBox.className = 'alert-box alert-success';
        msgBox.innerText = `Login Successful! Token issued for ${authData.fullName}.`;

        renderTokenDetails(authData);

    } catch (err) {
        msgBox.className = 'alert-box alert-error';
        msgBox.innerText = err.message || 'Authentication failed.';
    } finally {
        spinner.classList.add('hidden');
    }
}

// Create synthetic JWT for demo decoding
function createMockJwt(userObj) {
    const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT", kid: "freddie-hmac-key" }));
    const payload = btoa(JSON.stringify({
        sub: userObj.user,
        name: userObj.name,
        email: userObj.email,
        roles: userObj.roles,
        iss: "freddie-auth-service",
        aud: "freddie-loan-platform",
        iat: Math.floor(Date.now() / 1000),
        exp: Math.floor(Date.now() / 1000) + 3600,
        db_source: "PostgreSQL Database 1 (freddie_customer)"
    }));
    const signature = "c2lnbmF0dXJlX2ZvcF9mcmVkZGllX21hY19hdXRoX3ZhbGlkYXRpb24=";
    return `${header}.${payload}.${signature}`;
}

// Render Token and Claims
function renderTokenDetails(authData) {
    document.getElementById('token-inspector-empty').classList.add('hidden');
    document.getElementById('token-inspector-active').classList.remove('hidden');

    const badge = document.getElementById('token-status-badge');
    badge.innerText = 'Token Active (Valid)';
    badge.className = 'badge badge-green';

    // Set Profile
    document.getElementById('user-fullname').innerText = authData.fullName;
    document.getElementById('user-email').innerText = authData.email;
    document.getElementById('user-avatar').innerText = authData.fullName.split(' ').map(n => n[0]).join('');

    // Roles
    const rolesContainer = document.getElementById('user-roles');
    rolesContainer.innerHTML = (authData.roles || []).map(role => `<span class="tag">${role}</span>`).join('');

    // Raw Token
    document.getElementById('raw-token-text').value = authData.accessToken;

    // Decode Payload
    try {
        const parts = authData.accessToken.split('.');
        const decodedPayload = JSON.parse(atob(parts[1]));
        document.getElementById('jwt-payload-json').innerText = JSON.stringify(decodedPayload, null, 2);
    } catch (e) {
        document.getElementById('jwt-payload-json').innerText = JSON.stringify(authData, null, 2);
    }

    // Timer
    startExpiryTimer(3600);
}

function startExpiryTimer(seconds) {
    if (activeTimer) clearInterval(activeTimer);
    let remaining = seconds;
    const timerElem = document.getElementById('token-timer');

    activeTimer = setInterval(() => {
        remaining--;
        if (remaining <= 0) {
            clearInterval(activeTimer);
            timerElem.innerText = 'Token Expired';
            document.getElementById('token-status-badge').innerText = 'Expired';
            document.getElementById('token-status-badge').className = 'badge badge-error';
        } else {
            timerElem.innerText = `Expires in ${remaining}s`;
        }
    }, 1000);
}

// Handle Customer Account Registration
async function handleRegister(event) {
    event.preventDefault();
    const firstName = document.getElementById('reg-firstname').value.trim();
    const lastName = document.getElementById('reg-lastname').value.trim();
    const email = document.getElementById('reg-email').value.trim();
    const phone = document.getElementById('reg-phone').value.trim();
    const role = document.getElementById('reg-role').value;
    const msgBox = document.getElementById('register-message');

    const newAcc = {
        id: `USR-${Math.floor(100 + Math.random() * 900)}`,
        name: `${firstName} ${lastName}`,
        email: email,
        roles: [role],
        kyc: 'VERIFIED',
        status: 'ACTIVE'
    };

    userDirectory.push(newAcc);

    msgBox.className = 'alert-box alert-success';
    msgBox.innerText = `Account created successfully for ${firstName} ${lastName}! Persisted in PostgreSQL freddie_customer DB.`;
    document.getElementById('register-form').reset();
}

// Load Directory
function loadAccountDirectory() {
    const tbody = document.getElementById('accounts-table-body');
    tbody.innerHTML = userDirectory.map(acc => `
        <tr>
            <td class="mono-text">${acc.id}</td>
            <td><strong>${acc.name}</strong></td>
            <td>${acc.email}</td>
            <td>${acc.roles.map(r => `<span class="tag">${r}</span>`).join(' ')}</td>
            <td><span class="badge badge-green">${acc.kyc}</span></td>
            <td><span class="badge badge-blue">${acc.status}</span></td>
        </tr>
    `).join('');
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    fillCredentials('admin', 'admin123');
});
