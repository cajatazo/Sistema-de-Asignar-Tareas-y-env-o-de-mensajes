// Main application JavaScript
document.addEventListener('DOMContentLoaded', function() {
    // Auto-dismiss alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });

    // Confirmations for destructive actions
    const deleteButtons = document.querySelectorAll('[data-confirm]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            const message = this.getAttribute('data-confirm');
            if (!confirm(message)) {
                e.preventDefault();
            }
        });
    });

    // Form validation enhancements
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function() {
            const submitButton = this.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = '<span class="loading-spinner"></span> Procesando...';
            }
        });
    });

    // Real-time notifications check
    function checkNotifications() {
        // This would typically make an API call to check for new notifications
        console.log('Checking for new notifications...');
    }

    // Check every 30 seconds
    setInterval(checkNotifications, 30000);

    // Dynamic due date formatting
    const dueDates = document.querySelectorAll('[data-due-date]');
    dueDates.forEach(element => {
        const dueDate = new Date(element.getAttribute('data-due-date'));
        const now = new Date();
        const timeDiff = dueDate - now;
        const daysDiff = Math.ceil(timeDiff / (1000 * 60 * 60 * 24));

        if (daysDiff < 0) {
            element.classList.add('text-danger');
            element.title = 'Vencida';
        } else if (daysDiff === 0) {
            element.classList.add('text-warning');
            element.title = 'Vence hoy';
        } else if (daysDiff <= 2) {
            element.classList.add('text-warning');
            element.title = `Vence en ${daysDiff} días`;
        }
    });
});

// Utility functions
const App = {
    // Format date
    formatDate: function(dateString) {
        const options = { 
            year: 'numeric', 
            month: 'short', 
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        return new Date(dateString).toLocaleDateString('es-ES', options);
    },

    // Show loading state
    showLoading: function(element) {
        element.disabled = true;
        element.innerHTML = '<span class="loading-spinner"></span> Cargando...';
    },

    // Hide loading state
    hideLoading: function(element, originalText) {
        element.disabled = false;
        element.innerHTML = originalText;
    },

    // AJAX helper
    ajax: function(url, options = {}) {
        const defaults = {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'X-Requested-With': 'XMLHttpRequest'
            }
        };

        const config = { ...defaults, ...options };

        return fetch(url, config)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            });
    }
};