package com.georgster.control.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.georgster.coinfactory.model.CoinFactory;
import com.georgster.collectable.Collected;
import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.economy.CoinBank;
import com.georgster.gpt.MemberChatCompletions;
import com.georgster.profile.UserProfile;
import com.georgster.util.DateTimed;
import com.georgster.util.thread.ThreadPoolFactory;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Member;

/**
 * Manages all {@link UserProfile UserProfiles} for a {@link com.georgster.control.SoapClient SoapClient}.
 */
public class UserProfileManager extends GuildedSoapManager<UserProfile> {
    /**
     * The interval in milliseconds at which the factories will be processed.
     */
    public static final long FACTORY_PROCESSING_INTERVAL = 3600000;

    private static OpenAiService aiService; //The singleton AI Service to communicate with OpenAI's API
    private boolean isProcessingFactories; //Whether the factories are currently processing
    private DateTimed nextFactoryProcessTime; //The next time the factories will be processed
    
    /**
     * Creates a new UserProfileManager for the given SoapClient's {@link ClientContext}.
     * 
     * @param context The context of the SoapClient for this manager.
     */
    public UserProfileManager(ClientContext context) {
        super(context, ProfileType.PROFILES, UserProfile.class, "memberId");
        this.isProcessingFactories = false;
        createAiService();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The {@link UserProfileManager} also begins processing all {@link CoinFactory CoinFactories} upon loading.
     */
    @Override
    public void load() {
        dbService.getAllObjects().forEach(event -> {
            if (!exists(event)) {
                observees.add(event);
            }
        });
        startProcessingFactories();
        //ThreadPoolFactory.scheduleGeneralTask(handler.getId(), this::startProcessingFactories);
    }

    /**
     * Begins processing all factories in this manager. This manager will call {@link CoinFactory#process()} on each factory every {@link #FACTORY_PROCESSING_INTERVAL} ms.
     * <p>
     * Factories will only be processed once this method is called and will continue to be processed until {@link #stopProcessingFactories()} is called.
     * <p>
     * This method is called automatically when the manager is loaded and will run on a separate {@code GENERAL} task thread.
     */
    private void startProcessingFactories() {
        if (isProcessingFactories) {
            return;
        }
        isProcessingFactories = true;
        ThreadPoolFactory.scheduleGeneralTask(handler.getId(), () -> {
            while (isProcessingFactories) {
                try {
                    nextFactoryProcessTime = DateTimed.fromLocalDateTime(DateTimed.getCurrentLocalDateTime().plus(FACTORY_PROCESSING_INTERVAL, ChronoUnit.MILLIS));
                    Thread.sleep(FACTORY_PROCESSING_INTERVAL);
                    observees.forEach(profile -> {
                        profile.getFactory().process();
                    });
                    dbService.updateAllObjects(observees);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Returns the moment {@link DateTimed time} at which the factories will be processed.
     * 
     * @return The next time the factories will be processed as a {@link DateTimed}.
     */
    public DateTimed getNextFactoryProcessTime() {
        return nextFactoryProcessTime;
    }

    /**
     * Stops processing all factories in this manager. Following this method call, this manager will no longer call {@link CoinFactory#process()} on each factory every 60 seconds.
     */
    public void stopProcessingFactories() {
        isProcessingFactories = false;
    }

    /**
     * Creates the singleton OpenAiService.
     */
    private static void createAiService() {
        try {
            if (aiService == null) {
                aiService = new OpenAiService(Files.readString(Path.of(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "gpt", "openaikey.txt")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates all user profiles information with the data in the {@link GuildCreateEvent}.
     * 
     * @param event The event with all member information.
     */
    public void updateFromEvent(GuildCreateEvent event) {
        event.getGuild().getMembers().subscribe(member -> {
            String id = member.getId().asString();
            if (exists(id)) {
                // All manageables must maintained.
                UserProfile profile = get(id);
                MemberChatCompletions completions = profile.getCompletions();
                if (completions == null) completions = new MemberChatCompletions(id);
                CoinBank bank = profile.getBank();
                if (bank == null) bank = new CoinBank(id);
                List<Collected> collecteds = profile.getCollecteds();
                if (collecteds == null) collecteds = new ArrayList<>();
                CoinFactory factory = profile.getFactory();
                if (factory == null) factory = new CoinFactory(id);

                update(new UserProfile(event.getGuild().getId().asString(), id, member.getTag(), completions, bank, factory, collecteds));
            } else {
                add(new UserProfile(event.getGuild().getId().asString(), id, member.getTag()));
            }
        });
    }

    /**
     * Creates a new ChatCompletion for the provided {@link Member} based on the given
     * prompt and returns all responses.
     * <p>
     * The response will be based on the previous ten chat completions for that member
     * in this manager.
     * <p>
     * Uses OpenAI's gpt-3.5-turbo model.
     * <p>
     * <b>Note:</b> Only the first response will be saved in the member's chat completion log.
     * 
     * @param prompt The prompt from the user.
     * @param member The member who prompted the completion request.
     * @return All responses from the AI.
     */
    public List<String> createCompletionGetAll(String prompt, Member member) {
        UserProfile profile = get(member.getId().asString());

        List<String> responses = new ArrayList<>();

        createCompletionRequest(prompt, member).getChoices().forEach(choice -> responses.add(choice.getMessage().getContent()));
        profile.getCompletions().addCompletion(prompt, responses.get(0));

        update(profile);

        return responses;
    }

    /**
     * Creates a new ChatCompletion for the provided {@link Member} based on the given
     * prompt and returns the first response.
     * The response will be based on the previous ten chat completions for that member
     * in this manager. Uses OpenAI's gpt-3.5-turbo model.
     * 
     * @param prompt The prompt from the user.
     * @param member The member who prompted the completion request.
     * @return The first responses from the AI.
     */
    public String createCompletion(String prompt, Member member) {
        UserProfile profile = get(member.getId().asString());

        String response = createCompletionRequest(prompt, member).getChoices().get(0).getMessage().getContent();
        profile.getCompletions().addCompletion(prompt, response);

        update(profile);

        return response;
    }

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
     */
    public String createSummaryCompletion(List<String> messages, String channelName) {
        // Validation
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("Messages list cannot be null or empty");
        }
        if (channelName == null) {
            throw new IllegalArgumentException("Channel name cannot be null");
        }

        // Construct system prompt
        String systemPrompt = "You are summarizing a Discord text channel conversation. Create a single " +
                            "paragraph summary under 75 words that captures the main topics discussed. " +
                            "Focus on what was talked about, not who said what. Be concise and clear.";

        // Construct user prompt
        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Summarize these messages from the #").append(channelName).append(" channel:\n\n");
        
        for (int i = 0; i < messages.size(); i++) {
            userPrompt.append("[Message ").append(i + 1).append("]: ").append(messages.get(i)).append("\n");
        }

        // Create chat messages
        List<ChatMessage> chatMessages = new ArrayList<>();
        chatMessages.add(new ChatMessage("system", systemPrompt));
        chatMessages.add(new ChatMessage("user", userPrompt.toString()));

        // Create and execute request
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .messages(chatMessages)
                .model("gpt-3.5-turbo")
                .build();

        ChatCompletionResult result = aiService.createChatCompletion(request);
        
        // Extract and return summary
        return result.getChoices().get(0).getMessage().getContent();
    }

    /**
     * Creates a ChatCompletionRequest to OpenAI using the {@code gpt-3.5-turbo} model.
     * 
     * @param prompt The prompt from the user.
     * @param member The member who prompted the completion request.
     * @return The result of the request.
     */
    private ChatCompletionResult createCompletionRequest(String prompt, Member member) {
        String id = member.getId().asString();

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are a Discord bot called SOAP Bot."));

        get(id).getCompletions().getTokens().forEach(token -> 
            token.forEach((k,v) -> {
                messages.add(new ChatMessage("user", k));
                messages.add(new ChatMessage("assistant", v));
            })
        );

        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder().messages(messages).model("gpt-3.5-turbo").build();

       return aiService.createChatCompletion(request);
    }

    /**
     * Returns the total amount of coins for all users in this manager,
     * that is, the total amount of coins for users in a Guild.
     * 
     * @return The total amount of coins for all users in this manager.
     */
    public long getTotalCoins() {
        return getAll().stream().mapToLong(profile -> profile.getBank().getBalance()).sum();
    }

    public void updateFromCollectables(CollectableManager manager) {
        manager.getAll().forEach(collectable -> 
            collectable.getCollecteds().forEach(collected -> {
                UserProfile profile = get(collected.getMemberId());
                List<Collected> collecteds = profile.getCollecteds();
                collecteds.removeIf(c -> c.getIdentifier().equals(collected.getIdentifier()));
                collecteds.add(collected);
                update(profile);
            })
        );
    }
}
