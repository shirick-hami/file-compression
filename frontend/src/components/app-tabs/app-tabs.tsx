import {Component, Event, EventEmitter, Prop, h} from '@stencil/core';

export type TabType = 'docs' | 'compress' | 'decompress';

interface TabItem {
    id: TabType;
    label: string;
    icon: string;
}

@Component({
    tag: 'app-tabs',
    styleUrl: 'app-tabs.css',
    shadow: true,
})
export class AppTabs {
    @Prop() activeTab: TabType = 'docs';

    @Event() tabChange!: EventEmitter<TabType>;

    private tabs: TabItem[] = [
        {id: 'docs', label: 'Documentation', icon: '📖'},
        {id: 'compress', label: 'Compress', icon: '📦'},
        {id: 'decompress', label: 'Decompress', icon: '📂'},
    ];

    private handleTabClick = (tabId: TabType) => {
        this.tabChange.emit(tabId);
    };

    render() {
        return (
            <nav class="tabs-container" role="tablist" aria-label="Main navigation">
                <div class="tabs">
                    {this.tabs.map((tab) => (
                        <button
                            key={tab.id}
                            role="tab"
                            aria-selected={this.activeTab === tab.id ? 'true' : 'false'}
                            aria-controls={`${tab.id}-panel`}
                            class={{
                                'tab-button': true,
                                'active': this.activeTab === tab.id,
                                [`tab-${tab.id}`]: true,
                            }}
                            onClick={() => this.handleTabClick(tab.id)}
                        >
                            <span class="tab-icon">{tab.icon}</span>
                            <span class="tab-label">{tab.label}</span>
                        </button>
                    ))}
                    <div
                        class="tab-indicator"
                        style={{
                            '--tab-index': String(this.tabs.findIndex((t) => t.id === this.activeTab)),
                        }}
                    />
                </div>
            </nav>
        );
    }
}
