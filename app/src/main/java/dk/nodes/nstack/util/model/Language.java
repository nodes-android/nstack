package dk.nodes.nstack.util.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Mario on 28/12/16.
 */
public class Language implements Serializable {

    private int id;
    private String name, locale, direction;
    private boolean picked;

    public Language() {

    }

    public Language(int id, String name, String locale, String direction) {
        this.id = id;
        this.name = name;
        this.locale = locale;
        this.direction = direction;
    }

    public static Language parseFrom(JSONObject object) {
        int id = object.optInt("id");
        String name = object.optString("name");
        String locale = object.optString("locale");
        String direction = object.optString("direction");

        return new Language(id, name, locale, direction);
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }
}
