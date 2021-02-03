# Some notes..

## JADE Architecture
[From this document](https://jade.tilab.com/doc/tutorials/JADEProgramming-Tutorial-for-beginners.pdf), our application need:
- **Main container**, contains agents created by the user and two special agents, **AMS** (Agent Management System) and **DF** (Directory Facilitator).
- **setup()**, this method is intended to include agent initializations.
- **takeDown()**, opposite of setup, is invoked just before an agent terminates and is intended to include agent clean-up operations.
- **doDelete()** to terminate the agent.

...