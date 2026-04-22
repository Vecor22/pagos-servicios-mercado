package com.mercado.pagos.servicios.config;

import com.mercado.pagos.servicios.model.entity.Comprobante;
import com.mercado.pagos.servicios.model.entity.ConceptoCobro;
import com.mercado.pagos.servicios.model.entity.Deuda;
import com.mercado.pagos.servicios.model.entity.Pago;
import com.mercado.pagos.servicios.model.entity.Puesto;
import com.mercado.pagos.servicios.model.entity.Socio;
import com.mercado.pagos.servicios.model.entity.SocioPuesto;
import com.mercado.pagos.servicios.model.entity.Usuario;
import com.mercado.pagos.servicios.model.enums.EstadoComprobante;
import com.mercado.pagos.servicios.model.enums.EstadoDeuda;
import com.mercado.pagos.servicios.model.enums.EstadoPago;
import com.mercado.pagos.servicios.model.enums.EstadoSocio;
import com.mercado.pagos.servicios.model.enums.TipoCobro;
import com.mercado.pagos.servicios.model.enums.TipoGeneracionDeuda;
import com.mercado.pagos.servicios.repository.ComprobanteRepository;
import com.mercado.pagos.servicios.repository.ConceptoCobroRepository;
import com.mercado.pagos.servicios.repository.DeudaRepository;
import com.mercado.pagos.servicios.repository.PagoRepository;
import com.mercado.pagos.servicios.repository.PuestoRepository;
import com.mercado.pagos.servicios.repository.SocioPuestoRepository;
import com.mercado.pagos.servicios.repository.SocioRepository;
import com.mercado.pagos.servicios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_USERNAME = "admin01";
    private static final String ADMIN_PASSWORD = "Admin01@";
    private static final String ADMIN_NOMBRE_COMPLETO = "Administrador General 01";
    private static final int CANTIDAD_USUARIOS = 10;
    private static final int CANTIDAD_PUESTOS = 10;
    private static final int CANTIDAD_DEUDAS = 10;
    private static final int CANTIDAD_PAGOS = 5;
    private static final int NUMERO_DEUDA_EXONERADA = 10;

    private static final List<ConceptoSeed> CONCEPTOS_INICIALES = List.of(
            new ConceptoSeed("Mantenimiento", "Cobro fijo de mantenimiento", TipoCobro.FIJO),
            new ConceptoSeed("Consumo de servicios", "Cobro por consumo de servicios", TipoCobro.CONSUMO),
            new ConceptoSeed("Limpieza comunal", "Cobro fijo por limpieza de areas comunes", TipoCobro.FIJO),
            new ConceptoSeed("Agua compartida", "Cobro por consumo de agua del puesto", TipoCobro.CONSUMO),
            new ConceptoSeed("Seguridad", "Cobro fijo por seguridad del mercado", TipoCobro.FIJO)
    );

    private static final List<SocioSeed> SOCIOS_INICIALES = List.of(
            new SocioSeed("SOCIO-001", "Ana Maria", "Torres Rojas", "70000001", "999000001", "ana.torres@mercado.test"),
            new SocioSeed("SOCIO-002", "Carlos Alberto", "Mendoza Ruiz", "70000002", "999000002", "carlos.mendoza@mercado.test"),
            new SocioSeed("SOCIO-003", "Lucia Isabel", "Vargas Pena", "70000003", "999000003", "lucia.vargas@mercado.test"),
            new SocioSeed("SOCIO-004", "Miguel Angel", "Castillo Leon", "70000004", "999000004", "miguel.castillo@mercado.test"),
            new SocioSeed("SOCIO-005", "Rosa Elena", "Quispe Flores", "70000005", "999000005", "rosa.quispe@mercado.test")
    );

    private final UsuarioRepository usuarioRepository;
    private final ConceptoCobroRepository conceptoCobroRepository;
    private final SocioRepository socioRepository;
    private final PuestoRepository puestoRepository;
    private final SocioPuestoRepository socioPuestoRepository;
    private final DeudaRepository deudaRepository;
    private final PagoRepository pagoRepository;
    private final ComprobanteRepository comprobanteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Usuario admin = crearAdminSiNoExiste();
        crearUsuariosIniciales();
        List<ConceptoCobro> conceptos = crearConceptosIniciales();

        List<Socio> socios = crearSociosIniciales();
        List<Puesto> puestos = crearPuestosIniciales();
        crearAsignacionesIniciales(socios, puestos);
        List<Deuda> deudas = crearDeudasIniciales(admin, conceptos, socios, puestos);
        marcarDeudaExoneradaInicial(admin, deudas);
        List<Pago> pagos = crearPagosIniciales(admin, obtenerDeudasPagadasIniciales(deudas));
        crearComprobantesIniciales(pagos);
    }

    private Usuario crearAdminSiNoExiste() {
        return usuarioRepository.findByUsername(ADMIN_USERNAME)
                .orElseGet(() -> {
                    Usuario admin = Usuario.builder()
                            .username(ADMIN_USERNAME)
                            .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                            .nombreCompleto(ADMIN_NOMBRE_COMPLETO)
                            .build();

                    Usuario guardado = usuarioRepository.save(admin);
                    log.info("Usuario administrador inicial creado: {}", ADMIN_USERNAME);
                    return guardado;
                });
    }

    private void crearUsuariosIniciales() {
        IntStream.rangeClosed(2, CANTIDAD_USUARIOS)
                .forEach(numero -> obtenerOCrearUsuario(
                        String.format("admin%02d", numero),
                        String.format("Administrador General %02d", numero)
                ));
    }

    private Usuario obtenerOCrearUsuario(String username, String nombreCompleto) {
        return usuarioRepository.findByUsername(username)
                .orElseGet(() -> {
                    Usuario usuario = Usuario.builder()
                            .username(username)
                            .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                            .nombreCompleto(nombreCompleto)
                            .build();

                    return usuarioRepository.save(usuario);
                });
    }

    private List<ConceptoCobro> crearConceptosIniciales() {
        return CONCEPTOS_INICIALES.stream()
                .map(this::obtenerOCrearConcepto)
                .toList();
    }

    private ConceptoCobro obtenerOCrearConcepto(ConceptoSeed datos) {
        return conceptoCobroRepository.findAll().stream()
                .filter(concepto -> concepto.getNombre().equals(datos.nombre()))
                .findFirst()
                .orElseGet(() -> {
                    ConceptoCobro concepto = ConceptoCobro.builder()
                            .nombre(datos.nombre())
                            .descripcion(datos.descripcion())
                            .tipoCobro(datos.tipoCobro())
                            .build();

                    ConceptoCobro guardado = conceptoCobroRepository.save(concepto);
                    log.info("Concepto de cobro inicial creado: {}", datos.nombre());
                    return guardado;
                });
    }

    private List<Socio> crearSociosIniciales() {
        return SOCIOS_INICIALES.stream()
                .map(this::obtenerOCrearSocio)
                .toList();
    }

    private Socio obtenerOCrearSocio(SocioSeed datos) {
        return socioRepository.findAll().stream()
                .filter(socio -> socio.getDni().equals(datos.dni()) || socio.getCodigoSocio().equals(datos.codigo()))
                .findFirst()
                .orElseGet(() -> {
                    Socio socio = Socio.builder()
                            .codigoSocio(datos.codigo())
                            .nombres(datos.nombres())
                            .apellidos(datos.apellidos())
                            .dni(datos.dni())
                            .telefono(datos.telefono())
                            .correo(datos.correo())
                            .estado(EstadoSocio.ACTIVO)
                            .build();

                    return socioRepository.save(socio);
                });
    }

    private List<Puesto> crearPuestosIniciales() {
        return IntStream.rangeClosed(1, CANTIDAD_PUESTOS)
                .mapToObj(numero -> obtenerOCrearPuesto(String.format("PUESTO-%03d", numero)))
                .toList();
    }

    private Puesto obtenerOCrearPuesto(String codigo) {
        return puestoRepository.findAll().stream()
                .filter(puesto -> puesto.getCodigoPuesto().equals(codigo))
                .findFirst()
                .orElseGet(() -> {
                    Puesto puesto = Puesto.builder()
                            .codigoPuesto(codigo)
                            .build();

                    return puestoRepository.save(puesto);
                });
    }

    private void crearAsignacionesIniciales(List<Socio> socios, List<Puesto> puestos) {
        IntStream.range(0, SOCIOS_INICIALES.size()).forEach(index -> {
            Puesto puesto = puestos.get(index);
            if (socioPuestoRepository.existsByPuestoId(puesto.getId())) {
                return;
            }

            SocioPuesto asignacion = SocioPuesto.builder()
                    .socio(socios.get(index))
                    .puesto(puesto)
                    .fechaAsignacion(LocalDate.now().minusDays(5L - index))
                    .build();

            socioPuestoRepository.save(asignacion);
        });
    }

    private List<Deuda> crearDeudasIniciales(
            Usuario admin,
            List<ConceptoCobro> conceptos,
            List<Socio> socios,
            List<Puesto> puestos
    ) {
        return IntStream.rangeClosed(1, CANTIDAD_DEUDAS)
                .mapToObj(numero -> obtenerOCrearDeuda(
                        numero,
                        admin,
                        conceptos.get((numero - 1) % conceptos.size()),
                        socios.get((numero - 1) % socios.size()),
                        puestos.get((numero - 1) % socios.size()),
                        BigDecimal.valueOf(80L + (long) numero * 10L)
                ))
                .toList();
    }

    private Deuda obtenerOCrearDeuda(
            int numero,
            Usuario admin,
            ConceptoCobro concepto,
            Socio socio,
            Puesto puesto,
            BigDecimal monto
    ) {
        String codigo = String.format("DEUDA-%03d", numero);
        return deudaRepository.findAll().stream()
                .filter(deuda -> deuda.getCodigoDeuda().equals(codigo))
                .findFirst()
                .orElseGet(() -> {
                    Deuda deuda = Deuda.builder()
                            .codigoDeuda(codigo)
                            .conceptoCobro(concepto)
                            .puesto(puesto)
                            .socio(socio)
                            .deudaOrigen(null)
                            .monto(monto)
                            .tipoGeneracion(TipoGeneracionDeuda.INDIVIDUAL)
                            .estado(EstadoDeuda.PENDIENTE)
                            .fechaGeneracion(LocalDateTime.now().minusDays(CANTIDAD_DEUDAS - numero))
                            .observacion("Deuda inicial " + codigo)
                            .creadoPor(admin)
                            .build();

                    return deudaRepository.save(deuda);
                });
    }

    private List<Pago> crearPagosIniciales(Usuario admin, List<Deuda> deudas) {
        return IntStream.rangeClosed(1, deudas.size())
                .mapToObj(numero -> obtenerOCrearPago(numero, admin, deudas.get(numero - 1)))
                .toList();
    }

    private Pago obtenerOCrearPago(int numero, Usuario admin, Deuda deuda) {
        String codigo = String.format("PAGO-%03d", numero);
        return pagoRepository.findAll().stream()
                .filter(pago -> pago.getCodigoPago().equals(codigo))
                .findFirst()
                .map(pago -> {
                    if (!EstadoDeuda.PAGADA.equals(deuda.getEstado())) {
                        deuda.setEstado(EstadoDeuda.PAGADA);
                        deuda.setExoneradoPor(null);
                        deuda.setFechaExoneracion(null);
                        deuda.setMotivoExoneracion(null);
                        deudaRepository.save(deuda);
                    }
                    return pago;
                })
                .orElseGet(() -> {
                    deuda.setEstado(EstadoDeuda.PAGADA);
                    deuda.setExoneradoPor(null);
                    deuda.setFechaExoneracion(null);
                    deuda.setMotivoExoneracion(null);
                    deudaRepository.save(deuda);

                    Pago pago = Pago.builder()
                            .codigoPago(codigo)
                            .deuda(deuda)
                            .montoPagado(deuda.getMonto())
                            .medioPago(numero % 2 == 0 ? "TRANSFERENCIA" : "EFECTIVO")
                            .numeroOperacion(numero % 2 == 0 ? String.format("OP-%03d", numero) : null)
                            .fechaPago(LocalDateTime.now().minusDays(CANTIDAD_PAGOS - numero))
                            .estado(EstadoPago.REGISTRADO)
                            .registradoPor(admin)
                            .build();

                    return pagoRepository.save(pago);
                });
    }

    private void crearComprobantesIniciales(List<Pago> pagos) {
        IntStream.rangeClosed(1, pagos.size()).forEach(numero -> {
            Pago pago = pagos.get(numero - 1);
            if (comprobanteRepository.findByPagoId(pago.getId()).isPresent()) {
                return;
            }

            Comprobante comprobante = Comprobante.builder()
                    .pago(pago)
                    .numeroComprobante(String.format("COMP-%03d", numero))
                    .tipoComprobante("RECIBO")
                    .fechaEmision(pago.getFechaPago().plusMinutes(5))
                    .estado(EstadoComprobante.EMITIDO)
                    .build();

            comprobanteRepository.save(comprobante);
        });
    }

    private List<Deuda> obtenerDeudasPagadasIniciales(List<Deuda> deudas) {
        return deudas.stream()
                .limit(CANTIDAD_PAGOS)
                .toList();
    }

    private void marcarDeudaExoneradaInicial(Usuario admin, List<Deuda> deudas) {
        Deuda deuda = deudas.get(NUMERO_DEUDA_EXONERADA - 1);
        boolean tienePagoRegistrado = pagoRepository.findAll().stream()
                .anyMatch(pago -> pago.getDeuda().getId().equals(deuda.getId()));

        if (tienePagoRegistrado) {
            return;
        }

        deuda.setEstado(EstadoDeuda.EXONERADA);
        deuda.setExoneradoPor(admin);
        deuda.setFechaExoneracion(LocalDateTime.now().minusDays(1));
        deuda.setMotivoExoneracion("Exoneracion inicial de prueba para reportes");
        deudaRepository.save(deuda);
    }

    private record ConceptoSeed(
            String nombre,
            String descripcion,
            TipoCobro tipoCobro
    ) {
    }

    private record SocioSeed(
            String codigo,
            String nombres,
            String apellidos,
            String dni,
            String telefono,
            String correo
    ) {
    }

}
