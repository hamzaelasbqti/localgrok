# Troubleshooting

## Connection Issues

### "Connection Failed" or "Connection Error"

1. ✅ Verify Ollama is running on your server:
   ```bash
   ollama serve
   ```

2. ✅ Ensure Ollama is bound to all network interfaces:
   ```bash
   OLLAMA_HOST=0.0.0.0 ollama serve
   ```

3. ✅ Check both devices are on the same Wi-Fi network
   - Phone and server must be on the same subnet

4. ✅ Verify server IP address is correct
   - Check server IP hasn't changed (DHCP may assign new IP)

5. ✅ Test connectivity from another device:
   ```bash
   curl http://YOUR_SERVER_IP:11434/api/tags
   ```

6. ✅ Check firewall settings
   - Port `11434` must be open for Ollama
   - Port `8888` must be open for SearXNG (if using)

7. ✅ Try disabling mobile data on your phone
   - Some phones may try to use mobile data instead of Wi-Fi

## Model Issues

### "No Models Available"

- Run `ollama list` on your server to verify models are installed
- Pull a model if none exist:
  ```bash
  ollama pull llama3.2
  # Or try a smaller, faster model
  ollama pull qwen2.5:1.5b
  ```

### "Model Not Found"

- The selected model may have been deleted from the server
- Refresh the model list in settings
- Select a different available model

## Performance Issues

### Slow Response Times

- ⚡ **Use a GPU**: GPU acceleration dramatically improves speed
- 📦 **Use Smaller Models**: 7B parameter models are faster than 70B models
- 💻 **Server Hardware**: More RAM and CPU cores = faster inference
- 🔌 **Network Latency**: Ensure good Wi-Fi signal strength

### App Crashes or Freezes

- Clear app cache: Settings → Apps → localgrok → Storage → Clear Cache
- Restart the app
- Check for Android system updates

## Web Search Issues

### Web Search Not Working

1. Verify SearXNG is running:
   ```bash
   curl http://YOUR_SERVER_IP:8888/
   ```

2. Check SearXNG port in app settings matches your server configuration

3. Ensure brain toggle (💡) is enabled in the chat interface

4. Verify SearXNG is accessible from your network:
   ```bash
   # Test from another device
   curl http://YOUR_SERVER_IP:8888/search?q=test
   ```