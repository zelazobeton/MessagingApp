# MessagingApp

Application is a simple java text communicator based on console interface with client and server packages.

It operates on local sqlite db with registered users and their logging passwords.
Communication between client and server is implemented using Sockets and ServerSocket from java.net library.
On the server side each client has its corresponding thread implemented in the SocketProcess class.
Threads communicate with each other using message queue in main thread. 
Messages are implemented as string literals with fields separated with underscores.
Fields of each message are described in MessageFieldDocumentation.txt

Purpose of the project was to train using plain java: multithreading, sql db handling, oop.
