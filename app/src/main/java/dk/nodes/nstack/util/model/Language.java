package dk.nodes.nstack.util.model;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by tommyjepsen on 20/10/15.
 * 
 */
public class Language implements Serializable {

    public int id;
    public String name, locale, direction;
    public boolean picked;

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

    public static Language parseFrom(JSONObject object) {
        Language l = new Language();

        l.setId(object.optInt("id"));
        l.setName(object.optString("name"));
        l.setLocale(object.optString("locale"));
        l.setDirection(object.optString("direction"));
        l.setPicked(false);

        return l;
    }
}
