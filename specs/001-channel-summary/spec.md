# Feature Specification: Channel Summary

**Feature Branch**: `001-channel-summary`  
**Created**: January 17, 2026  
**Status**: Draft  
**Input**: User description: "Create a summary feature which will go through the last 50 messages sent to a respective guild text channel (the channel in which the action is provoked in) and give a one paragraph (under 75 word) recap of what has been said in those messages. This will utilize the already built in ai agent in the ask command of this bot."

## User Scenarios & Testing *(mandatory)*

<!--
  IMPORTANT: User stories should be PRIORITIZED as user journeys ordered by importance.
  Each user story/journey must be INDEPENDENTLY TESTABLE - meaning if you implement just ONE of them,
  you should still have a viable MVP (Minimum Viable Product) that delivers value.
  
  Assign priorities (P1, P2, P3, etc.) to each story, where P1 is the most critical.
  Think of each story as a standalone slice of functionality that can be:
  - Developed independently
  - Tested independently
  - Deployed independently
  - Demonstrated to users independently
-->

### User Story 1 - User Requests Channel Summary (Priority: P1)

A user in a Discord guild invokes the summary command in any text channel. The bot retrieves the last 50 messages from that channel and sends them to the existing AI agent (used in the ask command) to generate a concise one-paragraph recap. The recap is posted as a reply to the user's command invocation, providing a quick overview of recent conversation topics.

**Why this priority**: This is the core functionality and primary use case. Without this, the feature has no value.

**Independent Test**: Can be tested by invoking the summary command in a channel with message history and verifying that a one-paragraph summary under 75 words is returned.

**Acceptance Scenarios**:

1. **Given** a text channel with at least 50 messages, **When** a user executes the summary command, **Then** the bot retrieves the last 50 messages and passes them to the AI agent
2. **Given** the AI agent has generated a recap, **When** the recap is ready, **Then** the bot posts the recap as a reply in the same channel
3. **Given** a recap has been generated, **When** the recap is posted, **Then** the recap contains a single paragraph and is under 75 words

---

### User Story 2 - Command Handles Few Messages (Priority: P2)

A user invokes the summary command in a channel that has fewer than 50 messages. The bot should gracefully handle this scenario by summarizing all available messages instead of failing or waiting for more messages.

**Why this priority**: Edge case handling ensures the feature works across all scenarios. Users may invoke the command in lower-activity channels.

**Independent Test**: Can be tested by invoking the summary command in a channel with fewer than 50 messages and verifying that available messages are summarized.

**Acceptance Scenarios**:

1. **Given** a text channel with fewer than 50 messages, **When** a user executes the summary command, **Then** the bot retrieves all available messages
2. **Given** fewer messages are available, **When** the AI agent processes them, **Then** a summary is still generated successfully
3. **Given** a summary is generated from fewer messages, **When** it is posted, **Then** the recap is under 75 words

---

### User Story 3 - Command Respects Channel Context (Priority: P1)

The summary command should only summarize messages from the specific channel where it was invoked, ensuring that each channel's conversation remains isolated and contextually relevant.

**Why this priority**: Critical for ensuring data isolation and providing users with accurate, channel-relevant summaries.

**Independent Test**: Can be tested by invoking the summary command in multiple channels and verifying that each returns a summary specific to that channel's messages only.

**Acceptance Scenarios**:

1. **Given** multiple channels with different message histories, **When** the summary command is invoked in channel A, **Then** only messages from channel A are summarized
2. **Given** the summary is generated, **When** it is posted, **Then** it reflects only the conversation topics from that specific channel

---

### Edge Cases

- What happens when a channel has 0 messages? System should notify the user there are no messages to summarize.
- What happens when the AI agent fails to generate a summary? System should return a user-friendly error message.
- What happens when the summary exceeds 75 words? The AI agent should be instructed to prioritize brevity and stay within the limit.
- What happens when messages contain sensitive information? No additional filtering is assumed; existing AI agent guidelines apply.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST retrieve the last 50 messages from the channel where the command is invoked
- **FR-002**: System MUST pass the retrieved messages to the existing AI agent (ask command's AI agent)
- **FR-003**: System MUST ensure the AI agent generates a summary of one paragraph
- **FR-004**: System MUST ensure the summary is under 75 words
- **FR-005**: System MUST post the generated summary as a reply in the same channel
- **FR-006**: System MUST handle cases where fewer than 50 messages exist by summarizing all available messages
- **FR-007**: System MUST notify the user if the channel has no messages to summarize
- **FR-008**: System MUST handle AI agent failures gracefully with a user-friendly error message
- **FR-009**: System MUST only summarize messages from the specific channel where the command was invoked

### Key Entities

- **Message**: Discord message object containing content, author, timestamp, and channel information
- **Channel**: Discord text channel where the summary command is invoked and where messages are retrieved
- **Summary**: AI-generated text recap that is one paragraph and under 75 words

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can invoke the summary command and receive a summary within 10 seconds
- **SC-002**: 100% of generated summaries are single paragraphs under 75 words
- **SC-003**: 95% of summaries accurately capture the main topics from the last 50 messages
- **SC-004**: The feature works in channels with any message count from 0 to 10,000+ messages without errors
- **SC-005**: Summary command integration with the existing AI agent requires no modifications to the ask command itself

## Assumptions

- The existing AI agent in the ask command is available and can be reused for summary generation
- Discord API access is already established for retrieving message history
- Users have appropriate permissions to invoke commands in channels where they use the summary feature
- "Under 75 words" is interpreted as a hard constraint that the AI agent must respect via prompt instructions
