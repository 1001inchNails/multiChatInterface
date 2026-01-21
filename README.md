# Multi-Chat Server Application

## Project Overview
JavaFX-based multi-client chat server application that simulates running a chat server and multiple client connections from a single interface

## Project Structure
    ├── ChatApplication.java # Entry point
    ├── server/
    │ ├── ServerTabController.java # Server UI controller
    │ └── ChatServer.java # Server logic
    └── client/
    ├── ClientTabController.java # Client UI controller
    ├── ChatClient.java # Client connection logic
    └── ClientHandler.java # Server side client management


## Architecture

### 1. **ChatApplication.java** - Main Application Controller
**Role**: Entry point and main window manager

**Responsibilities**:
- Initializes JavaFX application window
- Creates tabbed interface with menu bar
- Manages server and client tab creation
- Handles client tab lifecycle (creation/closing)

**Key Features**:
- TabPane for organizing server and client interfaces
- Menu bar with "New Client" and "Exit" options
- Automatic client numbering (Client 1, Client 2, etc.)
- Proper cleanup on tab closure

### 2. **Server Components**

#### **ServerTabController.java** - Server UI
**Role**: Server management interface controller

**Responsibilities**:
- Server start/stop controls
- Port and client limit configuration
- Real-time server log display
- Connected client count tracking

**UI Components**:
- Start/Stop server buttons
- Port and max clients configuration fields
- Server status indicator (Running/Stopped)
- Log text area with timestamped messages
- Connected client counter

**Key Methods**:
- `startServer()`: Validates input, creates ChatServer instance
- `stopServer()`: Shuts down server and cleans up
- `logMessage()`: Thread-safe log updates via Platform.runLater()
- `updateClientCount()`: Real-time client count updates

#### **ChatServer.java** - Server Backend
**Role**: Multi-threaded server implementation

**Responsibilities**:
- Creates ServerSocket on specified port
- Manages thread pool for client connections
- Maintains list of connected clients
- Delegates client handling to ClientHandler

**Key Components**:
- `ServerSocket`: Listens for incoming connections
- `ExecutorService`: Fixed thread pool for client handlers
- `CopyOnWriteArrayList<ClientHandler>`: Thread-safe client list
- `AtomicBoolean`: Thread-safe server running state

**Workflow**:
1. Initialize server socket and thread pool
2. Enter accept loop for incoming connections
3. For each client: create ClientHandler, add to list, execute in pool
4. Broadcast messages between clients
5. Clean shutdown on stop command

### 3. **Client Components**

#### **ClientTabController.java** - Client UI
**Role**: Individual client chat interface

**Responsibilities**:
- Connection management (connect/disconnect)
- Displaying messages
- Chat history display
- Connection status monitoring

**UI Components**:
- Connect/Disconnect buttons
- Port configuration
- Chat message display area
- Message input field with send button
- Auto-scroll option

**Key Features**:
- Unique client ID per tab
- Real-time message display with timestamps
- Automatic scroll to new messages
- Connection status indicators

#### **ChatClient.java** - Client Connection
**Role**: Manages client-server communication

**Responsibilities**:
- Establishes socket connection to server
- Sends messages to server
- Receives messages in background thread
- Maintains connection state

**Protocol Details**:
- First message to server: `CLIENT_ID: <tab_number>`
- Continuous message receiving in separate thread
- Proper resource cleanup on disconnect

#### **ClientHandler.java** - Server-side Client Management
**Role**: Handles individual client connections on server

**Responsibilities**:
- Reads client messages
- Broadcasts messages to other clients
- Manages client disconnection
- Maintains client identity

**Key Features**:
- Client ID synchronization (matches tab number)
- Message broadcasting to all except sender
- Connection lifecycle management
- Thread-safe client list operations

## Communication Flow

### Connection Establishment:

    Client Tab → ChatClient.connect()
    ↓
    Socket Connection → Server.accept()
    ↓
    ClientHandler created → Client ID logic
    ↓
    Welcome message → Client added to broadcast list


### Message Sending:

    User types message → ClientTabController.sendMessage()
    ↓
    ChatClient.sendMessage() → Socket output stream
    ↓
    Server receives → ClientHandler.broadcast()
    ↓
    All other clients receive → Display in respective tabs


### Client Disconnection:

    User clicks Disconnect → ChatClient.disconnect()
    ↓
    Socket closed → Server detects EOF
    ↓
    ClientHandler.disconnect() → Remove from list
    ↓
    Broadcast "left chat" message → Update client count


## Threading Model
### Server-side Threading:
- **Main Server Thread**: Accepts new connections
- **Client Handler Threads**: One per connected client (managed by ExecutorService)
- **UI Update Thread**: Platform.runLater() for JavaFX updates

### Client-side Threading:
- **Message Receiving Thread**: Separate thread for listening to server messages
- **UI Thread**: JavaFX application thread for all UI updates

## Configuration
### Server Configuration:
- **Port**: Default 8080 (configurable)
- **Max Clients**: Default 10 (configurable)

### Client Configuration:
- **Port**: Must match server port
- **Client ID**: Automatically assigned based on tab number

## Usage Instructions
1. **Start the Application**: Run `ChatApplication.java`
2. **Start Server**:
    - Navigate to Server tab
    - Configure port and max clients
    - Click "Start Server"
3. **Create Clients**:
    - Use "File → New Client" menu
    - Multiple clients can be created
4. **Connect Clients**:
    - In each client tab, click "Connect"
    - Default port 8080 should match server
5. **Chat**:
    - Type messages in input field
    - Messages broadcast to all connected clients
    - Server logs all activity

## Implementation Details:
1. **Client ID Synchronization**:
    - Each client tab has a unique ID (tab number)
    - First message to server includes `CLIENT_ID: <id>`
    - Ensures consistent identification across server and clients

2. **Thread Safety**:
    - `CopyOnWriteArrayList` for client lists
    - `AtomicBoolean` for server running state
    - `AtomicInteger` for client counts
    - All UI updates via Platform.runLater()

3. **Resource Management**:
    - Proper try-catch-finally blocks for all I/O operations
    - ExecutorService shutdown on server stop
    - Socket cleanup on disconnection

4. **UI Responsiveness**:
    - Long-running network operations in separate threads
    - Non-blocking UI updates
    - Real-time status indicators

## Dependencies
- Java 8 or higher
- JavaFX SDK