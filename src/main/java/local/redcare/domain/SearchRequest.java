package local.redcare.domain;

import java.time.LocalDate;


public record SearchRequest(

        String q,
        LocalDate since,
        String lang

) implements HasKey {

    @Override
    public String getKey() {
        StringBuilder sb = new StringBuilder();
        sb.append(q);
        sb.append(":");
        if (since != null) {
            sb.append(since);
        }
        sb.append(":");
        if (lang != null) {
            sb.append(lang);
        }

        return sb.toString();
    }

}
