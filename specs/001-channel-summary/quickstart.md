# Quickstart Guide: Channel Summary Feature

**Feature**: Channel Summary Command  
**Estimated Implementation Time**: 3-4 hours  
**Difficulty**: Intermediate (requires Discord4J and OpenAI API familiarity)

## Prerequisites

- ✅ Java development environment set up (Java 11+)
- ✅ Maven installed and configured
- ✅ Workspace: `c:\Users\Michael\Workspace\soapbot`
- ✅ Existing codebase knowledge (Command pattern, UserProfileManager)
- ✅ OpenAI API key already configured in `src/main/java/com/georgster/gpt/openaikey.txt`
- ✅ Discord4J dependency already in pom.xml

## Implementation Checklist

### Phase 1: Create SummaryCommand Class (1-1.5 hours)

#### Step 1.1: Create Package and Class File

```bash
# Create directory structure
mkdir -p src/main/java/com/georgster/summary

# Create the class file
# File: src/main/java/com/georgster/summary/SummaryCommand.java
```

#### Step 1.2: Implement Basic Structure

Reference: [GPTCommand.java](../../../src/main/java/com/georgster/gpt/GPTCommand.java) for pattern

```java
package com.georgster.summary;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import com.georgster.ParseableCommand;
import com.georgster.control.manager.UserProfileManager;
import com.georgster.control.util.ClientContext;
import com.georgster.control.util.CommandExecutionEvent;
import com.georgster.logs.LogDestination;
import com.georgster.logs.MultiLogger;
import com.georgster.permissions.PermissibleAction;
import com.georgster.util.commands.CommandParser;
import com.georgster.util.handler.GuildInteractionHandler;
import com.georgster.util.handler.InteractionHandler.MessageFormatting;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.discordjson.json.ApplicationCommandRequest;

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
    public SummaryCommand(ClientContext context) {
        this.manager = context.getUserProfileManager();
    }

    // TODO: Implement execute() method
    // TODO: Implement getCommandParser() method
    // TODO: Implement getAliases() method
    // TODO: Implement help() method
    // TODO: Implement getRequiredPermission() method
    // TODO: Implement getCommandApplicationInformation() method
    // TODO: Implement shouldDefer() method
}
```

#### Step 1.3: Implement execute() Method

This is the core logic. See [contracts/java-method-contracts.md](contracts/java-method-contracts.md) for detailed contract.

```java
@Override
public void execute(CommandExecutionEvent event) {
    GuildInteractionHandler handler = event.getGuildInteractionHandler();
    MultiLogger logger = event.getLogger();

    logger.append("- Retrieving last 50 messages from channel\n", LogDestination.NONAPI, LogDestination.API);

    try {
        // 1. Get the channel where command was invoked
        MessageChannel channel = event.getDiscordEvent().getMessage()
            .flatMap(Message::getChannel)
            .block();

        if (channel == null) {
            handler.sendMessage("Unable to access this channel.", MessageFormatting.ERROR);
            return;
        }

        // 2. Retrieve last 50 messages
        List<Message> messages = channel.getMessagesBefore(Snowflake.of(Instant.now()))
            .take(50)
            .collectList()
            .block();

        if (messages == null || messages.isEmpty()) {
            handler.sendMessage("No messages to summarize in this channel.", MessageFormatting.INFO);
            logger.append("- No messages found in channel", LogDestination.NONAPI);
            return;
        }

        // 3. Filter to user messages only (exclude bots and system messages)
        List<String> userMessageContents = messages.stream()
            .filter(msg -> msg.getAuthor().map(user -> !user.isBot()).orElse(false))
            .filter(msg -> msg.getType() == Message.Type.DEFAULT || 
                          msg.getType() == Message.Type.REPLY)
            .map(Message::getContent)
            .collect(Collectors.toList());

        if (userMessageContents.isEmpty()) {
            handler.sendMessage("No messages to summarize in this channel.", MessageFormatting.INFO);
            logger.append("- All messages filtered out (bots/system messages)", LogDestination.NONAPI);
            return;
        }

        logger.append("- Found " + userMessageContents.size() + " user messages, generating summary\n", 
                     LogDestination.NONAPI, LogDestination.API);

        // 4. Generate summary using AI
        String channelName = channel.toString(); // or extract name from channel object
        String summary = manager.createSummaryCompletion(userMessageContents, channelName);

        // 5. Post summary
        handler.sendPlainMessage(summary);
        logger.append("- Summary generated and posted successfully", LogDestination.NONAPI);

    } catch (RuntimeException e) {
        handler.sendMessage("Sorry, I couldn't process this in time. Please try again.", MessageFormatting.ERROR);
        logger.append("- Summary generation failed: " + e.getMessage(), LogDestination.NONAPI);
        e.printStackTrace();
    }
}
```

#### Step 1.4: Implement Remaining Methods

```java
@Override
public CommandParser getCommandParser() {
    return new CommandParser("V"); // No arguments required
}

@Override
public List<String> getAliases() {
    return List.of("summary", "summarize", "recap");
}

@Override
public String help() {
    return "Aliases: " + getAliases().toString() +
           "\n- !summary - Generate a summary of the last 50 messages in this channel" +
           "\n- /summary - Same as above (slash command)";
}

@Override
public PermissibleAction getRequiredPermission(List<String> args) {
    return PermissibleAction.SUMMARYCOMMAND;
}

@Override
public ApplicationCommandRequest getCommandApplicationInformation() {
    return ApplicationCommandRequest.builder()
            .name(getAliases().get(0))
            .description("Generate a summary of the last 50 messages in this channel")
            .build();
}

@Override
public boolean shouldDefer() {
    return true; // May take >3 seconds
}
```

**Checkpoint**: ✅ SummaryCommand class is complete

---

### Phase 2: Enhance UserProfileManager (30-45 minutes)

#### Step 2.1: Add createSummaryCompletion() Method

**File**: `src/main/java/com/georgster/control/manager/UserProfileManager.java`

**Location**: After existing `createCompletion()` and `createCompletionGetAll()` methods (around line 200-230)

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
 * @param messages List of message content strings to summarize, ordered oldest to newest.
 * @param channelName Name of the Discord channel being summarized.
 * @return A string containing the AI-generated summary (single paragraph, ~50-75 words)
 * @throws RuntimeException if OpenAI API call times out or fails
 * @throws IllegalArgumentException if messages list is null or empty
 */
public String createSummaryCompletion(List<String> messages, String channelName) {
    if (messages == null || messages.isEmpty()) {
        throw new IllegalArgumentException("Messages list cannot be null or empty");
    }
    if (channelName == null) {
        throw new IllegalArgumentException("Channel name cannot be null");
    }

    // Construct system prompt with constraints
    String systemPrompt = "You are summarizing a Discord text channel conversation. " +
                         "Create a single paragraph summary under 75 words that captures " +
                         "the main topics discussed. Focus on what was talked about, not " +
                         "who said what. Be concise and clear.";

    // Construct user prompt with message list
    StringBuilder userPrompt = new StringBuilder();
    userPrompt.append("Summarize these messages from the #").append(channelName).append(" channel:\n\n");
    
    for (int i = 0; i < messages.size(); i++) {
        userPrompt.append("[Message ").append(i + 1).append("]: ")
                  .append(messages.get(i)).append("\n");
    }

    // Build message list for API
    List<ChatMessage> chatMessages = new ArrayList<>();
    chatMessages.add(new ChatMessage("system", systemPrompt));
    chatMessages.add(new ChatMessage("user", userPrompt.toString()));

    // Create and execute request
    ChatCompletionRequest request = ChatCompletionRequest.builder()
        .messages(chatMessages)
        .model("gpt-3.5-turbo")
        .build();

    ChatCompletionResult result = aiService.createChatCompletion(request);

    // Extract and return summary (does NOT save to user profile)
    return result.getChoices().get(0).getMessage().getContent();
}
```

**Imports to Add** (at top of UserProfileManager.java):
```java
import java.util.ArrayList; // May already be imported
import com.theokanning.openai.completion.chat.ChatMessage; // May already be imported
```

**Checkpoint**: ✅ UserProfileManager enhanced with summary method

---

### Phase 3: Add Permission Enum (10 minutes)

#### Step 3.1: Locate PermissibleAction Enum

Find the PermissibleAction enum definition (likely in `com.georgster.permissions` package).

#### Step 3.2: Add SUMMARYCOMMAND Entry

Add this entry to the enum:

```java
/**
 * Permission to execute the summary command.
 * Allows users to generate AI summaries of channel message history.
 */
SUMMARYCOMMAND
```

**Example Location** (if enum looks like):
```java
public enum PermissibleAction {
    DEFAULT,
    GPTCOMMAND,
    MUSICCOMMAND,
    // ... other permissions
    SUMMARYCOMMAND  // <-- Add this
}
```

**Checkpoint**: ✅ Permission enum updated

---

### Phase 4: Register Command (10 minutes)

#### Step 4.1: Locate CommandRegistry

Find where commands are registered (likely `CommandRegistry.java` in `com.georgster.control` package).

#### Step 4.2: Register SummaryCommand

Add SummaryCommand to the registry (exact mechanism depends on existing registry pattern):

```java
// Example (adjust to actual registry pattern):
registry.register(SummaryCommand.class);

// Or if using explicit registration:
registry.put("summary", SummaryCommand.class);
```

**Note**: Consult existing command registrations (GPTCommand, etc.) to match the pattern.

**Checkpoint**: ✅ Command registered

---

### Phase 5: Build and Test (30 minutes)

#### Step 5.1: Compile

```bash
cd c:\Users\Michael\Workspace\soapbot
mvn clean compile
```

**Expected**: No compilation errors

#### Step 5.2: Run Bot

```bash
mvn exec:java
# Or your usual run command
```

#### Step 5.3: Test Cases

**Test 1**: Basic Summary
1. Go to a Discord channel with message history
2. Type `/summary` or `!summary`
3. Wait 3-10 seconds
4. **Expected**: Bot posts a single paragraph summary under 75 words

**Test 2**: Empty Channel
1. Create a new channel with no messages
2. Type `/summary`
3. **Expected**: "No messages to summarize in this channel."

**Test 3**: Bot-Only Channel
1. Go to a channel with only bot messages
2. Type `/summary`
3. **Expected**: "No messages to summarize in this channel."

**Test 4**: Slash vs Prefix
1. Test `/summary` (slash command)
2. Test `!summary` (prefix command)
3. **Expected**: Both work identically

**Test 5**: Aliases
1. Test `!summarize`
2. Test `!recap`
3. **Expected**: All aliases work

**Checkpoint**: ✅ All tests pass

---

## Quick Reference

### File Checklist

- [x] `src/main/java/com/georgster/summary/SummaryCommand.java` (NEW)
- [x] `src/main/java/com/georgster/control/manager/UserProfileManager.java` (MODIFIED - add createSummaryCompletion())
- [x] `src/main/java/com/georgster/permissions/PermissibleAction.java` (MODIFIED - add SUMMARYCOMMAND)
- [x] Command registry file (MODIFIED - register SummaryCommand)

### Key Dependencies (Already in Project)

- Discord4J (for message retrieval and command handling)
- OpenAI Java SDK (for AI completion)
- Existing command infrastructure (Command, ParseableCommand, CommandExecutionEvent, etc.)

### Important Notes

1. **No Database Changes**: This feature is entirely ephemeral (no persistence)
2. **Reuses Existing AI Service**: Uses the singleton OpenAiService from UserProfileManager
3. **Follows Existing Patterns**: Matches GPTCommand structure and conventions
4. **Defers Response**: Command uses `shouldDefer() = true` to avoid Discord timeout
5. **User-Friendly Errors**: All errors show friendly messages (no stack traces to users)

---

## Troubleshooting

### Common Issues

**Issue**: Compilation error: "Cannot resolve symbol ChatMessage"
- **Fix**: Ensure `import com.theokanning.openai.completion.chat.ChatMessage;` is present in UserProfileManager.java

**Issue**: Command not recognized in Discord
- **Fix**: Verify SummaryCommand is registered in CommandRegistry

**Issue**: "I don't have permission" error when testing
- **Fix**: Ensure bot has `READ_MESSAGE_HISTORY` and `VIEW_CHANNEL` permissions in the test channel

**Issue**: AI timeout every time
- **Fix**: Check OpenAI API key is valid and not rate-limited. Verify internet connection.

**Issue**: Summary exceeds 75 words
- **Fix**: This is an AI prompt adherence issue. The prompt constrains it, but AI may occasionally exceed. This is acceptable per spec (logged, not enforced).

---

## Next Steps After Implementation

1. **Monitor Logs**: Watch for errors or unexpected behavior in production
2. **Gather Feedback**: Ask users if summaries are useful and accurate
3. **Iterate on Prompt**: Adjust system prompt in `createSummaryCompletion()` if summaries need improvement
4. **Consider Enhancements** (Future):
   - Configurable message count (not just 50)
   - Summary length options (short/medium/long)
   - Channel selection (summarize other channels)
   - Scheduled summaries (daily recap at midnight)

---

## Estimated Timeline

| Phase | Task | Time |
|-------|------|------|
| 1 | Create SummaryCommand class | 1-1.5 hours |
| 2 | Enhance UserProfileManager | 30-45 minutes |
| 3 | Add permission enum | 10 minutes |
| 4 | Register command | 10 minutes |
| 5 | Build and test | 30 minutes |
| **Total** | | **3-4 hours** |

---

## Success Criteria

- ✅ `/summary` and `!summary` commands work in Discord
- ✅ Summary generated in <10 seconds
- ✅ Summary is single paragraph
- ✅ Summary is approximately ≤75 words
- ✅ Bot and system messages are excluded
- ✅ Empty channel case handled gracefully
- ✅ Error messages are user-friendly
- ✅ No compilation errors
- ✅ No runtime exceptions (all caught and handled)
- ✅ Code follows existing patterns (matches GPTCommand style)
- ✅ All constitution principles respected (see plan.md)

---

**Status**: 📋 READY FOR IMPLEMENTATION

For detailed technical specifications, see:
- [plan.md](plan.md) - Overall implementation plan
- [research.md](research.md) - Technical research and decisions
- [data-model.md](data-model.md) - Entity and data flow definitions
- [contracts/discord-command-contract.md](contracts/discord-command-contract.md) - Discord API contract
- [contracts/java-method-contracts.md](contracts/java-method-contracts.md) - Java method signatures and contracts
