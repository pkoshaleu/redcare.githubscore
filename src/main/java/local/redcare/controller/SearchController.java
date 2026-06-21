package local.redcare.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import local.redcare.controller.dto.SearchForm;
import local.redcare.controller.dto.SearchView;
import local.redcare.service.SearchHandler;


@RestController
@RequestMapping("/api/repos")
@AllArgsConstructor
public class SearchController {

    private final SearchHandler handler;

    @GetMapping("/search")
    public ResponseEntity<SearchView> search(@Valid SearchForm form) {
        SearchView view = SearchView.of(handler.invoke(form.toRequest()), form.getPage(), form.getLimit());
        return ResponseEntity.ok(view);
    }

}
