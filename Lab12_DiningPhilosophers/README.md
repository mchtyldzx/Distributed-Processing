# Dining Philosophers Problem

## Problem Description
The dining philosophers problem is a classic synchronization problem in computer science. It illustrates challenges in resource allocation and deadlock prevention.

### Scenario
- 5 or more philosophers sit at a round table
- Each philosopher alternates between thinking and eating
- A fork (resource) is placed between each pair of philosophers
- To eat, a philosopher must acquire both the left and right forks
- A fork can only be used by one philosopher at a time

### Challenges
- Deadlock: If each philosopher holds one fork and waits for another, the system deadlocks
- Starvation: Some philosophers might never get to eat
- Resource contention: Proper synchronization is needed for fork access

## Solution Implementation

This implementation uses a client-server architecture:
- `ButlerServer.java`: Central arbiter that controls resource allocation
- `PhilosopherClient.java`: Individual philosophers that request resources

### Deadlock Prevention
The solution uses the "resource hierarchy" approach with a butler (arbiter) that:
- Ensures no more than 4 philosophers can compete for forks at the same time
- Manages fork allocation through synchronized access
- Keeps track of which philosophers are currently eating

## How to Compile and Run

### Compile the Code
```
javac ButlerServer.java
javac PhilosopherClient.java
```

### Run the Server
```
java ButlerServer
```

### Run Philosopher Clients (in separate terminals)
```
java PhilosopherClient 0
java PhilosopherClient 1
java PhilosopherClient 2
java PhilosopherClient 3
java PhilosopherClient 4
```
Note: You can run more philosophers if needed (5, 6, etc.)

## Architecture Details

### Butler Server
- Listens for connections from philosophers
- Manages which philosophers can eat concurrently
- Ensures no more than 4 philosophers compete for resources at once
- Uses synchronization to prevent deadlocks

### Philosopher Client
- Connects to the Butler Server
- Alternates between thinking and requesting to eat
- Waits for permission from the butler before eating
- Releases resources when finished eating

## Communication Protocol
1. Philosopher connects and sends ID to server
2. Philosopher sends "REQUEST_EAT" when hungry
3. Server responds with "WAIT" or "GRANTED"
4. Philosopher sends "DONE_EATING" when finished

This implementation solves the dining philosophers problem by preventing deadlocks while allowing maximum concurrency. 