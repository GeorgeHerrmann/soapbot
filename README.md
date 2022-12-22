# soapbot

A simple Discord Bot. You may notice in very early commits that an API key is visible, that is for the old Bot registration and doesn't do anything anymore.
Currently the key is hidden and read from an external location by the program.
SOAP Bot has a basic API via the SOAP API (https://github.com/GeorgeHerrmann/soapapi)

Current features:
  - Simulating a game of Plinko in a text channel
    - Features a basic reward system
  - Playing audio tracks (via YouTube, Soundcloud, etc.) in a voice channel
    - Features a queue system allowing queueing of one track at a time or a whole playlist at once
    - Features the option to show the queue and skip tracks in the queue (or skip all tracks)
  - Creating to and reserving for events with a full save system
    - Allows the flexibility of multiple types of events
  - Basic text-based responses to commands in text-channels
  - Saves and loads data via "profiles" for SOAP Bot to keep track of basic user information even when the bot goes offline or is reloaded
