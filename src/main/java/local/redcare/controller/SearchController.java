package local.redcare.controller;

import jakarta.validation.Valid;
import local.redcare.controller.dto.SearchForm;
import local.redcare.controller.dto.SearchView;
import local.redcare.service.DirectSearchHandler;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/repos")
@AllArgsConstructor
public class SearchController {

    private final DirectSearchHandler handler;

    @GetMapping("/search")
    public ResponseEntity<SearchView> search(@Valid SearchForm form) {
        SearchView view = SearchView.of(handler.invoke(form.toRequest()));
        return ResponseEntity.ok(view);
    }

}
