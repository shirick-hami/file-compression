import {Component, State, h} from '@stencil/core';
import {apiService, CompressionResult} from '../../services/api.service';
import {downloadBase64File, formatBytes, formatTime} from '../../utils/helpers';

interface ProgressPhase {
    phase: string;
    percent: number;
}

@Component({
    tag: 'compress-tab',
    styleUrl: 'compress-tab.css',
    shadow: true,
})
export class CompressTab {
    @State() selectedFile: File | null = null;
    @State() isProcessing: boolean = false;
    @State() progress: number = 0;
    @State() progressPhase: string = '';
    @State() result: CompressionResult | null = null;
    @State() error: string | null = null;

    private progressInterval: ReturnType<typeof setInterval> | null = null;

    private readonly phases: ProgressPhase[] = [
        {phase: 'Reading file...', percent: 10},
        {phase: 'Building frequency table...', percent: 25},
        {phase: 'Building Huffman tree...', percent: 40},
        {phase: 'Generating codes...', percent: 55},
        {phase: 'Encoding data...', percent: 75},
        {phase: 'Finalizing...', percent: 90},
    ];

    private handleFileSelected = (event: CustomEvent<File>) => {
        this.selectedFile = event.detail;
        this.result = null;
        this.error = null;
    };

    private handleFileCleared = () => {
        this.selectedFile = null;
        this.result = null;
        this.error = null;
    };

    private simulateProgress = () => {
        let currentPhaseIndex = 0;

        this.progressInterval = setInterval(() => {
            if (currentPhaseIndex < this.phases.length) {
                const phase = this.phases[currentPhaseIndex];
                this.progressPhase = phase.phase;
                this.progress = phase.percent;
                currentPhaseIndex++;
            }
        }, 200);
    };

    private stopProgressSimulation = () => {
        if (this.progressInterval) {
            clearInterval(this.progressInterval);
            this.progressInterval = null;
        }
    };

    private handleCompress = async () => {
        if (!this.selectedFile) return;

        this.isProcessing = true;
        this.error = null;
        this.result = null;
        this.progress = 0;
        this.progressPhase = 'Starting...';

        this.simulateProgress();

        try {
            const result = await apiService.compress(this.selectedFile);

            this.stopProgressSimulation();

            if (result.success) {
                this.progress = 100;
                this.progressPhase = 'Complete!';
                this.result = result;
            } else {
                throw new Error(result.error || result.errorMessage || 'Compression failed');
            }
        } catch (err) {
            this.stopProgressSimulation();
            this.error = err instanceof Error ? err.message : 'Compression failed';
            this.progress = 0;
            this.progressPhase = 'Failed';
        } finally {
            this.isProcessing = false;
        }
    };

    private handleDownload = () => {
        if (this.result?.data && this.result?.fileName) {
            downloadBase64File(this.result.data, this.result.fileName);
        }
    };

    private handleReset = () => {
        this.selectedFile = null;
        this.result = null;
        this.error = null;
        this.progress = 0;
        this.progressPhase = '';
    };

    render() {
        const isComplete = this.result?.success;

        return (
            <div class="compress-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="card-icon">📦</div>
                        <div class="card-title-group">
                            <h2 class="card-title">Compress File</h2>
                            <p class="card-subtitle">
                                Reduce file size using Huffman coding algorithm
                            </p>
                        </div>
                    </div>

                    <div class="card-body">
                        <file-dropzone
                            label="Drop any file to compress"
                            hint="Supports all file types up to 100MB"
                            icon="📄"
                            variant="primary"
                            disabled={this.isProcessing}
                            onFileSelected={this.handleFileSelected}
                            onFileCleared={this.handleFileCleared}
                        />

                        {this.selectedFile && !this.result && (
                            <div class="action-section">
                                <button
                                    class="btn btn-primary"
                                    onClick={this.handleCompress}
                                    disabled={this.isProcessing}
                                >
                                    {this.isProcessing ? (
                                        <span class="btn-content">
                      <span class="spinner"/>
                      Compressing...
                    </span>
                                    ) : (
                                        <span class="btn-content">
                      <span class="btn-icon">🗜️</span>
                      Compress File
                    </span>
                                    )}
                                </button>
                            </div>
                        )}

                        {(this.isProcessing || isComplete) && (
                            <div class="progress-section">
                                <progress-bar
                                    progress={this.progress}
                                    phase={this.progressPhase}
                                    variant="primary"
                                    complete={isComplete}
                                />
                            </div>
                        )}

                        {this.error && (
                            <div class="error-message">
                                <span class="error-icon">⚠️</span>
                                <span>{this.error}</span>
                            </div>
                        )}

                        {isComplete && this.result && (
                            <div class="results-section">
                                <h3 class="results-title">Compression Results</h3>
                                <div class="results-grid">
                                    <div class="result-item">
                                        <span class="result-label">Original Size</span>
                                        <span class="result-value">
                      {this.result.formattedOriginalSize || formatBytes(this.result.originalSize || 0)}
                    </span>
                                    </div>
                                    <div class="result-item">
                                        <span class="result-label">Compressed Size</span>
                                        <span class="result-value highlight">
                      {this.result.formattedCompressedSize || formatBytes(this.result.compressedSize || 0)}
                    </span>
                                    </div>
                                    <div class="result-item">
                                        <span class="result-label">Compression Ratio</span>
                                        <span class="result-value success">
                      {this.result.formattedCompressionRatio || 'N/A'}
                    </span>
                                    </div>
                                    <div class="result-item">
                                        <span class="result-label">Processing Time</span>
                                        <span class="result-value">
                      {formatTime(this.result.processingTimeMs || 0)}
                    </span>
                                    </div>
                                </div>

                                <div class="action-buttons">
                                    <button class="btn btn-primary" onClick={this.handleDownload}>
                    <span class="btn-content">
                      <span class="btn-icon">⬇️</span>
                      Download {this.result.fileName}
                    </span>
                                    </button>
                                    <button class="btn btn-secondary" onClick={this.handleReset}>
                    <span class="btn-content">
                      <span class="btn-icon">🔄</span>
                      Compress Another
                    </span>
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div class="info-card">
                    <h3>💡 Tips for Better Compression</h3>
                    <ul>
                        <li>
                            <strong>Text files</strong> typically compress very well (50-80% reduction)
                        </li>
                        <li>
                            <strong>Already compressed files</strong> (ZIP, JPEG, MP3) may not compress further
                        </li>
                        <li>
                            <strong>Repetitive data</strong> achieves the best compression ratios
                        </li>
                        <li>
                            The <strong>.huff</strong> extension identifies Huffman compressed files
                        </li>
                    </ul>
                </div>
            </div>
        );
    }
}
