import {Component, Prop, h} from '@stencil/core';

@Component({
    tag: 'progress-bar',
    styleUrl: 'progress-bar.css',
    shadow: true,
})
export class ProgressBar {
    @Prop() progress: number = 0;
    @Prop() phase: string = '';
    @Prop() variant: 'primary' | 'secondary' = 'primary';
    @Prop() showPercentage: boolean = true;
    @Prop() animated: boolean = true;
    @Prop() complete: boolean = false;

    render() {
        const clampedProgress = Math.min(100, Math.max(0, this.progress));

        return (
            <div
                class={{
                    'progress-container': true,
                    [`variant-${this.variant}`]: true,
                    'complete': this.complete,
                }}
            >
                {this.phase && (
                    <div class="progress-header">
                        <span class="progress-phase">{this.phase}</span>
                        {this.showPercentage && (
                            <span class="progress-percentage">{Math.round(clampedProgress)}%</span>
                        )}
                    </div>
                )}

                <div class="progress-track">
                    <div
                        class={{
                            'progress-fill': true,
                            'animated': this.animated && !this.complete,
                        }}
                        style={{width: `${clampedProgress}%`}}
                        role="progressbar"
                        aria-valuenow={clampedProgress}
                        aria-valuemin={0}
                        aria-valuemax={100}
                        aria-label={this.phase || 'Progress'}
                    />
                </div>
            </div>
        );
    }
}
