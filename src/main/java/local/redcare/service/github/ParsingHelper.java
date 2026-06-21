package local.redcare.service.github;

public final class ParsingHelper {

    private ParsingHelper() {
        //
    }

    public static Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
