package dk.nodes.nstack.util.appopen.message;

/**
 * Created by Mario on 29/12/2016.
 */

public class Message {

    private int id;
    private int projectId;
    private String platform;
    private String showSetting;
    private int viewCount;
    private String message;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getShowSetting() {
        return showSetting;
    }

    public void setShowSetting(String showSetting) {
        this.showSetting = showSetting;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
