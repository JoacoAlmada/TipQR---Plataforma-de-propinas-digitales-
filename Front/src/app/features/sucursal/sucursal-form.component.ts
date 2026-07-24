import {
  Component, OnInit, AfterViewInit, OnDestroy, ElementRef,
  PLATFORM_ID, inject, signal, viewChild, NgZone
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SucursalService } from '../../core/services/sucursal.service';
import { SucursalRequest } from '../../core/models/sucursal.model';

interface Sugerencia { nombre: string; lat: number; lon: number; }

@Component({
  selector: 'app-sucursal-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './sucursal-form.component.html'
})
export class SucursalFormComponent implements OnInit, AfterViewInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly sucursalService = inject(SucursalService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly zone = inject(NgZone);
  private readonly platformId = inject(PLATFORM_ID);

  readonly mapaRef = viewChild<ElementRef<HTMLDivElement>>('mapa');

  private sucursalId: number | null = null;

  // Córdoba capital como centro por defecto
  private readonly CENTRO = { lat: -31.4201, lng: -64.1888 };
  private L: any;
  private map: any;
  private marker: any;
  private debounce: any;

  esEdicion = signal(false);
  loading = signal(false);
  errorMsg = signal('');

  latitud = signal<number | null>(null);
  longitud = signal<number | null>(null);
  sugerencias = signal<Sugerencia[]>([]);
  buscando = signal(false);

  form = this.fb.group({
    nombre:    ['', [Validators.required, Validators.maxLength(120)]],
    direccion: ['', Validators.maxLength(160)],
    telefono:  ['', Validators.maxLength(30)]
  });

  get f() { return this.form.controls; }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.sucursalId = Number(idParam);
      this.esEdicion.set(true);
      this.loading.set(true);
      this.sucursalService.obtener(this.sucursalId).subscribe({
        next: (s) => {
          this.form.patchValue({ nombre: s.nombre, direccion: s.direccion ?? '', telefono: s.telefono ?? '' });
          this.latitud.set(s.latitud ?? null);
          this.longitud.set(s.longitud ?? null);
          this.loading.set(false);
          this.centrarEnActual();
        },
        error: () => { this.errorMsg.set('No se pudo cargar la sucursal'); this.loading.set(false); }
      });
    }
  }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.cargarLeaflet().then(() => this.initMapa());
  }

  ngOnDestroy(): void {
    clearTimeout(this.debounce);
    if (this.map) this.map.remove();
  }

  // ── Mapa (Leaflet vía CDN) ──────────────────────────────

  private cargarLeaflet(): Promise<void> {
    return new Promise((resolve) => {
      if ((window as any).L) { resolve(); return; }
      if (!document.getElementById('leaflet-css')) {
        const link = document.createElement('link');
        link.id = 'leaflet-css';
        link.rel = 'stylesheet';
        link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
        document.head.appendChild(link);
      }
      const espera = () => (window as any).L ? resolve() : setTimeout(espera, 100);
      if (document.getElementById('leaflet-js')) { espera(); return; }
      const s = document.createElement('script');
      s.id = 'leaflet-js';
      s.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
      s.onload = () => resolve();
      document.head.appendChild(s);
    });
  }

  private initMapa(): void {
    const L = (window as any).L;
    if (!L) return;
    this.L = L;
    const el = this.mapaRef()?.nativeElement;
    if (!el) return;

    const lat = this.latitud() ?? this.CENTRO.lat;
    const lng = this.longitud() ?? this.CENTRO.lng;
    this.map = L.map(el).setView([lat, lng], this.latitud() != null ? 16 : 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '© OpenStreetMap', maxZoom: 19
    }).addTo(this.map);

    if (this.latitud() != null) this.ponerMarcador(lat, lng);

    this.map.on('click', (e: any) =>
      this.zone.run(() => this.fijarUbicacion(e.latlng.lat, e.latlng.lng, true)));

    setTimeout(() => this.map.invalidateSize(), 200);
  }

  private icono() {
    return this.L.icon({
      iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
      iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
      shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
      iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34], shadowSize: [41, 41]
    });
  }

  private ponerMarcador(lat: number, lng: number): void {
    if (this.marker) {
      this.marker.setLatLng([lat, lng]);
    } else {
      this.marker = this.L.marker([lat, lng], { icon: this.icono(), draggable: true }).addTo(this.map);
      this.marker.on('dragend', () => {
        const p = this.marker.getLatLng();
        this.zone.run(() => this.fijarUbicacion(p.lat, p.lng, true));
      });
    }
  }

  private centrarEnActual(): void {
    if (this.map && this.latitud() != null && this.longitud() != null) {
      this.map.setView([this.latitud()!, this.longitud()!], 16);
      this.ponerMarcador(this.latitud()!, this.longitud()!);
    }
  }

  // ── Buscador de dirección (Nominatim / OpenStreetMap) ──

  onBuscarInput(): void {
    // Al editar el texto a mano se invalidan las coordenadas hasta elegir una opción o el mapa.
    this.latitud.set(null);
    this.longitud.set(null);
    clearTimeout(this.debounce);
    const q = (this.form.controls.direccion.value ?? '').trim();
    if (q.length < 3) { this.sugerencias.set([]); return; }
    this.debounce = setTimeout(() => this.buscar(q), 450);
  }

  private async buscar(q: string): Promise<void> {
    this.zone.run(() => this.buscando.set(true));
    try {
      const url = 'https://nominatim.openstreetmap.org/search?format=jsonv2'
        + '&q=' + encodeURIComponent(q)
        + '&countrycodes=ar&addressdetails=1&limit=6&accept-language=es';
      const res = await fetch(url);
      const data = await res.json();
      const sug: Sugerencia[] = (data ?? []).map((d: any) => ({
        nombre: d.display_name, lat: +d.lat, lon: +d.lon
      }));
      this.zone.run(() => this.sugerencias.set(sug));
    } catch {
      this.zone.run(() => this.sugerencias.set([]));
    } finally {
      this.zone.run(() => this.buscando.set(false));
    }
  }

  elegir(s: Sugerencia): void {
    this.form.controls.direccion.setValue(s.nombre.slice(0, 160));
    this.latitud.set(s.lat);
    this.longitud.set(s.lon);
    this.sugerencias.set([]);
    if (this.map) {
      this.map.setView([s.lat, s.lon], 16);
      this.ponerMarcador(s.lat, s.lon);
    }
  }

  /** Fija coordenadas desde un click/arrastre en el mapa y completa la dirección (reverse geocoding). */
  private async fijarUbicacion(lat: number, lng: number, reverse: boolean): Promise<void> {
    this.latitud.set(lat);
    this.longitud.set(lng);
    this.ponerMarcador(lat, lng);
    if (!reverse) return;
    try {
      const url = 'https://nominatim.openstreetmap.org/reverse?format=jsonv2'
        + '&lat=' + lat + '&lon=' + lng + '&accept-language=es';
      const res = await fetch(url);
      const d = await res.json();
      if (d?.display_name) {
        this.zone.run(() => this.form.controls.direccion.setValue(String(d.display_name).slice(0, 160)));
      }
    } catch { /* si falla el reverse, se conserva la dirección tipeada */ }
  }

  soloNumeros(): void {
    const c = this.form.controls.telefono;
    const limpio = (c.value ?? '').replace(/\D/g, '');
    if (limpio !== c.value) c.setValue(limpio, { emitEvent: false });
  }

  onSubmit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }

    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form.value;
    const request: SucursalRequest = {
      nombre: v.nombre!,
      direccion: v.direccion || null,
      latitud: this.latitud(),
      longitud: this.longitud(),
      telefono: v.telefono || null
    };

    const peticion = this.esEdicion()
      ? this.sucursalService.actualizar(this.sucursalId!, request)
      : this.sucursalService.crear(request);

    peticion.subscribe({
      next: () => this.router.navigate(['/app/sucursales']),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo guardar la sucursal');
      }
    });
  }
}
