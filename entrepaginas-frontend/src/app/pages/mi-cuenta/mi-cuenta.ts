import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MiCuentaService } from '../../services/mi-cuenta';

@Component({
  selector: 'app-mi-cuenta',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.css'
})
export class MiCuenta implements OnInit {
  usuario: any = null;
  perfil: any = null;
  prestamos: any[] = [];
  ventas: any[] = [];
  cargando = true;
  guardando = false;
  mensajeExito = '';
  mensajeError = '';

  // Campos editables
  nombre = '';
  dni = '';
  telefono = '';
  direccion = '';

  // Errores validación
  errores: any = {};

  constructor(
    private auth: AuthService,
    private miCuentaService: MiCuentaService,
    private router: Router
  ) {}

  ngOnInit() {
    this.usuario = this.auth.obtenerSesion();
    if (!this.usuario) { this.router.navigate(['/login']); return; }

    this.miCuentaService.obtenerPerfil(this.usuario.correo).subscribe({
      next: (data: any) => {
        this.perfil = data;
        this.nombre = data.nombre || '';
        this.dni = data.dni || '';
        this.telefono = data.telefono || '';
        this.direccion = data.direccion || '';
        this.prestamos = data.prestamos || [];
        this.ventas = data.ventas || [];
        this.cargando = false;
      },
      error: () => { this.cargando = false; }
    });
  }

  validarCampo(campo: string) {
    switch(campo) {
      case 'nombre':
        this.errores.nombre = this.nombre.trim().length < 3 ? 'Mínimo 3 caracteres' : '';
        break;
      case 'dni':
        this.errores.dni = this.dni && !/^\d{8}$/.test(this.dni) ? 'El DNI debe tener 8 dígitos' : '';
        break;
      case 'telefono':
        this.errores.telefono = this.telefono && !/^\d{9}$/.test(this.telefono) ? 'El teléfono debe tener 9 dígitos' : '';
        break;
    }
  }

  guardar() {
    this.validarCampo('nombre');
    this.validarCampo('dni');
    this.validarCampo('telefono');

    if (Object.values(this.errores).some(e => e)) {
      this.mensajeError = 'Corrige los errores antes de guardar';
      return;
    }

    this.guardando = true;
    this.mensajeExito = '';
    this.mensajeError = '';

    this.miCuentaService.actualizar({
      correo: this.usuario.correo,
      nombre: this.nombre,
      dni: this.dni,
      telefono: this.telefono,
      direccion: this.direccion
    }).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.mensajeExito = '¡Perfil actualizado correctamente!';
          setTimeout(() => this.mensajeExito = '', 3000);
        } else {
          this.mensajeError = res.message || 'Error al guardar';
        }
        this.guardando = false;
      },
      error: () => {
        this.mensajeError = 'Error de conexión';
        this.guardando = false;
      }
    });
  }

  prestamosActivos() { return this.prestamos.filter(p => p.activo); }
  prestamosDevueltos() { return this.prestamos.filter(p => !p.activo); }

  estaVencido(p: any): boolean {
    return p.fechaDevolucion && new Date(p.fechaDevolucion) < new Date();
  }

  vencePronto(p: any): boolean {
    if (!p.fechaDevolucion) return false;
    const fecha = new Date(p.fechaDevolucion);
    const en3dias = new Date();
    en3dias.setDate(en3dias.getDate() + 3);
    return fecha <= en3dias && fecha >= new Date();
  }

  cerrarSesion() {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}