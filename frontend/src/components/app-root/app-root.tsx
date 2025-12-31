import {Component, State, h} from '@stencil/core';
import {getPollingHealthInSeconds} from "@utils/env.service";

export type TabType = 'docs' | 'compress' | 'decompress';

@Component({
    tag: 'app-root',
    styleUrl: 'app-root.css',
    shadow: true,
})
export class AppRoot {
    @State() activeTab: TabType = 'docs';

    private handleTabChange = (tab: TabType) => {
        this.activeTab = tab;
    };

    render() {
        return (
            <div class="app-container">
                <header class="app-header">
                    <div class="header-content">
                        <div class="header-top">
                            <div class="logo">
                                <span class="logo-icon">🗜️</span>
                                <h1>Huffman Compressor</h1>
                            </div>
                            <health-indicator
                                pollInterval={getPollingHealthInSeconds()}
                                showText={true}
                                showLastChecked={true}
                            />
                        </div>
                        <p class="tagline">
                            Lossless data compression using Huffman coding algorithm
                        </p>
                    </div>
                </header>

                <main class="app-main">
                    <div class="container">
                        <app-tabs
                            activeTab={this.activeTab}
                            onTabChange={(e: CustomEvent<TabType>) => this.handleTabChange(e.detail)}
                        />

                        <div class="tab-content">
                            {this.activeTab === 'docs' && <docs-tab/>}
                            {this.activeTab === 'compress' && <compress-tab/>}
                            {this.activeTab === 'decompress' && <decompress-tab/>}
                        </div>
                    </div>
                </main>

                <footer class="app-footer">
                    <div class="container">
                        <p>
                            Built with{' '}
                            <a href="https://stenciljs.com" target="_blank" rel="noopener noreferrer">
                                Stencil.js
                            </a>{' '}
                            &{' '}
                            <a href="https://spring.io/projects/spring-boot" target="_blank" rel="noopener noreferrer">
                                Spring Boot
                            </a>
                        </p>
                        <p class="copyright">© 2024 Huffman Compressor</p>
                    </div>
                </footer>
            </div>
        );
    }
}
