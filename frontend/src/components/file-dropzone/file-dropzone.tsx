import {Component, Event, EventEmitter, Prop, State, h} from '@stencil/core';
import {formatBytes} from '../../utils/helpers';

@Component({
    tag: 'file-dropzone',
    styleUrl: 'file-dropzone.css',
    shadow: true,
})
export class FileDropzone {
    @Prop() accept?: string;
    @Prop() label: string = 'Drop your file here';
    @Prop() hint: string = 'or click to browse';
    @Prop() icon: string = '📄';
    @Prop() variant: 'primary' | 'secondary' = 'primary';
    @Prop() disabled: boolean = false;

    @State() isDragOver: boolean = false;
    @State() selectedFile: File | null = null;

    @Event() fileSelected!: EventEmitter<File>;
    @Event() fileCleared!: EventEmitter<void>;

    private fileInput!: HTMLInputElement;

    private handleDragOver = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        if (!this.disabled) {
            this.isDragOver = true;
        }
    };

    private handleDragLeave = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        this.isDragOver = false;
    };

    private handleDrop = (e: DragEvent) => {
        e.preventDefault();
        e.stopPropagation();
        this.isDragOver = false;

        if (this.disabled) return;

        const files = e.dataTransfer?.files;
        if (files && files.length > 0) {
            this.selectFile(files[0]);
        }
    };

    private handleClick = () => {
        if (!this.disabled) {
            this.fileInput.click();
        }
    };

    private handleFileInput = (e: Event) => {
        const input = e.target as HTMLInputElement;
        if (input.files && input.files.length > 0) {
            this.selectFile(input.files[0]);
        }
    };

    private selectFile = (file: File) => {
        this.selectedFile = file;
        this.fileSelected.emit(file);
    };

    private clearFile = (e: Event) => {
        e.stopPropagation();
        this.selectedFile = null;
        this.fileInput.value = '';
        this.fileCleared.emit();
    };

    private getFileIcon = (filename: string): string => {
        const ext = filename.split('.').pop()?.toLowerCase();
        const iconMap: Record<string, string> = {
            txt: '📝',
            pdf: '📕',
            doc: '📘',
            docx: '📘',
            xls: '📗',
            xlsx: '📗',
            png: '🖼️',
            jpg: '🖼️',
            jpeg: '🖼️',
            gif: '🖼️',
            zip: '🗜️',
            huff: '🗜️',
            js: '📜',
            ts: '📜',
            html: '🌐',
            css: '🎨',
            json: '📋',
        };
        return iconMap[ext || ''] || '📄';
    };

    render() {
        return (
            <div
                class={{
                    'dropzone': true,
                    'drag-over': this.isDragOver,
                    'has-file': !!this.selectedFile,
                    'disabled': this.disabled,
                    [`variant-${this.variant}`]: true,
                }}
                onDragOver={this.handleDragOver}
                onDragLeave={this.handleDragLeave}
                onDrop={this.handleDrop}
                onClick={this.handleClick}
                role="button"
                tabIndex={this.disabled ? -1 : 0}
                aria-label={this.label}
            >
                <input
                    type="file"
                    class="file-input"
                    accept={this.accept}
                    onChange={this.handleFileInput}
                    ref={(el) => (this.fileInput = el as HTMLInputElement)}
                    disabled={this.disabled}
                />

                {this.selectedFile ? (
                    <div class="file-preview">
                        <span class="file-icon">{this.getFileIcon(this.selectedFile.name)}</span>
                        <div class="file-info">
                            <span class="file-name">{this.selectedFile.name}</span>
                            <span class="file-size">{formatBytes(this.selectedFile.size)}</span>
                        </div>
                        <button
                            class="clear-button"
                            onClick={this.clearFile}
                            aria-label="Remove file"
                            title="Remove file"
                        >
                            ✕
                        </button>
                    </div>
                ) : (
                    <div class="dropzone-content">
                        <span class="dropzone-icon">{this.icon}</span>
                        <p class="dropzone-label">{this.label}</p>
                        <p class="dropzone-hint">{this.hint}</p>
                    </div>
                )}
            </div>
        );
    }
}
