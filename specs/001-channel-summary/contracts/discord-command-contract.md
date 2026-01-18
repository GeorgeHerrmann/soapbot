# Discord Slash Command Contract: /summary

**Version**: 1.0  
**Command Name**: `summary`  
**Command Type**: Slash Command (ApplicationCommandInteraction)  
**Aliases**: `summarize`, `recap` (for text-based prefix commands like `!summary`)

## Command Definition

### Discord Application Command Registration

```json
{
  "name": "summary",
  "description": "Generate a summary of the last 100 messages in this channel",
  "type": 1,
  "options": []
}
```

**Properties**:
- `name`: `"summary"` (primary alias for slash command)
- `description`: `"Generate a summary of the last 50 messages in this channel"`
- `type`: `1` (CHAT_INPUT - standard slash command)
- `options`: `[]` (no parameters required)

### Java Implementation (Discord4J)

```java
ApplicationCommandRequest.builder()
    .name("summary")
    .description("Generate a summary of the last 100 messages in this channel")
    .build()
```

## Request Specification

### Input

**Command Invocation**:
- `/summary` - Discord slash command
- `!summary` - Prefix command (alternative)
- `!summarize` - Prefix command alias
- `!recap` - Prefix command alias

**Context (Implicit)**:
- `channelId`: The ID of the channel where command was invoked (from Discord event)
- `userId`: The ID of the user who invoked command (from Discord event)
- `guildId`: The ID of the guild/server (from Discord event)

**Parameters**: None (command takes no user-provided arguments)

**Preconditions**:
- User must have permission to use the command (checked via PermissibleAction.SUMMARYCOMMAND)
- Bot must have `VIEW_CHANNEL` and `READ_MESSAGE_HISTORY` permissions in the channel
- Bot must have `SEND_MESSAGES` permission to reply
- Channel must be a text channel (not voice, category, etc.)

## Response Specification

### Success Response (200 OK equivalent)

**Type**: Plain text message posted to Discord channel  
**Format**: Discord message reply to the original command invocation

**Structure**:
```
{summary text content}
```

**Example**:
```
Users discussed the new feature launch, shared feedback on the UI changes, 
and coordinated meeting times for next week. Several bug reports were filed 
and acknowledged by the team. Weekend plans were also briefly mentioned.
```

**Properties**:
- Single paragraph (no multiple line breaks)
- Maximum 75 words
- Posted as a reply to the command message (slash command interaction response)
- Plain text (no embeds, no special formatting)

**Response Time**: 3-10 seconds typical

### Error Responses

#### No Messages Available

**Condition**: Channel has 0 messages, or all messages filtered out (all bots/system)

**Response**:
```
No messages to summarize in this channel.
```

**Type**: INFO message (neutral tone)

#### Permission Error

**Condition**: Bot lacks required permissions (READ_MESSAGE_HISTORY, VIEW_CHANNEL, SEND_MESSAGES)

**Response**:
```
I don't have permission to read messages in this channel.
```

**Type**: ERROR message (red/error formatting via MessageFormatting.ERROR)

#### AI Service Error

**Condition**: OpenAI API timeout, rate limit, or service error

**Response**:
```
Sorry, I couldn't process this in time. Please try again.
```

**Type**: ERROR message

**Logging**: Full exception stack trace logged internally (not shown to user)

#### Generic Error

**Condition**: Unexpected exception during command execution

**Response**:
```
{Command help text - shows usage information}
```

**Type**: Default error handler (CommandExecutionEvent logs and shows help)

## Internal Contract: UserProfileManager.createSummaryCompletion()

This is the internal API contract between SummaryCommand and UserProfileManager.

### Method Signature

```java
/**
 * Creates a summary of the provided messages using OpenAI's GPT model.
 * Unlike createCompletion(), this does NOT save to user chat history.
 * 
 * @param messages List of message contents to summarize (ordered, most recent last)
 * @param channelName Name of the channel being summarized (for context)
 * @return A single-paragraph summary under 75 words
 * @throws RuntimeException if OpenAI API call fails or times out
 */
public String createSummaryCompletion(List<String> messages, String channelName);
```

### Request (Internal)

**Input Parameters**:
- `messages: List<String>` - Ordered list of message text content
  - Must not be empty (validated by caller)
  - Should be ≤50 entries
  - Order: oldest to newest (most recent last)
- `channelName: String` - Name of Discord channel (e.g., "general", "dev-chat")
  - Used for logging and potential context

**Internal Processing**:
1. Construct system prompt:
   ```
   You are summarizing a Discord text channel conversation. Create a single 
   paragraph summary under 75 words that captures the main topics discussed. 
   Focus on what was talked about, not who said what. Be concise and clear.
   ```

2. Construct user prompt:
   ```
   Summarize these messages from the channel:

   [Message 1]: {content}
   [Message 2]: {content}
   ...
   [Message N]: {content}
   ```

3. Create ChatCompletionRequest:
   ```java
   ChatCompletionRequest.builder()
       .messages(List.of(
           new ChatMessage("system", systemPrompt),
           new ChatMessage("user", userPrompt)
       ))
       .model("gpt-3.5-turbo")
       .build()
   ```

4. Call `aiService.createChatCompletion(request)`

5. Extract first choice: `result.getChoices().get(0).getMessage().getContent()`

6. Return summary string

### Response (Internal)

**Success**:
- Returns `String` containing the summary
- Summary format: Single paragraph, ≤75 words (enforced by AI prompt)

**Failure**:
- Throws `RuntimeException` on timeout or API error
- Exception message contains error details for logging
- Caller (SummaryCommand) catches and handles with user-friendly message

### Constraints

- **Does NOT save to user profile**: Unlike `createCompletion()`, this method does NOT call `profile.getCompletions().addCompletion()`
- **No conversation history**: Does NOT include previous chat completions in the request
- **Stateless**: Each invocation is independent
- **Token limit**: If messages exceed ~4000 tokens, truncate to most recent messages that fit

## Example Interaction Flow

### Successful Summary

1. **User Input**: `/summary` (in #general channel)
2. **Bot Action**: 
   - Defers response (Discord slash command acknowledgment)
   - Retrieves last 100 messages from #general
   - Filters to 78 user messages (22 bot/system messages excluded)
   - Formats messages into prompt
   - Calls OpenAI API
3. **Bot Response** (5 seconds later):
   ```
   The team discussed upcoming feature priorities, with several members 
   proposing new ideas for the dashboard. Bug fixes were coordinated, 
   and a code review session was scheduled for Thursday. Some off-topic 
   banter about weekend plans also occurred.
   ```

### Edge Case: New Channel

1. **User Input**: `/summary` (in newly created #announcements channel with 0 messages)
2. **Bot Action**:
   - Defers response
   - Retrieves messages (returns empty list)
   - Detects empty list
3. **Bot Response** (1 second later):
   ```
   No messages to summarize in this channel.
   ```

### Error Case: API Timeout

1. **User Input**: `/summary`
2. **Bot Action**:
   - Defers response
   - Retrieves and filters messages
   - Calls OpenAI API
   - API times out (network issue)
3. **Bot Response** (10+ seconds later):
   ```
   Sorry, I couldn't process this in time. Please try again.
   ```
4. **Internal Log**:
   ```
   [ERROR] - Summary generation failed: SocketTimeoutException: Read timed out
   ```

## Testing Contract

### Test Cases

1. **TC-001**: Command in channel with 100+ messages → Summary posted
2. **TC-002**: Command in channel with <100 messages → Summary of all available messages
3. **TC-003**: Command in channel with 0 messages → "No messages to summarize"
4. **TC-004**: Command in channel with only bot messages → "No messages to summarize"
5. **TC-005**: Command with missing permissions → Permission error message
6. **TC-006**: Command during OpenAI API outage → Timeout error message
7. **TC-007**: Summary length verification → Confirm ≤75 words
8. **TC-008**: Summary format verification → Confirm single paragraph
9. **TC-009**: Response time verification → Confirm <10 seconds typical
10. **TC-010**: Slash command vs prefix command → Both work identically

### Acceptance Criteria

- ✅ Summary posted within 10 seconds for typical case
- ✅ Summary is single paragraph
- ✅ Summary is ≤75 words
- ✅ Only user messages included (bots/system filtered)
- ✅ Error messages are user-friendly (no technical details)
- ✅ Command works via slash and prefix invocation
- ✅ No data persisted (ephemeral operation)
- ✅ Follows existing command patterns (GPTCommand style)

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-01-17 | Initial contract definition |

---

**Contract Status**: ✅ APPROVED - Ready for implementation
