package za.co.entelect.challenge.command;

public class TweetCommand implements Command {

    @Override
    public String render() {
        return String.format("USE_TWEET");
    }
}
