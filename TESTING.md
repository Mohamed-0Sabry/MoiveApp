# Testing Movie Night App on the Same Machine

This guide explains how to test the Movie Night App's screen sharing functionality on a single machine using the new test mode.

## Prerequisites

- Java 21 or higher
- JavaFX SDK 21.0.7 or higher

## Testing Steps

### Method 1: Using the Test Mode (Recommended)

1. Open a command prompt
2. Run:
   ```
   run-test.bat
   ```
3. In the test screen, you have three options:
   - **Test Host Mode**: Opens only the host screen
   - **Test Viewer Mode**: Opens only the viewer screen
   - **Test Both Modes Simultaneously**: Opens both host and viewer screens

4. If you choose "Test Both Modes Simultaneously":
   - The host screen will open first
   - The viewer screen will open second
   - You can interact with both screens simultaneously

5. In the host screen:
   - Click "Start as Host"
   - Load and play a video

6. In the viewer screen:
   - Click "Start as Viewer"
   - The viewer should connect to the host and display the video

### Method 2: Using Separate Instances (Legacy)

If you prefer to run separate instances, you can still use the old method:

1. Open two separate command prompt windows
2. In the first window, run:
   ```
   run-host.bat
   ```
3. In the second window, run:
   ```
   run-viewer.bat
   ```
4. In the host application, click "Start as Host"
5. In the viewer application, click "Start as Viewer"

## How It Works

The test mode uses different network ports for the host and viewer instances:

- Host: Uses default ports (8888 for discovery, 8889 for commands)
- Viewer: Uses alternative ports (8890 for discovery, 8891 for commands)

This allows both instances to run on the same machine without port conflicts.

## Troubleshooting

If you encounter connection issues:

1. Make sure both applications are running
2. Check that your firewall is not blocking the connections
3. Try restarting both applications
4. Ensure you're using the correct command-line arguments
5. Make sure your classes are compiled in the target/classes directory 