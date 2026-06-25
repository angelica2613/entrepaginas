import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  correo = '';
  contrasena = '';
  error = '';
  cargando = false;

  constructor(private auth: AuthService, private router: Router) {}

  iniciarSesion() {
    this.error = '';
    this.cargando = true;

    this.auth.login(this.correo, this.contrasena).subscribe({
      next: (res: any) => {
        if (res.success) {
          this.auth.guardarSesion(res);
          if (res.rol === 'ADMIN') {
            this.router.navigate(['/dashboard']);
          } else {
            this.router.navigate(['/catalogo']);
          }
        } else {
          this.error = res.message;
        }
        this.cargando = false;
      },
      error: () => {
        this.error = 'Correo o contraseña incorrectos';
        this.cargando = false;
      }
    });
  }
}