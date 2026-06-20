package local.redcare.controller;

import jakarta.validation.Valid;
import local.redcare.controller.dto.SearchForm;
import local.redcare.controller.dto.SearchView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/repos")
public class SearchController {

    @GetMapping("/search")
    public ResponseEntity<SearchView> search(@Valid SearchForm form) {

        return ResponseEntity.ok().build();
    }


}
