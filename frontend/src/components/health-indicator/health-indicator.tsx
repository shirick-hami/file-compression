import {Component, State, Prop, h, Host} from '@stencil/core';
import {apiService, HealthResult} from '../../services';

export type HealthStatus = 'UP' | 'DOWN' | 'CHECKING' | 'UNKNOWN';

@Component({
    tag: 'health-indicator',
    styleUrl: 'health-indicator.css',
    shadow: true,
})
export class HealthIndicator {
    /**
     * Polling interval in seconds (default: 30 seconds)
     */
    @Prop() pollInterval: number = 30;

    /**
     * Whether to show the status text (default: true)
     */
    @Prop() showText: boolean = true;

    /**
     * Whether to show the last checked time (default: false)
     */
    @Prop() showLastChecked: boolean = false;

    /**
     * Current health status
     */
    @State() status: HealthStatus = 'CHECKING';

    /**
     * Last successful health check timestamp
     */
    @State() lastChecked: Date | null = null;

    /**
     * Response time in milliseconds
     */
    @State() responseTime: number | null = null;

    /**
     * Is currently checking
     */
    @State() isChecking: boolean = false;

    private pollingTimer: number | null = null;

    componentWillLoad() {
        // Initial health check
        this.checkHealth();
    }

    componentDidLoad() {
        // Start polling
        this.startPolling();
    }

    disconnectedCallback() {
        // Clean up timer when component is removed
        this.stopPolling();
    }

    /**
     * Start the polling timer
     */
    private startPolling() {
        if (this.pollingTimer) {
            this.stopPolling();
        }

        // Convert seconds to milliseconds
        const intervalMs = this.pollInterval * 1000;

        this.pollingTimer = window.setInterval(() => {
            this.checkHealth();
        }, intervalMs);
    }

    /**
     * Stop the polling timer
     */
    private stopPolling() {
        if (this.pollingTimer) {
            window.clearInterval(this.pollingTimer);
            this.pollingTimer = null;
        }
    }

    /**
     * Check the health of the backend service
     */
    private async checkHealth() {
        if (this.isChecking) return;

        this.isChecking = true;
        const startTime = performance.now();

        try {
            const result: HealthResult = await apiService.health();
            const endTime = performance.now();

            this.responseTime = Math.round(endTime - startTime);
            this.lastChecked = new Date();

            if (result.status === 'UP') {
                this.status = 'UP';
            } else {
                this.status = 'DOWN';
            }
        } catch (error) {
            console.error('Health check failed:', error);
            this.status = 'DOWN';
            this.responseTime = null;
        } finally {
            this.isChecking = false;
        }
    }

    /**
     * Manual refresh handler
     */
    private handleManualRefresh = () => {
        this.checkHealth();
    };

    /**
     * Format the last checked time
     */
    private formatLastChecked(): string {
        if (!this.lastChecked) return 'Never';

        const now = new Date();
        const diffMs = now.getTime() - this.lastChecked.getTime();
        const diffSec = Math.floor(diffMs / 1000);

        if (diffSec < 5) return 'Just now';
        if (diffSec < 60) return `${diffSec}s ago`;

        const diffMin = Math.floor(diffSec / 60);
        if (diffMin < 60) return `${diffMin}m ago`;

        return this.lastChecked.toLocaleTimeString();
    }

    /**
     * Get status text
     */
    private getStatusText(): string {
        switch (this.status) {
            case 'UP':
                return 'Online';
            case 'DOWN':
                return 'Offline';
            case 'CHECKING':
                return 'Checking...';
            default:
                return 'Unknown';
        }
    }

    /**
     * Get status icon
     */
    private getStatusIcon(): string {
        switch (this.status) {
            case 'UP':
                return '●';
            case 'DOWN':
                return '●';
            case 'CHECKING':
                return '◌';
            default:
                return '○';
        }
    }

    render() {
        return (
            <Host>
                <div
                    class={{
                        'health-indicator': true,
                        'status-up': this.status === 'UP',
                        'status-down': this.status === 'DOWN',
                        'status-checking': this.status === 'CHECKING',
                        'status-unknown': this.status === 'UNKNOWN',
                    }}
                >
                    <button
                        class="indicator-btn"
                        onClick={this.handleManualRefresh}
                        title={`Backend Status: ${this.getStatusText()}${this.responseTime ? ` (${this.responseTime}ms)` : ''}\nClick to refresh\nPolling every ${this.pollInterval}s`}
                        disabled={this.isChecking}
                    >
            <span class={{'status-dot': true, 'pulse': this.status === 'UP', 'checking': this.isChecking}}>
              {this.getStatusIcon()}
            </span>

                        {this.showText && (
                            <span class="status-text">{this.getStatusText()}</span>
                        )}

                        {this.responseTime !== null && this.status === 'UP' && (
                            <span class="response-time">{this.responseTime}ms</span>
                        )}
                    </button>

                    {this.showLastChecked && this.lastChecked && (
                        <span class="last-checked">
              Last checked: {this.formatLastChecked()}
            </span>
                    )}
                </div>
            </Host>
        );
    }
}
