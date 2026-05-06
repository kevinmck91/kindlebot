
Kindlebot Release

1. Download and extract the ZIP file.

2. Set up Firebase:
   - Create a Firebase project at https://console.firebase.google.com/
   - Enable Firestore Database and Storage
   - Create a service account key (JSON) from Project Settings > Service Accounts
   - Copy the service account key JSON to config/serviceAccountKey.json
   - Copy config/application.properties_template to config/application.properties
   - Edit config/application.properties and replace YOUR_PROJECT_ID with your Firebase project ID

3. Run the app:
   - Double-click run.bat (Windows) or run the equivalent command on other OS

The app will start a web server. Access it via your browser.