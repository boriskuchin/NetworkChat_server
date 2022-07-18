package components;

public enum Prefix {


    AUTH_CMD_PREFIX("/auth"),
    AUTHOK_CMD_PREFIX("/authok"),
    AUTHERR_CMD_PREFIX("/autherr"),
    CLIENT_MSG_CMD_PREFIX("/cMsg"),
    SERVER_MSG_CMD_PREFIX("/sMsg"),
    PRIVATE_MSG_CMD_PREFIX("/pm"),
    STOP_SERVER_CMD_PREFIX("/stop"),
    END_CLIENT_CMD_PREFIX("/end"),
    LIST_CLIENTS_CMD_PREFIX("/usrs"),
    NEW_USR_CMD_PREFIX("/new"),
    NEW_USR_OK_CMD_PREFIX("/newok"),
    NEW_USR_ERR_CMD_PREFIX("/newerr")
    ;
    private String prefix;

    Prefix(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public static Prefix getPrefixFromText(String str) {
        for (Prefix prefix : Prefix.values()) {
            if (prefix.getPrefix().equals(str)) {
                return prefix;
            }
        }
        return null;
    }
}

