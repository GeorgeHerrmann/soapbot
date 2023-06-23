package com.georgster.control.manager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.georgster.control.util.ClientContext;
import com.georgster.database.ProfileType;
import com.georgster.gpt.MemberChatCompletions;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import discord4j.core.object.entity.Member;

/**
 * A {@link SoapManager} which manages {@link MemberChatCompletions} requests from OpenAI's API.
 * <p>
 * Although this manager functions like any {@link SoapManager}, allowing the use of {@link #add(MemberChatCompletions)},
 * this manager can mostly handle member management itself, allowing Chat Completions through the use of
 * the more simple {@link #createCompletion(String, Member)}.
 */
public class ChatCompletionManager extends SoapManager<MemberChatCompletions> {
    private static OpenAiService aiService; //The singleton AI Service to communicate with OpenAI's API

    /**
     * Creates a new ChatCompletionManager with the given ClientContext.
     * 
     * @param context The context of the SoapClient for this manager.
     */
    public ChatCompletionManager(ClientContext context) {
        super(context, ProfileType.COMPLETIONS, MemberChatCompletions.class, "memberId");
        createService();
    }

    /**
     * Creates the singleton OpenAiService.
     */
    private static void createService() {
        try {
            aiService = new OpenAiService(Files.readString(Path.of(System.getProperty("user.dir"),"src", "main", "java", "com", "georgster", "gpt", "openaikey.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads all previous chat completions into this manager.
     */
    public void load() {
        dbService.getAllObjects().forEach(completion -> observees.add(completion));
    }

    /**
     * Creates a new ChatCompletion for the provided {@code Member} based on the given
     * prompt and returns the first response.
     * The response will be based on the previous ten chat completions for that member
     * in this manager. Uses OpenAI's gpt-3.5-turbo model.
     * 
     * @param prompt The prompt from the user.
     * @param member The member who prompted the completion request.
     * @return The response from the AI.
     */
    public String createCompletion(String prompt, Member member) {
        String id = member.getId().asString();

        add(new MemberChatCompletions(id));

        MemberChatCompletions completions = get(id);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are a Discord bot called SOAP Bot."));

        completions.getTokens().forEach(token -> 
            token.forEach((k,v) -> {
                messages.add(new ChatMessage("user", k));
                messages.add(new ChatMessage("assistant", v));
            })
        );

        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder().messages(messages).model("gpt-3.5-turbo").build();

        String response = aiService.createChatCompletion(request).getChoices().get(0).getMessage().getContent();
        completions.addCompletion(prompt, response);

        update(completions);

        return response;
        
    }

    /**
     * Creates a new ChatCompletion for the provided {@code Member} based on the given
     * prompt and returns all responses.
     * The response will be based on the previous ten chat completions for that member
     * in this manager. Uses OpenAI's gpt-3.5-turbo model.
     * <p>
     * <b>Note:</b> Only the first response will be saved in the member's chat completion log.
     * 
     * @param prompt The prompt from the user.
     * @param member The member who prompted the completion request.
     * @return All responses from the AI.
     */
    public List<String> createCompletionGetAll(String prompt, Member member) {
        String id = member.getId().asString();

        add(new MemberChatCompletions(id));

        MemberChatCompletions completions = get(id);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new ChatMessage("system", "You are a Discord bot called SOAP Bot who was created by georgster." +
                                                    "You have many features, but if anyone is confused on how to use the bot, tell them to use !help"));

        completions.getTokens().forEach(token -> 
            token.forEach((k,v) -> {
                messages.add(new ChatMessage("user", k));
                messages.add(new ChatMessage("assistant", v));
            })
        );

        messages.add(new ChatMessage("user", prompt));

        ChatCompletionRequest request = ChatCompletionRequest.builder().messages(messages).model("gpt-3.5-turbo").build();

        List<String> responses = new ArrayList<>();

        aiService.createChatCompletion(request).getChoices().forEach(choice -> responses.add(choice.getMessage().getContent()));
        completions.addCompletion(prompt, responses.get(0));

        update(completions);

        return responses;
    }
}
