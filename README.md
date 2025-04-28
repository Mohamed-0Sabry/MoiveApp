
## Main Features
- [ ] **Host Functionality**
	- [ ] Upload movie/video file
	- [ ] Share screen (maybe via screen capture frames)
	- [ ] Open camera and broadcast
	- [ ] Start movie playback (control "play", "pause", "stop" for all viewers) (*Sabry, Still Working, 20%*)
	- [ ] Allow downloads for viewers
- [ ] **Viewer Functionality**
	- [ ] Connect to host on same WLAN
	- [ ] View live movie/camera or screen
	- [ ] Download uploaded files
	- [ ] Chat with host or other viewers
- [ ] **User Roles**
	- [ ] **Host:** Controls everything
	- [ ] **Viewer:** Only watches, downloads, and chats

### Design Patterns
- **MVC** ➔ Separate GUI (View), Logic (Controller), and Data (Model).
- **SOLID** ➔ Code will be highly maintainable and expandable.
Example (MVC structure): These are samples you could make your own but follow the same Structure

``` 
com.movieapp
├── model
│    ├── User.java
│    ├── FileTransfer.java
│    ├── StreamSession.java
├── view
│    ├── HostScreen.fxml
│    ├── ViewerScreen.fxml
│    └── ChatWindow.fxml
├── controller
│    ├── HostController.java
│    ├── ViewerController.java
│    └── ChatController.java
├── network
│    ├── Server.java
│    └── Client.java
├── utils
│    ├── FileUtils.java
│    └── ScreenCaptureUtils.java
└── Main.java
```

| Tasks                                             | Goal                         |
| ------------------------------------------------- | ---------------------------- |
| Project setup, libraries, basic networking        | Networking foundations ready |
| Build Host GUI (upload, start stream)             | Host-side UI working         |
| Build Viewer GUI (connect, view stream, download) | Viewer-side UI working       |
| Implement Chat and Camera (**Camera Optional**)   | Communication added          |
| Polish UX/UI, Documentation                       | اخر حاجه ان شاء الله         |

# 📝 To-Do Task List
### 🔵Setup
-  Setup JavaFX with FXML
-  Setup basic TCP/UDP Socket communication
-  Write a basic Server (Host) and Client (Viewer)
### 🔵Host Features
-  Build Host Screen (FXML + Controller)
-  Add file upload functionality
-  Implement Screen Sharing (start with static screenshots)
-  Create "Start Movie" Button ➔ Sends signal to clients
-  Structure Model classes: `User`, `MovieSession`
### 🔵Viewer Features
-  Build Viewer Screen (FXML + Controller)
-  Connect to Host (Manually and Automatically)
-  Display shared screen / movie
-  Download file functionality
-  Implement `StreamSession` management
### 🔵Extra Features
-  Build Chat window (separate small window)
-  Create simple text chat protocol
-  Add Webcam broadcasting option (use `webcam-capture` API)
-  Enhance Stream (smooth screen sharing, optimize images/video chunks)


### Networking Core Tasks:
-  Create `Server` class (Host side)
    - Start TCP server socket listening on a port (e.g., 5555 or 8888).
    - Accept incoming client connections.
    - Store connected sockets in a `List<Socket>`.
-  Create `Client` class (Viewer side)
    - Connect to the Host’s IP and Port.
    - Handle connection errors (ex: Host not reachable).
-  Create `MessageProtocol` (later will be used for commands like start movie, send file, etc.)
-  Use **Threads** (`ExecutorService`) to handle multiple clients simultaneously.
### File Upload Tasks:
- Create `FileUploaderService` class (handles reading and sending file chunks)
-  Create File Upload button in Host Screen (FXML + Controller method)
-  When Host selects a file:
    - Send file metadata (filename, size) to all connected clients.
    - Clients prepare to receive file on user request.
    - Host selects a file using `FileChooser`.
-  Implement download button for Viewer.
-  Add progress bar during file transfer.
### Screen Sharing Tasks:
- Create `ScreenCaptureUtils` class (captures screen image every X milliseconds or find a way to make it smooth)
-  Compress images (low-quality JPEG or PNG) for faster transmission.
-  Send images over TCP/UDP to all connected viewers.
-  Display images in Viewer’s Screen (`ImageView`).
### Camera Sharing Tasks:
- Integrate `webcam-capture` library (or OpenCV لو انت حابب) **try to make it simple as possible**.
-  Similar to screen sharing:
    - Capture frame
    - Compress and send
    - Display on Viewer side
    - Capture frames at 15-30 FPS.
### Movie Playback Synchronization Tasks:
- Add control buttons: Play, Pause, Stop, Seek.
-  Create commands for playback control (e.g., `"PLAY"`, `"PAUSE"`, `"STOP"`).
-  Send these commands over the socket.
-  Viewers listen for commands and control local MediaPlayer.
### Chat System Tasks:
-  Build Chat Window (JavaFX ListView for messages, TextField for input)
-  Define chat message protocol (prefix with `"CHAT:"`)
-  Implement method to send/receive chat messages.
-  Display messages in Chat Window.
### Downloading Content Tasks:
- Create Download button when host shares file.
-  Implement method to request file.
-  Show download progress bar.
-  Save file locally after full download on App folder.
---
# BONUS: Helpful Utilities
-  `NetworkUtils`: To handle IP address fetching, socket utilities.
-  `CompressionUtils`: To compress screen images before sending.
-  `FileUtils`: To read/write files easily.
-  `MessageParser`: To parse incoming messages (chat, file request, video controls).
# 🔥 FINAL HIGH LEVEL WORKFLOW
| Host                           | Viewer                       |
| ------------------------------ | ---------------------------- |
| Create Server Socket           | Connect to Server            |
| Broadcast file/screen/video    | Receive and show             |
| Send Chat Messages             | Send Chat Messages           |
| Handle download requests       | Request downloads            |
| Send playback control commands | Sync playback                |
# 📌 Important while coding:
**Follow SOLID:**
- Every service (chat, file transfer, screen share) in its own class.
- Use interfaces like `TransferService`, `StreamingService`.
**Follow MVC:**
- View (FXMLs): only visual stuff.
- Controller (JavaFX Controllers): user interaction.
- Model: networking, file, and streaming services.
**Threading:**
- Network and background tasks should NEVER block JavaFX Main Thread!
**Exceptions:**
- Catch and show user-friendly error messages for network issues.



# 📦 Package: **com.movieapp.model**

### `User.java`
- Fields: `username`, `isHost`, `Socket socket`
- Represents a user (Host or Viewer)
- Methods:
    - `boolean isHost()`
    - `Socket getSocket()`
    - `String getUsername()`
---
### `FileTransfer.java`
- Handles file upload/download logic metadata.
- Fields: `String fileName`, `long fileSize`, `byte[] fileData`
- Methods:
    - `byte[] readFile(String path)`
    - `void saveFile(byte[] data, String destinationPath)`
---
### `StreamSession.java`
- Represents a movie streaming session or screen sharing session.
- Fields:
    - `boolean isScreenSharing`
    - `boolean isCameraSharing`
    - `boolean isStreaming`
- Methods:
    - `void startScreenShare()`
    - `void startCameraShare()`
    - `void stopStreaming()`

---
# 📦 Package: **com.movieapp.view**
(_Just FXML files! no logic here._)
### `HostScreen.fxml`

- Layout for host:
    - Start server
    - Upload file
    - Share screen / camera
    - Play, Pause, Stop buttons
    - Access Chat
---
### `ViewerScreen.fxml`
- Layout for viewer:
    - Connect to host
    - See stream
    - Download file
    - Chat window access
---
### `ChatWindow.fxml`
- Layout:
    - Messages list
    - Text input field
    - Send button
---
# 📦 Package: **com.movieapp.controller**

### `HostController.java`
- Controls `HostScreen.fxml`.
- Methods:
    - `void onStartServerClicked()`
    - `void onUploadFileClicked()`
    - `void onStartScreenShareClicked()`
    - `void onStartCameraShareClicked()`
    - `void onPlayClicked()`
    - `void onPauseClicked()`
    - `void onStopClicked()`
    - `void onOpenChatClicked()`
- **Dependency**: `Server`, `FileUtils`, `ScreenCaptureUtils`
---
### `ViewerController.java`
- Controls `ViewerScreen.fxml`.
- Methods:
    - `void onConnectClicked()`
    - `void onDownloadFileClicked()`
    - `void onStartViewingStream()`
    - `void onOpenChatClicked()`
- **Dependency**: `Client`
---
### `ChatController.java`
- Controls `ChatWindow.fxml`.
- Methods:
    - `void onSendMessageClicked()`
    - `void onMessageReceived(String message)`
---
# 📦 Package: **com.movieapp.network**

### `Server.java`
- Handles:
    - Accepting Viewer connections
    - Sending files, chat messages, stream data
    - Sending playback control commands
- Fields:
    - `ServerSocket serverSocket`
    - `List<User> connectedClients`
- Methods:
    - `void start(int port)`
    - `void broadcastMessage(String message)`
    - `void sendFileToAll(File file)`
    - `void sendStreamFrame(byte[] frameData)`
---
### `Client.java`
- Handles:
    - Connecting to Host
    - Receiving files
    - Receiving stream frames
    - Receiving chat messages
- Fields:
    - `Socket socket`
    - `InputStream inputStream`
    - `OutputStream outputStream`
- Methods:
    - `void connectToHost(String ip, int port)`
    - `void listenForMessages()`
    - `void sendChatMessage(String message)`
    - `void requestFileDownload(String filename)`
---
# 📦 Package: **com.movieapp.utils**
### `FileUtils.java`
- Utility methods:
    - `static byte[] readFileToBytes(String path)`
    - `static void writeBytesToFile(byte[] data, String path)`
    - `static String getFileNameFromPath(String path)`
---
### `ScreenCaptureUtils.java`
- Utility methods:
    - `static byte[] captureScreen()`
        - Captures entire screen and returns compressed byte[]
    - `static byte[] captureCameraFrame()`
        - Captures webcam frame and returns compressed byte[]
---
# 📦 **Main Class**
### `Main.java`
- Starts the JavaFX Application
- Loads either HostScreen or ViewerScreen depending on user choice
- Methods:
    - `start(Stage primaryStage)`
    - `void showHostScreen()`
    - `void showViewerScreen()`