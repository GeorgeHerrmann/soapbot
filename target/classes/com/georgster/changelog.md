SOAPBot Version 2.0 Changes:
    - Fully reworked backend structure
        - SOAP Bot in totality is controlled by the SoapClientManager
        - A SoapClient controls the objects and actions SOAPBot will take in a specific Guild
            - Each Client has its own CommandRegistry, meaning Guild-specific commands are now possible
            - Each Client now has its own EventManager to easily keep track of all SoapEvents
            - Each client has its own AudioInterface to control audio playback in Voice Channels
        - The SoapClient will build and distribute GuildManagers to different parts of SOAPBot
            - A GuildManager is a smaller version of the SOAPClient, containing utility to manage a Guild
              without needing every object the SoapClient has
        - Commands are now saved in the CommandRegistry for each Guild, allowing Guild-Specific objects
          to be constructed in them
        - Commands and their subsystems are no longer permitted to interact with the ProfileHandler, and instead
          the ProfileHandler is constructed for each Guild and is interacted with by the SoapClient's managers and handlers
        - Instead of parsing each command in the command's execute method, the general CommandParser will now
          handle the parsing of all command arguments and automatically determine each aspect of what the user was trying to do
        - SOAP Bot now logs each system with the MultiLogger to a log file, discord channel and logger's STDIO
        - Created the foundation for a basic Permissions System
        - Generalized SOAPBot's events, the ReserveEvent now implements the SoapEvent interface.
        - SoapEvents now fulfill themselves instead of being fulfilled by the SoapEventHandler.
    - Bug fixes:
        - General
            - Fixed an issue where, upon parsing a user's command, the CommandParser would hang and crash the thread it was parsing on
            - Fixed an issue where SOAP Bot would send redundant requests to the SOAP API
        - Music
            - Fixed an issue where audio could only be played in one Discord server at a time
            - Fixed an issue where all guilds shared the same TrackScheduler
            - Fixed an issue where the AudioInterface would timeout and fail to play audio on a valid request
            - Updated messages to be more clear for all music commands.
        - Reserve and Events
            - Fixed an issue where events would lose track of themselves, causing the system to delete the event
            - Fixed an issue where upon reserving to or creating an event, SOAP Bot would run out of memory and crash
            - Fixed an issue where events would sometimes de-schedule themselves upon SOAPBot's startup sequence
            - Fixed an issue where event names could be, at maximum, one word
            - Fixed an issue where, upon a user attempting to create a new event, the CommandParser would fail to
              register the time and playercount of the desired event if they were in the correct order
            - Fixed an issue where the command to unreserve from an event was in the wrong format
            - Fixed an issue where, upon the creation of a new ReserveEvent, the system would display the incorrect amount of spots 
              available
            - Fixed an issue where users could input a time for an event without specifying an exact time or AM/PM
            - Fixed an issue where, when showing information about an event, SOAP Bot would mention all reserved users before
              the event popped
            - Fixed an issue where events would overwrite the entire events file if an invalid event was allowed to be created
            - Updated messages to be more clear for all reserve and event commands.
    - Quality of Life:
        - Commands
            - Commands can now have aliases. For example, the event command can now be accessed by !events or !event
                - All command aliases can be viewed by accessing its associated help command.
        - General
            - Standardized how SOAP Bot handles all actions it takes in a Discord Server. The goal is to significantly
              improve the user interface for interacting with SOAP Bot.
            - Significantly improved the speed at which SOAP Bot performs most actions.
            - Significantly reduced the amount of memory required for SOAP Bot to do most actions

Changes to commands:
    - To unreserve from an event you now type
        - !events [event name] unreserve
    - Commands now have aliases
    - Event names can now have multiple words, but cannot have a number unless it is attached to a word (1 v 1 vs 1v1)
    - The order in which you input the name of the event, the time of the event and the number of people needed no longer matters