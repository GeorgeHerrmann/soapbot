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

2.381-BETA
    - Fixed an issue where SOAP Bot could vote on quick polls
    - Fixed an issue where a wizard's primary user would not update on unlocked Reaction Listeners

2.382-BETA
    - Added forgotten dates to !events 'NAME' response
    - Replaced tags with mentions on !events 'name' response

2.383-BETA
    - Added shortcut !poll subcommands
        - !poll create to go to the create a poll screen
        - !poll view to view all polls
        - !poll vote to vote for a poll
    - Added !poll quickpolls in addition to !poll present to bring up quick poll voting

2.390
    - Removed the ChatCompletionManager
    - All MemberChatCompletions are now a part of the user's UserProfile.
        - All previous chat completions have been wiped as a result.
    - Created the foundation for Manageable's to be added to a UserProfile
    - All UserProfiles will be kept up to date with MemberJoinEvents on new user join and GuildCreateEvents on guild joins

2.340-BETA
    - Added a basic economy system foundation, housed in UserProfiles.
    - Updated PlinkoGames to give a coin-based reward
        - Basic rewards (outer two) give 5 coins
        - Advanced rewards (middle two) give 20 coins
        - Legendary reward (inner one) gives 50 coins
    - A basic coin balance can be viewed with !bank or !coins
        - This system is in beta, more will be added to it in the future

2.341-BETA
    - Created a basic framework for SOAP Bot Discord Games
    - Placed channel restrictions on SOAP Bot Discord Games.
        - Only one game can be played per text channel at a time

2.342-BETA
    - Increased maximum thread counts for commands and events
        - Both set to 30
    - Fixed an issue where internal PermissionGroups would not be updated on role creation or update from Discord

2.343-BETA
    - Created the EchoCommand
        - Allows users to have SOAP Bot say a message.
            - Requires ECHOCOMMAND permission

2.400
    - Added a basic reward structure to DiscordGames with entry and reward amounts
    - Created a basic CardGame framework
        - PlayingCards are objects for CardGames with a Suit and Value
        - A CardDeck is a deck (internally a stack) of PlayingCards for a player
        - CardGame is abstract and handles all PlayingCards and CardDecks in a DiscordGame
        - A CardDeck is abstract and can either be a GlobalCardDeck or PlayerCardDeck
            - GlobalCardDecks are usually drawing or discard decks
    - Created a BlackjackGame which users can wage coins
        - Uses a BlackjackWizard
    - Added a leaderboard via !bank leaderboard to see rankings of coins in a Guild
    - Modified how the InputWizard and InputListener handle responses and users
        - The InputListener and InputWizard now use Users instead of Members, allowing for private channel wizards.
        - The InputListener now can be customized to allow only the owner of the wizard to respond, or anybody
        - The InputListener and InputWizard can now log notes about a prompt or response in their WizardState.
        - The InputWizard can now return a WizardResponse, a more detailed object about a user's response from an InputListener
        - A WizardResponse is DateTimed for the moment of the response, records which User responded, their message and any notes
        - InputWizard implementations can request a WizardResponse instead of a basic String response with withFullResponse()
        - ReactionListeners will now provide a note saying whether a reaction response was "added" or "removed"
            - One example of this is the QuickPollWizard, which can use these features to more quickly assign votes based on QuickPoll reactions
              removing various latency issues causing missed votes, and can also now remove votes when you un-react to a QuickPoll.
    - Created a more generic date/time framework via DateTimed
        - TimedEvents are now DateTimed

2.401
    - Fixed an issue preventing users other than the user who started the Wizard from voting on Global Wizards (Wizards that allowAllUsers)
    - Reworked package structure for Permissions and Wizards/Listeners out of util and into their own package