package app.model;

import app.engine.command.Command;

import java.util.ArrayList;

public record Specification(
        ArrayList<Command> commands,
        Intersection intersection
){}
