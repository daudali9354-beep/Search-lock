SEARCH LOCK - ANDROID PROJECT (Version 1)

IMPORTANT:
- This is source code, not an installable APK.
- Chrome monitoring requires the user to manually enable the Android Accessibility Service.
- Android/Chrome may not expose address-bar/search text consistently. This app can miss searches and cannot guarantee detection.
- It only checks accessible text from the Chrome package and compares it locally. It does not upload search text.
- Accessibility access is powerful. Enable it only for this app if you understand and accept the permission.
- Notification permission may also be requested on Android 13+.

BUILD:
1. Upload this complete project folder to a private GitHub repository.
2. GitHub Actions workflow is included at .github/workflows/android.yml.
3. Run the workflow and download the generated app-debug.apk artifact.
4. Install the APK on your own Android phone.
5. Open Search Lock, save phrase/message/PIN, activate, then enable Search Lock in Android Accessibility settings.
6. Test with a harmless phrase in Chrome.

This project is a first prototype. PIN is stored locally in app preferences in this prototype; do not reuse an important PIN. For a public release, use secure credential storage and improve permission/onboarding handling.
