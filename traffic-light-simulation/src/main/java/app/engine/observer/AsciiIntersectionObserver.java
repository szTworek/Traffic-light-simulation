package app.engine.observer;

public class AsciiIntersectionObserver implements IntersectionObserver {
    private final AsciiIntersectionRenderer renderer = new AsciiIntersectionRenderer();

    @Override
    public void onIntersectionChanged(IntersectionSnapshot snapshot) {
        System.out.println(renderer.render(snapshot));
    }
}
