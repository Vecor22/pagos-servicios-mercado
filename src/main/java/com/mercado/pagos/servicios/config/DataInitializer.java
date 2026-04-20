//package com.mercado.pagos.servicios.config;
//
//import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
//import com.mercado.pagos.servicios.model.entity.Usuario;
//import com.mercado.pagos.servicios.model.enums.TipoCobro;
//import com.mercado.pagos.servicios.repository.ConceptoCobroRepository;
//import com.mercado.pagos.servicios.repository.UsuarioRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class DataInitializer implements CommandLineRunner {
//
//    private final UsuarioRepository usuarioRepository;
//    private final ConceptoCobroRepository conceptoCobroRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    @Override
//    public void run(String... args) {
//        crearAdminSiNoExiste();
//        crearConceptosIniciales();
//    }
//
//    private void crearAdminSiNoExiste() {
//        if (!usuarioRepository.existsByUsername("admin")) {
//            Usuario admin = Usuario.builder()
//                    .username("admin")
//                    .passwordHash(passwordEncoder.encode("Admin123!"))
//                    .nombreCompleto("Administrador General")
//                    .build();
//
//            usuarioRepository.save(admin);
//            log.info("Usuario admin creado correctamente.");
//        }
//    }
//
//    private void crearConceptosIniciales() {
//        crearConceptoSiNoExiste("Mantenimiento", "Cobro general de mantenimiento", TipoCobro.FIJO);
//        crearConceptoSiNoExiste("Agua", "Cobro por consumo de agua", TipoCobro.CONSUMO);
//        crearConceptoSiNoExiste("Luz", "Cobro por consumo de energía eléctrica", TipoCobro.CONSUMO);
//    }
//
//    private void crearConceptoSiNoExiste(String nombre, String descripcion, TipoCobro tipoCobro) {
//        boolean existe = conceptoCobroRepository.findAll()
//                .stream()
//                .anyMatch(c -> c.getNombre().equalsIgnoreCase(nombre));
//
//        if (!existe) {
//            ConceptoCobro concepto = ConceptoCobro.builder()
//                    .nombre(nombre)
//                    .descripcion(descripcion)
//                    .tipoCobro(tipoCobro)
//                    .build();
//
//            conceptoCobroRepository.save(concepto);
//            log.info("Concepto de cobro creado: {}", nombre);
//        }
//    }
//
//}
