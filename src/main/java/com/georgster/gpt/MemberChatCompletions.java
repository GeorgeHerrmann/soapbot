package com.georgster.gpt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.georgster.control.manager.Manageable;

public class MemberChatCompletions implements Manageable {
    private String memberId;

    private List<Map<String, String>> tokens;

    public MemberChatCompletions(List<Map<String, String>> tokens, String memberId) {
        this.memberId = memberId;
        this.tokens = tokens;
    }

    public MemberChatCompletions(String memberId) {
        this.memberId = memberId;
        this.tokens = new ArrayList<>();
    }

    public String getIdentifier() {
        return memberId;
    }

    public List<Map<String, String>> getTokens() {
        return tokens;
    }

    public List<String> getPrompts() {
        List<String> prompts = new ArrayList<>();
        tokens.forEach(set -> set.forEach((k, v) -> prompts.add(k)));
        return prompts;
    }

    public List<String> getResponses() {
        List<String> prompts = new ArrayList<>();
        tokens.forEach(set -> set.forEach((k, v) -> prompts.add(v)));
        return prompts;
    }

    public void addCompletion(String prompt, String response) {
        while (tokens.size() >= 10) {
            tokens.remove(0);
        }
        tokens.add(new HashMap<>());
        tokens.get(tokens.size() - 1).put(prompt, response);
    }
}
