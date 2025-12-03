# NB Command Remote GM Tool

## Introduction

NB Command is a graphical remote GM command tool designed specifically for [Nebula](https://github.com/Melledy/Nebula). This tool provides an intuitive and easy-to-use interface to execute various game server management commands without manually typing complex command-line instructions.

## Features

-  Multi-language support: Chinese, English, Japanese, and Korean
-  Game management: Provides player management, item management, character management, and more
-  Graphical interface: Intuitive operation interface that simplifies the execution of complex commands
-  Configuration saving: Automatically saves server addresses and authentication tokens
-  Command history: Records executed command history for traceability and reuse
-  Data handbook: Built-in character and item handbooks for quick lookup and selection

## Installation and Running

1. Download the latest released executable package
2. Extract and run the executable file for your platform

Or build from source:
-  Java 21
-  maven

```bash
# Run the application
mvn javafx:run
```

```bash
# Build executable
mvn package
```

## Usage Instructions

1. Enter your server address and authentication token at the top
2. Select a command category from the left panel
3. Choose a specific command from the middle list
4. Fill in command parameters as needed
5. Preview the generated command and execute it after confirming it's correct


## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.