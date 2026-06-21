package local.redcare.domain.github;

import local.redcare.domain.SearchRequest;

public final class GitHubQuery {

    public static final int MAX_LENGTH = 255;

    private GitHubQuery() {
        //
    }

    public static String of(SearchRequest request) {
        StringBuilder sb = new StringBuilder(request.q());
        if (request.lang() != null)  {
            sb.append(" lang:").append(request.lang());
        }
        if (request.since() != null) {
            sb.append(" created:>").append(request.since());
        }
        return sb.toString();
    }

}
