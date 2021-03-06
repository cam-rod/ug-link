# ug-link

This simple program that locates an unloaded undergraduate engineering (UG EECG) server at the University of Toronto, and then launches an SSH session. This project is inspired by [Junhao Liao's iCtrl](https://github.com/junhaoliao/iCtrl) program.

Currently in alpha development. This project is made available under the [MIT License](LICENSE).

## Requirements

- Java 16
- Windows 10/11 (the project may work if recompiled for other platforms)
- [PuTTY](https://www.putty.org/)

## Testing

Build the project with the default `gradle build` command. `gradle jar` will compile a Java archive for the project. The library uses `Console.readPassword()`, so the program must be run from the terminal. Use the batch or Bash files in the test folder to launch the compiled project for debugging, which you can then attach to from an IDE like IntelliJ, or from the terminal with `jdb`.

