package com.example.demo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.example.demo.entity.Persona;
import com.example.demo.service.PersonaService;

@RestController
@RequestMapping("/api/persona")
@CrossOrigin(origins = "http://localhost:4200")
public class PersonaController {

	@Autowired
	private PersonaService service;

	@GetMapping
	public ResponseEntity<List<Persona>> readAll() {
		List<Persona> lista = service.readAll();
		return lista.isEmpty() ? new ResponseEntity<>(null, HttpStatus.NO_CONTENT)
				: new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@PostMapping("/save")
	public ResponseEntity<Persona> create(@Valid @RequestBody Persona obj) {
		return new ResponseEntity<>(service.create(obj), HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Persona> read(@PathVariable Long id) {
		return service.read(id).map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/update/{id}")
	public ResponseEntity<Persona> update(@PathVariable Long id, @Valid @RequestBody Persona obj) {
		return service.read(id).map(existing -> new ResponseEntity<>(service.update(obj), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}