# Architecture

## Overview

localgrok follows **Clean Architecture** principles with clear separation of concerns:

```
com.localgrok/
├── data/
│   ├── local/              # Room database layer
│   │   ├── dao/            # Data Access Objects (ChatDao, MessageDao)
│   │   ├── entity/         # Database entities (ChatEntity, MessageEntity)
│   │   └── LocalGrokDatabase.kt
│   ├── remote/             # Network layer
│   │   ├── model/          # API response models
│   │   ├── OllamaApiService.kt    # Ollama REST API client
│   │   ├── OllamaClient.kt        # Retrofit setup for Ollama
│   │   └── SearxngService.kt      # SearXNG API client
│   └── repository/         # Data repository layer
│       ├── ChatRepository.kt      # Chat & message operations
│       └── SettingsRepository.kt  # User preferences (DataStore)
├── domain/                 # Business logic
│   └── SystemPromptBuilder.kt    # System prompt generation
├── ui/
│   ├── components/         # Reusable Compose components
│   │   ├── chat/          # Chat-specific components
│   │   │   ├── ChatInputBar.kt
│   │   │   ├── MessageBubble.kt
│   │   │   ├── ThinkingIndicator.kt
│   │   │   └── ToolStatus.kt
│   │   ├── ChatComponents.kt
│   │   └── ModelSelectorSheet.kt
│   ├── navigation/        # Navigation setup
│   │   └── Navigation.kt
│   ├── screens/           # Screen composables
│   │   ├── ChatScreen.kt
│   │   └── SettingsScreen.kt
│   ├── theme/             # UI theming
│   │   ├── Color.kt       # Color palettes
│   │   ├── Theme.kt       # Material theme
│   │   └── Type.kt        # Typography
│   └── viewmodel/         # ViewModels (MVVM)
│       ├── ChatViewModel.kt
│       └── SettingsViewModel.kt
├── LocalGrokApplication.kt # Application class (DI)
└── MainActivity.kt         # Entry point
```

## Data Flow

1. **UI Layer** (Compose) → Observes `StateFlow` from ViewModels
2. **ViewModel** → Handles UI logic, coordinates between UI and Repository
3. **Repository** → Manages data sources (Room DB, Remote APIs)
4. **Data Sources** → Room (local) and Retrofit (network)

## Design Patterns

- **MVVM**: ViewModels expose StateFlow for reactive UI updates
- **Repository Pattern**: Single source of truth for data operations
- **Dependency Injection**: Manual DI via Application class
- **Flow-based**: Kotlin Coroutines and Flow for asynchronous operations

## Tech Stack

| Category | Technology |
|----------|-----------|
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (Material 3) |
| **Architecture** | MVVM with Clean Architecture |
| **Networking** | Retrofit 2.11.0 + OkHttp 4.12.0 |
| **Serialization** | Kotlinx Serialization JSON |
| **Local Database** | Room 2.6.1 |
| **Preferences** | DataStore Preferences 1.1.1 |
| **Image Loading** | Coil 2.7.0 |
| **Markdown** | multiplatform-markdown-renderer-m3 |
| **Navigation** | Navigation Compose 2.8.4 |
| **Coroutines** | Kotlinx Coroutines 1.9.0 |
| **Dependency Injection** | Manual DI (no external library) |
| **Build System** | Gradle 8.13.1 with Kotlin DSL |

## Theme & Design

### Color Palettes

**Space Theme** (Default):
- Background: `#050505` (Near-black)
- Surface: `#0C0C0E`
- Text Primary: `#F5F5F7`
- Accent: `#0A84FF` (Blue)

**Dark Theme**:
- Background: `#1C1C1E` (Dark grey)
- Surface: `#242426`
- Text Primary: `#FFFFFF`
- Accent: `#0A84FF` (Blue)

### Typography

- **Primary Font**: Space Grotesk (Headings, UI elements)
- **Monospace Font**: JetBrains Mono (Code blocks, terminal-like elements)
- **Body Font**: Inter (General text)