import {Component, State, h} from '@stencil/core';
import {apiService, DecompressionResult} from '../../services/api.service';
import {downloadBase64File, formatBytes, formatTime, isHuffFile} from '../../utils/helpers';

interface ProgressPhase {
    phase: string;
    percent: number;
}

@Component({
    tag: 'decompress-tab',
    styleUrl: 'decompress-tab.css',
    shadow: true,
})
export class DecompressTab {
    @State() selectedFile: File | null = null;
    @State() isProcessing: boolean = false;
    @State() progress: number = 0;
    @State() progressPhase: string = '';
    @State() result: DecompressionResult | null = null;
    @State() error: string | null = null;
    @State() validationError: string | null = null;

    private progressInterval: ReturnType<typeof setInterval> | null = null;

    private readonly phases: ProgressPhase[] = [
        {phase: 'Reading compressed file...', percent: 10},
        {phase: 'Validating format...', percent: 25},
        {phase: 'Rebuilding Huffman tree...', percent: 45},
        {phase: 'Decoding data...', percent: 70},
        {phase: 'Finalizing...', percent: 90},
    ];

    private handleFileSelected = async (event: CustomEvent<File>) => {
        const file = event.detail;
        this.result = null;
        this.error = null;
        this.validationError = null;

        // Validate that it's a .huff file
        if (!isHuffFile(file.name)) {
            this.validationError = 'Please select a .huff file. Only Huffman compressed files can be decompressed.';
            this.selectedFile = null;
            return;
        }

        // Validate with backend
        const validation = await apiService.validate(file);
        if (!validation.valid) {
            this.validationError = validation.message || 'This file is not a valid Huffman compressed file.';
            this.selectedFile = null;
            return;
        }

        this.selectedFile = file;
    };

    private handleFileCleared = () => {
        this.selectedFile = null;
        this.result = null;
        this.error = null;
        this.validationError = null;
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

    private handleDecompress = async () => {
        if (!this.selectedFile) return;

        this.isProcessing = true;
        this.error = null;
        this.result = null;
        this.progress = 0;
        this.progressPhase = 'Starting...';

        this.simulateProgress();

        try {
            const result = await apiService.decompress(this.selectedFile);

            this.stopProgressSimulation();

            if (result.success) {
                this.progress = 100;
                this.progressPhase = 'Complete!';
                this.result = result;
            } else {
                throw new Error(result.error || result.errorMessage || 'Decompression failed');
            }
        } catch (err) {
            this.stopProgressSimulation();
            this.error = err instanceof Error ? err.message : 'Decompression failed';
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
        this.validationError = null;
        this.progress = 0;
        this.progressPhase = '';
    };

    private calculateExpansionRatio(): string {
        if (this.result?.compressedSize && this.result?.originalSize) {
            return (this.result.originalSize / this.result.compressedSize).toFixed(2) + 'x';
        }
        return 'N/A';
    }

    render() {
        const isComplete = this.result?.success;

        return (
            <div class="decompress-tab">
                <div class="card">
                    <div class="card-header">
                        <div class="card-icon">📂</div>
                        <div class="card-title-group">
                            <h2 class="card-title">Decompress File</h2>
                            <p class="card-subtitle">
                                Restore original files from .huff compressed format
                            </p>
                        </div>
                    </div>

                    <div class="card-body">
                        <file-dropzone
                            label="Drop a .huff file to decompress"
                            hint="Only Huffman compressed files are supported"
                            icon="🗜️"
                            variant="secondary"
                            accept=".huff"
                            disabled={this.isProcessing}
                            onFileSelected={this.handleFileSelected}
                            onFileCleared={this.handleFileCleared}
                        />

                        {this.validationError && (
                            <div class="warning-message">
                                <span class="warning-icon">⚠️</span>
                                <span>{this.validationError}</span>
                            </div>
                        )}

                        {this.selectedFile && !this.result && (
                            <div class="action-section">
                                <button
                                    class="btn btn-secondary-action"
                                    onClick={this.handleDecompress}
                                    disabled={this.isProcessing}
                                >
                                    {this.isProcessing ? (
                                        <span class="btn-content">
                      <span class="spinner"/>
                      Decompressing...
                    </span>
                                    ) : (
                                        <span class="btn-content">
                      <span class="btn-icon">📤</span>
                      Decompress File
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
                                    variant="secondary"
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
                                <h3 class="results-title">Decompression Results</h3>
                                <div class="results-grid">
                                    <div class="result-item">
                                        <span class="result-label">Compressed Size</span>
                                        <span class="result-value">
                      {this.result.formattedCompressedSize || formatBytes(this.result.compressedSize || 0)}
                    </span>
                                    </div>
                                    <div class="result-item">
                                        <span class="result-label">Original Size</span>
                                        <span class="result-value highlight">
                      {this.result.formattedOriginalSize || formatBytes(this.result.originalSize || 0)}
                    </span>
                                    </div>
                                    <div class="result-item">
                                        <span class="result-label">Expansion Ratio</span>
                                        <span class="result-value success">
                      {this.calculateExpansionRatio()}
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
                                    <button class="btn btn-secondary-action" onClick={this.handleDownload}>
                    <span class="btn-content">
                      <span class="btn-icon">⬇️</span>
                      Download {this.result.fileName}
                    </span>
                                    </button>
                                    <button class="btn btn-outline" onClick={this.handleReset}>
                    <span class="btn-content">
                      <span class="btn-icon">🔄</span>
                      Decompress Another
                    </span>
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>

                <div class="info-card">
                    <h3>ℹ️ About Decompression</h3>
                    <ul>
                        <li>
                            <strong>Lossless restoration</strong> - Original file is perfectly recovered
                        </li>
                        <li>
                            <strong>File integrity</strong> - Compressed files include validation headers
                        </li>
                        <li>
                            <strong>Original filename</strong> - The output filename is preserved
                        </li>
                        <li>
                            <strong>Any file type</strong> - Works with text, binary, images, etc.
                        </li>
                    </ul>
                </div>
            </div>
        );
    }
}
