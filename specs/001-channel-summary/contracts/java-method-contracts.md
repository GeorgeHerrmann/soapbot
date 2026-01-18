# Java Method Contracts: Channel Summary Feature

## SummaryCommand Class Contract

### Class Definition

```java
package com.georgster.summary;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.util.commands.CommandParser;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.List;

/**
 * A command for generating a summary of recent messages in a Discord text channel.
 * Retrieves the last 50 messages (excluding bots and system messages) and uses
 * OpenAI to create a concise one-paragraph recap under 75 words.
 */
public class SummaryCommand implements ParseableCommand {
    private final UserProfileManager manager;
    
    /**
     * Creates a SummaryCommand from the given context.
     * 
     * @param context The context of the SoapClient for this command.
     */
    public SummaryCommand(ClientContext context);
    
    /**
     * {@inheritDoc}
     * 
     * Retrieves last 50 messages from the channel, filters to user messages only,
     * generates AI summary, and posts result to channel.
     */
    public void execute(CommandExecutionEvent event);
    
    /**
     * {@inheritDoc}
     * 
     * @return "V" - No arguments required (though we implement ParseableCommand for consistency)
     */
    public CommandParser getCommandParser();
    
    /**
     * {@inheritDoc}
     * 
     * @return List of aliases: ["summary", "summarize", "recap"]
     */
    public List<String> getAliases();
    
    /**
     * {@inheritDoc}
     * 
     * @return Help text showing command usage
     */
    public String help();
    
    /**
     * {@inheritDoc}
     * 
     * @return PermissibleAction.SUMMARYCOMMAND for non-empty args, PermissibleAction.DEFAULT otherwise
     */
    public PermissibleAction getRequiredPermission(List<String> args);
    
    /**
     * {@inheritDoc}
     * 
     * @return ApplicationCommandRequest for /summary slash command
     */
    public ApplicationCommandRequest getCommandApplicationInformation();
    
    /**
     * {@inheritDoc}
     * 
     * @return true (command may take >3 seconds, requires deferral)
     */
    public boolean shouldDefer();
}
```

### execute() Method Contract

```java
/**
 * Executes the summary command workflow:
 * 1. Retrieves last 50 messages from the invocation channel
 * 2. Filters out bot and system messages
 * 3. Handles edge case of no valid messages
 * 4. Calls AI service to generate summary
 * 5. Posts summary or error message to channel
 * 
 * @param event The command execution event containing Discord context and utilities
 * 
 * @pre event != null
 * @pre event.getDiscordEvent() is a valid message or slash command event
 * @pre event.getGuildInteractionHandler() is available
 * @pre event.getLogger() is available
 * 
 * @post Summary is posted to the same channel where command was invoked
 * @post Execution is logged (success or failure)
 * @post No state is persisted (ephemeral operation)
 * 
 * @throws No checked exceptions (all errors handled internally with user messages)
 */
public void execute(CommandExecutionEvent event) {
    // Implementation steps:
    // 1. Get channel from event.getDiscordEvent().getMessage().getChannel() or slash command channel
    // 2. Call channel.getMessagesBefore(Snowflake.of(Instant.now())).take(50).collectList()
    // 3. Filter: msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false)
    // 4. Filter: msg -> msg.getType() == Message.Type.DEFAULT || msg.getType() == Message.Type.REPLY
    // 5. If filtered list is empty: sendMessage("No messages to summarize", INFO), return
    // 6. Extract message contents: msg.getContent()
    // 7. Try: summary = manager.createSummaryCompletion(contents, channelName)
    // 8. Catch RuntimeException: sendMessage(error, ERROR), log, return
    // 9. Success: sendPlainMessage(summary), log success
}
```

---

## UserProfileManager Enhancement Contract

### New Method: createSummaryCompletion()

```java
/**
 * Creates a summary of the provided Discord messages using OpenAI's GPT model.
 * 
 * This method is distinct from createCompletion() in that it:
 * - Does NOT save the summary to any user's chat completion history
 * - Does NOT include conversation context from previous completions
 * - Uses a dedicated summary system prompt with strict formatting constraints
 * - Is stateless and ephemeral (no persistence side effects)
 * 
 * The AI is instructed to create a single paragraph under 75 words focusing on
 * main conversation topics without mentioning specific users.
 * 
 * @param messages List of message content strings to summarize, ordered oldest to newest.
 *                 Should be pre-filtered to exclude bot/system messages. Must not be empty.
 * @param channelName Name of the Discord channel being summarized (e.g., "general").
 *                    Used for logging and context. Must not be null.
 * 
 * @return A string containing the AI-generated summary (single paragraph, ~50-75 words)
 * 
 * @throws RuntimeException if OpenAI API call times out, rate limit exceeded, or service error
 * @throws IllegalArgumentException if messages list is null or empty
 * 
 * @pre messages != null && !messages.isEmpty()
 * @pre channelName != null
 * @pre aiService != null (initialized by createAiService())
 * 
 * @post No user profiles are modified
 * @post No chat completions are saved
 * @post No database writes occur
 * @post Return value is non-null and non-empty on success
 * 
 * @implNote If messages exceed token limit (~4000 tokens), implementation should
 *           truncate to most recent messages that fit and log a warning.
 */
public String createSummaryCompletion(List<String> messages, String channelName) {
    // Implementation steps:
    // 1. Validate: if (messages == null || messages.isEmpty()) throw IllegalArgumentException
    // 2. Validate: if (channelName == null) throw IllegalArgumentException
    // 3. Construct system prompt:
    //    "You are summarizing a Discord text channel conversation. Create a single 
    //     paragraph summary under 75 words that captures the main topics discussed. 
    //     Focus on what was talked about, not who said what. Be concise and clear."
    // 4. Construct user prompt:
    //    "Summarize these messages from the #" + channelName + " channel:\n\n"
    //    For each message: "[Message N]: " + content + "\n"
    // 5. Create ChatMessage list:
    //    - new ChatMessage("system", systemPrompt)
    //    - new ChatMessage("user", userPrompt)
    // 6. Create ChatCompletionRequest:
    //    .messages(chatMessages)
    //    .model("gpt-3.5-turbo")
    //    .build()
    // 7. Call aiService.createChatCompletion(request) (may throw RuntimeException)
    // 8. Extract: result.getChoices().get(0).getMessage().getContent()
    // 9. Return summary string
    // 
    // NOTE: No calls to profile.getCompletions().addCompletion()
    // NOTE: No calls to update(profile)
}
```

### Implementation Location

**File**: `src/main/java/com/georgster/control/manager/UserProfileManager.java`

**Insertion Point**: After existing `createCompletion()` and `createCompletionGetAll()` methods (around line 200-230)

**Access to aiService**: Uses existing static `OpenAiService aiService` field (already initialized)

---

## PermissibleAction Enum Addition

### Enum Value Contract

```java
/**
 * Permission to execute the summary command.
 * Allows users to generate AI summaries of channel message history.
 */
SUMMARYCOMMAND
```

**File**: Enum definition for PermissibleAction (location TBD based on existing codebase structure)

**Usage**: Returned by `SummaryCommand.getRequiredPermission()` when command is invoked with valid context

---

## Method Signatures Summary

### SummaryCommand Methods

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| Constructor | void | ClientContext | Initialize with manager reference |
| execute | void | CommandExecutionEvent | Main command logic |
| getCommandParser | CommandParser | none | Return "V" parser (no args) |
| getAliases | List<String> | none | Return ["summary", "summarize", "recap"] |
| help | String | none | Return usage information |
| getRequiredPermission | PermissibleAction | List<String> | Return SUMMARYCOMMAND |
| getCommandApplicationInformation | ApplicationCommandRequest | none | Return slash command definition |
| shouldDefer | boolean | none | Return true (may take >3s) |

### UserProfileManager Enhancement

| Method | Return Type | Parameters | Purpose |
|--------|-------------|------------|---------|
| createSummaryCompletion | String | List<String> messages, String channelName | Generate AI summary without saving to history |

### Helper Structures (Optional)

While not strictly necessary, the following helper methods may improve code clarity:

```java
/**
 * Filters a list of Discord messages to include only user-generated content.
 * Excludes bot messages and system messages (joins, pins, etc.).
 * 
 * @param messages List of Discord4J Message objects
 * @return Filtered list containing only user messages
 */
private List<Message> filterUserMessages(List<Message> messages) {
    return messages.stream()
        .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false))
        .filter(msg -> msg.getType() == Message.Type.DEFAULT || 
                      msg.getType() == Message.Type.REPLY)
        .collect(Collectors.toList());
}

/**
 * Extracts text content from filtered messages.
 * 
 * @param messages List of filtered Message objects
 * @return List of message content strings
 */
private List<String> extractMessageContents(List<Message> messages) {
    return messages.stream()
        .map(Message::getContent)
        .collect(Collectors.toList());
}
```

---

## Exception Handling Contract

### SummaryCommand Exception Behavior

| Exception Type | Source | Handling | User Message |
|---------------|--------|----------|-------------|
| DiscordException (permissions) | Discord4J message retrieval | Catch, log, send error | "I don't have permission to read messages in this channel." |
| RuntimeException (timeout) | OpenAI API call | Catch, log, send error | "Sorry, I couldn't process this in time. Please try again." |
| IllegalArgumentException | UserProfileManager validation | Should not occur (validated in SummaryCommand) | Help text (default handler) |
| Any other Exception | Unexpected errors | Default CommandExecutionEvent handler | Help text + log stack trace |

### UserProfileManager Exception Behavior

| Condition | Exception | When Thrown |
|-----------|-----------|-------------|
| messages is null | IllegalArgumentException | Immediately in method |
| messages is empty | IllegalArgumentException | Immediately in method |
| channelName is null | IllegalArgumentException | Immediately in method |
| OpenAI timeout | RuntimeException | During aiService.createChatCompletion() |
| OpenAI rate limit | RuntimeException | During aiService.createChatCompletion() |
| Network error | RuntimeException | During aiService.createChatCompletion() |

---

## Testing Contracts

### Unit Test Requirements

```java
// SummaryCommand Tests
testExecute_WithValidMessages_PostsSummary()
testExecute_WithNoMessages_PostsNoMessagesInfo()
testExecute_WithOnlyBotMessages_PostsNoMessagesInfo()
testExecute_WithAITimeout_PostsErrorMessage()
testGetAliases_ReturnsCorrectList()
testGetCommandApplicationInformation_ReturnsValidRequest()
testShouldDefer_ReturnsTrue()

// UserProfileManager Tests
testCreateSummaryCompletion_WithValidMessages_ReturnsSummary()
testCreateSummaryCompletion_WithNullMessages_ThrowsException()
testCreateSummaryCompletion_WithEmptyMessages_ThrowsException()
testCreateSummaryCompletion_DoesNotSaveToProfile()
testCreateSummaryCompletion_DoesNotIncludeConversationHistory()
```

### Integration Test Requirements

```java
testFullFlow_UserInvokesCommand_ReceivesSummary()
testFullFlow_CommandInNewChannel_ReceivesNoMessagesInfo()
testFullFlow_SlashCommandVsPrefixCommand_BothWork()
testPermissions_UserWithPermission_CommandExecutes()
testPermissions_UserWithoutPermission_CommandDenied()
```

---

**Contract Status**: ✅ COMPLETE - All method signatures and behaviors defined
