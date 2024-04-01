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

2.500
    - Completely rewrote the CommandParser system.
        - The CommandParser features completely redesigned logic
        - A ParsedArguments object now represents the arguments parsed by a command parser
          and is included in the CommandExecutionEvent.
        - A SubcommandSystem can be used by commands to easily created subcommands
        - Many subcommand arguments can now be input in any order and still work
    - Switched all commands and systems to the new parsing system.
    - Fixed a bug where certain commands would not execute properly if no arguments were given
        - The BankCommand now displays balance on !bank or !bal
            - !bank balance subcommand removed
        - The PollCommand now brings up the full PollEventWizard on !poll
            - !poll wizard subcommand removed
        - The PlinkoCommand now simulates a PlinkoGame on !plinko
            - !plinko play subcommand removed
    - Fixed a bug where the ReserveCommand would not recognize date inputs unless they were one word
    - Fixed a bug where ReserveCommand arguments had to be placed in a specific order when creating a new event
        - Note: There should still be some argument between the event name and date to prevent overlapping.
            - Example: !r new event december 11 9pm 5 - May not work
                       !r new event 9pm december 11 5 - Will work, since '9pm' separates 'new event' and 'december 11'

2.510
    - Refactored GuildInteractionHandler
        - Now implements the InteractionHandler, which will share logic between all handlers which interact directly with discord's API
            - Not completely done, more refactoring required
        - MultiLogger now uses a GuildInteractionHandler instance for discord logging
    - ReserveEventCommand now displays the list of events on an empty input

2.511
    - Fixed an issue where the String "cs2" was being forcefully considered a date by the CommandParser

2.512
    - Slightly reworked InteractionHandler to have better logic control
    - Updated all user-based Manageables to use Discord ID's instead of the old tags

2.513
    - Created functionality for InputListener's to "copy" their critical properties between eachother
        - Allows for InputWizard's to have different InputListener's for different windows.
    - Updated the ReserveEventWizard, PollEventWizard and PermissionsWizard to have ButtonMessageListeners for
      certain windows, making them easier to navigate and understand.
    - Created a UserInteractionHandler, which will handler interactions between SOAP Bot and a User.

2.514
    - Added Image attachment capabilities to InteractionHandlers
        - Can only be attached to embed messaged with titles or titles and layout components
            - Image links can simply be sent in plain messages, non-title embed would never need images

2.515
    - Added format control to InteractionHandlers
        - Commands and various systems can now use MessageFormatting to determine how a sent Message looks
    - Updated various systems to the new standards
        - Commands that produce error message will use the ERROR formatting
        - Commands that generate information and logs produced by the MultiLogger will use the INFO formatting
        - All other formatting will be DEFAULT

2.516
    - Fixed an issue where ReserveEvents would not be fired correctly when their fulfillment condition was met
    - Fixed an issue where invisible users on all Platforms could not use SOAP Bot commands
    - Removed old unnecessary logs from InteractionHandlers

2.517
    - Fixed an issue where offline users could not execute commands
    - Improved QuickPollWizard poll present
        - The current wizard will immediately switch to the QuickPoll voting screen
    - Updated MessageCommand to new Command systems

2.518
    - Fixed an issue where some QuickPolls would create a new Message on every vote

2.519
    - Fixed an issue where the CommandParser would ignore duplicate instances of the same "word" on some input configurations
        - For example, attempting to create an event called "the end of the world" would previously only use "the end of" as the event name,
          as two instances of "the" were present. This, however, has now been fixed to properly handle duplicate words.

2.520
    - Commands can now be executed with Messages starting with "/"
        - Note that these commands will behave like using the "!" syntax, not like slash-commands via Discord ApplicationCommands

2.521
    - Fixed an issue where some wizards which overrode their default listener on the first window would create duplicate Messages when switching windows
    - Fixed an issue where the MessageCommand was not working

2.600-BETA
    Command Parser
        - Fixed an issue where the command parser would duplicate single-word arguments if that word was present in another argument
        - Fixed an issue where the command parser would cut off some inputs if certain words were present multiple times in a single argument
            - These issues were related to an issue with how the parser treated "variable" arguments by comparing words to substrings in other arguments
        - Fixed an issue where the command parser would assume some input strings were date inputs if there was a number in them, even if the "in" keyword was not present
    Permissions
        - Updated PermissionGroups to be identified by their Role id, rather than their name
        - Fixed an issue where updating a Discord Role would cause a duplicate PermissionGroup to be made, resetting that group's permissions as the system fell back to a new default group
        - Fixed an issue where moving users between Discord Roles could cause unintended behaviors with the Permissions System
    Blackjack
        - The dealer will now automatically win if it has not busted and it has a greater score than the player
        - The dealer will now draw with the user if both decks have a total of 16
    Trading Cards
        - Created a new Trading Card System
            - Cards can be created via !cards create
            - Individual versions of cards can be bought and sold from SOAP Bot
                - The price doubles on a sale, and halves on a purchase
            - Cards can be viewed with !cards or !cards view
            - Cards can be put on the marketplace for a specific price with !cards market
            - You can view your own cards with !cards mine
            - You can view another users cards with !cards @[USER]
            - Cards can be traded between users with !trade @[USER]
            - You can view all the cards ranked by their cost with !cards leaderboard
            - Cards can be inflated in value
                - Actual inflation cost is based on the number of cards in circulation
            - Cards have rarities, determined by the number of coins total in a Guild and the cost of the card
                - Can be COMMON, UNCOMMON, RARE, LEGENDARY or UNIQUE
    Trading
        - Coins and any Tradeable objects (including Cards) can be traded with !trade @[USER]
            - You can create a trade offer, then send it to a User, who has 5 minutes to accept or reject
    Interaction Handler
        - Added the ability for Interaction Handlers to send messages with outside EmbedCreateSpecs
    InputWizard
        - Greatly enhanced InputWizard utility
            - InputWizards can now send images or custom EmbedCreateSpecs in their Messages.
            - InputWizards can now be switched to UserWizards, which will live in a User's private MessageChannel
                - Note that these can only be interacted with via ComponentInteractions (Menus and Buttons)
        - Fixed an issue where an InputWizard which failed to run properly would run in an infinite loop
        - Added the new AlternateWizard, which can swap between two InputWizards with a reaction
            - This is used with !cards view, for example, which switches between a menu or button based UI
        - Various minor performance optimizations
    InputListener
        - Added the ability for ButtonMessageListeners to have Danger buttons by prepending a "!" to an option
    General
        - Fixed an issue where testMode was not behaving as intended

2.601-BETA
    - Added !cards ranges which will show the value ranges for each rarity, as well as total coin values for a Guild

2.602-BETA
    - Fixed an issue where purchasing multiple copies of cards worth zero cards, then selling those cards, would lead to exponentially high card costs
        - Cards can now have a minimum cost of 1 coin
        - Cards purchased at one coin will not affect the overall cost of a card once sold
    - Fixed an issue where selling a card after inflating its value would deposit the incorrect amount of coins to your account
    - Fixed an issue where inflating a cards value would inflate the value too high, allowing for exponential gains when selling cards
        - The algorithm determining the overall inflation amount has been improved
    - Added the ability for the creator of a card to change its image in the Card Manager
        - The ability to transfer ownership of a card will come soon
    - Added the ability to lock and unlock cards in the Card Manager
        - A locked card can not have copies be purchased
            - Only cards with a single copy can be locked

2.603-BETA
    - The CollectableViewWizard no longer displays displays the 'wizard ended' informational text after ending or timing out in most cases
        - This includes viewing cards via '!cards/!cards view' '!cards [name]' '!cards [ID]' '!cards @[user]' '!cards leaderboard' '!cards mine' or after creating a new card
    - The CollectableCreateWizard now displays the 'wizard ended' informational text after ending the wizard following a successful card creation
    - Added the ability for the creator of a card to delete the card in the Card Manager
        - There must be zero active copies of a card to delete it

2.604-BETA
    - Only a User who owns a copy of the desired card, or the card's creator may now inflate that card's value
    - Locked cards can no longer have their value inflated
    - The ManageCollectableWizard (CardManager) can now be ended with the 'X' reaction
    - Card Inflation has been moved from the CollectableViewWizard to the ManageCollectableWizard (CardManager)
        - Accessing the CardManager will now display the option to inflate a card's cost
            - This was always the desired functionality, but a backend issue with personal User Wizards had to get fixed first

2.605-BETA
    - When creating a Collectable, an image can be uploaded and sent in a Message to be used as the image URL
    - When viewing a specific event via the ReserveEventCommand (!re, !events, etc), a "manage" button will appear
        - The person who executed the command can click that button to directly bring up the manage screen for that event in the ReserveEventWizard
        - When the wizard is ended, the old Display comes back up
    - Updated InputWizards and InputListeners to be able to pass around the respondants Message object, if it exists

2.606
    - Trading Cards are proving to be stable and have been taken out of beta
    - Fixed multiple issues with the Application Command (slash commands) registration system
        - Fixed an issue where Global Application Commands were being registered multiple times, once for each Guild SOAP Bot is in
        - Fixed an issue where the registration system would attempt to register a new application for commands which were already registered
        - Fixed an isssue where new Commands may not be automatically be registered
    
2.700
    - New: Mention Groups
        - MentionGroups allow you to easily mention a pre-determined list of members at once
            - Mention Groups can be accessed with !mention, !ping or !p
                - !mention create to create a new groups
                - !mention edit to edit a new group
                - !mention [name] to mention all people in a group
                - !mention list to view all groups
    - Fixed an issue where the GPTCommand was formatting its AI responses, instead of sending them in plain text
    - The current PingCommand (!ping) has been changed to execute with !pong, as the MentionGroupCommand now occupies !ping

2.701
    - Fixed an issue where the HelpCommand was not updating its ApplicationCommand (Slash Command) definition when new Commands were being created
    - Fixed an issue where TimedEvents (Poll and Reserve Events) were still adjusting for daylight savings, even though it is over
    
2.702
    - Fixed various formatting issues with Mention Groups (including help and log messages)
    - Added a "silent" option to MentionGroups to view all the mentions in a group without pinging Members
        - Use "silent", "quiet" or "s" anywhere in the command to use the silent option
            - Example: '!mention cs2 silent'
                - Or, to be really concise, you can use '!p cs2 s' or '!p s cs2'
        - The ApplicationCommand (slash command) description has been updated to reflect this new option

2.703
    Misc:
        - View all outputs are now split into different Windows for long outputs when viewing all AudioTracks, ReserveEvents and MentionGroups
        - The SoapEventHandler has been refactored to be part of the SoapEventManager
            - Each SoapEventManager schedules its own events
            - The SoapEventHandler has been removed
    ReserveEvents
        - When using the ReserveCommand, if an Reserve event name that already exists is present, the command will attempt to reserve the User to the event,
        irregardless if the user indicated they were trying to create a new event or not
            - For example, consider an existing ReserveEvent called "cs2"
                - Like before, when using '!reserve cs2', SOAP Bot will attempt to reserve the user to the event
                - However, if the user used '!reserve cs2 5 9pm', SOAP Bot will now also attempt to reserve the user to event "cs2",
                instead of informing the user the event already exists like it did before.
        - The event date is no longer present for timeless ReserveEvents (events with no time and only a max number of reservees)
        - Various formatting improvements to ReserveCommand informational messages

2.704
    MentionGroups
        - When using '!mention list' to view all MentionGroups, the repeat reaction can now be used to switch to an Alternate view which
          displays the members of each group

2.710-BETA
    - Created a UserSettings system
        - Users can now have global settings to change how they interact with SOAP Bot
        - UserSettings are global across every Guild SOAP Bot is in. The settings a user sets in one Guild will modify how SOAP Bot interacts with them in every Guild.
        - Settings can be managed with the UserSettingsWizard via '!settings'
    - Created a TimezoneSetting
        - Users can now change their timezone for SOAP Bot.
            - SOAP Bot offers the 24 most common timezones as options
        - Any system (such as ReserveEvents) which use exact times will now display the time and date based on the User's timezone
          This affects the following:
            - Creating a new ReserveEvent
                - When creating a ReserveEvent, the time will be adjusted based on the User's Timezone.
                  For example: The command '!reserve cs2 5 6pm' can be used to create an event for 9PM EST if the User's timezone setting is set to PST (America/Los_Angeles).
                - When viewing a ReserveEvent
                  For example: An event set for 9pm EST will display as 6pm PST if the user's timezone is set to PST
                    - This, of course, only applies to commands done by the user. Each command will display the time based on the timezone of the user who executed the command.
                - Managing a ReserveEvent

2.720-BETA
    - Added message color settings options to the UserSettings system
        - Message colors for DEFAULT, ERROR and INFO messages can be customized in the UserSettingsWizard with !settings

2.721-BETA
    - Fixed an issue where the BlackjackCommand could not be used with the slash command feature
    - Updated the Reserve and Unreserve commands to show detailed event information every time someone reserves or unreserves from an event
    - Fixed an issue where some commands would fail when using slash commands and a floating point input was expected but not guaranteed

2.722
    - User Settings out of beta
    - Events
        - Fixed an issue where Reserve Events made by users in the EST timezone would not have the correct Timezone reflected when creating a timed event after daylight savings
        - Fixed an issue where timeless events could not be viewed with the ReserveEventCommand
        - Fixed an issue where timeless events could not have their date or time managed with the ReserveEventWizard
        - Fixed an issue where timeless events would show an error message when users reserved or unreserved to them
            - These issues were related to internal Java Timezone conversions still being attempted when no time was present

2.723
    - Events
        - Fixed an issue where DateTimed objects (ReserveEvents, PollEvents) were incorrectly displaying their time if the user's timezone was 'EST' due to daylight savings adjustments