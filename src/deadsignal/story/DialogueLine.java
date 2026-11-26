package deadsignal.story;

/** One immutable line of dialogue. */
public final class DialogueLine {
    private final Speaker speaker;
    private final String text;

    public DialogueLine(Speaker speaker, String text) { this.speaker = speaker; this.text = text; }

    public Speaker getSpeaker() { return speaker; }
    public String getText() { return text.replace("{name}", PlayerName.get()); }
}
