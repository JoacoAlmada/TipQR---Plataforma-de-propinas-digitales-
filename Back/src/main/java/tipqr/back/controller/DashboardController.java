package tipqr.back.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tipqr.back.dto.HistorialPropinasResponse;
import tipqr.back.dto.RankingEmpleadoResponse;
import tipqr.back.dto.ReportePeriodoResponse;
import tipqr.back.dto.ResumenDuenoResponse;
import tipqr.back.service.DashboardService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** Historial de propinas del empleado logueado. */
    @GetMapping("/empleado/propinas")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ENCARGADO')")
    public ResponseEntity<HistorialPropinasResponse> historialEmpleado(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(dashboardService.historialEmpleado(user.getUsername()));
    }

    /** Resumen de propinas de la empresa (dashboard del dueño). */
    @GetMapping("/dashboard/resumen")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ResumenDuenoResponse> resumenDueno(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(dashboardService.resumenDueno(user.getUsername()));
    }

    /** Ranking de empleados por propinas recibidas. */
    @GetMapping("/dashboard/ranking")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<List<RankingEmpleadoResponse>> ranking(
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(dashboardService.ranking(user.getUsername()));
    }

    /** Reporte de propinas pagadas en un rango de fechas (yyyy-MM-dd). */
    @GetMapping("/dashboard/reporte")
    @PreAuthorize("hasRole('DUENO')")
    public ResponseEntity<ReportePeriodoResponse> reporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(dashboardService.reportePeriodo(user.getUsername(), desde, hasta));
    }
}
