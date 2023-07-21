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

2.342
    - Updated ReserveEvent's incorrectly scheduling a day in advance
    - Fixed various formatting issues with the switch to the new Discord formatting system

2.350
    - Standardized all events which use dates or times to the TimedEvent
        - Will handle all data management involving times and dates for all implementations
    - Added a poll system
        - Uses the new InputWizard system to handle all user interaction
        - Polls can be created with a prompt and an unlimited number of options
        - A duration for how long a poll can be active can be specified
            - Like Reserve Events, polls can be scheduled for any time in the future
    - Fixed an issue causing timed events to duplicate themselves when being restarted and the time until the event's expiration was between -60 and 0 seconds
    - Fixed an issue where certain reserve events could not be viewed using the ReserveEventCommand
    
2.351
    - Fixed an issue where events with a time, but no date, could not be created.

2.352
    - Reworked the InputWizard Engine
        - Can now fully run implementators without any direction from the implementation
            - Only logic from the extending class on what to prompt and how to handle the response is required

2.353
    - Fixed an issue where TimedEvents with only a time would fail to be created on AM/PM inputs
      because the time was not converted before determining the correct scheduling date.

2.354
    - Added a ButtonMessage UserInputListener
        - Functions similarly to the MenuMessage listener, but with buttons.

2.355
    - Added the ability for InputWizards to override their default UserInputListener on a window to window basis
    - Cleaned up the withResponse handler to use a single method, with a boolean to determine the back option

2.356
    - Added edit polls functionality to the Poll Event Wizard
        - Users can add or remove options, or remove a poll all together.

2.360
    - Added GPT 3.5 integration to SOAP Bot via !ask
        - SOAP Bot will recall a user's previous ten messages between the GPT AI and themselves and use it as context
        - Each conversation is between one member and the AI, SOAP Bot will only send context of the user who is using the command at that time.
        - SOAP Bot will instruct the AI to respond as the "Discord Bot, SOAP Bot"
        - SOAP Bot will give mild context on how to use the bot to the AI
            - More coming soon for this
        - When gathering a response from the GPT AI, SOAP Bot will simply send the response if only one existed,
          or provide an interactive wizard (the IterableStringWizard) to switch between responses if multiple possibilities were sent.
    - Fixed an issue where the Help Command would not list all commands when using !help
    - Fixed an issue where some commands would fail to respond when using slash commands and they took longer than three seconds to compute a response
        - Created a basic framework for reply deferrals for longer executing commands (rare)
    - Fixed an issue where some PermissionGroups had null permissions, causing issues with Permission Management.
    - Fixed an issue where some Reserve Events would not properly mention users when using !events [name] mention
        - A temporary solution has been implemented, with a more full one to the entire event system coming later

2.361
    - Restructred the way top-level control objects structure their data
        - SoapClients now use a more scalable ClientContext to hold most of their data
        - Structured AbstractSoapManagers to extend SoapManagers
        - There is now a UserProfileManager to manage UserProfiles properly with the new system
            - SoapBot's management system is now fully unified
            - A necessary change to the log messages on startup has been made as a result

2.362
    - Fixed an issue where GPTCommand requests would time out and fall back to displaying the help message
        - Will now give a proper error message when OpenAI fails to respond in time

2.370
    - Reworked the InputListener to be much more modular and now share varying properties.
        - InputListeners can be created by extending the InputListener class
        - Can be customized via the InputListenerBuilder and obtained via the InputListenerFactory
    - Reworked Help Messages into iterable Wizards which now display the message one window at a time.

2.371
    - Added a MessageListener

2.372
    - Fixed an issue where multiple InputWizards being active at once could stop other commands from executing
        - Each CommandThreadPool can now run ten command tasks at once.

2.373
    - Fixed an issue where InputListeners with the XReaction mode on would not be cancellable
      with the reaction past the initial window.

2.374
    - Created a ReactionListener (needs work)
    - Created a SwappingWizard which can swap between a Message and a Wizard with a reaction.
        - Migrated the HelpCommand to use a SwappingWizard.

2.380-BETA
    - Added quick polls and greatly enhanced the wizard and listener system
        - Added a reaction listener
            - Allows users to present reactions as options and allows users to select a reaction as a response
                - Can be customized to allow anyone to click options
        - Added various customization to input wizards and input listeners
        - Added quick polls, which are PollEvents which last for one hour and have only "yes" and "no" as options
            - Quick polls can be created via !poll '[prompt]'
                - Doing so brings up a reaction wizard allowing any user to vote for the duration of the wizard's lifecycle
                - If the wizard times out, or the message is lost, quick polls can be presented with !poll present
            - The current PollEventWizard is now accessable with !poll wizard
            - Simply doing !poll now brings up the help menu for polls.
