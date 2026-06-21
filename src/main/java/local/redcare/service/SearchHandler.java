package local.redcare.service;

import local.redcare.domain.ScoredEntry;
import local.redcare.domain.SearchRequest;

import java.util.List;


public interface SearchHandler {

    List<ScoredEntry> invoke(SearchRequest request);

}
