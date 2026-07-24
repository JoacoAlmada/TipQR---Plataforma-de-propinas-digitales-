import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { DashboardService } from '../../core/services/dashboard.service';
import { ReporteAutomatico } from '../../core/models/dashboard.model';

@Component({
  selector: 'app-reportes-ia',
  standalone: true,
  imports: [],
  templateUrl: './reportes-ia.component.html'
})
export class ReportesIaComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);
  private readonly sanitizer = inject(DomSanitizer);

  reportes = signal<ReporteAutomatico[]>([]);
  loading = signal(true);
  generando = signal(false);
  msg = signal('');

  // Búsqueda, filtro por fecha y orden
  busqueda = signal('');
  desde = signal('');
  hasta = signal('');
  orden = signal<'desc' | 'asc'>('desc');

  reportesFiltrados = computed(() => {
    const q = this.busqueda().trim().toLowerCase();
    const d = this.desde() ? new Date(this.desde() + 'T00:00:00').getTime() : null;
    const h = this.hasta() ? new Date(this.hasta() + 'T23:59:59').getTime() : null;
    const asc = this.orden() === 'asc';
    return this.reportes()
      .filter(r => {
        if (q && !(`${r.titulo} ${r.contenido}`.toLowerCase().includes(q))) return false;
        const t = r.fechaIso ? new Date(r.fechaIso).getTime() : 0;
        if (d != null && t < d) return false;
        if (h != null && t > h) return false;
        return true;
      })
      .sort((a, b) => {
        const ta = a.fechaIso ? new Date(a.fechaIso).getTime() : 0;
        const tb = b.fechaIso ? new Date(b.fechaIso).getTime() : 0;
        return asc ? ta - tb : tb - ta;
      });
  });

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.loading.set(true);
    this.dashboardService.reportesIa().subscribe({
      next: (r) => { this.reportes.set(r); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }

  generar(): void {
    this.msg.set('');
    this.generando.set(true);
    this.dashboardService.generarReporteIa().subscribe({
      next: (r) => {
        this.generando.set(false);
        this.reportes.update(list => [r, ...list]);
        this.msg.set(r.generadoPorIa
          ? 'Resumen generado con IA.'
          : 'Resumen generado (redacción local — configurá GEMINI_API_KEY para usar IA).');
      },
      error: (err) => {
        this.generando.set(false);
        this.msg.set(err?.error?.error ?? 'No se pudo generar el reporte.');
      }
    });
  }

  eliminar(r: ReporteAutomatico): void {
    this.dashboardService.eliminarReporteIa(r.id).subscribe({
      next: () => this.reportes.update(list => list.filter(x => x.id !== r.id))
    });
  }

  // ── Render del contenido (Markdown → HTML seguro) ──

  /** Renderiza el contenido Markdown del reporte como HTML sanitizado para mostrarlo con formato. */
  render(md: string): SafeHtml {
    return this.sanitizer.bypassSecurityTrustHtml(this.mdToHtml(md));
  }

  /**
   * Conversor de Markdown a HTML acotado (encabezados, negrita, listas y párrafos).
   * Escapa el texto de origen antes de inyectar tags propios, así no hay riesgo de HTML injection.
   * Usa estilos inline para verse igual dentro de la app y en la ventana de exportación a PDF.
   */
  private mdToHtml(md: string): string {
    const esc = (s: string) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const inline = (s: string) => esc(s).replace(/\*\*(.+?)\*\*/g, '<strong style="font-weight:600">$1</strong>');
    const lines = (md || '').replace(/\r\n/g, '\n').split('\n');
    let html = '';
    let list: 'ul' | 'ol' | null = null;
    const closeList = () => { if (list) { html += `</${list}>`; list = null; } };

    for (const raw of lines) {
      const line = raw.trim();
      if (!line) { closeList(); continue; }

      let m: RegExpMatchArray | null;
      if ((m = line.match(/^(#{1,6})\s+(.*)$/))) {
        closeList();
        html += `<h3 style="font-weight:600;font-size:1rem;margin:1rem 0 .35rem;color:#420001">${inline(m[2])}</h3>`;
      } else if ((m = line.match(/^[-*]\s+(.*)$/))) {
        if (list !== 'ul') { closeList(); html += '<ul style="margin:.4rem 0;padding-left:1.25rem;list-style:disc">'; list = 'ul'; }
        html += `<li style="margin:.2rem 0;line-height:1.55">${inline(m[1])}</li>`;
      } else if ((m = line.match(/^\d+[.)]\s+(.*)$/))) {
        if (list !== 'ol') { closeList(); html += '<ol style="margin:.4rem 0;padding-left:1.35rem;list-style:decimal">'; list = 'ol'; }
        html += `<li style="margin:.2rem 0;line-height:1.55">${inline(m[1])}</li>`;
      } else {
        closeList();
        html += `<p style="margin:.5rem 0;line-height:1.6">${inline(line)}</p>`;
      }
    }
    closeList();
    return html;
  }

  // ── Exportar a PDF ──

  private money(n: number | null): string {
    return n == null ? '—' : '$' + Number(n).toLocaleString('es-AR', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  }

  /** Abre el reporte maquetado en una ventana e invoca la impresión para guardarlo como PDF. */
  exportarPdf(r: ReporteAutomatico): void {
    const w = window.open('', '_blank', 'width=820,height=1000');
    if (!w) { this.msg.set('Permití las ventanas emergentes para exportar el PDF.'); return; }

    const cuerpo = this.mdToHtml(r.contenido);
    const origen = r.generadoPorIa ? ' · IA' : ' · Local';
    w.document.write(`<!doctype html><html lang="es"><head><meta charset="utf-8">
      <title>${r.titulo}</title>
      <style>
        *{box-sizing:border-box}
        body{font-family:-apple-system,Segoe UI,Roboto,Helvetica,Arial,sans-serif;color:#1f1f1f;margin:0;padding:34px 42px;font-size:14px}
        .head{border-bottom:2px solid #420001;padding-bottom:12px;margin-bottom:20px}
        .brand{color:#420001;font-weight:700;letter-spacing:.6px;font-size:11px;text-transform:uppercase}
        h1{font-size:20px;margin:6px 0 2px}
        .fecha{color:#777;font-size:12px}
        .cards{display:flex;gap:12px;margin:18px 0 6px}
        .card{flex:1;border:1px solid #e6e3e0;border-radius:10px;padding:10px 14px}
        .card .lbl{font-size:10px;text-transform:uppercase;letter-spacing:.5px;color:#999}
        .card .val{font-size:18px;font-weight:700;margin-top:2px;color:#420001}
        .foot{margin-top:30px;border-top:1px solid #eee;padding-top:10px;color:#aaa;font-size:11px;text-align:center}
        @media print{ body{padding:0} @page{margin:16mm} }
      </style></head><body>
      <div class="head">
        <div class="brand">TipQR · Reporte de propinas${origen}</div>
        <h1>${r.titulo}</h1>
        <div class="fecha">${r.fecha ?? ''}</div>
      </div>
      <div class="cards">
        <div class="card"><div class="lbl">Total recaudado</div><div class="val">${this.money(r.totalRecaudado)}</div></div>
        <div class="card"><div class="lbl">Propinas</div><div class="val">${r.cantidadPropinas ?? '—'}</div></div>
        <div class="card"><div class="lbl">Ticket promedio</div><div class="val">${this.money(r.ticketPromedio)}</div></div>
      </div>
      <div>${cuerpo}</div>
      <div class="foot">Generado por TipQR — ${new Date().toLocaleString('es-AR')}</div>
      </body></html>`);
    w.document.close();
    w.focus();
    setTimeout(() => w.print(), 350);
  }
}
