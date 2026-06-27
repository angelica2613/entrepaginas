import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class LibroService {
  private apiUrl = 'http://localhost:8080/entrepaginas/api';

  constructor(private http: HttpClient) {}

  obtenerTodos(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/libros`);
  }

  obtenerPopulares(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/libros/populares`);
  }

  obtenerRecientes(): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/libros/recientes`);
  }

  buscar(q: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/libros/buscar?q=${q}`);
  }

  solicitarPrestamo(datos: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/auth/prestamo/solicitar`, datos);
  }
}