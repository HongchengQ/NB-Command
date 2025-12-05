# NB Command Remote GM Tool

## Introduction

NB Command is a graphical remote command execution tool designed specifically for [Nebula](https://github.com/Melledy/Nebula).

## Features

-  Multi-language support: Supports Chinese, English, Japanese, and Korean
-  Game management: Provides player management, item management, character management, and more
-  Graphical interface: Intuitive operation interface, simplifying the execution process of complex commands
-  Configuration saving: Automatically saves server address and authentication token
-  Command history: Records executed command history, facilitating traceability and reuse
-  Data handbook: Built-in character and item data handbook, supporting quick search and selection

## Installation and Running

1. Download the latest released executable package for your platform
2. Extract and run the executable file

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
Output directory is at `/target/nbcommand`

## Usage Instructions

1. Enter your server address and authentication token at the top
2. Select a command category from the left panel
3. Choose a specific command from the middle list
4. Fill in command parameters as needed
5. Preview the generated command and execute it after confirming it's correct

### How to Obtain Token
> Nebula > Ensure `config.json` > `remoteCommand` > `useRemoteServices` is `true`

#### Administrator Permission
> Nebula > `config.json` > `remoteCommand` > `serverAdminKey` as Token

#### User Permission
> Use the in-game command `!remote` and your Token will pop up on screen

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.