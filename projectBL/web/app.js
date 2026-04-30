const page = document.body.dataset.page;

window.addEventListener('DOMContentLoaded', () => {
    renderSessionInfo();

    if (page === 'home') {
        loadDashboard();
    }
    if (page === 'book') {
        attachForm('bookForm', '/api/book', renderBookingResult, 'bookMessage');
    }
    if (page === 'track') {
        attachForm('trackForm', '/api/track', renderTrackResult, 'trackMessage');
    }
    if (page === 'invoice') {
        attachForm('invoiceForm', '/api/invoice', renderInvoiceResult, 'invoiceMessage');
    }
    if (page === 'parcel-status') {
        attachForm('parcelStatusForm', '/api/parcel-status', renderParcelStatusResult, 'parcelStatusMessage');
    }
    if (page === 'update-pickup') {
        attachForm('updatePickupForm', '/api/officer/update-time', renderGenericResult, 'updateTimeMessage');
    }
    if (page === 'login') {
        attachForm('loginForm', '/api/login', renderLoginResult, 'loginMessage');
    }
    if (page === 'register') {
        attachForm('registerForm', '/api/register', renderRegisterResult, 'registerMessage');
    }
});

function renderSessionInfo() {
    const container = document.getElementById('sessionInfo');
    if (!container) return;
    const session = getSession();
    if (!session) {
        container.innerHTML = '<a class="button button-secondary" href="login.html">Login</a>';
        return;
    }
    const roleLabel = session.officer ? ' (Officer)' : '';
    container.innerHTML = `
        <div class="session-text">Logged in as <strong>${escapeHtml(session.userName)}</strong>${roleLabel}</div>
        <button id="logoutButton" class="button button-secondary small">Logout</button>
    `;
    const logoutButton = document.getElementById('logoutButton');
    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            clearSession();
            window.location.href = 'login.html';
        });
    }
}

function getSession() {
    try {
        return JSON.parse(localStorage.getItem('parcelSession'));
    } catch {
        return null;
    }
}

function setSession(userName, officer = false) {
    localStorage.setItem('parcelSession', JSON.stringify({ userName, officer }));
    renderSessionInfo();
}

function clearSession() {
    localStorage.removeItem('parcelSession');
    renderSessionInfo();
}

function attachForm(formId, url, successRenderer, messageId) {
    const form = document.getElementById(formId);
    if (!form) return;
    form.addEventListener('submit', async event => {
        event.preventDefault();
        const messageElement = document.getElementById(messageId);
        const result = await postForm(url, form);
        if (result.ok) {
            showMessage(messageElement, result.message, true);
            if (formId === 'loginForm') {
                const userName = form.userName?.value || 'User';
                setSession(userName, result.officer);
            }
            if (successRenderer) {
                successRenderer(result);
            }
        } else {
            showMessage(messageElement, result.message || 'Something went wrong.', false);
        }
    });
}

async function loadDashboard() {
    const session = getSession();
    if (!session) {
        window.location.href = 'login.html';
        return;
    }
    const response = await fetch(`/api/dashboard?userName=${encodeURIComponent(session.userName)}`);
    const result = await response.json();
    const summaryElement = document.getElementById('homeSummary');
    if (!result.ok) {
        summaryElement.textContent = result.message;
        summaryElement.classList.add('error');
        return;
    }

    let html = `<p><strong>Name:</strong> ${result.customerName}</p>`;
    html += `<p><strong>Email:</strong> ${result.email}</p>`;
    html += `<p><strong>Contact:</strong> +${result.countryCode} ${result.mobileNumber}</p>`;
    html += `<p><strong>Address:</strong> ${result.address}</p>`;
    if (result.booking) {
        html += `<div class="summary"><h3>Current Booking</h3>`;
        html += `<p><strong>Booking ID:</strong> ${result.booking.bookingId}</p>`;
        html += `<p><strong>Recipient:</strong> ${result.booking.recipientName}</p>`;
        html += `<p><strong>Parcel Status:</strong> ${result.booking.parcelStatus}</p>`;
        html += `<p><strong>Pickup:</strong> ${result.booking.pickupTime}</p>`;
        html += `<p><strong>Dropoff:</strong> ${result.booking.dropoffTime}</p>`;
        html += `<p><strong>Service Cost:</strong> ₹${result.booking.serviceCost.toFixed(2)}</p>`;
        html += '</div>';
    } else {
        html += '<p><em>No booking found yet. Create one on the Booking page.</em></p>';
    }
    summaryElement.innerHTML = html;
}

function renderRegisterResult(result) {
    if (result.ok) {
        window.location.href = 'login.html';
    }
}

function renderLoginResult(result) {
    if (result.ok) {
        window.location.href = 'home.html';
    }
}

function renderBookingResult(result) {
    const summary = document.getElementById('bookingSummary');
    if (!summary) return;
    summary.innerHTML = `<strong>Booking created successfully.</strong><br/>Booking ID: ${result.bookingId}`;
    summary.classList.remove('hidden');
}

function renderTrackResult(result) {
    const summary = document.getElementById('trackSummary');
    if (!summary) return;
    summary.innerHTML = `
        <p><strong>Booking ID:</strong> ${result.bookingId}</p>
        <p><strong>Recipient:</strong> ${result.recipientName}</p>
        <p><strong>Address:</strong> ${result.recipientAddress}</p>
        <p><strong>Status:</strong> ${result.parcelStatus}</p>
        <p><strong>Pickup:</strong> ${result.pickupTime}</p>
        <p><strong>Dropoff:</strong> ${result.dropoffTime}</p>
        <p><strong>Service Cost:</strong> ₹${Number(result.serviceCost).toFixed(2)}</p>
        <p><strong>Payment Time:</strong> ${result.paymentTime}</p>
    `;
    summary.classList.remove('hidden');
}

function renderInvoiceResult(result) {
    const summary = document.getElementById('invoiceSummary');
    if (!summary) return;
    summary.innerHTML = `
        <h3>Invoice</h3>
        <p><strong>Customer:</strong> ${result.customerName}</p>
        <p><strong>Email:</strong> ${result.email}</p>
        <p><strong>Address:</strong> ${result.address}</p>
        <p><strong>Booking ID:</strong> ${result.bookingId}</p>
        <p><strong>Recipient:</strong> ${result.recipientName}</p>
        <p><strong>Parcel Status:</strong> ${result.parcelStatus}</p>
        <p><strong>Pickup:</strong> ${result.pickupTime}</p>
        <p><strong>Dropoff:</strong> ${result.dropoffTime}</p>
        <p><strong>Service Cost:</strong> ₹${Number(result.serviceCost).toFixed(2)}</p>
        <p><strong>Payment Time:</strong> ${result.paymentTime}</p>
    `;
    summary.classList.remove('hidden');
}

function renderParcelStatusResult(result) {
    const summary = document.getElementById('parcelStatusSummary');
    if (!summary) return;
    summary.innerHTML = `
        <p><strong>Booking ID:</strong> ${result.bookingId}</p>
        <p><strong>Parcel Status:</strong> ${result.parcelStatus}</p>
        <p><strong>Pickup:</strong> ${result.pickupTime}</p>
        <p><strong>Dropoff:</strong> ${result.dropoffTime}</p>
    `;
    summary.classList.remove('hidden');
}

function renderGenericResult(result) {
    // Nothing extra to render.
}

async function postForm(url, form) {
    const formData = new FormData(form);
    const session = getSession();
    if (session && url.endsWith('/api/book')) {
        formData.append('userName', session.userName);
    }
    const response = await fetch(url, {
        method: 'POST',
        body: new URLSearchParams(formData),
    });
    return response.json();
}

function showMessage(element, message, success) {
    if (!element) return;
    element.textContent = message;
    element.classList.toggle('success', success);
    element.classList.toggle('error', !success);
}

function escapeHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}
