import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { LibroService } from '../../services/libro';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-catalogo',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.css'
})
export class Catalogo implements OnInit {
  populares: any[] = [];
  recientes: any[] = [];
  todos: any[] = [];
  resultadosBusqueda: any[] = [];
  busqueda = '';
  buscando = false;

  // Modal libro
  libroSeleccionado: any = null;
  modalLibroAbierto = false;

  // Modal préstamo
  modalPrestamoAbierto = false;
  fechaDevolucion = '';
  errorPrestamo = '';
  exitoPrestamo = false;
  cargandoPrestamo = false;
  fechaMinima = '';

  usuario: any = null;

  constructor(
    private libroService: LibroService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.usuario = this.auth.obtenerSesion();
    if (!this.usuario) {
      this.router.navigate(['/login']);
      return;
    }

    const hoy = new Date();
    hoy.setDate(hoy.getDate() + 3);
    this.fechaMinima = hoy.toISOString().split('T')[0];

    this.libroService.obtenerPopulares().subscribe({
      next: (data) => this.populares = data,
      error: () => {}
    });

    this.libroService.obtenerRecientes().subscribe({
      next: (data) => this.recientes = data,
      error: () => {}
    });

    this.libroService.obtenerTodos().subscribe({
      next: (data) => this.todos = data,
      error: () => {}
    });
  }

  buscarLibros() {
    if (!this.busqueda.trim()) {
      this.resultadosBusqueda = [];
      this.buscando = false;
      return;
    }
    this.buscando = true;
    this.libroService.buscar(this.busqueda).subscribe({
      next: (data) => this.resultadosBusqueda = data,
      error: () => {}
    });
  }

  limpiarBusqueda() {
    this.busqueda = '';
    this.resultadosBusqueda = [];
    this.buscando = false;
  }

  abrirLibro(libro: any) {
    this.libroSeleccionado = libro;
    this.modalLibroAbierto = true;
  }

  cerrarModalLibro() {
    this.modalLibroAbierto = false;
    this.libroSeleccionado = null;
  }

  abrirSolicitud() {
    this.modalLibroAbierto = false;
    this.modalPrestamoAbierto = true;
    this.fechaDevolucion = '';
    this.errorPrestamo = '';
    this.exitoPrestamo = false;
  }

  cerrarModalPrestamo() {
    this.modalPrestamoAbierto = false;
    this.exitoPrestamo = false;
    this.errorPrestamo = '';
  }

  solicitarPrestamo() {
    if (!this.fechaDevolucion) {
      this.errorPrestamo = 'Selecciona una fecha de devolución';
      return;
    }

    this.cargandoPrestamo = true;
    this.errorPrestamo = '';

    const datos = {
      libroId: this.libroSeleccionado.id,
      correo: this.usuario.correo,
      fechaDevolucion: this.fechaDevolucion
    };

    this.libroService.solicitarPrestamo(datos).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.exitoPrestamo = true;
        } else {
          this.errorPrestamo = res.message;
        }
        this.cargandoPrestamo = false;
      },
      error: () => {
        this.errorPrestamo = 'Error de conexión';
        this.cargandoPrestamo = false;
      }
    });
  }

  getImagen(libro: any): string {
    return libro.imagen || 'https://via.placeholder.com/300x400?text=Sin+imagen';
  }

  cerrarSesion() {
    this.auth.cerrarSesion();
    this.router.navigate(['/login']);
  }
}