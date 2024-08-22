package de.fraunhofer.iosb.ilt.ams.utility;

public final class MESSAGE {
    private MESSAGE() throws Exception {
        throw new Exception("Private constructor");
    }

    public static final String LANGUAGE_ERROR = "Field %s also needs a language code.";
    public static final String ENTITY_NOT_FOUND =
            "Entity with id: %s not found."
                    + System.lineSeparator()
                    + "Possible reasons: "
                    + System.lineSeparator()
                    + "1. Entity with this id does not exist."
                    + System.lineSeparator()
                    + "2. Entity with this id is not of the claimed type.";
    public static final String DELETED = "Deleted %s";
    public static final String NOT_ENOUGH_ARGUMENTS =
            "The entity %s should either have a semantic reference "
                    + "or a combination of label and description";

    public static final String NOT_ENOUGH_ARGUMENTS_GENERIC =
            "This entity should either have a semantic reference "
                    + "or a combination of label and description";
    public static final String SUCCESS = "Success";

    public static final String ENTITY_NOT_CONTAINED = "Entity %s does not contain %s";

    public static final int LOOP_DETECTED = 205;

    public static final String LOOP_STRING = "Query %s";

    public static final String LOG_MESSAGE = "User from group %s called method %s.";
}
