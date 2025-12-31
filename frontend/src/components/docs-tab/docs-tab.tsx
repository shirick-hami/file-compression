import {Component, State, h} from '@stencil/core';
import {EXTERNAL_LINKS} from '@/constants';
import {
    getApiUrl,
    getGithubRepoCloneCommand,
    getGithubRepoHttpCloneCommand,
    getGithubUrl,
    getRestDocsUrl,
    getSwaggerUrl
} from "@utils/env.service";

type DocSection = 'overview' | 'algorithm' | 'api' | 'file-format' | 'examples' | 'about';

interface SectionItem {
    id: DocSection;
    label: string;
    icon: string;
}

@Component({
    tag: 'docs-tab',
    styleUrl: 'docs-tab.css',
    shadow: true,
})
export class DocsTab {
    @State() activeSection: DocSection = 'overview';
    @State() copiedCommand: string | null = null;

    private sections: SectionItem[] = [
        {id: 'overview', label: 'Overview', icon: '📋'},
        {id: 'algorithm', label: 'How It Works', icon: '⚙️'},
        {id: 'api', label: 'API Reference', icon: '🔌'},
        {id: 'file-format', label: 'File Format', icon: '📁'},
        {id: 'examples', label: 'Examples', icon: '💡'},
        {id: 'about', label: 'About', icon: 'ℹ️'},
    ];

    private handleSectionChange = (section: DocSection) => {
        this.activeSection = section;
    };

    private copyToClipboard = async (text: string, commandType: string) => {
        try {
            await navigator.clipboard.writeText(text);
            this.copiedCommand = commandType;
            setTimeout(() => {
                this.copiedCommand = null;
            }, 2000);
        } catch (err) {
            console.error('Failed to copy:', err);
        }
    };

    private renderOverview() {
        return (
            <div class="doc-section">
                <h2>Welcome to Huffman Compressor</h2>

                <p>
                    Huffman Compressor is a powerful, production-grade file compression tool that uses
                    the <strong>Huffman coding algorithm</strong> to achieve lossless data compression.
                    This means your files are compressed without any loss of data — the original file
                    can be perfectly restored from the compressed version.
                </p>

                <div class="feature-grid">
                    <div class="feature-card">
                        <span class="feature-icon">🗜️</span>
                        <h3>Lossless Compression</h3>
                        <p>Your data is preserved exactly. No information is lost during compression.</p>
                    </div>
                    <div class="feature-card">
                        <span class="feature-icon">⚡</span>
                        <h3>Fast Processing</h3>
                        <p>Efficient algorithms ensure quick compression and decompression times.</p>
                    </div>
                    <div class="feature-card">
                        <span class="feature-icon">📊</span>
                        <h3>Variable Compression</h3>
                        <p>Compression ratio varies based on data redundancy — more repetition means better
                            compression.</p>
                    </div>
                    <div class="feature-card">
                        <span class="feature-icon">🔒</span>
                        <h3>File Validation</h3>
                        <p>Built-in magic number and checksums ensure file integrity.</p>
                    </div>
                </div>

                <h3>Quick Start</h3>
                <ol class="numbered-list">
                    <li>
                        <strong>To compress a file:</strong> Click the "Compress" tab, drag and drop
                        any file, and click "Compress File". Download your <code>.huff</code> file.
                    </li>
                    <li>
                        <strong>To decompress a file:</strong> Click the "Decompress" tab, drag and
                        drop a <code>.huff</code> file, and click "Decompress File". Download your
                        restored original file.
                    </li>
                </ol>

                <div class="callout callout-info">
                    <span class="callout-icon">💡</span>
                    <div class="callout-content">
                        <strong>Pro Tip:</strong> Text files and files with repetitive data compress
                        best. Already-compressed files (ZIP, JPEG, MP3) may not compress further.
                    </div>
                </div>
            </div>
        );
    }

    private renderAlgorithm() {
        return (
            <div class="doc-section">
                <h2>How Huffman Coding Works</h2>

                <p>
                    Huffman coding is a lossless data compression algorithm developed by David A. Huffman
                    in 1952. It's a type of <strong>entropy encoding</strong> that assigns variable-length
                    codes to input characters based on their frequencies.
                </p>

                <h3>The Core Principle</h3>
                <p>
                    The fundamental idea is simple: <strong>characters that appear more frequently
                    get shorter codes</strong>, while characters that appear less frequently get
                    longer codes. This is similar to Morse code, where the letter 'E' (most common
                    in English) is represented by a single dot.
                </p>

                <h3>Step-by-Step Process</h3>

                <div class="step-list">
                    <div class="step">
                        <div class="step-number">1</div>
                        <div class="step-content">
                            <h4>Build Frequency Table</h4>
                            <p>
                                Count how many times each byte (character) appears in the input data.
                                For example, in "AAAAABBC", 'A' appears 5 times, 'B' appears 2 times,
                                and 'C' appears 1 time.
                            </p>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">2</div>
                        <div class="step-content">
                            <h4>Build the Huffman Tree</h4>
                            <p>
                                Create a binary tree where leaf nodes represent characters. The algorithm
                                repeatedly combines the two nodes with the lowest frequencies until only
                                one node (the root) remains.
                            </p>
                            <div class="tree-diagram">
                <pre>{`        [8]
       /   \\
     [3]   [A:5]
     / \\
  [C:1] [B:2]`}</pre>
                            </div>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">3</div>
                        <div class="step-content">
                            <h4>Generate Codes</h4>
                            <p>
                                Traverse the tree from root to each leaf. Going left adds '0', going
                                right adds '1'. The path to each character becomes its binary code.
                            </p>
                            <table class="code-table">
                                <thead>
                                <tr>
                                    <th>Character</th>
                                    <th>Frequency</th>
                                    <th>Huffman Code</th>
                                    <th>Bits Used</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr>
                                    <td>A</td>
                                    <td>5</td>
                                    <td><code>1</code></td>
                                    <td>1 bit</td>
                                </tr>
                                <tr>
                                    <td>B</td>
                                    <td>2</td>
                                    <td><code>01</code></td>
                                    <td>2 bits</td>
                                </tr>
                                <tr>
                                    <td>C</td>
                                    <td>1</td>
                                    <td><code>00</code></td>
                                    <td>2 bits</td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>

                    <div class="step">
                        <div class="step-number">4</div>
                        <div class="step-content">
                            <h4>Encode the Data</h4>
                            <p>
                                Replace each character with its Huffman code. "AAAAABBC" becomes:
                            </p>
                            <code class="code-block">1 1 1 1 1 01 01 00 = 11111010100</code>
                            <p>
                                Original: 8 bytes (64 bits) → Compressed: 11 bits + header overhead
                            </p>
                        </div>
                    </div>
                </div>

                <h3>Prefix-Free Property</h3>
                <div class="callout callout-success">
                    <span class="callout-icon">✓</span>
                    <div class="callout-content">
                        Huffman codes are <strong>prefix-free</strong>: no code is a prefix of another.
                        This ensures unambiguous decoding — you can read the compressed data bit by bit
                        and always know where each code ends.
                    </div>
                </div>

                <h3>Decompression Process</h3>
                <ol class="numbered-list">
                    <li>Read the frequency table from the file header</li>
                    <li>Rebuild the Huffman tree from the frequencies</li>
                    <li>Read the compressed data bit by bit</li>
                    <li>Traverse the tree for each bit (0 = left, 1 = right)</li>
                    <li>When reaching a leaf, output that character and restart from root</li>
                </ol>

                <h3>Time & Space Complexity</h3>
                <table class="complexity-table">
                    <thead>
                    <tr>
                        <th>Operation</th>
                        <th>Time Complexity</th>
                        <th>Space Complexity</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>Build Frequency Table</td>
                        <td>O(n)</td>
                        <td>O(k) where k = unique symbols</td>
                    </tr>
                    <tr>
                        <td>Build Huffman Tree</td>
                        <td>O(k log k)</td>
                        <td>O(k)</td>
                    </tr>
                    <tr>
                        <td>Encode Data</td>
                        <td>O(n)</td>
                        <td>O(1)</td>
                    </tr>
                    <tr>
                        <td>Decode Data</td>
                        <td>O(n)</td>
                        <td>O(1)</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        );
    }

    private renderApi() {
        return (
            <div class="doc-section">
                <h2>API Reference</h2>

                <p>
                    The Huffman Compressor backend provides a RESTful API for programmatic access.
                    All endpoints accept multipart/form-data for file uploads.
                </p>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/huffman/compress</code>
                    </div>
                    <p>Compress a file and receive the compressed binary data.</p>
                    <h4>Request</h4>
                    <pre>{`curl -X POST ${getApiUrl()}/huffman/compress \\
  -F "file=@document.txt"`}</pre>
                    <h4>Response Headers</h4>
                    <ul class="param-list">
                        <li><code>X-Original-Size</code> - Size of original file in bytes</li>
                        <li><code>X-Compressed-Size</code> - Size of compressed file in bytes</li>
                        <li><code>X-Compression-Ratio</code> - Compression ratio percentage</li>
                        <li><code>X-Processing-Time-Ms</code> - Processing time in milliseconds</li>
                    </ul>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/huffman/compress/json</code>
                    </div>
                    <p>Compress a file and receive JSON response with base64-encoded data.</p>
                    <h4>Request</h4>
                    <pre>{`curl -X POST ${getApiUrl()}/huffman/compress/json \\
  -F "file=@document.txt"`}</pre>
                    <h4>Response</h4>
                    <pre>{`{
  "success": true,
  "data": "SFVGR...(base64)...",
  "fileName": "document.txt.huff",
  "originalSize": 1024,
  "compressedSize": 512,
  "formattedOriginalSize": "1 KB",
  "formattedCompressedSize": "512 B",
  "formattedCompressionRatio": "50.00%",
  "processingTimeMs": 23
}`}</pre>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/huffman/decompress</code>
                    </div>
                    <p>Decompress a .huff file and receive the original binary data.</p>
                    <h4>Request</h4>
                    <pre>{`curl -X POST ${getApiUrl()}/huffman/decompress \\
  -F "file=@document.txt.huff"`}</pre>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/api/decompress/json</code>
                    </div>
                    <p>Decompress a .huff file and receive JSON response with base64-encoded data.</p>
                    <h4>Response</h4>
                    <pre>{`{
  "success": true,
  "data": "SGVsbG8...(base64)...",
  "fileName": "document.txt",
  "originalSize": 1024,
  "compressedSize": 512,
  "formattedOriginalSize": "1 KB",
  "formattedCompressedSize": "512 B",
  "processingTimeMs": 15
}`}</pre>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/huffman/analyze</code>
                    </div>
                    <p>Analyze a file's compression potential without compressing.</p>
                    <h4>Response</h4>
                    <pre>{`{
  "success": true,
  "originalSize": 1024,
  "estimatedCompressedSize": 620,
  "estimatedRatio": "39.45%",
  "uniqueSymbols": 42
}`}</pre>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method post">POST</span>
                        <code>/huffman/validate</code>
                    </div>
                    <p>Check if a file is a valid Huffman compressed file.</p>
                    <h4>Response</h4>
                    <pre>{`{
  "valid": true,
  "message": "Valid Huffman compressed file"
}`}</pre>
                </div>

                <div class="api-endpoint">
                    <div class="endpoint-header">
                        <span class="method get">GET</span>
                        <code>/huffman/health</code>
                    </div>
                    <p>Check API health status.</p>
                    <h4>Response</h4>
                    <pre>{`{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00Z"
}`}</pre>
                </div>

                <h3>Error Responses</h3>
                <pre>{`{
  "success": false,
  "error": "ERROR_CODE",
  "errorMessage": "Human-readable error description"
}`}</pre>

                <h4>Common Error Codes</h4>
                <ul class="param-list">
                    <li><code>INVALID_FILE</code> - Not a valid Huffman compressed file</li>
                    <li><code>FILE_TOO_LARGE</code> - File exceeds maximum size (100MB)</li>
                    <li><code>EMPTY_FILE</code> - File is empty</li>
                    <li><code>CORRUPTED_DATA</code> - Compressed data is corrupted</li>
                </ul>
            </div>
        );
    }

    private renderFileFormat() {
        return (
            <div class="doc-section">
                <h2>HUFF File Format Specification</h2>

                <p>
                    The <code>.huff</code> file format is a custom binary format designed for
                    storing Huffman-compressed data along with all metadata needed for decompression.
                </p>

                <h3>File Structure</h3>
                <div class="file-structure">
                    <table>
                        <thead>
                        <tr>
                            <th>Offset</th>
                            <th>Size</th>
                            <th>Field</th>
                            <th>Description</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>0</td>
                            <td>4 bytes</td>
                            <td>Magic Number</td>
                            <td><code>HUFF</code> (0x48 0x55 0x46 0x46)</td>
                        </tr>
                        <tr>
                            <td>4</td>
                            <td>1 byte</td>
                            <td>Version</td>
                            <td>Format version (currently 1)</td>
                        </tr>
                        <tr>
                            <td>5</td>
                            <td>8 bytes</td>
                            <td>Original Size</td>
                            <td>Size of uncompressed data (big-endian long)</td>
                        </tr>
                        <tr>
                            <td>13</td>
                            <td>4 bytes</td>
                            <td>Symbol Count</td>
                            <td>Number of unique symbols (big-endian int)</td>
                        </tr>
                        <tr>
                            <td>17</td>
                            <td>Variable</td>
                            <td>Frequency Table</td>
                            <td>Pairs of (byte, frequency) for each symbol</td>
                        </tr>
                        <tr>
                            <td>Variable</td>
                            <td>Variable</td>
                            <td>Encoded Data</td>
                            <td>Huffman-encoded bit stream</td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <h3>Frequency Table Format</h3>
                <p>For each unique symbol:</p>
                <ul class="param-list">
                    <li><code>1 byte</code> - The symbol (byte value 0-255)</li>
                    <li><code>4 bytes</code> - Frequency count (big-endian int)</li>
                </ul>

                <h3>Example Header</h3>
                <pre>{`Offset  | Hex                           | Meaning
--------|-------------------------------|------------------
0x00    | 48 55 46 46                   | "HUFF" magic
0x04    | 01                            | Version 1
0x05    | 00 00 00 00 00 00 04 00       | Original: 1024 bytes
0x0D    | 00 00 00 03                   | 3 unique symbols
0x11    | 41 00 00 02 00                | 'A' appears 512 times
0x16    | 42 00 00 01 80                | 'B' appears 384 times  
0x1B    | 43 00 00 00 80                | 'C' appears 128 times
0x20    | [encoded bits...]             | Compressed data`}</pre>

                <h3>Validation</h3>
                <div class="callout callout-warning">
                    <span class="callout-icon">⚠️</span>
                    <div class="callout-content">
                        Before decompression, the following validations are performed:
                        <ul>
                            <li>Magic number must be "HUFF" (0x48554646)</li>
                            <li>Version must be supported (currently only v1)</li>
                            <li>File must be large enough to contain header</li>
                            <li>Frequency table must be complete and valid</li>
                        </ul>
                    </div>
                </div>
            </div>
        );
    }

    private renderExamples() {
        return (
            <div class="doc-section">
                <h2>Usage Examples</h2>

                <h3>JavaScript/TypeScript</h3>
                <pre>{`// Compress a file
async function compressFile(file: File) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch('/api/compress/json', {
    method: 'POST',
    body: formData,
  });

  const result = await response.json();
  
  if (result.success) {
    // Download the compressed file
    const blob = base64ToBlob(result.data);
    downloadBlob(blob, result.fileName);
  }
}

// Decompress a file
async function decompressFile(huffFile: File) {
  const formData = new FormData();
  formData.append('file', huffFile);

  const response = await fetch('/api/decompress/json', {
    method: 'POST',
    body: formData,
  });

  const result = await response.json();
  
  if (result.success) {
    const blob = base64ToBlob(result.data);
    downloadBlob(blob, result.fileName);
  }
}`}</pre>

                <h3>Python</h3>
                <pre>{`import requests
import base64

# Compress a file
def compress_file(filepath):
    with open(filepath, 'rb') as f:
        files = {'file': f}
        response = requests.post(
            'http://localhost:8080/api/compress/json',
            files=files
        )
    
    result = response.json()
    if result['success']:
        data = base64.b64decode(result['data'])
        with open(result['fileName'], 'wb') as f:
            f.write(data)
        print(f"Compressed: {result['formattedCompressionRatio']}")

# Decompress a file
def decompress_file(filepath):
    with open(filepath, 'rb') as f:
        files = {'file': f}
        response = requests.post(
            'http://localhost:8080/api/decompress/json',
            files=files
        )
    
    result = response.json()
    if result['success']:
        data = base64.b64decode(result['data'])
        with open(result['fileName'], 'wb') as f:
            f.write(data)`}</pre>

                <h3>cURL</h3>
                <pre>{`# Compress a file (binary response)
curl -X POST http://localhost:8080/api/compress \\
  -F "file=@document.txt" \\
  -o document.txt.huff

# Decompress a file (binary response)
curl -X POST http://localhost:8080/api/decompress \\
  -F "file=@document.txt.huff" \\
  -o document.txt

# Check compression statistics
curl -X POST http://localhost:8080/api/analyze \\
  -F "file=@document.txt"

# Validate a .huff file
curl -X POST http://localhost:8080/api/validate \\
  -F "file=@document.txt.huff"`}</pre>

                <h3>Java</h3>
                <pre>{`import java.net.http.*;
import java.nio.file.*;

public class HuffmanClient {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final String BASE_URL = "http://localhost:8080";

    public static byte[] compress(Path file) throws Exception {
        String boundary = "----FormBoundary" + System.currentTimeMillis();
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/compress"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(createMultipartBody(file, boundary))
            .build();

        HttpResponse<byte[]> response = client.send(
            request, 
            HttpResponse.BodyHandlers.ofByteArray()
        );
        
        return response.body();
    }
}`}</pre>

                <h3>Expected Compression Ratios</h3>
                <table class="examples-table">
                    <thead>
                    <tr>
                        <th>File Type</th>
                        <th>Expected Ratio</th>
                        <th>Notes</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>Plain Text (.txt)</td>
                        <td>40-60%</td>
                        <td>English text compresses well</td>
                    </tr>
                    <tr>
                        <td>Source Code</td>
                        <td>50-70%</td>
                        <td>Repetitive keywords help</td>
                    </tr>
                    <tr>
                        <td>Log Files</td>
                        <td>60-80%</td>
                        <td>Highly repetitive content</td>
                    </tr>
                    <tr>
                        <td>CSV/JSON</td>
                        <td>50-70%</td>
                        <td>Structured data with patterns</td>
                    </tr>
                    <tr>
                        <td>Binary Executables</td>
                        <td>10-30%</td>
                        <td>Less redundancy</td>
                    </tr>
                    <tr>
                        <td>Already Compressed</td>
                        <td>0-5%</td>
                        <td>ZIP, JPEG, MP3 won't compress</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        );
    }

    private renderAbout() {
        return (
            <div class="doc-section">
                <h2>About Huffman Compressor</h2>

                <p>
                    Huffman Compressor is an open-source file compression tool built with modern technologies.
                    It demonstrates the practical application of Huffman coding algorithm for lossless data compression.
                </p>

                {/* License Section */}
                <h3>📜 License</h3>
                <div class="about-card">
                    <div class="license-badge">
                        <span class="license-icon">⚖️</span>
                        <span class="license-text">MIT License</span>
                    </div>
                    <p>
                        This project is licensed under the <strong>MIT License</strong> - a permissive open-source
                        license
                        that allows you to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
                        of the software.
                    </p>
                    <a
                        href={EXTERNAL_LINKS.MIT_LICENSE}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="about-link"
                    >
                        <span class="link-icon">🔗</span>
                        View MIT License Terms
                        <span class="external-icon">↗</span>
                    </a>
                </div>

                {/* Credits Section */}
                <h3>👏 Credits</h3>
                <div class="about-card">
                    <div class="credits-grid">
                        <div class="credit-item">
                            <h4>Algorithm</h4>
                            <p>
                                Based on <strong>Huffman Coding</strong>, invented by David A. Huffman in 1952
                                while he was a Ph.D. student at MIT.
                            </p>
                        </div>
                        <div class="credit-item">
                            <h4>Backend</h4>
                            <p>
                                Built with <strong>Spring Boot</strong> (Java) - A powerful framework for
                                building production-ready applications.
                            </p>
                        </div>
                        <div class="credit-item">
                            <h4>Frontend</h4>
                            <p>
                                Built with <strong>Stencil.js</strong> - A Web Component compiler for building
                                fast, reusable UI components.
                            </p>
                        </div>
                        <div class="credit-item">
                            <h4>Icons</h4>
                            <p>
                                Using native <strong>Emoji</strong> icons for cross-platform compatibility
                                and lightweight design.
                            </p>
                        </div>
                    </div>
                </div>

                {/* API Documentation Links */}
                <h3>📚 API Documentation</h3>
                <div class="about-card">
                    <p>
                        Explore our interactive API documentation powered by Swagger/OpenAPI:
                    </p>
                    <div class="links-grid">
                        <a
                            href={getSwaggerUrl()}
                            target="_blank"
                            rel="noopener noreferrer"
                            class="doc-link-card"
                        >
                            <span class="doc-link-icon">🔍</span>
                            <div class="doc-link-content">
                                <strong>Swagger UI</strong>
                                <span>Interactive API explorer</span>
                            </div>
                            <span class="external-icon">↗</span>
                        </a>
                        <a
                            href={getRestDocsUrl()}
                            target="_blank"
                            rel="noopener noreferrer"
                            class="doc-link-card"
                        >
                            <span class="doc-link-icon">📄</span>
                            <div class="doc-link-content">
                                <strong>OpenAPI Spec</strong>
                                <span>JSON/YAML specification</span>
                            </div>
                            <span class="external-icon">↗</span>
                        </a>
                    </div>
                </div>

                {/* GitHub Section */}
                <h3>🐙 Source Code</h3>
                <div class="about-card github-card">
                    <div class="github-header">
            <span class="github-icon">
              <svg viewBox="0 0 24 24" width="32" height="32" fill="currentColor">
                <path
                    d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
              </svg>
            </span>
                        <div class="github-info">
                            <strong>huffman-compressor</strong>
                            <span>View source code and contribute</span>
                        </div>
                    </div>
                    <a
                        href={getGithubUrl()}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="about-link github-link"
                    >
                        <span class="link-icon">🔗</span>
                        Visit GitHub Repository
                        <span class="external-icon">↗</span>
                    </a>
                </div>

                {/* Clone Commands */}
                <h3>⬇️ Clone Repository</h3>
                <div class="about-card">
                    <p>Get started by cloning the repository:</p>

                    <div class="clone-section">
                        <label>HTTPS</label>
                        <div class="clone-command">
                            <code>{getGithubRepoHttpCloneCommand()}</code>
                            <button
                                class={{
                                    'copy-btn': true,
                                    'copied': this.copiedCommand === 'https'
                                }}
                                onClick={() => this.copyToClipboard(getGithubRepoHttpCloneCommand(), 'https')}
                                title="Copy to clipboard"
                            >
                                {this.copiedCommand === 'https' ? '✓' : '📋'}
                            </button>
                        </div>
                    </div>

                    <div class="clone-section">
                        <label>SSH</label>
                        <div class="clone-command">
                            <code>{getGithubRepoCloneCommand()}</code>
                            <button
                                class={{
                                    'copy-btn': true,
                                    'copied': this.copiedCommand === 'ssh'
                                }}
                                onClick={() => this.copyToClipboard(getGithubRepoCloneCommand(), 'ssh')}
                                title="Copy to clipboard"
                            >
                                {this.copiedCommand === 'ssh' ? '✓' : '📋'}
                            </button>
                        </div>
                    </div>
                </div>

                {/* Quick Links */}
                <h3>🔗 Quick Links</h3>
                <div class="quick-links-grid">
                    <a
                        href={getGithubUrl()}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="quick-link"
                    >
                        <span>🐙</span>
                        GitHub
                    </a>
                    <a
                        href={getSwaggerUrl()}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="quick-link"
                    >
                        <span>🔍</span>
                        Swagger UI
                    </a>
                    <a
                        href={getRestDocsUrl()}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="quick-link"
                    >
                        <span>📄</span>
                        API Docs
                    </a>
                    <a
                        href={EXTERNAL_LINKS.MIT_LICENSE}
                        target="_blank"
                        rel="noopener noreferrer"
                        class="quick-link"
                    >
                        <span>⚖️</span>
                        License
                    </a>
                </div>

                <div class="callout callout-info">
                    <span class="callout-icon">💡</span>
                    <div class="callout-content">
                        <strong>Contributions Welcome!</strong> Found a bug or want to add a feature?
                        Feel free to open an issue or submit a pull request on GitHub.
                    </div>
                </div>
            </div>
        );
    }

    render() {
        return (
            <div class="docs-tab">
                <div class="docs-layout">
                    <nav class="docs-sidebar">
                        <ul class="sidebar-nav">
                            {this.sections.map((section) => (
                                <li key={section.id}>
                                    <button
                                        class={{
                                            'sidebar-link': true,
                                            'active': this.activeSection === section.id,
                                        }}
                                        onClick={() => this.handleSectionChange(section.id)}
                                    >
                                        <span class="sidebar-icon">{section.icon}</span>
                                        <span>{section.label}</span>
                                    </button>
                                </li>
                            ))}
                        </ul>
                    </nav>

                    <div class="docs-content">
                        <div class="content-card">
                            {this.activeSection === 'overview' && this.renderOverview()}
                            {this.activeSection === 'algorithm' && this.renderAlgorithm()}
                            {this.activeSection === 'api' && this.renderApi()}
                            {this.activeSection === 'file-format' && this.renderFileFormat()}
                            {this.activeSection === 'examples' && this.renderExamples()}
                            {this.activeSection === 'about' && this.renderAbout()}
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}
