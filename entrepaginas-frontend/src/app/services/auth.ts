import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/entrepaginas/api/auth';

  constructor(private http: HttpClient) {}

  login(correo: string, contrasena: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { correo, contrasena });
  }

  registro(correo: string, contrasena: string, nombre: string, dni: string, telefono: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/registro`, { correo, contrasena, nombre, dni, telefono });
  }

  guardarSesion(usuario: any) {
    localStorage.setItem('usuario', JSON.stringify(usuario));
  }

  obtenerSesion() {
    const u = localStorage.getItem('usuario');
    return u ? JSON.parse(u) : null;
  }

  cerrarSesion() {
    localStorage.removeItem('usuario');
  }

  estaLogueado(): boolean {
    return this.obtenerSesion() !== null;
  }

  esAdmin(): boolean {
    const u = this.obtenerSesion();
    return u && u.rol === 'ADMIN';
  }
}