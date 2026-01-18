# Research: Channel Summary Feature

**Date**: January 17, 2026  
**Feature**: Channel Summary Command  
**Purpose**: Resolve technical unknowns and establish best practices for implementation

## Research Tasks Completed

### 1. Discord4J Message History Retrieval

**Decision**: Use `MessageChannel.getMessagesBefore()` or `MessageChannel.getMessagesAfter()` with limit parameter

**Rationale**:
- Discord4J provides built-in pagination support for message history
- `getMessagesBefore(Snowflake messageId)` returns a `Flux<Message>` that can be limited with `.take(50)`
- Can use `Snowflake.of(Instant.now())` to get messages before current time
- Alternatively, use channel's `getLastMessageId()` as starting point
- Built-in methods handle Discord rate limits and pagination automatically

**Alternatives considered**:
- Manual REST API calls: Rejected due to complexity and lack of built-in rate limiting
- Storing messages locally: Rejected due to memory constraints and stale data risk
- WebSocket real-time tracking: Rejected due to resource overhead for this use case

**Best Practices**:
```java
// Get last 50 messages from a channel
channel.getMessagesBefore(Snowflake.of(Instant.now()))
    .take(50)
    .collectList()
    .subscribe(messages -> {
        // Process messages
    });
```

### 2. Message Filtering Strategies

**Decision**: Filter using `Message.getAuthor()` to exclude bots and check `Message.getType()` for system messages

**Rationale**:
- Discord4J Message objects have `.getAuthor()` which returns a User
- User objects have `.isBot()` method to identify bot messages
- Message type enum (`Message.Type`) includes system message types (JOIN, PIN, etc.)
- Filtering should happen after retrieval to minimize API calls
- Keep user text content only: `Message.getContent()` returns the text

**Alternatives considered**:
- Server-side filtering: Not supported by Discord API for message history
- Regex-based content filtering: Rejected as too complex and error-prone
- Whitelist approach: Rejected as it's more brittle than blacklist for this use case

**Best Practices**:
```java
messages.stream()
    .filter(msg -> msg.getAuthor()
        .map(user -> !user.isBot())
        .orElse(false))
    .filter(msg -> msg.getType() == Message.Type.DEFAULT || 
                   msg.getType() == Message.Type.REPLY)
    .collect(Collectors.toList());
```

### 3. AI Prompt Construction for Summarization

**Decision**: Create a new method `createSummaryCompletion()` in UserProfileManager with dedicated system prompt

**Rationale**:
- Existing `createCompletion()` methods use "You are a Discord bot called SOAP Bot" which is too general
- Summary requires specific constraints: one paragraph, under 75 words, topic-focused
- Should NOT save summary in user's chat completion history (no conversation context needed)
- Use standalone request without conversation history to avoid token waste
- OpenAI's gpt-3.5-turbo supports instruction-following well for constrained outputs

**System Prompt Design**:
```
You are summarizing a Discord text channel conversation. Create a single paragraph summary 
under 75 words that captures the main topics discussed. Focus on what was talked about, 
not who said what. Be concise and clear.
```

**User Prompt Design**:
```
Summarize these messages from the channel:

[Message 1]: {content}
[Message 2]: {content}
...
[Message N]: {content}
```

**Alternatives considered**:
- Reuse existing `createCompletion()`: Rejected because it saves to chat history and uses wrong system prompt
- Include usernames in summary: Rejected per spec (focus on topics, not participants)
- Multiple AI calls for very long message sets: Rejected as 50 messages typically fit in token limit

**Token Estimation**:
- 50 messages × 100 words average = 5,000 words ≈ 6,700 tokens (within gpt-3.5-turbo's 4,096 token context)
- If token limit exceeded: Truncate to most recent messages that fit

### 4. Error Handling Patterns

**Decision**: Three-tier error handling: Discord errors, AI errors, and empty channel cases

**Rationale**:
- Follow existing pattern in GPTCommand (try-catch with user-friendly messages)
- Discord API errors: Permissions, rate limits, channel access
- OpenAI API errors: Timeouts, rate limits, service unavailable
- Edge cases: 0 messages, all messages filtered out, summary generation failure

**Error Response Patterns**:
```java
// No messages case
if (messages.isEmpty()) {
    handler.sendMessage("No messages to summarize in this channel.", 
                       MessageFormatting.INFO);
    return;
}

// AI timeout/error case
try {
    String summary = // ... AI call
} catch (RuntimeException e) {
    handler.sendMessage("Summary generation failed. Please try again later.", 
                       MessageFormatting.ERROR);
    logger.append("- Summary generation failed: " + e.getMessage(), 
                  LogDestination.NONAPI);
}
```

**Alternatives considered**:
- Detailed error messages to users: Rejected per spec (user-friendly only)
- Retry logic: Deferred to future enhancement (keep simple initially)
- Fallback to shorter message window: Rejected as adds complexity

### 5. Command Registration and Permissions

**Decision**: Simple command with no arguments; require default permissions (SUMMARYCOMMAND permission action)

**Rationale**:
- Follows existing command pattern (GPTCommand has GPTCOMMAND permission)
- No parameters needed (always summarizes last 50 messages from invocation channel)
- Slash command with `shouldDefer() = true` (may take >3 seconds)
- Primary alias: "summary", additional aliases: "summarize", "recap"

**ApplicationCommandRequest**:
```java
ApplicationCommandRequest.builder()
    .name("summary")
    .description("Generate a summary of the last 50 messages in this channel")
    .build()
```

**Alternatives considered**:
- Parameterized message count: Rejected to keep MVP simple
- Channel parameter: Rejected (always use invocation channel per spec)
- Admin-only permission: Rejected (should be available to all users by default)

## Dependencies Confirmed

### Existing Code to Leverage

1. **UserProfileManager** (`com.georgster.control.manager.UserProfileManager`)
   - Access to OpenAI service singleton
   - Pattern for creating chat completions
   - Add new method: `createSummaryCompletion(List<String> messages, String channelName)`

2. **GPTCommand** (`com.georgster.gpt.GPTCommand`)
   - Template for command structure
   - Error handling patterns
   - Slash command registration pattern

3. **GuildInteractionHandler** (`com.georgster.util.handler.GuildInteractionHandler`)
   - Message sending utilities
   - Message formatting (ERROR, INFO, plain)

4. **CommandExecutionEvent** (`com.georgster.control.util.CommandExecutionEvent`)
   - Access to Discord event context
   - Logger access
   - Handler access

### New Code Required

1. **SummaryCommand.java** (new file in `com.georgster.summary` package)
   - Implements ParseableCommand (though no arguments needed, follows pattern)
   - Retrieve messages using Discord4J
   - Filter messages
   - Format for AI
   - Call UserProfileManager
   - Post response

2. **UserProfileManager enhancement** (modify existing)
   - Add `createSummaryCompletion(List<String> messages, String channelName)` method
   - Does NOT save to user chat history
   - Uses dedicated summary system prompt

3. **PermissibleAction enum** (modify existing)
   - Add SUMMARYCOMMAND entry

## Implementation Notes

### Message Retrieval Flow
1. Get channel from CommandExecutionEvent
2. Retrieve last 50 messages using Discord4J API
3. Filter out bot messages and system messages
4. Extract text content only
5. Handle case where <50 messages available

### AI Integration Flow
1. Format filtered messages into prompt structure
2. Call new UserProfileManager.createSummaryCompletion()
3. UserProfileManager constructs request with summary system prompt
4. OpenAI API call with gpt-3.5-turbo
5. Return first response (ignore multiple choices if any)

### Response Flow
1. Post summary as reply to command invocation
2. Use plain message format (not embed, keep simple)
3. Log success/failure appropriately

### Performance Considerations
- Message retrieval: ~200-500ms (Discord API call)
- AI generation: ~2-8 seconds (OpenAI API call)
- Total: ~3-9 seconds (within 10 second target)
- Slash command deferral ensures no Discord timeout

## Risks and Mitigations

| Risk | Mitigation |
|------|-----------|
| Token limit exceeded (>4096 tokens) | Truncate to most recent messages that fit; log warning |
| All messages filtered out | Treat same as 0 messages; inform user |
| OpenAI rate limit hit | Catch error, display user-friendly message, log for monitoring |
| Channel permission error | Catch Discord4J error, inform user of permission issue |
| Memory spike from large messages | Use streaming where possible; limit to 50 messages enforces ceiling |

## Summary of Decisions

All technical unknowns from the plan have been resolved:
- ✅ Message retrieval API: Discord4J getMessagesBefore() with .take(50)
- ✅ Filtering strategy: Filter by isBot() and message type
- ✅ AI prompt design: Dedicated summary system prompt with constraints
- ✅ Error handling: Three-tier with user-friendly messages
- ✅ Command structure: Simple no-arg command following existing patterns
- ✅ Performance approach: Slash command deferral for >3 second operations

**Ready to proceed to Phase 1: Design**
