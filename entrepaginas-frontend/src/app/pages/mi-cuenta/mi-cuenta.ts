import { Component, OnInit, ViewEncapsulation} from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';
import { MiCuentaService } from '../../services/mi-cuenta';

@Component({
  selector: 'app-mi-cuenta',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mi-cuenta.html',
  styleUrl: './mi-cuenta.css',
  encapsulation: ViewEncapsulation.None
})
export class MiCuenta implements OnInit {
  usuario: any = null;
  perfil: any = null;
  prestamos: any[] = [];
  ventas: any[] = [];
  cargando = true;
  editando = false;
  guardando = false;
  mensajeExito = '';
  
  mensajeError = '';

  // Campos vista
  nombre = '';
  dni = '';
  telefono = '';
  direccion = '';

  // Campos edición (copia temporal)
  editNombre = '';
  editDni = '';
  editTelefono = '';
  editDireccion = '';
  errores: any = {};

  constructor(
    private auth: AuthService,
    private miCuentaService: MiCuentaService,
    private router: Router
  ) {}

  ngOnInit() {
    this.usuario = this.auth.obtenerSesion();
    if (!this.usuario) { this.router.navigate(['/login']); return; }
    this.cargarPerfil();
  }

  cargarPerfil() {
    this.cargando = true;
    this.miCuentaService.obtenerPerfil(this.usuario.id).subscribe({
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

  abrirEdicion() {
    this.editNombre = this.nombre;
    this.editDni = this.dni;
    this.editTelefono = this.telefono;
    this.editDireccion = this.direccion;
    this.errores = {};
    this.mensajeError = '';
    this.editando = true;
  }

  cancelarEdicion() {
    this.editando = false;
    this.errores = {};
    this.mensajeError = '';
  }

  validarCampo(campo: string) {
    switch(campo) {
      case 'nombre':
        this.errores.nombre = this.editNombre.trim().length < 3 ? 'Mínimo 3 caracteres' : '';
        break;
      case 'dni':
        this.errores.dni = this.editDni && !/^\d{8}$/.test(this.editDni) ? 'El DNI debe tener 8 dígitos' : '';
        break;
      case 'telefono':
        this.errores.telefono = this.editTelefono && !/^\d{9}$/.test(this.editTelefono) ? 'El teléfono debe tener 9 dígitos' : '';
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
    this.mensajeError = '';

    this.miCuentaService.actualizar({
      correo: this.usuario.correo,
      nombre: this.editNombre,
      dni: this.editDni,
      telefono: this.editTelefono,
      direccion: this.editDireccion
    }).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.nombre = this.editNombre;
          this.dni = this.editDni;
          this.telefono = this.editTelefono;
          this.direccion = this.editDireccion;
          this.editando = false;
          this.mensajeExito = '¡Perfil actualizado!';
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