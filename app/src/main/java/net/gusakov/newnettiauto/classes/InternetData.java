package net.gusakov.newnettiauto.classes;

/**
 * Created by hasana on 4/4/2017.
 */

public class InternetData {
    private String URL;
    private String HTML;

    public InternetData(String HTML, String URL) {
        this.URL = URL;
        this.HTML = HTML;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public String getHTML() {
        return HTML;
    }

    public void setHTML(String HTML) {
        this.HTML = HTML;
    }
}
