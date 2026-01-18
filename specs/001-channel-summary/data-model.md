# Data Model: Channel Summary Feature

**Date**: January 17, 2026  
**Feature**: Channel Summary Command  
**Purpose**: Define entities, fields, relationships, and state transitions for the summary feature

## Entities

### 1. SummaryCommand

**Type**: Command Implementation (not persisted)  
**Package**: `com.georgster.summary`  
**Purpose**: Execute the channel summary workflow when invoked by a user

**Fields**:
- `manager: UserProfileManager` - Access to AI service via existing profile manager

**Relationships**:
- Uses → UserProfileManager (existing)
- Implements → ParseableCommand (existing interface)
- Receives → CommandExecutionEvent (existing)
- Uses → GuildInteractionHandler (existing)

**Lifecycle**: Instantiated per command invocation, destroyed after execution completes

**Validation Rules**:
- None (command has no user-provided parameters)

### 2. FilteredMessage (Internal DTO)

**Type**: Data Transfer Object (transient, in-memory only)  
**Purpose**: Represent a user message eligible for summarization

**Fields**:
- `content: String` - The text content of the message (required, non-empty)
- `timestamp: Instant` - When the message was sent (for ordering)

**Relationships**:
- Created from → Discord4J Message object
- Consumed by → UserProfileManager.createSummaryCompletion()

**Lifecycle**: Created during filtering phase, passed to AI, then discarded

**Validation Rules**:
- Content must not be empty after filtering
- Content must be from a non-bot user
- Content must be from a DEFAULT or REPLY message type (not system messages)

**State Transitions**:
```
Discord Message → Filter Check → FilteredMessage DTO → AI Prompt → Discarded
                      ↓
                   [Rejected if bot/system]
```

### 3. SummaryRequest (Internal Model)

**Type**: AI Request Model (transient)  
**Purpose**: Encapsulate the data sent to OpenAI for summary generation

**Fields**:
- `messages: List<String>` - Ordered list of message contents (most recent last)
- `channelName: String` - Name of channel being summarized (for context)
- `systemPrompt: String` - The summary instruction prompt (constant)

**Relationships**:
- Created by → SummaryCommand
- Consumed by → UserProfileManager.createSummaryCompletion()
- Transformed into → ChatCompletionRequest (OpenAI SDK)

**Lifecycle**: Created after filtering, sent to OpenAI, response received, then discarded

**Validation Rules**:
- messages list must not be empty
- messages list should have ≤50 entries
- Total token count should be <4096 tokens (estimated: message count × 134 tokens average)

### 4. ChannelSummary (Output Model)

**Type**: Response Model (transient)  
**Purpose**: Represent the generated summary to be posted in Discord

**Fields**:
- `summary: String` - The AI-generated summary text
- `messageCount: int` - Number of messages summarized
- `channelId: String` - Channel where summary was generated
- `generatedAt: Instant` - Timestamp of summary generation

**Relationships**:
- Created by → UserProfileManager.createSummaryCompletion()
- Consumed by → SummaryCommand.execute()
- Posted via → GuildInteractionHandler

**Lifecycle**: Created after AI response, posted to Discord, then discarded

**Validation Rules**:
- summary must not be empty
- summary should be ≤75 words (enforced by AI prompt, validated for logging)
- summary should be a single paragraph (no multiple newlines)

**Post-Conditions**:
- Summary posted to Discord channel
- Logged to MultiLogger (success or failure)
- No persistence (ephemeral feature)

## Relationships Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                     Discord User Input                       │
│                    "/summary" command                        │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                      SummaryCommand                          │
│  • Receives CommandExecutionEvent                            │
│  • Has reference to UserProfileManager                       │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                  Message Retrieval Phase                     │
│  • Call channel.getMessagesBefore().take(50)                 │
│  • Returns Flux<Message> from Discord4J                      │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   Message Filtering Phase                    │
│  For each Message:                                           │
│    • Check if author.isBot() == false                        │
│    • Check if type is DEFAULT or REPLY                       │
│    • Extract content string                                  │
│  → Creates List<FilteredMessage> (or List<String>)           │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                  Validation & Edge Cases                     │
│  • If list empty → Send "No messages" to user               │
│  • If list valid → Continue to AI phase                      │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│             UserProfileManager.createSummaryCompletion()     │
│  • Constructs SummaryRequest                                 │
│  • Builds ChatCompletionRequest with:                        │
│    - System: "You are summarizing..."                        │
│    - User: "Summarize these messages:\n[list]"               │
│  • Calls aiService.createChatCompletion()                    │
│  • Returns summary string (does NOT save to user profile)    │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                    Response Handling Phase                   │
│  • Receives summary string                                   │
│  • Creates ChannelSummary object (for structure)             │
│  • Posts via handler.sendPlainMessage(summary)               │
│  • Logs success via logger.append()                          │
└────────────────────────────┬────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                     Discord Response                         │
│              Summary posted in channel                       │
└─────────────────────────────────────────────────────────────┘
```

## State Transitions

### Command Execution State Machine

```
[Idle] 
  ↓ (User invokes /summary)
[Discord Event Fired]
  ↓
[Command Instantiated]
  ↓
[Deferring Response] (slash command deferred to avoid timeout)
  ↓
[Retrieving Messages]
  ↓ (Success)                    ↓ (Error: Permission/API)
[Filtering Messages]          [Error State] → Post error message → [Complete]
  ↓ (Messages found)            ↓ (No messages after filter)
[Calling AI Service]          [Edge Case] → Post "No messages" → [Complete]
  ↓ (Success)                    ↓ (Error: Timeout/Rate Limit)
[Posting Summary]             [Error State] → Post error message → [Complete]
  ↓
[Logging Success]
  ↓
[Complete] (Command instance destroyed)
```

### Message Flow State

```
[Discord Message Object]
  ↓
[Check: Is Bot?]
  ├─ Yes → [DISCARD]
  └─ No → [Check: System Message?]
            ├─ Yes → [DISCARD]
            └─ No → [Extract Content]
                      ↓
                    [FilteredMessage/String]
                      ↓
                    [Add to List]
                      ↓
                    [Format for AI Prompt]
                      ↓
                    [Sent to OpenAI]
                      ↓
                    [Included in Summary]
```

## Data Flow Summary

1. **Input**: Discord slash command `/summary` in a text channel
2. **Process**:
   - Retrieve last 50 messages from channel via Discord4J API
   - Filter to user messages only (exclude bots and system messages)
   - Format as prompt for AI
   - Send to OpenAI via UserProfileManager
   - Receive one-paragraph summary (≤75 words)
3. **Output**: Summary posted as plain text message in the same channel
4. **Persistence**: None (completely ephemeral feature)

## Constraints and Invariants

### Invariants
- Message retrieval always targets the channel where command was invoked
- Filtering always excludes bot and system messages
- AI request always uses dedicated summary system prompt (not conversational prompt)
- Summary generation never saves to user chat completion history
- Response always posts in same channel as invocation

### Constraints
- Maximum 50 messages retrieved per invocation
- Summary must be ≤75 words (enforced via AI system prompt)
- Summary must be single paragraph (enforced via AI system prompt)
- Total execution time target: <10 seconds
- Memory usage: <100MB overhead per invocation

### Edge Case Handling
- **0 messages in channel**: Post "No messages to summarize in this channel."
- **0 messages after filtering**: Post "No messages to summarize in this channel."
- **<50 messages available**: Summarize all available messages
- **AI timeout**: Post "Summary generation failed. Please try again later."
- **Discord permission error**: Post "I don't have permission to read messages in this channel."
- **Token limit exceeded**: Truncate to most recent messages that fit (log warning)

## Integration Points

### Existing Systems
- **Discord4J**: Message retrieval, command registration, event handling
- **OpenAI Service**: AI completion generation (singleton service)
- **UserProfileManager**: Access to AI service, new method added
- **CommandRegistry**: Command registration and routing
- **PermissibleAction**: Permission checking (new SUMMARYCOMMAND action)
- **MultiLogger**: Logging for debugging and monitoring

### New Components
- **SummaryCommand**: New command class in com.georgster.summary package
- **createSummaryCompletion()**: New method in UserProfileManager

## Non-Persisted Design Rationale

This feature is entirely ephemeral by design:
- **No storage needed**: Summaries are generated on-demand and not historical
- **No caching**: Each invocation retrieves fresh messages (ensures accuracy)
- **No user state**: Unlike GPT chat completions, summaries don't build on conversation history
- **Minimal memory footprint**: All objects garbage-collected after command completes
- **Aligns with Constitution**: Minimal resource usage principle (no database overhead)

## Validation Rules Summary

| Entity | Validation | Enforcement Point |
|--------|-----------|-------------------|
| FilteredMessage | Must be from non-bot user | SummaryCommand filtering logic |
| FilteredMessage | Must be DEFAULT/REPLY type | SummaryCommand filtering logic |
| FilteredMessage | Content must be non-empty | Discord4J (messages always have content) |
| SummaryRequest | Messages list must not be empty | SummaryCommand pre-AI check |
| SummaryRequest | Token count <4096 | UserProfileManager (truncate if needed) |
| ChannelSummary | Summary not empty | OpenAI API (always returns content) |
| ChannelSummary | ≤75 words (soft constraint) | OpenAI system prompt (logged if violated) |
| ChannelSummary | Single paragraph | OpenAI system prompt |

---

**Design Complete**: All entities, relationships, and state transitions defined. Ready for contract generation (Phase 1 continuation).
