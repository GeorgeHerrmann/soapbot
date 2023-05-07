2.310
    - Updated SOAP Bot's Threading Model, is now much more efficient and organized
        - All Guilds now have a ThreadPoolManager, which can be accessed via the ThreadPoolFactory
        - To execute Multi-Threaded actions, each Guild gets:
            - Three threads for commands
            - An adjustable amount of threads for database operations
            - Ten threads to keep track of events
            - One thread for audio playback/the audio system
    - Fixed a bug where all commands were recieving auto-formatted arguments, causing some commands to incorrectly read information
        - ParseableCommands can now control autoformatting via their CommandParser
    - Fixed a bug where database operations were desynced from the rest of SOAP Bot, causing rare race conditions
    - Fixed a bug where SOAP Bot could crash if too many unkept threads were active

2.311
    - Fixed a bug where SOAPEvents would unconditionally be restarted by the event manager on reconnection GuildCreateEvents, even if they were already active, causing duplicate versions to exist

2.312
    - Added "!helloworld" command

2.321
    - Reworked SOAPBot's client-level managers into abstract classes for easier additions