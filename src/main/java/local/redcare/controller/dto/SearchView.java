package local.redcare.controller.dto;

import local.redcare.domain.ScoredEntry;
import lombok.Data;

import java.util.List;

@Data
public class SearchView {

    private List<ScoredEntry> entries;

    public static SearchView of(List<ScoredEntry> entries) {
        SearchView view = new SearchView();
        view.setEntries(entries);
        return view;
    }

}
