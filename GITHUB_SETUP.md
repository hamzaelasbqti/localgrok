# GitHub Setup Guide

Follow these steps to push your project to GitHub:

## Step 1: Configure Git (if not already done)

```bash
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"
```

Replace with your actual name and GitHub email.

## Step 2: Add and Commit Your Files

```bash
# Add all files
git add .

# Create your first commit
git commit -m "Initial commit: localgrok Android app"
```

## Step 3: Create Repository on GitHub

1. Go to https://github.com and log in
2. Click the **"+" icon** in the top right corner
3. Select **"New repository"**
4. Fill in the details:
   - **Repository name**: `localgrok` (or whatever you want)
   - **Description**: "Privacy-focused Android client for local LLM servers running Ollama"
   - **Visibility**: Choose Public or Private
   - **DO NOT** check "Initialize with README" (you already have one)
   - **DO NOT** add .gitignore or license (you already have them)
5. Click **"Create repository"**

## Step 4: Connect Local Repository to GitHub

After creating the repo, GitHub will show you commands. Use these:

```bash
# Add the remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/localgrok.git

# Rename branch to main (GitHub's default)
git branch -M main

# Push your code
git push -u origin main
```

If GitHub uses a different URL format (SSH), use:
```bash
git remote add origin git@github.com:YOUR_USERNAME/localgrok.git
```

## Step 5: Add Screenshots

1. Create the screenshots directory:
   ```bash
   mkdir -p docs/screenshots
   ```

2. Add your 4 screenshots:
   - `docs/screenshots/1.png` - Main chat with reasoning mode
   - `docs/screenshots/2.png` - Chat list
   - `docs/screenshots/3.png` - Settings
   - `docs/screenshots/4.png` - Web search with reasoning

3. Commit them:
   ```bash
   git add docs/screenshots/
   git commit -m "Add screenshots"
   git push
   ```

## Troubleshooting

### If you get authentication errors:
- GitHub requires authentication. Use a **Personal Access Token** (not your password):
  1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
  2. Generate new token with `repo` permissions
  3. Use the token as your password when pushing

### If branch name conflicts:
- If you see "master" vs "main" issues, rename your branch:
  ```bash
  git branch -M main
  ```

### To update later:
```bash
git add .
git commit -m "Your commit message"
git push
```
