# Setup Guide

Complete setup instructions for localgrok.

## 1. Download Fonts

The app uses custom fonts for its distinctive terminal aesthetic. Run the provided script:

```bash
cd scripts
chmod +x download_fonts.sh
./download_fonts.sh
```

**Or manually download and place these fonts in `app/src/main/res/font/`:**

- **JetBrains Mono**: [Download](https://www.jetbrains.com/lp/mono/)
  - `jetbrains_mono_regular.ttf`
  - `jetbrains_mono_medium.ttf`
  - `jetbrains_mono_bold.ttf`
- **Space Grotesk**: [Download from Google Fonts](https://fonts.google.com/specimen/Space+Grotesk)
  - `space_grotesk_bold.ttf`
  - `space_grotesk_medium.ttf`
- **Inter**: Already included in the project

## 2. Setup Ollama Server

On your server machine (PC, Mac, or Linux):

```bash
# Install Ollama
curl -fsSL https://ollama.com/install.sh | sh

# Pull a model (e.g., llama3.2 or qwen2.5)
ollama pull llama3.2

# Start Ollama with network binding
OLLAMA_HOST=0.0.0.0 ollama serve
```

> **⚠️ Important**: By default, Ollama only listens on `localhost`. You **must** set `OLLAMA_HOST=0.0.0.0` to allow connections from other devices on your network.

**For persistent network access**, add to your shell profile (`~/.bashrc`, `~/.zshrc`, etc.):

```bash
export OLLAMA_HOST=0.0.0.0
```

## 3. (Optional) Setup SearXNG for Web Search

SearXNG enables web search capabilities when the brain toggle (💡) is enabled:

```bash
# Using Docker (recommended)
docker run -d \
  --name searxng \
  -p 8888:8080 \
  -v "${PWD}/searxng:/etc/searxng:rw" \
  -e "BASE_URL=http://localhost:8888/" \
  searxng/searxng:latest

# Or install directly
# See https://docs.searxng.org/admin/installation.html
```

## 4. Find Your Server IP Address

On your server machine, find your local IP address:

**Linux/Mac:**
```bash
ip addr | grep "inet " | grep -v 127.0.0.1
# Or
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Windows:**
```cmd
ipconfig
```

Your IP will look like `192.168.1.XX` or `10.0.0.XX`.

## 5. Configure the App

1. Open **localgrok** on your Android device
2. Tap the **Settings** icon (⚙️) in the top right
3. Enter your **Server IP** address (e.g., `192.168.1.50`)
4. Set **Ollama Port** to `11434` (default)
5. (Optional) Set **SearXNG Port** to `8888` (default) if you have SearXNG running
6. Tap **"Test Connection"** to verify
7. Tap **"Save Settings"**

That's it! You're ready to chat with your local LLM.

## Usage

### Basic Chat

1. **Start a New Chat**: Tap the menu icon (☰) to open the sidebar, then tap "New Chat"
2. **Send Messages**: Type your message in the input field and tap send or press Enter
3. **View Responses**: Responses stream in real-time as the AI generates them

### Advanced Features

#### Brain Toggle (💡)
- **Enabled**: AI can use reasoning mode and web search tools (requires SearXNG)
- **Disabled**: Standard chat mode without tools
- Toggle via the lightbulb icon in the chat input bar

#### Model Selection
- Tap the model name in the input bar to see available models
- Select a different model for each chat
- Model preference is saved and persisted across sessions

#### Multiple Chats
- Swipe right or tap the menu icon to access your chat list
- Tap any chat to resume the conversation
- Long-press to delete individual chats
- Use "Delete All" option in settings to clear all chats

#### Themes
- Access settings via the gear icon
- Choose between **Space** (jet black) and **Dark** (grey) themes
- Theme preference is saved automatically