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

2.320
    - Reworked SOAPBot's client-level managers into abstract classes for easier additions

2.321
    - Added addall subcommand to PermissionsManager

2.330
    - Added dates of ReserveEvents
        - Reserve events can now be scheduled for a date associated with their time.
            - Accepts formats such as: "tomorrow", "in x days", "MMMDD", etc
            - Dates are not mandatory and can be left out on event creation
        - All event-based commands can now view an event's date

2.331
    - Fixed reserve event scheduling for events with no dates and a time that was set earlier in the day

2.332
    - Updated command application lifecycle
        - Command's no longer decide their registration status, the CommandRegistry does
            - Definition updates are done automatically on startup for Global Commands
        - Added a static global discord api call ThreadPool which will manage all non-guild specific tasks for all of SOAPBot

2.333
    - Fixed parsing of ReserveCommand when the number of people was put after the time

2.334
    - Fixed an issue with the HelpCommand not formatting command help messages properly

2.335
    - Fixed an issue with the PermissionsCommand "addall" feature not properly updating group's permissions

2.340
    - EventCommand:
        - Changed to the ReserveEventCommand, all relevant texts modified
        - Added more aliases such as "re", "resevent", "revent", etc.
        - Added a Reserve Event wizard, allowing users to edit existing Reserve Events
            - Can be accessed via !event manage

2.341
    - Updated all logging and help-based formats.
        - Now consistent with new discord bulleted formatting
        - The multi-logger will convert to appropriate logging standards for each format