import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  // LOGIN
  correo = '';
  contrasena = '';
  errorLogin = '';
  cargandoLogin = false;
  verContrasena = false;

  // REGISTRO MODAL
  modalAbierto = false;
  regNombre = '';
  regCorreo = '';
  regPassword = '';
  regConfirmPassword = '';
  regDni = '';
  regTelefono = '';
  errorRegistro = '';
  cargandoRegistro = false;
  registroExitoso = false;
  verPasswordReg = false;
  errores: any = {};

  constructor(private auth: AuthService, private router: Router) {}

  // ── LOGIN ──
iniciarSesion() {
    this.errorLogin = '';
    if (!this.correo) { this.errorLogin = 'El correo es obligatorio'; return; }
    if (!this.contrasena) { this.errorLogin = 'La contraseña es obligatoria'; return; }
    if (!this.validarEmail(this.correo)) { this.errorLogin = 'El correo no es válido'; return; }

    this.cargandoLogin = true;
    this.auth.login(this.correo, this.contrasena).subscribe({
      next: (res: any) => {
        if (res.success) {
          if (res.rol === 'ADMIN') {
            this.autoLoginAdmin();
          } else {
            this.auth.guardarSesion(res);
            this.router.navigate(['/catalogo']);
          }
        } else {
          this.errorLogin = res.message || 'Credenciales incorrectas';
        }
        this.cargandoLogin = false;
      },
      error: () => {
        this.errorLogin = 'Correo o contraseña incorrectos';
        this.cargandoLogin = false;
      }
    });
  }

  autoLoginAdmin() {
    // Crea un formulario invisible y lo envía por POST a Spring Boot
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = 'http://localhost:8080/entrepaginas/acceder';

    const inputCorreo = document.createElement('input');
    inputCorreo.type = 'hidden';
    inputCorreo.name = 'correo';
    inputCorreo.value = this.correo;

    const inputPass = document.createElement('input');
    inputPass.type = 'hidden';
    inputPass.name = 'contrasena';
    inputPass.value = this.contrasena;

    form.appendChild(inputCorreo);
    form.appendChild(inputPass);
    document.body.appendChild(form);
    form.submit();
  }

  // ── MODAL ──
  abrirModal() {
    this.modalAbierto = true;
    this.limpiarRegistro();
  }

  cerrarModal() {
    this.modalAbierto = false;
    this.limpiarRegistro();
  }

  limpiarRegistro() {
    this.regNombre = ''; this.regCorreo = ''; this.regPassword = '';
    this.regConfirmPassword = ''; this.regDni = ''; this.regTelefono = '';
    this.errorRegistro = ''; this.errores = {};
    this.registroExitoso = false; this.cargandoRegistro = false;
  }

  irAlLogin() {
    this.cerrarModal();
    // El login ya está en esta página, solo cerramos el modal
  }

  // ── VALIDACIONES EN TIEMPO REAL ──
  validarCampo(campo: string) {
    switch(campo) {
      case 'nombre':
        this.errores.nombre = this.regNombre.trim().length < 3
          ? 'El nombre debe tener al menos 3 caracteres' : '';
        break;
      case 'correo':
        this.errores.correo = !this.validarEmail(this.regCorreo)
          ? 'El correo no es válido' : '';
        break;
      case 'password':
        this.errores.password = this.regPassword.length < 6
          ? 'La contraseña debe tener al menos 6 caracteres' : '';
        if (this.regConfirmPassword) this.validarCampo('confirmPassword');
        break;
      case 'confirmPassword':
        this.errores.confirmPassword = this.regPassword !== this.regConfirmPassword
          ? 'Las contraseñas no coinciden' : '';
        break;
      case 'dni':
        this.errores.dni = !/^\d{8}$/.test(this.regDni)
          ? 'El DNI debe tener exactamente 8 dígitos' : '';
        break;
      case 'telefono':
        this.errores.telefono = !/^\d{9}$/.test(this.regTelefono)
          ? 'El teléfono debe tener 9 dígitos' : '';
        break;
    }
  }

  validarEmail(email: string): boolean {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
  }

  formularioValido(): boolean {
    return this.regNombre.trim().length >= 3 &&
           this.validarEmail(this.regCorreo) &&
           this.regPassword.length >= 6 &&
           this.regPassword === this.regConfirmPassword &&
           /^\d{8}$/.test(this.regDni) &&
           /^\d{9}$/.test(this.regTelefono);
  }

  registrar() {
    this.validarCampo('nombre');
    this.validarCampo('correo');
    this.validarCampo('password');
    this.validarCampo('confirmPassword');
    this.validarCampo('dni');
    this.validarCampo('telefono');

    if (!this.formularioValido()) {
      this.errorRegistro = 'Por favor corrige los errores antes de continuar';
      return;
    }

    this.cargandoRegistro = true;
    this.errorRegistro = '';

    this.auth.registro(this.regCorreo, this.regPassword, this.regNombre, this.regDni, this.regTelefono).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.registroExitoso = true;
        } else {
          this.errorRegistro = res.message || 'Error al crear la cuenta';
        }
        this.cargandoRegistro = false;
      },
      error: () => {
        this.errorRegistro = 'Error de conexión con el servidor';
        this.cargandoRegistro = false;
      }
    });
  }
}