package com.georgster.util.commands;

import java.util.List;

public class ParsedArguments {
    private List<String> args;

    protected ParsedArguments(List<String> args) {
        this.args = args;
    }

    public List<String> getArguments() {
        return args;
    }
}
