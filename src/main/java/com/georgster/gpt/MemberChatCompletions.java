package com.georgster.gpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.georgster.control.manager.Manageable;

/**
 * A model for a member's conversations with SOAP Bot's GPT AI.
 * A {@code MemberChatCompletions} will only keep record of the previous
 * ten prompts and responses between a member and the AI.
 * <p>
 * The member's prompts are keys which are mapped to the AI's responses as values.
 * <p>
 * This {@link Manageable}'s identifier is the member's Snowflake ID.
 */
public class MemberChatCompletions implements Manageable {
    private String memberId;

    private List<Map<String, String>> tokens; // A list of the user's prompts and the AI's response

    /**
     * Constructs a {@code MemberChatCompletions} with the given tokens and the member's id.
     * Generally used when pulling from the database.
     * 
     * @param tokens The tokens between the user and the AI.
     * @param memberId The member's snowflake id.
     */
    public MemberChatCompletions(List<Map<String, String>> tokens, String memberId) {
        this.memberId = memberId;
        this.tokens = tokens;
    }

    /**
     * Creates an empty {@code MemberChatCompletions} with the given tokens and the member's id.
     * 
     * @param memberId The member's snowflake id.
     */
    public MemberChatCompletions(String memberId) {
        this.memberId = memberId;
        this.tokens = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    public String getIdentifier() {
        return memberId;
    }

    /**
     * Returns all tokens between the member and the AI in this model.
     * 
     * @return The tokens between the member and the AI.
     */
    public List<Map<String, String>> getTokens() {
        return tokens;
    }

    /**
     * Returns all prompts given to the AI by the user in this model.
     * 
     * @return The prompts from the user.
     */
    public List<String> getPrompts() {
        List<String> prompts = new ArrayList<>();
        tokens.forEach(set -> set.forEach((k, v) -> prompts.add(k)));
        return prompts;
    }

    /**
     * Returns all responses given by the AI to the user in this model.
     * 
     * @return The responses from the AI.
     */
    public List<String> getResponses() {
        List<String> prompts = new ArrayList<>();
        tokens.forEach(set -> set.forEach((k, v) -> prompts.add(v)));
        return prompts;
    }

    /**
     * Adds a new chat completion to this {@code MemberChatCompletions}.
     * <p>
     * A {@code MemberChatCompletions} can only hold ten completions at a time, and will
     * discard the oldest completion if that capacity has been reached in a queue-like fashion.
     * <p>
     * A new completion should only be added once OpenAI's API has responded with a
     * {@code response} from a {@code prompt}. This model <b>does not</b> send a completion
     * request to the API, as that is done via
     * {@link com.georgster.control.manager.UserProfileManager#createCompletion(String, discord4j.core.object.entity.Member) UserProfileManager.createCompletion(Sting, Member)}.
     * 
     * @param prompt The prompt from the member.
     * @param response The response from the AI.
     */
    public void addCompletion(String prompt, String response) {
        while (tokens.size() >= 10) {
            tokens.remove(0);
        }
        tokens.add(new HashMap<>());
        tokens.get(tokens.size() - 1).put(prompt, response);
    }
}
