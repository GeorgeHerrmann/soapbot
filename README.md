# soapbot

SOAP Bot is a highly efficient and capable Discord bot written in Java using the Discord4J wrapper. At a high level, a ClientManager controls all SOAPClient's in a shard, which corresponds to each Guild/server SOAP Bot is in. This system of systems design allows SOAP Bot to have highly complex systems for each server it is in, while maintaining a high level of speed and low memory usage. The systems are only active once the ClientManager distributes Discord's events to the Client. All of SOAP Bot runs in a 1 vcore 1GB RAM server. To read more about how SOAP Bot works, visit http://georgster.com. SOAP Bot's codebase is open-source and highly organized and commented. The master branch remains in production, so to see on-going feature development check the other branches.

Invite SOAP Bot to your server: https://discord.com/api/oauth2/authorize?client_id=1032686902026973225&permissions=277062872640&scope=applications.commands%20bot

SOAP Bot has a basic API via the SOAP API (https://github.com/GeorgeHerrmann/soapapi)

Current features:
  - Simulating games in text-channels (ex: plinko, blackjack)
  - Playing audio (via Youtube, Soundcloud, etc.) in voice-channels
    - Features a queue and skip system, allows for complete autonomy between servers
  - A complex and functional event system, allowing users to schedule various types of events
    - Events are saved and scheduled, even if the bot happens to go offline.
    - Features a high level of flexibility, allowing for various types of events with times, player counts, etc.
    - Allows users to manage events and features utility for events
  - A poll system with a user friendly window system.
    - Polls can be created, edited, voted on and viewed and last for a specified amount of time.
  - A fully-featured permissions system that integrates with Discord's role system.
  - A back and forth dynamic conversational system via the InputWizard and UserInputListener
    - Allows for users to input data via user-friendly "wizards"
    - Is highly modular and easy to setup and edit, allowing for full flexibility and customization.
  - A basic economy system
  - Full OpenAI integration
    - SOAP Bot's AI is trained to act as SOAP Bot and give helpful suggestions about the bot
    - Uses up to 10 previous messages with the AI as context for a response
  - and more!


Ongoing features are in development
