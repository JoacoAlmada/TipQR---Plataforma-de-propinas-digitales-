import {
  Component, ElementRef, NgZone, AfterViewInit, DestroyRef,
  PLATFORM_ID, inject, signal, viewChild
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { interval, switchMap, Subscription } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RegistroService, TipoDocumento } from '../../../core/services/registro.service';

declare const grecaptcha: any;
const RECAPTCHA_SITE_KEY = '6LfzvQstAAAAAGnQzbE15GLLRj1JL_cFdbgXacvN';

@Component({
  selector: 'app-registro',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registro.component.html',
  styleUrl: './registro.component.css'
})
export class RegistroComponent implements AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly registroService = inject(RegistroService);
  private readonly router = inject(Router);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);
  private readonly platformId = inject(PLATFORM_ID);

  readonly captchaRef = viewChild<ElementRef<HTMLDivElement>>('captcha');

  paso = signal(1);
  registroToken = signal<string | null>(null);
  verificacionPendiente = signal(false);
  emailVerificado = signal(false);
  loading = signal(false);
  errorMsg = signal('');
  finalizado = signal(false);

  captchaToken = signal<string | null>(null);
  private widgetId: number | null = null;

  docsSubidos = signal<TipoDocumento[]>([]);
  subiendoDoc = signal<string | null>(null);
  private pollingSub?: Subscription;

  readonly tiposDoc: { tipo: TipoDocumento; label: string; accept: string; hint: string }[] = [
    { tipo: 'DNI_FRENTE', label: 'DNI — Frente', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'DNI_DORSO', label: 'DNI — Dorso', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'SELFIE', label: 'Selfie con tu DNI', accept: 'image/png,image/jpeg', hint: 'Imagen JPG o PNG' },
    { tipo: 'CONSTANCIA_AFIP', label: 'Constancia de AFIP', accept: 'application/pdf', hint: 'Archivo PDF' }
  ];

  form1 = this.fb.group({
    nombre:   ['', [Validators.required, Validators.maxLength(60)]],
    apellido: ['', [Validators.required, Validators.maxLength(60)]],
    email:    ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    telefono: ['', [Validators.required, Validators.maxLength(30)]],
    cuit:     ['', [Validators.required, Validators.pattern(/^\d{2}-?\d{8}-?\d{1}$/)]],
    dni:      ['', [Validators.required, Validators.pattern(/^\d{7,9}$/)]]
  });

  form2 = this.fb.group({
    nombreEmpresa:  ['', [Validators.required, Validators.maxLength(120)]],
    nombreFantasia: ['', Validators.maxLength(120)],
    provincia:      ['', Validators.required],
    calle:          ['', Validators.required],
    numeracion:     ['', Validators.required],
    cuit:           ['', Validators.pattern(/^$|^\d{2}-?\d{8}-?\d{1}$/)],
    rubro:          ['', Validators.required]
  });

  get f1() { return this.form1.controls; }
  get f2() { return this.form2.controls; }

  ngAfterViewInit(): void {
    if (!isPlatformBrowser(this.platformId)) return;
    this.cargarRecaptcha().then(() => this.renderCaptcha());
  }

  // ── reCAPTCHA ──────────────────────────────────────
  private cargarRecaptcha(): Promise<void> {
    return new Promise((resolve) => {
      if ((window as any).grecaptcha?.render) { resolve(); return; }
      const existente = document.getElementById('recaptcha-script');
      const esperar = () => {
        if ((window as any).grecaptcha?.render) resolve();
        else setTimeout(esperar, 120);
      };
      if (existente) { esperar(); return; }
      const s = document.createElement('script');
      s.id = 'recaptcha-script';
      s.src = 'https://www.google.com/recaptcha/api.js?render=explicit';
      s.async = true; s.defer = true;
      s.onload = esperar;
      document.head.appendChild(s);
    });
  }

  private renderCaptcha(): void {
    const el = this.captchaRef()?.nativeElement;
    if (!el || this.widgetId !== null) return;
    this.widgetId = grecaptcha.render(el, {
      sitekey: RECAPTCHA_SITE_KEY,
      callback: (token: string) => this.zone.run(() => this.captchaToken.set(token)),
      'expired-callback': () => this.zone.run(() => this.captchaToken.set(null))
    });
  }

  private resetCaptcha(): void {
    if (this.widgetId !== null && (window as any).grecaptcha) {
      grecaptcha.reset(this.widgetId);
    }
    this.captchaToken.set(null);
  }

  // ── Paso 1 ─────────────────────────────────────────
  enviarPaso1(): void {
    if (this.form1.invalid) { this.form1.markAllAsTouched(); return; }
    if (!this.captchaToken()) { this.errorMsg.set('Confirmá que no sos un robot.'); return; }

    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form1.value;

    this.registroService.paso1({
      nombre: v.nombre!, apellido: v.apellido!, email: v.email!, password: v.password!,
      telefono: v.telefono!, cuit: v.cuit!, dni: v.dni!, captchaToken: this.captchaToken()!
    }).subscribe({
      next: (est) => {
        this.registroToken.set(est.registroToken);
        this.verificacionPendiente.set(true);
        this.loading.set(false);
        this.iniciarPolling();
      },
      error: (err) => {
        this.loading.set(false);
        this.resetCaptcha();
        this.errorMsg.set(err?.error?.error ?? 'No se pudo crear la cuenta.');
      }
    });
  }

  private iniciarPolling(): void {
    this.pollingSub = interval(4000).pipe(
      switchMap(() => this.registroService.estado(this.registroToken()!)),
      takeUntilDestroyed(this.destroyRef)
    ).subscribe({
      next: (est) => { if (est.emailVerificado) this.emailQuedoVerificado(); }
    });
  }

  chequearVerificacion(): void {
    if (!this.registroToken()) return;
    this.registroService.estado(this.registroToken()!).subscribe({
      next: (est) => {
        if (est.emailVerificado) this.emailQuedoVerificado();
        else this.errorMsg.set('Todavía no detectamos la verificación. Revisá tu email.');
      }
    });
  }

  private emailQuedoVerificado(): void {
    this.pollingSub?.unsubscribe();
    this.emailVerificado.set(true);
    this.verificacionPendiente.set(false);
    this.errorMsg.set('');
    this.paso.set(2);
  }

  // ── Paso 2 ─────────────────────────────────────────
  enviarPaso2(): void {
    if (this.form2.invalid) { this.form2.markAllAsTouched(); return; }
    this.loading.set(true);
    this.errorMsg.set('');
    const v = this.form2.value;

    this.registroService.paso2(this.registroToken()!, {
      nombreEmpresa: v.nombreEmpresa!, nombreFantasia: v.nombreFantasia || null,
      provincia: v.provincia!, calle: v.calle!, numeracion: v.numeracion!,
      cuit: v.cuit || null, rubro: v.rubro!
    }).subscribe({
      next: () => { this.loading.set(false); this.paso.set(3); },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudieron guardar los datos del comercio.');
      }
    });
  }

  // ── Paso 3 ─────────────────────────────────────────
  onArchivo(tipo: TipoDocumento, event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.subiendoDoc.set(tipo);
    this.errorMsg.set('');
    this.registroService.subirDocumento(this.registroToken()!, tipo, file).subscribe({
      next: () => {
        this.subiendoDoc.set(null);
        if (!this.docsSubidos().includes(tipo)) {
          this.docsSubidos.update(list => [...list, tipo]);
        }
      },
      error: (err) => {
        this.subiendoDoc.set(null);
        input.value = '';
        this.errorMsg.set(err?.error?.error ?? 'No se pudo subir el archivo.');
      }
    });
  }

  estaSubido(tipo: TipoDocumento): boolean {
    return this.docsSubidos().includes(tipo);
  }

  get todosLosDocs(): boolean {
    return this.docsSubidos().length === this.tiposDoc.length;
  }

  finalizar(): void {
    this.loading.set(true);
    this.errorMsg.set('');
    this.registroService.finalizar(this.registroToken()!).subscribe({
      next: () => { this.loading.set(false); this.finalizado.set(true); },
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err?.error?.error ?? 'No se pudo finalizar el registro.');
      }
    });
  }

  irAlLogin(): void {
    this.router.navigate(['/login']);
  }
}
