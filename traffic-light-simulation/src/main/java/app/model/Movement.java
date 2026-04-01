package app.model;

public record Movement(Direction from, Direction to) {
    public MovementType getType() {
        if (to == from.getLeft()) return MovementType.LEFT;
        if (to == from.getRight()) return MovementType.RIGHT;
        return MovementType.STRAIGHT;
    }
}
