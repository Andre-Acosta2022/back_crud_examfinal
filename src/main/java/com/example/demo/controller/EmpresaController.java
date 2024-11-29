package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.example.demo.dto.RegistroDTO;
import com.example.demo.entity.Empresa;
import com.example.demo.entity.EstadoPPP;
import com.example.demo.entity.RepresentanteLegal;
import com.example.demo.entity.Ubigeo;
import com.example.demo.repository.EmpresaRepository;
import com.example.demo.repository.EstadoPPPRepository;
import com.example.demo.repository.RepresentanteLegalRepository;
import com.example.demo.repository.UbigeoRepository;
import com.example.demo.service.EmpresaService;

@RestController
@RequestMapping("/api/empresa")
@CrossOrigin(origins = "http://localhost:4200")
public class EmpresaController {

	@Autowired
	private EmpresaService service;

	@GetMapping
	public ResponseEntity<List<Empresa>> readAll() {
		List<Empresa> lista = service.readAll();
		return lista.isEmpty() ? new ResponseEntity<>(null, HttpStatus.NO_CONTENT)
				: new ResponseEntity<>(lista, HttpStatus.OK);
	}

	@PostMapping
	public ResponseEntity<Empresa> create(@Valid @RequestBody Empresa obj) {
		return new ResponseEntity<>(service.create(obj), HttpStatus.CREATED);
	}

	@GetMapping("/{id}")
	public ResponseEntity<Empresa> read(@PathVariable Long id) {
		return service.read(id).map(ResponseEntity::ok).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@PutMapping("/{id}")
	public ResponseEntity<Empresa> update(@PathVariable Long id, @Valid @RequestBody Empresa obj) {
		return service.read(id).map(existing -> new ResponseEntity<>(service.update(obj), HttpStatus.OK))
				.orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		service.delete(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
	
	@Autowired
	private EstadoPPPRepository estadoPPPRepository;

	
	@Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private RepresentanteLegalRepository representanteLegalRepository;

    @Autowired
    private UbigeoRepository ubigeoRepository;
    
    @GetMapping("/listar")
    public ResponseEntity<List<RegistroDTO>> listarEmpresas() {
        List<Empresa> empresas = empresaRepository.findAll();
        
        if (empresas.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<RegistroDTO> listaDTO = new ArrayList<>();

        for (Empresa empresa : empresas) {
            RegistroDTO dto = new RegistroDTO(
                empresa.getNombre(), 
                empresa.getRuc(), 
                empresa.getTelefono(), 
                empresa.getCorreo(), 
                empresa.getUbigeo().getDepartamento(), 
                empresa.getUbigeo().getDistrito(), 
                empresa.getUbigeo().getProvincia(), 
                empresa.getDireccion(), 
                empresa.getRepresentanteLegal().getNombre(),
                empresa.getRepresentanteLegal().getCargo(),
                empresa.getRepresentanteLegal().getCorreo(),
                empresa.getRepresentanteLegal().getTelefono()
            );
            listaDTO.add(dto);
        }

        return new ResponseEntity<>(listaDTO, HttpStatus.OK);
    }

    @PostMapping("/registrar")
    public ResponseEntity<Object> registrarEmpresa(@RequestBody RegistroDTO dto) {
        try {
            // Buscar o crear el Ubigeo
            Ubigeo ubigeo = ubigeoRepository.findByDepartamentoAndProvinciaAndDistrito(
                    dto.getDepartamentoUbigeo(),
                    dto.getProvinciaUbigeo(),
                    dto.getDistritoUbigeo()
            );

            if (ubigeo == null) {
                ubigeo = new Ubigeo();
                ubigeo.setDepartamento(dto.getDepartamentoUbigeo());
                ubigeo.setProvincia(dto.getProvinciaUbigeo());
                ubigeo.setDistrito(dto.getDistritoUbigeo());
                ubigeo = ubigeoRepository.save(ubigeo);
            }

            // Crear RepresentanteLegal
            RepresentanteLegal representante = new RepresentanteLegal();
            representante.setNombre(dto.getNombreRepresentante());
            representante.setCargo(dto.getCargoRepresentante());
            representante.setCorreo(dto.getCorreoRepresentante());
            representante.setTelefono(dto.getTelefonoRepresentante());
            representante = representanteLegalRepository.save(representante);

            // Asignar el estado predeterminado con id_estado = 6 ("En Proceso")
            EstadoPPP estadoEnProceso = estadoPPPRepository.findById(6L).orElseThrow(() -> 
                    new RuntimeException("Error: No se encontró el estado con id 6 (En Proceso)"));

            // Crear Empresa
            Empresa empresa = new Empresa();
            empresa.setNombre(dto.getNombreEmpresa());
            empresa.setRuc(dto.getRucEmpresa());
            empresa.setTelefono(dto.getTelefonoEmpresa());
            empresa.setCorreo(dto.getCorreoEmpresa());
            empresa.setDireccion(dto.getDireccionEmpresa());
            empresa.setUbigeo(ubigeo);
            empresa.setRepresentanteLegal(representante);
            empresa.setEstado_ppp(estadoEnProceso); // Asignar el estado "En Proceso"

            empresaRepository.save(empresa);

            // Devuelve un objeto JSON con un mensaje de éxito
            return ResponseEntity.ok(new ResponseMessage("Empresa registrada exitosamente con estado 'En Proceso'"));
        } catch (Exception e) {
            // Devuelve un objeto JSON con un mensaje de error
            return ResponseEntity.status(500).body(new ResponseMessage("Error al registrar la empresa: " + e.getMessage()));
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<Empresa> actualizarEmpresa(@PathVariable Long id, @RequestBody RegistroDTO registroDTO) {
        // Buscar la empresa
        Empresa empresa = empresaRepository.findById(id).orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        // Actualizar la empresa con los nuevos datos del DTO
        empresa.setNombre(registroDTO.getNombreEmpresa());
        empresa.setRuc(registroDTO.getRucEmpresa());
        empresa.setTelefono(registroDTO.getTelefonoEmpresa());
        empresa.setCorreo(registroDTO.getCorreoEmpresa());
        empresa.setDireccion(registroDTO.getDireccionEmpresa());

        // Actualizar el representante legal
        RepresentanteLegal representante = empresa.getRepresentanteLegal();
        representante.setNombre(registroDTO.getNombreRepresentante());
        representante.setTelefono(registroDTO.getTelefonoRepresentante());
        representante.setCorreo(registroDTO.getCorreoRepresentante());
        representante.setCargo(registroDTO.getCargoRepresentante());
        
        // Verificar si el ubigeo ya existe
        Ubigeo ubigeo = ubigeoRepository.findByDepartamentoAndProvinciaAndDistrito(
                registroDTO.getDepartamentoUbigeo(),
                registroDTO.getProvinciaUbigeo(),
                registroDTO.getDistritoUbigeo()
        );
        
        if (ubigeo == null) {
            // Si no existe, crear un nuevo ubigeo
            ubigeo = new Ubigeo();
            ubigeo.setDepartamento(registroDTO.getDepartamentoUbigeo());
            ubigeo.setProvincia(registroDTO.getProvinciaUbigeo());
            ubigeo.setDistrito(registroDTO.getDistritoUbigeo());
            ubigeoRepository.save(ubigeo);  // Guarda el nuevo ubigeo
        }

        // Asociar el ubigeo actualizado con la empresa
        empresa.setUbigeo(ubigeo);

        // Guardar la empresa con los cambios
        Empresa empresaActualizada = empresaRepository.save(empresa);

        return ResponseEntity.ok(empresaActualizada);
    }
    
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<Void> eliminarEmpresa(@PathVariable Long id) {
        Optional<Empresa> empresa = empresaRepository.findById(id);
        if (empresa.isPresent()) {
            empresaRepository.deleteById(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Clase auxiliar para la respuesta
    public class ResponseMessage {
        private String message;

        public ResponseMessage(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}