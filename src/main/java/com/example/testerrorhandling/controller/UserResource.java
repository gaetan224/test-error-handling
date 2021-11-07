package com.example.testerrorhandling.controller;

import com.example.testerrorhandling.model.Utilisateur;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.URISyntaxException;

/**
 * REST controller for managing
 */
@RestController
@RequestMapping("/api")
@Validated
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    /**
     * {@code POST  /photos} : Create a new photo.
     *
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new photo, or with status {@code 400 (Bad Request)} if the photo has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/users")
    public ResponseEntity<Utilisateur> createPhoto(@Valid @RequestBody Utilisateur utilisateur) throws URISyntaxException {
        log.debug("REST request to save utilisateur : {}", utilisateur);
       return ResponseEntity.ok(utilisateur);
    }

    /**
     * {@code GET  /photos/:id} : get the "id" photo.
     *
     * @param id the id of the photo to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the photo, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users")
    public ResponseEntity<Utilisateur> getPhoto(@RequestParam(required = false) // to avoid throwing MissingServletRequestParameterException
                                                                                // insteadof MissingServletRequestParameterException
                                                    @NotNull @Min(3) String id) {
        log.debug("REST request to get Photo : {}", id);
        return ResponseEntity.ok(new Utilisateur());
    }
}
