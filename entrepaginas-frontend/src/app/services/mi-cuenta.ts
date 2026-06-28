import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class MiCuentaService {
  private api = 'http://localhost:8080/entrepaginas/api/mi-cuenta';

  constructor(private http: HttpClient) {}

  obtenerPerfil(correo: string): Observable<any> {
    return this.http.get(`${this.api}/${correo}`);
  }

  actualizar(datos: any): Observable<any> {
    return this.http.put(`${this.api}/actualizar`, datos);
  }
}