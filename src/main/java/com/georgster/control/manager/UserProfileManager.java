package com.georgster.control.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.economy.CoinBank;
import com.georgster.gpt.MemberChatCompletions;
import com.georgster.profile.UserProfile;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.object.entity.Member;

/**
 * Manages all {@link UserProfile UserProfiles} for a {@link com.georgster.control.SoapClient SoapClient}.
 */
public class UserProfileManager extends SoapManager<UserProfile> {

    private static OpenAiService aiService; //The singleton AI Service to communicate with OpenAI's API
    
    /**
     * Creates a new UserProfileManager for the given SoapClient's {@link ClientContext}.
     * 
     * @param context The context of the SoapClient for this manager.
     */
    public UserProfileManager(ClientContext context) {
        super(context, ProfileType.PROFILES, UserProfile.class, "memberId");
        createAiService();
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
                update(new UserProfile(event.getGuild().getId().asString(), id, member.getTag(), completions, bank));
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
}
